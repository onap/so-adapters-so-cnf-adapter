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
package org.onap.so.cnfm.lcm.lifecycle;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import org.onap.so.cnfm.lcm.SoCnfmAsLcmManagerUrlProvider;
import org.onap.so.cnfm.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.cnfm.lcm.model.AsInstanceLinksSelf;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc.CancelModeEnum;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc.OperationEnum;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc.OperationStateEnum;
import org.onap.so.cnfm.lcm.model.AsLcmOpOccLinks;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class AsLcmOperationOccurrenceManager {
    private static final Logger logger = getLogger(AsLcmOperationOccurrenceManager.class);
    private final DatabaseServiceProvider databaseServiceProvider;
    private final SoCnfmAsLcmManagerUrlProvider cnfmAsLcmManagerUrlProvider;

    @Autowired
    public AsLcmOperationOccurrenceManager(final DatabaseServiceProvider databaseServiceProvider,
            final SoCnfmAsLcmManagerUrlProvider cnfmAsLcmManagerUrlProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
        this.cnfmAsLcmManagerUrlProvider = cnfmAsLcmManagerUrlProvider;
    }

    public Optional<AsLcmOpOcc> getAsLcmOperationOccurrence(final String asLcmOpOccId) {
        logger.info("Getting AS LCM Operation Occurrence Operation for id: {}", asLcmOpOccId);

        final Optional<org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc> optionalDatabaseEntry =
                databaseServiceProvider.getAsLcmOpOcc(asLcmOpOccId);

        if (optionalDatabaseEntry.isEmpty()) {
            logger.info("No AS LCM Operation Occurrence found for id: {}", asLcmOpOccId);
            return Optional.empty();
        }
        logger.info("Found AS LCM Operation Occurrence for id: {}", asLcmOpOccId);
        final org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc asLcmOpOccDatabaseEntry = optionalDatabaseEntry.get();
        final AsLcmOpOcc asLcmOpOcc = convertToAsLcmOpOccsAsLcmOpOcc(asLcmOpOccDatabaseEntry);
        return Optional.of(asLcmOpOcc);
    }

    private AsLcmOpOcc convertToAsLcmOpOccsAsLcmOpOcc(
            final org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc databaseEntry) {

        final AsLcmOpOcc asLcmOpOcc = new AsLcmOpOcc().id(databaseEntry.getId())
                .stateEnteredTime(databaseEntry.getStateEnteredTime()).startTime(databaseEntry.getStartTime())
                .isAutomaticInvocation(databaseEntry.getIsAutoInvocation())
                .isCancelPending(databaseEntry.getIsCancelPending());

        if (databaseEntry.getAsInst() != null) {
            asLcmOpOcc.setAsInstanceId(databaseEntry.getAsInst().getAsInstId());
        }

        if (databaseEntry.getOperationState() != null) {
            asLcmOpOcc.setOperationState(OperationStateEnum.fromValue(databaseEntry.getOperationState().toString()));
        }

        if (databaseEntry.getOperation() != null) {
            asLcmOpOcc.setOperation(OperationEnum.fromValue(databaseEntry.getOperation().toString()));
        }

        if (databaseEntry.getOperationParams() != null) {
            asLcmOpOcc.setOperationParams(databaseEntry.getOperationParams());
        }

        if (databaseEntry.getCancelMode() != null) {
            asLcmOpOcc.setCancelMode(CancelModeEnum.fromValue(databaseEntry.getCancelMode().toString()));
        }

        asLcmOpOcc.setLinks(generateLinks(databaseEntry));

        logger.info("Database AsLcmOpOcc converted to API AsLcmOpOcc successfully... {}", asLcmOpOcc);

        return asLcmOpOcc;
    }

    private AsLcmOpOccLinks generateLinks(final org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc databaseEntry) {
        logger.info("Generating links...");
        final String asLcmOpOccId = databaseEntry.getId();

        final AsInstanceLinksSelf asLcmOpOccLinksSelf =
                new AsInstanceLinksSelf().href(cnfmAsLcmManagerUrlProvider.getAsLcmOpOccUri(asLcmOpOccId).toString());

        final AsLcmOpOccLinks links = new AsLcmOpOccLinks().self(asLcmOpOccLinksSelf);

        if (databaseEntry.getAsInst() != null) {
            final String asInstId = databaseEntry.getAsInst().getAsInstId();
            final AsInstanceLinksSelf asInstanceLinksSelf = new AsInstanceLinksSelf()
                    .href(cnfmAsLcmManagerUrlProvider.getCreatedAsResourceUri(asInstId).toString());
            links.setAsInstance(asInstanceLinksSelf);
        }

        return links;

    }

}
