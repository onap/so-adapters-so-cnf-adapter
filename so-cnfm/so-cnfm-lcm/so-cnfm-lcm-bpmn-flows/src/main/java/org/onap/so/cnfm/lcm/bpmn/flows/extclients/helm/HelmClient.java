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

package org.onap.so.cnfm.lcm.bpmn.flows.extclients.helm;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.HelmClientExecuteException;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface HelmClient {

    void runHelmChartInstallWithDryRunFlag(final String releaseName, final Path kubeconfig, final Path helmChart)
            throws HelmClientExecuteException;

    List<String> getKubeKinds(final String releaseName, final Path kubeconfig, final Path helmChart)
            throws HelmClientExecuteException;

    List<String> getKubeKindsUsingManifestCommand(final String releaseName, final Path kubeconfig)
            throws HelmClientExecuteException;

    void installHelmChart(final String releaseName, final Path kubeconfig, final Path helmChart,
            final Map<String, String> lifeCycleParams) throws HelmClientExecuteException;

    void unInstallHelmChart(final String releaseName, final Path kubeConfigFilePath) throws HelmClientExecuteException;
}
