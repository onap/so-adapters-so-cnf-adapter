package org.onap.so.adapters.cnf.service.synchrornization;

import org.onap.so.client.exception.BadResponseException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@Primary
public class SubscriptionsRecoveryProvider {

    public Set<String> getInstanceList() throws BadResponseException {
        return Collections.emptySet();
    }
}