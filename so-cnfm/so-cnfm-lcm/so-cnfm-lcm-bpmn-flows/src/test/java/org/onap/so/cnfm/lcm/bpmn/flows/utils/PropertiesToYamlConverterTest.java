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
package org.onap.so.cnfm.lcm.bpmn.flows.utils;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class PropertiesToYamlConverterTest {

    @Test
    public void testGetValuesYamlFileContent() {
        final String expected = "primary:\n" + "  service:\n" + "    nodePorts:\n" + "      mysql: '1234'\n"
                + "    ports:\n" + "      mysql: dummy\n";
        final PropertiesToYamlConverter objUnderTest = new PropertiesToYamlConverter();
        final Map<String, String> lifeCycleParams =
                Map.of("primary.service.ports.mysql", "dummy", "primary.service.nodePorts.mysql", "1234");
        final String actual = objUnderTest.getValuesYamlFileContent(lifeCycleParams);

        assertEquals(expected, actual);

    }
}
