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

package org.onap.so.adapters.cnf.util;

import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.K8sResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author m.krasowski
 *
 */
public class AaiRepositoryDummy implements IAaiRepository {
	private static final Logger logger = LoggerFactory.getLogger(IAaiRepository.class);
	private static final IAaiRepository instance = new AaiRepositoryDummy();
	
	public static IAaiRepository instance() {
		return instance;
	}
	
	private AaiRepositoryDummy() {
		
	}

	@Override
	public void update(K8sResource resource, AaiRequest request) {
		logger.info("aai synchronization disabled - mocking update AAI with resource {} and request {}", resource, request);
	}

	@Override
	public void delete(AaiRequest aaiRequest) {
		logger.info("aai synchronization disabled - mocking delete from AAI resource {}", aaiRequest);
	}
}