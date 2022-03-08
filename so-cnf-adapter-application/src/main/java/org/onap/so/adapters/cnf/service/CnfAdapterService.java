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

package org.onap.so.adapters.cnf.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.onap.so.client.exception.BadResponseException;
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

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CnfAdapterService {
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterService.class);
    private static final String INSTANCE_CREATE_PATH = "/v1/instance";

    private final RestTemplate restTemplate;
    private final String uri;

    @Autowired
    public CnfAdapterService(RestTemplate restTemplate,
                             MulticloudConfiguration multicloudConfiguration) {
        this.restTemplate = restTemplate;
        this.uri = multicloudConfiguration.getMulticloudUrl();
    }

    public String createInstance(BpmnInstanceRequest bpmnInstanceRequest) {
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

    public String queryInstanceResources(String instanceId, String kind, String apiVersion, String name, String labels,
                                         String namespace) {
        logger.info("CnfAdapterService queryInstanceResources called");
        ResponseEntity<String> queryResponse = null;
        try {
            String path = "/v1/instance/" + instanceId + "/query";
            UriBuilder builder = UriBuilder.fromUri(uri).path(path).queryParam("Kind", kind).
                    queryParam("ApiVersion", apiVersion);
            if (namespace != null)
                builder = builder.queryParam("Namespace", namespace);
            if (name != null)
                builder = builder.queryParam("Name", name);
            if (labels != null)
                builder = builder.queryParam("Labels", labels);
            String endpoint = builder.build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            queryResponse = restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            logger.info("response: " + queryResponse);
            return queryResponse.getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(e.getResponseBodyAsString());
            }
            throw e;
        }
    }

    public String queryResources(String kind, String apiVersion, String name, String labels,
                                 String namespace, String cloudRegion) {
        logger.info("CnfAdapterService queryResources called");
        ResponseEntity<String> queryResponse = null;
        try {
            String path = "/v1/query";
            UriBuilder builder = UriBuilder.fromUri(uri).path(path).queryParam("Kind", kind).
                    queryParam("ApiVersion", apiVersion).queryParam("CloudRegion", cloudRegion);
            if (namespace != null)
                builder = builder.queryParam("Namespace", namespace);
            if (name != null)
                builder = builder.queryParam("Name", name);
            if (labels != null)
                builder = builder.queryParam("Labels", labels);
            String endpoint = builder.build().toString();
            HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders());
            logger.info("request: " + requestEntity);
            queryResponse = restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
            logger.info("response: " + queryResponse);
            return queryResponse.getBody();
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

    public String deleteInstanceByInstanceId(String instanceId) {

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
