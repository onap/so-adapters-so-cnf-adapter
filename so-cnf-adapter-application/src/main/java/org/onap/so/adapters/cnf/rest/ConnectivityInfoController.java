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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
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
public class ConnectivityInfoController {

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityInfoController.class);
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String uri;

    @Autowired
    public ConnectivityInfoController(MulticloudConfiguration multicloudConfiguration) {
        this.uri = multicloudConfiguration.getMulticloudUrl();
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createConnectivityInfo(@RequestBody ConnectivityInfo cIE) throws Exception {
        logger.info("create ConnectivityInfo called.");

        HttpPost post = new HttpPost(uri + "/v1/connectivity-info");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(cIE);
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getConnectivityInfo(@PathVariable("connname") String connName) throws Exception {
        logger.info("get Connectivity Info called.");

        HttpGet req = new HttpGet(uri + "/v1/connectivity-info/" + connName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public String deleteConnectivityInfo(@PathVariable("connname") String connName) throws Exception {
        logger.info("delete Connectivity Info called.");

        HttpDelete req = new HttpDelete(uri + "/v1/connectivity-info/" + connName);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            logger.info("response: " + response.getEntity());
            return EntityUtils.toString(response.getEntity());
        }
    }
}
