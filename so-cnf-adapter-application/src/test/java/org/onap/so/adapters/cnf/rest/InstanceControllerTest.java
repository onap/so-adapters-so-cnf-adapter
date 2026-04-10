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
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceMiniResponse;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.Resource;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.onap.so.adapters.cnf.service.upgrade.InstanceUpgradeService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;


@SpringBootTest
@RunWith(SpringRunner.class)
public class InstanceControllerTest {

    @InjectMocks
    InstanceController instanceController;

    @Mock
    CnfAdapterService cnfAdapterService;

    @Mock
    SynchronizationService synchService;

    @Mock
    InstanceUpgradeService instanceUpgradeService;

    @Mock
    ResponseEntity<InstanceMiniResponseList> instacneMiniResponseList;

    @Mock
    ResponseEntity<InstanceStatusResponse> instanceStatusResponse;

    @Test
    public void createInstanceTest() throws Exception {

        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<String, String>();
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        bpmnInstanceRequest.setCloudRegionId("v1");
        bpmnInstanceRequest.setLabels(labels);
        bpmnInstanceRequest.setModelInvariantId("krd");
        bpmnInstanceRequest.setModelVersionId("p1");
        bpmnInstanceRequest.setOverrideValues(overrideValues);
        bpmnInstanceRequest.setVfModuleUUID("20200824");
        List<Resource> resourceList = new ArrayList<Resource>();
        InstanceResponse instanceResponse = new InstanceResponse();
        instanceResponse.setId("123");
        instanceResponse.setNamespace("testNamespace");
        instanceResponse.setRequest(new MulticloudInstanceRequest());
        instanceResponse.setResources(resourceList);
        ResponseEntity<String> createInstanceResponse = new ResponseEntity<String>(HttpStatus.CREATED);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.createInstance(bpmnInstanceRequest))
                .thenReturn(String.valueOf(createInstanceResponse));
        instanceController.createInstance(bpmnInstanceRequest);
        Assert.assertNotNull(createInstanceResponse);
        assertEquals(HttpStatus.CREATED, createInstanceResponse.getStatusCode());
    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        ResponseEntity<String> createInstanceResponse = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByInstanceId(instanceId))
                .thenReturn(String.valueOf(createInstanceResponse));
        instanceController.getInstanceByInstanceId(instanceId);
        Assert.assertNotNull(createInstanceResponse);
        assertEquals(HttpStatus.OK, createInstanceResponse.getStatusCode());
    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        SynchronizationService synchService = Mockito.mock(SynchronizationService.class);
        Mockito.when(cnfAdapterService.deleteInstanceByInstanceId(instanceId)).thenReturn(String.valueOf(response));
        doCallRealMethod().when(synchService).deleteSubscriptionIfExists(instanceId);
        instanceController.deleteInstanceByInstanceId(instanceId);
        Assert.assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {

        String instanceId = "123";
        instanceStatusResponse = new ResponseEntity<InstanceStatusResponse>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceStatusByInstanceId(instanceId))
                .thenReturn(String.valueOf(instanceStatusResponse));
        instanceController.getInstanceStatusByInstanceId(instanceId);
        Assert.assertNotNull(instanceStatusResponse);
        assertEquals(HttpStatus.OK, instanceStatusResponse.getStatusCode());
    }

    @Test
    public void queryInstanceResourcesTest() {
        String instanceId = "123";
        String kind = "Service";
        String apiVersion = "v1";
        String queryResponseMock = "queryResponseMock";

        Mockito.when(cnfAdapterService.queryInstanceResources(instanceId, kind, apiVersion, null, null,
                null)).thenReturn(queryResponseMock);

        String result = instanceController.queryInstanceResources(instanceId, kind, apiVersion, null, null,
                null);
        assertThat(result).isEqualTo(queryResponseMock);
    }

    @Test
    public void queryResourcesTest() {
        String kind = "Service";
        String apiVersion = "v1";
        String cloudRegion = "region";
        String queryResponseMock = "queryResponseMock";

        Mockito.when(cnfAdapterService.queryResources(kind, apiVersion, null, null,
                null, cloudRegion)).thenReturn(queryResponseMock);

        String result = instanceController.queryResources(kind, apiVersion, null, null,
                null, cloudRegion);
        assertThat(result).isEqualTo(queryResponseMock);
    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {

        String rbName = "xyz";
        String rbVersion = "v1";
        String profileName = "p1";
        MulticloudInstanceRequest request = null;
        InstanceMiniResponse instanceMiniResponse = new InstanceMiniResponse(HttpStatus.OK.toString());

        instanceMiniResponse.setId("a");
        instanceMiniResponse.setNameSpace("ab");
        instanceMiniResponse.setErrorMsg("Error");
        instanceMiniResponse.setRequest(request);

        instanceMiniResponse.getId();
        instanceMiniResponse.getNameSpace();
        instanceMiniResponse.getRequest();
        instanceMiniResponse.getErrorMsg();

        List<InstanceMiniResponse> instancList = new ArrayList<InstanceMiniResponse>();
        instancList.add(instanceMiniResponse);
        InstanceMiniResponseList instanceMiniRespList = new InstanceMiniResponseList(HttpStatus.OK.toString());
        instanceMiniRespList.setInstancList(instancList);
        instanceMiniRespList.setErrorMsg(HttpStatus.OK.toString());
        ResponseEntity<InstanceMiniResponseList> respone =
                new ResponseEntity<InstanceMiniResponseList>(instanceMiniRespList, HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName))
                .thenReturn(String.valueOf(instanceMiniResponse));
        instanceController.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
        Assert.assertNotNull(instacneMiniResponseList);
        assertEquals(HttpStatus.OK.toString(), instanceMiniRespList.getErrorMsg());
    }

    @Test
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
        instanceController.upgrade("123", instanceUpgradeRequest);
        Assert.assertNotNull(upgradeResponse);
        assertEquals(HttpStatus.OK, upgradeResponse.getStatusCode());
    }
}
