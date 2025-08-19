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

package org.onap.so.cnfm.lcm.bpmn.flows.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.HelmClientExecuteException;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.helm.HelmClient;
import org.springframework.stereotype.Service;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class MockedHelmClient implements HelmClient {

    private final Map<String, Integer> counter = new ConcurrentHashMap<>();
    private final Map<String, Integer> unInstallCounter = new ConcurrentHashMap<>();
    private final List<String> kubeKinds;

    public MockedHelmClient(final List<String> kubeKinds) {
        this.kubeKinds = kubeKinds;
    }

    @Override
    public void runHelmChartInstallWithDryRunFlag(final String namespace, final String releaseName,
            final Path kubeconfig, final Path helmChart) {
        Integer count = counter.get(releaseName);
        if (count == null) {
            count = 0;
        }
        counter.put(releaseName, ++count);

    }

    @Override
    public List<String> getKubeKinds(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart) {
        Integer count = counter.get(releaseName);
        if (count == null) {
            count = 0;
        }
        counter.put(releaseName, ++count);
        return kubeKinds;
    }

    @Override
    public List<String> getKubeKindsUsingManifestCommand(final String namespace, final String releaseName,
            final Path kubeconfig) {
        Integer count = unInstallCounter.get(releaseName);
        if (count == null) {
            count = 0;
        }
        unInstallCounter.put(releaseName, ++count);
        return kubeKinds;
    }

    @Override
    public void installHelmChart(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart, final Map<String, String> lifeCycleParams) {
        Integer count = counter.get(releaseName);
        if (count == null) {
            count = 0;
        }
        counter.put(releaseName, ++count);
    }

    @Override
    public void unInstallHelmChart(final String namespace, final String releaseName, final Path kubeConfigFilePath)
            throws HelmClientExecuteException {
        Integer count = unInstallCounter.get(releaseName);
        if (count == null) {
            count = 0;
        }
        unInstallCounter.put(releaseName, ++count);
    }

    public Map<String, Integer> getCounter() {
        return counter;
    }

    public Map<String, Integer> getUnInstallCounter() {
        return unInstallCounter;
    }

    public void clear() {
        counter.clear();
        unInstallCounter.clear();
    }

}
