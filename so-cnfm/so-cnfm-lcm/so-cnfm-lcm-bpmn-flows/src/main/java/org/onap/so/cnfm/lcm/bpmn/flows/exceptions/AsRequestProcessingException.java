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
package org.onap.so.cnfm.lcm.bpmn.flows.exceptions;

import org.onap.so.cnfm.lcm.model.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class AsRequestProcessingException extends RuntimeException {

    private static final long serialVersionUID = 66862444537194516L;
    private final ErrorDetails errorDetails;

    public AsRequestProcessingException(final String message) {
        super(message);
        errorDetails = null;
    }

    public AsRequestProcessingException(final String message, final ErrorDetails errorContents) {
        super(message);
        this.errorDetails = errorContents;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

}
