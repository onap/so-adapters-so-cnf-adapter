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
package org.onap.so.cnfm.lcm.app;

import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.ClassUtils;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class DefaultToShortClassNameBeanNameGenerator extends AnnotationBeanNameGenerator {
    private static final Logger logger = getLogger(DefaultToShortClassNameBeanNameGenerator.class);

    @Override
    protected String buildDefaultBeanName(final BeanDefinition definition) {
        final String beanClassName = definition.getBeanClassName();
        if (beanClassName != null) {
            return ClassUtils.getShortName(beanClassName);
        }
        logger.warn("Bean class name is not specified...");
        return null;
    }
}
