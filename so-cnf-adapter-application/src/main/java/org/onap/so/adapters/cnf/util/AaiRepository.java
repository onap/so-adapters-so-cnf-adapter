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

import com.google.gson.Gson;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAITransactionalClient;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.K8sResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Optional;


public class AaiRepository implements IAaiRepository {
	private final static Logger logger = LoggerFactory.getLogger(IAaiRepository.class);
	private final static Gson gson = new Gson();

	private final AAIResourcesClient aaiClient;
	private final AAITransactionalClient transaction;

	public static IAaiRepository instance() {
		return new AaiRepository();
	}

	private AaiRepository() {
		aaiClient = new AAIResourcesClient(AAIVersion.LATEST);
		transaction = aaiClient.beginTransaction();
	}

	@Override
	public void commit(boolean dryrun) {
		try {
			transaction.execute(dryrun);
		} catch (BulkProcessFailed bulkProcessFailed) {
			throw new RuntimeException("Failed to exectute transaction", bulkProcessFailed);
		}
	}

	@Override
	public void update(K8sResource resource, AaiRequest aaiRequest) {
		logger.info("updating AAI with resource {} and request {}", resource, aaiRequest);

		org.onap.aaiclient.client.generated.fluentbuilders.K8sResource k8sResource = AAIFluentTypeBuilder.cloudInfrastructure()
				.cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
				.tenant(aaiRequest.getTenantId())
				.k8sResource(resource.getId());
		
		AAIResourceUri k8sResourceUri = 				
				AAIUriFactory.createResourceUri(k8sResource.build(), aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion(), aaiRequest.getTenantId(), resource.getId());
		String payload = gson.toJson(resource);
		transaction.createIfNotExists(k8sResourceUri, Optional.of(payload));

		logger.info("URI: " + k8sResourceUri);

		createRelationToGenericVnf(k8sResourceUri, aaiRequest.getGenericVnfId());
		createRealtionToVfModule(k8sResourceUri, aaiRequest.getVfModuleId());
	}

	private void createRelationToGenericVnf(AAIResourceUri k8sResourceUri, String vnfId) {
		if(!StringUtils.hasText(vnfId)) {
			logger.info("AAI udpate: skipping creating relation to vnf with empty id");
			return;
		}

	}

	private void createRealtionToVfModule(AAIResourceUri k8sResourceUri, String vfModuleId) {
		if(!StringUtils.hasText(vfModuleId)) {
			logger.info("AAI udpate: skipping creating relation to vf module with empty id");
			return;
		}
	}

	@Override
	public void delete(AaiRequest aaiRequest) {
		logger.info("deleting from AAI resource {}", aaiRequest);
		String vnfId = aaiRequest.getGenericVnfId();
		String vfModuleId = aaiRequest.getVfModuleId();
		if (vnfId != null && vfModuleId != null) {
			AAIResourceUri vfModuleUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
			AAIResultWrapper vfModule = getAaiClient().get(vfModuleUri);

			if (vfModule.hasRelationshipsTo(Types.VNFC)) {
				getAaiClient().delete(vfModuleUri);
			}
		}
		AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
				.cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion()).tenant(aaiRequest.getTenantId())
				.build());
		getAaiClient().delete(aaiUri);
	}

	private AAIResourcesClient getAaiClient() {
		return aaiClient;
	}
}
