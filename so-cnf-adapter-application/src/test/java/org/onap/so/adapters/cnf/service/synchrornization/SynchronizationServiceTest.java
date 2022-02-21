/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Samsung Technologies Co.
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
package org.onap.so.adapters.cnf.service.synchrornization;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SynchronizationServiceTest {

    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final String SUFFIX = "-cnf-adapter";
    private static final String SUBSCRIPTION_NAME = INSTANCE_ID + SUFFIX;

    @InjectMocks
    private SynchronizationService tested;

    @Mock
    private MulticloudClient multicloudClient;
    @Captor
    private ArgumentCaptor<SubscriptionRequest> subscriptionRequestCaptor;

    @Test
    public void shouldCreateSubscription() throws BadResponseException {
        // given
        String callbackUrl = "http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/INSTANCE_ID/status/notify";
        AaiRequest aaiRequest = mock(AaiRequest.class);

        // when
        when(aaiRequest.getInstanceId()).thenReturn(INSTANCE_ID);

        // then
        tested.createSubscriptionIfNotExists(aaiRequest);

        verify(multicloudClient).registerSubscription(eq(INSTANCE_ID), subscriptionRequestCaptor.capture());
        SubscriptionRequest subscriptionRequest = subscriptionRequestCaptor.getValue();
        assertEquals(SUBSCRIPTION_NAME, subscriptionRequest.getName());
        assertEquals(callbackUrl, subscriptionRequest.getCallbackUrl());
        assertEquals(30, subscriptionRequest.getMinNotifyInterval());
    }

    @Test
    public void shouldGetSubscriptionName() {
        // given
        // when
        // then
        String actual = tested.getSubscriptionName(INSTANCE_ID);
        assertEquals(SUBSCRIPTION_NAME, actual);
    }

    @Test
    public void shouldCheckIfSubscriptionActive() {
        // given
        // when
        // then
        boolean subscriptionActive = tested.isSubscriptionActive(INSTANCE_ID);
        assertFalse(subscriptionActive);
    }
}