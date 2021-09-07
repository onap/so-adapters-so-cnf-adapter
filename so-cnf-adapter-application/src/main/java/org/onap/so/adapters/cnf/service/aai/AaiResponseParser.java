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

package org.onap.so.adapters.cnf.service.aai;

import org.apache.http.client.utils.URIBuilder;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AaiResponseParser {

    private final static String INSTANCE_ID = "k8splugin.io/rb-instance-id";

    private final AaiIdGeneratorService aaiIdGeneratorService;

    public AaiResponseParser(AaiIdGeneratorService aaiIdGeneratorService) {
        this.aaiIdGeneratorService = aaiIdGeneratorService;
    }

    K8sResource parse(K8sRbInstanceResourceStatus status, AaiRequest aaiRequest) {
        K8sResource result = new K8sResource();
        K8sRbInstanceGvk gvk = status.getGvk();
        K8sStatus k8sStatus = status.getStatus();
        K8sStatusMetadata metadata = k8sStatus.getK8sStatusMetadata();
        String id = aaiIdGeneratorService.generateId(status, aaiRequest);
        result.setId(id);
        result.setName(status.getName());
        result.setGroup(gvk.getGroup());
        result.setVersion(gvk.getVersion());
        result.setKind(gvk.getKind());
        result.setNamespace(metadata.getNamespace());
        List<String> labels = parseLabels(metadata.getLabels());
        result.setLabels(labels);
        URIBuilder uriBuilder = new URIBuilder();
        String selfLink;
        try {
            selfLink = uriBuilder
                    .setScheme("http")
                    .setHost("so-cnf-adapter")
                    .setPort(8090)
                    .setPath("/api/cnf-adapter/v1/instance/" + aaiRequest.getInstanceId() + "/query")
                    .setParameter("ApiVersion", gvk.getVersion())
                    .setParameter("Kind", gvk.getKind())
                    .setParameter("Name", status.getName())
                    .setParameter("Namespace", metadata.getNamespace())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        result.setK8sResourceSelfLink(selfLink);
        return result;
    }

    private List<String> parseLabels(Map<String, String> labels) {
        List<String> result = new ArrayList<>();
        labels.entrySet().stream()
                .filter(i -> i.getKey().equals(INSTANCE_ID))
                .findFirst()
                .ifPresent(i -> addInstanceIdFist(i, result));
        labels.entrySet().stream()
                .filter(i -> !i.getKey().equals(INSTANCE_ID))
                .forEach(i -> {
                    result.add(i.getKey());
                    result.add(i.getValue());
                });
        return result;
    }

    private void addInstanceIdFist(Map.Entry<String, String> instanceId, List<String> result) {
        result.add(instanceId.getKey());
        result.add(instanceId.getValue());
    }
}
