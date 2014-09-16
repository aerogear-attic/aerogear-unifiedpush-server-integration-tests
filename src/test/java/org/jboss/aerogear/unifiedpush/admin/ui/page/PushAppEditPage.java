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

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.ModalDialog;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities.isEmpty;
import static org.jboss.aerogear.unifiedpush.admin.ui.utils.WebElementUtils.clearNfill;

public class PushAppEditPage {

    @FindBy(tagName = "form")
    private WebElement form;

    @FindByJQuery(".modal")
    private ModalDialog modalDialog;

    @FindBy(id = "createAppName")
    private WebElement nameField;

    @FindBy(id = "createAppDescription")
    private WebElement descriptionField;

    private static final String PAGE_URL = "/ag-push/#/applications";

    public String getPageURL() {
        return PAGE_URL;
    }

    public String getName() {
        return nameField.getAttribute("value");
    }

    public String getDescription() {
        return descriptionField.getAttribute("value");
    }

    public void registerNewPushApp(String name, String desc) {
        clearNfill(nameField, name);
        clearNfill(descriptionField, desc);
        modalDialog.ok();
    }

    public void updatePushApp(String name, String desc) {
        if (!isEmpty(name)) {
            clearNfill(nameField, name);
        }
        if (!isEmpty(desc)) {
            clearNfill(descriptionField, desc);
        }
        modalDialog.ok();
    }

    public void cancel() {
        modalDialog.cancel();
    }
}
