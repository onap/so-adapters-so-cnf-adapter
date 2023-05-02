/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 * ================================================================================
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.APPLICATION_NAME_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_ID_PARAM_NAME;
import static org.onap.so.cnfm.lcm.bpmn.flows.extclients.sdc.SdcCsarPropertiesConstants.DESCRIPTOR_INVARIANT_ID_PARAM_NAME;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(Parameterized.class)
public class SdcCsarPackageParserTest {

    private static final String RESOURCE_ASD_PACKAGE_CSAR_PATH =
            "src/test/resources/resource-Generatedasdpackage-csar.csar";

    private static final String RESOURCE_ASD_ALPHABETICAL_PACKAGE_CSAR_PATH =
            "src/test/resources/resource-Generatedasdpackage-csar-alphabetical.csar";


    @Parameterized.Parameter
    public String resourceCsarPath;

    @Parameterized.Parameters
    public static Iterable<String> data() {
        return Arrays.asList(RESOURCE_ASD_PACKAGE_CSAR_PATH, RESOURCE_ASD_ALPHABETICAL_PACKAGE_CSAR_PATH);
    }

    @Test
    public void testResourceAsdCsar() throws IOException {
        final SdcCsarPackageParser objUnderTest = new SdcCsarPackageParser();

        final byte[] content = getFileContent(Paths.get(getAbsolutePath(resourceCsarPath)));

        final Map<String, Object> properties = objUnderTest.getAsdProperties(content);
        assertEquals("123e4567-e89b-12d3-a456-426614174000", properties.get(DESCRIPTOR_ID_PARAM_NAME));
        assertEquals("123e4yyy-e89b-12d3-a456-426614174abc", properties.get(DESCRIPTOR_INVARIANT_ID_PARAM_NAME));
        assertEquals("SampleApp", properties.get(APPLICATION_NAME_PARAM_NAME));
        assertEquals("2.3", properties.get(SdcCsarPropertiesConstants.APPLICATION_VERSION_PARAM_NAME));
        assertEquals("MyCompany", properties.get(SdcCsarPropertiesConstants.PROVIDER_PARAM_NAME));

        @SuppressWarnings("unchecked")
        final List<DeploymentItem> items =
                (List<DeploymentItem>) properties.get(SdcCsarPropertiesConstants.DEPLOYMENT_ITEMS_PARAM_NAME);
        assertNotNull(items);
        assertEquals(2, items.size());

        final DeploymentItem deploymentItem = items.get(0);
        assertEquals("sampleapp-db", deploymentItem.getName());
        assertEquals("1", deploymentItem.getItemId());
        assertEquals("1", deploymentItem.getDeploymentOrder());
        assertEquals("Artifacts/Deployment/HELM/sampleapp-db-operator-helm.tgz", deploymentItem.getFile());


    }

    private String getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.getAbsolutePath();
    }

    private byte[] getFileContent(final Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
