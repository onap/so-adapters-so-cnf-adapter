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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.onap.so.cnfm.lcm.bpmn.flows.Constants;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.HelmClientExecuteException;
import org.onap.so.cnfm.lcm.bpmn.flows.utils.PropertiesToYamlConverter;
import org.onap.so.cnfm.lcm.database.beans.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class HelmClientTest {
    private static final Logger logger = LoggerFactory.getLogger(HelmClientTest.class);

    private static final int SUCCESSFUL_EXIT_CODE = 0;
    private static final int FAILED_EXIT_CODE = 1;

    private static final Path DUMMY_HELM_CHART = Paths.get("/some/dir/dummy/dummy-chart.tgz");
    private static final Path DUMMY_KUBE_CONFIG = Paths.get("/some/dir/dummy/kube-config");
    private static final String DUMMY_RELEASE_NAME = "RELEASE_NAME";
    private static final PropertiesToYamlConverter PROPERTIES_TO_YAML_CONVERTER = new PropertiesToYamlConverter();
    private static final List<String> EXPECTED_COMMANDS = Arrays.asList("helm", "install", DUMMY_RELEASE_NAME, "-n",
            "default", DUMMY_HELM_CHART.toString(), "--dry-run", "--kubeconfig", DUMMY_KUBE_CONFIG.toString());

    private static final List<String> EXPECTED_GET_KUBE_KINDS_COMMANDS = Arrays.asList("sh", "-c",
            "helm template " + DUMMY_RELEASE_NAME + " -n default " + DUMMY_HELM_CHART.toString() + " --dry-run"
                    + " --kubeconfig " + DUMMY_KUBE_CONFIG.toString() + " --skip-tests | grep kind | uniq");

    private static final List<String> EXPECTED_GET_KUBE_KINDS_MANIFEST_COMMANDS =
            Arrays.asList("sh", "-c", "helm get manifest " + DUMMY_RELEASE_NAME + " -n default --kubeconfig "
                    + DUMMY_KUBE_CONFIG.toString() + " | grep kind | uniq");

    private static final List<String> EXPECTED_HELM_INSTALL_COMMANDS = Arrays.asList("sh", "-c", "helm install "
            + DUMMY_RELEASE_NAME + " -n default " + DUMMY_HELM_CHART + " --kubeconfig " + DUMMY_KUBE_CONFIG.toString());

    private static final List<String> EXPECTED_HELM_UNINSTALL_COMMANDS = Arrays.asList("helm", "uninstall",
            DUMMY_RELEASE_NAME, "-n", "default", "--kubeconfig", DUMMY_KUBE_CONFIG.toString());

    @Test(expected = Test.None.class)
    public void testRunHelmChartInstallWithDryRunFlag_successfulCase() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("".getBytes());
                final ByteArrayInputStream inputStream = new ByteArrayInputStream("Successful".getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream,
                    SUCCESSFUL_EXIT_CODE, EXPECTED_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);

            objUnderTest.runHelmChartInstallWithDryRunFlag(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG, DUMMY_HELM_CHART);
        }

    }

    @Test(expected = HelmClientExecuteException.class)
    public void testRunHelmChartInstallWithDryRunFlag_processFailed_throwException() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("process failed".getBytes());
                final ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream, FAILED_EXIT_CODE,
                    EXPECTED_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);

            objUnderTest.runHelmChartInstallWithDryRunFlag(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG, DUMMY_HELM_CHART);
        }
    }

    @Test
    public void testGetKubeKinds_successfulCase() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("".getBytes());
                final ByteArrayInputStream inputStream =
                        new ByteArrayInputStream(Constants.KIND_REPLICA_SET.getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_GET_KUBE_KINDS_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream, SUCCESSFUL_EXIT_CODE,
                    EXPECTED_GET_KUBE_KINDS_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);
            final List<String> actualKubeKinds =
                    objUnderTest.getKubeKinds(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG, DUMMY_HELM_CHART);

            assertEquals(Arrays.asList(Constants.KIND_REPLICA_SET), actualKubeKinds);

        }
    }

    @Test
    public void testGetKubeKindsUsingManifestCommand_successfulCase() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("".getBytes());
                final ByteArrayInputStream inputStream =
                        new ByteArrayInputStream(Constants.KIND_DAEMON_SET.getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_GET_KUBE_KINDS_MANIFEST_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream, SUCCESSFUL_EXIT_CODE,
                    EXPECTED_GET_KUBE_KINDS_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);

            final List<String> actualKubeKinds =
                    objUnderTest.getKubeKindsUsingManifestCommand(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG);
            assertEquals(Arrays.asList(Constants.KIND_DAEMON_SET), actualKubeKinds);
        }
    }

    @Test(expected = Test.None.class)
    public void testInstallHelmChart_successfulCase() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("".getBytes());
                final ByteArrayInputStream inputStream = new ByteArrayInputStream("Successful".getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_HELM_INSTALL_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream, SUCCESSFUL_EXIT_CODE,
                    EXPECTED_HELM_INSTALL_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);

            objUnderTest.installHelmChart(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG, DUMMY_HELM_CHART,
                    Collections.emptyMap());
        }
    }

    @Test(expected = Test.None.class)
    public void testUnInstallHelmChart_successfulCase() throws Exception {

        try (final ByteArrayInputStream errorStream = new ByteArrayInputStream("".getBytes());
                final ByteArrayInputStream inputStream = new ByteArrayInputStream("Successful".getBytes());) {

            final ProcessBuilder mockedProcessBuilder = mock(ProcessBuilder.class);

            final ListMatcher expectedCommandsMatcher = new ListMatcher(EXPECTED_HELM_UNINSTALL_COMMANDS);
            mockProcessBuilder(mockedProcessBuilder, expectedCommandsMatcher, errorStream, inputStream, SUCCESSFUL_EXIT_CODE,
                    EXPECTED_HELM_UNINSTALL_COMMANDS);

            final HelmClient objUnderTest =
                    new StubbedHelmClientImpl(PROPERTIES_TO_YAML_CONVERTER, mockedProcessBuilder);

            objUnderTest.unInstallHelmChart(DUMMY_RELEASE_NAME, DUMMY_KUBE_CONFIG);
        }
    }

    private void mockProcessBuilder(final ProcessBuilder mockedProcessBuilder,
            final ListMatcher expectedCommandsMatcher, final ByteArrayInputStream errorStream,
            final ByteArrayInputStream inputStream, final int exitCode, final List<String> expectedCommands)
            throws InterruptedException, IOException {

        final Process mockedProcess = mock(Process.class);
        when(mockedProcessBuilder.command(argThat(expectedCommandsMatcher))).thenReturn(mockedProcessBuilder);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.exitValue()).thenReturn(exitCode);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(mockedProcessBuilder.command()).thenReturn(expectedCommands);
        when(mockedProcessBuilder.start()).thenReturn(mockedProcess);
    }

    private class ListMatcher implements ArgumentMatcher<List<String>> {

        private final List<String> expectArgumentList;

        public ListMatcher(final List<String> expectArgumentList) {
            this.expectArgumentList = expectArgumentList;
        }

        @Override
        public boolean matches(final List<String> actualArgumentList) {
            final boolean result = Utils.isEquals(expectArgumentList, actualArgumentList);
            if (!result) {
                logger.error("Mismatch arguments expected: {}, actual: {}", expectArgumentList, actualArgumentList);
            }
            return result;
        }
    }

    private class StubbedHelmClientImpl extends HelmClientImpl {

        private final ProcessBuilder processBuilder;

        public StubbedHelmClientImpl(final PropertiesToYamlConverter propertiesToYamlConverter,
                final ProcessBuilder processBuilder) {
            super(propertiesToYamlConverter);
            this.processBuilder = processBuilder;
        }

        @Override
        ProcessBuilder getProcessBuilder() {
            return processBuilder;
        }

    }

}
