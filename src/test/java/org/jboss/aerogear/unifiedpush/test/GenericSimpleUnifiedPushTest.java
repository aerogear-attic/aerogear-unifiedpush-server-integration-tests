package org.jboss.aerogear.unifiedpush.test;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class GenericSimpleUnifiedPushTest {

    @ArquillianResource
    protected URL root;
    
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.unifiedPushServer();
    }
    
    protected abstract String getContextRoot();
}
