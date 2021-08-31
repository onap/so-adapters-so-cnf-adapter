package org.onap.so.adapters.cnf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BpmnInfraConfiguration {

    @Value("${so.bpmn.username}")
    private String username;

    @Value("${so.bpmn.password}")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
