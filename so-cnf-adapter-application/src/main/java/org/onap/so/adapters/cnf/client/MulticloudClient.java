package org.onap.so.adapters.cnf.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.adapters.cnf.model.halthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.halthcheck.K8sRbInstanceHealthCheckSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Component
public class MulticloudClient {

    private static final Logger log = LoggerFactory.getLogger(MulticloudClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MulticloudClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public K8sRbInstanceHealthCheckSimple startInstanceHealthCheck(String instanceId) {
        String endpoint = MulticloudApiUrl.withInstanceId(instanceId).apiUrl() + "/healthcheck";
        ResponseEntity<String> result  = restTemplate.exchange(endpoint, POST, getHttpEntity(), String.class);
        String body = result.getBody();
        log.info("startInstanceHealthCheck response body: {}", body);

        try {
            return objectMapper.readValue(body, K8sRbInstanceHealthCheckSimple.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public K8sRbInstanceHealthCheck getInstanceHealthCheck(String instanceId, String healthCheckInstance) {
        String endpoint = MulticloudApiUrl.withInstanceId(instanceId).apiUrl() + "/healthcheck/" + healthCheckInstance;
        ResponseEntity<String> result  = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
        String body = result.getBody();
        log.info("getInstanceHealthCheck response body: {}", body);

        try {
            return objectMapper.readValue(body, K8sRbInstanceHealthCheck.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteInstanceHealthCheck(String instanceId, String healthCheckInstance) {
        String endpoint = MulticloudApiUrl.withInstanceId(instanceId).apiUrl() + "/healthcheck/" + healthCheckInstance;
        ResponseEntity<String> result  = restTemplate.exchange(endpoint, DELETE, getHttpEntity(), String.class);
        String body = result.getBody();
        log.info("getInstanceHealthCheck response body: {}", body);

        if (!result.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Delete response different than 2xx:" + result.getStatusCode());
        }
    }

    private HttpEntity<?> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(headers);
    }

    private static class MulticloudApiUrl {

        private final String INSTANCE_URI = "http://multicloud-k8s:9015/v1/instance/";
        private final String instanceId;

        private MulticloudApiUrl(String instanceId) {
            this.instanceId = instanceId;
        }

        static MulticloudApiUrl withInstanceId(String instanceId) {
            return new MulticloudApiUrl(instanceId);
        }

        String apiUrl() {
            return instanceId.equals("") ? INSTANCE_URI : INSTANCE_URI + instanceId;
        }
    }
}
