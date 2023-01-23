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
package org.onap.so.cnfm.lcm.bpmn.flows;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.onap.so.cnfm.lcm.bpmn.flows.utils.LocalDateTimeTypeAdapter;
import org.onap.so.cnfm.lcm.bpmn.flows.utils.OffsetDateTimeTypeAdapter;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class GsonProvider {

    private final OffsetDateTimeTypeAdapter offsetDateTimeTypeAdapter = new OffsetDateTimeTypeAdapter();

    public Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, offsetDateTimeTypeAdapter)
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()).create();
    }


}
