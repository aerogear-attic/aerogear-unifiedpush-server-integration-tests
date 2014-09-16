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

import java.util.ArrayList;
import java.util.List;

public class VariantDetails {

    @FindByJQuery("div.content section h2")
    private WebElement headerTitle;

    @FindByJQuery("div.content section input.rcue-code:eq(0)")
    private WebElement variantId;

    @FindByJQuery("div.content section input.rcue-code:eq(1)")
    private WebElement secret;

    @FindByJQuery("div#mobile-application-variant-table")
    private WebElement mobileInstallationsTableContainer;

    @FindByJQuery("div#mobile-application-variant-table table.rcue-table tbody tr")
    private List<WebElement> mobileInstallationRows;

    @FindByJQuery("div.content div:eq(1) a:eq(0)")
    private WebElement breadcrumbPushAppsLink;

    @FindByJQuery("div.content div:eq(1) a:eq(1)")
    private WebElement breadcrumbVariantsLink;

    public void navigateToPushAppsPage() {
        breadcrumbPushAppsLink.click();
    }

    public void navigateToVariantsPage() {
        breadcrumbVariantsLink.click();
    }

    public String getHeaderTitle() {
        return headerTitle.getText();
    }

    public String getVariantId() {
        return variantId.getAttribute("value");
    }

    public String getSecret() {
        return secret.getAttribute("value");
    }

    public void pressInstallationLink(int rowNum) {
        final List<WebElement> anchors = mobileInstallationRows.get(rowNum).findElements(By.tagName("a"));
        anchors.get(0).click();
    }

    public int findInstallationRow(String token) {
        final List<Installation> installationsList = getInstallationList();
        if (token != null && installationsList != null && !installationsList.isEmpty()) {
            for (int i = 0; i < installationsList.size(); i++) {
                if (token.equals(installationsList.get(i).getDeviceToken())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public List<Installation> getInstallationList() {
        final List<Installation> installationList = new ArrayList<Installation>();
        for (WebElement row : mobileInstallationRows) {
            final List<WebElement> tableDataList = row.findElements(By.tagName("td"));
            if (tableDataList.size() == 4) {
                final String token = tableDataList.get(0).getText();
                final String device = tableDataList.get(1).getText();
                final String platform = tableDataList.get(2).getText();
                final String status = tableDataList.get(3).getText();
                installationList.add(new Installation(token, device, null, null, platform, status, null, null));
            }
        }
        return installationList;
    }

    public boolean tokenIdExistsInList(String tokenId, List<Installation> list) {
        if (tokenId != null && list != null && !list.isEmpty()) {
            for (Installation installation : list) {
                if (tokenId.equals(installation.getDeviceToken())) {
                    return true;
                }
            }
        }
        return false;
    }

}
