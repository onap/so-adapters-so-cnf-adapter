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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AaiResponseParserTest {

    private final static String INSTANCE_ID = "k8splugin.io/rb-instance-id";
    private final static String INSTANCE_ID_VALUE = "rb-instance-id_value";

    @InjectMocks
    private AaiResponseParser aaiResponseParser;

    @Mock
    private AaiIdGeneratorService aaiIdGeneratorService;


    @Test
    public void shouldParseAaiResponse() {
        // given
        String id = "id";
        String name = "name";
        String group = "group";
        String version = "version";
        String kind = "kind";
        String namespace = "namespace";
        Map<String, String> labelsMap = new HashMap<>();
        labelsMap.put("key", "value");
        labelsMap.put(INSTANCE_ID, INSTANCE_ID_VALUE);
        K8sRbInstanceResourceStatus status = mock(K8sRbInstanceResourceStatus.class);
        AaiRequest aaiRequest = mock(AaiRequest.class);
        K8sRbInstanceGvk gvk = mock(K8sRbInstanceGvk.class);
        K8sStatusMetadata metadata = mock(K8sStatusMetadata.class);
        K8sStatus k8sStatus = mock(K8sStatus.class);

        // when
        when(status.getGvk()).thenReturn(gvk);
        when(status.getStatus()).thenReturn(k8sStatus);
        when(k8sStatus.getK8sStatusMetadata()).thenReturn(metadata);
        when(aaiIdGeneratorService.generateId(status, aaiRequest)).thenReturn(id);
        when(status.getName()).thenReturn(name);
        when(gvk.getGroup()).thenReturn(group);
        when(gvk.getVersion()).thenReturn(version);
        when(gvk.getKind()).thenReturn(kind);
        when(metadata.getNamespace()).thenReturn(namespace);
        when(aaiRequest.getInstanceId()).thenReturn(id);

        when(metadata.getLabels()).thenReturn(labelsMap);

        // then
        KubernetesResource actual = aaiResponseParser.parse(status, aaiRequest);

        Assert.assertNotNull(actual);
        assertEquals(id, actual.getId());
        assertEquals(name, actual.getName());
        assertEquals(group, actual.getGroup());
        assertEquals(version, actual.getVersion());
        assertEquals(kind, actual.getKind());
        assertEquals(namespace, actual.getNamespace());
        assertEquals(4, actual.getLabels().size());
        assertEquals(INSTANCE_ID, actual.getLabels().get(0));
        assertEquals(INSTANCE_ID_VALUE, actual.getLabels().get(1));
        assertEquals("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/id/query?ApiVersion=version&Kind=kind&Name=name&Namespace=namespace", actual.getSelflink());
    }
}
