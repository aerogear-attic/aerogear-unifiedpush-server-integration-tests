package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.page.PushAppsPage;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

public class Navigation {

    @FindByJQuery("a[href='#/dashboard']")
    private WebElement dashboardLink;

    @FindByJQuery("a[href='#/applications']")
    private WebElement applicationsLink;

    @FindByJQuery("a[href='#/compose']")
    private WebElement sendPushLink;

    @FindByJQuery("[ng-show='isViewLoading'] i")
    private WebElement spinner;

    @Page
    private PushAppsPage applicationsPage;

    public void goToDashboard() {
        waitGui().until().element(dashboardLink).is().present();
        dashboardLink.click();
    }

    public void goToApplications() {
        waitGui().until().element(dashboardLink).is().present();
        try {
            applicationsLink.click();
        } catch (Exception e) {
            System.out.println();
        }

        applicationsPage.waitForPage();
    }

    public void goToSendPush() {
        waitGui().until().element(dashboardLink).is().present();
        sendPushLink.click();
    }
}
