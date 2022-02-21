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
package org.onap.so.adapters.cnf.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionResponse;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@RunWith(SpringRunner.class)
public class MulticloudClientTest {

    private static final String instanceId = "INSTANCE_ID";
    private static final String endpoint = "API_URL";

    @InjectMocks
    private MulticloudClient tested;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private MulticloudApiUrl multicloudApiUrl;
    @Captor
    private ArgumentCaptor<String> instanceIdCaptor;


    @Test
    public void shouldRegisterSubscription() throws BadResponseException {
        // given
        SubscriptionRequest request = mock(SubscriptionRequest.class);
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "{\"name\":\"name\"}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/status/subscription")), eq(POST), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        SubscriptionResponse actual = tested.registerSubscription(instanceId, request);

        assertEquals("name", actual.getName());
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldGetSubscription() throws BadResponseException {
        // given
        String request = "subscriptionId";
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "{\"name\":\"name\"}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/status/subscription/" + request)), eq(GET), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        SubscriptionResponse actual = tested.getSubscription(instanceId, request);

        assertEquals("name", actual.getName());
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldDeleteSubscription() throws BadResponseException {
        // given
        String request = "subscriptionId";
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "{\"name\":\"name\"}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/status/subscription/" + request)), eq(DELETE), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        tested.deleteSubscription(instanceId, request);

        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldUpgradeInstance() throws BadResponseException {
        // given
        MulticloudInstanceRequest request = mock(MulticloudInstanceRequest.class);
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "body";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/upgrade")), eq(POST), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        String actual = tested.upgradeInstance(instanceId, request);

        assertEquals(body, actual);
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldGetInstanceStatus() throws BadResponseException {
        // given
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "{\"ready\":true}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/status")), eq(GET), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        K8sRbInstanceStatus actual = tested.getInstanceStatus(instanceId);

        assertEquals(true, actual.isReady());
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldStartInstanceHealthCheck() throws BadResponseException {
        // given
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "{\"status\":\"SUCCEED\"}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/healthcheck")), eq(POST), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        K8sRbInstanceHealthCheckSimple actual = tested.startInstanceHealthCheck(instanceId);

        assertEquals("SUCCEED", actual.getStatus());
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldGetInstanceHealthCheck() throws BadResponseException {
        // given
        ResponseEntity result = mock(ResponseEntity.class);
        String healthCheckInstance = "healthCheckInstance";
        String body = "{\"status\":\"SUCCEED\"}";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/healthcheck/" + healthCheckInstance)), eq(GET), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        K8sRbInstanceHealthCheck actual = tested.getInstanceHealthCheck(instanceId, healthCheckInstance);

        assertEquals("SUCCEED", actual.getStatus());
        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldDeleteInstanceHealthCheck() throws BadResponseException {
        // given
        ResponseEntity result = mock(ResponseEntity.class);
        String healthCheckInstance = "healthCheckInstance";
        String body = "body";

        // when
        when(multicloudApiUrl.apiUrl(instanceId)).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("/healthcheck/" + healthCheckInstance)), eq(DELETE), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        tested.deleteInstanceHealthCheck(instanceId, healthCheckInstance);

        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertInstanceIdCaperedValue(instanceIdCaptor.getValue());
    }

    @Test
    public void shouldGetAllInstances() throws BadResponseException {
        // given
        ResponseEntity result = mock(ResponseEntity.class);
        String body = "[{\"id\":\"clever_proskuriakova\",\"release-name\":\"rel-1-apache\",\"namespace\":\"test-cnf\"}]";

        // when
        when(multicloudApiUrl.apiUrl("")).thenReturn(endpoint);
        when(restTemplate.exchange(eq(getUrl("")), eq(GET), any(), eq(String.class))).thenReturn(result);
        when(result.getStatusCode()).thenReturn(HttpStatus.OK);
        when(result.getBody()).thenReturn(body);

        // then
        List<InstanceResponse> actual = tested.getAllInstances();

        verify(multicloudApiUrl).apiUrl(instanceIdCaptor.capture());
        assertEquals("", instanceIdCaptor.getValue());
        assertEquals(1, actual.size());
        assertEquals("clever_proskuriakova", actual.get(0).getId());
        assertEquals("rel-1-apache", actual.get(0).getReleaseName());
        assertEquals("test-cnf", actual.get(0).getNamespace());
    }

    private void assertInstanceIdCaperedValue(String instanceIdCapturedValue) {
        assertEquals(instanceId, instanceIdCapturedValue);
    }

    private String getUrl(String path) {
        return endpoint + path;
    }
}