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

import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAISingleTransactionClient;
import org.onap.aaiclient.client.aai.AAITransactionalClient;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIBaseResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.GraphInventoryResourcesClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.K8sResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author m.krasowski
 *
 */
public class AaiRepository implements IAaiRepository {
	private final static Logger logger = LoggerFactory.getLogger(IAaiRepository.class);
	private final static IAaiRepository instance = new AaiRepository();
	private final static Gson gson = new Gson();

	private final AAIResourcesClient aaiClient;

	public static IAaiRepository instance() {
		return instance;
	}

	private AaiRepository() {
		aaiClient = new AAIResourcesClient();
	}

	@Override
	public void update(K8sResource resource, AaiRequest aaiRequest) {
		logger.info("updating AAI with resource {} and request {}", resource, aaiRequest);
		AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
				.cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion()).tenant(aaiRequest.getTenantId())
				.build());
		String payload = gson.toJson(resource);
		getAaiClient().create(aaiUri, payload);

		String vnfId = aaiRequest.getVnfId();
		String vfModuleId = aaiRequest.getVfModuleId();
		if (vnfId != null && vfModuleId != null) {
			aaiUri = AAIUriFactory
					.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId).build());
			getAaiClient().create(aaiUri, payload);
		}
	}

	@Override
	public void delete(AaiRequest aaiRequest) {
		logger.info("deleting from AAI resource {}", aaiRequest);
		String vnfId = aaiRequest.getVnfId();
		String vfModuleId = aaiRequest.getVfModuleId();
		if (vnfId != null && vfModuleId != null) {
			AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network()
					.genericVnf(aaiRequest.getVnfId()).vfModule(aaiRequest.getVfModuleId()).build());
			getAaiClient().delete(aaiUri);
		}
		AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
				.cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion()).tenant(aaiRequest.getTenantId())
				.build());
		getAaiClient().delete(aaiUri);
	}

	private GraphInventoryResourcesClient<AAIResourcesClient, AAIBaseResourceUri<?, ?>, AAIResourceUri, AAIPluralResourceUri, AAIEdgeLabel, AAIResultWrapper, AAITransactionalClient, AAISingleTransactionClient> getAaiClient() {
		return aaiClient;
	}
}
