package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BpmnInfraConfiguration {

    private static final String BY_SPACE = " ";

    @Value("${mso.adapters.requestDb.auth}")
    private String auth;

    public String getHeaderName() {
        checkAuthValue();
        return auth.split(BY_SPACE)[0];
    }

    public String getHeaderValue() {
        checkAuthValue();
        return auth.split(BY_SPACE)[1];
    }

    private void checkAuthValue() {
        String[] split = auth.split(BY_SPACE);
        if (split.length != 2) {
            throw new RuntimeException("Wrong auth property value");
        }
    }
}
