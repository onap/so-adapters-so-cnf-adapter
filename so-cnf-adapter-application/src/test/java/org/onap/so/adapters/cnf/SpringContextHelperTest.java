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
package org.onap.so.adapters.cnf;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringContextHelperTest {

  @Autowired
  SpringContextHelper helper;

  @Test
  // The SpringContextHelper is part of the so common library
  // and is in a package that would normally not be picked up
  // by spring's context scanning.
  // This test thus essentially assures that the spring boot app
  // has a @ComponentScan that includes org.onap.so.spring
  public void thatSpringContextHelperIsAvailable() {
    assertNotNull(helper);
  }
}
