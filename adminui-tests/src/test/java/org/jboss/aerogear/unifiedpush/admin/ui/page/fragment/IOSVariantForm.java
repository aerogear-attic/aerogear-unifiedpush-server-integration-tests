package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class IOSVariantForm {

    @FindByJQuery("input[type='file']")
    private WebElement certificateInput;

    @FindBy(id = "iosPassphrase")
    private WebElement passphraseInput;

    @FindBy(id = "iosType1")
    private WebElement productionRadio;

    @FindBy(id = "iosType2")
    private WebElement developmentRadio;

    public void setProduction() {
        productionRadio.click();
    }

    public void setDevelopment() {
        developmentRadio.click();
    }

    public void setCertificate(String certificate) {
        certificateInput.sendKeys(certificate);
    }

    public void setPassphrase(String passphrase) {
        passphraseInput.clear();
        passphraseInput.sendKeys(passphrase);
    }

    public boolean isProduction() {
        return productionRadio.isSelected();
    }

    public boolean isDevelopment() {
        return developmentRadio.isSelected();
    }
}
