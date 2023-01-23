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
package org.onap.so.cnfm.lcm;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SoCnfmAsLcmManagerUrlProvider {

    private final String soCnfmLcmManagerEndpoint;

    @Autowired
    public SoCnfmAsLcmManagerUrlProvider(
            @Value("${so-cnfm-lcm.endpoint:http://so-cnfm-lcm.onap:9888}") final String soCnfmLcmManagerEndpoint) {
        this.soCnfmLcmManagerEndpoint = soCnfmLcmManagerEndpoint;
    }

    public URI getCreatedAsResourceUri(final String asInstanceId) {
        return URI.create(soCnfmLcmManagerEndpoint + Constants.AS_LIFE_CYCLE_MANAGEMENT_BASE_URL + "/as_instances/"
                + asInstanceId);
    }

    public URI getAsLcmOpOccUri(final String asLcmOpOccId) {
        return URI.create(soCnfmLcmManagerEndpoint + Constants.AS_LIFE_CYCLE_MANAGEMENT_BASE_URL + "/as_lcm_op_occs/"
                + asLcmOpOccId);
    }

}
