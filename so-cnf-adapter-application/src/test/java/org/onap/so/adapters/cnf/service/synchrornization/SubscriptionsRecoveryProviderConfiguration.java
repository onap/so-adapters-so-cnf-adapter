package org.onap.so.adapters.cnf.service.synchrornization;

import java.util.Collections;
import java.util.Set;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SubscriptionsRecoveryProviderConfiguration {

    @Bean
    @Primary
    public SubscriptionsRecoveryProvider subscriptionsRecoveryProvider() {
        return new SubscriptionsRecoveryProvider(null) {
            @Override
            public Set<String> getInstanceList() throws BadResponseException {
                return Collections.emptySet();
            }
        };
    }

}
