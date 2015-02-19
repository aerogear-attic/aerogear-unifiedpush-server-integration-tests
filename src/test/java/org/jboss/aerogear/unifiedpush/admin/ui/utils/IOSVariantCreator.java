package org.jboss.aerogear.unifiedpush.admin.ui.utils;

import org.jboss.aerogear.test.FileUtils;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.test.api.auth.LoginRequest;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantBlueprint;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantContext;
import org.jboss.aerogear.test.api.variant.ios.iOSVariantWorker;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.test.UnifiedPushServer;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IOSVariantCreator {

    private URL upsURL;

    private URL authURL;

    private WebDriver driver;

    public IOSVariantCreator(URL upsURL, URL authURL, WebDriver driver) {
        this.upsURL = upsURL;
        this.authURL = authURL;
        this.driver = driver;
    }

    public void create(String applicationName, String name, String desc, String appleCertPath, String passphrase, boolean isProd) {
        UnifiedPushServer ups = new UnifiedPushServer() {
            @Override
            protected UnifiedPushServer setup() {
                this.username = "admin";
                this.password = "123";
                this.session = LoginRequest
                        .request()
                        .setUnifiedPushServerUrl(upsURL)
                        .setAuthServerUrl(authURL)
                        .username("admin")
                        .password("123")
                        .login();
                return this;
            }
        }.setup();

        iOSVariantWorker worker = iOSVariantWorker.worker();
        PushApplication pushApplication = null;
        for (PushApplication application : ups.with(PushApplicationWorker.worker()).findAll().detachEntities()) {
            if (application.getName().equals(applicationName)) {
                pushApplication = application;
                break;
            }
        }
        iOSVariantContext context = worker.createContext(ups.getSession(), pushApplication);

        iOSVariantBlueprint blueprint = context.create();
        blueprint.setName(name);
        blueprint.setDescription(desc);
        blueprint.setCertificate(FileUtils.toByteArray(new File(appleCertPath)));
        blueprint.setPassphrase(passphrase);
        blueprint.setProduction(isProd);
        blueprint.persist();
    }

    private Map<String, ?> getBrowserCookies() {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Cookie cookie : driver.manage().getCookies()) {
            ret.put(cookie.getName(), cookie.getValue());
        }
        return ret;
    }
}
