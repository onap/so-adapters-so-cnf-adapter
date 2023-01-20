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
package org.onap.so.cnfm.lcm.database.beans;

import static org.onap.so.cnfm.lcm.database.beans.utils.Utils.toIndentedString;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Entity
@Table(name = "JOB_STATUS")
public class JobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private JobStatusEnum status;

    @Column(name = "DESCRIPTION", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "UPDATED_TIME", nullable = false)
    private LocalDateTime updatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_ID", nullable = false)
    private Job job;

    public int getId() {
        return id;
    }

    public JobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(final JobStatusEnum status) {
        this.status = status;
    }

    public JobStatus status(final JobStatusEnum status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public JobStatus description(final String description) {
        this.description = description;
        return this;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public JobStatus updatedTime(final LocalDateTime addTime) {
        this.updatedTime = addTime;
        return this;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    public JobStatus job(final Job job) {
        this.job = job;
        return this;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, status, updatedTime, description, job != null ? job.getJobId() : 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        if (obj instanceof JobStatus) {
            final JobStatus other = (JobStatus) obj;
            return Objects.equals(id, other.id) && Objects.equals(status, other.status)
                    && Objects.equals(updatedTime, other.updatedTime) && Objects.equals(description, other.description)
                    && (job == null ? other.job == null
                            : other.job != null && Objects.equals(job.getJobId(), other.job.getJobId()));
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class JobStatus {\n");
        sb.append("    Id: ").append(toIndentedString(id)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    descp: ").append(toIndentedString(description)).append("\n");
        sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
        sb.append("    jobId: ").append(job != null ? toIndentedString(job.getJobId()) : "").append("\n");
        sb.append("}");
        return sb.toString();
    }

}
