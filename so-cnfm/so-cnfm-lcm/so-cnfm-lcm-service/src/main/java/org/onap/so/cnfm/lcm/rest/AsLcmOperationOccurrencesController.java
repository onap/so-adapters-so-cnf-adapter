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
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.onap.so.cnfm.lcm.lifecycle.AsLcmOperationOccurrenceManager;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Controller
@RequestMapping(value = AS_LIFE_CYCLE_MANAGEMENT_BASE_URL)
public class AsLcmOperationOccurrencesController {
    private static final Logger logger = getLogger(AsLcmOperationOccurrencesController.class);
    private final AsLcmOperationOccurrenceManager asLcmOperationOccurrenceManager;

    @Autowired
    public AsLcmOperationOccurrencesController(final AsLcmOperationOccurrenceManager asLcmOperationOccurrenceManager) {
        this.asLcmOperationOccurrenceManager = asLcmOperationOccurrenceManager;
    }

    @GetMapping(value = "/as_lcm_op_occs/{asLcmOpOccId}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<?> getOperationStatus(@PathVariable("asLcmOpOccId") final String asLcmOpOccId) {
        logger.info("Received request to retrieve operation status for asLcmOpOccId: {}", asLcmOpOccId);
        final Optional<AsLcmOpOcc> optionalAsLcmOpOccs =
                asLcmOperationOccurrenceManager.getAsLcmOperationOccurrence(asLcmOpOccId);

        if (optionalAsLcmOpOccs.isPresent()) {
            final AsLcmOpOcc asLcmOpOcc = optionalAsLcmOpOccs.get();
            logger.info("Sending back AsLcmOpOcc: {}", asLcmOpOcc);
            return ResponseEntity.ok().body(asLcmOpOcc);
        }

        final String errorMessage = "Unable to retrieve operation occurrence status for asLcmOpOccId: " + asLcmOpOccId;
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDetails().detail(errorMessage));
    }
}
