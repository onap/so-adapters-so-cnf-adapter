package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AaiConfiguration {

    @Value("${aai.enabled}")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }
}
