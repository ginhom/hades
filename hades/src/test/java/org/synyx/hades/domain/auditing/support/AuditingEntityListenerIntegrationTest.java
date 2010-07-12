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
package org.synyx.hades.domain.auditing.support;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.synyx.hades.dao.AuditableUserDao;
import org.synyx.hades.domain.auditing.Auditable;
import org.synyx.hades.domain.auditing.AuditableRole;
import org.synyx.hades.domain.auditing.AuditableUser;


/**
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:auditing-entity-listener.xml")
public class AuditingEntityListenerIntegrationTest {

    @Autowired
    AuditableUserDao dao;


    @Test
    public void auditsRootEntityCorrectly() throws Exception {

        AuditableUser user = new AuditableUser();
        dao.save(user);

        assertDatesSet(user);
    }


    @Test
    public void auditsTransitiveEntitiesCorrectly() throws Exception {

        AuditableRole role = new AuditableRole();
        role.setName("ADMIN");

        AuditableUser user = new AuditableUser();
        user.addRole(role);
        dao.save(user);

        assertDatesSet(user);
        assertDatesSet(role);
    }


    private void assertDatesSet(Auditable<?, ?> auditable) {

        assertThat(auditable.getCreatedDate(), is(notNullValue()));
        assertThat(auditable.getLastModifiedDate(), is(notNullValue()));
    }
}
