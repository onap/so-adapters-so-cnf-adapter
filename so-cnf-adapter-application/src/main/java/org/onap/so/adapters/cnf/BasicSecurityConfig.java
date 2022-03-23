package org.onap.so.adapters.cnf;

import org.onap.so.security.UserCredentials;
import org.onap.so.security.UserDetailsServiceImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(
        prefix = "spring.security"
)
public class BasicSecurityConfig {
    private List<UserCredentials> credentials = new ArrayList<UserCredentials>();
    private final List<String> roles = new ArrayList<String>();

    public BasicSecurityConfig() {
    }

    public List<String> getRoles() {
        return this.roles;
    }

    @PostConstruct
    private void addRoles() {
        for(int i = 0; i < this.credentials.size(); ++i) {
            this.roles.add(((UserCredentials)this.credentials.get(i)).getRole());
        }

    }

    public List<UserCredentials> getUsercredentials() {
        return this.credentials;
    }

    public void setUsercredentials(final List<UserCredentials> usercredentials) {
        if (usercredentials != null) {
            this.credentials = usercredentials;
        }

    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
