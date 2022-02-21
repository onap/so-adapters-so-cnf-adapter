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

import org.junit.Assert;
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
import org.onap.so.adapters.cnf.service.aai.AaiService;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SynchronizationServiceTest {

    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final String NAME = "subscriptionName";

    @InjectMocks
    private SynchronizationService tested;

    @Mock
    private AaiService aaiService;
    @Mock
    private SubscriptionNameProvider subscriptionNameProvider;
    @Mock
    private MulticloudClient multicloudClient;
    @Captor
    private ArgumentCaptor<SubscriptionRequest> subscriptionRequestCaptor;
    @Captor
    private ArgumentCaptor<AaiRequest> aaiRequestCaptor;

    @Test
    public void shouldCreateSubscription() throws BadResponseException {
        // given
        String callbackUrl = "http://so-cnf-adapter:8090/cnf-notify/instanceId/INSTANCE_ID/name/subscriptionName";
        AaiRequest aaiRequest = mock(AaiRequest.class);

        // when
        when(subscriptionNameProvider.generateName()).thenReturn(NAME);

        // then
        tested.createSubscription(INSTANCE_ID, aaiRequest);

        verify(multicloudClient).registerSubscription(eq(INSTANCE_ID), subscriptionRequestCaptor.capture());
        verify(aaiService).aaiUpdate(aaiRequestCaptor.capture());
        Assert.assertEquals(callbackUrl, subscriptionRequestCaptor.getValue().getCallbackUrl());
        Assert.assertEquals(NAME, subscriptionRequestCaptor.getValue().getName());
        Assert.assertEquals(30, subscriptionRequestCaptor.getValue().getMinNotifyInterval());
        Assert.assertSame(aaiRequest, aaiRequestCaptor.getValue());
    }


}