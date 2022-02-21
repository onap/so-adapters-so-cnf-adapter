package org.onap.so.adapters.cnf.service.synchrornization;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.rest.SubscriptionEndpointService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SynchronizationService {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizationService.class);

    private final SubscriptionEndpointService subscriptionEndpointService;
    private final MulticloudClient multicloudClient;

    public SynchronizationService(SubscriptionEndpointService subscriptionEndpointService,
                                  MulticloudClient multicloudClient) {
        this.subscriptionEndpointService = subscriptionEndpointService;
        this.multicloudClient = multicloudClient;
    }

    public void createSubscription(String instanceId) throws BadResponseException {
        String callbackEndpoint = subscriptionEndpointService.generateCallbackEndpoint(instanceId);
        String endpointPath = subscriptionEndpointService.generateEndpointPath(instanceId);

        subscriptionEndpointService.enableEndpoint(endpointPath);
        String name = UUID.randomUUID().toString();
        SubscriptionRequest subscriptionRequest = getSubscriptionRequest(name, callbackEndpoint);
        multicloudClient.registerSubscription(instanceId, subscriptionRequest);
    }

    public void deleteSubscription(String instanceId) throws BadResponseException {
        String endpointPath = subscriptionEndpointService.generateEndpointPath(instanceId);
        subscriptionEndpointService.disableEndpoint(endpointPath);

        // TODO: Hardcoded subId
        multicloudClient.deleteSubscription(instanceId, "subscriptionId");
    }


    private SubscriptionRequest getSubscriptionRequest(String name, String endpoint) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setName(name);
        subscriptionRequest.setCallbackUrl(endpoint);
        subscriptionRequest.setMinNotifyInterval(30);
        // FIXME: change this paameter
        subscriptionRequest.setMetadata(null);

        return subscriptionRequest;
    }
}
