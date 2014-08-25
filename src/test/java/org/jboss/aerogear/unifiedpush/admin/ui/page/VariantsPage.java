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

import java.util.List;

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.Variant;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class VariantsPage {

    @FindByNg(action = "addVariant()")
    private WebElement addVariantButton;

    @FindByJQuery(".breadcrumb li:last span")
    private WebElement headerTitle;

    @FindByJQuery("table tr:contains('Application ID') td:last input")
    private WebElement applicationId;

    @FindByJQuery("table tr:contains('Master Secret') td:last input")
    private WebElement masterSecret;

    @FindByNg(repeat = "variant in application.variants")
    private List<Variant> variantsList;

    @FindByJQuery(".breadcrumb a")
    private WebElement backToPushAppsLink;

    public void navigateToPushAppsPage() {
        backToPushAppsLink.click();
    }

    public void addVariant() {
        addVariantButton.click();
    }

    public String getHeaderTitle() {
        return headerTitle.getText();
    }

    public String getApplicationId() {
        return applicationId.getAttribute("value");
    }

    public String getMasterSecret() {
        return masterSecret.getAttribute("value");
    }

    public int countVariants() {
        return variantsList.size();
    }

    public List<Variant> getVariantList() {
        return variantsList;
    }

    public Variant findVariantRow(String name) {
        for (Variant variant : variantsList) {
            if (variant.getName().equals(name)) {
                return variant;
            }
        }
        return null;
    }

}
