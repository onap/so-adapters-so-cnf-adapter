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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.onap.aaiclient.client.aai.AAIVersion.V19;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.cnfm.lcm.bpmn.flows.BaseTest;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.AsRequestProcessingException;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobStatusEnum;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.model.AsInfoModificationRequestDeploymentItems;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetList;
import io.kubernetes.client.openapi.models.V1DaemonSetSpec;
import io.kubernetes.client.openapi.models.V1DaemonSetStatus;
import io.kubernetes.client.openapi.models.V1DaemonSetUpdateStrategy;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobCondition;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.openapi.models.V1JobStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ReplicaSetStatus;
import io.kubernetes.client.openapi.models.V1RollingUpdateDaemonSet;
import io.kubernetes.client.openapi.models.V1RollingUpdateStatefulSetStrategy;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.openapi.models.V1StatefulSetStatus;
import io.kubernetes.client.openapi.models.V1StatefulSetUpdateStrategy;
import io.kubernetes.client.util.Watch;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
public class InstantiateAsTaskTest extends BaseTest {
    private static final String NAMESPACE_VALUE = "default";
    private static final String BATCH_V1 = "batch/v1";
    private static final String V1 = "v1";
    private static final String APPS_V1 = "apps/v1";
    private static final String RESPONSE_TYPE_ADDED = "ADDED";
    private static final String DEPLOYMENT_ITEM_1_RELEASE_NAME = "testOne";
    private static final String DEPLOYMENT_ITEM_2_RELEASE_NAME = "testTwo";
    private static final String DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_1 = ".Values.primary.service.ports.mysql";
    private static final String DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_2 = ".Values.primary.service.nodePorts.mysql";

    private static final String DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_1 = ".Values.service.ports.http";
    private static final String DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_2 = ".Values.service.ports.https";
    private static final String DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_3 = ".Values.service.nodePorts";

    private static final String DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE = "dummy";
    private static final String RANDOM_UUID = UUID.randomUUID().toString();
    private static final String SERVICE_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String AS_INST_ID = SERVICE_INSTANCE_ID;

    private static final String SERVICE_INSTANCE_ID2 = UUID.randomUUID().toString();
    private static final String AS_INST_ID2 = SERVICE_INSTANCE_ID2;
    private static final String ASD_NAME = "InstantiateCnfService";
    private static final String AS_INST_NAME = ASD_NAME + "-" + System.currentTimeMillis();
    private static final String ASD_ID = AS_INST_ID;
    private static final String SRC_TEST_DIR = "src/test/resources";

    private static final String RESOURCE_ASD_PACKAGE_CSAR_PATH =
            SRC_TEST_DIR + "/resource-Generatedasdpackage-csar.csar";

    private static final String AS_DEPLOYMENT_ITEM_1_INST_ID = UUID.randomUUID().toString();
    private static final String AS_DEPLOYMENT_ITEM_2_INST_ID = UUID.randomUUID().toString();
    private static final String AS_DEPLOYMENT_ITEM_1_INST_ID2 = UUID.randomUUID().toString();
    private static final String AS_DEPLOYMENT_ITEM_2_INST_ID2 = UUID.randomUUID().toString();

    @Value("${cnfm.csar.dir}")
    private String dir;

    @Value("${cnfm.kube-configs-dir}")
    private String kubeConfigsDir;

    @Autowired
    private JobExecutorService objUnderTest;

    @Autowired
    private MockedHelmClient mockedHelmClient;

    @Autowired
    private MockedKubernetesClientProvider kubernetesClientProvider;

    @Autowired
    private GsonProvider gsonProvider;
    private Gson gson;

    @Before
    public void before() {
        wireMockServer.resetAll();
        try {
            deleteFoldersAndFiles(Paths.get(kubeConfigsDir));
            Files.createDirectory(Paths.get(kubeConfigsDir));
        } catch (final IOException ioException) {
            throw new RuntimeException(
                    "Failed to create/Delete Directory in InstantiateAsTaskTest due to: " + ioException.getMessage());
        }
        kubernetesClientProvider.setWireMockServer(wireMockServer);

        gson = gsonProvider.getGson();
        mockedHelmClient.clear();
    }

    @After
    public void after() {
        wireMockServer.resetAll();
        final Path path = Paths.get(dir, AS_INST_ID);
        try {
            deleteFoldersAndFiles(path);
            deleteFoldersAndFiles(Paths.get(kubeConfigsDir));
        } catch (final IOException ioException) {
            logger.debug("Exception occurred while deleting folder and files: {}", ioException.getMessage());
        }
    }

