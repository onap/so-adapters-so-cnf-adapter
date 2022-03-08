package org.onap.so.adapters.cnf.service.healthcheck;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HealthCheckServiceTest {
    private final int portNumber = 8443;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(portNumber);

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Mock
    private MulticloudClient multicloudClient;

    @Test
    public void healthCheckTest() {
        try {
            doReturn(getK8sRbInstanceHealthCheckSimple()).when(multicloudClient).startInstanceHealthCheck(any());
            doReturn(getK8sRbInstanceHealthCheck()).when(multicloudClient).getInstanceHealthCheck(any(), any());
            HealthCheckResponse response = healthCheckService.healthCheck(getTestCheckInstanceRequest());

            verify(multicloudClient, times(3)).startInstanceHealthCheck(any());
            verify(multicloudClient, times(3)).getInstanceHealthCheck(any(), any());
            assertThat(response.getInstanceResponse()).hasSize(getTestCheckInstanceRequest().getInstances().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckInstanceRequest getTestCheckInstanceRequest() {
        CheckInstanceRequest checkInstanceRequest = new CheckInstanceRequest();
        checkInstanceRequest.setInstances(asList(getRandomInstance(), getRandomInstance(), getRandomInstance()));
        return checkInstanceRequest;
    }

    private InstanceRequest getRandomInstance() {
        InstanceRequest instanceRequest = new InstanceRequest();
        instanceRequest.setInstanceId(UUID.randomUUID().toString());
        return instanceRequest;
    }

    private K8sRbInstanceHealthCheckSimple getK8sRbInstanceHealthCheckSimple() {
        K8sRbInstanceHealthCheckSimple k8sRbInstanceHealthCheckSimple = new K8sRbInstanceHealthCheckSimple();
        k8sRbInstanceHealthCheckSimple.setId(UUID.randomUUID().toString());
        k8sRbInstanceHealthCheckSimple.setStatus("RUNNING");
        return k8sRbInstanceHealthCheckSimple;
    }

    private K8sRbInstanceHealthCheck getK8sRbInstanceHealthCheck() {
        K8sRbInstanceHealthCheck k8sRbInstanceHealthCheck = new K8sRbInstanceHealthCheck();
        k8sRbInstanceHealthCheck.setStatus("STOPPED");
        return k8sRbInstanceHealthCheck;
    }
}
