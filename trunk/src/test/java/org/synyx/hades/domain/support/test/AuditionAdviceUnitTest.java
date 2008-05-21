package org.synyx.hades.domain.support.test;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.synyx.hades.domain.AuditableUser;
import org.synyx.hades.domain.support.AuditionAdvice;
import org.synyx.hades.domain.support.AuditorAware;


/**
 * Unit test for {@code AuditionAdvice}.
 * 
 * @author Oliver Gierke - gierke@synyx.de
 */
@SuppressWarnings("unchecked")
public class AuditionAdviceUnitTest extends TestCase {

    private AuditionAdvice auditionAdvice;
    private AuditorAware<AuditableUser, Long> auditorAware;

    private AuditableUser user;


    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {

        auditionAdvice = new AuditionAdvice();

        user = new AuditableUser();

        auditorAware = EasyMock.createNiceMock(AuditorAware.class);
        EasyMock.expect(auditorAware.getCurrentAuditor()).andReturn(user)
                .once();
    }


    /**
     * Checks that the advice does not set auditor on the target entity if no
     * {@code AuditorAware} was configured.
     * 
     * @throws Exception
     */
    public void testDoesNotSetAuditorIfNotConfigured() throws Exception {

        auditionAdvice.touch(user);

        assertNotNull(user.getCreatedDate());
        assertNotNull(user.getLastModifiedDate());

        assertNull(user.getCreatedBy());
        assertNull(user.getLastModifiedBy());
    }


    /**
     * Checks that the advice sets the auditor on the target entity if an
     * {@code AuditorAware} was configured.
     * 
     * @throws Exception
     */
    public void testSetsAuditorIfConfigured() {

        auditionAdvice.setAuditorAware(auditorAware);
        EasyMock.replay(auditorAware);

        auditionAdvice.touch(user);

        assertNotNull(user.getCreatedDate());
        assertNotNull(user.getLastModifiedDate());

        assertNotNull(user.getCreatedBy());
        assertNotNull(user.getLastModifiedBy());

        EasyMock.verify(auditorAware);
    }


    /**
     * Checks that the advice does not set modification information on creation
     * if the falg is set to {@code false}.
     * 
     * @throws Exception
     */
    public void testHonoursModifiedOnCreationFlag() throws Exception {

        auditionAdvice.setAuditorAware(auditorAware);
        EasyMock.replay(auditorAware);

        auditionAdvice.setModifyOnCreation(false);
        auditionAdvice.touch(user);

        assertNotNull(user.getCreatedDate());
        assertNotNull(user.getCreatedBy());

        assertNull(user.getLastModifiedBy());
        assertNull(user.getLastModifiedDate());

        EasyMock.verify(auditorAware);
    }


    /**
     * Tests that the advice only sets modification data if a not-new entity is
     * handled.
     */
    public void testOnlySetsModificationDataOnNotNewEntities() {

        user.setId(1L);

        auditionAdvice.setAuditorAware(auditorAware);
        EasyMock.replay(auditorAware);

        auditionAdvice.touch(user);

        assertNull(user.getCreatedBy());
        assertNull(user.getCreatedDate());

        assertNotNull(user.getLastModifiedBy());
        assertNotNull(user.getLastModifiedDate());

        EasyMock.verify(auditorAware);
    }
}
