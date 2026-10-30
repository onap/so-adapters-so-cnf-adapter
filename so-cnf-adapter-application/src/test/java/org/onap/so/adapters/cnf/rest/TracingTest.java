/*
 * Copyright © 2025 Deutsche Telekom
 *
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
 */
package org.onap.so.adapters.cnf.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.client.MulticloudHttpClient;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.InstanceRequest;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheck;
import org.onap.so.adapters.cnf.model.healthcheck.K8sRbInstanceHealthCheckSimple;
import org.onap.so.adapters.cnf.service.aai.KubernetesResource;
import org.onap.so.adapters.cnf.service.healthcheck.HealthCheckService;
import org.onap.so.adapters.cnf.util.IAaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * Verifies that the tracing pipeline introduced with the Spring Boot 3 migration (Micrometer Tracing + the Brave
 * bridge, which replaced Spring Cloud Sleuth) is actually wired and reports finished spans.
 *
 * {@code @AutoConfigureObservability} is required: {@code @SpringBootTest} otherwise installs Spring Boot's
 * observability test customizer, which disables tracing and registers a no-op {@code Tracer}. With it enabled, a test
 * {@link SpanHandler} bean is collected by Boot's {@code BraveAutoConfiguration} and wired into the live Brave
 * {@code Tracing}, so the assertion observes a real span flowing through the pipeline. This restores the behavioural
 * verification of the original Sleuth-based test without its {@code Thread.sleep}-then-poll-WireMock flakiness: the
 * handler captures the span synchronously on {@code end()}.
 */
@RunWith(SpringRunner.class)
@AutoConfigureObservability
@SpringBootTest
@Import(TracingTest.TestSpanHandlerConfiguration.class)
@TestPropertySource(properties = {"management.tracing.enabled=true", "management.tracing.sampling.probability=1.0"})
public class TracingTest {

    @ClassRule
    public static WireMockRule multicloud = new WireMockRule(wireMockConfig().dynamicPort());

    @ClassRule
    public static WireMockRule aai = new WireMockRule(wireMockConfig().dynamicPort());

    @DynamicPropertySource
    static void endpoints(DynamicPropertyRegistry registry) {
        registry.add("multicloud.endpoint", multicloud::baseUrl);
        registry.add("aai.endpoint", aai::baseUrl);
    }

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    @Qualifier("cnfAsyncExecutor")
    Executor cnfAsyncExecutor;

    @Autowired
    MulticloudHttpClient multicloudHttpClient;

    @Autowired
    HealthCheckService healthCheckService;

    @MockitoBean
    MulticloudClient multicloudClient;

    @Autowired
    Tracer tracer;

    @Autowired
    TestSpanHandler testSpanHandler;

    @Test
    public void thatTracingBeansAreAvailable() {
        assertNotNull("ObservationRegistry bean should be present", observationRegistry);
        assertNotNull("Tracer bean should be present", tracer);
    }

    @Test
    public void thatFinishedSpansAreReportedThroughThePipeline() {
        testSpanHandler.clear();

        final Span span = tracer.nextSpan().name("cnf-adapter-tracing-test-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            // the span only needs to be opened and closed for it to be reported
        } finally {
            span.end();
        }

        assertTrue("A sampled span must be reported to the registered SpanHandler",
                testSpanHandler.spanNames().contains("cnf-adapter-tracing-test-span"));
    }

    @Test
    public void thatCnfAsyncExecutorRunsTasksInTheCallersTrace() throws Exception {
        final CompletableFuture<String> traceIdInTask = new CompletableFuture<>();

        final String traceId = inObservation(() -> cnfAsyncExecutor
                .execute(() -> traceIdInTask.complete(currentTraceId())));

        assertEquals(traceId, traceIdInTask.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void thatHealthCheckPollsMulticloudInTheCallersTrace() throws Exception {
        final CompletableFuture<String> traceIdInPoll = new CompletableFuture<>();
        final K8sRbInstanceHealthCheckSimple started = new K8sRbInstanceHealthCheckSimple();
        started.setId("hc-1");
        when(multicloudClient.startInstanceHealthCheck(anyString())).thenReturn(started);
        when(multicloudClient.getInstanceHealthCheck(anyString(), any())).thenAnswer(invocation -> {
            traceIdInPoll.complete(currentTraceId());
            final K8sRbInstanceHealthCheck status = new K8sRbInstanceHealthCheck();
            status.setStatus("SUCCEEDED");
            return status;
        });
        final InstanceRequest instance = new InstanceRequest();
        instance.setInstanceId("instance-1");
        final CheckInstanceRequest request = new CheckInstanceRequest();
        request.setInstances(List.of(instance));

        final String traceId = inObservation(() -> {
            try {
                healthCheckService.healthCheck(request);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals(traceId, traceIdInPoll.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void thatMulticloudHttpClientSendsTraceparent() {
        multicloud.stubFor(get(urlEqualTo("/v1/rb/definition")).willReturn(aResponse().withStatus(200).withBody("[]")));

        final String traceId = inObservation(() -> multicloudHttpClient.get("/v1/rb/definition"));

        multicloud.verify(getRequestedFor(urlEqualTo("/v1/rb/definition")).withHeader("traceparent",
                matching("00-" + traceId + "-[0-9a-f]{16}-01")));
    }

    @Test
    public void thatAaiRequestsAreTracedInTheCallersTrace() {
        final String k8sResourcePath = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someOwner/someRegion"
                + "/tenants/tenant/someTenant/k8s-resources/k8s-resource/someId";
        aai.stubFor(get(urlPathEqualTo(k8sResourcePath)).willReturn(aResponse().withStatus(404)));
        final KubernetesResource resource = new KubernetesResource();
        resource.setId("someId");
        resource.setName("someName");
        final AaiRequest request = new AaiRequest();
        request.setCloudOwner("someOwner");
        request.setCloudRegion("someRegion");
        request.setTenantId("someTenant");
        testSpanHandler.clear();

        final String traceId = inObservation(() -> IAaiRepository.instance(true).update(resource, request));

        aai.verify(getRequestedFor(urlPathEqualTo(k8sResourcePath)).withHeader("traceparent",
                matching("00-" + traceId + "-[0-9a-f]{16}-01")));
        assertTrue("The AAI request must be reported as a client span",
                testSpanHandler.spanNames().contains("http get"));
    }

    private String inObservation(final Runnable action) {
        final Observation observation = Observation.start("cnf-adapter-propagation-test", observationRegistry);
        try (Observation.Scope ignored = observation.openScope()) {
            action.run();
            return currentTraceId();
        } finally {
            observation.stop();
        }
    }

    private String currentTraceId() {
        final Span span = tracer.currentSpan();
        return span == null ? null : span.context().traceId();
    }

    @TestConfiguration
    static class TestSpanHandlerConfiguration {
        @Bean
        TestSpanHandler testSpanHandler() {
            return new TestSpanHandler();
        }
    }

    /**
     * Captures the names of spans reported to Brave on {@code end()}. Brave only invokes {@code end()} for sampled
     * spans, which is why the test forces a sampling probability of 1.0.
     */
    static class TestSpanHandler extends SpanHandler {
        private final List<String> spanNames = new CopyOnWriteArrayList<>();

        @Override
        public boolean end(final TraceContext context, final MutableSpan span, final Cause cause) {
            spanNames.add(span.name());
            return true;
        }

        List<String> spanNames() {
            return spanNames;
        }

        void clear() {
            spanNames.clear();
        }
    }

}
