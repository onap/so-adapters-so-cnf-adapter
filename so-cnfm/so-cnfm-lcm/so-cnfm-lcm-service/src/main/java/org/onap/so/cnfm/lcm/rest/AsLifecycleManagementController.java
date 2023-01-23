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
package org.onap.so.cnfm.lcm.rest;

import static org.onap.so.cnfm.lcm.Constants.AS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.cnfm.lcm.lifecycle.AsLifeCycleManager;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Controller
@RequestMapping(value = AS_LIFE_CYCLE_MANAGEMENT_BASE_URL)
public class AsLifecycleManagementController {
    private static final Logger logger = getLogger(AsLifecycleManagementController.class);

    private final AsLifeCycleManager asLifeCycleManager;

    @Autowired
    public AsLifecycleManagementController(final AsLifeCycleManager asLifeCycleManager) {
        this.asLifeCycleManager = asLifeCycleManager;
    }

    @PostMapping(value = "/as_instances", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<AsInstance> createAs(@RequestBody final CreateAsRequest createAsRequest) {
        logger.info("Received Create AS Request: {}n", createAsRequest);

        final ImmutablePair<URI, AsInstance> nsInstance = asLifeCycleManager.createAs(createAsRequest);

        final URI resourceUri = nsInstance.getLeft();
        final AsInstance createdAsresponse = nsInstance.getRight();

        logger.info("AS resource created successfully. Resource location: {}, response: {}", resourceUri,
                createdAsresponse);

        return ResponseEntity.created(resourceUri).body(createdAsresponse);
    }

    @PostMapping(value = "/as_instances/{asInstanceId}/instantiate",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Void> instantiateAs(@PathVariable("asInstanceId") final String asInstanceId,
            @RequestBody final InstantiateAsRequest instantiateAsRequest) {
        logger.debug("Received instantiate AS request: {}\n with asInstanceId: {}", instantiateAsRequest, asInstanceId);
        final URI resourceUri = asLifeCycleManager.instantiateAs(asInstanceId, instantiateAsRequest);
        logger.info("{} AS Instantiation started successfully. Resource Operation Occurrence uri: {}", asInstanceId,
                resourceUri);
        return ResponseEntity.accepted().location(resourceUri).build();
    }

    @PostMapping(value = "/as_instances/{asInstanceId}/terminate",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Void> terminateAs(@PathVariable("asInstanceId") final String asInstanceId,
            @RequestBody(required = false) final TerminateAsRequest terminateAsRequest) {
        logger.debug("Received terminate AS request: {}\n with asInstanceId: {}", terminateAsRequest, asInstanceId);
        final URI resourceUri = asLifeCycleManager.terminateAs(asInstanceId, terminateAsRequest);
        logger.info("{} As Terminate started successfully. Resource Operation Occurrence uri: {}", asInstanceId,
                resourceUri);
        return ResponseEntity.accepted().location(resourceUri).build();
    }

    @DeleteMapping(value = "/as_instances/{asInstanceId}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Void> deleteAs(@PathVariable("asInstanceId") final String asInstanceId) {
        logger.debug("Received delete AS request for asInstanceId: {}", asInstanceId);
        asLifeCycleManager.deleteAs(asInstanceId);
        logger.info("Successfully deleted AS for asInstanceId: {}", asInstanceId);
        return ResponseEntity.noContent().build();
    }

}
