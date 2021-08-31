package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BpmnInfraConfiguration {

    public String getAuth() {
        return auth;
    }

    @Value("${mso.adapters.requestDb.auth}")
    private String auth;

}
