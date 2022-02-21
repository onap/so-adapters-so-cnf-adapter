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
        String url = multicloudConfiguration.getMulticloudUrl() + "/v1/instance";
        if (!instanceId.isEmpty()) {
            url += "/" + instanceId;
        }
        return url;
    }

}