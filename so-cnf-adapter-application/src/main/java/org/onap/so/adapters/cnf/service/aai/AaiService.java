package org.onap.so.adapters.cnf.service.aai;

import org.onap.so.adapters.cnf.AaiConfiguration;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceResourceStatus;
import org.onap.so.adapters.cnf.model.statuscheck.K8sRbInstanceStatus;
import org.onap.so.adapters.cnf.util.IAaiRepository;
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
    private final AaiResponseParser responseParser;
    private final AaiConfiguration configuration;

    public AaiService(MulticloudClient multicloudClient, AaiResponseParser responseParser, AaiConfiguration configuration) {
        this.multicloudClient = multicloudClient;
        this.responseParser = responseParser;
        this.configuration = configuration;
    }

    public void aaiUpdate(AaiRequest aaiRequest) throws BadResponseException {
        List<KubernetesResource> parseStatus = parseStatus(aaiRequest);
        IAaiRepository aaiRepository = IAaiRepository.instance(configuration.isEnabled());
        parseStatus.forEach(status -> aaiRepository.update(status, aaiRequest));
        aaiRepository.commit(true);
    }

    public void aaiDelete(AaiRequest aaiRequest) throws BadResponseException {
        List<KubernetesResource> parseStatus = parseStatus(aaiRequest);

        IAaiRepository aaiRepository = IAaiRepository.instance(configuration.isEnabled());
        parseStatus.forEach(status -> aaiRepository.delete(status, aaiRequest));
        aaiRepository.commit(true);
    }

    private List<KubernetesResource> parseStatus(AaiRequest aaiRequest) throws BadResponseException {
        String instanceId = aaiRequest.getInstanceId();
        K8sRbInstanceStatus instanceStatus = multicloudClient.getInstanceStatus(instanceId);

        List<K8sRbInstanceResourceStatus> resourcesStatus = instanceStatus.getResourcesStatus();
        return resourcesStatus.stream()
                .map(status -> responseParser.parse(status, aaiRequest))
                .collect(Collectors.toList());
    }
}
