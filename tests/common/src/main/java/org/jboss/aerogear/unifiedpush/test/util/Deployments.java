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
package org.jboss.aerogear.unifiedpush.test.util;


import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public final class Deployments {

    public static final String AG_PUSH = "ag_push";
    public static final String AUTH_SERVER = "auth_server";
    public static final String TEST_EXTENSION = "test_extension";

    private Deployments() {
        throw new UnsupportedOperationException("No instantiation.");
    }

    public static WebArchive testExtension() {
        return ShrinkWrap.create(WebArchive.class, "unifiedpush-test-extension-server.war");
    }

    public static WebArchive unifiedPushServer() {
        return ShrinkWrap.create(WebArchive.class, "ag-push.war");
    }

    public static WebArchive authServer() {
        return ShrinkWrap.create(WebArchive.class, "auth-server.war");
    }

}
