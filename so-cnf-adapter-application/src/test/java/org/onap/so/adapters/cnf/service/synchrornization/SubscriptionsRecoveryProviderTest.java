/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.cnf.service.synchrornization;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.cnf.client.MulticloudClient;
import org.onap.so.adapters.cnf.model.InstanceResponse;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SubscriptionsRecoveryProviderTest {

    @InjectMocks
    private SubscriptionsRecoveryProvider provider;

    @Mock
    private MulticloudClient multicloudClient;

    @Test
    public void getInstanceList_returnsInstanceIds() throws BadResponseException {
        // given
        InstanceResponse r1 = new InstanceResponse();
        r1.setId("instance-1");
        InstanceResponse r2 = new InstanceResponse();
        r2.setId("instance-2");

        when(multicloudClient.getAllInstances()).thenReturn(Arrays.asList(r1, r2));

        // when
        Set<String> result = provider.getInstanceList();

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains("instance-1"));
        assertTrue(result.contains("instance-2"));
    }

    @Test
    public void getInstanceList_returnsEmptySet_whenNoInstances() throws BadResponseException {
        // given
        when(multicloudClient.getAllInstances()).thenReturn(Arrays.asList());

        // when
        Set<String> result = provider.getInstanceList();

        // then
        assertTrue(result.isEmpty());
    }
}
