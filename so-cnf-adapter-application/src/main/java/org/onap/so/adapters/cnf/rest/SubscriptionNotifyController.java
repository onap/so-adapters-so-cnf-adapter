package org.onap.so.adapters.cnf.rest;


import com.google.gson.Gson;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.NotificationRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SubscriptionNotifyController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionNotifyController.class);
    private final static Gson gson = new Gson();

    private final SubscriptionEndpointService subscriptionEndpointService;
    private final AaiService aaiService;

    public SubscriptionNotifyController(SubscriptionEndpointService subscriptionEndpointService, AaiService aaiService) {
        this.subscriptionEndpointService = subscriptionEndpointService;
        this.aaiService = aaiService;
    }

    @PostMapping(value = "/{endpoint}")
    public ResponseEntity subscriptionNotifyEndpoint(@PathVariable String endpoint, @RequestBody NotificationRequest body) throws BadResponseException {
        boolean isEndpointActive = subscriptionEndpointService.isEndpointActive(endpoint);
        if (isEndpointActive) {
            AaiRequest aaiRequest = convertMetadataToAaiRequest(body.getMetadata());
            logger.info("AAI UPDATE START");
            Runnable task = () -> {
                try {
                    aaiService.aaiUpdate(aaiRequest);
                } catch (BadResponseException e) {
                    logger.error("Cannot execute aai update", e);
                }
            };
            task.run();
            return ResponseEntity.accepted().build();
        } else {
            logger.warn("endpoint: {} is disabled", endpoint);
            return ResponseEntity.badRequest().body("Cannot handle notification.");
        }
    }

    private AaiRequest convertMetadataToAaiRequest(Map<String, Object> metadata) {
        String json = gson.toJsonTree(metadata)
                .getAsJsonObject()
                .get("metadata")
                .toString();

        return gson.fromJson(json, AaiRequest.class);
    }

}
