package org.jboss.aerogear.unifiedpush.admin.ui.page.fragment;

import org.jboss.aerogear.unifiedpush.admin.ui.page.PushAppsPage;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.jboss.arquillian.graphene.page.Page;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class Navigation {

    @Root
    private WebElement navigation;

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
        waitModel().until().element(navigation).is().present();
        dashboardLink.click();
    }

    public void goToApplications() {
        waitModel().until().element(navigation).is().present();
        applicationsLink.click();

        applicationsPage.waitForPage();
    }

    public void goToSendPush() {
        waitModel().until().element(navigation).is().present();
        sendPushLink.click();
    }
}
