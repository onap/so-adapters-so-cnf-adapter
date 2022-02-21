package org.onap.so.adapters.cnf.service.synchrornization;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class SubscriptionNameProvider {

    public String generateName() {
        return UUID.randomUUID().toString();
    }

}
