/*
 * Copyright © 2025 Deutsche Telekom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.so.adapters.cnf.util;

import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.cnf.model.aai.AaiRequest;
import org.onap.so.adapters.cnf.service.aai.KubernetesResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AaiRepositoryTest {

  private static int wireMockPort = SocketUtils.findAvailableTcpPort();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(wireMockPort));

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aai.enabled", () -> "true");
    registry.add("aai.endpoint", () -> "http://localhost:" + wireMockPort);
  }

  @Test
  @SneakyThrows
  public void thatAAIResourcesCanBeUpdated() {
    final String K8S_URL = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someOwner/someRegion/tenants/tenant/someTenant/k8s-resources/k8s-resource/someId";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(K8S_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("k8sResource.json")));
    final String VFMODULE_URL = "/aai/v24/network/generic-vnfs/generic-vnf/someGenericVnfId/vf-modules/vf-module/someVfModuleId";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(VFMODULE_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("vfModule.json")));
    final String GENERIC_VNF_URL = "/aai/v24/network/generic-vnfs/generic-vnf/someGenericVnfId";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(GENERIC_VNF_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("genericVnf.json")));
    final String BULK_URL = "/aai/v24/bulkprocess";
    final String expectedBulkBody = new String(
        Files.readAllBytes(Paths.get("src/test/resources/__files/bulkProcessRequestBody.json")));
    WireMock.stubFor(WireMock.put(WireMock.urlEqualTo(BULK_URL)).withRequestBody(WireMock.equalToJson(expectedBulkBody))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.CREATED.value()).withBody("{}")));
    final String expectedVnfVfModuleBulkBody = new String(
        Files.readAllBytes(Paths.get("src/test/resources/__files/bulkProcessVnfVfModuleRequest.json")));
    WireMock.stubFor(
        WireMock.put(WireMock.urlEqualTo(BULK_URL)).withRequestBody(WireMock.equalToJson(expectedVnfVfModuleBulkBody))
            .willReturn(WireMock.aResponse().withStatus(HttpStatus.CREATED.value()).withBody("{}")));

    KubernetesResource resource = new KubernetesResource();
    resource.setId("someId");
    resource.setName("someName");
    AaiRequest request = new AaiRequest();
    request.setCloudOwner("someOwner");
    request.setCloudRegion("someRegion");
    request.setTenantId("someTenant");
    request.setGenericVnfId("someGenericVnfId");
    request.setVfModuleId("someVfModuleId");
    IAaiRepository aaiRepository = IAaiRepository.instance(true);
    aaiRepository.update(resource, request);
    WireMock.verify(exactly(0), WireMock.putRequestedFor(WireMock.urlEqualTo(BULK_URL)));
    aaiRepository.commit(false);
    WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(K8S_URL)));
    WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo(BULK_URL))
        .withRequestBody(WireMock.equalToJson(expectedBulkBody)));
    WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo(BULK_URL))
        .withRequestBody(WireMock.equalToJson(expectedVnfVfModuleBulkBody)));
  }

  @Test
  @SneakyThrows
  public void thatAAIResourcesCanBeDeleted() {
    final String K8S_NOT_FOUND_URL = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someCloudOwner/someCloudRegionId/tenants/tenant/someTenantId/k8s-resources/k8s-resource/doesNotExist";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(K8S_NOT_FOUND_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.NOT_FOUND.value())
            .withBodyFile("k8sResourceNotFoundResponse.json")));
    final String K8S_URL = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someCloudOwner/someCloudRegionId/tenants/tenant/someTenantId/k8s-resources/k8s-resource/someK8sResource";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(K8S_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("k8sResource.json")));
    final String K8S_EXISTS_URL = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someOwner/someRegion/tenants/tenant/someTenant/k8s-resources/k8s-resource/12345";
    final String K8S_EXISTS_URL_MINIMAL = "/aai/v24/cloud-infrastructure/cloud-regions/cloud-region/someOwner/someRegion/tenants/tenant/someTenant/k8s-resources/k8s-resource/12345?format=count&resultIndex=0&resultSize=1";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(K8S_EXISTS_URL_MINIMAL))
        .willReturn(
            WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("k8sResourceExistsResponse.json")));
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(K8S_EXISTS_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value()).withBodyFile("k8sResource.json").withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())));
    WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo(K8S_EXISTS_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));
    final String VFMODULE_URL = "/aai/v24/network/generic-vnfs/generic-vnf/someGenericVnfId/vf-modules/vf-module/someVfModuleId";
    WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(VFMODULE_URL))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
            .withBodyFile("vfModuleWithRelationshipsResponse.json")));

    final String BULK_URL = "/aai/v24/bulkprocess";
    final String expectedBulkBody = new String(
        Files.readAllBytes(Paths.get("src/test/resources/__files/bulkDeleteRequestBody.json")));
    WireMock.stubFor(WireMock.put(WireMock.urlEqualTo(BULK_URL)).withRequestBody(WireMock.equalToJson(expectedBulkBody))
        .willReturn(WireMock.aResponse().withStatus(HttpStatus.CREATED.value()).withBody("{}")));

    KubernetesResource resource = new KubernetesResource();
    resource.setId("someId");
    resource.setName("someName");
    AaiRequest request = new AaiRequest();
    request.setCloudOwner("someOwner");
    request.setCloudRegion("someRegion");
    request.setTenantId("someTenant");
    request.setGenericVnfId("someGenericVnfId");
    request.setVfModuleId("someVfModuleId");
    IAaiRepository aaiRepository = IAaiRepository.instance(true);
    List<KubernetesResource> resourceList = new ArrayList<>();
    resourceList.add(resource);
    aaiRepository.delete(request, resourceList);
    WireMock.verify(exactly(0),WireMock.deleteRequestedFor(WireMock.urlEqualTo(BULK_URL)));
    aaiRepository.commit(false);
    WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo(BULK_URL))
        .withRequestBody(WireMock.equalToJson(expectedBulkBody)));
  }
}
