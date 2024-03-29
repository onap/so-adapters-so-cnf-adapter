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

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class Constants {

    public static final String SERVICE_NAME = "so-cnfm";
    public static final String SERVICE_VERSION = "v1";
    public static final String BASE_URL = "/so/" + SERVICE_NAME + "/" + SERVICE_VERSION + "/api";
    public static final String AS_LIFE_CYCLE_MANAGEMENT_BASE_URL = BASE_URL + "/aslcm/v1";

    private Constants() {}

}