    @Test
    public void testInstantiateAsWorkflow_JustUpdateStatus_SuccessfullCase() throws InterruptedException, IOException {

        mockKubernetesClientEndpoint();

        mockAAIEndpoints();

        mockSdcPackageDownloadEndpoint();

        final AsInst asInst = createAsInst(AS_INST_ID, AS_DEPLOYMENT_ITEM_1_INST_ID, AS_DEPLOYMENT_ITEM_2_INST_ID);

        databaseServiceProvider.saveAsInst(asInst);

        createKubeConfigFile(asInst);

        final String asLcmOpOccId = objUnderTest.runInstantiateAsJob(asInst.getAsInstId(), getInstantiateAsRequest());

        final Optional<Job> optional = getJobByResourceId(asInst.getAsInstId());
        assertTrue(optional.isPresent());
        final Job job = optional.get();


        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> asInstOptional = databaseServiceProvider.getAsInst(asInst.getAsInstId());
        final AsInst actualAsInst = asInstOptional.get();
        assertEquals(State.INSTANTIATED, actualAsInst.getStatus());

        final Optional<AsLcmOpOcc> asLcmOpOccOptional = databaseServiceProvider.getAsLcmOpOcc(asLcmOpOccId);
        assertTrue(asLcmOpOccOptional.isPresent());
        assertEquals(OperationStateEnum.COMPLETED, asLcmOpOccOptional.get().getOperationState());

        final List<AsDeploymentItem> actualAsDeploymentItems =
                databaseServiceProvider.getAsDeploymentItemByAsInstId(actualAsInst.getAsInstId());
        assertEquals(2, actualAsDeploymentItems.size());

        actualAsDeploymentItems.forEach(asDeploymentItem -> {
            assertEquals(State.INSTANTIATED, asDeploymentItem.getStatus());
        });

        final Map<String, Integer> counter = mockedHelmClient.getCounter();
        assertEquals(2, counter.size());
        assertEquals(Integer.valueOf(3), counter.get(asInst.getAsdeploymentItems().get(0).getReleaseName()));
        assertEquals(Integer.valueOf(3), counter.get(asInst.getAsdeploymentItems().get(1).getReleaseName()));


    }

