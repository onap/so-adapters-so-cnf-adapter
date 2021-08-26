package org.onap.so.adapters.cnf.service.aai;

import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class AaiResponseParser {

    private final AaiIdGeneratorService aaiIdGeneratorService;

    public AaiResponseParser(AaiIdGeneratorService aaiIdGeneratorService) {
        this.aaiIdGeneratorService = aaiIdGeneratorService;
    }

    ParseResult parse(K8sRbInstanceResourceStatus status, AaiRequest aaiRequest) {
        ParseResult result = new ParseResult();
        K8sRbInstanceGvk gvk = status.getGvk();
        K8sStatus k8sStatus = status.getStatus();
        K8sStatusMetadata metadata = k8sStatus.getK8sStatusMetadata();
        String id = aaiIdGeneratorService.generateId(status, aaiRequest);
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
        result.setK8sResourceSelfLink(String.format("http://so-cnf-adapter:8090/api/cnf-adapter/v1/instance/%s/query", aaiRequest.getInstanceId()));
        return result;
    }

}
