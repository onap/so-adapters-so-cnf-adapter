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

package org.onap.so.adapters.cnf.exceptions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.GroupVersionKind;
import org.onap.so.adapters.cnf.model.InstanceStatusResponse;
import org.onap.so.adapters.cnf.model.MulticloudInstanceRequest;
import org.onap.so.adapters.cnf.model.PodStatus;
import org.onap.so.adapters.cnf.model.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
public class ApplicationExceptionTest {

    @Test
    public void Test() throws Exception {

         MulticloudInstanceRequest request = null;
         List<PodStatus> podStatuses = null;
         List<?> servicesStatuses = null;

         String[] ipAddresses = new String[0];
         
         GroupVersionKind gVK = null;
         
        InstanceStatusResponse instanceStatusResponse=new InstanceStatusResponse("Err");

        instanceStatusResponse.setPodStatuses(podStatuses);
        instanceStatusResponse.setReady(true);
        instanceStatusResponse.setRequest(request);
        instanceStatusResponse.setResourceCount("s");
        instanceStatusResponse.setServicesStatuses(servicesStatuses);

        instanceStatusResponse.getPodStatuses();
        instanceStatusResponse.getRequest();
        instanceStatusResponse.getResourceCount();
        instanceStatusResponse.getServicesStatuses();
        instanceStatusResponse.isReady();

        PodStatus podStatus = new PodStatus();

        podStatus.setIpAddresses(ipAddresses);
        podStatus.setName("name");
        podStatus.setNameSpace("namespace");
        podStatus.setReady(true);
        podStatus.setStatus("ok");

        podStatus.getIpAddresses();
        podStatus.getName();
        podStatus.getNameSpace();
        podStatus.getStatus();
        podStatus.isReady();

        Resource resource = new Resource();
        
        resource.setGVK(gVK);
        resource.setName("name");
        
        resource.getGVK();
        resource.getName();

        ApplicationException applicationexception=new ApplicationException(1,"Error");

        applicationexception.setErrorCode(1);
        applicationexception.setErrorMsg("Error");

        applicationexception.getErrorCode();
        applicationexception.getErrorMsg();
        applicationexception.buildErrorResponse();

    }
}