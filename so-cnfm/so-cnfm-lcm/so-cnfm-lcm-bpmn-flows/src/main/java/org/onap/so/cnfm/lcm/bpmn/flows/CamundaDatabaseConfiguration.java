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
package org.onap.so.cnfm.lcm.bpmn.flows;


import static org.slf4j.LoggerFactory.getLogger;
import javax.sql.DataSource;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
@EnableTransactionManagement
public class CamundaDatabaseConfiguration {

    private static final String CAMUNDA_TRANSACTION_MANAGER_BEAN_NAME = "camundaTransactionManager";

    private static final String CAMUNDA_DATA_SOURCE_BEAN_NAME = "camundaBpmDataSource";

    private static final Logger logger = getLogger(CamundaDatabaseConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.camunda")
    public HikariConfig camundaDbConfig() {
        logger.debug("Creating Camunda HikariConfig bean ... ");
        return new HikariConfig();
    }

    @Bean(name = CAMUNDA_DATA_SOURCE_BEAN_NAME)
    public DataSource camundaDataSource(@Autowired(required = false) final MBeanExporter mBeanExporter) {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean(CAMUNDA_DATA_SOURCE_BEAN_NAME);
        }
        logger.debug("Creating Camunda HikariDataSource bean ... ");
        final HikariConfig hikariConfig = this.camundaDbConfig();
        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = CAMUNDA_TRANSACTION_MANAGER_BEAN_NAME)
    public PlatformTransactionManager camundaTransactionManager(
            @Qualifier(CAMUNDA_DATA_SOURCE_BEAN_NAME) final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SpringBootProcessEnginePlugin transactionManagerProcessEnginePlugin(
            @Qualifier(CAMUNDA_TRANSACTION_MANAGER_BEAN_NAME) final PlatformTransactionManager camundaTransactionManager) {
        return new SpringBootProcessEnginePlugin() {
            @Override
            public void preInit(final SpringProcessEngineConfiguration processEngineConfiguration) {
                logger.info("Setting Camunda TransactionManager ...");
                processEngineConfiguration.setTransactionManager(camundaTransactionManager);

            }
        };
    }
}
