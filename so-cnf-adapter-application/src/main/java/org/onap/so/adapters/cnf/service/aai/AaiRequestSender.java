package org.onap.so.adapters.cnf.service.aai;

import com.google.gson.Gson;
import java.util.Optional;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.onap.so.client.exception.BadResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class AaiRequestSender {
    private static final Logger logger = LoggerFactory.getLogger(AaiRequestSender.class);

    private static final Gson gson = new Gson();
    private AAIResourcesClient aaiClient;

    void sendUpdateRequestToAai(ParseResult parseResult, AaiRequest aaiRequest) throws BadResponseException {

        AAIResourceUri aaiTenantURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                                                              .cloudRegion(aaiRequest.getCloudOwner(),
                                                                                      aaiRequest.getCloudRegion())
                                                                              .tenant(aaiRequest.getTenantId())
                                                                              .build());
        try {
            if (getAaiClient().exists(aaiTenantURI)) {
                AAIResourceUri aaiK8sResourceURI = AAIUriFactory.createResourceUri(
                        AAIFluentTypeBuilder.Types.K8S_RESOURCE.getFragment(parseResult.getId()));
                String payload = gson.toJson(parseResult);
                getAaiClient().createIfNotExists(aaiTenantURI, Optional.of(payload))
                        .connect(aaiTenantURI, aaiK8sResourceURI);
            } else {
                throw new BadResponseException("The cloudRegion or tenant not exist in AAI!");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    void sendDeleteRequestToAai(ParseResult parseResult, AaiRequest aaiRequest) throws BadResponseException {
        AAIResourceUri k8sResourceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                                                                                .cloudRegion(aaiRequest.getCloudOwner(),
                                                                                        aaiRequest.getCloudRegion())
                                                                                .tenant(aaiRequest.getTenantId())
                                                                                .k8sResource(parseResult.getId())
                                                                                .build());
        try {
            if (getAaiClient().exists(k8sResourceURI)) {
                getAaiClient().delete(k8sResourceURI);
            } else {
                throw new BadResponseException("The cloudRegion, tenant or k8sResource not exist in AAI!");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private AAIResourcesClient getAaiClient() {
        if (aaiClient == null) {
            aaiClient = new AAIResourcesClient();
        }
        return aaiClient;
    }

    private void handleException(Exception e) throws BadResponseException {
        if (e instanceof BadResponseException) {
            throw (BadResponseException) e;
        }

        logger.error("Exception during sending the request to AAI", e);
        throw new BadResponseException(e.getMessage());
    }
}
