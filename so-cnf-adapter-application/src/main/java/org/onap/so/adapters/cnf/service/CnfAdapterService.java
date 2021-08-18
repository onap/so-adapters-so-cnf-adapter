/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.service.statuscheck.SimpleStatusCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
public class CnfAdapterService {
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterService.class);
    private static final String INSTANCE_CREATE_PATH = "/v1/instance";

    private final RestTemplate restTemplate;
    private final HealthCheckService healthCheckService;
    private final SimpleStatusCheckService simpleStatusCheckService;
    private final String uri;

    @Autowired
    public CnfAdapterService(RestTemplate restTemplate,
                             HealthCheckService healthCheckService,
                             SimpleStatusCheckService simpleStatusCheckService,
                             MulticloudConfiguration multicloudConfiguration) {
        this.restTemplate = restTemplate;
        this.healthCheckService = healthCheckService;
        this.simpleStatusCheckService = simpleStatusCheckService;
        this.uri = multicloudConfiguration.getMulticloudUrl();
    }

    public HealthCheckResponse healthCheck(CheckInstanceRequest healthCheckRequest) throws Exception {
        logger.info("CnfAdapterService healthCheck called");
        return healthCheckService.healthCheck(healthCheckRequest);
    }

    public String createInstance(BpmnInstanceRequest bpmnInstanceRequest)
            throws JsonParseException, JsonMappingException, IOException {
        try {
            logger.info("CnfAdapterService createInstance called");
            MulticloudInstanceRequest multicloudInstanceRequest = new MulticloudInstanceRequest();
            ResponseEntity<String> instanceResponse = null;
            if (bpmnInstanceRequest.getK8sRBProfileName() != null) {    
                multicloudInstanceRequest.setCloudRegion(bpmnInstanceRequest.getCloudRegionId());
                multicloudInstanceRequest.setLabels(bpmnInstanceRequest.getLabels());
                multicloudInstanceRequest.setOverrideValues(bpmnInstanceRequest.getOverrideValues());
                multicloudInstanceRequest.setProfileName(bpmnInstanceRequest.getK8sRBProfileName());
                multicloudInstanceRequest.setRbName(bpmnInstanceRequest.getModelInvariantId());
                if (bpmnInstanceRequest.getModelCustomizationId() != null) {
                    multicloudInstanceRequest.setRbVersion(bpmnInstanceRequest.getModelCustomizationId());
                    logger.info("vfModuleModelCustomizationId used for rb-version: " + multicloudInstanceRequest.getRbVersion());
                } else {
                    multicloudInstanceRequest.setRbVersion(bpmnInstanceRequest.getModelVersionId());
                    logger.info("vfModuleModelUUID used for rb-version: " + multicloudInstanceRequest.getRbVersion());
                }
                
                if (bpmnInstanceRequest.getK8sRBInstanceReleaseName() != null) {
                    multicloudInstanceRequest.setReleaseName(bpmnInstanceRequest.getK8sRBInstanceReleaseName());
                    logger.info("Specified release name used: " + multicloudInstanceRequest.getReleaseName());
                } else {
                    multicloudInstanceRequest.setReleaseName(
                            bpmnInstanceRequest.getK8sRBProfileName() + "-" + bpmnInstanceRequest.getVfModuleUUID());
                    logger.info("Generated release name used: " + multicloudInstanceRequest.getReleaseName());
                }
            } else {
                logger.error("k8sProfileName should not be null");
                // return instanceResponse;
            }
            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String endpoint = UriBuilder.fromUri(uri).path(INSTANCE_CREATE_PATH).build().toString();
            HttpEntity<?> entity = getHttpEntity(multicloudInstanceRequest);
            logger.info("request: " + entity);
            instanceResponse = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            logger.info("response: " + instanceResponse);
            return instanceResponse.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        } catch (HttpStatusCodeException e) {
            logger.error("Error in Multicloud", e);
            throw e;
        }
    }

    public String getInstanceByInstanceId(String instanceId)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("CnfAdapterService getInstanceByInstanceId called");
        ResponseEntity<String> instanceResponse = null;
        try {

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String path = "/v1/instance/" + instanceId;
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            instanceResponse = restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            logger.info("response: " + instanceResponse);
            return instanceResponse.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        } catch (HttpStatusCodeException e) {
            logger.error("Error in Multicloud", e);
            throw e;
        }
    }

    public String getInstanceStatusByInstanceId(String instanceId)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("CnfAdapterService getInstanceStatusByInstanceId called");
        ResponseEntity<String> instanceResponse = null;
        try {

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String path = "/v1/instance/" + instanceId + "/status";
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            instanceResponse = restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            logger.info("response: " + instanceResponse);
            return instanceResponse.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        } catch (HttpStatusCodeException e) {
            logger.error("Error in Multicloud", e);
            throw e;
        }

    }

    public String getInstanceQueryByInstanceId(String instanceId) {
        logger.info("CnfAdapterService getInstanceQueryByInstanceId called");
        ResponseEntity<String> instanceResponse = null;
        try {
            String path = "/v1/instance/" + instanceId + "/query";
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            instanceResponse = restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            logger.info("response: " + instanceResponse);
            return instanceResponse.getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public String getInstanceByRBNameOrRBVersionOrProfileName(String rbName, String rbVersion, String profileName)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("CnfAdapterService getInstanceByRBNameOrRBVersionOrProfileName called");
        ResponseEntity<String> instanceMiniResponseList = null;
        try {

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String path =
                    "/v1/instance" + "?rb-name=" + rbName + "&rb-version=" + rbVersion + "&profile-name=" + profileName;
            String endPoint = uri + path;
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            instanceMiniResponseList = restTemplate.exchange(endPoint, HttpMethod.GET, requestEntity, String.class);
            return instanceMiniResponseList.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        } catch (HttpStatusCodeException e) {
            logger.error("Error in Multicloud", e);
            throw e;
        }
    }

    public String deleteInstanceByInstanceId(String instanceId)
            throws JsonParseException, JsonMappingException, IOException {

        logger.info("CnfAdapterService deleteInstanceByInstanceId called");
        ResponseEntity<String> result = null;
        try {

            // String uri = env.getRequiredProperty("multicloud.endpoint"); //TODO:
            // This needs to be added as well
            // for configuration
            String path = "/v1/instance/" + instanceId;
            String endpoint = UriBuilder.fromUri(uri).path(path).build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            result = restTemplate.exchange(endpoint, HttpMethod.DELETE, requestEntity, String.class);
            logger.info("response: " + result);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling Multicloud, e");
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        } catch (HttpStatusCodeException e) {
            logger.error("Error in Multicloud", e);
            throw e;
        }
    }

    protected HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        /*
         * try { String userCredentials = CryptoUtils.decrypt(env.getRequiredProperty("mso.cnf.adapter.auth"),
         * env.getRequiredProperty("mso.msoKey")); if (userCredentials != null) { headers.add(HttpHeaders.AUTHORIZATION,
         * "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes())); } } catch
         * (GeneralSecurityException e) { logger.error("Security exception", e); }
         */
        return headers;
    }

    protected HttpEntity<?> getHttpEntity(MulticloudInstanceRequest request) {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(request, headers);
    }
}
