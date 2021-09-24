/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.model.healthcheck;

public class HealthCheckInstance {
    private final String instanceId;
    private final String healthCheckInstance;

    public HealthCheckInstance(String instanceId, String healthCheckInstance) {
        this.instanceId = instanceId;
        this.healthCheckInstance = healthCheckInstance;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHealthCheckInstance() {
        return healthCheckInstance;
    }

    @Override
    public String toString() {
        return "HealthCheckInstance{" +
                "instanceId='" + instanceId + '\'' +
                ", healthCheckInstance='" + healthCheckInstance + '\'' +
                '}';
    }
}
