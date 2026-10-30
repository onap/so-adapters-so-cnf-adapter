/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG.
 * ================================================================================
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cnfm.lcm.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcHttpRestServiceProviderConfiguration.SDC_REST_TEMPLATE_CLIENT_BEAN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * {@code @AutoConfigureObservability} is required: {@code @SpringBootTest} otherwise disables tracing and registers a
 * no-op {@code Tracer}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureObservability
@Import(TracingTest.TestSpanProcessorConfiguration.class)
// Own H2 databases: Liquibase fails on the in-memory databases another test context has already migrated
@TestPropertySource(properties = {"management.tracing.enabled=true", "management.tracing.sampling.probability=1.0",
        "management.otlp.tracing.export.enabled=false",
        "spring.datasource.hikari.camunda.jdbcUrl=jdbc:h2:mem:tracing-camunda;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.hikari.cnfm.jdbcUrl=jdbc:h2:mem:tracing-cnfm;DB_CLOSE_DELAY=-1;"
                + "INIT=CREATE SCHEMA IF NOT EXISTS cnfm;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;"
                + "CASE_INSENSITIVE_IDENTIFIERS=TRUE"})
public class TracingTest {

    @Autowired
    private ObservationRegistry observationRegistry;

    @Autowired
    private Tracer tracer;

    @Autowired
    private TestSpanProcessor testSpanProcessor;

    @Autowired
    private Executor asyncExecutor;

    @Autowired
    @Qualifier(SDC_REST_TEMPLATE_CLIENT_BEAN)
    private RestTemplate sdcRestTemplate;

    @Test
    public void thatFinishedSpansAreReportedThroughThePipeline() {
        testSpanProcessor.clear();

        final Span span = tracer.nextSpan().name("cnfm-lcm-tracing-test-span").start();
        span.end();

        assertTrue("A sampled span must be reported to the registered SpanProcessor",
                testSpanProcessor.spanNames().contains("cnfm-lcm-tracing-test-span"));
    }

    @Test
    public void thatAsyncExecutorRunsTasksInTheCallersTrace() throws Exception {
        final CompletableFuture<String> traceIdInTask = new CompletableFuture<>();

        final String traceId =
                inObservation(() -> asyncExecutor.execute(() -> traceIdInTask.complete(currentTraceId())));

        assertEquals(traceId, traceIdInTask.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void thatSdcRestTemplateSendsTraceparent() {
        final MockRestServiceServer sdc = MockRestServiceServer.bindTo(sdcRestTemplate).build();
        final AtomicReference<String> traceparent = new AtomicReference<>();
        sdc.expect(requestTo("http://sdc/sdc/v1/catalog/resources"))
                .andExpect(request -> traceparent.set(request.getHeaders().getFirst("traceparent")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        final String traceId = inObservation(
                () -> sdcRestTemplate.getForObject("http://sdc/sdc/v1/catalog/resources", String.class));

        sdc.verify();
        assertTrue("traceparent was " + traceparent.get(),
                String.valueOf(traceparent.get()).matches("00-" + traceId + "-[0-9a-f]{16}-01"));
    }

    private String inObservation(final Runnable action) {
        final Observation observation = Observation.start("cnfm-lcm-propagation-test", observationRegistry);
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
    static class TestSpanProcessorConfiguration {
        @Bean
        TestSpanProcessor testSpanProcessor() {
            return new TestSpanProcessor();
        }
    }

    static class TestSpanProcessor implements SpanProcessor {
        private final List<String> spanNames = new CopyOnWriteArrayList<>();

        @Override
        public void onStart(final Context parentContext, final ReadWriteSpan span) {}

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(final ReadableSpan span) {
            spanNames.add(span.getName());
        }

        @Override
        public boolean isEndRequired() {
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
