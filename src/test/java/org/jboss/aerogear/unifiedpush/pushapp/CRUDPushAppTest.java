/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.pushapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

public class CRUDPushAppTest extends GenericSimpleUnifiedPushTest {

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }

    private static Session session;
    private static PushApplication registeredPushApplication;

    @Test
    @InSequence(1)
    public void authenticate() {
        assertNotNull(getContextRoot());
        session = AuthenticationUtils.completeDefaultLogin(getContextRoot());
        assertNotNull(session);
        assertTrue(session.isValid());
    }

    @Test
    @InSequence(2)
    public void registeringPushApplication() {
        registeredPushApplication = PushApplicationUtils.generateAndRegister(session);
        assertNotNull(registeredPushApplication);
    }

    @Test
    @InSequence(3)
    public void retrievePushApplications() {
        List<PushApplication> pushApplications = PushApplicationUtils.listAll(session);

        assertNotNull(pushApplications);
        assertEquals(1, pushApplications.size());
        PushApplicationUtils.checkEquality(registeredPushApplication, pushApplications.get(0));
    }

    @Test
    @InSequence(4)
    public void retrieveRegisteredApplication() {
        PushApplication pushApplication = PushApplicationUtils.findById(registeredPushApplication.getPushApplicationID(),
                session);

        assertNotNull(pushApplication);
        PushApplicationUtils.checkEquality(registeredPushApplication, pushApplication);
    }

    @Test
    @InSequence(5)
    public void updatePushApplication() {
        // Let the PushApplicationUtils generate name and description
        PushApplication pushApplication = PushApplicationUtils.generate();

        // Use the generated values
        registeredPushApplication.setName(pushApplication.getName());
        registeredPushApplication.setDescription(pushApplication.getDescription());

        PushApplicationUtils.update(registeredPushApplication, session);
    }

    @Test
    @InSequence(6)
    public void retrieveUpdatedApplication() {
        PushApplication pushApplication = PushApplicationUtils.findById(registeredPushApplication.getPushApplicationID(),
                session);

        assertNotNull(pushApplication);
        PushApplicationUtils.checkEquality(registeredPushApplication, pushApplication);
    }

    @Test
    @InSequence(7)
    public void deleteRegisteredApplication() {
        PushApplicationUtils.delete(registeredPushApplication, session);
    }
}
