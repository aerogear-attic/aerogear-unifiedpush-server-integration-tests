package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Dropdown {

    @FindByJQuery(".dropdown-menu")
    private WebElement menu;

    @FindByJQuery(".dropdown-toggle")
    private WebElement toggle;

    @FindByJQuery(".dropdown-menu a")
    private List<WebElement> menuItems;

    public void click(String linkText) {
        if (!menu.isDisplayed()) toggle.click();
        for (WebElement item : menuItems) {
            if (item.getText().contains(linkText)) {
                item.click();
                return;
            }
        }
        throw new RuntimeException("Could not find menu item containing \"" + linkText + "\"");
    }


}
