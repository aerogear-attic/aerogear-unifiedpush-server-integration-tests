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

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.ModalDialog;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class VariantEditPage {

    @FindBy(id = "createAppName")
    private WebElement variantName;

    @FindBy(id = "createAppDescription")
    private WebElement variantDesc;

    @FindByJQuery(".modal")
    private ModalDialog modalDialog;

    public void fillVariantDetails(String name, String desc) {
        clearNfill(variantName, name);
        clearNfill(variantDesc, desc);
    }

    public String getName() {
        return variantName.getAttribute("value");
    }

    public String getDescription() {
        return variantDesc.getAttribute("value");
    }

    public void submitForm() {
        modalDialog.ok();
    }

    public void cancel() {
        modalDialog.cancel();
    }

    protected abstract void updateVariant(String... input);
}
