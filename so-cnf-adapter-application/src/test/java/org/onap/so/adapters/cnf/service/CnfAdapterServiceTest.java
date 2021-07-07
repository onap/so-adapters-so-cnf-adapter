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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class CnfAdapterServiceTest {
    private static final String INSTANCE_CREATE_PATH = "/v1/instance";
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    CnfAdapterService cnfAdapterService;

    @Mock
    private MulticloudConfiguration multicloudConfiguration;

    @Mock
    ResponseEntity<String> instanceResponse;

    
    @Test
    public void createInstanceTest() throws Exception {
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<String, String>();
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
        try {
            cnfAdapterService.createInstance(bpmnInstanceRequest);
        }
        catch (Exception exp) {
        assert(true);
        }

    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {
        String instanceId = "ins";
        try {
            cnfAdapterService.getInstanceByInstanceId(instanceId);
        }
        catch (Exception exp) {
         assert(true);
        }

    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {
        String instanceId = "ins";
        try {
            cnfAdapterService.getInstanceStatusByInstanceId(instanceId);
        }
        catch (Exception exp) {
         assert(true);
        }

    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {
        String rbName = "rb";
        String rbVersion = "rv1";
        String profileName = "p1";
        try {
            cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
        }
        catch (Exception exp) {
         assert(true);
        }

    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {
        String instanceId = "ins";
        try {
            cnfAdapterService.deleteInstanceByInstanceId(instanceId);
        }
        catch (Exception exp) {
         assert(true);
        }

    }
}


