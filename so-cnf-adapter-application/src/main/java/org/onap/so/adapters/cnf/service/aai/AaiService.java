package org.onap.so.adapters.cnf.service.aai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiUpdateRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AaiService {

    private final static Gson gson = new Gson();
    private final MulticloudClient multicloudClient;
    private final AaiIdGeneratorService aaiIdGeneratorService;
    private AAIResourcesClient aaiClient;

    public AaiService(MulticloudClient multicloudClient, AaiIdGeneratorService aaiIdGeneratorService) {
        this.multicloudClient = multicloudClient;
        this.aaiIdGeneratorService = aaiIdGeneratorService;
    }

    public void aaiUpdate(AaiUpdateRequest aaiUpdateRequest) throws BadResponseException {
        String instanceId = aaiUpdateRequest.getInstanceId();
        K8sRbInstanceStatus instanceStatus = multicloudClient.getInstanceStatus(instanceId);

        List<K8sRbInstanceResourceStatus> resourcesStatus = instanceStatus.getResourcesStatus();
        List<ParseResult> parsedStatus = resourcesStatus.stream()
                .map(status -> parse(status, aaiUpdateRequest))
                .collect(Collectors.toList());

        parsedStatus.forEach(status -> sendPostRequestToAai(status, aaiUpdateRequest));
    }

    private void sendPostRequestToAai(ParseResult parseResult, AaiUpdateRequest aaiUpdateRequest) {
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(aaiUpdateRequest.getCloudOwner(), aaiUpdateRequest.getCloudRegion())
                .tenant(aaiUpdateRequest.getTenantId())
                .build());
        String payload = gson.toJson(parseResult);
        getAaiClient().create(aaiUri, payload);
    }

    private AAIResourcesClient getAaiClient() {
        if (aaiClient == null) {
            aaiClient = new AAIResourcesClient();
        }
        return aaiClient;
    }

    private ParseResult parse(K8sRbInstanceResourceStatus status, AaiUpdateRequest aaiUpdateRequest) {
        ParseResult result = new ParseResult();
        K8sRbInstanceGvk gvk = status.getGvk();
        K8sStatusMetadata metadata = status.getStatus().getK8sStatusMetadata();
        String id = aaiIdGeneratorService.generateId(status, aaiUpdateRequest);
        result.setId(id);
        result.setName(status.getName());
        result.setGroup(gvk.getGroup());
        result.setVersion(gvk.getVersion());
        result.setKind(gvk.getKind());
        result.setNamespace(metadata.getNamespace());
        Collection<String> labels = new ArrayList<>();
        metadata.getLabels().forEach((key, value) -> {
            labels.add(key);
            labels.add(value);
        });
        result.setLabels(labels);
        result.setK8sResourceSelfLink(String.format("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/%s/query", aaiUpdateRequest.getInstanceId()));
        return result;
    }

    private class ParseResult {
        private String id;
        private String name;
        private String group;
        private String version;
        private String kind;
        private String namespace;
        private Collection<String> labels;
        private String k8sResourceSelfLink;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Collection<String> getLabels() {
            return labels;
        }

        public void setLabels(Collection<String> labels) {
            this.labels = labels;
        }

        public String getK8sResourceSelfLink() {
            return k8sResourceSelfLink;
        }

        public void setK8sResourceSelfLink(String k8sResourceSelfLink) {
            this.k8sResourceSelfLink = k8sResourceSelfLink;
        }
    }
}
