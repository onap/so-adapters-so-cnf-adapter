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
import org.onap.so.adapters.cnf.model.synchronization.SubscriptionRequest;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.eq;
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
    private SubscriptionEndpointService subscriptionEndpointService;
    @Mock
    private MulticloudClient multicloudClient;
    @Mock
    private SubscriptionNameProvider subscriptionNameProvider;
    @Captor
    private ArgumentCaptor<String> callbackEndpointCaptor;
    @Captor
    private ArgumentCaptor<SubscriptionRequest> subscriptionRequestCaptor;

    @Test
    public void shouldCreateSubscription() throws BadResponseException {
        // given
        String callbackEndpoint = "callbackEndpoint";
        String endpointPath = "endpointPath";

        // when
        when(subscriptionEndpointService.generateCallbackEndpoint(INSTANCE_ID)).thenReturn(callbackEndpoint);
        when(subscriptionEndpointService.generateEndpointPath(INSTANCE_ID)).thenReturn(endpointPath);
        when(subscriptionNameProvider.generateName()).thenReturn(NAME);

        // then
        tested.createSubscription(INSTANCE_ID);

        verify(subscriptionEndpointService).enableEndpoint(callbackEndpointCaptor.capture());
        verify(multicloudClient).registerSubscription(eq(INSTANCE_ID), subscriptionRequestCaptor.capture());
        Assert.assertEquals(callbackEndpointCaptor.getValue(), endpointPath);
        Assert.assertEquals(callbackEndpoint, subscriptionRequestCaptor.getValue().getCallbackUrl());
        Assert.assertEquals(NAME, subscriptionRequestCaptor.getValue().getName());
        Assert.assertEquals(30, subscriptionRequestCaptor.getValue().getMinNotifyInterval());
    }


}