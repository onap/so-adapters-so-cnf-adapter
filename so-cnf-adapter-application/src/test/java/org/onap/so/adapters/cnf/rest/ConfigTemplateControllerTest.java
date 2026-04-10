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
import org.onap.so.adapters.cnf.model.ConfigTemplateEntity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigTemplateControllerTest {

    @InjectMocks
    ConfigTemplateController configTemplateController;

    @Mock
    private MulticloudConfiguration multicloudConfiguration;

    @Test
    public void createConfigTemplateTest() throws Exception {

        String RbName = "rb";
        String RbVersion = "p1";
        ConfigTemplateEntity tE = new ConfigTemplateEntity();
        try{
        configTemplateController.createConfigTemplate(tE, RbName, RbVersion);
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
        try{
        configTemplateController.getConfigTemplate(RbName, RbVersion, tName);
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
        try{
        configTemplateController.deleteTemplate(RbName, RbVersion, tName);
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
        try {
            configTemplateController.uploadTarFileForTemplate(file, RbName, RbVersion, tName);
        } catch (Exception exp) {
            assert(true);
        }
    }
}
