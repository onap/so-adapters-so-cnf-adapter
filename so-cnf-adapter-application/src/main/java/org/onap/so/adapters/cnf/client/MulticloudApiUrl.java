package org.onap.so.adapters.cnf.client;

import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.springframework.stereotype.Component;

@Component
class MulticloudApiUrl {

    private final MulticloudConfiguration multicloudConfiguration;

    MulticloudApiUrl(MulticloudConfiguration multicloudConfiguration1) {
        this.multicloudConfiguration = multicloudConfiguration1;
    }

    String apiUrl(String instanceId) {
        return multicloudConfiguration.getMulticloudUrl() + "/v1/instance/" + instanceId;
    }

}