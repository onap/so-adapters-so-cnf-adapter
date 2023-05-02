/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.filter;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.APPLICATION_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.APPLICATION_VERSION_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DEPLOYMENT_ITEMS_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_INVARIANT_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.PROVIDER_PARAM_NAME;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.JsonArray;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.FileNotFoundInCsarException;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class SdcCsarPackageParser {
    private static final String TOCSA_METADATA_FILE_PATH = "TOSCA-Metadata/TOSCA.meta";
    private static final String ENTRY_DEFINITIONS_ENTRY = "Entry-Definitions";

    private static final Logger logger = LoggerFactory.getLogger(SdcCsarPackageParser.class);

    public Map<String, Object> getAsdProperties(final byte[] onapPackage) {

        try (final ByteArrayInputStream stream = new ByteArrayInputStream(onapPackage);
                final ZipInputStream zipInputStreamAsdLocation = new ZipInputStream(stream);
             final ZipInputStream zipInputStreamAsdContent = new ZipInputStream(stream);) {
            final String asdLocation = getAsdLocation(zipInputStreamAsdLocation);
            stream.reset();
            final String onapAsdContent = getFileInZip(zipInputStreamAsdContent, asdLocation).toString();
            logger.debug("ASD CONTENTS: {}", onapAsdContent);
            final JsonObject root = new Gson().toJsonTree(new Yaml().load(onapAsdContent)).getAsJsonObject();

            final JsonObject topologyTemplates = child(root, "topology_template");
            final JsonObject nodeTemplates = child(topologyTemplates, "node_templates");
            for (final JsonObject child : children(nodeTemplates)) {
                final String type = childElement(child, "type").getAsString();
                if ("tosca.nodes.asd".equals(type)) {
                    final JsonObject properties = child(child, "properties");
                    logger.debug("properties: {}", properties);
                    final Map<String, Object> propertiesValues = new HashMap<>();
                    propertiesValues.put(DESCRIPTOR_ID_PARAM_NAME,
                            getStringValue(properties, DESCRIPTOR_ID_PARAM_NAME));
                    propertiesValues.put(DESCRIPTOR_INVARIANT_ID_PARAM_NAME,
                            getStringValue(properties, DESCRIPTOR_INVARIANT_ID_PARAM_NAME));
                    propertiesValues.put(PROVIDER_PARAM_NAME, getStringValue(properties, PROVIDER_PARAM_NAME));
                    propertiesValues.put(APPLICATION_NAME_PARAM_NAME,
                            getStringValue(properties, APPLICATION_NAME_PARAM_NAME));
                    propertiesValues.put(APPLICATION_VERSION_PARAM_NAME,
                            getStringValue(properties, APPLICATION_VERSION_PARAM_NAME));
                    propertiesValues.put(DEPLOYMENT_ITEMS_PARAM_NAME, getDeploymentItems(child));

                    return propertiesValues;

                }
            }


        } catch (final Exception exception) {
            throw new IllegalArgumentException("Unable to parser CSAR package", exception);
        }
        return Collections.emptyMap();
    }

    private String getStringValue(final JsonObject properties, final String key) {
        final JsonElement element = properties.get(key);
        if (element != null && element.isJsonPrimitive()) {
            return element.getAsString();
        }
        logger.warn("'{}' value is not Primitive or null val:{}", key, element != null ? element.toString() : null);
        return null;
    }

    private List<DeploymentItem> getDeploymentItems(final JsonObject child) {
        final List<DeploymentItem> items = new ArrayList<>();

        final JsonObject artifacts = child(child, "artifacts");
        artifacts.keySet().forEach(key -> {
            final JsonObject element = artifacts.getAsJsonObject(key);
            final JsonObject artifactsProperties = child(element, "properties");
            final List<String> lcp = getLifecycleParameters(artifactsProperties);
            items.add(new DeploymentItem().name(key).itemId(getStringValue(artifactsProperties, "itemId"))
                    .file(getStringValue(element, "file"))
                    .deploymentOrder(getStringValue(artifactsProperties, "deployment_order")).lifecycleParameters(lcp));
        });
        return items;
    }

    private List<String> getLifecycleParameters(final JsonObject artifactsProperties) {
        final JsonArray lcParameters = childElement(artifactsProperties, "lifecycle_parameters").getAsJsonArray();
        final List<String> lifecycleParameters = new ArrayList<>();
        if (lcParameters != null) {
            final Iterator<JsonElement> it = lcParameters.iterator();
            while (it.hasNext()) {
                lifecycleParameters.add(it.next().getAsString());
            }
        }
        return lifecycleParameters;
    }

    private String getAsdLocation(final ZipInputStream zipInputStream) throws IOException {

        try (final ByteArrayOutputStream fileContent = getFileInZip(zipInputStream, TOCSA_METADATA_FILE_PATH);) {
            final String toscaMetadata = new String(fileContent.toByteArray());
            if (!toscaMetadata.isEmpty()) {
                final String entry =
                        filter(on("\n").split(toscaMetadata), line -> line.contains(ENTRY_DEFINITIONS_ENTRY)).iterator()
                                .next();
                return entry.replace(ENTRY_DEFINITIONS_ENTRY + ":", "").trim();
            }
            final String message = "Unable to find valid Tosca Path";
            logger.error(message);
            throw new FileNotFoundInCsarException(message);
        }
    }

    public ByteArrayOutputStream getFileInZip(final ZipInputStream zipInputStream, final String path)
            throws IOException {
        ZipEntry zipEntry;
        final Set<String> items = new HashSet<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            items.add(zipEntry.getName());
            if (zipEntry.getName().matches(path)) {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteStreams.copy(zipInputStream, byteArrayOutputStream);
                return byteArrayOutputStream;
            }
        }
        logger.error("Unable to find the {} in archive found: {}", path, items);
        throw new NoSuchElementException("Unable to find the " + path + " in archive found: " + items);
    }

    private JsonObject child(final JsonObject parent, final String name) {
        return childElement(parent, name).getAsJsonObject();
    }

    private JsonElement childElement(final JsonObject parent, final String name) {
        final JsonElement child = parent.get(name);
        if (child == null) {
            final String message = "Missing child " + name;
            logger.error(message);
            throw new PropertyNotFoundException(message);
        }
        return child;
    }

    private Collection<JsonObject> children(final JsonObject parent) {
        final ArrayList<JsonObject> childElements = new ArrayList<>();
        parent.keySet().stream().forEach(childKey -> {
            if (parent.get(childKey).isJsonObject()) {
                childElements.add(parent.get(childKey).getAsJsonObject());
            }
        });
        return childElements;
    }
}
