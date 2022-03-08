package org.onap.so.adapters.cnf.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class MulticloudApiUrlTest {

    private static final String BASE_URL = "http://test-multicloud.com:8080";

    @InjectMocks
    private MulticloudApiUrl tested;

    @Mock
    private MulticloudConfiguration multicloudConfiguration;

    @Test
    public void shouldPresentInstanceIdInPath() {
        // given
        String instanceId = "instanceId";
        // when
        when(multicloudConfiguration.getMulticloudUrl()).thenReturn(BASE_URL);

        // then
        String actual = tested.apiUrl(instanceId);

        assertEquals(BASE_URL + "/v1/instance/instanceId", actual);
    }

}