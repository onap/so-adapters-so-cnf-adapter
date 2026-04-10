/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class KubernetesResourceTest {

    private KubernetesResource build(String id, String name, String version, String kind,
            String group, String namespace, String dataOwner, String dataSource, String dataSourceVersion) {
        KubernetesResource r = new KubernetesResource();
        r.setId(id);
        r.setName(name);
        r.setVersion(version);
        r.setKind(kind);
        r.setGroup(group);
        r.setNamespace(namespace);
        r.setDataOwner(dataOwner);
        r.setDataSource(dataSource);
        r.setDataSourceVersion(dataSourceVersion);
        return r;
    }

    @Test
    public void compare_returnsFalse_whenReferenceIsNull() {
        KubernetesResource r = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        assertFalse(r.compare(null));
    }

    @Test
    public void compare_returnsTrue_whenAllFieldsMatch() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        assertTrue(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenIdDiffers() {
        KubernetesResource r1 = build("id1", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id2", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenNameDiffers() {
        KubernetesResource r1 = build("id", "name1", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name2", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenVersionDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v2", "Pod", "core", "default", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenKindDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Deployment", "core", "default", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenGroupDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "apps", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenNamespaceDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "ns1", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "ns2", "owner", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenDataOwnerDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner1", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "default", "owner2", "src", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenDataSourceDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src1", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src2", "srcV1");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsFalse_whenDataSourceVersionDiffers() {
        KubernetesResource r1 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV1");
        KubernetesResource r2 = build("id", "name", "v1", "Pod", "core", "default", "owner", "src", "srcV2");
        assertFalse(r1.compare(r2));
    }

    @Test
    public void compare_returnsTrue_whenAllFieldsAreNull() {
        KubernetesResource r1 = new KubernetesResource();
        KubernetesResource r2 = new KubernetesResource();
        assertTrue(r1.compare(r2));
    }

    @Test
    public void setLabels_sortsLabelsAlphabetically() {
        KubernetesResource r = new KubernetesResource();
        r.setLabels(Arrays.asList("c=3", "a=1", "b=2"));
        List<String> labels = r.getLabels();
        assertEquals(3, labels.size());
        assertEquals("a=1", labels.get(0));
        assertEquals("b=2", labels.get(1));
        assertEquals("c=3", labels.get(2));
    }

    @Test
    public void setLabels_withNull_keepsNull() {
        KubernetesResource r = new KubernetesResource();
        r.setLabels(null);
        assertNull(r.getLabels());
    }
}
