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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KubernetesClientProviderImpl implements KubernetesClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesClientProviderImpl.class);
    private static final Map<String, ApiClient> INSTANCES = new ConcurrentHashMap<>();

    @Override
    public ApiClient getApiClient(final String kubeConfigPath) {

        ApiClient client = INSTANCES.get(kubeConfigPath.toString());
        if (client == null) {
            synchronized (this) {
                try (final Reader input = new FileReader(kubeConfigPath);) {
                    logger.debug("{} Loading kube-config file", kubeConfigPath);
                    final KubeConfig kubeConfig = KubeConfig.loadKubeConfig(input);
                    logger.debug("{} kube-config loaded successfully", kubeConfigPath);
                    client = ClientBuilder.kubeconfig(kubeConfig).build();
                    logger.debug("ApiClient created successfully");
                    INSTANCES.put(kubeConfigPath, client);
                } catch (final FileNotFoundException fileNotFoundException) {
                    logger.error("{} KubeConfig not found", kubeConfigPath, fileNotFoundException);
                    throw new KubeConfigFileProcessingException(kubeConfigPath + " kube-config file not found",
                            fileNotFoundException);
                } catch (final Exception exception) {
                    final String message = "Unexpected exception while processing kube-config file";
                    logger.error(message, exception);
                    throw new KubeConfigFileProcessingException(message, exception);
                }
            }
        }
        logger.debug("Found ApiClient for {}", kubeConfigPath);
        return client;
    }

    @Override
    public void closeApiClient(final String kubeConfigFile) {
        final ApiClient client = INSTANCES.get(kubeConfigFile);
        if (client != null) {
            logger.debug("Closing ApiClient and removing it from local cache for {}", kubeConfigFile);
            INSTANCES.remove(kubeConfigFile);
        }
    }

}
