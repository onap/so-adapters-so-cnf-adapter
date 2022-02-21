package org.onap.so.adapters.cnf.rest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.service.synchrornization.SubscriptionEndpointService;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SubscriptionEndpointServiceTest {

    private final static String ENABLED = "enabled";
    private final static String DISABLED = "DISABLED";
    private final static String ADDRESS = "http://so-cnf-adapter:8090";
    private final static String INSTANCE_ID = "INSTANCE-ID";
    private final static String ENDPOINT = "/instanceId/" + INSTANCE_ID;
    private final static String URL = ADDRESS + ENDPOINT;
    private SubscriptionEndpointService tested = new SubscriptionEndpointService();

    @Before
    public void setUp() {
        tested.enableEndpoint(ENABLED);
        tested.disableEndpoint(DISABLED);
    }

    @Test
    public void shouldReturnEnabledEndpoint() {
        boolean actual = tested.isEndpointActive(ENABLED);
        Assert.assertTrue(actual);
    }

    @Test
    public void shouldReturnDisabledEndpoint() {
        boolean actual = tested.isEndpointActive(DISABLED);
        Assert.assertFalse(actual);
    }


    @Test
    public void shouldGenerateCallbackEndpoint() {
        String actual = tested.generateCallbackEndpoint(INSTANCE_ID);
        Assert.assertEquals(URL, actual);
    }

    @Test
    public void shouldGenerateEndpointPath() {
        String actual = tested.generateEndpointPath(INSTANCE_ID);
        Assert.assertEquals(ENDPOINT, actual);
    }
}