package org.onap.so.adapters.cnf.service.aai;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AaiIdGeneratorServiceTest {

    private AaiIdGeneratorService tested = new AaiIdGeneratorService();

    @Test
    public void shouldGenerateId() {
        // given
        String name = "name";
        String kind = "kind";
        String group = "group";
        String version = "version";
        String instanceId = "instanceId";
        String cloudOwner = "cloudOwner";
        String cloudRegion = "cloudRegion";
        String tenantId = "tenantId";
        K8sRbInstanceResourceStatus resourceStatus = mock(K8sRbInstanceResourceStatus.class);
        K8sStatus status = mock(K8sStatus.class);
        K8sStatusMetadata metadata = mock(K8sStatusMetadata.class);
        AaiRequest aaiRequest = mock(AaiRequest.class);
        K8sRbInstanceGvk gvk = mock(K8sRbInstanceGvk.class);

        // when
        when(resourceStatus.getGvk()).thenReturn(gvk);
        when(resourceStatus.getName()).thenReturn(name);
        when(resourceStatus.getStatus()).thenReturn(status);
        when(status.getK8sStatusMetadata()).thenReturn(metadata);
        when(gvk.getKind()).thenReturn(kind);
        when(gvk.getGroup()).thenReturn(group);
        when(gvk.getVersion()).thenReturn(version);
        when(aaiRequest.getInstanceId()).thenReturn(instanceId);
        when(aaiRequest.getCloudOwner()).thenReturn(cloudOwner);
        when(aaiRequest.getCloudRegion()).thenReturn(cloudRegion);
        when(aaiRequest.getTenantId()).thenReturn(tenantId);

        // then
        String actual = tested.generateId(resourceStatus, aaiRequest);
        String expected = "335d6ab87744a3140b36e70eee7537e97523b5e09f26d1d0ee887fd5839f87e2";

        Assert.assertEquals(expected, actual);
    }
}