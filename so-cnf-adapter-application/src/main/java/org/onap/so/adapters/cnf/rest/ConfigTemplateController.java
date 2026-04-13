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
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
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

@RestController
public class ConfigTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigTemplateController.class);
    private final MulticloudHttpClient httpClient;

    @Autowired
    public ConfigTemplateController(MulticloudHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template"},
            method = RequestMethod.POST, produces = "application/json")
    public String createConfigTemplate(@RequestBody ConfigTemplateEntity tE, @PathVariable("rb-name") String rbName,
                                       @PathVariable("rb-version") String rbVersion) throws Exception {
        logger.info("createConfigTemplate called.");
        return httpClient.post("/v1/rb/definition/" + rbName + "/" + rbVersion + "/config-template", tE);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getConfigTemplate(@PathVariable("rb-name") String rbName,
                                    @PathVariable("rb-version") String rbVersion, @PathVariable("tname") String tName) throws Exception {
        logger.info("getConfigTemplate called.");
        return httpClient.get("/v1/rb/definition/" + rbName + "/" + rbVersion + "/config-template/" + tName);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteTemplate(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                 @PathVariable("tname") String tName) throws Exception {
        logger.info("deleteTemplate called.");
        return httpClient.delete("/v1/rb/definition/" + rbName + "/" + rbVersion + "/config-template/" + tName);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/config-template/{tname}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadTarFileForTemplate(@RequestParam("file") MultipartFile file,
                                           @PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                           @PathVariable("tname") String tName) throws Exception {
        logger.info("uploadTarFileForTemplate called.");
        return httpClient.uploadMultipartFile(
                "/v1/rb/definition/" + rbName + "/" + rbVersion + "/config-template/" + tName + "/content", file);
    }
}
