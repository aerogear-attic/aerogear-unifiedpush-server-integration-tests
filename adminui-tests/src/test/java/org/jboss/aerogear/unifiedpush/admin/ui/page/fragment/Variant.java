package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Variant {

    @Root
    private WebElement root;

    @FindByNg(action = "expand(variant)")
    private WebElement name;

    @FindByJQuery("span:eq(0)")
    private WebElement description;

    @FindByJQuery("span:eq(1) span:visible")
    private WebElement installations;

    @FindByJQuery(".dropdown-toggle")
    private WebElement dropdownToggle;

    @FindByNg(action = "editVariant(variant, type)")
    private WebElement editButton;

    @FindByNg(action = "removeVariant(variant, type)")
    private WebElement removeButton;

    @FindByJQuery(".ups-toggle-box")
    private WebElement details;

    @FindByJQuery(".ups-tip-panel tr:contains('Variant ID') td:last")
    private WebElement id;

    @FindByJQuery(".ups-tip-panel tr:contains('Secret') td:last")
    private WebElement secret;

    @ArquillianResource
    private JavascriptExecutor js;


    public String getName() {
        return name.getText();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getId() {
        return id.getText();
    }

    public String getSecret() {
        return secret.getText();
    }

    public int getInstallationCount() {
        return StringUtilities.parseCount(installations.getText());
    }

    public void showDetails() {
        if (!details.isDisplayed()) {
            name.click();
        }
    }

    public void edit() {
        dropdownToggle.click();
        editButton.click();
    }

    public void remove() {
        dropdownToggle.click();
        removeButton.click();
    }

    public String getType() {
        return (String) js.executeScript("return arguments[0].parentNode.parentNode.getAttribute('type')", root);
    }

}
