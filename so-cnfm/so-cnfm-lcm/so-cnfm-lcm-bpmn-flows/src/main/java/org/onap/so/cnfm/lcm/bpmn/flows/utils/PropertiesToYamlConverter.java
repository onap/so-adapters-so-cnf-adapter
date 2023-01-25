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

import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */

@Service
public class PropertiesToYamlConverter {
    public String getValuesYamlFileContent(final Map<String, String> lifeCycleParams) {
        final Map<String, Object> root = new TreeMap<>();
        lifeCycleParams.entrySet().stream().forEach(entry -> processProperty(root, entry.getKey(), entry.getValue()));
        final Yaml yaml = new Yaml();
        return yaml.dumpAsMap(root);
    }

    @SuppressWarnings("unchecked")
    private void processProperty(final Map<String, Object> root, final String key, final String value) {
        Map<String, Object> local = root;
        final String[] keys = key.split("\\.");
        final int lastIndex = keys.length - 1;
        for (int index = 0; index < lastIndex; index++) {
            final String currentKey = keys[index];
            if (!local.containsKey(currentKey)) {
                final Map<String, Object> subMap = new TreeMap<>();
                local.put(currentKey, subMap);
                local = subMap;
            } else {
                local = (Map<String, Object>) local.get(currentKey);
            }
        }
        local.put(keys[lastIndex], value);
    }
}
