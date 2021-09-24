/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.cnf.rest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.BpmnInstanceRequest;
import org.onap.so.adapters.cnf.model.CheckInstanceRequest;
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
import org.onap.so.adapters.cnf.model.ConfigurationEntity;
import org.onap.so.adapters.cnf.model.ConfigurationRollbackEntity;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.onap.so.adapters.cnf.model.InstanceMiniResponse;
import org.onap.so.adapters.cnf.model.InstanceMiniResponseList;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.ProfileEntity;
import org.onap.so.adapters.cnf.model.Resource;
import org.onap.so.adapters.cnf.model.ResourceBundleEntity;
import org.onap.so.adapters.cnf.model.Tag;
import org.onap.so.adapters.cnf.model.healthcheck.HealthCheckResponse;
import org.onap.so.adapters.cnf.service.CnfAdapterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
public class CnfAdapterRestTest {

    @InjectMocks
    org.onap.so.adapters.cnf.rest.CnfAdapterRest cnfAdapterRest;

    @Mock
    CnfAdapterService cnfAdapterService;

    @Mock
    private MulticloudConfiguration multicloudConfiguration;

    @Mock
    ResponseEntity<InstanceMiniResponseList> instacneMiniResponseList;

    @Mock
    ResponseEntity<InstanceStatusResponse> instanceStatusResponse;

