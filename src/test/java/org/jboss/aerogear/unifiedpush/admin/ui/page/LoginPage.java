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

import static org.jboss.aerogear.unifiedpush.admin.ui.utils.WebElementUtils.clearNfill;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage {

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "kc-login")
    private WebElement submitButton;

    @FindBy(id = "kc-header")
    private WebElement headerTitle;

    private final static String TITLE = "LOG IN TO UNIFIEDPUSH SERVER";

    private final static String PAGE_URL = "#/login";

    public String getHeaderTitle() {
        return headerTitle.getText();
    }

    public String getExpectedTitle() {
        return TITLE;
    }

    public void login(String username, String password) {
        clearNfill(usernameField, username);
        clearNfill(passwordField, password);
        submitButton.click();
    }

    public void waitUntilPageIsLoaded() {
        waitModel().until().element(submitButton).is().visible();
    }

    public String getPageURL() {
        return PAGE_URL;
    }
}
