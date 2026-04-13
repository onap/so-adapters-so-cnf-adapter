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

@RestController
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final MulticloudHttpClient httpClient;

    @Autowired
    public ProfileController(MulticloudHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.POST, produces = "application/json")
    public String createProfile(@RequestBody ProfileEntity fE, @PathVariable("rb-name") String rbName,
                                @PathVariable("rb-version") String rbVersion) throws Exception {
        logger.info("create Profile called.");
        return httpClient.post("/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile", fE);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.GET, produces = "application/json")
    public String getProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                             @PathVariable("pr-name") String prName) throws Exception {
        logger.info("get Profile called.");
        return httpClient.get("/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile"},
            method = RequestMethod.GET, produces = "application/json")
    public String getListOfProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion)
            throws Exception {
        logger.info("getListOfProfile called.");
        return httpClient.get("/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile");
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}"},
            method = RequestMethod.DELETE, produces = "application/json")
    public String deleteProfile(@PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                @PathVariable("pr-name") String prName) throws Exception {
        logger.info("delete Profile called.");
        return httpClient.delete("/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/rb/definition/{rb-name}/{rb-version}/profile/{pr-name}/content"},
            method = RequestMethod.POST, produces = "multipart/form-data")
    public String uploadArtifactForProfile(@RequestParam("file") MultipartFile file,
                                           @PathVariable("rb-name") String rbName, @PathVariable("rb-version") String rbVersion,
                                           @PathVariable("pr-name") String prName) throws Exception {
        logger.info("Upload  Artifact For Profile called.");
        return httpClient.uploadMultipartFile(
                "/v1/rb/definition/" + rbName + "/" + rbVersion + "/profile/" + prName + "/content", file);
    }
}