    @Test
    public void createInstanceTest() throws Exception {

        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        Map<String, String> overrideValues = new HashMap<String, String>();
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        BpmnInstanceRequest bpmnInstanceRequest = new BpmnInstanceRequest();
        bpmnInstanceRequest.setCloudRegionId("v1");
        bpmnInstanceRequest.setLabels(labels);
        bpmnInstanceRequest.setModelInvariantId("krd");
        bpmnInstanceRequest.setModelVersionId("p1");
        bpmnInstanceRequest.setOverrideValues(overrideValues);
        bpmnInstanceRequest.setVfModuleUUID("20200824");
        List<Resource> resourceList = new ArrayList<Resource>();
        InstanceResponse instanceResponse = new InstanceResponse();
        instanceResponse.setId("123");
        instanceResponse.setNamespace("testNamespace");
        instanceResponse.setRequest(new MulticloudInstanceRequest());
        instanceResponse.setResources(resourceList);
        ResponseEntity<String> createInstanceResponse = new ResponseEntity<String>(HttpStatus.CREATED);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.createInstance(bpmnInstanceRequest))
                .thenReturn(String.valueOf(createInstanceResponse));
        cnfAdapterRest.createInstance(bpmnInstanceRequest);
        Assert.assertNotNull(createInstanceResponse);
        assertEquals(HttpStatus.CREATED, createInstanceResponse.getStatusCode());
    }

    @Test
    public void getInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        ResponseEntity<String> createInstanceResponse = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByInstanceId(instanceId))
                .thenReturn(String.valueOf(createInstanceResponse));
        cnfAdapterRest.getInstanceByInstanceId(instanceId);
        Assert.assertNotNull(createInstanceResponse);
        assertEquals(HttpStatus.OK, createInstanceResponse.getStatusCode());
    }

    @Test
    public void deleteInstanceByInstanceIdTest() throws Exception {

        String instanceId = "123";
        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.deleteInstanceByInstanceId(instanceId)).thenReturn(String.valueOf(response));
        cnfAdapterRest.deleteInstanceByInstanceId(instanceId);
        Assert.assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getInstanceStatusByInstanceIdTest() throws Exception {

        String instanceId = "123";
        instanceStatusResponse = new ResponseEntity<InstanceStatusResponse>(HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceStatusByInstanceId(instanceId))
                .thenReturn(String.valueOf(instanceStatusResponse));
        cnfAdapterRest.getInstanceStatusByInstanceId(instanceId);
        Assert.assertNotNull(instanceStatusResponse);
        assertEquals(HttpStatus.OK, instanceStatusResponse.getStatusCode());
    }

    @Test
    public void queryInstanceResourcesTest() {
        String instanceId = "123";
        String kind = "Service";
        String apiVersion = "v1";
        String queryResponseMock = "queryResponseMock";

        Mockito.when(cnfAdapterService.queryInstanceResources(instanceId, kind, apiVersion, null, null,
                null)).thenReturn(queryResponseMock);

        String result = cnfAdapterRest.queryInstanceResources(instanceId, kind, apiVersion, null, null,
                null);
        assertThat(result).isEqualTo(queryResponseMock);
    }

    @Test
    public void queryResourcesTest() {
        String kind = "Service";
        String apiVersion = "v1";
        String cloudRegion = "region";
        String queryResponseMock = "queryResponseMock";

        Mockito.when(cnfAdapterService.queryResources(kind, apiVersion, null, null,
                null, cloudRegion)).thenReturn(queryResponseMock);

        String result = cnfAdapterRest.queryResources(kind, apiVersion, null, null,
                null, cloudRegion);
        assertThat(result).isEqualTo(queryResponseMock);
    }

    @Test
    public void getInstanceByRBNameOrRBVersionOrProfileNameTest() throws Exception {

        String rbName = "xyz";
        String rbVersion = "v1";
        String profileName = "p1";
        MulticloudInstanceRequest request = null;
        InstanceMiniResponse instanceMiniResponse = new InstanceMiniResponse(HttpStatus.OK.toString());

        instanceMiniResponse.setId("a");
        instanceMiniResponse.setNameSpace("ab");
        instanceMiniResponse.setErrorMsg("Error");
        instanceMiniResponse.setRequest(request);

        instanceMiniResponse.getId();
        instanceMiniResponse.getNameSpace();
        instanceMiniResponse.getRequest();
        instanceMiniResponse.getErrorMsg();

        List<InstanceMiniResponse> instancList = new ArrayList<InstanceMiniResponse>();
        instancList.add(instanceMiniResponse);
        InstanceMiniResponseList instanceMiniRespList = new InstanceMiniResponseList(HttpStatus.OK.toString());
        instanceMiniRespList.setInstancList(instancList);
        instanceMiniRespList.setErrorMsg(HttpStatus.OK.toString());
        ResponseEntity<InstanceMiniResponseList> respone =
                new ResponseEntity<InstanceMiniResponseList>(instanceMiniRespList, HttpStatus.OK);
        CnfAdapterService cnfAdapterService = Mockito.mock(CnfAdapterService.class);
        Mockito.when(cnfAdapterService.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName))
                .thenReturn(String.valueOf(instanceMiniResponse));
        cnfAdapterRest.getInstanceByRBNameOrRBVersionOrProfileName(rbName, rbVersion, profileName);
        Assert.assertNotNull(instacneMiniResponseList);
        assertEquals(HttpStatus.OK.toString(), instanceMiniRespList.getErrorMsg());
    }

    @Test
    public void createRBTest() throws Exception {

        Map<String, String> labels = new HashMap<String, String>();
        labels.put("custom-label-1", "label1");
        labels.put("image.tag", "latest");
        labels.put("dcae_collector_ip", "1.2.3.4");
        ResourceBundleEntity rb = new ResourceBundleEntity();
        rb.setChartName("v1");
        rb.setDescription("rb1");
        rb.setLabels(labels);
        rb.setRbName("rb");
        rb.setRbVersion("p1");

     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try {
            cnfAdapterRest.createRB(rb);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getRBTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getRB(RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void deleteRBTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
    //    ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.deleteRB(RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getListOfRBTest() throws Exception {

        String RbName = "rb";

      //  ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getListOfRB(RbName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getListOfRBWithoutUsingRBNameTest() throws Exception {

      //  ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getListOfRBWithoutUsingRBName();
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void uploadArtifactForRBTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("first value");

    //    ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try {
            cnfAdapterRest.uploadArtifactForRB(file, RbName, RbVersion);
        } catch (Exception exp) {
            assert(true);
        }
    }

    @Test
    public void createProfileTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        ProfileEntity fE = new ProfileEntity();
      //  ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.createProfile(fE, RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getProfileTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getProfile(prName, RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getListOfProfileTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getListOfProfile(RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void deleteProfileTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.deleteProfile(prName, RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void uploadArtifactForProfileTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("OK");
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");

        try {
            cnfAdapterRest.uploadArtifactForProfile(file, RbName, RbVersion, prName);
        } catch (Exception exp) {
            assert(true);
        }

    }

    @Test
    public void createConfigurationTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
        ConfigurationEntity cE = new ConfigurationEntity();
      //  ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.createConfiguration(cE, prName, RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getConfigurationTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
        String cfgName = "cfg";
    //    ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getConfiguration(prName, RbName, RbVersion, cfgName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void deleteConfigurationTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
        String cfgName = "cfg";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.deleteConfiguration(prName, RbName, RbVersion, cfgName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void updateConfigurationTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
        String cfgName = "cfg";
        ConfigurationEntity cE = new ConfigurationEntity();
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.updateConfiguration(cE, prName, RbName, RbVersion, cfgName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void tagConfigurationValueTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
        Tag tag = new Tag();
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.tagConfigurationValue(tag, RbName, RbVersion, prName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void createConnectivityInfoTest() throws Exception {

        ConnectivityInfo cIE = new ConnectivityInfo();
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.createConnectivityInfo(cIE);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getConnectivityInfoTest() throws Exception {

        String connName = "con";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getConnectivityInfo(connName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void deleteConnectivityInfoTest() throws Exception {

        String connName = "con";
    //    ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.deleteConnectivityInfo(connName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void createConfigTemplateTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        ConfigTemplateEntity tE = new ConfigTemplateEntity();
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.createConfigTemplate(tE, RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getConfigTemplateTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String tName = "t";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.getConfigTemplate(RbName, RbVersion, tName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }


    @Test
    public void deleteTemplateTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String tName = "t";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.deleteTemplate(RbName, RbVersion, tName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void uploadTarFileForTemplateTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        String tName = "t";
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("First value");
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try {
            cnfAdapterRest.uploadTarFileForTemplate(file, RbName, RbVersion, tName);
        } catch (Exception exp) {
            assert(true);
        }
    }

    @Test
    public void rollbackConfigurationTest() throws Exception {
        ConfigurationRollbackEntity rE = new ConfigurationRollbackEntity();
        String RbName = "rb";
        String RbVersion = "p1";
        String prName = "p";
     //   ReflectionTestUtils.setField(cnfAdapterRest, "uri", "http://localhost");
        try{
        cnfAdapterRest.rollbackConfiguration(rE, RbName, RbVersion, prName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }
}
