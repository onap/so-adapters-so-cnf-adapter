/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.cnf;

import org.onap.so.client.ClientBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * Propagates the current trace context onto the outgoing (aai-client) JAX-RS requests.
 *
 * <p>
 * This replaces the Spring Cloud Sleuth based {@code brave.jaxrs2.TracingClientFilter} that was used before the Spring
 * Boot 3 migration. Micrometer Tracing (the Brave bridge) is now the tracing abstraction, and the JAX-RS client stack
 * moved from {@code javax.ws.rs} to {@code jakarta.ws.rs} (CXF 4). Brave's {@code brave-instrumentation-jaxrs2} filter
 * only targets the {@code javax.ws.rs} API, so it can no longer be registered on the jakarta {@link ClientBuilder} used
 * by the aai-client. Instead, a small {@link ClientRequestFilter} injects the trace context of the current span into
 * the outbound request headers using the Micrometer {@link Propagator} (W3C/B3, as configured under
 * {@code management.tracing.propagation}).
 * </p>
 *
 * The configuration is only active when a {@link Tracer} bean is present, i.e. when tracing is enabled - mirroring the
 * previous {@code @ConditionalOnBean(Tracing.class)} guard.
 */
@Configuration
@ConditionalOnBean(Tracer.class)
public class TracingConfiguration {

    @Bean
    ClientBuilderCustomizer tracingClientBuilderCustomizer(final Tracer tracer, final Propagator propagator) {
        return builder -> builder.register(new TracePropagatingClientRequestFilter(tracer, propagator));
    }

    /**
     * Injects the current span's trace context into the outgoing request headers.
     */
    static class TracePropagatingClientRequestFilter implements ClientRequestFilter {

        private final Tracer tracer;
        private final Propagator propagator;

        TracePropagatingClientRequestFilter(final Tracer tracer, final Propagator propagator) {
            this.tracer = tracer;
            this.propagator = propagator;
        }

        @Override
        public void filter(final ClientRequestContext requestContext) {
            final Span currentSpan = tracer.currentSpan();
            if (currentSpan == null) {
                return;
            }
            propagator.inject(currentSpan.context(), requestContext.getHeaders(),
                    (carrier, key, value) -> carrier.putSingle(key, value));
        }
    }
}
