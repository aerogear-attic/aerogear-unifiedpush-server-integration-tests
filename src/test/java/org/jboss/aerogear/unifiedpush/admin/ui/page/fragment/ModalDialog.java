package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

public class ModalDialog {

    @Root
    private WebElement dialog;

    @FindBy(id = "createAppName")
    private WebElement nameInput;

    @FindBy(id = "createAppDescription")
    private WebElement descriptionInput;

    @FindByJQuery(".modal-footer .btn-default")
    private WebElement cancelButton;

    @FindByJQuery(".modal-footer .btn-primary")
    private WebElement okButton;

    @FindByJQuery(".modal-footer .btn-danger")
    private WebElement removeButton;

    @FindByJQuery("input[ng-model*='confirm']")
    private WebElement confirmNameInput;

    @FindByJQuery(".modal-backdrop")
    private WebElement backdrop;

    public void setName(String name) {
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void setDescription(String description) {
        descriptionInput.clear();
        descriptionInput.sendKeys(description);
    }

    public String getName() {
        return nameInput.getAttribute("value");
    }

    public String getDescription() {
        return descriptionInput.getAttribute("value");
    }

    public void confirmName(String name) {
        confirmNameInput.sendKeys(name);
    }

    public void cancel() {
        cancelButton.click();
        waitGui().until().element(backdrop).is().not().present();
    }

    public void ok() {
        okButton.click();
        waitGui().until().element(backdrop).is().not().visible();
    }

    public void remove() {
        removeButton.click();
        waitGui().until().element(backdrop).is().not().visible();
    }

    public void waitForDialog() {
        waitGui().until().element(dialog).is().visible();
    }

}
