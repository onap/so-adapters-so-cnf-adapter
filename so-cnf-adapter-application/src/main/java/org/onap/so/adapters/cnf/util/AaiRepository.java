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
import org.onap.aai.domain.yang.Relationship;
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
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.generated.fluentbuilders.CloudRegion;
import org.onap.aaiclient.client.generated.fluentbuilders.Tenant;
import org.onap.aaiclient.client.graphinventory.GraphInventoryResourcesClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.K8sResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;


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
		CloudRegion cloudRegion = AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion());
		Tenant tenant = cloudRegion.tenant(aaiRequest.getTenantId());
		// FIXME 
		org.onap.aaiclient.client.generated.fluentbuilders.K8sResource k8sResource = tenant.k8sResource(resource.getId());
		
		AAIResourceUri k8sResourceUri = 				
				AAIUriFactory.createResourceUri(k8sResource.build());
		String payload = gson.toJson(resource);
		getAaiClient().create(k8sResourceUri, payload);
		AAIResultWrapper newK8sResource = getAaiClient().get(k8sResourceUri);
		
		createRelationToVnf(k8sResourceUri, aaiRequest.getVnfId());
		createRealtionToVfModule(k8sResourceUri, aaiRequest.getVfModuleId());
		/*
		if (vnfId != null && vfModuleId != null) {
			AAIResultWrapper k8sResource = getAaiClient().get(aaiUri);
			AAIResourceUri relation = k8sResource.hasRelationshipsTo()
			
			
			AAIResourceUri vfModuleUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
			AAIResultWrapper vfModule = getAaiClient().get(vfModuleUri);

			if (vfModule.hasRelationshipsTo(Types.VNFC)) {
				List<AAIResourceUri> vnfcUris = vfModule.getRelationships().get().getRelatedUris(Types.VNFC);
				Optional<AAIResourceUri> foundVnfcURI = vnfcUris.stream()
						.filter(resourceUri -> resourceUri.getURIKeys().get("vnfc-name").startsWith(resource.getK8sResourceSelfLink()))
						.findFirst();
				Relationship relationship = new Relationship();
				relationship.setRelatedLink(foundVnfcURI.get().build().toString());
				getAaiClient().create(vfModuleUri, relationship);
			}
		}
		*/
	}

	private void createRelationToVnf(AAIResourceUri k8sResourceUri, String vnfId) {
		if(!StringUtils.hasText(vnfId)) {
			logger.info("AAI udpate: skipping creating relation to vnf with empty id");
			return;
		}
		// FIXME
		
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
		String vnfId = aaiRequest.getVnfId();
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

	private GraphInventoryResourcesClient<AAIResourcesClient, AAIBaseResourceUri<?, ?>, AAIResourceUri, AAIPluralResourceUri, AAIEdgeLabel, AAIResultWrapper, AAITransactionalClient, AAISingleTransactionClient> getAaiClient() {
		return aaiClient;
	}
}
