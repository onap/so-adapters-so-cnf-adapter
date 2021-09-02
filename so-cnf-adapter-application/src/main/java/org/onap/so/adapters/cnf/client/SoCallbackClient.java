package org.onap.so.adapters.cnf.client;

import com.google.gson.Gson;
import org.onap.so.adapters.cnf.BasicSecurityConfig;
import org.onap.so.security.SoUserCredentialConfiguration;
import org.onap.so.security.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.POST;

@Component
public class SoCallbackClient {

    private final static Gson gson = new Gson();

    private final RestTemplate restTemplate;
    private final BasicSecurityConfig userCredentialConfiguration;
    private final String role = "ACTUATOR";
    private final UserCredentials credentials;

    @Autowired
    public SoCallbackClient(RestTemplate restTemplate, BasicSecurityConfig userCredentialConfiguration) {
        this.restTemplate = restTemplate;
        this.userCredentialConfiguration = userCredentialConfiguration;
        if (!userCredentialConfiguration.getRoles().contains(role))
            throw new RuntimeException("Missing authentication role: " + role);
        credentials = userCredentialConfiguration.getUsercredentials().stream().filter(
                creds -> role.equals(creds.getRole())).findAny().orElse(null);
    }

    public ResponseEntity<String> sendPostCallback(String url, Object body) {
        return restTemplate.exchange(url, POST, httpEntity(body), String.class);
    }

    private HttpEntity<?> httpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.TEXT_PLAIN);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBasicAuth(credentials.getUsername(), credentials.getPassword());

        return new HttpEntity<>(gson.toJson(body), headers);
    }
}
