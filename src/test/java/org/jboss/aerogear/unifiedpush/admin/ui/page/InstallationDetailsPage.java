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

import org.jboss.aerogear.unifiedpush.admin.ui.model.Installation;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class InstallationDetailsPage {

    @FindByJQuery("div.content section h3")
    private WebElement headerTitle;

    @FindByJQuery("div.rcue-code.token.network-details")
    private WebElement token;

    @FindByJQuery("table.rcue-table")
    private WebElement installationDetailsTable;

    @FindByJQuery("table.rcue-table tbody tr")
    private WebElement installationDetails;

    @FindByJQuery("table.rcue-table tbody tr:eq(0) td:eq(4)")
    private WebElement statusField;

    @FindByJQuery("div.content div:eq(0) a:eq(1)")
    private WebElement breadcrumbVariantsLink;

    @FindByJQuery("div.content div:eq(0) a:eq(2)")
    private WebElement breadcrumbVariantLink;

    public void navigateToVariantPage() {
        breadcrumbVariantLink.click();
    }

    public void navigateToVariantsPage() {
        breadcrumbVariantsLink.click();
    }

    public void pressToggleLink() {
        final WebElement toggleAnchor = installationDetails.findElement(By.tagName("a"));
        toggleAnchor.click();
        waitModel().pollingEvery(1, TimeUnit.SECONDS).until().element(statusField).text().contains("Disabled");
    }

    public Installation getInstallationDetails() {
        Installation installation = null;
        final List<WebElement> tableDataList = installationDetails.findElements(By.tagName("td"));
        if (tableDataList.size() == 6) {
            final String alias = tableDataList.get(0).getText();
            final String category = tableDataList.get(1).getText();
            final String deviceType = tableDataList.get(2).getText();
            final String platform = tableDataList.get(3).getText();
            final String status = tableDataList.get(4).getText();
            installation = new Installation(getToken(), deviceType, null, alias, platform, status, null, category);
        }

        return installation;
    }

    public String getToken() {
        return token.getText().substring("Device Token: ".length()).trim();
    }

    public String getHeaderTitle() {
        return headerTitle.getText();
    }

    private List<WebElement> filterInstallationRows() {
        return null;
    }
}
