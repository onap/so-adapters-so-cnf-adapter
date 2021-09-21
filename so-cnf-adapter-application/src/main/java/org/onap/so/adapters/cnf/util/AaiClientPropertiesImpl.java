/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.cnf.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.client.CacheProperties;
import org.onap.so.spring.SpringContextHelper;
import org.onap.so.utils.CryptoUtils;
import org.springframework.context.ApplicationContext;

public class AaiClientPropertiesImpl implements AAIProperties {

    private final static String aaiEndpoint;
    private final static String auth;
    private final static String key;
    private final static Long readTimeout;
    private final static Long connectionTimeout;
    private final static boolean enableCaching;
    private final static Long cacheMaxAge;
    private static final String SYSTEM_NAME = "MSO";

    static {
        ApplicationContext context = SpringContextHelper.getAppContext();
        aaiEndpoint = context.getEnvironment().getProperty("aai.endpoint");
        readTimeout = context.getEnvironment().getProperty("aai.readTimeout", Long.class, 60000L);
        connectionTimeout = context.getEnvironment().getProperty("aai.connectionTimeout", Long.class, 60000L);
        enableCaching = context.getEnvironment().getProperty("aai.caching.enabled", Boolean.class, false);
        cacheMaxAge = context.getEnvironment().getProperty("aai.caching.maxAge", Long.class, 60000L);
        key = "07a7159d3bf51a0e53be7a8f89699be7";
        String authTmp = context.getEnvironment().getProperty("aai.auth");
        if (authTmp != null && !authTmp.isEmpty() && authTmp.split(" ").length == 2) {
            authTmp = authTmp.split(" ")[1].trim();
            authTmp = new String(Base64.decodeBase64(authTmp));
            try {
                authTmp = CryptoUtils.encrypt(authTmp, key);
            } catch (GeneralSecurityException e) {
                authTmp = "";
                e.printStackTrace();
            }
        }
        auth = authTmp;
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(aaiEndpoint != null ? aaiEndpoint : "");
    }

    @Override
    public String getSystemName() {
        return SYSTEM_NAME;
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.LATEST;
    }

    @Override
    public String getAuth() { return auth; }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Long getReadTimeout() {
        return readTimeout;
    }

    @Override
    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public boolean isCachingEnabled() {
        return enableCaching;
    }

    @Override
    public CacheProperties getCacheProperties() {
        return new AAICacheProperties() {
            @Override
            public Long getMaxAge() {
                return cacheMaxAge;
            }
        };
    }
}
