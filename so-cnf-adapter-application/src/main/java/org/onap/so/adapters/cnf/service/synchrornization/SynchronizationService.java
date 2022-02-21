/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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
package org.onap.so.adapters.cnf.service.synchrornization;

import org.apache.http.client.utils.URIBuilder;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SynchronizationService {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizationService.class);

    private static final String PROTOCOL = "http";
    private static final String HOST = "so-cnf-adapter";
    private static final int PORT = 8090;

    private final Map<String, String> subscriptionInstanceIdMap = new ConcurrentHashMap<>();
    private final AaiService aaiService;
    private final SubscriptionNameProvider subscriptionNameProvider;
    private final MulticloudClient multicloudClient;

    public SynchronizationService(AaiService aaiService,
                                  SubscriptionNameProvider subscriptionNameProvider,
                                  MulticloudClient multicloudClient) {
        this.aaiService = aaiService;
        this.subscriptionNameProvider = subscriptionNameProvider;
        this.multicloudClient = multicloudClient;
    }

    public void createSubscription(String instanceId, AaiRequest aaiRequest) throws BadResponseException {
        logger.debug("createSubscription- START");
        String name = subscriptionNameProvider.generateName();
        String callbackUrl = generateCallbackUrl(instanceId, name);

        subscriptionInstanceIdMap.put(name, instanceId);
        multicloudClient.registerSubscription(instanceId, getSubscriptionRequest(name, callbackUrl));
        aaiService.aaiUpdate(aaiRequest);
        logger.debug("createSubscription- END");
    }

    public void deleteSubscription(String instanceId, AaiRequest aaiRequest) throws BadResponseException {
        logger.debug("deleteSubscription- START");
        String name = subscriptionInstanceIdMap.entrySet().stream()
                .filter(e -> e.getValue().equals(instanceId))
                .map(Map.Entry::getKey)
                .findFirst().orElseThrow(RuntimeException::new);
        multicloudClient.deleteSubscription(instanceId, name);
        aaiService.aaiDelete(aaiRequest);
        subscriptionInstanceIdMap.remove(name);
        logger.debug("deleteSubscription- END");
    }

    public boolean isSubscriptionActive(String instanceId, String name) {
        return instanceId.equals(subscriptionInstanceIdMap.get(name));
    }

    private SubscriptionRequest getSubscriptionRequest(String name, String endpoint) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setName(name);
        subscriptionRequest.setCallbackUrl(endpoint);
        subscriptionRequest.setMinNotifyInterval(30);

        return subscriptionRequest;
    }

    private String generateCallbackUrl(String instanceId, String name) {
        String path = String.format("/cnf-notify/instanceId/%s/name/%s", instanceId, name);

        URIBuilder uriBuilder = new URIBuilder();

        try {
            return uriBuilder
                    .setScheme(PROTOCOL)
                    .setHost(HOST)
                    .setPort(PORT)
                    .setPath(path)
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
