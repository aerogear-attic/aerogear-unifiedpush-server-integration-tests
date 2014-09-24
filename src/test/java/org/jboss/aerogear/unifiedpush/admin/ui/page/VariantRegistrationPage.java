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

import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.AndroidVariantForm;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.IOSVariantForm;
import org.jboss.aerogear.unifiedpush.admin.ui.page.fragment.ModalDialog;
import org.jboss.aerogear.unifiedpush.admin.ui.utils.IOSVariantCreator;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;

public class VariantRegistrationPage {

    @FindByJQuery(".modal [ng-show='variant.type == \'android\'']")
    private AndroidVariantForm androidForm;

    @FindByJQuery(".modal [ng-show='variant.type == \'ios\'']")
    private IOSVariantForm iOSForm;

    @FindBy(id = "variantTypeAndroid")
    private WebElement radioButtonAndroid;

    @FindBy(id = "variantTypeChrome")
    private WebElement radioButtonChrome;

    @FindBy(id = "variantTypeIOS")
    private WebElement radioButtonApple;

    @FindBy(id = "variantTypeSimplePush")
    private WebElement radioButtonSimplePush;

    @FindByJQuery(".modal-dialog")
    private ModalDialog modalDialog;

    @Drone
    private WebDriver driver;

    public void registerAndroidVariant(String name, String desc, String projectNumber, String googleApiKey) {
        fillVariantDetails(name, desc);
        radioButtonAndroid.click();
        androidForm.setApiKey(googleApiKey);
        androidForm.setProjectNumber(projectNumber);
        modalDialog.ok();
    }

    public void registeriOSVariant(String name, String desc, String appleCertPath, String passphrase, boolean isProd) {

        File cert = new File(appleCertPath);
        fillVariantDetails(name, desc);
        radioButtonApple.click();
        iOSForm.setCertificate(cert.getAbsolutePath());
        iOSForm.setPassphrase(passphrase);
        if (isProd) {
            iOSForm.setProduction();
        } else {
            iOSForm.setDevelopment();
        }

        modalDialog.ok();
    }

    public void registerSimplePushVariant(String name, String desc) {
        fillVariantDetails(name, desc);
        radioButtonSimplePush.click();
        modalDialog.ok();
    }

    private void fillVariantDetails(String name, String desc) {
        modalDialog.setName(name);
        modalDialog.setDescription(desc);
    }

}
