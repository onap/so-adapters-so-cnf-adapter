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
package org.onap.so.cnfm.lcm.rest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.cnfm.lcm.bpmn.flows.service.KubConfigProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sagar Shetty (sagar.shetty@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudKubeConfigControllerTest {

    @Mock
    private KubConfigProvider objUnderTest;

    @InjectMocks
    private CloudKubeConfigController controller;

    @Test
    public void uploadKubeConfigTest() throws Exception {
        final MultipartFile file = Mockito.mock(MultipartFile.class);
        final ResponseEntity<String> response = controller.uploadKubeConfig("owner", "athlone", "1234", file);
        assertEquals(202, response.getStatusCode().value());
        Mockito.verify(objUnderTest, Mockito.times(1)).addKubeConfigFile(file, "owner", "athlone", "1234");
    }

}
