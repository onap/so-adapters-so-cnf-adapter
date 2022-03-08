/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.client.SoCallbackClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.aai.AaiCallbackResponse;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckResponse;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.service.statuscheck.SimpleStatusCheckService;
import org.onap.so.adapters.cnf.service.upgrade.InstanceUpgradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
public class CnfRestTest {

    @InjectMocks
    org.onap.so.adapters.cnf.rest.CnfAdapterRest cnfAdapterRest;

    @Mock
    InstanceUpgradeService instanceUpgradeService;

    @Mock
    HealthCheckService healthCheckService;

    @Mock
    private SoCallbackClient callbackClient;

    @Mock
    SimpleStatusCheckService simpleStatusCheckService;

    @Mock
    ResponseEntity<InstanceMiniResponseList> instacneMiniResponseList;

    @Mock
    ResponseEntity<InstanceStatusResponse> instanceStatusResponse;

    @Test(expected = EntityNotFoundException.class)
    public void upgradeTest() throws Exception {

        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<String, String>();
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        InstanceUpgradeRequest instanceUpgradeRequest = new InstanceUpgradeRequest();
        instanceUpgradeRequest.setCloudRegionId("v1");
        instanceUpgradeRequest.setK8sRBInstanceStatusCheck(true);
        instanceUpgradeRequest.setK8sRBProfileName("test");
        instanceUpgradeRequest.setLabels(labels);
        instanceUpgradeRequest.setModelCustomizationId("12345");
        instanceUpgradeRequest.setModelInvariantId("krd");
        instanceUpgradeRequest.setOverrideValues(overrideValues);
        instanceUpgradeRequest.setVfModuleUUID("20200824");

        ResponseEntity<String> upgradeResponse = new ResponseEntity<String>(HttpStatus.OK);
        InstanceUpgradeService instanceUpgradeService = Mockito.mock(InstanceUpgradeService.class);
        Mockito.when(instanceUpgradeService.upgradeInstance("123", instanceUpgradeRequest))
                .thenReturn(String.valueOf(upgradeResponse));
        cnfAdapterRest.upgrade("123", instanceUpgradeRequest);
        Assert.assertNotNull(upgradeResponse);
        assertEquals(HttpStatus.OK, upgradeResponse.getStatusCode());
    }

    @Test(expected = EntityNotFoundException.class)
    public void healthCheckTest() throws Exception {
        HealthCheckResponse response = new HealthCheckResponse();
        DeferredResult<HealthCheckResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);
        HealthCheckService healthCheckService = Mockito.mock(HealthCheckService.class);
        CheckInstanceRequest healthCheckRequest = Mockito.mock(CheckInstanceRequest.class);
        Mockito.when(healthCheckService.healthCheck(healthCheckRequest)).thenReturn(response);

        cnfAdapterRest.healthCheck(healthCheckRequest);

        Assert.assertNotNull(response);
    }

    @Test(expected = EntityNotFoundException.class)
    public void aaiUpdateTest() throws Exception {
        AaiCallbackResponse response = new AaiCallbackResponse();
        DeferredResult<AaiCallbackResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        AaiRequest aaiRequest = new AaiRequest();
        aaiRequest.setCallbackUrl("asdf");
        aaiRequest.setVfModuleId("20200824");
        AaiService aaiService = Mockito.mock(AaiService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);

        cnfAdapterRest.aaiUpdate(aaiRequest);

        Assert.assertNotNull(response);
    }

    @Test(expected = EntityNotFoundException.class)
    public void aaiDeleteTest() throws Exception {
        AaiCallbackResponse response = new AaiCallbackResponse();
        DeferredResult<AaiCallbackResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        AaiRequest aaiRequest = new AaiRequest();
        aaiRequest.setCallbackUrl("asdfds");
        aaiRequest.setVfModuleId("20200824");
        AaiService aaiService = Mockito.mock(AaiService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);

        cnfAdapterRest.aaiDelete(aaiRequest);

        Assert.assertNotNull(response);
    }

    @Test(expected = EntityNotFoundException.class)
    public void statusCheckTest() throws Exception {
        StatusCheckResponse response = new StatusCheckResponse();
        DeferredResult<StatusCheckResponse> deferredResponse = new DeferredResult<>();
        deferredResponse.setResult(response);

        CheckInstanceRequest statusCheckRequest = Mockito.mock(CheckInstanceRequest.class);
        SimpleStatusCheckService simpleStatusCheckService = Mockito.mock(SimpleStatusCheckService.class);
        SoCallbackClient callbackClient = Mockito.mock(SoCallbackClient.class);
        Mockito.when(simpleStatusCheckService.statusCheck(statusCheckRequest)).thenReturn(response);
        cnfAdapterRest.statusCheck(statusCheckRequest);

        Assert.assertNotNull(response);
    }
}
