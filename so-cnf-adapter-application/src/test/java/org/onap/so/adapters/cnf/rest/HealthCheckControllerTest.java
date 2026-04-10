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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.cnf.client.SoCallbackClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckResponse;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.service.statuscheck.SimpleStatusCheckService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.async.DeferredResult;


@SpringBootTest
@RunWith(SpringRunner.class)
public class HealthCheckControllerTest {

    @InjectMocks
    HealthCheckController healthCheckController;

    @Mock
    HealthCheckService healthCheckService;

    @Mock
    private SoCallbackClient callbackClient;

    @Mock
    SimpleStatusCheckService simpleStatusCheckService;

    @Test
    public void healthCheckTest() throws Exception {
        HealthCheckResponse response = new HealthCheckResponse();
        DeferredResult<HealthCheckResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);
        HealthCheckService healthCheckService = Mockito.mock(HealthCheckService.class);
        CheckInstanceRequest healthCheckRequest = Mockito.mock(CheckInstanceRequest.class);
        Mockito.when(healthCheckService.healthCheck(healthCheckRequest)).thenReturn(response);

        healthCheckController.healthCheck(healthCheckRequest);

        Assert.assertNotNull(response);
    }

    @Test
    public void statusCheckTest() throws Exception {
        StatusCheckResponse response = new StatusCheckResponse();
        DeferredResult<StatusCheckResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        CheckInstanceRequest statusCheckRequest = Mockito.mock(CheckInstanceRequest.class);
        SimpleStatusCheckService simpleStatusCheckService = Mockito.mock(SimpleStatusCheckService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);
        Mockito.when(simpleStatusCheckService.statusCheck(statusCheckRequest)).thenReturn(response);
        healthCheckController.statusCheck(statusCheckRequest);

        Assert.assertNotNull(response);
    }
}
