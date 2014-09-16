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

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

public class ConfirmationBoxPage {

    @FindByJQuery("input[type=\"text\"]")
    private WebElement NAME_FIELD;

    @FindByJQuery("button.topcoat-button")
    private WebElement CANCEL_BUTTON;

    @FindByJQuery("button.topcoat-button--cta")
    private WebElement REMOVE_APP_BUTTON;

    @FindByJQuery("#deleteVariantButton")
    private WebElement REMOVE_VARIANT_BUTTON;

    public void remove(String name) {
        Graphene.waitModel().pollingEvery(1, TimeUnit.SECONDS).until().element(NAME_FIELD).is().visible();
        NAME_FIELD.clear();
        NAME_FIELD.sendKeys(name);
    }

    public void clickRemoveAppButton() {
        REMOVE_APP_BUTTON.click();
    }

    public void clickRemoveVariantButton() {
        REMOVE_VARIANT_BUTTON.click();
    }

    public void clickCancelButton() {
        Graphene.waitModel().pollingEvery(1, TimeUnit.SECONDS).until().element(CANCEL_BUTTON).is().visible();
        CANCEL_BUTTON.click();
    }
}
