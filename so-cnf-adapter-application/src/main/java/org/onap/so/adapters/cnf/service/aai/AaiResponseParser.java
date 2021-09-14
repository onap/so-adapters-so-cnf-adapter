package org.onap.so.adapters.cnf.service.aai;

import org.apache.http.client.utils.URIBuilder;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceGvk;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sStatusMetadata;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
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

        URIBuilder uriBuilder = new URIBuilder();
        String selfLink = null;
        try {
            selfLink = uriBuilder
                    .setScheme("http")
                    .setHost("so-cnf-adapter")
                    .setPort(8090)
                    .setPath("/api/cnf-adapter/v1/instance/" + aaiRequest.getInstanceId() + "/query")
                    .setParameter("ApiVersion", gvk.getVersion())
                    .setParameter("Kind", gvk.getKind())
                    .setParameter("Name", status.getName())
                    .setParameter("Namespace", metadata.getNamespace())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        result.setK8sResourceSelfLink(selfLink);
        return result;
    }
}
