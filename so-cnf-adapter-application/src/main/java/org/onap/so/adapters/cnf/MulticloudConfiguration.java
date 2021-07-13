package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "multicloud")
public class MulticloudConfiguration {


    @Value("${protocol}")
    private String protocol;
    @Value("${host}")
    private String host;
    @Value("${protocol}")
    private Integer port;

    public String getMulticloudUrl() {
        return protocol + "://" + host + ":" + port;
    }
}
