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
public class Constants {

    public static final String TENANT_ID = "as-workflow-engine-tenant";
    public static final String AS_WORKFLOW_ENGINE = "AS-WORKFLOW-ENGINE";
    public static final String CREATE_AS_WORKFLOW_NAME = "CreateAs";
    public static final String INSTANTIATE_AS_WORKFLOW_NAME = "InstantiateAs";
    public static final String TERMINATE_AS_WORKFLOW_NAME = "TerminateAs";
    public static final String DELETE_AS_WORKFLOW_NAME = "DeleteAs";

    public static final String KIND_STATEFUL_SET = "StatefulSet";
    public static final String KIND_DAEMON_SET = "DaemonSet";
    public static final String KIND_REPLICA_SET = "ReplicaSet";
    public static final String KIND_DEPLOYMENT = "Deployment";
    public static final String KIND_SERVICE = "Service";
    public static final String KIND_POD = "Pod";
    public static final String KIND_JOB = "Job";

    private Constants() {}

}
