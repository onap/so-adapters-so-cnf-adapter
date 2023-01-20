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
package org.onap.so.cnfm.lcm.database.repository;

import java.util.Optional;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.OperationStateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface AsLcmOpOccRepository extends JpaRepository<AsLcmOpOcc, String> {

    Optional<AsLcmOpOcc> findById(final String id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AsLcmOpOcc SET operationState = (:operationState) WHERE id = (:id)")
    int updateAsLcmOpOccOperationState(@Param("id") final String id,
            @Param("operationState") final OperationStateEnum operationState);
}
