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

import java.util.HashMap;

import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.test.GenericSimpleUnifiedPushTest;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class RegisterPushAppWithoutLoginTest extends GenericSimpleUnifiedPushTest {

    @Test
    public void registeringPushApplicationWithoutBeingLogged() {
        assertNotNull(getContextRoot());
        String pushAppName = "My App";
        String pushAppDesc = "Awesome App";
        PushApplication pushApp = PushApplicationUtils.createPushApplication(pushAppName, pushAppDesc, null, null, null);
        Response response = PushApplicationUtils.registerPushApplication(pushApp, new HashMap<String, String>(), null,
                getContextRoot());

        assertNotNull(response);
        assertEquals(response.getStatusCode(), Status.UNAUTHORIZED.getStatusCode());
    }

    @Override
    protected String getContextRoot() {
        return root.toExternalForm();
    }
}
