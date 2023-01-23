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

import java.util.Iterator;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.configuration.rest.HttpComponentsClientConfiguration;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.onap.so.rest.service.HttpRestServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SdcHttpRestServiceProviderConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SdcHttpRestServiceProviderConfiguration.class);

    public static final String SDC_REST_TEMPLATE_CLIENT_BEAN = "SdcRestTemplateClientBean";
    public static final String SDC_HTTP_REST_SERVICE_PROVIDER_BEAN = "SdcHttpRestServiceProviderBean";

    @Autowired
    private GsonProvider gsonProvider;

    @Bean
    @Qualifier(SDC_REST_TEMPLATE_CLIENT_BEAN)
    public RestTemplate sdcAdapterRestTemplate(
            @Autowired final HttpComponentsClientConfiguration httpComponentsClientConfiguration) {

        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                httpComponentsClientConfiguration.httpComponentsClientHttpRequestFactory();

        final RestTemplate restTemplate =
                new RestTemplate(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;

    }

    @Bean
    @Qualifier(SDC_HTTP_REST_SERVICE_PROVIDER_BEAN)
    public HttpRestServiceProvider sdcHttpRestServiceProvider(
            @Qualifier(SDC_REST_TEMPLATE_CLIENT_BEAN) @Autowired final RestTemplate restTemplate) {

        try {
            logger.info("Setting SSLConnectionSocketFactory with Default SSL ...");
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault());
            final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            final HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        } catch (final Exception exception) {
            logger.error("Error reading truststore, TLS connection to SDC will fail.", exception);
        }
        setGsonMessageConverter(restTemplate);


        return new HttpRestServiceProviderImpl(restTemplate);
    }

    private void setGsonMessageConverter(final RestTemplate restTemplate) {
        final Iterator<HttpMessageConverter<?>> iterator = restTemplate.getMessageConverters().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter(gsonProvider.getGson()));
    }
}
