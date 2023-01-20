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
package org.onap.so.cnfm.lcm.database.config;

import static org.slf4j.LoggerFactory.getLogger;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
        basePackages = {"org.onap.so.cnfm.lcm.database.repository"})
public class CnfmDatabaseConfiguration {
    private static final Logger logger = getLogger(CnfmDatabaseConfiguration.class);

    private static final String PERSISTENCE_UNIT = "cnfm";
    private static final String CNFM_DATA_SOURCE_QUALIFIER = "cnfmDataSource";

    @Autowired(required = false)
    private MBeanExporter mBeanExporter;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.cnfm")
    public HikariConfig cnfmDbConfig() {
        logger.debug("Creating CNFM HikariConfig bean ... ");
        return new HikariConfig();
    }

    @Primary
    @FlywayDataSource
    @Bean(name = CNFM_DATA_SOURCE_QUALIFIER)
    public DataSource dataSource() {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean(CNFM_DATA_SOURCE_QUALIFIER);
        }
        logger.debug("Creating CNFM HikariDataSource bean ... ");
        final HikariConfig hikariConfig = this.cnfmDbConfig();
        return new HikariDataSource(hikariConfig);
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder,
            @Qualifier(CNFM_DATA_SOURCE_QUALIFIER) final DataSource dataSource) {
        return builder.dataSource(dataSource).packages(Job.class.getPackage().getName())
                .persistenceUnit(PERSISTENCE_UNIT).build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
