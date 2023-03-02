/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.binary.Base64;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.BasicAuthConfigException;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SdcClientConfigurationProvider {

    private static final String SERVICE_NAME = "SO-CNFM";

    @Value("${sdc.username:#{null}}")
    private String sdcUsername;

    @Value("${sdc.password:#{null}}")
    private String sdcPassword;

    @Value("${sdc.key:#{null}}")
    private String sdcKey;

    @Value("${sdc.endpoint:https://sdc-be.onap:8443}")
    private String baseUrl;

    private static String basicAuth = null;

    private synchronized String getBasicAuth() {
        if (basicAuth == null) {
            try {
                final String auth = sdcUsername + ":" + CryptoUtils.decrypt(sdcPassword, sdcKey);
                final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                basicAuth = "Basic " + new String(encodedAuth);
            } catch (final GeneralSecurityException exception) {
                throw new BasicAuthConfigException("Unable to process basic auth information", exception);
            }
        }
        return basicAuth;
    }

    public HttpHeaders getSdcDefaultHttpHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        if (isNotBlank(sdcUsername) && isNotBlank(sdcPassword) && isNotBlank(sdcKey)) {
            headers.add(HttpHeaders.AUTHORIZATION, getBasicAuth());
        }
        headers.add("X-ECOMP-InstanceID", SERVICE_NAME);
        headers.add("X-FromAppId", SERVICE_NAME);
        return headers;
    }

    public String getSdcPackageUrl(final String packageId) {
        return baseUrl + "/sdc/v1/catalog/resources/" + packageId + "/toscaModel";

    }

}
