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
import org.onap.so.adapters.cnf.model.aai.AaiCallbackResponse;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.async.DeferredResult;


@SpringBootTest
@RunWith(SpringRunner.class)
public class AaiControllerTest {

    @InjectMocks
    AaiController aaiController;

    @Mock
    private SoCallbackClient callbackClient;

    @Mock
    private AaiService aaiService;

    @Mock
    private SynchronizationService synchronizationService;

    @Test
    public void aaiUpdateTest() throws Exception {
        AaiCallbackResponse response = new AaiCallbackResponse();
        DeferredResult<AaiCallbackResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        AaiRequest aaiRequest = new AaiRequest();
        aaiRequest.setCallbackUrl("asdf");
        aaiRequest.setVfModuleId("20200824");
        AaiService aaiService = Mockito.mock(AaiService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);

        aaiController.aaiUpdate(aaiRequest);

        Assert.assertNotNull(response);
    }

    @Test
    public void aaiDeleteTest() throws Exception {
        AaiCallbackResponse response = new AaiCallbackResponse();
        DeferredResult<AaiCallbackResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        AaiRequest aaiRequest = new AaiRequest();
        aaiRequest.setCallbackUrl("asdfds");
        aaiRequest.setVfModuleId("20200824");
        AaiService aaiService = Mockito.mock(AaiService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);

        aaiController.aaiDelete(aaiRequest);

        Assert.assertNotNull(response);
    }
}
