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
    private final MulticloudHttpClient httpClient;

    @Autowired
    public ConfigurationController(MulticloudHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName)
            throws Exception {
        logger.info("create Configuration called.");
        return httpClient.post("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config", cE);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfiguration(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                   @PathVariable("profile-name") String prName, @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("get Configuration called.");
        return httpClient.get("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config/" + cfgName);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteConfiguration(@PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
                                      @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("delete Configuration called.");
        return httpClient.delete("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config/" + cfgName);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/config/{cfg-name}"},
            method = RequestMethod.PUT, produces = "application/json")
    public String updateConfiguration(@RequestBody ConfigurationEntity cE, @PathVariable("rb-name") String rbName,
                                      @PathVariable("rb-version") String rbVersion, @PathVariable("profile-name") String prName,
                                      @PathVariable("cfg-name") String cfgName) throws Exception {
        logger.info("update Configuration called.");
        return httpClient.put("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config/" + cfgName, cE);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rb-name}/{rb-version}/profile/{profile-name}/tagit"},
            method = RequestMethod.POST, produces = "application/json")
    public String tagConfigurationValue(@RequestBody Tag tag, @PathVariable("rb-name") String rbName,
                                        @PathVariable("rb-version") String rbVersion, @PathVariable("pr-name") String prName) throws Exception {
        logger.info("Tag Configuration called.");
        return httpClient.post("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config/tagit", tag);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/definition/{rbName}/{rbVersion}/profile/{prName}/config/rollback"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String rollbackConfiguration(@RequestBody ConfigurationRollbackEntity rE,
                                        @PathVariable("rbName") String rbName, @PathVariable("rbVersion") String rbVersion,
                                        @PathVariable("prName") String prName) throws Exception {
        logger.info("rollbackConfiguration called.");
        return httpClient.post("/v1/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/config/rollback", rE);
    }
}
