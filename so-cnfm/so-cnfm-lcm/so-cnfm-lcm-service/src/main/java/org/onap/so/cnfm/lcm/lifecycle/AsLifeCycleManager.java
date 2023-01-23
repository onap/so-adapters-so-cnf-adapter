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
package org.onap.so.cnfm.lcm.lifecycle;

import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.cnfm.lcm.SoCnfmAsLcmManagerUrlProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class AsLifeCycleManager {
    private static final Logger logger = getLogger(AsLifeCycleManager.class);

    private final JobExecutorService jobExecutorService;

    private final SoCnfmAsLcmManagerUrlProvider cnfmAsLcmManagerUrlProvider;

    @Autowired
    public AsLifeCycleManager(final JobExecutorService jobExecutorService,
            final SoCnfmAsLcmManagerUrlProvider cnfmAsLcmManagerUrlProvider) {
        this.jobExecutorService = jobExecutorService;
        this.cnfmAsLcmManagerUrlProvider = cnfmAsLcmManagerUrlProvider;
    }

    public ImmutablePair<URI, AsInstance> createAs(final CreateAsRequest createAsRequest) {
        logger.info("Will execute Create AS for CreateAsRequest: {}", createAsRequest);

        final AsInstance nsInstanceResponse = jobExecutorService.runCreateAsJob(createAsRequest);

        return ImmutablePair.of(
                cnfmAsLcmManagerUrlProvider.getCreatedAsResourceUri(nsInstanceResponse.getAsInstanceid()),
                nsInstanceResponse);
    }

    public URI instantiateAs(final String asInstanceId, final InstantiateAsRequest instantiateAsRequest) {
        logger.info("Will execute Instantiate AS for InstantiateAsRequest: {} and asInstanceId: {}",
                instantiateAsRequest, asInstanceId);

        final String asLcmOpOccId = jobExecutorService.runInstantiateAsJob(asInstanceId, instantiateAsRequest);
        return cnfmAsLcmManagerUrlProvider.getAsLcmOpOccUri(asLcmOpOccId);

    }

    public URI terminateAs(final String asInstanceId, final TerminateAsRequest terminateAsRequest) {
        logger.info("Will execute Terminate AS for TerminateAsRequest: {} and asInstanceId: {}", terminateAsRequest,
                asInstanceId);

        final String asLcmOpOccId = jobExecutorService.runTerminateAsJob(asInstanceId, terminateAsRequest);
        return cnfmAsLcmManagerUrlProvider.getAsLcmOpOccUri(asLcmOpOccId);
    }

    public void deleteAs(final String asInstanceId) {
        logger.info("Will execute Delete AS for asInstanceId: {}", asInstanceId);
        jobExecutorService.runDeleteAsJob(asInstanceId);
    }

}
