package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MulticloudConfiguration {

    @Value("${multicloud.endpoint}")
    private String endpoint;

    public String getMulticloudUrl() {
        return endpoint;
    }
}
