package org.onap.so.adapters.cnf.service.aai;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AaiResponseParserTest {

    @InjectMocks
    private AaiResponseParser aaiResponseParser;

    @Mock
    private AaiIdGeneratorService aaiIdGeneratorService;


    @Test
    public void shouldParseAaiResponse() {
        // given
        String id = "id";
        String name = "name";
        String group = "group";
        String version = "version";
        String kind = "kind";
        String namespace = "namespace";
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("key", "value");
        K8sRbInstanceResourceStatus status = mock(K8sRbInstanceResourceStatus.class);
        AaiRequest aaiRequest = mock(AaiRequest.class);
        K8sRbInstanceGvk gvk = mock(K8sRbInstanceGvk.class);
        K8sStatusMetadata metadata = mock(K8sStatusMetadata.class);
        K8sStatus k8sStatus = mock(K8sStatus.class);

        // when
        when(status.getGvk()).thenReturn(gvk);
        when(status.getStatus()).thenReturn(k8sStatus);
        when(k8sStatus.getK8sStatusMetadata()).thenReturn(metadata);
        when(aaiIdGeneratorService.generateId(status, aaiRequest)).thenReturn(id);
        when(status.getName()).thenReturn(name);
        when(gvk.getGroup()).thenReturn(group);
        when(gvk.getVersion()).thenReturn(version);
        when(gvk.getKind()).thenReturn(kind);
        when(metadata.getNamespace()).thenReturn(namespace);
        when(aaiRequest.getInstanceId()).thenReturn(id);

        when(metadata.getLabels()).thenReturn(labelsMap);

        // then
        K8sResource actual = aaiResponseParser.parse(status, aaiRequest);

        Assert.assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(name, actual.getName());
        assertEquals(group, actual.getGroup());
        assertEquals(version, actual.getVersion());
        assertEquals(kind, actual.getKind());
        assertEquals(namespace, actual.getNamespace());
        assertEquals(2, actual.getLabels().size());
        assertEquals("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/id/query", actual.getK8sResourceSelfLink());

    }

    K8sResource parse(K8sRbInstanceResourceStatus status, AaiRequest aaiRequest) {
        K8sResource result = new K8sResource();
        K8sRbInstanceGvk gvk = status.getGvk();
        K8sStatus k8sStatus = status.getStatus();
        K8sStatusMetadata metadata = k8sStatus.getK8sStatusMetadata();
        String id = aaiIdGeneratorService.generateId(status, aaiRequest);
        result.setId(id);
        result.setName(status.getName());
        result.setGroup(gvk.getGroup());
        result.setVersion(gvk.getVersion());
        result.setKind(gvk.getKind());
        result.setNamespace(metadata.getNamespace());
        Collection<String> labels = new ArrayList<>();
        metadata.getLabels().forEach((key, value) -> {
            labels.add(key);
            labels.add(value);
        });
        result.setLabels(labels);
        result.setK8sResourceSelfLink(String.format("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/%s/query", aaiRequest.getInstanceId()));
        return result;
    }
}