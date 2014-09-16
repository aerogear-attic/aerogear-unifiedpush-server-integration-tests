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

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.aerogear.unifiedpush.admin.ui.utils.WebElementUtils.clearNfill;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class PasswordChangePage {

    @FindBy(id = "password-new")
    private WebElement passwordField;

    @FindBy(id = "password-confirm")
    private WebElement confirmPasswordField;

    @FindByJQuery("[type='submit']")
    private WebElement submitButton;

    public void changePassword(String password) {
        clearNfill(passwordField, password);
        clearNfill(confirmPasswordField, password);
        submitButton.click();
    }

    public void waitUntilPageIsLoaded() {
        waitModel().until().element(submitButton).is().visible();
    }
}
