package org.onap.so.adapters.cnf.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.POST;

@Component
public class CallbackClient {

    private static final Logger log = LoggerFactory.getLogger(CallbackClient.class);
    private final RestTemplate restTemplate;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CallbackClient(RestTemplate restTemplate) {
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

        String json = null;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Cannot process object to string: ", e);
        }

        return new HttpEntity<>(json, headers);
    }
}
