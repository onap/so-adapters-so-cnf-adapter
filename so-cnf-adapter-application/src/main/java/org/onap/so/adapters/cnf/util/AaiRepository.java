/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.cnf.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAITransactionalClient;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.generated.fluentbuilders.K8sResource;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.KubernetesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AaiRepository implements IAaiRepository {
    private final static Logger logger = LoggerFactory.getLogger(IAaiRepository.class);
    private final static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final AAIResourcesClient aaiClient;
    private final AAITransactionalClient transaction;

    public static IAaiRepository instance() {
        return new AaiRepository();
    }

    private AaiRepository() {
        aaiClient = new AAIResourcesClient(AAIVersion.LATEST);
        transaction = aaiClient.beginTransaction();
    }

    @Override
    public void commit(boolean dryrun) {
        try {
            transaction.execute(dryrun);
        } catch (BulkProcessFailed bulkProcessFailed) {
            throw new RuntimeException("Failed to exectute transaction", bulkProcessFailed);
        }
    }

    @Override
    public void update(KubernetesResource resource, AaiRequest aaiRequest) {
        logger.info("updating AAI with resource {} and request {}", resource, aaiRequest);

        K8sResource k8sResource = AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                .tenant(aaiRequest.getTenantId())
                .k8sResource(resource.getId());

        AAIResourceUri k8sResourceUri =
                AAIUriFactory.createResourceUri(k8sResource.build(), aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion(), aaiRequest.getTenantId(), resource.getId());

        String payload = gson.toJson(resource);
        logger.debug("K8s resource URI: " + k8sResourceUri + " with payload [" + payload + "]");
        transaction.createIfNotExists(k8sResourceUri, Optional.of(payload));
        // add edge from vf module to k8s resource
        final String genericVnfId = null;//aaiRequest.getGenericVnfId();
        final String vfModuleId = aaiRequest.getVfModuleId();

        if (genericVnfId == null || vfModuleId == null) {
            logger.debug("No genericVnfId or vfModuleId to create relations for payload [\" + payload + \"]\");");
            return;
        }

        var vfModuleUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId).vfModule(vfModuleId));
        var instance = aaiClient.get(vfModuleUri);

        var foundUri = instance.getRelationships().get().getRelatedUris(Types.K8S_RESOURCE)
                .stream().filter(resourceUri -> resourceUri
                        .getURIKeys()
                        .get("k8s_resource-id")
                        .startsWith(resource.getId()))
                .findFirst();

        transaction.connect(foundUri.get(), k8sResourceUri);

        var genericVnfUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId));
        instance = aaiClient.get(genericVnfUri);

        foundUri = instance.getRelationships().get().getRelatedUris(Types.K8S_RESOURCE)
                .stream().filter(resourceUri -> resourceUri
                        .getURIKeys()
                        .get("k8s_resource-id")  // FIXME double check names
                        .startsWith(resource.getId()))
                .findFirst();

        transaction.connect(foundUri.get(), k8sResourceUri);
    }

    @Override
    public void delete(KubernetesResource resource, AaiRequest aaiRequest) {
        logger.info("deleting from AAI resource {}", aaiRequest);
        // dge from vf module to k8s resource
        final String genericVnfId = aaiRequest.getGenericVnfId();
        final String vfModuleId = aaiRequest.getVfModuleId();

        var vfModuleUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId).vfModule(vfModuleId));
        var instance = aaiClient.get(vfModuleUri);

        if (instance.hasRelationshipsTo(Types.K8S_RESOURCE)) {
            List<KubernetesResource> resources = instance.getRelationships().get().getByType(Types.K8S_RESOURCE)
                    .stream()
                    .map(r -> r.asBean(KubernetesResource.class))
                    .filter(r -> r.get().getLabels().get(1).equals(resource.getLabels().get(1)))
                    .map(Optional::get)
                    .collect(Collectors.toList());
            resources.stream().map(r -> {
                K8sResource k8sResource = AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                        .tenant(aaiRequest.getTenantId())
                        .k8sResource(r.getId());
                AAIResourceUri k8sResourceUri =
                        AAIUriFactory.createResourceUri(k8sResource.build(), aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion(), aaiRequest.getTenantId(), r.getId());
                return k8sResourceUri;
            }).forEach(uri -> transaction.delete(uri));
        }

    }
}
