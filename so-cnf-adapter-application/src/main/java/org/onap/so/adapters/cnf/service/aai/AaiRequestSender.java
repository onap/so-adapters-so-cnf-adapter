package org.onap.so.adapters.cnf.service.aai;

import com.google.gson.Gson;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.adapters.cnf.model.instantiation.AaiRequest;
import org.springframework.stereotype.Component;

@Component
class AaiRequestSender {

    private final static Gson gson = new Gson();
    private AAIResourcesClient aaiClient;

    void sendUpdateRequestToAai(ParseResult parseResult, AaiRequest aaiRequest) {
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                .tenant(aaiRequest.getTenantId())
                .build());
        String payload = gson.toJson(parseResult);
        getAaiClient().create(aaiUri, payload);

        String vnfId = aaiRequest.getVnfId();
        String vfModuleId = aaiRequest.getVfModuleId();
        if (vnfId != null && vfModuleId != null) {
            aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network()
                    .genericVnf(vnfId)
                    .vfModule(vfModuleId)
                    .build());
            getAaiClient().create(aaiUri, payload);
        }
    }

    void sendDeleteRequestToAai(AaiRequest aaiRequest) {
        String vnfId = aaiRequest.getVnfId();
        String vfModuleId = aaiRequest.getVfModuleId();
        if (vnfId != null && vfModuleId != null) {
            AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network()
                    .genericVnf(aaiRequest.getVnfId())
                    .vfModule(aaiRequest.getVfModuleId())
                    .build());
            getAaiClient().delete(aaiUri);
        }
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(aaiRequest.getCloudOwner(), aaiRequest.getCloudRegion())
                .tenant(aaiRequest.getTenantId())
                .build());
        getAaiClient().delete(aaiUri);
    }

    private AAIResourcesClient getAaiClient() {
        if (aaiClient == null) {
            aaiClient = new AAIResourcesClient();
        }
        return aaiClient;
    }
}
