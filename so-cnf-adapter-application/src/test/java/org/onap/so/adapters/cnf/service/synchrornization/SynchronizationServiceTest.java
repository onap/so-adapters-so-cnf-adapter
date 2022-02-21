package org.onap.so.adapters.cnf.service.synchrornization;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SynchronizationServiceTest {
    // TODO: 03.03.2022 IMPLEMENT ME
    // TODO: 03.03.2022 IMPLEMENT ME
    // TODO: 03.03.2022 IMPLEMENT ME

    private static final String INSTANCE_ID = "INSTANCE_ID";

    @InjectMocks
    private SynchronizationService tested;

    @Mock
    private SubscriptionEndpointService subscriptionEndpointService;

    @Mock
    private MulticloudClient multicloudClient;

    @Mock
    private SubscriptionNameProvider subscriptionNameProvider;

    @Test
    public void shouldCreateSubscription() throws BadResponseException {
        // given
        String callbackEndpoint = "callbackEndpoint";
        String endpointPath = "endpointPath";
        String name = "subscriptionName";

        // when
        when(subscriptionEndpointService.generateCallbackEndpoint(INSTANCE_ID)).thenReturn(callbackEndpoint);
        when(subscriptionEndpointService.generateEndpointPath(INSTANCE_ID)).thenReturn(endpointPath);
        when(subscriptionNameProvider.generateName()).thenReturn(name);


        // than
        tested.createSubscription(INSTANCE_ID);

    }
}