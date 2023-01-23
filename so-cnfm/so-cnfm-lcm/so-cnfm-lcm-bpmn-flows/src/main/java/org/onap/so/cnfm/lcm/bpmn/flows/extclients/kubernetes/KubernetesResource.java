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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes;

import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class KubernetesResource implements Serializable {

    private static final long serialVersionUID = -4342437130757578686L;

    private String id;
    private String name;
    private String group;
    private String version;
    private String kind;
    private String namespace;
    private String selflink;
    private String resourceVersion;
    private List<String> labels;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public KubernetesResource id(final String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public KubernetesResource name(final String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public KubernetesResource group(final String group) {
        this.group = group;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public KubernetesResource version(final String version) {
        this.version = version;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    public KubernetesResource kind(final String kind) {
        this.kind = kind;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public KubernetesResource namespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(final String selflink) {
        this.selflink = selflink;
    }

    public KubernetesResource selflink(final String selflink) {
        this.selflink = selflink;
        return this;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(final String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public KubernetesResource resourceVersion(final String resourceVersion) {
        this.resourceVersion = resourceVersion;
        return this;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(final List<String> labels) {
        this.labels = labels;
    }

    public KubernetesResource labels(final List<String> labels) {
        this.labels = labels;
        return this;
    }

    public KubernetesResource label(final String label) {
        if (this.labels == null) {
            this.labels = new ArrayList<>();
        }

        this.labels.add(label);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, group, version, kind, namespace, selflink, resourceVersion, labels);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof KubernetesResource) {
            final KubernetesResource other = (KubernetesResource) obj;
            return Objects.equals(id, other.id) && Objects.equals(name, other.name)
                    && Objects.equals(group, other.group) && Objects.equals(version, other.version)
                    && Objects.equals(kind, other.kind) && Objects.equals(namespace, other.namespace)
                    && Objects.equals(selflink, other.selflink)
                    && Objects.equals(resourceVersion, other.resourceVersion) && Objects.equals(labels, other.labels);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class KubernetesResource {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    selflink: ").append(toIndentedString(selflink)).append("\n");
        sb.append("    resourceVersion: ").append(toIndentedString(resourceVersion)).append("\n");
        sb.append("    labels: ").append(toIndentedString(labels)).append("\n");

        sb.append("}");
        return sb.toString();
    }

}
