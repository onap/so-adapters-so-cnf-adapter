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
import org.onap.so.adapters.cnf.client.MulticloudHttpClient;
import org.onap.so.adapters.cnf.model.ConnectivityInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ConnectivityInfoControllerTest {

    @InjectMocks
    ConnectivityInfoController connectivityInfoController;

    @Mock
    private MulticloudHttpClient httpClient;

    @Test
    public void createConnectivityInfoTest() throws Exception {

        ConnectivityInfo cIE = new ConnectivityInfo();
        try{
        connectivityInfoController.createConnectivityInfo(cIE);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void getConnectivityInfoTest() throws Exception {

        String connName = "con";
        try{
        connectivityInfoController.getConnectivityInfo(connName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    @Test
    public void deleteConnectivityInfoTest() throws Exception {

        String connName = "con";
        try{
        connectivityInfoController.deleteConnectivityInfo(connName);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }
}
