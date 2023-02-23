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

package org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes;

import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DAEMON_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_DEPLOYMENT;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_JOB;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_POD;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_REPLICA_SET;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_SERVICE;
import static org.onap.so.cnfm.lcm.bpmn.flows.Constants.KIND_STATEFUL_SET;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestProcessingException;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestTimeOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.apimachinery.GroupVersion;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetList;
import io.kubernetes.client.openapi.models.V1DaemonSetSpec;
import io.kubernetes.client.openapi.models.V1DaemonSetStatus;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobCondition;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ReplicaSetStatus;
import io.kubernetes.client.openapi.models.V1RollingUpdateStatefulSetStrategy;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.openapi.models.V1StatefulSetStatus;
import io.kubernetes.client.openapi.models.V1StatefulSetUpdateStrategy;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;
import okhttp3.Call;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class KubernetesClientImpl implements KubernetesClient {
    private static final String ROLLING_UPDATE = "RollingUpdate";
    private static final String EVENT_TYPE_ERROR = "ERROR";
    private static final String EVENT_TYPE_DELETED = "DELETED";
    private static final String EVENT_TYPE_MODIFIED = "MODIFIED";
    private static final String EVENT_TYPE_ADDED = "ADDED";
    private static final String TRUE_STRING = Boolean.TRUE.toString();
    private static final String JOB_FAILED = "Failed";
    private static final String JOB_COMPLETE = "Complete";
    private static final boolean DISABLE_WATCH = false;
    private static final boolean ENABLE_WATCH = true;
    private static final String POD_READY = "Ready";

    private static final Logger logger = LoggerFactory.getLogger(KubernetesClientImpl.class);

    /**
     * Event Listener timeout in seconds Note: this should be less then the timeout camunda task execution
     */
    @Value("${kubernetes.client.http-request.timeoutSeconds:5}")
    private Integer timeoutSeconds;

    @Override
    public boolean isJobReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if Job is ready using labelSelector: {}", labelSelector);
        try {
            final BatchV1Api batchV1Api = new BatchV1Api(apiClient);
            final Call call = batchV1Api.listJobForAllNamespacesCall(null, null, null, labelSelector, null, null, null,
                    null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1Job, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1Job>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1Job, String>> jobNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isJobReady))
                        .collect(Collectors.toList());

                if (jobNotReadyList.isEmpty()) {
                    logger.debug("JobList is ready ...");
                    return true;
                }
                logger.debug("JobList is not yet Ready: {}", jobNotReadyList);
                return false;

            }

            logger.warn("No items found in jobList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_JOB, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_JOB, labelSelector, runtimeException);
        }
        logger.debug("Returning false as Job is not ready ...");
        return false;
    }

    @Override
    public boolean isPodReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if Pod is ready using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            final Call call = coreV1Api.listPodForAllNamespacesCall(null, null, null, labelSelector, null, null, null,
                    null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1Pod, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1Pod>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1Pod, String>> podNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isPodReady))
                        .collect(Collectors.toList());

                if (podNotReadyList.isEmpty()) {
                    logger.debug("PodList is ready ...");
                    return true;
                }
                logger.debug("PodList is not yet Ready: {}", podNotReadyList);
                return false;

            }

            logger.warn("No items found in podList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_POD, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_POD, labelSelector, runtimeException);
        }

        logger.debug("Returning false as Pod is not ready ...");

        return false;
    }

    @Override
    public boolean isServiceReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if Service is ready using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api api = new CoreV1Api(apiClient);
            final Call call = api.listServiceForAllNamespacesCall(null, null, null, labelSelector, null, null, null,
                    null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1Service, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1Service>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1Service, String>> serviceNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isServiceReady))
                        .collect(Collectors.toList());

                if (serviceNotReadyList.isEmpty()) {
                    logger.debug("ServiceList is ready ...");
                    return true;
                }
                logger.debug("ServiceList is not yet Ready: {}", serviceNotReadyList);
                return false;

            }

            logger.warn("No items found in serviceList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_SERVICE, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_SERVICE, labelSelector, runtimeException);
        }

        logger.debug("Returning false as Service is not ready ...");
        return false;
    }

    @Override
    public boolean isDeploymentReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if Deployment is ready using labelSelector: {}", labelSelector);

        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final Call call = appsV1Api.listDeploymentForAllNamespacesCall(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1Deployment, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1Deployment>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1Deployment, String>> deploymentNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isDeploymentReady))
                        .collect(Collectors.toList());

                if (deploymentNotReadyList.isEmpty()) {
                    logger.debug("DeploymentList is ready ...");
                    return true;
                }
                logger.debug("DeploymentList is not yet Ready: {}", deploymentNotReadyList);
                return false;

            }

            logger.warn("No items found in deploymentList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_DEPLOYMENT, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DEPLOYMENT, labelSelector, runtimeException);
        }

        logger.debug("Returning false as Deployment is not ready ...");
        return false;
    }

    @Override
    public boolean isReplicaSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if ReplicaSet is ready using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final Call call = appsV1Api.listReplicaSetForAllNamespacesCall(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1ReplicaSet, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1ReplicaSet>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1ReplicaSet, String>> replicaSet = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isReplicaSetReady))
                        .collect(Collectors.toList());

                if (replicaSet.isEmpty()) {
                    logger.debug("ReplicaSetList is ready ...");
                    return true;
                }
                logger.debug("ReplicaSetList is not yet Ready: {}", replicaSet);
                return false;

            }

            logger.warn("No items found in replicaSetList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_REPLICA_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_REPLICA_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as ReplicaSet is not ready ...");
        return false;
    }

    @Override
    public boolean isDaemonSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if DaemonSet is ready using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final Call call = appsV1Api.listDaemonSetForAllNamespacesCall(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1DaemonSet, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1DaemonSet>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1DaemonSet, String>> daemonSetNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isDaemonSetReady))
                        .collect(Collectors.toList());

                if (daemonSetNotReadyList.isEmpty()) {
                    logger.debug("DaemonSetList is ready ...");
                    return true;
                }
                logger.debug("DaemonSetList is not yet Ready: {}", daemonSetNotReadyList);
                return false;

            }

            logger.warn("No items found in daemonSetList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_DAEMON_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DAEMON_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as DaemonSet is not ready ...");
        return false;
    }

    @Override
    public boolean isStatefulSetReady(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Will check if StatefulSet is ready using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final Call call = appsV1Api.listStatefulSetForAllNamespacesCall(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, ENABLE_WATCH, null);

            final Map<V1StatefulSet, String> readyResources =
                    getReadyResources(apiClient, call, new TypeToken<Response<V1StatefulSet>>() {}.getType());

            if (!readyResources.isEmpty()) {
                final List<Entry<V1StatefulSet, String>> statefulSetNotReadyList = readyResources.entrySet().stream()
                        .filter(entry -> !isResourceReady(entry.getKey(), entry.getValue(), this::isStatefulSetReady))
                        .collect(Collectors.toList());

                if (statefulSetNotReadyList.isEmpty()) {
                    logger.debug("StatefulSetList is ready ...");
                    return true;
                }
                logger.debug("StatefulSetList is not yet Ready: {}", statefulSetNotReadyList);
                return false;

            }

            logger.warn("No items found in statefulSetList : {}", readyResources);
            return false;

        } catch (final ApiException exception) {
            handleApiException(KIND_STATEFUL_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_STATEFUL_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as StatefulSet is not ready ...");
        return false;
    }

    @Override
    public boolean isServiceDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is Service deleted by using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            final V1ServiceList v1ServiceList = coreV1Api.listServiceForAllNamespaces(null, null, null, labelSelector,
                    null, null, null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list service for all Namespaces: {}", v1ServiceList);
            return v1ServiceList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_SERVICE, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_SERVICE, labelSelector, runtimeException);
        }
        logger.debug("Returning false as Service is not Deleted ...");
        return false;
    }

    @Override
    public boolean isPodDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is Pod deleted by using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            final V1PodList v1PodList = coreV1Api.listPodForAllNamespaces(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list Pod for all Namespaces: {}", v1PodList);
            return v1PodList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_POD, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_POD, labelSelector, runtimeException);
        }
        logger.debug("Returning false as Pod is not Deleted ...");
        return false;
    }

    @Override
    public boolean isJobDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is Job deleted by using labelSelector: {}", labelSelector);
        try {
            final BatchV1Api batchV1Api = new BatchV1Api(apiClient);
            final V1JobList v1JobList = batchV1Api.listJobForAllNamespaces(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list Job for all Namespaces: {}", v1JobList);
            return v1JobList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_JOB, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_JOB, labelSelector, runtimeException);
        }
        logger.debug("Returning false as Job is not Deleted ...");
        return false;
    }

    @Override
    public boolean isDeploymentDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is Deployment deleted by using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api batchV1Api = new AppsV1Api(apiClient);
            final V1DeploymentList v1DeploymentList = batchV1Api.listDeploymentForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list Deployment for all Namespaces: {}", v1DeploymentList);
            return v1DeploymentList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_DEPLOYMENT, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DEPLOYMENT, labelSelector, runtimeException);
        }
        logger.debug("Returning false as Deployment is not Deleted ...");
        return false;
    }

    @Override
    public boolean isReplicaSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is ReplicaSet deleted by using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api batchV1Api = new AppsV1Api(apiClient);
            final V1ReplicaSetList v1ReplicaSetList = batchV1Api.listReplicaSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list ReplicaSet for all Namespaces: {}", v1ReplicaSetList);
            return v1ReplicaSetList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_REPLICA_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_REPLICA_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as ReplicaSet is not Deleted ...");
        return false;
    }

    @Override
    public boolean isDaemonSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is DaemonSet deleted by using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api batchV1Api = new AppsV1Api(apiClient);
            final V1DaemonSetList v1DaemonSetList = batchV1Api.listDaemonSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list DaemonSet for all Namespaces: {}", v1DaemonSetList);
            return v1DaemonSetList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_DAEMON_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DAEMON_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as DaemonSet is not Deleted ...");
        return false;
    }

    @Override
    public boolean isStatefulSetDeleted(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Check is StatefulSet deleted by using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api batchV1Api = new AppsV1Api(apiClient);
            final V1StatefulSetList v1StatefulSetList = batchV1Api.listStatefulSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);
            logger.debug("Response from list StatefulSet for all Namespaces: {}", v1StatefulSetList);
            return v1StatefulSetList.getItems().isEmpty();
        } catch (final ApiException exception) {
            handleApiException(KIND_STATEFUL_SET, labelSelector, exception);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_STATEFUL_SET, labelSelector, runtimeException);
        }
        logger.debug("Returning false as StatefulSet is not Deleted ...");
        return false;
    }


    @Override
    public List<KubernetesResource> getJobResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving Jobs using labelSelector: {}", labelSelector);
        try {
            final BatchV1Api batchV1Api = new BatchV1Api(apiClient);
            final V1JobList jobList = batchV1Api.listJobForAllNamespaces(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received Jobs: {}", jobList);
            return getKubernetesResource(jobList);

        } catch (final ApiException exception) {
            handleApiException(KIND_JOB, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_JOB, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_JOB, labelSelector, runtimeException);
        }

        logger.error("Unable to find any job resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getDeploymentResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving Deployment using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final V1DeploymentList deploymentList = appsV1Api.listDeploymentForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received Deployments: {}", deploymentList);
            return getKubernetesResource(deploymentList);

        } catch (final ApiException exception) {
            handleApiException(KIND_DEPLOYMENT, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_DEPLOYMENT, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DEPLOYMENT, labelSelector, runtimeException);
        }

        logger.error("Unable to find any Deployment resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getPodResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving Pod using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            final V1PodList podList = coreV1Api.listPodForAllNamespaces(null, null, null, labelSelector, null, null,
                    null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received Pods: {}", podList);
            return getKubernetesResource(podList);

        } catch (final ApiException exception) {
            handleApiException(KIND_POD, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_POD, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_POD, labelSelector, runtimeException);
        }

        logger.error("Unable to find any Pod resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getServiceResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving Service using labelSelector: {}", labelSelector);
        try {
            final CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            final V1ServiceList serviceList = coreV1Api.listServiceForAllNamespaces(null, null, null, labelSelector,
                    null, null, null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received Services: {}", serviceList);
            return getKubernetesResource(serviceList);

        } catch (final ApiException exception) {
            handleApiException(KIND_SERVICE, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_SERVICE, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_SERVICE, labelSelector, runtimeException);
        }

        logger.error("Unable to find any Service resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getReplicaSetResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving ReplicaSet using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            final V1ReplicaSetList replicaSetList = appsV1Api.listReplicaSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received ReplicaSets: {}", replicaSetList);
            return getKubernetesResource(replicaSetList);

        } catch (final ApiException exception) {
            handleApiException(KIND_REPLICA_SET, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_REPLICA_SET, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_REPLICA_SET, labelSelector, runtimeException);
        }

        logger.error("Unable to find any ReplicaSet resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getDaemonSetResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving DaemonSet using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);

            final V1DaemonSetList daemonSetList = appsV1Api.listDaemonSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received DaemonSets: {}", daemonSetList);
            return getKubernetesResource(daemonSetList);

        } catch (final ApiException exception) {
            handleApiException(KIND_DAEMON_SET, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_DAEMON_SET, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_DAEMON_SET, labelSelector, runtimeException);
        }

        logger.error("Unable to find any DaemonSet resources ...");
        return Collections.emptyList();
    }

    @Override
    public List<KubernetesResource> getStatefulSetResources(final ApiClient apiClient, final String labelSelector)
            throws KubernetesRequestProcessingException {
        logger.debug("Retrieving StatefulSet using labelSelector: {}", labelSelector);
        try {
            final AppsV1Api appsV1Api = new AppsV1Api(apiClient);

            final V1StatefulSetList statefulSetList = appsV1Api.listStatefulSetForAllNamespaces(null, null, null,
                    labelSelector, null, null, null, null, timeoutSeconds, DISABLE_WATCH);

            logger.debug("Received StatefulSets: {}", statefulSetList);
            return getKubernetesResource(statefulSetList);

        } catch (final ApiException exception) {
            handleApiException(KIND_STATEFUL_SET, labelSelector, exception);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleIllegalArgumentException(KIND_STATEFUL_SET, labelSelector, illegalArgumentException);
        } catch (final RuntimeException runtimeException) {
            handleRuntimeException(KIND_STATEFUL_SET, labelSelector, runtimeException);
        }

        logger.error("Unable to find any StatefulSet resources ...");
        return Collections.emptyList();

    }

    private List<KubernetesResource> getKubernetesResource(final KubernetesListObject kubernetesListObject) {
        if (kubernetesListObject != null && kubernetesListObject.getItems() != null) {
            final List<KubernetesResource> kubernetesResources = new ArrayList<>();
            final List<? extends KubernetesObject> items = kubernetesListObject.getItems();
            items.forEach(item -> {
                final String apiVersion =
                        item.getApiVersion() != null ? item.getApiVersion() : kubernetesListObject.getApiVersion();
                final String kind = item.getKind() != null ? item.getKind() : kubernetesListObject.getKind();
                kubernetesResources.add(getKubernetesResource(apiVersion, kind, item.getMetadata()));
            });
            logger.debug("KubernetesResources found: {}", kubernetesResources);
            return kubernetesResources;
        }
        logger.error("kubernetesListObject or items is null {}", kubernetesListObject);
        return Collections.emptyList();
    }

    private KubernetesResource getKubernetesResource(final String apiVersion, final String kind,
            final V1ObjectMeta metadata) {
        final GroupVersion groupVersion = GroupVersion.parse(apiVersion);
        return new KubernetesResource().id(metadata.getUid()).name(metadata.getName()).group(groupVersion.getGroup())
                .version(groupVersion.getVersion()).kind(kind).resourceVersion(metadata.getResourceVersion())
                .namespace(metadata.getNamespace() != null ? metadata.getNamespace() : "")
                .labels(getLabels(metadata.getLabels()));
    }

    private List<String> getLabels(final Map<String, String> labels) {
        if (labels != null) {
            final List<String> result = new ArrayList<>();
            labels.entrySet().forEach(entry -> result.add(entry.getKey() + "=" + entry.getValue()));
            return result;
        }
        return Collections.emptyList();

    }

    private boolean isJobReady(final V1Job job) {
        if (job.getStatus() != null && job.getStatus().getConditions() != null) {
            logger.debug("Received Job with conditions ..");
            for (final V1JobCondition condition : job.getStatus().getConditions()) {
                if (JOB_COMPLETE.equalsIgnoreCase(condition.getType())
                        && TRUE_STRING.equalsIgnoreCase(condition.getStatus())) {
                    logger.debug("Job completed successfully ...");
                    return true;
                }
                if (JOB_FAILED.equalsIgnoreCase(condition.getType())
                        && TRUE_STRING.equalsIgnoreCase(condition.getStatus())) {
                    final String message = "Job failed with reason: " + condition.getReason();
                    logger.error(message);
                    throw new KubernetesRequestProcessingException(message);

                }
            }
        }

        logger.debug("Job is not ready ...");
        return false;
    }

    private boolean isPodReady(final V1Pod pod) {
        final Optional<V1PodCondition> optional = getPodReadyCondition(pod);
        if (optional.isPresent()) {
            final V1PodCondition condition = optional.get();
            return TRUE_STRING.equalsIgnoreCase(condition.getStatus());
        }

        return false;
    }

    private boolean isServiceReady(final V1Service service) {
        return true;
    }

    private boolean isDeploymentReady(final V1Deployment deployment) {
        final V1DeploymentSpec spec = deployment.getSpec();
        final V1DeploymentStatus status = deployment.getStatus();

        if (status == null || status.getReplicas() == null || status.getAvailableReplicas() == null) {
            logger.debug("AvailableReplicas is null in status");
            return false;
        }

        if (spec == null || spec.getReplicas() == null) {
            logger.debug("Replicas is null in spec");
            return false;
        }

        return spec.getReplicas().intValue() == status.getReplicas().intValue()
                && status.getAvailableReplicas().intValue() <= spec.getReplicas().intValue();
    }

    private boolean isReplicaSetReady(final V1ReplicaSet replicaSet) {
        final V1ReplicaSetSpec spec = replicaSet.getSpec();
        final V1ReplicaSetStatus status = replicaSet.getStatus();

        if (status == null || status.getReadyReplicas() == null) {
            logger.debug("ReadyReplicas is null in status");
            return false;
        }

        if (spec == null || spec.getReplicas() == null) {
            logger.debug("Replicas is null in spec");
            return false;
        }

        return spec.getReplicas().intValue() == status.getReadyReplicas().intValue();
    }

    private boolean isDaemonSetReady(final V1DaemonSet daemonSet) {

        final V1DaemonSetSpec spec = daemonSet.getSpec();
        final V1DaemonSetStatus status = daemonSet.getStatus();

        if (status == null || spec == null) {
            logger.debug("Found null status/spec \n DaemonSet: {}", daemonSet);
            return false;
        }

        if (spec.getUpdateStrategy() != null && spec.getUpdateStrategy().getType() != null) {
            if (!ROLLING_UPDATE.equalsIgnoreCase(spec.getUpdateStrategy().getType())) {
                logger.debug("Type is {} returning true", spec.getUpdateStrategy().getType());
                return true;
            }
        }

        if (status.getDesiredNumberScheduled() != null && status.getUpdatedNumberScheduled() != null) {
            if (status.getUpdatedNumberScheduled().intValue() != status.getDesiredNumberScheduled().intValue()) {
                logger.debug("DaemonSet is not ready {} out of {} expected pods have been scheduled",
                        status.getUpdatedNumberScheduled(), status.getDesiredNumberScheduled());
                return false;
            }

            if (spec.getUpdateStrategy() != null && spec.getUpdateStrategy().getRollingUpdate() != null
                    && status.getNumberReady() != null) {

                final Integer maxUnavailable =
                        getMaxUnavailable(spec.getUpdateStrategy().getRollingUpdate().getMaxUnavailable(),
                                status.getDesiredNumberScheduled());

                final int expectedReady = status.getDesiredNumberScheduled().intValue() - maxUnavailable.intValue();
                final int numberReady = status.getNumberReady().intValue();
                if (!(numberReady >= expectedReady)) {
                    logger.debug("DaemonSet is not ready {} out of {} expected pods are ready", numberReady,
                            expectedReady);
                    return false;
                }
                logger.debug("DaemonSet is ready {} out of {} expected pods are ready", numberReady, expectedReady);
                return true;
            }

        }

        return false;
    }

    private boolean isStatefulSetReady(final V1StatefulSet statefulSet) {
        final V1StatefulSetSpec spec = statefulSet.getSpec();
        final V1StatefulSetStatus status = statefulSet.getStatus();

        if (status == null || spec == null) {
            logger.debug("Found null status/spec \n StatefulSet: {}", statefulSet);
            return false;
        }

        final V1StatefulSetUpdateStrategy updateStrategy = spec.getUpdateStrategy();
        if (updateStrategy != null && updateStrategy.getType() != null) {
            if (!ROLLING_UPDATE.equalsIgnoreCase(updateStrategy.getType())) {
                logger.debug("Type is {} returning true", updateStrategy.getType());
                return true;
            }

            // Dereference all the pointers because StatefulSets like them
            int partition = 0;
            // 1 is the default for replicas if not set
            int replicas = 1;
            final V1RollingUpdateStatefulSetStrategy rollingUpdate = updateStrategy.getRollingUpdate();
            if (rollingUpdate != null && rollingUpdate.getPartition() != null) {
                partition = updateStrategy.getRollingUpdate().getPartition().intValue();
            }

            if (spec.getReplicas() != null) {
                replicas = spec.getReplicas().intValue();
            }

            final int expectedReplicas = replicas - partition;

            if (status.getUpdatedReplicas() != null && status.getUpdatedReplicas().intValue() < expectedReplicas) {
                logger.debug("StatefulSet is not ready. {} out of {} expected pods have been scheduled",
                        status.getUpdatedReplicas(), expectedReplicas);
                return false;
            }

            if (status.getReadyReplicas() != null && status.getReadyReplicas().intValue() != replicas) {
                logger.debug("StatefulSet is not ready. {} out of {} expected pods are ready",
                        status.getReadyReplicas(), replicas);
                return false;
            }

            logger.debug("{} out of {} expected pods are ready", status.getReadyReplicas(), replicas);
            logger.debug("StatefulSet is Ready...");
            return true;
        }

        logger.debug("StatefulSet is not ready ...");
        return false;
    }

    private Integer getMaxUnavailable(final IntOrString maxUnavailable, final Integer desiredNumberScheduled) {
        if (maxUnavailable == null) {
            logger.debug("maxUnavailable value is {}", maxUnavailable);
            return desiredNumberScheduled;
        }

        if (maxUnavailable.isInteger()) {
            logger.debug("maxUnavailable is Integer: {}", maxUnavailable);
            return maxUnavailable.getIntValue();
        }

        if (!maxUnavailable.isInteger()) {
            final Integer maxUnavailableIntValue = getIntegerValue(maxUnavailable);
            if (maxUnavailableIntValue != null) {
                return (maxUnavailableIntValue.intValue() * desiredNumberScheduled.intValue()) / 100;
            }

            logger.debug("maxUnavailableIntValue is null {}", maxUnavailableIntValue);
        }
        logger.debug("Returning desiredNumberScheduled: {}", desiredNumberScheduled);
        return desiredNumberScheduled;
    }

    private Integer getIntegerValue(final IntOrString maxUnavailable) {
        try {
            final String strValue = maxUnavailable.getStrValue();
            if (strValue != null && strValue.length() > 1) {
                if (strValue.contains("%")) {
                    final String val = strValue.trim().replace("%", "");
                    return Integer.valueOf(val);
                }
                logger.debug("maxUnavailable is not a percentage");
            }
        } catch (final Exception exception) {
            logger.error("Unable to parse maxUnavailable value: {}", maxUnavailable);
        }
        return null;
    }

    private void closeWatchSilently(final Watch<?> watch) {
        try {
            watch.close();
        } catch (final IOException exception) {
            logger.warn("Unexpected IOException while closing watch suppressing exception");
        }
    }

    private void handleRuntimeException(final String resourceType, final String labelSelector,
            final RuntimeException runtimeException) {
        if (runtimeException.getCause() instanceof SocketTimeoutException) {
            final Throwable cause = runtimeException.getCause();
            final String message = "Unexpected SocketTimeoutException occurred while getting " + resourceType
                    + " status using labelSelector: " + labelSelector + " message: " + cause.getMessage();
            logger.error(message, cause);
            throw new KubernetesRequestTimeOut(message, cause);
        }
        final String message = "Unexpected RuntimeException occurred while getting " + resourceType
                + " status using labelSelector: " + labelSelector;
        logger.error(message, runtimeException);
        throw new KubernetesRequestProcessingException(message, runtimeException);
    }

    private void handleApiException(final String resourceType, final String labelSelector,
            final ApiException exception) {
        final String message = "Unexpected ApiException occurred while getting " + resourceType
                + " status using labelSelector: " + labelSelector + " \n response code: " + exception.getCode()
                + " \n response body: " + exception.getResponseBody();
        logger.error(message, exception);
        throw new KubernetesRequestProcessingException(message, exception);
    }

    private void handleIllegalArgumentException(final String resourceType, final String labelSelector,
            final IllegalArgumentException illegalArgumentException) {
        final String message = "Unexpected IllegalArgumentException occurred while getting " + resourceType
                + " resource using labelSelector: " + labelSelector;
        logger.error(message, illegalArgumentException);
        throw new KubernetesRequestProcessingException(message, illegalArgumentException);
    }

    private Optional<V1PodCondition> getPodReadyCondition(final V1Pod pod) {
        if (pod.getStatus() != null && pod.getStatus().getConditions() != null) {
            final List<V1PodCondition> conditions = pod.getStatus().getConditions();
            return conditions.stream().filter(condition -> POD_READY.equals(condition.getType()))
                    .peek(condition -> logger.debug("Found {}", condition)).findFirst();

        }
        logger.warn("Unable to find a {} condition {}", POD_READY, pod.getStatus());
        return Optional.empty();
    }

    /**
     * Capturing resource events and objects
     * 
     * @param <T>
     * @param apiClient
     * @param call
     * @param type
     * @return
     * @throws ApiException
     */
    private <T> Map<T, String> getReadyResources(final ApiClient apiClient, final Call call, final Type type)
            throws ApiException {
        final Watch<T> watch = Watch.createWatch(apiClient, call, type);
        logger.debug("Listening for {} events ....", type.getTypeName());

        final Map<T, String> resources = new HashMap<>();
        try {
            while (watch.hasNext()) {
                final Response<T> next = watch.next();
                final T object = next.object;
                logger.debug("Received object: {}", object);
                final String event = next.type;
                logger.debug("Received Event: {}", event);
                resources.put(object, event);
            }

        } finally {
            closeWatchSilently(watch);
        }
        logger.debug("Finished Listening for {} events ....", type.getTypeName());
        return resources;

    }

    private static <T> boolean isResourceReady(final T object, final String eventType, final Predicate<T> predicate) {

        switch (eventType) {
            case EVENT_TYPE_ADDED:
            case EVENT_TYPE_MODIFIED:
                final boolean isReady = predicate.test(object);
                logger.debug("{} is {} ...", object != null ? object.getClass().getSimpleName() : object,
                        isReady ? "ready" : " not ready");
                return isReady;
            case EVENT_TYPE_DELETED:
                logger.debug("{} event received marking it as successfully", EVENT_TYPE_DELETED);
                return true;
            case EVENT_TYPE_ERROR:
                final String message = "Error event received for " + (object != null ? object.getClass() : "null");
                logger.error(message);
                logger.debug("{} received: {}", (object != null ? object.getClass() : "null"), object);
                throw new KubernetesRequestProcessingException(message);

            default:
                logger.warn("Unhandled event received ... ");
                return false;
        }

    }
}
