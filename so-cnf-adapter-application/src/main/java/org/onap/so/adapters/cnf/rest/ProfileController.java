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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.ProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String uri;

    @Autowired
    public ProfileController(MulticloudConfiguration multicloudConfiguration) {
        this.uri = multicloudConfiguration.getMulticloudUrl();
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.POST, produces = "application/json")
    public String createProfile(@RequestBody ProfileEntity fE, @PathVariable("rb-name") String rbName,
                                @PathVariable("rb-version") String rbVersion) throws Exception {
        logger.info("create Profile called.");

        HttpPost post =
                new HttpPost(uri + "/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(fE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response:" + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                             @PathVariable("pr-name") String prName) throws Exception {
        logger.info("get Profile called.");

        HttpGet req = new HttpGet(
                uri + "/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.GET, produces = "application/json")
    public String getListOfProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {
        logger.info("getListOfProfile called.");

        HttpGet req =
                new HttpGet(uri + "/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile");
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                @PathVariable("pr-name") String prName) throws Exception {
        logger.info("delete Profile called.");

        HttpDelete req = new HttpDelete(
                uri + "/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadArtifactForProfile(@RequestParam("file") MultipartFile file,
                                           @PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                           @PathVariable("pr-name") String prName) throws Exception {
        logger.info("Upload  Artifact For Profile called.");

        File convFile = new File(file.getOriginalFilename());
        file.transferTo(convFile);
        FileBody fileBody = new FileBody(convFile, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        HttpPost post = new HttpPost(uri + "/v1/rb/definition/" + rbName + "/" + rbVersion
                + "/profile/" + prName + "/content");
        post.setHeader("Content-Type", "multipart/form-data");

        logger.info(String.valueOf(post));
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }
}
