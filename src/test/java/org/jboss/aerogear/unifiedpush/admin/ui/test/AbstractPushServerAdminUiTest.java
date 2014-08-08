/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.admin.ui.test;

import java.net.URL;

import org.jboss.aerogear.unifiedpush.test.Deployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import category.AdminUI;

@RunWith(Arquillian.class)
@Category(AdminUI.class)
public abstract class AbstractPushServerAdminUiTest {

    /**
     * The context root.
     */
    @ArquillianResource
    @OperateOnDeployment(Deployments.AG_PUSH)
    protected URL contextRoot;

    /**
     * The browser instance.
     */
    @Drone
    protected WebDriver driver;

    @Deployment(name = Deployments.AUTH_SERVER, testable = false, order = 1)
    @TargetsContainer("main-server-group")
    public static WebArchive createAuthServerDeployment() {
        return Deployments.authServer();
    }

    @Deployment(name = Deployments.AG_PUSH, testable = false, order = 2)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    /**
     * Initializes the page URL.
     */
    public void initializePageUrl() {
        try {
            driver.get(new URL(contextRoot, getPagePath()).toExternalForm());
        } catch (final Exception ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * Navigates to page.
     */
    public void navigateToURL(String page) {
        try {
            driver.get(new URL(contextRoot, page).toExternalForm());
        } catch (final Exception ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * The abstract method whose implementation defines which page will be initialized.
     *
     * @return A string which specifies the path.
     */
    protected abstract String getPagePath();
}
