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

    /**
     * Execute <a href="https://helm.sh/docs/helm/helm_install/">helm install</a> with dry run flag
     *
     * @param namespace Namespace scope for this request
     * @param releaseName Name of the release given to helm install
     * @param kubeconfig kubernetes configuration file path
     * @param helmChart path of the helm chart to install
     *
     * @throws HelmClientExecuteException when exception occurs while executing command
     */
    void runHelmChartInstallWithDryRunFlag(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart) throws HelmClientExecuteException;

    /**
     * Retrieve kube kinds using <a href="https://helm.sh/docs/helm/helm_template/">helm template</a> with dry run and
     * skip-tests flag
     * 
     * 
     * @param namespace Namespace scope for this request
     * @param releaseName Name of the release given to helm install
     * @param kubeconfig kubernetes configuration file path
     * @param helmChart path of the helm chart to install
     *
     * @return Resources for helmChart as a List of strings
     */
    List<String> getKubeKinds(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart) throws HelmClientExecuteException;

    /**
     * Retrieve kube kinds using <a href="https://helm.sh/docs/helm/helm_get_manifest/">helm get manifest</a>
     * 
     * @param namespace Namespace scope for this request
     * @param releaseName Name of the release given to helm install
     * @param kubeconfig kubernetes configuration file path
     * @return
     * @throws HelmClientExecuteException
     */
    List<String> getKubeKindsUsingManifestCommand(final String namespace, final String releaseName,
            final Path kubeconfig) throws HelmClientExecuteException;

    /**
     * @param namespace Namespace scope for this request
     * @param releaseName Name of the release given to helm install
     * @param kubeconfig kubernetes configuration file path
     * @param helmChart path of the helm chart to install
     * @param lifeCycleParams override values in a chart
     * @throws HelmClientExecuteException
     */
    void installHelmChart(final String namespace, final String releaseName, final Path kubeconfig, final Path helmChart,
            final Map<String, String> lifeCycleParams) throws HelmClientExecuteException;

    /**
     * 
     * @param namespace Namespace scope for this request
     * @param releaseName Name of the release given to helm install
     * @param kubeConfigFilePath kubernetes configuration file path
     * @throws HelmClientExecuteException
     */
    void unInstallHelmChart(final String namespace, final String releaseName, final Path kubeConfigFilePath)
            throws HelmClientExecuteException;
}
