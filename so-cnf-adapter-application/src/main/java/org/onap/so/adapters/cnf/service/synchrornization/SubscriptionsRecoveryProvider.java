package org.onap.so.adapters.cnf.service.synchrornization;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SubscriptionsRecoveryProvider {

    private final MulticloudClient multicloudClient;

    public SubscriptionsRecoveryProvider(MulticloudClient multicloudClient) {
        this.multicloudClient = multicloudClient;
    }

    public Set<String> getInstanceList() throws BadResponseException {
        Set<String> instanceIds;
        instanceIds = multicloudClient.getAllInstances().stream()
                .map(InstanceResponse::getId)
                .collect(Collectors.toSet());
        return instanceIds;
    }
}
