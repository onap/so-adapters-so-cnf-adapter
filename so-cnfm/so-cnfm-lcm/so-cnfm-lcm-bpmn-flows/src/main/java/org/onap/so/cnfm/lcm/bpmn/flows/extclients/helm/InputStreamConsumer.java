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
package org.onap.so.cnfm.lcm.bpmn.flows.extclients.helm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class InputStreamConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(InputStreamConsumer.class);

    private final InputStream inputStream;
    private final StringBuilder builder = new StringBuilder();
    private final Object lock = new Object();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    public InputStreamConsumer(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        logger.debug("Starting InputStreamConsumer Thread ...");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));) {
            String line;
            while ((line = reader.readLine()) != null) {
                this.builder.append(line).append("\n");
            }
        } catch (final IOException ioException) {
            logger.error("Failed while gobbling the input stream: ", ioException);
        } finally {
            this.isStopped.set(true);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
        logger.debug("InputStreamConsumer Thread Finished ...");

    }

    public String getContent() {
        if (!this.isStopped.get()) {
            try {
                synchronized (lock) {
                    logger.debug("Waiting for Thread to finish reading the input stream ... ");
                    lock.wait();
                }
            } catch (final InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
        return this.builder.toString();
    }

}
