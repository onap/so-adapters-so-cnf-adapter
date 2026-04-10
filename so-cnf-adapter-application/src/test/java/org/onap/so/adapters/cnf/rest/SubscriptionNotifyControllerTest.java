/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.NotificationRequest;
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.adapters.cnf.service.synchrornization.SynchronizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SubscriptionNotifyControllerTest {

    private static final String INSTANCE_ID = "test-instance-id";
    private static final String SUBSCRIPTION_NAME = INSTANCE_ID + "-cnf-adapter";

    @InjectMocks
    private SubscriptionNotifyController controller;

    @Mock
    private AaiService aaiService;

    @Mock
    private SynchronizationService synchronizationService;

    @Test
    public void shouldReturn202Accepted_whenSubscriptionIsActive() throws Exception {
        // given
        AaiRequest metadata = new AaiRequest();
        NotificationRequest body = new NotificationRequest();
        body.setMetadata(metadata);

        when(synchronizationService.getSubscriptionName(INSTANCE_ID)).thenReturn(SUBSCRIPTION_NAME);
        when(synchronizationService.isSubscriptionActive(SUBSCRIPTION_NAME)).thenReturn(true);

        // when
        ResponseEntity response = controller.subscriptionNotifyEndpoint(INSTANCE_ID, body);

        // then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(aaiService).aaiUpdate(metadata);
    }

    @Test
    public void shouldReturn400BadRequest_whenSubscriptionIsNotActive() throws Exception {
        // given
        NotificationRequest body = new NotificationRequest();

        when(synchronizationService.getSubscriptionName(INSTANCE_ID)).thenReturn(SUBSCRIPTION_NAME);
        when(synchronizationService.isSubscriptionActive(SUBSCRIPTION_NAME)).thenReturn(false);

        // when
        ResponseEntity response = controller.subscriptionNotifyEndpoint(INSTANCE_ID, body);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(SUBSCRIPTION_NAME));
        verifyNoInteractions(aaiService);
    }
}
