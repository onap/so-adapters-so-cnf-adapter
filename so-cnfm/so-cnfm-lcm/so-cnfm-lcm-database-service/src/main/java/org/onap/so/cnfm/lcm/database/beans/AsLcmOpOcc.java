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
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Entity
@Table(name = "AS_LCM_OP_OCCS")
public class AsLcmOpOcc {

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION_STATE", nullable = false)
    private OperationStateEnum operationState;

    @Column(name = "STATE_ENTERED_TIME")
    private LocalDateTime stateEnteredTime;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AS_INST_ID", nullable = false)
    private AsInst asInst;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION", nullable = false)
    private AsLcmOpType operation;

    @Column(name = "IS_AUTO_INVOCATION", nullable = false)
    private boolean isAutoInvocation;

    @Column(name = "OPERATION_PARAMS", columnDefinition = "LONGTEXT", nullable = false)
    private String operationParams;

    @Column(name = "IS_CANCEL_PENDING", nullable = false)
    private boolean isCancelPending;

    @Enumerated(EnumType.STRING)
    @Column(name = "CANCEL_MODE")
    private CancelModeType cancelMode;

    public AsLcmOpOcc() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public AsLcmOpOcc id(final String id) {
        this.id = id;
        return this;
    }

    public OperationStateEnum getOperationState() {
        return operationState;
    }

    public void setOperationState(final OperationStateEnum operationState) {
        this.operationState = operationState;
    }

    public AsLcmOpOcc operationState(final OperationStateEnum operationState) {
        this.operationState = operationState;
        return this;
    }

    public LocalDateTime getStateEnteredTime() {
        return stateEnteredTime;
    }

    public void setStateEnteredTime(final LocalDateTime stateEnteredTime) {
        this.stateEnteredTime = stateEnteredTime;
    }

    public AsLcmOpOcc stateEnteredTime(final LocalDateTime stateEnteredTime) {
        this.stateEnteredTime = stateEnteredTime;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public AsLcmOpOcc startTime(final LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public AsInst getAsInst() {
        return asInst;
    }

    public void setAsInst(final AsInst asInst) {
        this.asInst = asInst;
    }

    public AsLcmOpOcc asInst(final AsInst asInst) {
        this.asInst = asInst;
        return this;
    }

    public AsLcmOpType getOperation() {
        return operation;
    }

    public void setOperation(final AsLcmOpType operation) {
        this.operation = operation;
    }

    public AsLcmOpOcc operation(final AsLcmOpType operation) {
        this.operation = operation;
        return this;
    }

    public boolean getIsAutoInvocation() {
        return isAutoInvocation;
    }

    public void setIsAutoInvocation(final boolean isAutoInvocation) {
        this.isAutoInvocation = isAutoInvocation;
    }

    public AsLcmOpOcc isAutoInvocation(final boolean isAutoInvocation) {
        this.isAutoInvocation = isAutoInvocation;
        return this;
    }

    public CancelModeType getCancelMode() {
        return cancelMode;
    }

    public void setCancelMode(final CancelModeType cancelMode) {
        this.cancelMode = cancelMode;
    }

    public AsLcmOpOcc cancelMode(final CancelModeType cancelMode) {
        this.cancelMode = cancelMode;
        return this;
    }

    public String getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(final String operationParams) {
        this.operationParams = operationParams;
    }

    public AsLcmOpOcc operationParams(final String operationParams) {
        this.operationParams = operationParams;
        return this;
    }

    public boolean getIsCancelPending() {
        return isCancelPending;
    }

    public void setIsCancelPending(final boolean isCancelPending) {
        this.isCancelPending = isCancelPending;
    }

    public AsLcmOpOcc isCancelPending(final boolean isCancelPending) {
        this.isCancelPending = isCancelPending;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final AsLcmOpOcc that = (AsLcmOpOcc) obj;
        return Objects.equals(id, that.id) && Objects.equals(operationState, that.operationState)
                && Objects.equals(stateEnteredTime, that.stateEnteredTime) && Objects.equals(startTime, that.startTime)
                && (asInst == null ? that.asInst == null : that.asInst != null && Objects.equals(asInst, that.asInst))
                && Objects.equals(operation, that.operation) && Objects.equals(isAutoInvocation, that.isAutoInvocation)
                && Objects.equals(operationParams, that.operationParams)
                && Objects.equals(isCancelPending, that.isCancelPending) && Objects.equals(cancelMode, that.cancelMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, operationState, stateEnteredTime, startTime, asInst != null ? asInst.getAsInstId() : 0,
                operation, isAutoInvocation, operationParams, isCancelPending, cancelMode);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NsLcmOpOcc {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    operationState: ").append(toIndentedString(operationState)).append("\n");
        sb.append("    stateEnteredTime: ").append(toIndentedString(stateEnteredTime)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    asInstId: ").append(asInst != null ? toIndentedString(asInst.getAsInstId()) : null).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    isAutoInvocation: ").append(toIndentedString(isAutoInvocation)).append("\n");
        sb.append("    operationParams: ").append(toIndentedString(operationParams)).append("\n");
        sb.append("    isCancelPending: ").append(toIndentedString(isCancelPending)).append("\n");
        sb.append("    cancelMode: ").append(toIndentedString(cancelMode)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
