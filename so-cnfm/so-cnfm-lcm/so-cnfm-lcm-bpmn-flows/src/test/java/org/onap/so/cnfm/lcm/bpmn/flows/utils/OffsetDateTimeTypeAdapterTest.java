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
package org.onap.so.cnfm.lcm.bpmn.flows.utils;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.Test;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class OffsetDateTimeTypeAdapterTest {
    private static final OffsetDateTime OFF_SET_DATETIME_VALUE =
            OffsetDateTime.of(2023, 1, 1, 12, 00, 15, 0, ZoneOffset.UTC);
    private static final String STRING_VALUE = "\"2023-01-01T12:00:15Z\"";

    @Test
    public void testReadWithValidOffsetDateTimeString() throws IOException {
        final OffsetDateTimeTypeAdapter objUnderTest = new OffsetDateTimeTypeAdapter();

        final Reader reader = new StringReader(STRING_VALUE);
        final JsonReader jsonReader = new JsonReader(reader);

        final OffsetDateTime actual = objUnderTest.read(jsonReader);
        assertEquals(OFF_SET_DATETIME_VALUE, actual);

    }

    @Test
    public void testWritedWithValidOffsetDateTime() throws IOException {
        final OffsetDateTimeTypeAdapter objUnderTest = new OffsetDateTimeTypeAdapter();

        final StringWriter writer = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(writer);

        objUnderTest.write(jsonWriter, OFF_SET_DATETIME_VALUE);
        assertEquals(STRING_VALUE, writer.toString());

    }
}
