/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
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

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

@Configuration
public class MulticloudConfiguration {

    @Value("${multicloud.endpoint}")
    private String endpoint;

    private final String username;

    private final String password;

    private String authorization;

    public MulticloudConfiguration(Environment environment) {
        Binder binder = new Binder(ConfigurationPropertySources.get(environment), PlaceholdersResolver.NONE);
        username = binder.bind("multicloud.auth.username", String.class).orElse("");
        password = binder.bind("multicloud.auth.password", String.class).orElse("");
    }

    @PostConstruct
    void validateAuthentication() {
        if (username.isEmpty() && password.isEmpty()) {
            return;
        }
        if (username.isBlank() || password.isBlank() || username.contains(":")) {
            throw new IllegalArgumentException("Invalid multicloud.auth credentials: username and password must be "
                    + "configured together, must not be blank, and username must not contain a colon");
        }
        authorization = "Basic " + HttpHeaders.encodeBasicAuth(username, password, StandardCharsets.UTF_8);
    }

    public void applyAuthentication(HttpHeaders headers) {
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    public String getMulticloudUrl() {
        return endpoint;
    }
}
