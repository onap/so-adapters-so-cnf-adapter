package org.onap.so.adapters.cnf.service.aai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiUpdateRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AaiService {

    private final MulticloudClient multicloudClient;
    private final AaiIdGeneratorService aaiIdGeneratorService;
    private AAIResourcesClient aaiClient;

    public AaiService(MulticloudClient multicloudClient, AaiIdGeneratorService aaiIdGeneratorService) {
        this.multicloudClient = multicloudClient;
        this.aaiIdGeneratorService = aaiIdGeneratorService;
    }


    public void aaiUpdate(AaiUpdateRequest aaiUpdateRequest) {
        String instanceId = aaiUpdateRequest.getInstanceId();
        K8sRbInstanceStatus instanceStatus = multicloudClient.getInstanceStatus(instanceId);

        List<K8sRbInstanceResourceStatus> resourcesStatus = instanceStatus.getResourcesStatus();
        List<ParseResult> parsedStatus = resourcesStatus.stream()
                .map(status -> parse(status, instanceId))
                .collect(Collectors.toList());

        parsedStatus.stream()
                .forEach(this::sendPostRequestToAai);
    }

    private void sendPostRequestToAai(ParseResult parseResult) {
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation()
                .model("serviceModelInvariantUUID").modelVer("serviceModelVersionId"));
        aaiUri.depth(Depth.ZERO); // Do not return relationships if any
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> payload = objectMapper.convertValue(parseResult, Map.class);

        getAaiClient().create(aaiUri, payload);
    }

    private AAIResourcesClient getAaiClient() {
        if (aaiClient == null) {
            aaiClient = new AAIResourcesClient();
        }
        return aaiClient;
    }

    private ParseResult parse(K8sRbInstanceResourceStatus status, String instanceId) {
        ParseResult result = new ParseResult();
        K8sRbInstanceGvk gvk = status.getGvk();
        K8sStatusMetadata metadata = status.getStatus().getK8sStatusMetadata();
        String id = aaiIdGeneratorService.generateId(status);
        result.setId(id);
        result.setName(status.getName());
        result.setGroup(gvk.getGroup());
        result.setVersion(gvk.getVersion());
        result.setKind(gvk.getKind());
        result.setNamespace(metadata.getNamespace());
        result.setLabels(new ArrayList<>());
        result.setK8sResourceSelfLink(String.format("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/%s/status", instanceId));
        return result;
    }

    private class ParseResult {
        private String id;
        private String name;
        private String group;
        private String version;
        private String kind;
        private String namespace;
        private List<String> labels;
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

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
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
