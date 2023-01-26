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
package org.onap.so.cnfm.lcm.rest.exceptions;

import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.onap.so.cnfm.lcm.rest.AsLcmOperationOccurrencesController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@ControllerAdvice(assignableTypes = AsLcmOperationOccurrencesController.class)
public class AsLcmOpOccControllerExceptionHandler {

    @ExceptionHandler(AsLcmOpOccStatusNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleAsLcmOpOccStatusNotFoundException(
            final AsLcmOpOccStatusNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDetails().status(HttpStatus.NOT_FOUND.value()).detail(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAsLcmOpOccException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorDetails().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(exception.getMessage()));
    }

}
