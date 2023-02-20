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
package org.onap.so.cnfm.lcm.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.junit.Test;
import org.onap.so.cnfm.lcm.database.beans.AsDeploymentItem;
import org.onap.so.cnfm.lcm.database.beans.AsInst;
import org.onap.so.cnfm.lcm.database.beans.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.database.beans.AsLifecycleParam;
import org.onap.so.cnfm.lcm.database.beans.Job;
import org.onap.so.cnfm.lcm.database.beans.JobStatus;
import org.onap.so.openpojo.rules.ToStringTester;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
public class PojoClassesTests {

    private static final Validator VALIDATOR = ValidatorBuilder.create().with(new SetterTester())
            .with(new GetterTester()).with(new ToStringTester()).build();

    @Test
    public void test_database_job_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(Job.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(JobStatus.class, new JobStatus().job(new Job()), new JobStatus().job(new Job()))
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_job_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(Job.class));
    }

    @Test
    public void test_database_jobStatus_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(JobStatus.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(Job.class, new Job(), new Job()).withIgnoredAnnotations(Entity.class, Id.class)
                .verify();
    }

    @Test
    public void test_database_jobStatus_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(JobStatus.class));
    }

    @Test
    public void test_database_asInst_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(AsInst.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(AsDeploymentItem.class, new AsDeploymentItem(), new AsDeploymentItem())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_asInst_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(AsInst.class));
    }

    @Test
    public void test_database_asdeploymentItem_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(AsDeploymentItem.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(AsInst.class, new AsInst(), new AsInst())
                .withPrefabValues(AsDeploymentItem.class, new AsDeploymentItem(), new AsDeploymentItem())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_asdeploymentItem_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(AsDeploymentItem.class));
    }


    @Test
    public void test_database_asLcmOpOcc_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(AsLcmOpOcc.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(AsInst.class, new AsInst(), new AsInst())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_asLcmOpOcc_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(AsLcmOpOcc.class));
    }

    @Test
    public void test_database_asLifecycleParam_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(AsLifecycleParam.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(AsDeploymentItem.class, new AsDeploymentItem(), new AsDeploymentItem())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_asLifecycleParam_getterSetterMethod() {
        VALIDATOR.validate(PojoClassFactory.getPojoClass(AsLifecycleParam.class));
    }

}
