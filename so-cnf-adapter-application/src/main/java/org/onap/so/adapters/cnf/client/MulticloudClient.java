/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
 * Modifications Copyright (C) 2021 Orange.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.cnf.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionResponse;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
public class MulticloudClient {

    private static final Logger log = LoggerFactory.getLogger(MulticloudClient.class);

    private final RestTemplate restTemplate;
    private final MulticloudApiUrl multicloudApiUrl;
    private final ObjectMapper objectMapper;

    public MulticloudClient(RestTemplate restTemplate, MulticloudApiUrl multicloudApiUrl) {
        this.restTemplate = restTemplate;
        this.multicloudApiUrl = multicloudApiUrl;
        this.objectMapper = new ObjectMapper();
    }

    public List<InstanceResponse> getAllInstances() throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl("");
        ResponseEntity<String> result = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("getAllInstances response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("getAllInstances response body: {}", body);

        try {
            return Arrays.asList(objectMapper.readValue(body, InstanceResponse[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public SubscriptionResponse registerSubscription(String instanceId, SubscriptionRequest subscriptionRequest) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/status/subscription";
        ResponseEntity<String> result = restTemplate.exchange(endpoint, POST, getHttpEntity(subscriptionRequest), String.class);
        checkResponseStatusCode(result);
        log.info("registerSubscription response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("registerSubscription response body: {}", body);

        try {
            return objectMapper.readValue(body, SubscriptionResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean hasSubscription(String instanceId, String subscriptionId) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/status/subscription/" + subscriptionId;
        try {
            ResponseEntity<String> result = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
            checkResponseStatusCode(result);
            log.info("hasSubscription response status: {}", result.getStatusCode());
            return true;
        } catch (HttpServerErrorException e) {
            if (e.getMessage().contains("no documents")) {
                log.info("hasSubscription response status: {}", 500);
                return false;
            } else
                throw e;
        }
    }

    public SubscriptionResponse getSubscription(String instanceId, String subscriptionId) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/status/subscription/" + subscriptionId;
        try {
            ResponseEntity<String> result = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
            checkResponseStatusCode(result);
            log.info("getSubscription response status: {}", result.getStatusCode());
            String body = result.getBody();
            log.debug("getSubscription response body: {}", body);
            return objectMapper.readValue(body, SubscriptionResponse.class);
        } catch (HttpServerErrorException e) {
            if (e.getMessage().contains("no documents")) {
                throw new BadResponseException("Multicloud response status error 404");
            } else
                throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteSubscription(String instanceId, String subscriptionId) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/status/subscription/" + subscriptionId;
        ResponseEntity<String> result = restTemplate.exchange(endpoint, DELETE, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("deleteSubscription response status: {}", result.getStatusCode());
    }

    public String upgradeInstance(String instanceId, MulticloudInstanceRequest upgradeRequest) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/upgrade";
        ResponseEntity<String> result = restTemplate.exchange(endpoint, POST, getHttpEntity(upgradeRequest), String.class);
        checkResponseStatusCode(result);
        log.info("upgradeInstance response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("upgradeInstance response body: {}", body);
        return body;
    }

    public K8sRbInstanceStatus getInstanceStatus(String instanceId) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/status";
        ResponseEntity<String> result = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("getInstanceStatus response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("getInstanceStatus response body: {}", body);

        try {
            return objectMapper.readValue(body, K8sRbInstanceStatus.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public K8sRbInstanceHealthCheckSimple startInstanceHealthCheck(String instanceId) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/healthcheck";
        ResponseEntity<String> result = restTemplate.exchange(endpoint, POST, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("startInstanceHealthCheck response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("startInstanceHealthCheck response body: {}", body);

        try {
            return objectMapper.readValue(body, K8sRbInstanceHealthCheckSimple.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public K8sRbInstanceHealthCheck getInstanceHealthCheck(String instanceId, String healthCheckInstance) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/healthcheck/" + healthCheckInstance;
        ResponseEntity<String> result = restTemplate.exchange(endpoint, GET, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("getInstanceHealthCheck response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("getInstanceHealthCheck response body: {}", body);

        try {
            return objectMapper.readValue(body, K8sRbInstanceHealthCheck.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteInstanceHealthCheck(String instanceId, String healthCheckInstance) throws BadResponseException {
        String endpoint = multicloudApiUrl.apiUrl(instanceId) + "/healthcheck/" + healthCheckInstance;
        ResponseEntity<String> result = restTemplate.exchange(endpoint, DELETE, getHttpEntity(), String.class);
        checkResponseStatusCode(result);
        log.info("deleteInstanceHealthCheck response status: {}", result.getStatusCode());
        String body = result.getBody();
        log.debug("deleteInstanceHealthCheck response body: {}", body);

        if (!result.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Delete response different than 2xx:" + result.getStatusCode());
        }
    }

    private HttpEntity<Void> getHttpEntity() {
        HttpHeaders headers = getHttpHeaders();

        return new HttpEntity<>(headers);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> HttpEntity<T> getHttpEntity(T body) {
        HttpHeaders headers = getHttpHeaders();

        return new HttpEntity<>(body, headers);
    }

    private void checkResponseStatusCode(ResponseEntity<String> result) throws BadResponseException {
        HttpStatus statusCode = result.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new BadResponseException("Multicloud response status error", String.valueOf(statusCode.value()));
        }
    }
}
