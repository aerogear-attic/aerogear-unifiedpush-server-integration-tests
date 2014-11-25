package org.jboss.aerogear.unifiedpush.test;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.SessionRequest;
import org.jboss.aerogear.test.api.UPSContext;
import org.jboss.aerogear.test.api.UPSWorker;
import org.jboss.aerogear.test.api.auth.LoginRequest;
import org.jboss.aerogear.test.api.extension.TestExtensionRequest;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.keycloak.representations.AccessTokenResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A JUnit rule that prepares UnifiedPush Server instance for test execution.
 * <p/>
 * It is able to multiply test execution based on {@link ArquillianResource} injection points.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public abstract class UnifiedPushServer implements MethodRule {

    protected static final Logger log = Logger.getLogger(UnifiedPushServer.class.getName());

    private static final String ADMIN_LOGIN_NAME = "admin";
    private static final String ADMIN_PASSWORD = "123";

    @OperateOnDeployment(Deployments.AG_PUSH)
    @ArquillianResource
    protected URL unifiedPushUrl;

    @OperateOnDeployment(Deployments.AUTH_SERVER)
    @ArquillianResource
    protected URL authServerUrl;

    @OperateOnDeployment(Deployments.TEST_EXTENSION)
    @ArquillianResource
    protected URL testExtensionUrl;

    protected Session session;

    protected String username;
    protected String password;

    public UnifiedPushServer() {
        this.username = ADMIN_LOGIN_NAME;
        this.password = ADMIN_PASSWORD;
    }

    /**
     * Returns current session. Always use this method to refer to session in tests
     */
    public Session getSession() {
        return session;
    }


    public <ENTITY,
            ENTITY_ID,
            BLUEPRINT extends ENTITY,
            EDITOR extends ENTITY,
            PARENT,
            CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT,
                    WORKER>> CONTEXT with(WORKER worker) {
        return with(worker, null);
    }

    public <ENTITY,
            ENTITY_ID,
            BLUEPRINT extends ENTITY,
            EDITOR extends ENTITY,
            PARENT,
            CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
            WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>> CONTEXT
    with(WORKER worker, PARENT parent) {

        return worker.createContext(getSession(), parent);
    }

    public <REQUEST extends SessionRequest<REQUEST>> REQUEST with(REQUEST request) {
        return request.withSession(getSession());
    }

    public <REQUEST extends TestExtensionRequest<REQUEST>> REQUEST with(REQUEST request) {
        URL realTestExtensionUrl;
        try {
            realTestExtensionUrl = getRealUrl(testExtensionUrl);
        } catch (MalformedURLException e) {
            // The url should never get malformed, but if it does, we just throw a runtime exception.
            throw new IllegalStateException(e);
        }

        Session testExtensionSession = new Session(realTestExtensionUrl, new AccessTokenResponse());

        return request.withSession(testExtensionSession);
    }

    /**
     * Evaluates content of test method against all opened UPS sessions. Session is initialized once per test class
     * execution.
     */
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                // if there are no session, simply initialize sessions, login and prepare data
                if (session == null) {
                    try {
                        session = initializeSession(username, password);
                    } catch (Throwable t){
                        log.log(Level.SEVERE, "Session could not be established!");
                        throw t;
                    }
                }

                setup();
                log.log(Level.INFO, "Testing {0}() on {1}", new Object[] { method.getName(), session });
                base.evaluate();
            }
        };
    }

    /**
     * Initializes session. Creates one UPS session per {@link ArquillianResource} URL injection. Override this
     * method to get
     * different behavior.
     *
     * @param username Username
     * @param password Password
     * @return initialized session
     * @throws Exception
     */
    protected Session initializeSession(String username, String password) throws Exception {
        URL realUnifiedPushUrl = getRealUrl(unifiedPushUrl);
        URL realAuthServerUrl = getRealUrl(authServerUrl);

        log.log(Level.INFO, "Establishing Auth Server session at {0}", realAuthServerUrl);

        return LoginRequest
                .request()
                .setUnifiedPushServerUrl(realUnifiedPushUrl)
                .setAuthServerUrl(realAuthServerUrl)
                .username(username)
                .password(password)
                .login();
    }

    /**
     * Returns url with protocol based on port. If the port is 80 or 8080, http protocol is used. If the port is 443
     * or 8443 then https is used. This is to overcome limitation of Arquillian which always returns `http`.
     */
    protected URL getRealUrl(URL url) throws MalformedURLException {
        String realProtocol = url.getProtocol();
        int port = url.getPort();
        if (port == 80 || port == 8080) {
            realProtocol = "http";
        } else if (port == 443 || port == 8443) {
            realProtocol = "https";
        }
        return new URL(realProtocol, url.getHost(), url.getPort(), url.getFile());
    }

    /**
     * Creates initial data on UnifiedPush server instance. These data are created against only one session,
     * this makes them available in all sessions
     *
     * @return current rules
     */
    protected abstract UnifiedPushServer setup();

}
