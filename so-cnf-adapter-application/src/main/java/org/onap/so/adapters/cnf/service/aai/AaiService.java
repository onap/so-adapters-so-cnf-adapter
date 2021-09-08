package org.onap.so.adapters.cnf.service.aai;

import org.onap.so.adapters.cnf.AaiConfiguration;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AaiService {

    private final static Logger log = LoggerFactory.getLogger(AaiService.class);

    private final MulticloudClient multicloudClient;
    private final AaiRequestSender aaiRequestSender;
    private final AaiResponseParser responseParser;
    private final AaiConfiguration aaiConfiguration;

    public AaiService(MulticloudClient multicloudClient,
                      AaiRequestSender aaiRequestSender,
                      AaiResponseParser responseParser,
                      AaiConfiguration aaiConfiguration) {
        this.multicloudClient = multicloudClient;
        this.aaiRequestSender = aaiRequestSender;
        this.responseParser = responseParser;
        this.aaiConfiguration = aaiConfiguration;
    }

    public void aaiUpdate(AaiRequest aaiRequest) throws BadResponseException {
        if (aaiConfiguration.isEnabled()) {
            List<ParseResult> parseStatus = parseStatus(aaiRequest);
            parseStatus.forEach(status -> aaiRequestSender.sendUpdateRequestToAai(status, aaiRequest));
        } else {
            log.info("aai.enabled=false, do not execute aaiUpdate flow");
        }
    }

    public void aaiDelete(AaiRequest aaiRequest) throws BadResponseException {
        if (aaiConfiguration.isEnabled()) {
            List<ParseResult> parseStatus = parseStatus(aaiRequest);
            parseStatus.forEach(status -> aaiRequestSender.sendDeleteRequestToAai(aaiRequest));
        } else {
            log.info("aai.enabled=false, do not execute aaiDelete flow");
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
