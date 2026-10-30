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
package org.onap.so.adapters.cnf.rest;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;

/**
 * Sends real requests to the running adapter, with the actuator base path OOM configures, and checks which of them are
 * traced.
 */
@RunWith(SpringRunner.class)
@AutoConfigureObservability
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(UntracedPathsTest.TestConfig.class)
@TestPropertySource(properties = {"management.tracing.enabled=true", "management.tracing.sampling.probability=1.0",
        "management.endpoints.web.base-path=/manage"})
public class UntracedPathsTest {

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    private int port;

    @Autowired
    private RecordingSpanHandler spans;

    @Before
    public void clearSpans() {
        spans.clear();
    }

    @Test
    public void healthProbeIsNotTracedWhileOtherRequestsAre() throws InterruptedException {
        restTemplate.getForEntity("http://localhost:" + port + "/manage/health", String.class);
        restTemplate.getForEntity("http://localhost:" + port + "/api/cnf-adapter/v1/untraced-paths-test",
                String.class);

        awaitTrue("other requests must still be traced: " + spans.urls(), () -> spans.urls().stream()
                .anyMatch(url -> url.startsWith("/api/cnf-adapter/v1/untraced-paths-test")));
        assertTrue("health requests were traced: " + spans.urls(),
                spans.urls().stream().noneMatch(url -> url.startsWith("/manage/health")));
    }

    private static void awaitTrue(String message, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError(message);
            }
            Thread.sleep(50);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RecordingSpanHandler recordingSpanHandler() {
            return new RecordingSpanHandler();
        }
    }

    static class RecordingSpanHandler extends SpanHandler {
        private final List<String> urls = new CopyOnWriteArrayList<>();

        @Override
        public boolean end(final TraceContext context, final MutableSpan span, final Cause cause) {
            final String url = span.tag("http.url");
            if (url != null) {
                urls.add(url);
            }
            return true;
        }

        List<String> urls() {
            return urls;
        }

        void clear() {
            urls.clear();
        }
    }
}
