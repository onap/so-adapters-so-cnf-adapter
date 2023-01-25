/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cnfm.lcm.rest;

import static org.onap.so.cnfm.lcm.Constants.BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import javax.ws.rs.core.MediaType;
import org.onap.so.cnfm.lcm.bpmn.flows.service.KubConfigProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sagar Shetty (sagar.shetty@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Controller
@RequestMapping(value = BASE_URL)
public class CloudKubeConfigController {

    private static final Logger logger = getLogger(CloudKubeConfigController.class);
    private final KubConfigProvider kubConfigProvider;

    @Autowired
    public CloudKubeConfigController(final KubConfigProvider kubConfigProvider) {
        this.kubConfigProvider = kubConfigProvider;
    }

    @PutMapping(value = "/kube-config/cloudOwner/{cloudOwner}/cloudRegion/{cloudRegion}/tenantId/{tenantId}/upload",
            produces = {MediaType.APPLICATION_JSON},
            consumes = {MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM})
    public ResponseEntity<String> uploadKubeConfig(
            @PathVariable(name = "cloudOwner", required = true) final String cloudOwner,
            @PathVariable(name = "cloudRegion", required = true) final String cloudRegion,
            @PathVariable(name = "tenantId", required = true) final String tenantId,
            @RequestParam(name = "file", required = true) final MultipartFile file) {
        try {
            kubConfigProvider.addKubeConfigFile(file, cloudOwner, cloudRegion, tenantId);
            logger.info(
                    "Successfully retrieved kube-config file for cloud Owner: {}, " + "cloud region: {}, tenant Id: {}",
                    cloudOwner, cloudRegion, tenantId);
            return ResponseEntity.accepted().build();
        } catch (final Exception e) {
            logger.error("Error while saving kube-config file due to: {} for cloud Owner: {}, "
                    + "cloud region: {}, tenant Id: {}", e.getMessage(), cloudOwner, cloudRegion, tenantId);
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }

    }
}
