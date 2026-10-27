package org.onap.so.adapters.cnf.client;

import com.google.gson.Gson;
import org.onap.so.utils.BasicAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final String authorization;

    @Autowired
    public SoCallbackClient(RestTemplate restTemplate, @Value("${so.auth.username:#{null}}") String username,
            @Value("${so.auth.password:#{null}}") String password) {
        this.restTemplate = restTemplate;
        this.authorization = BasicAuth.header(username, password);
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
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        return new HttpEntity<>(gson.toJson(body), headers);
    }
}
