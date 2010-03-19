package org.synyx.hades.dao.orm;

import static org.mockito.Mockito.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.synyx.hades.dao.config.AbstractDaoConfigIntegrationTest;


/**
 * Assures the injected DAO instances are wired to the customly configured
 * {@link EntityManagerFactory}.
 * 
 * @author Oliver Gierke - gierke@synyx.de
 */
@ContextConfiguration(locations = "classpath:multiple-entity-manager-context.xml")
public class EntityManagerFactoryRefIntegrationTest extends
        AbstractDaoConfigIntegrationTest {

    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory first;

    @Autowired
    @Qualifier("secondEntityManagerFactory")
    private EntityManagerFactory second;


    @Test
    public void daosGetTheSecondEntityManagerFactoryInjected() throws Exception {

        verify(first.createEntityManager(), never());
        verify(second.createEntityManager(), atLeastOnce());
    }

    /**
     * A simple No-Op {@link PersistenceExceptionTranslator} to be configured in
     * the test case's config file as it is required.
     * 
     * @author Oliver Gierke - gierke@synyx.de
     */
    static class NoOpPersistenceExceptionTranslator implements
            PersistenceExceptionTranslator {

        public DataAccessException translateExceptionIfPossible(
                RuntimeException ex) {

            return null;
        }
    }

    /**
     * {@link BeanPostProcessor} to configure the mock
     * {@link EntityManagerFactory} instances. {@code entityManagerFactory} is
     * configured to be never invoked, {@code secondEntityManagerFactory} is
     * configured to be invoked at least once.
     * 
     * @author Oliver Gierke - gierke@synyx.de
     */
    static class MockPreparingBeanPostProcessor implements BeanPostProcessor {

        public Object postProcessAfterInitialization(Object bean,
                String beanName) throws BeansException {

            if ("secondEntityManagerFactory".equals(beanName)) {

                HibernateEntityManagerFactory entityManagerFactory =
                        (HibernateEntityManagerFactory) bean;

                when(entityManagerFactory.createEntityManager()).thenReturn(
                        mock(EntityManager.class));
            }

            return bean;
        }


        public Object postProcessBeforeInitialization(Object bean,
                String beanName) throws BeansException {

            return bean;
        }
    }
}
