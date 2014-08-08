package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class Navigation {

    @FindByJQuery("a[href='#/dashboard']")
    private WebElement dashboardLink;

    @FindByJQuery("a[href='#/applications']")
    private WebElement applicationsLink;

    @FindByJQuery("a[href='#/compose']")
    private WebElement sendPushLink;

    @FindByJQuery("[ng-show='isViewLoading'] i")
    private WebElement spinner;

    public void goToDashboard() {
        waitGui().until().element(dashboardLink).is().present();
        dashboardLink.click();
    }

    public void goToApplications() {
        waitGui().until().element(dashboardLink).is().present();
        applicationsLink.click();
    }

    public void goToSendPush() {
        waitGui().until().element(dashboardLink).is().present();
        sendPushLink.click();
    }
}
