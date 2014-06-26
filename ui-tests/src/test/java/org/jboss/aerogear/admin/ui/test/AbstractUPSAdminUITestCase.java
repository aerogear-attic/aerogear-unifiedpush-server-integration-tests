/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.admin.ui.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractUPSAdminUITestCase {

    // @ArquillianResource
    // protected URL contextRoot;

    @Drone
    protected WebDriver driver;

    // utility deployment class should go into "common" module since
    // ui tests and integration tests would deploy stuff by the same way

    @Deployment(testable = false, order = 1, name = "authServer")
    public static WebArchive getAuthServerDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(Foo.class);
    }

    @Deployment(testable = false, order = 2, name = "upsServer")
    public static WebArchive getUpsServerDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(Foo.class);
    }

    private static final class Foo {

    }
}
