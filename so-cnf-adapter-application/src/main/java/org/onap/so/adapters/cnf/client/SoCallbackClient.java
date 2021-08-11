package org.onap.so.adapters.cnf.client;

import com.google.gson.Gson;
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

    private final RestTemplate restTemplate;
    private final static Gson gson = new Gson();

    @Autowired
    public SoCallbackClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> sendPostCallback(String url, Object body) {
        return restTemplate.exchange(url, POST, httpEntity(body), String.class);
    }

    private HttpEntity<?> httpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(gson.toJson(body), headers);
    }
}
