/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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


import com.google.gson.Gson;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.NotificationRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SubscriptionNotifyController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionNotifyController.class);
    private final static Gson gson = new Gson();

    private final AaiService aaiService;
    private final SynchronizationService synchronizationService;

    public SubscriptionNotifyController(AaiService aaiService, SynchronizationService synchronizationService) {
        this.aaiService = aaiService;
        this.synchronizationService = synchronizationService;
    }

    @PostMapping(value = "/api/cnf-adapter/v1/instance/{instanceId}/status/notify")
    public ResponseEntity subscriptionNotifyEndpoint(@PathVariable String instanceId,
                                                     @RequestBody NotificationRequest body) throws BadResponseException {
        String subscriptionName = synchronizationService.getSubscriptionName(instanceId);
        boolean isSubscriptionActive = synchronizationService.isSubscriptionActive(subscriptionName);
        if (isSubscriptionActive) {
            logger.info("AAI update- START");
            aaiService.aaiUpdate(body.getMetadata());
            return ResponseEntity
                    .accepted()
                    .build();
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Cannot handle notification. Subscription %s not exists", subscriptionName));
        }
    }

    private AaiRequest convertMetadataToAaiRequest(Map<String, Object> metadata) {
        String json = gson.toJsonTree(metadata)
                .getAsJsonObject()
                .get("metadata")
                .toString();

        return gson.fromJson(json, AaiRequest.class);
    }

}
