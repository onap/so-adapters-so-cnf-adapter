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
package org.onap.so.cnfm.lcm.bpmn.flows.service;

import java.nio.file.Path;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubeConfigFileNotFoundException;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface KubConfigProvider {

    Path getKubeConfigFile(final String cloudOwner, final String cloudRegion, final String tenantId)
            throws KubeConfigFileNotFoundException;

    void addKubeConfigFile(final MultipartFile file, final String cloudOwner, final String cloudRegion,
            final String tenantId);

}
