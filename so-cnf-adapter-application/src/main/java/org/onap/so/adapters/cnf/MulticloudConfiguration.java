package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MulticloudConfiguration {

    @Value("${multicloud.protocol}")
    private String protocol;
    @Value("${multicloud.host}")
    private String host;
    @Value("${multicloud.port}")
    private Integer port;

    public String getMulticloudUrl() {
        return protocol + "://" + host + ":" + port;
    }
}
