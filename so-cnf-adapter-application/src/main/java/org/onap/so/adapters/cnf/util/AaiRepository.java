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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAITransactionalClient;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.generated.fluentbuilders.K8sResource;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.adapters.cnf.exceptions.ApplicationException;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.KubernetesResource;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AaiRepository implements IAaiRepository {
    private final static Logger logger = LoggerFactory.getLogger(IAaiRepository.class);

    private final AAITransactionHelper aaiTransactionHelper;
    private final ObjectMapper objectMapper;

    public static IAaiRepository instance() {
        return new AaiRepository();
    }

    private AaiRepository() {
        aaiTransactionHelper = new AAITransactionHelper();
        this.objectMapper = new ObjectMapper();
    }

    private AAITransactionHelper getTransaction() {
        return aaiTransactionHelper;
    }

    @Override
    public void commit(boolean dryrun) throws BulkProcessFailed {
        aaiTransactionHelper.execute(dryrun);
    }

    @Override
    public void update(KubernetesResource resource, AaiRequest aaiRequest) {
        logger.info("updating AAI with resource {} and request {}", resource, aaiRequest);

        K8sResource k8sResource = AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                .tenant(aaiRequest.getTenantId())
                .k8sResource(resource.getId());

        AAIResourceUri k8sResourceUri =
                AAIUriFactory.createResourceUri(k8sResource.build(), aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion(), aaiRequest.getTenantId(), resource.getId());

        AAIResourcesClient aaiClient = aaiTransactionHelper.getAaiClient();
        var k8sResourceInstance = aaiClient.get(k8sResourceUri);
        boolean updateK8sResource = true;
        if (!k8sResourceInstance.isEmpty()) {
            try {
                KubernetesResource resourceReference = objectMapper.readValue(k8sResourceInstance.getJson(), KubernetesResource.class);
                updateK8sResource = !resourceReference.compare(resource);
                if (updateK8sResource)
                    resource.setResourceVersion(resourceReference.getResourceVersion());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if (updateK8sResource) {
            String payload;
            try {
                payload = objectMapper.writeValueAsString(resource);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            logger.debug("K8s resource URI: " + k8sResourceUri + " with payload [" + payload + "]");
            if (resource.getResourceVersion() != null)
                getTransaction().update(k8sResourceUri, payload);
            else
                getTransaction().create(k8sResourceUri, payload);
        }

        final String genericVnfId = aaiRequest.getGenericVnfId();
        final String vfModuleId = aaiRequest.getVfModuleId();

        if (genericVnfId == null || vfModuleId == null) {
            logger.debug("No genericVnfId or vfModuleId to create relations for payload [\" + payload + \"]\");");
            return;
        }

        var vfModuleUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId).vfModule(vfModuleId));
        var instance = aaiClient.get(vfModuleUri);

        if (instance.isEmpty())
            logger.error("Specified VfModule [" + vfModuleId + "] does not exist in AAI");
        else if (k8sResourceInstance.isEmpty() || !k8sResourceInstance.hasRelationshipsTo(Types.VF_MODULE)) {
            getTransaction().connect(k8sResourceUri, vfModuleUri);
        }

        var genericVnfUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId));
        instance = aaiClient.get(genericVnfUri);

        if (instance.isEmpty())
            logger.error("Specified GenericVnf [" + genericVnfId + "] does not exist in AAI");
        else if (k8sResourceInstance.isEmpty() || !k8sResourceInstance.hasRelationshipsTo(Types.GENERIC_VNF)) {
            getTransaction().connect(k8sResourceUri, genericVnfUri);
        }
    }

    @Override
    public void delete(AaiRequest aaiRequest, List<KubernetesResource> excludedList) {
        logger.info("deleting from AAI resource {}", aaiRequest);
        final String genericVnfId = aaiRequest.getGenericVnfId();
        final String vfModuleId = aaiRequest.getVfModuleId();
        final List<String> excludedIds = excludedList == null ? List.of() :
                excludedList.stream().map(KubernetesResource::getId).collect(Collectors.toList());

        if (genericVnfId == null || vfModuleId == null) {
            logger.debug("No genericVnfId or vfModuleId to delete k8s-resources");
            return;
        }

        var vfModuleUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.network().genericVnf(genericVnfId).vfModule(vfModuleId));

        AAIResourcesClient aaiClient = aaiTransactionHelper.getAaiClient();
        var instance = aaiClient.get(vfModuleUri);

        if (instance.hasRelationshipsTo(Types.K8S_RESOURCE) && instance.getRelationships().isPresent()) {
            List<KubernetesResource> resources = instance.getRelationships().get().getByType(Types.K8S_RESOURCE)
                    .stream()
                    .map(r -> r.asBean(KubernetesResource.class))
                    .filter(r -> r.isPresent() && !excludedIds.contains(r.get().getId()))
                    .map(Optional::get)
                    .collect(Collectors.toList());
            resources.stream().map(r -> {
                K8sResource k8sResource = AAIFluentTypeBuilder.cloudInfrastructure()
                        .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                        .tenant(aaiRequest.getTenantId())
                        .k8sResource(r.getId());
                return AAIUriFactory.createResourceUri(k8sResource.build(), aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion(), aaiRequest.getTenantId(), r.getId());
            }).filter(r -> {
                if (aaiClient.exists(r))
                    return true;
                else {
                    logger.warn("K8sResource " + r.toString() + "] does not exist in AAI. Skipping delete in AAI");
                    return false;
                }
            }).forEach(uri -> getTransaction().delete(uri));
        }
    }

    static class AAITransactionHelper {
        private final AAIResourcesClient aaiClient;
        private final AAITransactionStore createStore;
        private final AAITransactionStore updateStore;
        private final AAITransactionStore deleteStore;

        AAITransactionHelper() {
            this.aaiClient = new AAIResourcesClient(AAIVersion.LATEST);
            this.createStore = new AAITransactionStore(this.aaiClient, "Create");
            this.updateStore = new AAITransactionStore(this.aaiClient, "Update");
            this.deleteStore = new AAITransactionStore(this.aaiClient, "Delete");
        }

        AAIResourcesClient getAaiClient() {
            return this.aaiClient;
        }

        void execute(boolean dryRun) throws BulkProcessFailed {
            this.createStore.execute(dryRun);
            this.updateStore.execute(dryRun);
            this.deleteStore.execute(dryRun);
        }

        void create(AAIResourceUri uri, String payload) {
            createStore.create(uri, payload);
        }

        void update(AAIResourceUri uri, String payload) {
            updateStore.create(uri, payload);
        }

        void connect(AAIResourceUri uriA, AAIResourceUri uriB) {
            createStore.connect(uriA, uriB);
        }

        void delete(AAIResourceUri uri) {
            deleteStore.delete(uri);
        }
    }

    static class AAITransactionStore {
        private AAIResourcesClient aaiClient;
        private List<AAITransactionalClient> transactions;
        private int transactionCount;
        private static final int TRANSACTION_LIMIT = 30;
        private final String label;

        public AAITransactionStore(AAIResourcesClient aaiClient, String label) {
            this.label = label;
            this.aaiClient = aaiClient;
            transactions = new ArrayList<>();
            transactionCount = TRANSACTION_LIMIT;
        }

        private AAITransactionalClient getTransaction() {
            if (transactionCount == TRANSACTION_LIMIT) {
                transactions.add(aaiClient.beginTransaction());
                transactionCount = 0;
            }
            return transactions.get(transactions.size() - 1);
        }

        void execute(boolean dryRun) throws BulkProcessFailed {
            if (transactions.size() > 0) {
                for (AAITransactionalClient transaction : transactions) {
                    transaction.execute(dryRun);
                }
                transactions.clear();
            } else
                logger.info("Nothing to commit in AAI for " + label + " transactions");
        }

        void create(AAIResourceUri uri, String payload) {
            getTransaction().create(uri, payload);
            transactionCount++;
        }

        void connect(AAIResourceUri uriA, AAIResourceUri uriB) {
            getTransaction().connect(uriA, uriB);
            transactionCount++;
        }

        void delete(AAIResourceUri uri) {
            getTransaction().delete(uri);
            transactionCount++;
        }
    }
}
