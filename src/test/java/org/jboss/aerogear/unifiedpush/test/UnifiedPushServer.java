package org.jboss.aerogear.unifiedpush.test;

import org.jboss.aerogear.arquillian.test.smarturl.SchemeName;
import org.jboss.aerogear.arquillian.test.smarturl.UriScheme;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.SessionRequest;
import org.jboss.aerogear.test.api.UPSContext;
import org.jboss.aerogear.test.api.UPSWorker;
import org.jboss.aerogear.test.api.auth.LoginRequest;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String ADMIN_OLD_PASSWORD = "123";
    private static final String ADMIN_NEW_PASSWORD = "opensource2013";

    @OperateOnDeployment(Deployments.AG_PUSH)
    @ArquillianResource
    protected URL httpUrl;

    @OperateOnDeployment(Deployments.AUTH_SERVER)
    @ArquillianResource
    protected URL authServerHttpUrl;

    @OperateOnDeployment(Deployments.AG_PUSH)
    @ArquillianResource
    @UriScheme(name = SchemeName.HTTPS, port = 8443)
    protected URL httpsUrl;

    @OperateOnDeployment(Deployments.AUTH_SERVER)
    @ArquillianResource
    @UriScheme(name = SchemeName.HTTPS, port = 8443)
    protected URL authServerHttpsUrl;

    protected int currentSession;
    protected List<Session> sessions;

    protected String username;
    protected String oldPassword;
    protected String newPassword;

    protected boolean initPerSession;

    private static final UriScheme defaultUriScheme = new UriScheme() {

        @Override
        public SchemeName name() {
            return SchemeName.HTTP;
        }

        @Override
        public int port() {
            return 8080;
        }

        @Override
        public String user() {
            return "";
        }

        @Override
        public String password() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return UriScheme.class;
        }
    };

    public UnifiedPushServer() {
        this.sessions = new ArrayList<Session>();
        this.currentSession = 0;
        this.username = ADMIN_LOGIN_NAME;
        this.oldPassword = ADMIN_OLD_PASSWORD;
        this.newPassword = ADMIN_NEW_PASSWORD;
        this.initPerSession = Boolean.parseBoolean(System.getProperty("ups.initPerSession", "false"));
    }

    /**
     * Returns current session. Always use this method to refer to session in tests
     */
    public Session getSession() {
        return sessions.get(currentSession);
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
                if (UnifiedPushServer.this.sessions.isEmpty()) {
                    UnifiedPushServer.this.sessions = initializeSessions(username, oldPassword, newPassword);

                    // FIXME looks like for domain, datasets are not shared
                    if (UnifiedPushServer.this.initPerSession) {
                        for (int i = 0; i < UnifiedPushServer.this.sessions.size(); i++) {
                            UnifiedPushServer.this.currentSession = i;
                            setup();
                        }
                    }
                    // prepare data, is is sufficient to do that for the very first session
                    else {
                        setup();
                    }
                }

                // fire test multiple times and setup the session
                int i = 0;
                for (Session session : UnifiedPushServer.this.sessions) {
                    UnifiedPushServer.this.currentSession = i;
                    UnifiedPushServer.log.log(Level.INFO, "Testing {0}() on {1}", new Object[] { method.getName(),
                            session });
                    base.evaluate();
                    i++;
                }
                // FIXME add cleanup method
            }
        };
    }

    /**
     * Initializes session. Creates one UPS session per {@link ArquillianResource} URL injection. Override this
     * method to get
     * different behavior.
     *
     * @param username    Username
     * @param oldPassword Old (default) password
     * @param newPassword New password for UnifiedPush Server
     * @return
     * @throws Exception
     */
    protected List<Session> initializeSessions(String username, String oldPassword,
                                               String newPassword) throws Exception {

        List<Session> sessions = new ArrayList<Session>();

        // check all fields that are injecting URL via ArquillianResource
        List<Field> fields = getFieldsWithAnnotation(this.getClass(), ArquillianResource.class);

        Map<UriScheme, DeploymentURLPair> deploymentURLPairMap = new HashMap<UriScheme, DeploymentURLPair>();
        for (Field field : fields) {
            if (URL.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(OperateOnDeployment.class)) {
                URL url = (URL) field.get(this);
                OperateOnDeployment operateOnDeployment = field.getAnnotation(OperateOnDeployment.class);
                UriScheme uriScheme = field.getAnnotation(UriScheme.class);
                if (uriScheme == null) {
                    uriScheme = defaultUriScheme;
                }
                if (TestUtils.ignoreHttp() && uriScheme.name() == SchemeName.HTTP) {
                    continue;
                }
                if (TestUtils.ignoreHttps() && uriScheme.name() == SchemeName.HTTPS) {
                    continue;
                }

                if (!deploymentURLPairMap.containsKey(uriScheme)) {
                    deploymentURLPairMap.put(uriScheme, new DeploymentURLPair());
                }

                DeploymentURLPair pair = deploymentURLPairMap.get(uriScheme);
                if (operateOnDeployment.value().equals(Deployments.AG_PUSH)) {
                    if (pair.getUnifiedPushServerUrl() != null) {
                        throw new IllegalStateException("Unified Push Server already set for UriScheme: " + uriScheme);
                    }
                    pair.setUnifiedPushServerUrl(url);
                } else if (operateOnDeployment.value().equals(Deployments.AUTH_SERVER)) {
                    if (pair.getAuthServerUrl() != null) {
                        throw new IllegalStateException("Auth Server already set for UriScheme: " + uriScheme);
                    }
                    pair.setAuthServerUrl(url);
                } else {
                    // FIXME unknown deployment name
                    continue;
                }
            }
        }

        for (DeploymentURLPair pair : deploymentURLPairMap.values()) {
            log.log(Level.INFO, "Establishing Auth Server session at {0}", pair.getAuthServerUrl());

            Session session = LoginRequest
                    .request()
                    .setUnifiedPushServerUrl(pair.getUnifiedPushServerUrl())
                    .setAuthServerUrl(pair.getAuthServerUrl())
                    .username(username)
                    .password(oldPassword)
                    .login();
            sessions.add(session);
        }

/*        for (Field field : fields) {
            if (URL.class.isAssignableFrom(field.getType())) {
                URL url = (URL) field.get(this);
                log.log(Level.INFO, "Establishing UPS session at {0}", url);
                LoginRequest.request().setAuthServerUrl(url).username(username).password(oldPassword).login();
                sessions.add(login(url, username, oldPassword, newPassword));
            }
        }*/

        return sessions;
    }

    /**
     * Logins to UPS and established session
     *
     * @param url
     * @param username
     * @param oldPassword
     * @param newPassword
     * @return
     */
    // FIXME replace with credentials object, it would be more flexible
    /*protected Session login(URL url, String username, String oldPassword, String newPassword) {
        return new Session(url, username, newPassword, new HashMap<String, Object>()).completeLogin(oldPassword);
    }*/

    /**
     * Creates initial data on UnifiedPush server instance. These data are created against only one session,
     * this makes them available in all sessions
     *
     * @param ups UPS with already established session
     * @return current rules
     */
    protected abstract UnifiedPushServer setup();

    protected static List<Field> getFieldsWithAnnotation(final Class<?> source,
                                                         final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController.doPrivileged(new PrivilegedAction<List<Field>>() {
            public List<Field> run() {
                List<Field> foundFields = new ArrayList<Field>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotationClass)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            foundFields.add(field);
                        }
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            }
        });
        return declaredAccessableFields;
    }

    private static class DeploymentURLPair {

        private URL unifiedPushServerUrl;
        private URL authServerUrl;

        public URL getUnifiedPushServerUrl() {
            return unifiedPushServerUrl;
        }

        public DeploymentURLPair setUnifiedPushServerUrl(URL unifiedPushServerUrl) {
            this.unifiedPushServerUrl = unifiedPushServerUrl;
            return this;
        }

        public URL getAuthServerUrl() {
            return authServerUrl;
        }

        public DeploymentURLPair setAuthServerUrl(URL authServerUrl) {
            this.authServerUrl = authServerUrl;
            return this;
        }


    }

}
