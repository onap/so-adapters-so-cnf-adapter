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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.util.List;

public class KubernetesResource {
    private String id;
    private String name;
    private String group;
    private String version;
    private String kind;
    private String namespace;
    private List<String> labels;
    private String selflink;
    private String dataOwner;
    private String dataSource;
    private String dataSourceVersion;
    private String resourceVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getSelflink() { return selflink; }

    public void setSelflink(String selflink) { this.selflink = selflink; }

    public String getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSourceVersion() {
        return dataSourceVersion;
    }

    public void setDataSourceVersion(String dataSourceVersion) {
        this.dataSourceVersion = dataSourceVersion;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public boolean compare(KubernetesResource reference) {
        boolean result = reference != null &&
                Objects.equal(id, reference.id) &&
                Objects.equal(name, reference.name) &&
                Objects.equal(version, reference.version) &&
                Objects.equal(kind, reference.kind) &&
                Objects.equal(group, reference.group) &&
                Objects.equal(namespace, reference.namespace) &&
                Objects.equal(dataOwner, reference.dataOwner) &&
                Objects.equal(dataSource, reference.dataSource) &&
                Objects.equal(dataSourceVersion, reference.dataSourceVersion) &&
                Objects.equal(labels, reference.labels);
        return result;
    }
}