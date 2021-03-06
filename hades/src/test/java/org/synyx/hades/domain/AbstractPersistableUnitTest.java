package org.synyx.hades.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for {@link AbstractPersistable}.
 * 
 * @author Oliver Gierke
 */
public class AbstractPersistableUnitTest {

    private SampleUser user;


    @Before
    public void setUp() {

        this.user = new SampleUser(null);
    }


    /**
     * Asserts that {@link #equals(Object)} returns true for self references and
     * other references of the exact same class and id and {@code false}
     * otherwise.
     * 
     * @throws Exception
     */
    @Test
    public void correctEquals() throws Exception {

        // Selfreference
        assertEquals(user, user);

        // Reference with null id
        assertNotEquals(user, new SampleUser(null));

        // Reference with same id
        user.setId(1L);
        assertEquals(user, new SampleUser(1L));

        // Inequality on subclasses
        assertNotEquals(user, new SampleUser(1L) {

            private static final long serialVersionUID = 7716397091688803365L;
        });

        // Another class with same id
        assertNotEquals(user, new SampleRole(1L));
    }


    /**
     * Asserts that {@link #equals(Object)} being invoked on the objects returns
     * {@code false}.
     * 
     * @param expected
     * @param given
     */
    private void assertNotEquals(Object expected, Object given) {

        assertFalse(expected.equals(given));
    }

    /**
     * Sample user class.
     * 
     * @author Oliver Gierke
     */
    private class SampleUser extends AbstractPersistable<Long> {

        private static final long serialVersionUID = 4088038726522998404L;


        /**
         * Creates a new {@link SampleUser}.
         * 
         * @param id
         */
        public SampleUser(Long id) {

            this.setId(id);
        }
    }

    /**
     * Sample role class
     * 
     * @author Oliver Gierke
     */
    private class SampleRole extends AbstractPersistable<Long> {

        private static final long serialVersionUID = 1L;


        /**
         * Creates a new {@link SampleRole}.
         * 
         * @param id
         */
        public SampleRole(Long id) {

            this.setId(id);
        }
    }
}
