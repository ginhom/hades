/*
 * Copyright 2008-2010 the original author or authors.
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
package org.synyx.hades.dao.test;

import org.junit.Ignore;


/**
 * Testcase to run {@link org.synyx.hades.dao.UserDao} integration tests on top
 * of EclipseLink. So far not running as of an EclipseLink bug.
 * 
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=312132
 * @author Oliver Gierke
 */
// @ContextConfiguration(value = "classpath:eclipselink.xml", inheritLocations =
// true)
@Ignore
public class EclipseLinkNamespaceUserDaoIntegrationTest extends
        NamespaceUserDaoIntegrationTest {

}
