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

import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcHttpRestServiceProviderConfiguration.SDC_HTTP_REST_SERVICE_PROVIDER_BEAN;
import java.util.Optional;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.SdcPackageRequestFailureException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class SdcPackageProviderImpl implements SdcPackageProvider {
    private static final Logger logger = LoggerFactory.getLogger(SdcPackageProviderImpl.class);
    private final SdcClientConfigurationProvider sdcClientConfigurationProvider;
    private final HttpRestServiceProvider httpServiceProvider;

    @Autowired
    public SdcPackageProviderImpl(final SdcClientConfigurationProvider sdcClientConfigurationProvider,
            @Qualifier(SDC_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider) {
        this.sdcClientConfigurationProvider = sdcClientConfigurationProvider;
        this.httpServiceProvider = httpServiceProvider;
    }

    @Override
    public Optional<byte[]> getSdcResourcePackage(final String packageId) {
        try {
            final HttpHeaders headers = sdcClientConfigurationProvider.getSdcDefaultHttpHeaders();
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            logger.info("Will retrieve resource package with id: {} from SDC", packageId);
            final String url = sdcClientConfigurationProvider.getSdcPackageUrl(packageId);

            final ResponseEntity<byte[]> response = httpServiceProvider.getHttpResponse(url, headers, byte[].class);
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response.hasBody()) {
                    return Optional.of(response.getBody());
                }
                logger.error("Received response without body ...");
            }
            return Optional.empty();
        } catch (final Exception restProcessingException) {
            final String message = "Caught exception while getting resource package content for: " + packageId;
            throw new SdcPackageRequestFailureException(message, restProcessingException);
        }
    }

}
