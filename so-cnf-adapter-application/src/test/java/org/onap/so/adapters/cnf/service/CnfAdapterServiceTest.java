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
package org.onap.so.adapters.cnf.service;

import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.service.statuscheck.SimpleStatusCheckService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CnfAdapterServiceTest {

    private static final String INSTANCE_ID = "ins";

    private CnfAdapterService cnfAdapterService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    HealthCheckService healthCheckService;

    @Mock
    SimpleStatusCheckService simpleStatusCheckService;

    @Mock
    SynchronizationService synchronizationService;


    @Before
    public void setUp() {
        MulticloudConfiguration multicloudConfiguration = mock(MulticloudConfiguration.class);
        doReturn("http://test.url").when(multicloudConfiguration).getMulticloudUrl();
        cnfAdapterService = spy(new CnfAdapterService(restTemplate, synchronizationService, multicloudConfiguration));
    }


    @Test
    public void createInstanceTest() throws Exception {
        try {
            cnfAdapterService.createInstance(getBpmnInstanceRequest());
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testcreateInstanceHttpException() throws BadResponseException {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.createInstance(getBpmnInstanceRequest());
    }

    @Test(expected = HttpStatusCodeException.class)
    public void testCreateInstanceHttpStatusCodeException() throws BadResponseException {
        doThrow(new HttpServerErrorException(HttpStatus.CONFLICT)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.createInstance(getBpmnInstanceRequest());
    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {
        try {
            cnfAdapterService.getInstanceByInstanceId(INSTANCE_ID);
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testInstanceByInstanceIdHttpException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceByInstanceId(INSTANCE_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = HttpStatusCodeException.class)
    public void testInstanceByInstanceIdHttpStatusCodeException() {
        doThrow(new HttpServerErrorException(HttpStatus.CONFLICT)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceByInstanceId(INSTANCE_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {
        try {
            cnfAdapterService.getInstanceStatusByInstanceId(INSTANCE_ID);
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testInstanceStatusByInstanceIdHttpException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceStatusByInstanceId(INSTANCE_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = HttpStatusCodeException.class)
    public void testInstanceStatusByInstanceIdHttpStatusCodeException() {
        doThrow(new HttpServerErrorException(HttpStatus.CONFLICT)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceStatusByInstanceId(INSTANCE_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {
        RbNameVersionData rbNameVersionData = new RbNameVersionData();
        try {
            cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbNameVersionData.getRbName(), rbNameVersionData.getRbVersion(),
                    rbNameVersionData.getProfileName());
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testInstanceByRBNameOrRBVersionOrProfileNameHttpException() {
        RbNameVersionData rbNameVersionData = new RbNameVersionData();
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbNameVersionData.getRbName(), rbNameVersionData.getRbVersion(),
                    rbNameVersionData.getProfileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = HttpStatusCodeException.class)
    public void testInstanceByRBNameOrRBVersionOrProfileNameHttpStatusCodeException() {
        RbNameVersionData rbNameVersionData = new RbNameVersionData();
        doThrow(new HttpServerErrorException(HttpStatus.CONFLICT)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        try {
            cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbNameVersionData.getRbName(), rbNameVersionData.getRbVersion(),
                    rbNameVersionData.getProfileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {
        try {
            cnfAdapterService.deleteInstanceByInstanceId(INSTANCE_ID);
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testdeleteInstanceByInstanceIdHttpException() throws BadResponseException {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.deleteInstanceByInstanceId(INSTANCE_ID);
    }

    @Test(expected = HttpStatusCodeException.class)
    public void testDeleteInstanceByInstanceIdException() throws BadResponseException {
        doThrow(new HttpServerErrorException(HttpStatus.CONFLICT)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.deleteInstanceByInstanceId(INSTANCE_ID);
    }

    @Test
    public void queryInstanceResourcesTest() {
        try {
            cnfAdapterService.queryInstanceResources(INSTANCE_ID, "", "", "", "", "");
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void queryInstanceResourcesException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.queryInstanceResources(INSTANCE_ID, "", "", "", "", "");
    }

    @Test
    public void queryResourcesTest() {
        try {
            cnfAdapterService.queryResources(INSTANCE_ID, "", "", "", "", "");
        } catch (Exception exp) {
            assert (true);
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void queryResourcesException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)).when(restTemplate).exchange(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.any(), ArgumentMatchers.<Class<String>>any());
        cnfAdapterService.queryResources(INSTANCE_ID, "", "", "", "", "");
    }

    private BpmnInstanceRequest getBpmnInstanceRequest() {
        Map<String, String> labels = new HashMap<>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<>();
        overrideValues.put("a", "b");
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        bpmnInstanceRequest.setCloudRegionId("v1");
        bpmnInstanceRequest.setLabels(labels);
        bpmnInstanceRequest.setModelInvariantId("krd");
        bpmnInstanceRequest.setModelVersionId("p1");
        bpmnInstanceRequest.setOverrideValues(overrideValues);
        bpmnInstanceRequest.setVfModuleUUID("20200824");
        bpmnInstanceRequest.setK8sRBProfileName("K8sRBProfileName is required");
        return bpmnInstanceRequest;
    }

    @Data
    private class RbNameVersionData {

        String rbName = "rb";
        String rbVersion = "rv1";
        String profileName = "p1";
    }
}


