package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Switch {

    @FindBy(tagName = "label")
    private WebElement label;

    @FindBy(tagName = "input")
    private WebElement input;

    public boolean isOn() {
        return input.isSelected();
    }

    public void turnOn() {
        if (!isOn()) label.click();
    }

    public void turnOff() {
        if (isOn()) label.click();
    }
}
