/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
 * Modifications Copyright (C) 2021 Orange.
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.onap.so.adapters.cnf.service.upgrade.InstanceUpgradeService;
import org.onap.so.client.exception.BadResponseException;
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

import java.io.IOException;

@RestController
public class InstanceController {

    private static final Logger logger = LoggerFactory.getLogger(InstanceController.class);
    private final CnfAdapterService cnfAdapterService;
    private final InstanceUpgradeService instanceUpgradeService;
    private final SynchronizationService synchronizationService;

    @Autowired
    public InstanceController(CnfAdapterService cnfAdapterService,
                              InstanceUpgradeService instanceUpgradeService,
                              SynchronizationService synchronizationService) {
        this.cnfAdapterService = cnfAdapterService;
        this.instanceUpgradeService = instanceUpgradeService;
        this.synchronizationService = synchronizationService;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instanceID}/upgrade"}, method = RequestMethod.POST,
            produces = "application/json", consumes = "application/json")
    public String upgrade(@PathVariable("instanceID") String instanceId,
                          @RequestBody InstanceUpgradeRequest upgradeRequest) throws BadResponseException {
        logger.info("upgrade called for instance {}.", instanceId);
        return instanceUpgradeService.upgradeInstance(instanceId, upgradeRequest);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance"}, method = RequestMethod.POST,
            produces = "application/json", consumes = "application/json")
    public String createInstance(@RequestBody BpmnInstanceRequest bpmnInstanceRequest) throws BadResponseException {
        logger.info("createInstance called.");
        return cnfAdapterService.createInstance(bpmnInstanceRequest);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getInstanceByInstanceId(@PathVariable("instID") String instanceId)
            throws JsonParseException, JsonMappingException, IOException {
        logger.info("getInstanceByInstanceId called.");
        return cnfAdapterService.getInstanceByInstanceId(instanceId);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}/status"}, method = RequestMethod.GET,
            produces = "application/json")
    public String getInstanceStatusByInstanceId(@PathVariable("instID") String instanceId)
            throws JsonParseException, JsonMappingException, IOException {
        logger.info("getInstanceStatusByInstanceId called.");
        return cnfAdapterService.getInstanceStatusByInstanceId(instanceId);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instanceId}/query"}, method = RequestMethod.GET,
            produces = "application/json")
    public String queryInstanceResources(
            @PathVariable("instanceId") String instanceId,
            @RequestParam(value = "Kind") String kind,
            @RequestParam(value = "ApiVersion") String apiVersion,
            @RequestParam(value = "Labels", required = false) String labels,
            @RequestParam(value = "Namespace", required = false) String namespace,
            @RequestParam(value = "Name", required = false) String name) {
        logger.info("queryInstanceResources called.");
        return cnfAdapterService.queryInstanceResources(instanceId, kind, apiVersion, name, labels, namespace);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/query"}, method = RequestMethod.GET,
            produces = "application/json")
    public String queryResources(
            @RequestParam(value = "Kind") String kind,
            @RequestParam(value = "ApiVersion") String apiVersion,
            @RequestParam(value = "Labels", required = false) String labels,
            @RequestParam(value = "Namespace", required = false) String namespace,
            @RequestParam(value = "Name", required = false) String name,
            @RequestParam(value = "CloudRegion") String cloudRegion) {
        logger.info("queryResources called.");
        return cnfAdapterService.queryResources(kind, apiVersion, name, labels, namespace, cloudRegion);
    }

    @RequestMapping(value = {"/api/cnf-adapter/v1/instance"}, method = RequestMethod.GET, produces = "application/json")
    public String getInstanceByRBNameOrRBVersionOrProfileName(
            @RequestParam(value = "rb-name", required = false) String rbName,
            @RequestParam(value = "rb-version", required = false) String rbVersion,
            @RequestParam(value = "profile-name", required = false) String profileName)
            throws JsonParseException, JsonMappingException, IOException {
        logger.info("getInstanceByRBNameOrRBVersionOrProfileName called.");
        return cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/instance/{instID}"}, method = RequestMethod.DELETE,
            produces = "application/json")
    public String deleteInstanceByInstanceId(@PathVariable("instID") String instanceID) throws BadResponseException {
        logger.info("deleteInstanceByInstanceId called.");
        if (instanceID == null || instanceID.isEmpty() || instanceID.equals("null")) {
            logger.warn("Undefined instance ID delete attempt. Skipping delete");
            return "";
        }
        synchronizationService.deleteSubscriptionIfExists(instanceID);
        return cnfAdapterService.deleteInstanceByInstanceId(instanceID);
    }
}
