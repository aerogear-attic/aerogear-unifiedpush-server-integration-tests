package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities;
import org.jboss.arquillian.graphene.angular.findby.FindByNg;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Application {

    @FindBy(tagName = "h3")
    private WebElement name;

    @FindByJQuery("p")
    private WebElement description;

    @FindByJQuery(".lo-app-opts li:eq(0) a span:visible")
    private WebElement variants;

    @FindByJQuery(".lo-app-opts li:eq(1) a")
    private WebElement activity;

    @FindByJQuery(".lo-app-opts li:eq(2) a")
    private WebElement sendPush;

    @FindByJQuery(".dropdown")
    private Dropdown dropdown;

    public String getName() {
        return name.getText();
    }

    public String getDescription() {
        String desc = description.getText();
        return desc.substring(1, desc.length() - 1);
    }

    public int getVariantCount() {
        return StringUtilities.parseCount(variants.getText());
    }

    public void goToApp() {
        name.click();
    }

    public void goToVariants() {
        variants.click();
    }

    public void goToActivity() {
        activity.click();
    }

    public void goToSendPush() {
        sendPush.click();
    }

    public void edit() {
        dropdown.click("Edit");
    }

    public void remove() {
        dropdown.click("Remove");
    }


}
