package org.onap.so.adapters.cnf.service.synchrornization;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.rest.SubscriptionEndpointService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SynchronizationService {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizationService.class);

    private final Map<String, String> subscriptionInstanceIdMap = new ConcurrentHashMap<>();
    private final SubscriptionEndpointService subscriptionEndpointService;
    private final MulticloudClient multicloudClient;

    public SynchronizationService(SubscriptionEndpointService subscriptionEndpointService,
                                  MulticloudClient multicloudClient) {
        this.subscriptionEndpointService = subscriptionEndpointService;
        this.multicloudClient = multicloudClient;
    }

    public void createSubscription(String instanceId) throws BadResponseException {
        logger.debug("createSubscription- START");
        String callbackEndpoint = subscriptionEndpointService.generateCallbackEndpoint(instanceId);
        String endpointPath = subscriptionEndpointService.generateEndpointPath(instanceId);

        subscriptionEndpointService.enableEndpoint(endpointPath);
        String name = UUID.randomUUID().toString();
        subscriptionInstanceIdMap.put(name, instanceId);
        SubscriptionRequest subscriptionRequest = getSubscriptionRequest(name, callbackEndpoint);
        multicloudClient.registerSubscription(instanceId, subscriptionRequest);
        logger.debug("createSubscription- END");
    }

    public void deleteSubscription(String instanceId) throws BadResponseException {
        logger.debug("deleteSubscription- START");
        String endpointPath = subscriptionEndpointService.generateEndpointPath(instanceId);
        subscriptionEndpointService.disableEndpoint(endpointPath);

        String subscriptionId = subscriptionInstanceIdMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(instanceId))
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow(RuntimeException::new);
        multicloudClient.deleteSubscription(instanceId, subscriptionId);
        logger.debug("deleteSubscription- END");
    }


    private SubscriptionRequest getSubscriptionRequest(String name, String endpoint) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setName(name);
        subscriptionRequest.setCallbackUrl(endpoint);
        subscriptionRequest.setMinNotifyInterval(30);

        return subscriptionRequest;
    }
}
