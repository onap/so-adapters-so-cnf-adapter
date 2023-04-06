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
package org.onap.so.cnfm.lcm.bpmn.flows;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class CamundaVariableNameConstants {

    public static final String AS_DEPLOYMENT_ITEM_INST_ID_PARAM_NAME = "asDeploymentItemInstId";
    public static final String KUBE_KINDS_RESULT_PARAM_NAME = "kubeKindsResult";
    public static final String KUBE_CONFIG_FILE_PATH_PARAM_NAME = "kubeConfigFilePath";
    public static final String KUBE_KINDS_PARAM_NAME = "kubeKinds";
    public static final String KIND_PARAM_NAME = "kind";
    public static final String RELEASE_NAME_PARAM_NAME = "releaseName";
    public static final String NAMESPACE_PARAM_NAME = "namespace";
    public static final String JOB_ID_PARAM_NAME = "jobId";
    public static final String JOB_BUSINESS_KEY_PARAM_NAME = "jobBusinessKey";
    public static final String CREATE_AS_REQUEST_PARAM_NAME = "createAsRequest";

    public static final String AS_PACKAGE_MODEL_PARAM_NAME = "AsPackageModel";
    public static final String AS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME = "AsWorkflowProcessingException";
    public static final String CREATE_AS_RESPONSE_PARAM_NAME = "createAsResponse";

    public static final String INSTANTIATE_AS_REQUEST_PARAM_NAME = "instantiateAsRequest";
    public static final String AS_INSTANCE_ID_PARAM_NAME = "AsInstanceId";
    public static final String OCC_ID_PARAM_NAME = "occId";

    public static final String DEPLOYMENT_ITEM_INSTANTIATE_REQUESTS = "deploymentItemInstantiateRequests";
    public static final String DEPLOYMENT_ITEM_TERMINATE_REQUESTS = "deploymentItemTerminateRequests";

    public static final String TERMINATE_AS_REQUEST_PARAM_NAME = "terminateAsRequest";

    private CamundaVariableNameConstants() {}

}
