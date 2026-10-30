/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class MulticloudHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(MulticloudHttpClient.class);
    private static final MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public MulticloudHttpClient(MulticloudConfiguration multicloudConfiguration,
            RestTemplateBuilder restTemplateBuilder) {
        this.baseUrl = multicloudConfiguration.getMulticloudUrl();
        // The callers relay multicloud's response body whatever its status, so a 4xx/5xx must not throw.
        this.restTemplate = restTemplateBuilder.errorHandler(new PassThroughErrorHandler()).build();
    }

    public String get(String path) {
        return exchange(path, HttpMethod.GET, HttpEntity.EMPTY);
    }

    public String delete(String path) {
        return exchange(path, HttpMethod.DELETE, HttpEntity.EMPTY);
    }

    public String post(String path, Object body) throws IOException {
        return exchange(path, HttpMethod.POST, jsonEntity(body));
    }

    public String put(String path, Object body) throws IOException {
        return exchange(path, HttpMethod.PUT, jsonEntity(body));
    }

    public String uploadMultipartFile(String path, MultipartFile file) {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new HttpEntity<>(file.getResource(), partHeaders));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return exchange(path, HttpMethod.POST, new HttpEntity<>(parts, headers));
    }

    private HttpEntity<String> jsonEntity(Object body) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(JSON_UTF8);
        return new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
    }

    private String exchange(String path, HttpMethod method, HttpEntity<?> request) {
        ResponseEntity<String> response =
                restTemplate.exchange(URI.create(baseUrl + path), method, request, String.class);
        logger.info("{} {} returned {}", method, path, response.getStatusCode());
        return response.getBody();
    }

    private static class PassThroughErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) {
            return false;
        }
    }
}
