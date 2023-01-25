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
package org.onap.so.cnfm.lcm.bpmn.flows.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileNotFoundException;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class KubConfigProviderImpl implements KubConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(KubConfigProviderImpl.class);

    private final Path kubeConfigsDirPath;

    @Autowired
    public KubConfigProviderImpl(@Value("${cnfm.kube-configs-dir:/app/kube-configs}") final String kubeConfigsDir) {
        this.kubeConfigsDirPath = Paths.get(kubeConfigsDir).toAbsolutePath().normalize();
    }

    @Override
    public Path getKubeConfigFile(final String cloudOwner, final String cloudRegion, final String tenantId)
            throws KubeConfigFileNotFoundException {
        final String filename = getFilename(cloudOwner, cloudRegion, tenantId);
        final Path targetLocation = this.kubeConfigsDirPath.resolve(filename);
        logger.debug("Looking for kube-config file at location {}", targetLocation);

        if (Files.exists(targetLocation)) {
            logger.debug("Found kube-config file at location {}", targetLocation);
            if (Files.isReadable(targetLocation)) {
                return targetLocation;
            }
            throw new KubeConfigFileNotFoundException("kube-config file at " + targetLocation + " is not readable");
        }

        throw new KubeConfigFileNotFoundException("Unable to find kube-config file at " + targetLocation);
    }

    @Override
    public void addKubeConfigFile(final MultipartFile file, final String cloudOwner, final String cloudRegion,
            final String tenantId) {
        final String filename = getFilename(cloudOwner, cloudRegion, tenantId);
        final Path targetLocation = this.kubeConfigsDirPath.resolve(filename);

        try (final InputStream inputStream = file.getInputStream()) {
            logger.debug("Storing kube-config to location {} ", targetLocation);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException ex) {
            throw new KubeConfigFileUploadException(
                    "Could not store kube-config file " + filename + " to location " + targetLocation, ex);
        }

    }

    private String getFilename(final String cloudOwner, final String cloudRegion, final String tenantId) {
        return String.join("-", Arrays.asList(cloudOwner, cloudRegion, tenantId));
    }

}
