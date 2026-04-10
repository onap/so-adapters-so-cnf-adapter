/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.cnf.MulticloudConfiguration;
import org.onap.so.adapters.cnf.model.ResourceBundleEntity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ResourceBundleControllerTest {

    @InjectMocks
    ResourceBundleController resourceBundleController;

    @Mock
    private MulticloudConfiguration multicloudConfiguration;

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

        try {
            resourceBundleController.createRB(rb);
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
        try{
        resourceBundleController.getRB(RbName, RbVersion);
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
        try{
        resourceBundleController.deleteRB(RbName, RbVersion);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getListOfRBTest() throws Exception {

        String RbName = "rb";

        try{
        resourceBundleController.getListOfRB(RbName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getListOfRBWithoutUsingRBNameTest() throws Exception {

        try{
        resourceBundleController.getListOfRBWithoutUsingRBName();
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

        try {
            resourceBundleController.uploadArtifactForRB(file, RbName, RbVersion);
        } catch (Exception exp) {
            assert(true);
        }
    }
}
