/*
 * Copyright 2008-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.synyx.hades.dao.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.synyx.hades.domain.Specifications.*;
import static org.synyx.hades.domain.UserSpecifications.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.hades.dao.UserDao;
import org.synyx.hades.domain.Order;
import org.synyx.hades.domain.Page;
import org.synyx.hades.domain.PageImpl;
import org.synyx.hades.domain.PageRequest;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Role;
import org.synyx.hades.domain.Sort;
import org.synyx.hades.domain.Specification;
import org.synyx.hades.domain.User;


/**
 * Base integration test class for {@code UserDao}. Loads a basic
 * (non-namespace) Spring configuration file as well as Hibernate configuration
 * to execute tests.
 * <p>
 * To test further persistence providers subclass this class and provide a
 * custom provider configuration.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Transactional
public class UserDaoIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    // CUT
    @Autowired
    private UserDao userDao;

    // Test fixture
    private User firstUser;
    private User secondUser;
    private User thirdUser;
    private Integer id;


    @Before
    public void setUp() {

        firstUser = new User("Oliver", "Gierke", "gierke@synyx.de");
        secondUser = new User("Joachim", "Arrasz", "arrasz@synyx.de");
        thirdUser = new User("Dave", "Matthews", "no@email.com");
    }


    /**
     * Tests creation of users.
     */
    @Test
    public void testCreation() {

        Query countQuery = em.createQuery("select count(u) from User u");
        Long before = (Long) countQuery.getSingleResult();

        flushTestUsers();

        assertEquals(before + 3, countQuery.getSingleResult());
    }


    /**
     * Tests reading a single user.
     * 
     * @throws Exception
     */
    @Test
    public void testRead() throws Exception {

        flushTestUsers();

        User foundPerson = userDao.readByPrimaryKey(id);
        assertEquals(firstUser.getFirstname(), foundPerson.getFirstname());
    }


    /**
     * Asserts, that a call to {@code UserDao#readByPrimaryKey(Integer)} returns
     * {@code null} for invalid not {@code null} ids.
     */
    @Test
    public void testReadByPrimaryKeyReturnsNullForNotFoundEntities() {

        flushTestUsers();

        assertNull(userDao.readByPrimaryKey(id * 27));
    }


    @Test
    public void savesCollectionCorrectly() throws Exception {

        List<User> result =
                userDao.save(Arrays.asList(firstUser, secondUser, thirdUser));
        assertNotNull(result);
        assertThat(result.size(), is(3));
        assertThat(result, hasItems(firstUser, secondUser, thirdUser));
    }


    @Test
    public void savingNullCollectionIsNoOp() throws Exception {

        List<User> result = userDao.save((Collection<User>) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    public void savingEmptyCollectionIsNoOp() throws Exception {

        List<User> result = userDao.save(new ArrayList<User>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    /**
     * Tests updating a user.
     */
    @Test
    public void testUpdate() {

        flushTestUsers();

        User foundPerson = userDao.readByPrimaryKey(id);
        foundPerson.setLastname("Schlicht");

        User updatedPerson = userDao.readByPrimaryKey(id);
        assertEquals(foundPerson.getFirstname(), updatedPerson.getFirstname());
    }


    @Test
    public void existReturnsWhetherAnEntityCanBeLoaded() throws Exception {

        flushTestUsers();
        assertTrue(userDao.exists(id));
        assertFalse(userDao.exists(id * 27));
    }


    /**
     * Tests deleting a user.
     */
    @Test
    public void testDelete() {

        flushTestUsers();

        userDao.delete(firstUser);
        assertNull(userDao.readByPrimaryKey(id));
    }


    @Test
    public void returnsAllSortedCorrectly() throws Exception {

        flushTestUsers();
        List<User> result =
                userDao.readAll(new Sort(Order.ASCENDING, "lastname"));
        assertNotNull(result);
        assertThat(result.size(), is(3));
        assertThat(result.get(0), is(secondUser));
        assertThat(result.get(1), is(firstUser));
        assertThat(result.get(2), is(thirdUser));
    }


    @Test
    public void deleteColletionOfEntities() {

        flushTestUsers();

        long before = userDao.count();

        userDao.delete(Arrays.asList(firstUser, secondUser));
        assertThat(userDao.count(), is(before - 2));
    }


    @Test
    public void deleteEmptyCollectionDoesNotDeleteAnything() {

        assertDeleteCallDoesNotDeleteAnything(new ArrayList<User>());
    }


    @Test
    public void deleteWithNullDoesNotDeleteAnything() throws Exception {

        assertDeleteCallDoesNotDeleteAnything(null);
    }


    private void assertDeleteCallDoesNotDeleteAnything(List<User> collection) {

        flushTestUsers();
        Long count = userDao.count();

        userDao.delete(collection);
        assertEquals(count, userDao.count());
    }


    @Test
    public void executesManipulatingQuery() throws Exception {

        flushTestUsers();
        userDao.renameAllUsersTo("newLastname");

        assertEquals(userDao.count().intValue(),
                userDao.findByLastname("newLastname").size());
    }


    /**
     * Make sure no {@link NullPointerException} is being thrown.
     * 
     * @see Ticket #110
     */
    @Test
    public void testFinderInvocationWithNullParameter() {

        flushTestUsers();

        userDao.findByLastname(null);
    }


    /**
     * Tests, that searching by the lastname of the reference user returns
     * exactly that instance.
     * 
     * @throws Exception
     */
    @Test
    public void testFindByLastname() throws Exception {

        flushTestUsers();

        List<User> byName = userDao.findByLastname("Gierke");

        assertTrue(byName.size() == 1);
        assertEquals(firstUser, byName.get(0));
    }


    /**
     * Tests, that searching by the email address of the reference user returns
     * exactly that instance.
     * 
     * @throws Exception
     */
    @Test
    public void testFindByEmailAddress() throws Exception {

        flushTestUsers();

        User byName = userDao.findByEmailAddress("gierke@synyx.de");

        assertNotNull(byName);
        assertEquals(firstUser, byName);
    }


    /**
     * Tests reading all users.
     */
    @Test
    public void testReadAll() {

        flushTestUsers();

        List<User> reference = Arrays.asList(firstUser, secondUser);
        assertTrue(userDao.readAll().containsAll(reference));
    }


    /**
     * Tests that all users get deleted by triggering
     * {@link UserDao#deleteAll()}.
     * 
     * @throws Exception
     */
    @Test
    public void deleteAll() throws Exception {

        flushTestUsers();

        userDao.deleteAll();

        assertEquals((Long) 0L, userDao.count());
    }


    /**
     * Tests cascading persistence.
     */
    @Test
    public void testCascadesPersisting() {

        // Create link prior to persisting
        firstUser.addColleague(secondUser);

        // Persist
        flushTestUsers();

        // Fetches first user from .. bdatabase
        User firstReferenceUser = userDao.readByPrimaryKey(firstUser.getId());
        assertEquals(firstUser, firstReferenceUser);

        // Fetch colleagues and assert link
        Set<User> colleagues = firstReferenceUser.getColleagues();
        assertEquals(1, colleagues.size());
        assertTrue(colleagues.contains(secondUser));
    }


    /**
     * Tests, that persisting a relationsship without cascade attributes throws
     * a {@code DataAccessException}.
     */
    @Test(expected = DataAccessException.class)
    public void testPreventsCascadingRolePersisting() {

        firstUser.addRole(new Role("USER"));

        flushTestUsers();
    }


    /**
     * Tests cascading on {@literal merge} operation.
     */
    @Test
    public void testMergingCascadesCollegueas() {

        firstUser.addColleague(secondUser);
        flushTestUsers();

        firstUser.addColleague(new User("Florian", "Hopf", "hopf@synyx.de"));
        firstUser = userDao.save(firstUser);

        User reference = userDao.readByPrimaryKey(firstUser.getId());
        Set<User> colleagues = reference.getColleagues();

        assertNotNull(colleagues);
        assertEquals(2, colleagues.size());
    }


    /**
     * Tests, that the generic dao implements count correctly.
     */
    @Test
    public void testCountsCorrectly() {

        Long count = userDao.count();

        User user = new User();
        user.setEmailAddress("gierke@synyx.de");
        userDao.save(user);

        assertTrue(userDao.count().equals(count + 1));
    }


    /**
     * Tests invoking a method of a custom implementation of the DAO interface.
     */
    @Test
    public void testInvocationOfCustomImplementation() {

        userDao.someCustomMethod(new User());
    }


    /**
     * Tests that overriding a finder method is recognized by the DAO
     * implementation. If an overriding method is found it will will be invoked
     * instead of the automatically generated finder.
     */
    @Test
    public void testOverwritingFinder() {

        userDao.findByOverrridingMethod();
    }


    @Test
    public void testUsesHadesQueryAnnotation() {

        assertEquals(null, userDao.findByHadesQuery("gierke@synyx.de"));
    }


    @Test
    public void testExecutionOfProjectingMethod() {

        flushTestUsers();
        assertEquals(1, userDao.countWithFirstname("Oliver").longValue());
    }


    @Test
    public void executesSpecificationCorrectly() {

        flushTestUsers();
        assertThat(userDao.readAll(where(userHasFirstname("Oliver"))).size(),
                is(1));
    }


    @Test
    public void executesCombinedSpecificationsCorrectly() {

        flushTestUsers();
        Specification<User> spec =
                where(userHasFirstname("Oliver")).or(userHasLastname("Arrasz"));
        assertThat(userDao.readAll(spec).size(), is(2));
    }


    @Test
    public void executesCombinedSpecificationsWithPageableCorrectly() {

        flushTestUsers();
        Specification<User> spec =
                where(userHasFirstname("Oliver")).or(userHasLastname("Arrasz"));

        Page<User> users = userDao.readAll(spec, new PageRequest(0, 1));
        assertThat(users.getSize(), is(1));
        assertThat(users.hasPreviousPage(), is(false));
        assertThat(users.getTotalElements(), is(2L));
    }


    /**
     * Flushes test users to the database.
     */
    private void flushTestUsers() {

        firstUser = userDao.save(firstUser);
        secondUser = userDao.save(secondUser);
        thirdUser = userDao.save(thirdUser);

        userDao.flush();

        id = firstUser.getId();

        assertThat(id, is(notNullValue()));
        assertThat(secondUser.getId(), is(notNullValue()));
        assertThat(thirdUser.getId(), is(notNullValue()));

        assertThat(userDao.exists(id), is(true));
        assertThat(userDao.exists(secondUser.getId()), is(true));
        assertThat(userDao.exists(thirdUser.getId()), is(true));
    }


    @Test
    public void executesMethodWithNamedParametersCorrectly() throws Exception {

        firstUser = userDao.save(firstUser);
        secondUser = userDao.save(secondUser);

        assertTrue(userDao.findByLastnameOrFirstname("Oliver", "Arrasz")
                .containsAll(Arrays.asList(firstUser, secondUser)));
    }


    @Test
    public void executesMethodWithNamedParametersCorrectlyOnMethodsWithQueryCreation()
            throws Exception {

        firstUser = userDao.save(firstUser);
        secondUser = userDao.save(secondUser);

        assertTrue(userDao.findByFirstnameOrLastname("Oliver", "Arrasz")
                .containsAll(Arrays.asList(firstUser, secondUser)));
    }


    @Test
    public void executesLikeAndOrderByCorrectly() throws Exception {

        flushTestUsers();

        List<User> result =
                userDao.findByLastnameLikeOrderByFirstnameDesc("%r%");
        assertEquals(firstUser, result.get(0));
        assertEquals(secondUser, result.get(1));
    }


    @Test
    public void executesNotLikeCorrectly() throws Exception {

        flushTestUsers();

        List<User> result = userDao.findByLastnameNotLike("%er%");
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(secondUser, thirdUser));
    }


    @Test
    public void executesSimpleNotCorrectly() throws Exception {

        flushTestUsers();

        List<User> result = userDao.findByLastnameNot("Gierke");
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(secondUser, thirdUser));
    }


    @Test
    public void returnsSameListIfNoSpecGiven() throws Exception {

        flushTestUsers();
        assertSameElements(userDao.readAll(),
                userDao.readAll((Specification<User>) null));
    }


    @Test
    public void returnsSameListIfNoSortIsGiven() throws Exception {

        flushTestUsers();
        assertSameElements(userDao.readAll((Sort) null), userDao.readAll());
    }


    @Test
    public void returnsSamePageIfNoSpecGiven() throws Exception {

        Pageable pageable = new PageRequest(0, 1);

        flushTestUsers();
        assertEquals(userDao.readAll(pageable), userDao.readAll(null, pageable));
    }


    @Test
    public void returnsAllAsPageIfNoPageableIsGiven() throws Exception {

        flushTestUsers();
        assertEquals(new PageImpl<User>(userDao.readAll()),
                userDao.readAll((Pageable) null));
    }


    private static <T> void assertSameElements(Collection<T> first,
            Collection<T> second) {

        for (T element : first) {
            assertThat(element, isIn(second));
        }

        for (T element : second) {
            assertThat(element, isIn(first));
        }
    }


    @Test
    public void removeDetachedObject() throws Exception {

        flushTestUsers();

        em.detach(firstUser);
        userDao.delete(firstUser);

        assertThat(userDao.count(), is(2L));
    }


    @Test
    public void executesPagedSpecificationsCorrectly() throws Exception {

        Page<User> result = executeSpecWithSort(null);
        assertThat(result.asList(),
                anyOf(hasItem(firstUser), hasItem(thirdUser)));
        assertThat(result.asList(), not(hasItem(secondUser)));
    }


    @Test
    public void executesPagedSpecificationsWithSortCorrectly() throws Exception {

        Page<User> result =
                executeSpecWithSort(new Sort(Order.ASCENDING, "lastname"));

        assertThat(result.asList(), hasItem(firstUser));
        assertThat(result.asList(), not(hasItem(secondUser)));
        assertThat(result.asList(), not(hasItem(thirdUser)));
    }


    @Test
    public void executesPagedSpecificationWithSortCorrectly2() throws Exception {

        Page<User> result =
                executeSpecWithSort(new Sort(Order.DESCENDING, "lastname"));

        assertThat(result.asList(), hasItem(thirdUser));
        assertThat(result.asList(), not(hasItem(secondUser)));
        assertThat(result.asList(), not(hasItem(firstUser)));
    }


    private Page<User> executeSpecWithSort(Sort sort) {

        flushTestUsers();

        Specification<User> spec =
                where(userHasFirstname("Oliver")).or(
                        userHasLastname("Matthews"));

        Page<User> result = userDao.readAll(spec, new PageRequest(0, 1, sort));
        assertThat(result.getTotalElements(), is(2L));
        return result;
    }
}
