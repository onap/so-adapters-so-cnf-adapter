/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Co. Ltd. All rights reserved.
 * Modifications Copyright (C) 2021 Orange.
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

package org.onap.so.adapters.cnf.util;

import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.KubernetesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.Thread.sleep;

public interface IAaiRepository {
    static IAaiRepository instance(boolean enabled) {
        if (enabled)
            return AaiRepository.instance();
        else
            return AaiRepositoryDummy.instance();
    }

    void update(KubernetesResource resource, AaiRequest request);

    void delete(AaiRequest request, List<KubernetesResource> excludedList);

    void commit(boolean dryRun) throws RuntimeException;

    static class AaiRepositoryDummy implements IAaiRepository {
        private static final Logger logger = LoggerFactory.getLogger(IAaiRepository.class);
        private static final IAaiRepository instance = new AaiRepositoryDummy();

        private static final Long SLEEP_TIME = 5000l;

        public static IAaiRepository instance() {
            return instance;
        }

        private AaiRepositoryDummy() {

        }

        @Override
        public void update(KubernetesResource resource, AaiRequest request) {
            logger.info("aai synchronization disabled - mocking update AAI with resource {} and request {}", resource, request);
            try {
                sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.debug("aai synchronization disabled [update] - sleep failed");
            }
        }

        @Override
        public void delete(AaiRequest aaiRequest, List<KubernetesResource> excludedList) {
            logger.info("aai synchronization disabled - mocking delete from AAI resource {}", aaiRequest);
            try {
                sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.debug("aai synchronization disabled [delete] - sleep failed");
            }

        }

        @Override
        public void commit(boolean dryrun) {
            logger.info("aai synchronization disabled - commiting");
        }
    }
}
