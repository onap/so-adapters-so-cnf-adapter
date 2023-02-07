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
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.aaiclient.client.aai.AAIVersion.V19;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import io.kubernetes.client.openapi.models.V1DaemonSetList;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.beans.nsmf.OrchestrationStatusEnum;
import org.onap.so.cnfm.lcm.bpmn.flows.BaseTest;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.onap.so.cnfm.lcm.database.beans.State;
import org.onap.so.cnfm.lcm.model.TerminateAsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class TerminateAsTaskTest extends BaseTest {

    private static final String AS_INST_ID = UUID.randomUUID().toString();
    private static final String AS_DEPLOYMENT_ITEM_ONE_INST_ID = UUID.randomUUID().toString();
    private static final String AS_DEPLOYMENT_ITEM_TWO_INST_ID = UUID.randomUUID().toString();

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
                    "Failed to create/Delete Directory in TerminateAsTaskTest due to: " + ioException.getMessage());
        }
        kubernetesClientProvider.setWireMockServer(wireMockServer);

        gson = gsonProvider.getGson();
    }

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    @Test
    public void testTerminateAsTask_SuccessfulCase() throws InterruptedException, IOException {

        mockKubernetesClientEndpoint();
        mockAAIEndPoints();
        addDummyAsToDatabase(AS_INST_ID);

        final String asLcmOpOccId = objUnderTest.runTerminateAsJob(AS_INST_ID, new TerminateAsRequest());

        final Optional<Job> optional = getJobByResourceId(AS_INST_ID);
        assertTrue(optional.isPresent());
        final Job job = optional.get();
        assertTrue(waitForProcessInstanceToFinish(job.getProcessInstanceId()));

        final HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(job.getProcessInstanceId());
        assertNotNull(historicProcessInstance);
        assertEquals(HistoricProcessInstance.STATE_COMPLETED, historicProcessInstance.getState());

        final Optional<AsInst> optionalAsInst = databaseServiceProvider.getAsInst(AS_INST_ID);
        assertTrue(optionalAsInst.isPresent());
        final AsInst updatedAsInst = optionalAsInst.get();
        assertEquals(State.NOT_INSTANTIATED, updatedAsInst.getStatus());

        final Optional<AsLcmOpOcc> optionalAsLcmOpOcc = databaseServiceProvider.getAsLcmOpOcc(asLcmOpOccId);
        assertTrue(optionalAsLcmOpOcc.isPresent());
        final AsLcmOpOcc asLcmOpOcc = optionalAsLcmOpOcc.get();
        assertEquals(OperationStateEnum.COMPLETED, asLcmOpOcc.getOperationState());

        final Map<String, Integer> counter = mockedHelmClient.getUnInstallCounter();
        assertEquals(2, counter.size());
    }

    private void addDummyAsToDatabase(final String asInstanceId) throws IOException {
        final String asInstName = "TerminateCnfService-" + System.currentTimeMillis();
        final AsInst asInst = new AsInst().asInstId(asInstanceId).name(asInstName).asdId(asInstanceId)
                .asdInvariantId(asInstanceId).status(State.INSTANTIATED).statusUpdatedTime(LocalDateTime.now())
                .asApplicationName("asApplicationName").asApplicationVersion("asApplicationVersion")
                .asProvider("asProvider").serviceInstanceId(SERVICE_INSTANCE_ID)
                .serviceInstanceName(SERVICE_INSTANCE_NAME).cloudOwner("cloudOwner").cloudRegion("cloudRegion")
                .tenantId("tenantId");

        final String helmFile1 = "Artifacts/Deployment/HELM/sampleapp-db-operator-helm.tgz";
        final AsDeploymentItem dItemOne = new AsDeploymentItem().asDeploymentItemInstId(AS_DEPLOYMENT_ITEM_ONE_INST_ID)
                .asInst(asInst).status(State.INSTANTIATED).name("sampleapp-db").itemId("1").deploymentOrder(1)
                .artifactFilePath(helmFile1).createTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now())
                .releaseName("testOne");

        final String helmFile2 = "Artifacts/Deployment/HELM/sampleapp-services-helm.tgz";
        final AsDeploymentItem dItemTwo = new AsDeploymentItem().asDeploymentItemInstId(AS_DEPLOYMENT_ITEM_TWO_INST_ID)
                .asInst(asInst).status(State.INSTANTIATED).name("sampleapp-services").itemId("2").deploymentOrder(2)
                .artifactFilePath(helmFile2).createTime(LocalDateTime.now()).lastUpdateTime(LocalDateTime.now())
                .releaseName("testTwo");

        asInst.asdeploymentItems(dItemOne);
        asInst.asdeploymentItems(dItemTwo);
        databaseServiceProvider.saveAsInst(asInst);
        createKubeConfigFile(asInst);
    }

    private void mockKubernetesClientEndpoint() {

        wireMockServer.stubFor(get(urlMatching("/apis/batch/v1/jobs\\?labelSelector.*&watch=false"))
                .willReturn(aResponse().withBody(getJobList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/api/v1/pods\\?labelSelector.*&watch=false"))
                .willReturn(aResponse().withBody(getPodList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/api/v1/services\\?labelSelector.*&watch=false"))
                .willReturn(aResponse().withBody(getServiceList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/deployments\\?labelSelector.*&watch=false")).willReturn(
                aResponse().withBody(getDeploymentList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/daemonsets\\?labelSelector.*&watch=false"))
                .willReturn(aResponse().withBody(getDaemonList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/replicasets\\?labelSelector.*&watch=false")).willReturn(
                aResponse().withBody(getReplicaSetList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        wireMockServer.stubFor(get(urlMatching("/apis/apps/v1/statefulsets\\?labelSelector.*&watch=false")).willReturn(
                aResponse().withBody(getStatefulSetList()).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    private void mockAAIEndPoints() {

        final String vnfEndPoint = "/aai/" + V19 + "/network/generic-vnfs/generic-vnf/" + AS_INST_ID;

        wireMockServer.stubFor(get(urlMatching(vnfEndPoint)).willReturn(
                aResponse().withBody(gson.toJson(getGenericVnf())).withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
        wireMockServer.stubFor(post(urlMatching(vnfEndPoint)).willReturn(ok()));
    }

    private String getServiceList() {
        final V1ServiceList v1SeviceList = new V1ServiceList();
        v1SeviceList.setApiVersion("v1");
        v1SeviceList.setKind("ServiceList");
        return gson.toJson(v1SeviceList);
    }

    private String getPodList() {
        final V1PodList v1PodList = new V1PodList();
        v1PodList.setApiVersion("v1");
        v1PodList.setKind("PodList");
        return gson.toJson(v1PodList);
    }

    private String getJobList() {
        final V1JobList v1JobList = new V1JobList();
        v1JobList.setApiVersion("v1");
        v1JobList.setKind("JobList");
        return gson.toJson(v1JobList);
    }

    private String getDeploymentList() {
        final V1DeploymentList v1DeploymentList = new V1DeploymentList();
        v1DeploymentList.setApiVersion("v1");
        v1DeploymentList.setKind("DeploymentList");
        return gson.toJson(v1DeploymentList);
    }

    private String getDaemonList() {
        final V1DaemonSetList v1DaemonSetList = new V1DaemonSetList();
        v1DaemonSetList.setApiVersion("v1");
        v1DaemonSetList.setKind("DeploymentList");
        return gson.toJson(v1DaemonSetList);
    }

    private String getStatefulSetList() {
        final V1StatefulSetList v1StatefulSetList = new V1StatefulSetList();
        v1StatefulSetList.setApiVersion("v1");
        v1StatefulSetList.setKind("DeploymentList");
        return gson.toJson(v1StatefulSetList);
    }

    private String getReplicaSetList() {
        final V1ReplicaSetList v1ReplicaSetList = new V1ReplicaSetList();
        v1ReplicaSetList.setApiVersion("v1");
        v1ReplicaSetList.setKind("DeploymentList");
        return gson.toJson(v1ReplicaSetList);
    }

    private GenericVnf getGenericVnf() {
        final GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(AS_INST_ID);
        vnf.setOrchestrationStatus(OrchestrationStatusEnum.ACTIVATED.getValue());
        vnf.setResourceVersion("12345");
        return vnf;
    }

}