    @Test
    public void testInstantiateAsWorkflow_JobResourceFailedToStartUp() throws InterruptedException, IOException {

        final String asInstId = UUID.randomUUID().toString();
        final String asDeploymentItem1InstId = UUID.randomUUID().toString();
        final String asDeploymentItem2InstId = UUID.randomUUID().toString();
        final String release_name_3 = "testThree";
        final String release_name_4 = "testFour";

        final String jobResourceResponse = gson.toJson(new Watch.Response<V1Job>(RESPONSE_TYPE_ADDED,
                new V1Job().apiVersion(BATCH_V1).metadata(getV1ObjectMeta())
                        .status(new V1JobStatus().addConditionsItem(new V1JobCondition().type("Failed")
                                .status(Boolean.TRUE.toString()).reason("Image not found")))));

        final AsInst asInst = createAsInst(asInstId, asDeploymentItem1InstId, asDeploymentItem2InstId, release_name_3,
                release_name_4, asInstId);
        databaseServiceProvider.saveAsInst(asInst);

        wireMockServer.stubFor(get(urlMatching("/apis/batch/v1/jobs\\?labelSelector.*(" + release_name_3 + "|"
                + release_name_4 + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(jobResourceResponse).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        mockAAIEndpoints(asInstId, asDeploymentItem1InstId, asDeploymentItem2InstId);
        mockSdcPackageDownloadEndpoint(asInstId);

        createKubeConfigFile(asInst);

        try {
            objUnderTest.runInstantiateAsJob(asInst.getAsInstId(), getInstantiateAsRequest());
        } catch (final Exception exception) {
            assertEquals(AsRequestProcessingException.class, exception.getClass());
        }

        final Optional<Job> optional = getJobByResourceId(asInst.getAsInstId());
        assertTrue(optional.isPresent());
        final Job job = optional.get();


        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> asInstOptional = databaseServiceProvider.getAsInst(asInst.getAsInstId());
        final AsInst actualAsInst = asInstOptional.get();
        assertEquals(State.FAILED, actualAsInst.getStatus());


    }

    @Test(expected = AsRequestProcessingException.class)
    public void testInstantiateAsWorkflow_LifecycleParametersMissing_Fail() throws InterruptedException, IOException {

        mockSdcPackageDownloadEndpoint();

        final AsLifecycleParam lcp3 = new AsLifecycleParam().asLifecycleParam(".Values.extra.missing");
        final AsInst asInst1 = createAsInst(AS_INST_ID2, AS_DEPLOYMENT_ITEM_1_INST_ID2, AS_DEPLOYMENT_ITEM_2_INST_ID2);
        asInst1.getAsdeploymentItems().get(0).asLifecycleParams(lcp3);

        databaseServiceProvider.saveAsInst(asInst1);

        createKubeConfigFile(asInst1);

        objUnderTest.runInstantiateAsJob(asInst1.getAsInstId(), getInstantiateAsRequest());

    }

    @Test
    public void testInstantiateAsWorkflow_UpdateAsInstState_ExceptionCase() {

        final AsInst asInst = new AsInst().asInstId(UUID.randomUUID().toString()).name(AS_INST_NAME).asdId(ASD_ID)
                .asdInvariantId(AS_INST_ID).status(State.NOT_INSTANTIATED).statusUpdatedTime(LocalDateTime.now())
                .asApplicationName("asApplicationName").asApplicationVersion("asApplicationVersion")
                .asProvider("asProvider").serviceInstanceId(SERVICE_INSTANCE_ID)
                .serviceInstanceName("serviceInstanceName").cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId").namespace(NAMESPACE_VALUE);

        databaseServiceProvider.saveAsInst(asInst);

        assertThrows(AsRequestProcessingException.class,
                () -> objUnderTest.runInstantiateAsJob(asInst.getAsInstId(), getInstantiateAsRequest()));

        final Optional<Job> optional = getJobByResourceId(asInst.getAsInstId());
        final Job job = optional.get();

        final Optional<AsInst> asInstOptional = databaseServiceProvider.getAsInst(asInst.getAsInstId());

        assertEquals(JobStatusEnum.ERROR, job.getStatus());
        assertEquals(State.FAILED, asInstOptional.get().getStatus());
    }

    private void mockSdcPackageDownloadEndpoint() throws IOException {
        mockSdcPackageDownloadEndpoint(ASD_ID);
    }

    private void mockSdcPackageDownloadEndpoint(final String asdId) throws IOException {
        wireMockServer.stubFor(get("/sdc/v1/catalog/resources/" + asdId + "/toscaModel")
                .willReturn(aResponse().withBody(getFileContent(getAbsolutePath(RESOURCE_ASD_PACKAGE_CSAR_PATH)))
                        .withHeader(ACCEPT, APPLICATION_OCTET_STREAM_VALUE)));
    }

    private void mockKubernetesClientEndpoint() {
        wireMockServer.stubFor(get(urlMatching("/apis/batch/v1/namespaces/" + NAMESPACE_VALUE
                + "/jobs\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|" + DEPLOYMENT_ITEM_2_RELEASE_NAME
                + ")&timeoutSeconds=1&watch=true"))
                        .willReturn(aResponse().withBody(getJobResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(
                get(urlMatching("/apis/batch/v1/namespaces/" + NAMESPACE_VALUE + "/jobs\\?labelSelector.*&watch=false"))
                        .willReturn(aResponse().withBody(getJobList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching(
                "/api/v1/namespaces/" + NAMESPACE_VALUE + "/pods\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME
                        + "|" + DEPLOYMENT_ITEM_2_RELEASE_NAME + ")&timeoutSeconds=1&watch=true")).willReturn(
                                aResponse().withBody(getPodResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(
                get(urlMatching("/api/v1/namespaces/" + NAMESPACE_VALUE + "/pods\\?labelSelector.*&watch=false"))
                        .willReturn(aResponse().withBody(getPodList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/api/v1/namespaces/" + NAMESPACE_VALUE + "/services\\?labelSelector.*("
                + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|" + DEPLOYMENT_ITEM_2_RELEASE_NAME
                + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(getServiceResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(
                get(urlMatching("/api/v1/namespaces/" + NAMESPACE_VALUE + "/services\\?labelSelector.*&watch=false"))
                        .willReturn(aResponse().withBody(getServiceList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/namespaces/" + NAMESPACE_VALUE
                + "/deployments\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|"
                + DEPLOYMENT_ITEM_2_RELEASE_NAME + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(getDeploymentResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(get(urlMatching(
                "/apis/apps/v1/namespaces/" + NAMESPACE_VALUE + "/deployments\\?labelSelector.*&watch=false"))
                        .willReturn(
                                aResponse().withBody(getDeploymentList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/namespaces/" + NAMESPACE_VALUE
                + "/replicasets\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|"
                + DEPLOYMENT_ITEM_2_RELEASE_NAME + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(getReplicaSetResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(get(urlMatching(
                "/apis/apps/v1/namespaces/" + NAMESPACE_VALUE + "/replicasets\\?labelSelector.*&watch=false"))
                        .willReturn(
                                aResponse().withBody(getReplicaSetList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/namespaces/" + NAMESPACE_VALUE
                + "/daemonsets\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|"
                + DEPLOYMENT_ITEM_2_RELEASE_NAME + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(getDaemonSetResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(get(urlMatching(
                "/apis/apps/v1/namespaces/" + NAMESPACE_VALUE + "/daemonsets\\?labelSelector.*&watch=false"))
                        .willReturn(
                                aResponse().withBody(getDaemonSetList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/namespaces/" + NAMESPACE_VALUE
                + "/statefulsets\\?labelSelector.*(" + DEPLOYMENT_ITEM_1_RELEASE_NAME + "|"
                + DEPLOYMENT_ITEM_2_RELEASE_NAME + ")&timeoutSeconds=1&watch=true")).willReturn(
                        aResponse().withBody(getStatefulSetResponse()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(get(urlMatching(
                "/apis/apps/v1/namespaces/" + NAMESPACE_VALUE + "/statefulsets\\?labelSelector.*&watch=false"))
                        .willReturn(
                                aResponse().withBody(getStatefulSetList()).withHeader(ACCEPT, APPLICATION_JSON_VALUE)));
    }

    private String getStatefulSetResponse() {
        return gson.toJson(new Watch.Response<V1StatefulSet>(RESPONSE_TYPE_ADDED, getStatefulSet()));
    }

    private String getStatefulSetList() {
        final V1StatefulSetList v1StatefulSetList = new V1StatefulSetList();
        v1StatefulSetList.addItemsItem(getStatefulSet());
        return gson.toJson(v1StatefulSetList);
    }

    private V1StatefulSet getStatefulSet() {
        return new V1StatefulSet()
                .apiVersion(
                        APPS_V1)
                .metadata(getV1ObjectMeta())
                .spec(new V1StatefulSetSpec()
                        .updateStrategy(new V1StatefulSetUpdateStrategy().type("RollingUpdate")
                                .rollingUpdate(new V1RollingUpdateStatefulSetStrategy().partition(Integer.valueOf(0))))
                        .replicas(Integer.valueOf(2)))
                .status(new V1StatefulSetStatus().updatedReplicas(Integer.valueOf(2))
                        .readyReplicas(Integer.valueOf(2)));
    }

    private String getDaemonSetResponse() {
        return gson.toJson(new Watch.Response<V1DaemonSet>(RESPONSE_TYPE_ADDED, getDaemonSet()));
    }

    private String getDaemonSetList() {
        final V1DaemonSetList v1DaemonSetList = new V1DaemonSetList();
        v1DaemonSetList.addItemsItem(getDaemonSet());
        return gson.toJson(v1DaemonSetList);
    }

    private V1DaemonSet getDaemonSet() {
        return new V1DaemonSet().apiVersion(APPS_V1).metadata(getV1ObjectMeta())
                .spec(new V1DaemonSetSpec().updateStrategy(new V1DaemonSetUpdateStrategy().type("RollingUpdate")
                        .rollingUpdate(new V1RollingUpdateDaemonSet().maxUnavailable(new IntOrString("50%")))))
                .status(new V1DaemonSetStatus().desiredNumberScheduled(Integer.valueOf(2))
                        .numberReady(Integer.valueOf(2)).updatedNumberScheduled(Integer.valueOf(2)));
    }

    private String getReplicaSetResponse() {
        return gson.toJson(new Watch.Response<V1ReplicaSet>(RESPONSE_TYPE_ADDED, getReplicaSet()));
    }

    private String getReplicaSetList() {
        final V1ReplicaSetList v1ReplicaSetList = new V1ReplicaSetList();
        v1ReplicaSetList.addItemsItem(getReplicaSet());
        return gson.toJson(v1ReplicaSetList);
    }

    private V1ReplicaSet getReplicaSet() {
        return new V1ReplicaSet().apiVersion(APPS_V1).metadata(getV1ObjectMeta())
                .status(new V1ReplicaSetStatus().readyReplicas(Integer.valueOf(1)))
                .spec(new V1ReplicaSetSpec().replicas(Integer.valueOf(1)));
    }

    private V1ObjectMeta getV1ObjectMeta() {
        return new V1ObjectMeta().name("job-name").namespace("job-namespace").uid(RANDOM_UUID)
                .resourceVersion(RANDOM_UUID).labels(Map.of("label-key", "label-value"));
    }

    private String getDeploymentResponse() {
        return gson.toJson(new Watch.Response<V1Deployment>(RESPONSE_TYPE_ADDED, getDeployment()));
    }

    private String getDeploymentList() {
        final V1DeploymentList v1DeploymentList = new V1DeploymentList();
        v1DeploymentList.addItemsItem(getDeployment());
        return gson.toJson(v1DeploymentList);
    }

    private V1Deployment getDeployment() {
        return new V1Deployment().apiVersion(APPS_V1).metadata(getV1ObjectMeta())
                .status(new V1DeploymentStatus().replicas(Integer.valueOf(1)).availableReplicas(Integer.valueOf(1)))
                .spec(new V1DeploymentSpec().replicas(Integer.valueOf(1)));
    }

    private String getServiceResponse() {
        return gson.toJson(new Watch.Response<V1Service>(RESPONSE_TYPE_ADDED, getService()));

    }

    private String getServiceList() {
        final V1ServiceList v1ServiceList = new V1ServiceList();
        v1ServiceList.addItemsItem(getService());
        return gson.toJson(v1ServiceList);
    }

    private V1Service getService() {
        return new V1Service().apiVersion(V1).metadata(getV1ObjectMeta());
    }

    private String getPodList() {
        final V1PodList v1Podlist = new V1PodList();
        v1Podlist.addItemsItem(getPod());
        return gson.toJson(v1Podlist);
    }

    private String getPodResponse() {
        return gson.toJson(new Watch.Response<V1Pod>(RESPONSE_TYPE_ADDED, getPod()));
    }

    private V1Pod getPod() {
        return new V1Pod().apiVersion(V1).metadata(getV1ObjectMeta()).status(new V1PodStatus()
                .addConditionsItem(new V1PodCondition().type("Ready").status(Boolean.TRUE.toString())));
    }

    private String getJobResponse() {
        return gson.toJson(new Watch.Response<V1Job>(RESPONSE_TYPE_ADDED, getJob()));
    }

    private String getJobList() {
        final V1JobList v1JobList = new V1JobList();
        v1JobList.addItemsItem(getJob());
        return gson.toJson(v1JobList);
    }

    private V1Job getJob() {
        return new V1Job().apiVersion(BATCH_V1).metadata(getV1ObjectMeta()).status(new V1JobStatus()
                .addConditionsItem(new V1JobCondition().type("Complete").status(Boolean.TRUE.toString())));
    }

    private void mockAAIEndpoints() throws JsonProcessingException {
        mockAAIEndpoints(AS_INST_ID, AS_DEPLOYMENT_ITEM_1_INST_ID, AS_DEPLOYMENT_ITEM_2_INST_ID);
    }

    private void mockAAIEndpoints(final String as_inst_id, final String as_deployment_item_1_id,
            final String as_deployment_item_2_id) throws JsonProcessingException {
        final String vfModule1EndPoint = "/aai/" + V19 + "/network/generic-vnfs/generic-vnf/" + as_inst_id
                + "/vf-modules/vf-module/" + as_deployment_item_1_id;

        wireMockServer.stubFor(get(urlMatching(vfModule1EndPoint + "\\?resultIndex=0&resultSize=1&format=count"))
                .willReturn(notFound()));

        final String vfModule2EndPoint = "/aai/" + V19 + "/network/generic-vnfs/generic-vnf/" + as_inst_id
                + "/vf-modules/vf-module/" + as_deployment_item_2_id;

        wireMockServer.stubFor(get(urlMatching(vfModule2EndPoint + "\\?resultIndex=0&resultSize=1&format=count"))
                .willReturn(notFound()));

        wireMockServer.stubFor(put(urlMatching(vfModule1EndPoint)).willReturn(ok()));
        wireMockServer.stubFor(put(urlMatching(vfModule2EndPoint)).willReturn(ok()));

        final String k8sResourcesEndpoint = "/aai/" + V19
                + "/cloud-infrastructure/cloud-regions/cloud-region/cloudOwner/cloudRegion/tenants/tenant/tenantId/"
                + "k8s-resources/.*";
        wireMockServer.stubFor(get(urlMatching(k8sResourcesEndpoint)).willReturn(notFound()));
        wireMockServer.stubFor(put(urlMatching(k8sResourcesEndpoint)).willReturn(ok()));
        wireMockServer
                .stubFor(put(urlMatching(k8sResourcesEndpoint + "/relationship-list/relationship")).willReturn(ok()));


    }

    private AsInst createAsInst(final String as_inst_id, final String as_deployment_item_1_id,
            final String as_deployment_item_2_id) {
        return createAsInst(as_inst_id, as_deployment_item_1_id, as_deployment_item_2_id,
                DEPLOYMENT_ITEM_1_RELEASE_NAME, DEPLOYMENT_ITEM_2_RELEASE_NAME, ASD_ID);
    }

    private AsInst createAsInst(final String as_inst_id, final String as_deployment_item_1_id,
            final String as_deployment_item_2_id, final String as_deployment_item_1_release_name,
            final String as_deployment_item_2_release_name, final String asdId) {
        final AsInst asInst = new AsInst().asInstId(as_inst_id).name(AS_INST_NAME).asdId(asdId)
                .asdInvariantId(as_inst_id).status(State.NOT_INSTANTIATED).statusUpdatedTime(LocalDateTime.now())
                .asApplicationName("asApplicationName").asApplicationVersion("asApplicationVersion")
                .asProvider("asProvider").serviceInstanceId(as_inst_id).serviceInstanceName("serviceInstanceName")
                .cloudOwner("cloudOwner").cloudRegion("cloudRegion").tenantId("tenantId").namespace(NAMESPACE_VALUE);

        final String helmFile1 = "Artifacts/Deployment/HELM/sampleapp-db-operator-helm.tgz";
        final AsLifecycleParam lcp1 = new AsLifecycleParam().asLifecycleParam(DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_1);
        final AsLifecycleParam lcp2 = new AsLifecycleParam().asLifecycleParam(DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_2);

        final AsDeploymentItem item1 = new AsDeploymentItem().asDeploymentItemInstId(as_deployment_item_1_id)
                .asInst(asInst).status(State.NOT_INSTANTIATED).name("sampleapp-db").itemId("1").deploymentOrder(1)
                .artifactFilePath(helmFile1).createTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now())
                .releaseName(as_deployment_item_1_release_name).asLifecycleParams(lcp1).asLifecycleParams(lcp2);

        final String helmFile2 = "Artifacts/Deployment/HELM/sampleapp-services-helm.tgz";
        final AsLifecycleParam lcpitem2_1 =
                new AsLifecycleParam().asLifecycleParam(DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_1);
        final AsLifecycleParam lcpitem2_2 =
                new AsLifecycleParam().asLifecycleParam(DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_2);
        final AsLifecycleParam lcpitem2_3 =
                new AsLifecycleParam().asLifecycleParam(DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_3);

        final AsDeploymentItem item2 = new AsDeploymentItem().asDeploymentItemInstId(as_deployment_item_2_id)
                .asInst(asInst).status(State.NOT_INSTANTIATED).name("sampleapp-services").itemId("2").deploymentOrder(2)
                .artifactFilePath(helmFile2).createTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now())
                .releaseName(as_deployment_item_2_release_name).asLifecycleParams(lcpitem2_1)
                .asLifecycleParams(lcpitem2_2).asLifecycleParams(lcpitem2_3);

        asInst.asdeploymentItems(item1);
        asInst.asdeploymentItems(item2);
        return asInst;
    }

    private InstantiateAsRequest getInstantiateAsRequest() {
        final AsInfoModificationRequestDeploymentItems lifecycleParams_1 =
                new AsInfoModificationRequestDeploymentItems().deploymentItemsId("1").lifecycleParameterKeyValues(
                        Map.of(DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_1, DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE,
                                DEPLOYMENT_ITEM_1_LIFECYCLE_PARAM_2, DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE));

        final AsInfoModificationRequestDeploymentItems lifecycleParams_2 =
                new AsInfoModificationRequestDeploymentItems().deploymentItemsId("2").lifecycleParameterKeyValues(
                        Map.of(DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_1, DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE,
                                DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_2, DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE,
                                DEPLOYMENT_ITEM_2_LIFECYCLE_PARAM_3, DEPLOYMENT_ITEM_LIFECYCLE_PARAM_VALUE));

        return new InstantiateAsRequest().addDeploymentItemsItem(lifecycleParams_1)
                .addDeploymentItemsItem(lifecycleParams_2);
    }

    private Path getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.toPath();
    }

    private byte[] getFileContent(final Path path) throws IOException {
        return Files.readAllBytes(path);
    }

}
