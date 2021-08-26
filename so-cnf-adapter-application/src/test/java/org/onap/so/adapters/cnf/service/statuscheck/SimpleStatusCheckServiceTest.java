package org.onap.so.adapters.cnf.service.statuscheck;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.StatusCheckResponse;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SimpleStatusCheckServiceTest {

    @InjectMocks
    private SimpleStatusCheckService tested;

    @Mock
    private MulticloudClient instanceApi;


    @Test
    public void shouldReturnTrueStatusCheck() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        boolean isReady = true;
        K8sRbInstanceStatus instanceStatus = mock(K8sRbInstanceStatus.class);
        CheckInstanceRequest instanceIds = mock(CheckInstanceRequest.class);
        List<InstanceRequest> instanceRequests = new ArrayList<>();
        InstanceRequest instanceRequest = mock(InstanceRequest.class);
        instanceRequests.add(instanceRequest);

        // when
        when(instanceIds.getInstances()).thenReturn(instanceRequests);
        when(instanceRequest.getInstanceId()).thenReturn(instanceId);
        when(instanceApi.getInstanceStatus(instanceId)).thenReturn(instanceStatus);
        when(instanceStatus.isReady()).thenReturn(isReady);

        // then
        StatusCheckResponse actual = tested.statusCheck(instanceIds);

        Assert.assertFalse(actual.getInstanceResponse().isEmpty());
        Assert.assertEquals(1, actual.getInstanceResponse().size());
        Assert.assertTrue(actual.getInstanceResponse().get(0).isStatus());
        Assert.assertEquals(instanceId, actual.getInstanceResponse().get(0).getInstanceId());
    }

    @Test
    public void shouldReturnFalseStatusCheck() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        boolean isReady = false;
        K8sRbInstanceStatus instanceStatus = mock(K8sRbInstanceStatus.class);
        CheckInstanceRequest instanceIds = mock(CheckInstanceRequest.class);
        List<InstanceRequest> instanceRequests = new ArrayList<>();
        InstanceRequest instanceRequest = mock(InstanceRequest.class);
        instanceRequests.add(instanceRequest);

        // when
        when(instanceIds.getInstances()).thenReturn(instanceRequests);
        when(instanceRequest.getInstanceId()).thenReturn(instanceId);
        when(instanceApi.getInstanceStatus(instanceId)).thenReturn(instanceStatus);
        when(instanceStatus.isReady()).thenReturn(isReady);

        // then
        StatusCheckResponse actual = tested.statusCheck(instanceIds);

        Assert.assertFalse(actual.getInstanceResponse().isEmpty());
        Assert.assertEquals(1, actual.getInstanceResponse().size());
        Assert.assertFalse(actual.getInstanceResponse().get(0).isStatus());
        Assert.assertEquals(instanceId, actual.getInstanceResponse().get(0).getInstanceId());
    }
}