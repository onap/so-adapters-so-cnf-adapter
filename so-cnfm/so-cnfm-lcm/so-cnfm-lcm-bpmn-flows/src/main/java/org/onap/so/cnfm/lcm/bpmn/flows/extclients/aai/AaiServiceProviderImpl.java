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

package org.onap.so.cnfm.lcm.bpmn.flows.extclients.aai;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.K8SResource;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.beans.nsmf.OrchestrationStatusEnum;
import org.onap.so.cnfm.lcm.bpmn.flows.GsonProvider;
import org.onap.so.cnfm.lcm.bpmn.flows.extclients.kubernetes.KubernetesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Service
public class AaiServiceProviderImpl implements AaiServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(AaiServiceProviderImpl.class);
    private final AaiClientProvider aaiClientProvider;
    private final Gson gson;

    @Autowired
    public AaiServiceProviderImpl(final AaiClientProvider aaiClientProvider, final GsonProvider gsonProvider) {
        this.aaiClientProvider = aaiClientProvider;
        this.gson = gsonProvider.getGson();
    }

    @Override
    public void createGenericVnfAndConnectServiceInstance(final String serviceInstanceId, final String vnfId,
            final GenericVnf genericVnf) {
        logger.info("Creating GenericVnf in AAI: {}", genericVnf);
        final AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        final AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        aaiClientProvider.getAaiClient().createIfNotExists(genericVnfURI, Optional.of(genericVnf))
                .connect(genericVnfURI, serviceInstanceURI);

    }

    @Override
    public void connectGenericVnfToTenant(final String vnfId, final String cloudOwner, final String cloudRegion,
            final String tenantId) {
        logger.info("Connecting GenericVnf {} to {}/{}/{} in AAI", vnfId, cloudOwner, cloudRegion, tenantId);
        final AAIResourceUri tenantURI = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegion).tenant(tenantId));
        final AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        aaiClientProvider.getAaiClient().connect(tenantURI, genericVnfURI);
    }

    @Override
    public Optional<GenericVnf> getGenericVnf(final String vnfId) {
        return aaiClientProvider.getAaiClient().get(GenericVnf.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)));
    }

    @Override
    public void deleteGenericVnf(final String vnfId) {
        logger.info("Deleting GenericVnf with id: {} from AAI.", vnfId);
        final AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        aaiClientProvider.getAaiClient().delete(aaiResourceUri);
    }

    @Override
    public void updateGenericVnf(final String vnfId, final GenericVnf vnf) {
        logger.info("Updating GenericVnf of id: {} in AAI.", vnfId);
        final AAIResourceUri aaiResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        aaiClientProvider.getAaiClient().update(aaiResourceUri, vnf);
    }

    @Override
    public void createVfModule(final String vnfId, final String vfModuleId, final VfModule vfModule) {
        logger.info("Creating VfModule in AAI: {}", vfModule);

        final AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        aaiClientProvider.getAaiClient().createIfNotExists(vfModuleURI, Optional.of(vfModule));

    }

    @Override
    public void createK8sResource(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId, final K8SResource k8sResource) {
        logger.info("Creating K8SResource in AAI: {} using {}/{}/{}/{}", k8sResource, k8sResourceId, cloudOwner,
                cloudRegion, tenantId);

        final AAIResourceUri k8sResourceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwner, cloudRegion).tenant(tenantId).k8sResource(k8sResourceId));

        final String payload = gson.toJson(k8sResource);
        logger.debug("Creating K8sResource in A&AI: {}", payload);

        aaiClientProvider.getAaiClient().createIfNotExists(k8sResourceURI, Optional.of(payload));

    }

    @Override
    public void connectK8sResourceToVfModule(final String k8sResourceId, final String cloudOwner,
            final String cloudRegion, final String tenantId, final String vnfId, final String vfModuleId) {
        logger.info("Connecting K8sResource {}/{}/{}/{} to VF Moudle {}/{} in AAI", cloudOwner, cloudRegion, tenantId,
                k8sResourceId, vnfId, vfModuleId);

        final AAIResourceUri k8sResourceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwner, cloudRegion).tenant(tenantId).k8sResource(k8sResourceId));

        final AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        aaiClientProvider.getAaiClient().connect(k8sResourceURI, vfModuleURI);

    }

    @Override
    public void connectK8sResourceToGenericVnf(final String k8sResourceId, final String cloudOwner,
            final String cloudRegion, final String tenantId, final String vnfId) {
        logger.info("Connecting K8sResource {}/{}/{}/{} to Generic Vnf {} in AAI", cloudOwner, cloudRegion, tenantId,
                k8sResourceId, vnfId);
        final AAIResourceUri k8sResourceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwner, cloudRegion).tenant(tenantId).k8sResource(k8sResourceId));
        final AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        aaiClientProvider.getAaiClient().connect(k8sResourceURI, genericVnfURI);

    }

    @Override
    public List<KubernetesResource> getK8sResources(final String vnfId, final String vfModuleId) {
        logger.info("Getting K8S resources related to VfModule: {} from AAI", vfModuleId);
        final AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        final AAIResultWrapper vfModuleInstance = aaiClientProvider.getAaiClient().get(vfModuleURI);
        if (vfModuleInstance.hasRelationshipsTo(Types.K8S_RESOURCE)
                && vfModuleInstance.getRelationships().isPresent()) {
            logger.debug("VfModule has relations of type K8SResource");
            return vfModuleInstance.getRelationships().get().getByType(Types.K8S_RESOURCE).stream()
                    .map(relation -> relation.asBean(KubernetesResource.class)).filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
        }
        logger.info("No K8S resources found for VfModule :{}", vfModuleId);
        return Collections.emptyList();
    }

    @Override
    public void deleteK8SResource(final String k8sResourceId, final String cloudOwner, final String cloudRegion,
            final String tenantId) {
        logger.info("Deleting K8sResource {} from AAI", k8sResourceId);

        final AAIResourceUri k8sResourceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwner, cloudRegion).tenant(tenantId).k8sResource(k8sResourceId));
        aaiClientProvider.getAaiClient().deleteIfExists(k8sResourceURI);

        logger.info("K8S resource removed from AAI using URI: {}", k8sResourceURI);
    }

    @Override
    public void deleteVfModule(final String vnfId, final String vfModuleId) {
        logger.info("Deleting VfModule {} from AAI", vfModuleId);

        final AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        aaiClientProvider.getAaiClient().deleteIfExists(vfModuleURI);
        logger.info("VfModule deleted from AAI using URI: {}", vfModuleId);
    }

    @Override
    public boolean updateGenericVnfStatus(final String vnfId, final OrchestrationStatusEnum status) {
        logger.info("Updating GenericVnf status to deactivated for vnfID: {}", vnfId);
        final Optional<GenericVnf> optionalVnf = getGenericVnf(vnfId);
        if (optionalVnf.isPresent()) {
            final GenericVnf vnf = optionalVnf.get();
            vnf.setOrchestrationStatus(status.getValue());
            updateGenericVnf(vnfId, vnf);
            return true;
        }
        return false;
    }

}
