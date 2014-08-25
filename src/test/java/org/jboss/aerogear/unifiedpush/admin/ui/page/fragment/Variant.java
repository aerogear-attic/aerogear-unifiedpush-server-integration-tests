package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class Variant {

    @Root
    private WebElement root;

    @FindByNg(action = "expand(variant)")
    private WebElement name;

    @FindByJQuery("li span:eq(0)")
    private WebElement description;

    @FindByJQuery("li span:eq(1) span:visible")
    private WebElement installations;

    @FindByJQuery(".dropdown")
    private Dropdown dropdown;

    @FindByJQuery(".ups-toggle-box")
    private WebElement details;

    @FindByJQuery(".ups-tip-panel tr:contains('Variant ID') td:last input")
    private WebElement id;

    @FindByJQuery(".ups-tip-panel tr:contains('Secret') td:last input")
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
        return id.getAttribute("value");
    }

    public String getSecret() {
        return secret.getAttribute("value");
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
        dropdown.click("Edit");
    }

    public void remove() {
        dropdown.click("Remove");
    }

    public String getType() {
        return (String) js.executeScript("return $(arguments[0]).prevAll().addBack().find('.ups-panel-variants h2').last().text()", root);
    }

}
