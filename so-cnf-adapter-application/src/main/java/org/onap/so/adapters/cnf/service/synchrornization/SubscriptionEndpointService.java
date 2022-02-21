package org.onap.so.adapters.cnf.service.synchrornization;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscriptionEndpointService {

    private Map<String, Boolean> endpointsStatusMap = new ConcurrentHashMap<>();
    private static final String PROTOCOL = "http";
    private static final String HOST = "so-cnf-adapter";
    private static final int PORT = 8090;

    public void enableEndpoint(String endpoint) {
        endpointsStatusMap.put(endpoint, true);
    }

    public void disableEndpoint(String endpoint) {
        endpointsStatusMap.put(endpoint, false);
    }

    public boolean isEndpointActive(String endpoint) {
        return endpointsStatusMap.getOrDefault(endpoint, false);
    }

    public String generateCallbackEndpoint(String instanceId) {
        return generateUri(instanceId).toString();
    }

    public String generateEndpointPath(String instanceId) {
        return generateUri(instanceId).getPath();
    }

    private URI generateUri(String instanceId) {
        String path = String.format("/instanceId/%s", instanceId);

        URIBuilder uriBuilder = new URIBuilder();

        try {
            return uriBuilder
                    .setScheme(PROTOCOL)
                    .setHost(HOST)
                    .setPort(PORT)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
