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

import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DAEMON_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DEPLOYMENT;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_JOB;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_POD;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_REPLICA_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_SERVICE;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_STATEFUL_SET;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jvnet.jaxb2_commons.lang.StringUtils;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.HelmClientExecuteException;
import org.onap.so.cnfm.lcm.bpmn.flows.utils.PropertiesToYamlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelmClientImpl implements HelmClient {
    private static final String KIND_KEY = "kind: ";
    private static final String ANY_UNICODE_NEWLINE = "\\R";
    private static final Logger logger = LoggerFactory.getLogger(HelmClientImpl.class);
    private final PropertiesToYamlConverter propertiesToYamlConverter;

    @Autowired
    public HelmClientImpl(final PropertiesToYamlConverter propertiesToYamlConverter) {
        this.propertiesToYamlConverter = propertiesToYamlConverter;
    }

    private static final Set<String> SUPPORTED_KINDS = Set.of(KIND_JOB, KIND_POD, KIND_SERVICE, KIND_DEPLOYMENT,
            KIND_REPLICA_SET, KIND_DAEMON_SET, KIND_STATEFUL_SET);

    @Override
    public void runHelmChartInstallWithDryRunFlag(final String namespace, final String releaseName,
            final Path kubeconfig, final Path helmChart) throws HelmClientExecuteException {
        logger.info("Running dry-run on {} to cluster {} using namespace: {}, releaseName: {}", helmChart, kubeconfig,
                namespace, releaseName);
        final ProcessBuilder processBuilder = prepareDryRunCommand(namespace, releaseName, kubeconfig, helmChart);
        executeCommand(processBuilder);
        logger.info("Successfully ran dry for Chart {}", helmChart);

    }

    @Override
    public List<String> getKubeKinds(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart) {
        logger.info("Retrieving kinds from chart {} using namespace: {}, releaseName {}", helmChart, namespace,
                releaseName);
        final ProcessBuilder processBuilder = prepareKubeKindCommand(namespace, releaseName, kubeconfig, helmChart);
        final String response = executeCommand(processBuilder);
        if (StringUtils.isEmpty(response)) {
            logger.warn("Response is empty: {}", response);
            return Collections.emptyList();
        }
        final List<String> kinds = processKinds(response);

        logger.debug("Found kinds: {}", kinds);
        return kinds;
    }


    @Override
    public List<String> getKubeKindsUsingManifestCommand(final String namespace, final String releaseName,
            final Path kubeConfig) throws HelmClientExecuteException {
        logger.info("Retrieving kinds from helm release history using namespace: {}, releaseName {}", namespace,
                releaseName);

        final ProcessBuilder processBuilder = prepareGetKubeKindCommand(namespace, releaseName, kubeConfig);
        final String response = executeCommand(processBuilder);
        if (StringUtils.isEmpty(response)) {
            logger.warn("Response is empty: {}", response);
            return Collections.emptyList();
        }
        final List<String> kinds = processKinds(response);

        logger.debug("Kinds found from the helm release history: {}", kinds);
        return kinds;
    }

    @Override
    public void installHelmChart(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart, final Map<String, String> lifeCycleParams) throws HelmClientExecuteException {
        logger.info("Installing {} to cluster {} using releaseName: {}", helmChart, kubeconfig, releaseName);
        final ProcessBuilder processBuilder =
                prepareInstallCommand(namespace, releaseName, kubeconfig, helmChart, lifeCycleParams);
        executeCommand(processBuilder);
        logger.info("Chart {} installed successfully", helmChart);

    }

    @Override
    public void unInstallHelmChart(final String namespace, final String releaseName, final Path kubeConfigFilePath)
            throws HelmClientExecuteException {
        logger.info("uninstalling the release {} from cluster {}", releaseName, kubeConfigFilePath);
        final ProcessBuilder processBuilder = prepareUnInstallCommand(namespace, releaseName, kubeConfigFilePath);
        final String commandResponse = executeCommand(processBuilder);
        if (!StringUtils.isEmpty(commandResponse) && commandResponse.contains("Release not loaded")) {
            throw new HelmClientExecuteException(
                    "Unable to find the installed Helm chart by using releaseName: " + releaseName);
        }

        logger.info("Release {} uninstalled successfully", releaseName);
    }

    private ProcessBuilder prepareDryRunCommand(final String namespace, final String releaseName, final Path kubeconfig,
            final Path helmChart) {
        final List<String> helmArguments = List.of("helm", "install", releaseName, "-n", namespace,
                helmChart.toString(), "--dry-run", "--kubeconfig", kubeconfig.toString());
        return getProcessBuilder().command(helmArguments);
    }

    private ProcessBuilder prepareInstallCommand(final String namespace, final String releaseName,
            final Path kubeconfig, final Path helmChart, final Map<String, String> lifeCycleParams) {
        final List<String> commands = new ArrayList<>(List.of("helm", "install", releaseName, "-n", namespace,
                helmChart.toString(), "--kubeconfig", kubeconfig.toString()));

        if (lifeCycleParams != null && !lifeCycleParams.isEmpty()) {
            final String fileName = helmChart.getParent().resolve("values.yaml").toString();
            createYamlFile(fileName, lifeCycleParams);
            commands.add("-f ".concat(fileName));
        }
        final List<String> helmArguments = List.of("sh", "-c", toString(commands));
        return getProcessBuilder().command(helmArguments);
    }

    private void createYamlFile(final String fileName, final Map<String, String> lifeCycleParams) {
        logger.debug("Will create the runtime values.yaml file.");
        final String yamlContent = propertiesToYamlConverter.getValuesYamlFileContent(lifeCycleParams);
        logger.debug("Yaml file content : {}", yamlContent);
        try {
            Files.write(Paths.get(fileName), yamlContent.getBytes());
        } catch (final IOException ioException) {
            throw new HelmClientExecuteException(
                    "Failed to create the run time life cycle yaml file: {} " + ioException.getMessage(), ioException);
        }
    }

    private ProcessBuilder prepareUnInstallCommand(final String namespace, final String releaseName,
            final Path kubeConfig) {
        logger.debug("Will remove tis log after checking ubeconfig path: {}", kubeConfig.toFile().getName());
        final List<String> helmArguments = new ArrayList<>(
                List.of("helm", "uninstall", releaseName, "-n", namespace, "--kubeconfig", kubeConfig.toString()));
        return getProcessBuilder().command(helmArguments);
    }

    private ProcessBuilder prepareKubeKindCommand(final String namespace, final String releaseName,
            final Path kubeconfig, final Path helmChart) {
        final List<String> commands = List.of("helm", "template", releaseName, "-n", namespace, helmChart.toString(),
                "--dry-run", "--kubeconfig", kubeconfig.toString(), "--skip-tests", "| grep kind | uniq");
        final List<String> helmArguments = List.of("sh", "-c", toString(commands));
        return getProcessBuilder().command(helmArguments);
    }

    private ProcessBuilder prepareGetKubeKindCommand(final String namespace, final String releaseName,
            final Path kubeconfig) {
        final List<String> commands = List.of("helm", "get", "manifest", releaseName, "-n", namespace, "--kubeconfig",
                kubeconfig.toString(), "| grep kind | uniq");
        final List<String> helmArguments = List.of("sh", "-c", toString(commands));
        return getProcessBuilder().command(helmArguments);
    }

    private String executeCommand(final ProcessBuilder processBuilder) throws HelmClientExecuteException {
        final String commandStr = toString(processBuilder);

        try {
            logger.debug("Executing cmd: {}", commandStr);
            final Process process = processBuilder.start();

            final InputStreamConsumer errors = new InputStreamConsumer(process.getErrorStream());
            final InputStreamConsumer output = new InputStreamConsumer(process.getInputStream());

            final Thread errorsConsumer = new Thread(errors);
            final Thread outputConsumer = new Thread(output);
            errorsConsumer.start();
            outputConsumer.start();

            process.waitFor();

            errorsConsumer.join();
            outputConsumer.join();

            final int exitValue = process.exitValue();
            if (exitValue != 0) {
                final String stderr = errors.getContent();
                if (!stderr.isEmpty()) {
                    throw new HelmClientExecuteException("Command execution failed: " + commandStr + " " + stderr);
                }
            }

            final String stdout = output.getContent();
            logger.debug("Command <{}> execution, output: {}", commandStr, stdout);
            return stdout;

        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new HelmClientExecuteException(
                    "Failed to execute the Command: " + commandStr + ", the command was interrupted",
                    interruptedException);
        } catch (final Exception exception) {
            throw new HelmClientExecuteException("Failed to execute the Command: " + commandStr, exception);
        }
    }

    private List<String> processKinds(final String response) {

        logger.debug("Processing kube kinds");

        final List<String> kinds = new ArrayList<>();
        for (final String entry : response.split(ANY_UNICODE_NEWLINE)) {
            if (entry != null) {
                final String line = entry.trim();
                if (!line.isBlank()) {
                    final String kind = line.replace(KIND_KEY, "").trim();
                    if (SUPPORTED_KINDS.contains(kind)) {
                        logger.debug("Found Supported kind: {}", kind);
                        kinds.add(kind);
                    } else {
                        logger.warn("kind: {} is not currently supported", kind);
                    }
                }
            }
        }
        return kinds;
    }

    private String toString(final ProcessBuilder processBuilder) {
        return String.join(" ", processBuilder.command());
    }

    private String toString(final List<String> commands) {
        return String.join(" ", commands);
    }

    ProcessBuilder getProcessBuilder() {
        return new ProcessBuilder();
    }

}
