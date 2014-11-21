package org.jboss.aerogear.unifiedpush.test;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.aerogear.test.api.application.PushApplicationWorker;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(ArquillianRules.class)
public class UpsWithRuleTest {


    @ArquillianRule
    public static UnifiedPushServer ups = new UnifiedPushServer() {

        @Override
        protected UnifiedPushServer setup() {
            // persist new PushApplication
            List<PushApplication> apps = with(PushApplicationWorker.worker())
                    .generate().persist()
                    .generate().persist()
                    .generate().name("name").description("asd").persist()
                    .detachEntities();

            assertThat(apps, is(notNullValue()));

            return this;
        }
    };

    @BeforeClass
    public static void setup() {
        TestUtils.setupRestAssured();
    }

    @AfterClass
    public static void cleanup() {
        TestUtils.teardownRestAssured();
    }

    @Deployment(name = Deployments.AUTH_SERVER, testable = false, order = 1)
    @TargetsContainer("main-server-group")
    public static WebArchive createAuthServerDeployment() {
        return Deployments.authServer();
    }

    @Deployment(name = Deployments.AG_PUSH, testable = false, order = 2)
    @TargetsContainer("main-server-group")
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }

    @Test
    public void verifyAppIsCreated() throws Exception {
        List<PushApplication> apps = ups.with(PushApplicationWorker.worker()).findAll().detachEntities();
        assertThat(apps, is(notNullValue()));
        assertThat(apps.size(), is(3));
    }

    @Test
    public void verifyAppIsCreated2() throws Exception {
        List<PushApplication> apps = ups.with(PushApplicationWorker.worker()).findAll().detachEntities();
        assertThat(apps, is(notNullValue()));
        assertThat(apps.size(), is(3));
    }

}
