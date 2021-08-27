package org.onap.so.adapters.cnf.service.aai;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AaiServiceTest {

    @InjectMocks
    private AaiService aaiServiceTested;

    @Mock
    private MulticloudClient multicloudClient;
    @Mock
    private AaiRequestSender aaiRequestSender;
    @Mock
    private AaiResponseParser responseParser;

    @Test
    public void shouldTestAaiUpdate() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        AaiRequest aaiRequest = mock(AaiRequest.class);
        K8sRbInstanceStatus instanceStatus = mock(K8sRbInstanceStatus.class);
        K8sRbInstanceResourceStatus status = mock(K8sRbInstanceResourceStatus.class);
        List<K8sRbInstanceResourceStatus> resourcesStatus = new ArrayList<>();
        resourcesStatus.add(status);
        ParseResult parseResult = mock(ParseResult.class);
        List<ParseResult> parseResultList = new ArrayList<>();
        parseResultList.add(parseResult);

        // when
        when(aaiRequest.getInstanceId()).thenReturn(instanceId);
        when(multicloudClient.getInstanceStatus(instanceId)).thenReturn(instanceStatus);
        when(instanceStatus.getResourcesStatus()).thenReturn(resourcesStatus);
        when(responseParser.parse(status, aaiRequest)).thenReturn(parseResult);
        doNothing().when(aaiRequestSender).sendUpdateRequestToAai(parseResult, aaiRequest);

        // then
        aaiServiceTested.aaiUpdate(aaiRequest);

        verify(responseParser, atLeast(1)).parse(status, aaiRequest);
        verify(aaiRequestSender, atLeast(1)).sendUpdateRequestToAai(parseResult, aaiRequest);
    }


    @Test
    public void shouldTestAaiDelete() throws BadResponseException {
        // given
        String instanceId = "instanceId";
        AaiRequest aaiRequest = mock(AaiRequest.class);
        K8sRbInstanceStatus instanceStatus = mock(K8sRbInstanceStatus.class);
        K8sRbInstanceResourceStatus status = mock(K8sRbInstanceResourceStatus.class);
        List<K8sRbInstanceResourceStatus> resourcesStatus = new ArrayList<>();
        resourcesStatus.add(status);
        ParseResult parseResult = mock(ParseResult.class);
        List<ParseResult> parseResultList = new ArrayList<>();
        parseResultList.add(parseResult);

        // when
        when(aaiRequest.getInstanceId()).thenReturn(instanceId);
        when(multicloudClient.getInstanceStatus(instanceId)).thenReturn(instanceStatus);
        when(instanceStatus.getResourcesStatus()).thenReturn(resourcesStatus);
        when(responseParser.parse(status, aaiRequest)).thenReturn(parseResult);
        doNothing().when(aaiRequestSender).sendUpdateRequestToAai(parseResult, aaiRequest);

        // then
        aaiServiceTested.aaiDelete(aaiRequest);

        verify(responseParser, atLeast(1)).parse(status, aaiRequest);
        verify(aaiRequestSender, atLeast(1)).sendDeleteRequestToAai(parseResult,  aaiRequest);
    }
}