package org.jboss.aerogear.unifiedpush.admin.ui.keycloak.page.fragment;

import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class FlashMessage {

    @Root
    private WebElement root;


    public boolean isSuccess() {
        return root.getAttribute("class").contains("success");
    }

    public boolean isError() {
        return root.getAttribute("class").contains("error");
    }

    public String getText() {
        return root.getText();
    }

    public void waitUntilPresent() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitModel().until().element(root).is().present();
    }
}
