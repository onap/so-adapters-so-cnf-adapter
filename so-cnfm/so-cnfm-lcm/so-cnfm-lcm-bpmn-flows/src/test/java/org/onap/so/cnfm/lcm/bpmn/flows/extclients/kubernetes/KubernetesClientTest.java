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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.exceptions.KubernetesRequestProcessingException;
import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetSpec;
import io.kubernetes.client.openapi.models.V1DaemonSetStatus;
import io.kubernetes.client.openapi.models.V1DaemonSetUpdateStrategy;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobCondition;
import io.kubernetes.client.openapi.models.V1JobStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetSpec;
import io.kubernetes.client.openapi.models.V1ReplicaSetStatus;
import io.kubernetes.client.openapi.models.V1RollingUpdateDaemonSet;
import io.kubernetes.client.openapi.models.V1RollingUpdateStatefulSetStrategy;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.openapi.models.V1StatefulSetStatus;
import io.kubernetes.client.openapi.models.V1StatefulSetUpdateStrategy;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class KubernetesClientTest {

    private static final String DUMMY_NAME_SPACE = "default";
    private static final String DUMMY_LABEL_SELECTOR = "app.kubernetes.io/instance=test";
    private static final String BATCH_V1 = "batch/v1";
    private static final String V1 = "v1";
    private static final String APPS_V1 = "apps/v1";
    private static final String RESPONSE_TYPE_ADDED = "ADDED";
    private static final String RANDOM_UUID = UUID.randomUUID().toString();
    private final GsonProvider gsonProvider = new GsonProvider();
    private final Gson gson = gsonProvider.getGson();
    private final JSON json = new JSON();

    private final KubernetesClient objUnderTest = new KubernetesClientImpl();

    @Test
    public void testIsJobReady_jobStatusComplete_true() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(getJobResponse("Complete", "Running"));
        assertTrue(objUnderTest.isJobReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsJobReady_jobStatusFailed_throwException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(getJobResponse("Failed", "Not Running"));
        objUnderTest.isJobReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsJobReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isJobReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsJobReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isJobReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test
    public void testIsJobReady_jobStatusPending_false() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(getJobResponse("pending", "pending"));
        assertFalse(objUnderTest.isJobReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsPodReady_statusTrue_true() throws ApiException, IOException {

        final V1PodCondition condition = new V1PodCondition().type("Ready").status(Boolean.TRUE.toString());
        final ApiClient mockedApiClient = mockApiClientResponse(getPodResponse(condition));
        assertTrue(objUnderTest.isPodReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsPodReady_statusFalse_false() throws ApiException, IOException {

        final V1PodCondition condition = new V1PodCondition().type("Ready").status(Boolean.FALSE.toString());
        final ApiClient mockedApiClient = mockApiClientResponse(getPodResponse(condition));
        assertFalse(objUnderTest.isPodReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsPodReady_missingCondition_false() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(getPodResponse(null));
        assertFalse(objUnderTest.isPodReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsPodReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isPodReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsPodReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isPodReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test
    public void testIsServiceReady_exists_true() throws ApiException, IOException {
        final ApiClient mockedApiClient = mockApiClientResponse(getServiceResponse());
        assertTrue(objUnderTest.isServiceReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsServiceReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isServiceReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsServiceReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isServiceReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test
    public void testIsDeploymentReady_statusAvailable_true() throws ApiException, IOException {

        final V1DeploymentStatus status =
                new V1DeploymentStatus().replicas(Integer.valueOf(2)).availableReplicas(Integer.valueOf(2));
        final V1DeploymentSpec spec = new V1DeploymentSpec().replicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getDeploymentResponse(status, spec));
        assertTrue(objUnderTest.isDeploymentReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsDeploymentReady_statusNotAvailable_false() throws ApiException, IOException {

        final V1DeploymentStatus status =
                new V1DeploymentStatus().replicas(Integer.valueOf(2)).availableReplicas(Integer.valueOf(3));
        final V1DeploymentSpec spec = new V1DeploymentSpec().replicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getDeploymentResponse(status, spec));
        assertFalse(objUnderTest.isDeploymentReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsDeploymentReady_statusIsNull_false() throws ApiException, IOException {

        final V1DeploymentSpec spec = new V1DeploymentSpec().replicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getDeploymentResponse(null, spec));
        assertFalse(objUnderTest.isDeploymentReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsDeploymentReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isDeploymentReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsDeploymentReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isDeploymentReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }


    @Test
    public void testIsReplicaSetReady_statusReady_true() throws ApiException, IOException {

        final V1ReplicaSetStatus status = new V1ReplicaSetStatus().readyReplicas(Integer.valueOf(1));
        final V1ReplicaSetSpec spec = new V1ReplicaSetSpec().replicas(Integer.valueOf(1));

        final ApiClient mockedApiClient = mockApiClientResponse(getReplicaSetResponse(status, spec));
        assertTrue(objUnderTest.isReplicaSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsReplicaSetReady_statusNotReady_false() throws ApiException, IOException {

        final V1ReplicaSetStatus status = new V1ReplicaSetStatus().readyReplicas(Integer.valueOf(1));
        final V1ReplicaSetSpec spec = new V1ReplicaSetSpec().replicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getReplicaSetResponse(status, spec));
        assertFalse(objUnderTest.isReplicaSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsReplicaSetReady_specIsNull_false() throws ApiException, IOException {

        final V1ReplicaSetStatus status = new V1ReplicaSetStatus().readyReplicas(Integer.valueOf(1));

        final ApiClient mockedApiClient = mockApiClientResponse(getReplicaSetResponse(status, null));
        assertFalse(objUnderTest.isReplicaSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsReplicaSetReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isReplicaSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsReplicaSetReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isReplicaSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test
    public void testIsDaemonSetReady_statusRollingUpdate_true() throws ApiException, IOException {

        final V1RollingUpdateDaemonSet rollingUpdate =
                new V1RollingUpdateDaemonSet().maxUnavailable(new IntOrString("50%"));
        final V1DaemonSetSpec spec = new V1DaemonSetSpec()
                .updateStrategy(new V1DaemonSetUpdateStrategy().type("RollingUpdate").rollingUpdate(rollingUpdate));
        final V1DaemonSetStatus status = new V1DaemonSetStatus().desiredNumberScheduled(Integer.valueOf(2))
                .numberReady(Integer.valueOf(2)).updatedNumberScheduled(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getDaemonSetResponse(status, spec));
        assertTrue(objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));


    }

    @Test
    public void testIsDaemonSetReady_statusRollingUpdateTypeRecreate_true() throws ApiException, IOException {

        final V1DaemonSetSpec spec =
                new V1DaemonSetSpec().updateStrategy(new V1DaemonSetUpdateStrategy().type("Recreate"));
        final V1DaemonSetStatus status = new V1DaemonSetStatus();

        final ApiClient mockedApiClient = mockApiClientResponse(getDaemonSetResponse(status, spec));
        assertTrue(objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsDaemonSetReady_updateAndDesiredScheduledNumberIsNotSame_true() throws ApiException, IOException {

        final V1DaemonSetSpec spec =
                new V1DaemonSetSpec().updateStrategy(new V1DaemonSetUpdateStrategy().type("RollingUpdate"));
        final V1DaemonSetStatus status = new V1DaemonSetStatus().desiredNumberScheduled(Integer.valueOf(3))
                .updatedNumberScheduled(Integer.valueOf(2));


        final ApiClient mockedApiClient = mockApiClientResponse(getDaemonSetResponse(status, spec));
        assertFalse(objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsDaemonSetReady_rollingUpdateWithMaxUnavailableIntValAndDesiredNumberReady_true()
            throws ApiException, IOException {

        final V1RollingUpdateDaemonSet rollingUpdate =
                new V1RollingUpdateDaemonSet().maxUnavailable(new IntOrString(4));
        final V1DaemonSetSpec spec = new V1DaemonSetSpec()
                .updateStrategy(new V1DaemonSetUpdateStrategy().type("RollingUpdate").rollingUpdate(rollingUpdate));
        final V1DaemonSetStatus status = new V1DaemonSetStatus().desiredNumberScheduled(Integer.valueOf(6))
                .numberReady(Integer.valueOf(2)).updatedNumberScheduled(Integer.valueOf(6));

        final ApiClient mockedApiClient = mockApiClientResponse(getDaemonSetResponse(status, spec));
        assertTrue(objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));


    }

    @Test
    public void testIsDaemonSetReady_rollingUpdateWithMaxUnavailableIntValAndDesiredNumberNotReady_false()
            throws ApiException, IOException {

        final V1RollingUpdateDaemonSet rollingUpdate =
                new V1RollingUpdateDaemonSet().maxUnavailable(new IntOrString(4));
        final V1DaemonSetSpec spec = new V1DaemonSetSpec()
                .updateStrategy(new V1DaemonSetUpdateStrategy().type("RollingUpdate").rollingUpdate(rollingUpdate));
        final V1DaemonSetStatus status = new V1DaemonSetStatus().desiredNumberScheduled(Integer.valueOf(6))
                .numberReady(Integer.valueOf(1)).updatedNumberScheduled(Integer.valueOf(6));

        final ApiClient mockedApiClient = mockApiClientResponse(getDaemonSetResponse(status, spec));
        assertFalse(objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsDaemonSetReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsDaemonSetReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isDaemonSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test
    public void testIsStatefulSetReady_rollingUpdateAndReplicasSameAsReadyReplicas_true()
            throws IOException, ApiException {

        final V1StatefulSetUpdateStrategy updateStrategy = new V1StatefulSetUpdateStrategy().type("RollingUpdate")
                .rollingUpdate(new V1RollingUpdateStatefulSetStrategy().partition(Integer.valueOf(0)));
        final V1StatefulSetSpec spec =
                new V1StatefulSetSpec().updateStrategy(updateStrategy).replicas(Integer.valueOf(2));
        final V1StatefulSetStatus status =
                new V1StatefulSetStatus().updatedReplicas(Integer.valueOf(2)).readyReplicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getStatefulSetResponse(status, spec));
        assertTrue(objUnderTest.isStatefulSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));


    }

    @Test
    public void testIsStatefulSetReady_updateStrategyRecreate_true() throws IOException, ApiException {

        final V1StatefulSetUpdateStrategy updateStrategy = new V1StatefulSetUpdateStrategy().type("Recreate");
        final V1StatefulSetSpec spec = new V1StatefulSetSpec().updateStrategy(updateStrategy);
        final V1StatefulSetStatus status = new V1StatefulSetStatus();

        final ApiClient mockedApiClient = mockApiClientResponse(getStatefulSetResponse(status, spec));
        assertTrue(objUnderTest.isStatefulSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));

    }

    @Test
    public void testIsStatefulSetReady_rollingUpdateAndUnExpectedReplicas_false() throws IOException, ApiException {

        final V1StatefulSetUpdateStrategy updateStrategy = new V1StatefulSetUpdateStrategy().type("RollingUpdate")
                .rollingUpdate(new V1RollingUpdateStatefulSetStrategy().partition(Integer.valueOf(2)));
        final V1StatefulSetSpec spec =
                new V1StatefulSetSpec().updateStrategy(updateStrategy).replicas(Integer.valueOf(6));
        final V1StatefulSetStatus status = new V1StatefulSetStatus().updatedReplicas(Integer.valueOf(2));

        final ApiClient mockedApiClient = mockApiClientResponse(getStatefulSetResponse(status, spec));
        assertFalse(objUnderTest.isStatefulSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR));
    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsStatefulSetReady_apiExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(ApiException.class);
        objUnderTest.isStatefulSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    @Test(expected = KubernetesRequestProcessingException.class)
    public void testIsStatefulSetReady_RuntimeExceptionThrown_throwsException() throws ApiException, IOException {

        final ApiClient mockedApiClient = mockApiClientResponse(RuntimeException.class);
        objUnderTest.isStatefulSetReady(mockedApiClient, DUMMY_NAME_SPACE, DUMMY_LABEL_SELECTOR);

    }

    private String getStatefulSetResponse(final V1StatefulSetStatus status, final V1StatefulSetSpec spec) {
        return gson.toJson(new Watch.Response<V1StatefulSet>(RESPONSE_TYPE_ADDED, getStatefulSet(status, spec)));
    }

    private V1StatefulSet getStatefulSet(final V1StatefulSetStatus status, final V1StatefulSetSpec spec) {
        return new V1StatefulSet().apiVersion(APPS_V1).metadata(getV1ObjectMeta()).spec(spec).status(status);
    }

    private String getDaemonSetResponse(final V1DaemonSetStatus status, final V1DaemonSetSpec spec) {
        return gson.toJson(new Watch.Response<V1DaemonSet>(RESPONSE_TYPE_ADDED, getDaemonSet(status, spec)));
    }

    private V1DaemonSet getDaemonSet(final V1DaemonSetStatus status, final V1DaemonSetSpec spec) {
        return new V1DaemonSet().apiVersion(APPS_V1).metadata(getV1ObjectMeta()).spec(spec).status(status);
    }

    private String getReplicaSetResponse(final V1ReplicaSetStatus status, final V1ReplicaSetSpec spec) {
        return gson.toJson(new Watch.Response<V1ReplicaSet>(RESPONSE_TYPE_ADDED, getReplicaSet(status, spec)));
    }

    private V1ReplicaSet getReplicaSet(final V1ReplicaSetStatus status, final V1ReplicaSetSpec spec) {
        return new V1ReplicaSet().apiVersion(APPS_V1).metadata(getV1ObjectMeta()).status(status).spec(spec);
    }

    private String getDeploymentResponse(final V1DeploymentStatus status, final V1DeploymentSpec spec) {
        return gson.toJson(new Watch.Response<V1Deployment>(RESPONSE_TYPE_ADDED, getDeployment(status, spec)));
    }

    private V1Deployment getDeployment(final V1DeploymentStatus status, final V1DeploymentSpec spec) {
        return new V1Deployment().apiVersion(APPS_V1).metadata(getV1ObjectMeta()).status(status).spec(spec);
    }

    private String getServiceResponse() {
        return gson.toJson(new Watch.Response<V1Service>(RESPONSE_TYPE_ADDED, getService()));
    }

    private V1Service getService() {
        return new V1Service().apiVersion(V1).metadata(getV1ObjectMeta());
    }

    private String getPodResponse(final V1PodCondition condition) {
        return gson.toJson(new Watch.Response<V1Pod>(RESPONSE_TYPE_ADDED, getPod(condition)));
    }

    private V1Pod getPod(final V1PodCondition condition) {
        final V1Pod pod = new V1Pod().apiVersion(V1).metadata(getV1ObjectMeta());
        if (condition != null) {
            return pod.status(new V1PodStatus().addConditionsItem(condition));
        }
        return pod;
    }

    private String getJobResponse(final String type, final String reason) {
        return gson.toJson(new Watch.Response<V1Job>(RESPONSE_TYPE_ADDED, getJob(type, reason)));
    }

    private V1Job getJob(final String type, final String reason) {
        return new V1Job().apiVersion(BATCH_V1).metadata(getV1ObjectMeta()).status(new V1JobStatus()
                .addConditionsItem(new V1JobCondition().type(type).status(Boolean.TRUE.toString()).reason(reason)));
    }

    private V1ObjectMeta getV1ObjectMeta() {
        return new V1ObjectMeta().name("job-name").namespace("job-namespace").uid(RANDOM_UUID)
                .resourceVersion(RANDOM_UUID).labels(Map.of("label-key", "label-value"));
    }

    private ApiClient mockApiClientResponse(final String response) throws IOException, ApiException {
        final ApiClient mockedApiClient = mock(ApiClient.class);
        final Response mockedResponse = mock(Response.class);
        final ResponseBody mockedResponseBody = mock(ResponseBody.class);
        final BufferedSource mockedBufferedSource = mock(BufferedSource.class);
        final Call mockedCall = mock(Call.class);

        when(mockedApiClient.escapeString(anyString())).thenCallRealMethod();
        when(mockedApiClient.getJSON()).thenReturn(json);
        doNothing().when(mockedResponse).close();
        when(mockedResponse.body()).thenReturn(mockedResponseBody);
        when(mockedBufferedSource.readUtf8Line()).thenReturn(response);
        when(mockedBufferedSource.exhausted()).thenReturn(false).thenReturn(true);
        when(mockedResponseBody.source()).thenReturn(mockedBufferedSource);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedApiClient.buildCall(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockedCall);

        return mockedApiClient;
    }

    private ApiClient mockApiClientResponse(Class<? extends Throwable> exception) throws ApiException {
        final ApiClient mockedApiClient = mock(ApiClient.class);
        doThrow(exception).when(mockedApiClient).buildCall(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any());
        return mockedApiClient;
    }
}
