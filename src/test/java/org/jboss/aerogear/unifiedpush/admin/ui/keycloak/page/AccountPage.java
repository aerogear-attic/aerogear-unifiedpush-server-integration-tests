package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page;

import org.jboss.aerogear.unifiedpush.admin.ui.keycloak.model.Account;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AccountPage {

    @FindBy(id = "username")
    private WebElement username;

    @FindBy(id = "email")
    private WebElement email;

    @FindBy(id = "lastName")
    private WebElement lastName;

    @FindBy(id = "firstName")
    private WebElement firstName;

    @FindByJQuery("button[value='Cancel']")
    private WebElement cancel;

    @FindByJQuery("button[value='Save']")
    private WebElement save;

    public Account getAccount() {
        return new Account(username.getAttribute("value"), email.getAttribute("value"), lastName.getAttribute("value"), firstName.getAttribute("value"));
    }

    public void setAccount(Account account) {
        email.clear();
        email.sendKeys(account.getEmail());
        lastName.clear();
        lastName.sendKeys(account.getLastName());
        firstName.clear();
        firstName.sendKeys(account.getFirstName());
    }

    public void save() {
        save.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        guardAjax(save).click();
    }
}
