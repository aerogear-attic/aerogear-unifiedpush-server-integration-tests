package org.jboss.aerogear.unifiedpush.rest.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.users.Developer;
import org.jboss.aerogear.unifiedpush.utils.AdminUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

public class AdminEndpointTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return Constants.INSECURE_AG_PUSH_ENDPOINT;
    }

    public static AuthenticationUtils.Session session;
    public static Developer developer;

    @Deprecated
    public static Map<String, ?> getAuthCookies() {
        return session.getCookies();
    }

    @Test
    @InSequence(1)
    public void authenticate() {
        session = AuthenticationUtils.completeDefaultLogin(getContextRoot());
        assertNotNull(session);
        assertTrue(session.isValid());
    }

    @Test
    @InSequence(2)
    public void enroll() {
        developer = AdminUtils.generateAndEnroll(session);

        assertNotNull(developer);
    }

    @Test
    @InSequence(3)
    public void loginWithNewAccount() {
        String newPassword = UUID.randomUUID().toString();

        AuthenticationUtils.Session developerSession = AuthenticationUtils.completeLogin(developer.getLoginName(),
                developer.getPassword(), newPassword, getContextRoot());

        assertNotNull(developerSession);
        assertTrue(developerSession.isValid());

        developer.setNewPassword(newPassword);
    }
}
