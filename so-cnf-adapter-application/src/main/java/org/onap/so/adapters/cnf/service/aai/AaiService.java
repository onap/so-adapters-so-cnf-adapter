package org.onap.so.adapters.cnf.service.aai;

import java.util.List;
import java.util.stream.Collectors;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.stereotype.Service;

@Service
public class AaiService {

    private final MulticloudClient multicloudClient;
    private final AaiRequestSender aaiRequestSender;
    private final AaiResponseParser responseParser;

    public AaiService(MulticloudClient multicloudClient, AaiRequestSender aaiRequestSender, AaiResponseParser responseParser) {
        this.multicloudClient = multicloudClient;
        this.aaiRequestSender = aaiRequestSender;
        this.responseParser = responseParser;
    }

    public void aaiUpdate(AaiRequest aaiRequest) throws BadResponseException {
        List<ParseResult> parseStatus = parseStatus(aaiRequest);

        for (ParseResult status : parseStatus) {
            aaiRequestSender.sendUpdateRequestToAai(status, aaiRequest);
        }
    }

    public void aaiDelete(AaiRequest aaiRequest) throws BadResponseException {
        List<ParseResult> parseStatus = parseStatus(aaiRequest);

        for (ParseResult status : parseStatus) {
            aaiRequestSender.sendDeleteRequestToAai(status, aaiRequest);
        }
    }

    private List<ParseResult> parseStatus(AaiRequest aaiRequest) throws BadResponseException {
        String instanceId = aaiRequest.getInstanceId();
        K8sRbInstanceStatus instanceStatus = multicloudClient.getInstanceStatus(instanceId);

        List<K8sRbInstanceResourceStatus> resourcesStatus = instanceStatus.getResourcesStatus();
        return resourcesStatus.stream()
                .map(status -> responseParser.parse(status, aaiRequest))
                .collect(Collectors.toList());
    }
}
