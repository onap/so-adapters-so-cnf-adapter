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
import org.onap.so.adapters.cnf.model.aai.AaiCallbackResponse;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
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
public class AaiController {

    private static final Logger logger = LoggerFactory.getLogger(AaiController.class);
    private final AaiService aaiService;
    private final SoCallbackClient callbackClient;
    private final SynchronizationService synchronizationService;

    @Autowired
    public AaiController(AaiService aaiService,
                         SoCallbackClient callbackClient,
                         SynchronizationService synchronizationService) {
        this.aaiService = aaiService;
        this.callbackClient = callbackClient;
        this.synchronizationService = synchronizationService;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/aai-update/"}, method = RequestMethod.POST,
            produces = "application/json")
    public DeferredResult<ResponseEntity> aaiUpdate(@RequestBody AaiRequest aaiRequest) {
        logger.info("aai-update called.");
        DeferredResult<ResponseEntity> response = new DeferredResult<>();

        new Thread(() -> {
            logger.info("Processing aai update");
            AaiCallbackResponse callbackResponse = new AaiCallbackResponse();
            try {
                aaiService.aaiUpdate(aaiRequest);
                synchronizationService.createSubscriptionIfNotExists(aaiRequest);
                callbackResponse.setCompletionStatus(AaiCallbackResponse.CompletionStatus.COMPLETED);
            } catch (Exception e) {
                logger.warn("Failed to create resource in AAI {}", e);
                callbackResponse.setCompletionStatus(AaiCallbackResponse.CompletionStatus.FAILED);
                callbackResponse.setMessage(e.getMessage());
            }
            callbackClient.sendPostCallback(aaiRequest.getCallbackUrl(), callbackResponse);
        }).start();

        response.setResult(ResponseEntity.accepted().build());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = {"/api/cnf-adapter/v1/aai-delete/"}, method = RequestMethod.POST,
            produces = "application/json")
    public DeferredResult<ResponseEntity> aaiDelete(@RequestBody AaiRequest aaiRequest) {
        logger.info("aai-delete called.");
        DeferredResult<ResponseEntity> response = new DeferredResult<>();

        new Thread(() -> {
            logger.info("Processing aai delete");
            AaiCallbackResponse callbackResponse = new AaiCallbackResponse();
            try {
                synchronizationService.deleteSubscriptionIfExists(aaiRequest.getInstanceId());
                aaiService.aaiDelete(aaiRequest);
                callbackResponse.setCompletionStatus(AaiCallbackResponse.CompletionStatus.COMPLETED);
            } catch (Exception e) {
                logger.warn("Failed to delete resource from AAI {}", e);
                callbackResponse.setCompletionStatus(AaiCallbackResponse.CompletionStatus.FAILED);
                callbackResponse.setMessage(e.getMessage());
            }
            callbackClient.sendPostCallback(aaiRequest.getCallbackUrl(), callbackResponse);
        }).start();

        response.setResult(ResponseEntity.accepted().build());
        return response;
    }
}
