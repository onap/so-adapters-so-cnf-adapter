/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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
package org.onap.so.adapters.cnf.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.ConfigurationEntity;
import org.onap.so.adapters.cnf.model.ConfigurationRollbackEntity;
import org.onap.so.adapters.cnf.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String uri;

    @Autowired
    public ConfigurationController(MulticloudConfiguration multicloudConfiguration) {
        this.uri = multicloudConfiguration.getMulticloudUrl();
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName)
            throws Exception {
        logger.info("create Configuration called.");

        HttpPost post = new HttpPost(uri + "/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(cE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfiguration(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                   @PathVariable("profile-name") String prName, @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("get Configuration called.");

        HttpGet req = new HttpGet(uri + "/v1/definition/" + rbName + "/" + rbVersion + "/profile/"
                + prName + "/config/" + cfgName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteConfiguration(@PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
                                      @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("delete Configuration called.");

        HttpDelete req = new HttpDelete(uri + "/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/" + cfgName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.PUT, produces = "application/json")
    public String updateConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
                                      @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("update Configuration called.");

        HttpPut post = new HttpPut(uri + "/v1/definition/" + rbName + "/" + rbVersion + "/profile/"
                + prName + "/config/" + cfgName);
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(cE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/tagit"},
            method = RequestMethod.POST, produces = "application/json")
    public String tagConfigurationValue(@RequestBody Tag tag, @PathVariable("rb-name") String rbName,
                                        @PathVariable("rb-version") String rbVersion, @PathVariable("pr-name") String prName) throws Exception {
        logger.info("Tag Configuration called.");

        HttpPost post = new HttpPost(uri + "/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/tagit");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(tag);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rbName}/{rbVersion}/profile/{prName}/config/rollback"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String rollbackConfiguration(@RequestBody ConfigurationRollbackEntity rE,
                                        @PathVariable("rbName") String rbName, @PathVariable("rbVersion") String rbVersion,
                                        @PathVariable("prName") String prName) throws Exception {
        logger.info("rollbackConfiguration called.");

        HttpPost post = new HttpPost(uri + "/v1/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/config/rollback");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(rE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }
}
