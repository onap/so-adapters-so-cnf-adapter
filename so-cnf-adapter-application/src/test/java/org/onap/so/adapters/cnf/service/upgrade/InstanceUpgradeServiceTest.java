/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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
package org.onap.so.adapters.cnf.service.upgrade;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.upgrade.InstanceUpgradeRequest;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InstanceUpgradeServiceTest {

    @InjectMocks
    private InstanceUpgradeService tested;

    @Mock
    MulticloudClient instanceApi;

    @Captor
    ArgumentCaptor<MulticloudInstanceRequest> multicloudInstanceRequestCaptor;


    @Test
    public void shouldUpgradeInstanceSuccessfully() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        InstanceUpgradeRequest upgradeRequest = mock(InstanceUpgradeRequest.class);
        String k8sRBProfileName = "k8sRBProfileName";
        String modelInvariantId = "modelInvariantId";
        String modelCustomizationId = "modelCustomizationId";
        String cloudRegionId = "cloudRegionId";
        Map<String, String> labels = mock(Map.class);
        Map<String, String> overrideValues = mock(Map.class);
        String response = "response";

        // when
        when(upgradeRequest.getK8sRBProfileName()).thenReturn(k8sRBProfileName);
        when(upgradeRequest.getModelInvariantId()).thenReturn(modelInvariantId);
        when(upgradeRequest.getModelCustomizationId()).thenReturn(modelCustomizationId);
        when(upgradeRequest.getCloudRegionId()).thenReturn(cloudRegionId);
        when(upgradeRequest.getLabels()).thenReturn(labels);
        when(upgradeRequest.getOverrideValues()).thenReturn(overrideValues);
        when(instanceApi.upgradeInstance(eq(instanceId), any(MulticloudInstanceRequest.class))).thenReturn(response);

        // than
        String actual = tested.upgradeInstance(instanceId, upgradeRequest);

        assertEquals(actual, response);
        verify(instanceApi).upgradeInstance(eq(instanceId), multicloudInstanceRequestCaptor.capture());
        MulticloudInstanceRequest requestCaptureValue = multicloudInstanceRequestCaptor.getValue();
        assertEquals(requestCaptureValue.getRbName(), modelInvariantId);
        assertEquals(requestCaptureValue.getRbVersion(), modelCustomizationId);
        assertEquals(requestCaptureValue.getProfileName(), k8sRBProfileName);
        assertEquals(requestCaptureValue.getLabels(), labels);
        assertEquals(requestCaptureValue.getOverrideValues(), overrideValues);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionWhenProfileNameIsNull() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        InstanceUpgradeRequest upgradeRequest = mock(InstanceUpgradeRequest.class);

        // when
        when(upgradeRequest.getK8sRBProfileName()).thenReturn(null);

        // than

        tested.upgradeInstance(instanceId, upgradeRequest);
    }

}