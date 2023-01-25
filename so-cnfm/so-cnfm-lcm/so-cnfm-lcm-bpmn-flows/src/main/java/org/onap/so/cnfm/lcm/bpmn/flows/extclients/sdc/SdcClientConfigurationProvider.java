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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.binary.Base64;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.BasicAuthConfigException;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SdcClientConfigurationProvider {

    @Value("${sdc.username:mso}")
    private String sdcUsername;

    @Value("${sdc.password:76966BDD3C7414A03F7037264FF2E6C8EEC6C28F2B67F2840A1ED857C0260FEE731D73F47F828E5527125D29FD25D3E0DE39EE44C058906BF1657DE77BF897EECA93BDC07FA64F}")
    private String sdcPassword;

    @Value("${sdc.key:566B754875657232314F5548556D3665}")
    private String sdcKey;

    @Value("${sdc.endpoint:https://sdc-be.onap:8443}")
    private String baseUrl;

    private static String basicAuth = null;

    public synchronized String getBasicAuth() {
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

    public String getSdcPackageUrl(final String packageId) {
        return baseUrl + "/sdc/v1/catalog/resources/" + packageId + "/toscaModel";

    }

}
