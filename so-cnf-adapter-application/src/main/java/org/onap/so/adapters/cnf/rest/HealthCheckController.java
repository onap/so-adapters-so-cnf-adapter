/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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

import org.onap.so.adapters.cnf.client.SoCallbackClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckResponse;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.service.statuscheck.SimpleStatusCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private final HealthCheckService healthCheckService;
    private final SimpleStatusCheckService simpleStatusCheckService;
    private final SoCallbackClient callbackClient;

    @Autowired
    public HealthCheckController(HealthCheckService healthCheckService,
                                 SimpleStatusCheckService simpleStatusCheckService,
                                 SoCallbackClient callbackClient) {
        this.healthCheckService = healthCheckService;
        this.simpleStatusCheckService = simpleStatusCheckService;
        this.callbackClient = callbackClient;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/healthcheck"}, method = RequestMethod.POST,
            produces = "application/json")
    public DeferredResult<ResponseEntity> healthCheck(@RequestBody CheckInstanceRequest healthCheckRequest) {
        logger.info("healthCheck called.");
        DeferredResult<ResponseEntity> response = new DeferredResult<>();

        new Thread(() -> {
            logger.info("Processing health check request");

            HealthCheckResponse healthCheckResponse = null;
            try {
                healthCheckResponse = healthCheckService.healthCheck(healthCheckRequest);
            } catch (Exception e) {
                logger.error("END - Health check process failed", e);
                healthCheckResponse = healthCheckService.healthCheckError(healthCheckRequest, e);
            }
            callbackClient.sendPostCallback(healthCheckRequest.getCallbackUrl(), healthCheckResponse);
        }).start();

        response.setResult(ResponseEntity.accepted().build());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/statuscheck"}, method = RequestMethod.POST,
            produces = "application/json")
    public DeferredResult<ResponseEntity> statusCheck(@RequestBody CheckInstanceRequest statusCheckRequest) {
        logger.info("statusCheck called.");
        DeferredResult<ResponseEntity> response = new DeferredResult<>();

        new Thread(() -> {
            logger.info("Processing status check request");
            StatusCheckResponse statusCheckResponse = null;
            try {
                statusCheckResponse = simpleStatusCheckService.statusCheck(statusCheckRequest);
            } catch (Exception e) {
                logger.error("END - Status check process failed {}", e);
                statusCheckResponse = simpleStatusCheckService.statusCheckError(statusCheckRequest, e);
            }
            callbackClient.sendPostCallback(statusCheckRequest.getCallbackUrl(), statusCheckResponse);
        }).start();

        response.setResult(ResponseEntity.accepted().build());
        return response;
    }
}
