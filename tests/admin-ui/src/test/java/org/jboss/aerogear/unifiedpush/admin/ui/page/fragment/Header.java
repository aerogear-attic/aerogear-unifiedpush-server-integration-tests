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
package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class Header {

    @FindByJQuery(".dropdown")
    private WebElement dropdownWebElement;

    @FindByJQuery(".dropdown")
    private Dropdown dropdown;

    public void logout() {
        waitModel().until().element(dropdownWebElement).is().present();
        dropdown.click("Log Out");
    }

    public void accountManagement() {
        waitModel().until().element(dropdownWebElement).is().visible();
        dropdown.click("Account Management");
    }
}
