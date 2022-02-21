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
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SynchronizationService {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizationService.class);

    private static final String PROTOCOL = "http";
    private static final String HOST = "so-cnf-adapter";
    private static final int PORT = 8090;

    private final Set<String> subscriptions = new HashSet<>();
    private final MulticloudClient multicloudClient;
    private final SubscriptionsRecoveryProvider recoveryProvider;

    public SynchronizationService(MulticloudClient multicloudClient, SubscriptionsRecoveryProvider recoveryProvider) {
        this.multicloudClient = multicloudClient;
        this.recoveryProvider = recoveryProvider;
    }

    @PostConstruct
    private void postConstruct() {
        recoverSubscriptions();
    }

    private void recoverSubscriptions() {
        if (subscriptions.isEmpty()) {
            Set<String> instanceIds;
            try {

                instanceIds = recoveryProvider.getInstanceList();

                instanceIds.forEach(this::addSubscriptionIfSubscriptionFound);
            } catch (BadResponseException e) {
                logger.error("Instances not found", e);
            }
        }
    }

    private void addSubscriptionIfSubscriptionFound(String instanceId) {
        String subscriptionName = getSubscriptionName(instanceId);
        try {
            if (multicloudClient.hasSubscription(instanceId, subscriptionName))
                subscriptions.add(subscriptionName);
        } catch (BadResponseException e) {
            logger.warn("Subscriptions not found instanceId={} subscriptionName={}", instanceId, subscriptionName);
        }
    }

    public void createSubscriptionIfNotExists(AaiRequest aaiRequest) throws BadResponseException {
        logger.debug("createSubscriptionIfNotExists- START");
        String instanceId = aaiRequest.getInstanceId();
        String subscriptionName = getSubscriptionName(instanceId);
        String callbackUrl = generateCallbackUrl(instanceId);

        if (isSubscriptionActive(subscriptionName)) {
            logger.info("Subscription: {} already exits", subscriptionName);
        } else {
            multicloudClient.registerSubscription(instanceId, getSubscriptionRequest(subscriptionName, callbackUrl, aaiRequest));
            subscriptions.add(subscriptionName);
            logger.info("Subscription: {} registered", subscriptionName);
        }
        logger.debug("createSubscriptionIfNotExists- END");
    }

    public void deleteSubscriptionIfExists(AaiRequest aaiRequest) throws BadResponseException {
        logger.debug("deleteSubscriptionIfExists - START");
        String instanceId = aaiRequest.getInstanceId();
        String subscriptionName = getSubscriptionName(instanceId);

        if (isSubscriptionActive(subscriptionName)) {
            multicloudClient.deleteSubscription(instanceId, subscriptionName);
            subscriptions.remove(subscriptionName);
            logger.info("Subscription: {} removed", subscriptionName);
        } else {
            logger.info("Subscription: {} already removed or was not present", subscriptionName);
        }
        logger.debug("deleteSubscriptionIfExists - END");
    }

    public boolean isSubscriptionActive(String subscriptionName) {
        return subscriptions.contains(subscriptionName);
    }

    public String getSubscriptionName(String instanceId) {
        return instanceId + "-cnf-adapter";
    }

    private SubscriptionRequest getSubscriptionRequest(String name, String endpoint, AaiRequest aaiRequest) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setName(name);
        subscriptionRequest.setCallbackUrl(endpoint);
        subscriptionRequest.setMinNotifyInterval(30);

        return subscriptionRequest;
    }

    private String generateCallbackUrl(String instanceId) {
        String path = String.format("/api/cnf-adapter/v1/instance/%s/status/notify", instanceId);

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
