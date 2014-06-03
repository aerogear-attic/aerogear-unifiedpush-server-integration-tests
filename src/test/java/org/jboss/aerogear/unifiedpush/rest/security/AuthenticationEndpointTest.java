/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.CheckingExpectedException;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

// Ignored because this is now a Keycloak functionality (FIXME or is it?)
@Ignore
public class AuthenticationEndpointTest extends GenericSimpleUnifiedPushTest {

    private static Session session;
    @Rule
    public CheckingExpectedException thrown = CheckingExpectedException.none();

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    @Test
    @InSequence(1)
    public void loginDefaultCredentialsLeadsTo403() {
        thrown.expect(AuthenticationUtils.ExpiredPasswordException.class);
        AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(),
            AuthenticationUtils.getAdminOldPassword(), getContextRoot());
    }

    @Test
    @InSequence(2)
    public void updatePasswordUsingWrongOldPasswordLeadsTo401() {
        String wrongOldPassword = UUID.randomUUID().toString();
        thrown.expect(AuthenticationUtils.InvalidPasswordException.class);

        AuthenticationUtils.changePassword(AuthenticationUtils.getAdminLoginName(), wrongOldPassword,
            AuthenticationUtils.getAdminNewPassword(), getContextRoot());
    }

    @Test
    @InSequence(3)
    public void updatePasswordLeadsTo200() {
        boolean passwordChanged = AuthenticationUtils.changePassword(AuthenticationUtils.getAdminLoginName(),
            AuthenticationUtils.getAdminOldPassword(), AuthenticationUtils.getAdminNewPassword(), getContextRoot());

        assertTrue(passwordChanged);
    }

    @Test
    @InSequence(4)
    public void incorrectLoginLeadsTo401() {
        String wrongPassword = UUID.randomUUID().toString();
        thrown.expect(AuthenticationUtils.InvalidPasswordException.class);
        AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(),
            wrongPassword, getContextRoot());
    }

    @Test
    @InSequence(5)
    public void properLoginLeadsTo200() {
        session = AuthenticationUtils.login(AuthenticationUtils.getAdminLoginName(),
            AuthenticationUtils.getAdminNewPassword(), getContextRoot());

        assertNotNull(session);
        assertTrue(session.isValid());
    }

    @Test
    @InSequence(6)
    public void logoutLeadsTo200() {
        AuthenticationUtils.logout(session);
        assertFalse(session.isValid());
        assertThat(session.getCookies().entrySet(), Matchers.is(Matchers.empty()));
        assertNull(session.getBaseUrl());
    }

    @Test
    @InSequence(7)
    public void logoutWithoutBeingLoggedInLeadsTo401() {
        thrown.expect(IllegalStateException.class);
        AuthenticationUtils.logout(Session.forceCreateValidWithEmptyCookies(getContextRoot()));
    }
}
