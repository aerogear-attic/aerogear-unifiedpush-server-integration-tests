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
package org.jboss.aerogear.unifiedpush.admin.ui.page;

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.Application;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PushAppsPage {

    @FindByNg(action = "open()")
    private WebElement createButton;

    @FindByNg(repeat = "application in applications")
    private List<Application> applicationList;

    @FindByNg(repeat = "application in applications")
    private WebElement application;

    private final static String PAGE_URL = "/ag-push/#/applications";

    public String getPageURL() {
        return PAGE_URL;
    }

    public int countApplications() {
        return applicationList.size();
    }

    public void pressCreateButton() {
        createButton.click();
    }

    public boolean applicationExists(String name, String desc) {
        for (Application app : applicationList) {
            if (app.getName().equals(name) && app.getDescription().equals(desc)) {
                return true;
            }
        }
        return false;
    }

    public List<Application> getApplicationList() {
        return applicationList;
    }

    public Application findApplication(String name) {
        for (Application app : applicationList) {
            if (app.getName().equals(name)) {
                return app;
            }
        }
        return null;
    }

    public void waitForPage() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
