/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Orange.
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

package org.onap.so.adapters.cnf.service.statuscheck;

import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckInstanceResponse;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckResponse;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleStatusCheckService {

    private final Logger log = LoggerFactory.getLogger(SimpleStatusCheckService.class);
    private final MulticloudClient instanceApi;

    @Autowired
    public SimpleStatusCheckService(MulticloudClient instanceApi) {
        this.instanceApi = instanceApi;
    }

    public StatusCheckResponse statusCheck(CheckInstanceRequest instanceIds) throws BadResponseException {
        log.info("CnfAdapterService statusCheck called");
        StatusCheckResponse result = new StatusCheckResponse();

        List<StatusCheckInstanceResponse> simpleStatuses = new ArrayList<>();
        for (InstanceRequest instanceRequest : instanceIds.getInstances()) {
            String instanceId = instanceRequest.getInstanceId();
            StatusCheckInstanceResponse statusCheck = getStatusCheck(instanceId);
            simpleStatuses.add(statusCheck);
        }

        result.setInstanceResponse(simpleStatuses);
        return result;
    }

    public StatusCheckResponse statusCheckError(CheckInstanceRequest instanceIds, Exception e) {
        StatusCheckResponse result = new StatusCheckResponse();

        List<StatusCheckInstanceResponse> simpleStatuses = new ArrayList<>();
        for (InstanceRequest instanceRequest : instanceIds.getInstances()) {
            StatusCheckInstanceResponse statusCheck = new StatusCheckInstanceResponse(
                    instanceRequest.getInstanceId(), e.getMessage(), false);
            simpleStatuses.add(statusCheck);
        }

        result.setInstanceResponse(simpleStatuses);
        return result;
    }

    private StatusCheckInstanceResponse getStatusCheck(String instanceId) throws BadResponseException {
        log.debug("SIMPLE STATUS CHECK - START");
        K8sRbInstanceStatus instanceStatus = instanceApi.getInstanceStatus(instanceId);
        boolean isInstanceReady = instanceStatus.isReady();
        log.info("Get status for instanceId: {}", instanceId);
        log.info("Instance status: {}", instanceStatus);
        StatusCheckInstanceResponse result = new StatusCheckInstanceResponse(instanceId, null, isInstanceReady);
        log.debug("SIMPLE STATUS CHECK - END SUCCESS");

        return result;
    }

}
