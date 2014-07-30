package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.utils.StringUtilities;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ModalDialog {

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

    public void cancel() {
        cancelButton.click();
    }

    public void ok() {
        okButton.click();
    }

    public void remove() {
        removeButton.click();
    }

}
