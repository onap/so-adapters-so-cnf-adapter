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
package org.onap.so.cnfm.lcm.database.beans.utils;

import java.util.List;
import java.util.Objects;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class Utils {

    private Utils() {}

    public static final String toIndentedString(final Object object) {
        return object == null ? "null" : object.toString().replace("\n", "\n    ");
    }


    public static boolean isEquals(final List<?> first, final List<?> second) {
        if (first == null) {
            return second == null;
        }

        if (first.isEmpty()) {
            return second.isEmpty();
        }
        if ((first != null && second != null) && (first.size() == second.size())) {
            for (int index = 0; index < first.size(); index++) {
                if (!Objects.equals(first.get(index), second.get(index))) {
                    return false;
                }
            }
            return true;

        }
        return false;
    }


}
