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

import org.onap.so.adapters.cnf.client.MulticloudHttpClient;
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
    private final MulticloudHttpClient httpClient;

    @Autowired
    public ConnectivityInfoController(MulticloudHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info"}, method = RequestMethod.POST,
            produces = "application/json")
    public String createConnectivityInfo(@RequestBody ConnectivityInfo cIE) throws Exception {
        logger.info("create ConnectivityInfo called.");
        return httpClient.post("/v1/connectivity-info", cIE);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getConnectivityInfo(@PathVariable("connname") String connName) throws Exception {
        logger.info("get Connectivity Info called.");
        return httpClient.get("/v1/connectivity-info/" + connName);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/connectivity-info/{connname}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public String deleteConnectivityInfo(@PathVariable("connname") String connName) throws Exception {
        logger.info("delete Connectivity Info called.");
        return httpClient.delete("/v1/connectivity-info/" + connName);
    }
}
