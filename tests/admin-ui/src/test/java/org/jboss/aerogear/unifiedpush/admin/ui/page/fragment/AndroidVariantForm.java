package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AndroidVariantForm {

    @FindBy(id = "gcmApiKey")
    private WebElement googleApiKeyInputField;

    @FindBy(id = "gcmProjectNumber")
    private WebElement androidProjectNumber;

    public String getApiKey() {
        return googleApiKeyInputField.getAttribute("value");
    }

    public String getProjectNumber() {
        return androidProjectNumber.getAttribute("value");
    }

    public void setApiKey(String apiKey) {
        googleApiKeyInputField.clear();
        googleApiKeyInputField.sendKeys(apiKey);
    }

    public void setProjectNumber(String projectNumber) {
        androidProjectNumber.clear();
        androidProjectNumber.sendKeys(projectNumber);
    }
}
