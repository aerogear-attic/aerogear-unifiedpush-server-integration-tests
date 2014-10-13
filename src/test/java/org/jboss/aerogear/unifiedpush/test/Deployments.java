/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.test;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.internal.ApnsServiceImpl;

import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.jboss.aerogear.test.api.installation.Tokens;
import org.jboss.aerogear.test.api.sender.SenderStatistics;
import org.jboss.aerogear.unifiedpush.message.sender.GCMForChromePushNotificationSender;
import org.jboss.aerogear.unifiedpush.utils.JavaSenderTestEndpoint;
import org.jboss.aerogear.unifiedpush.utils.JavaSenderTestRestApplication;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Deployments {

    private static final Logger LOGGER = Logger.getLogger(Deployments.class.getName());

    public static final String AG_PUSH = "ag_push";
    public static final String AUTH_SERVER = "auth_server";
    public static final String JAVA_CLIENT = "java_client";

    private static final String PROPERTY_UPS_SOURCE = "ups.source";
    private static final String PROPERTY_UPS_VERSION = "ups.version";
    private static final String PROPERTY_UPS_REMOTE_URL = "ups.remote.url";
    private static final String PROPERTY_UPS_LOCAL_POM = "ups.local.pom";
    private static final String PROPERTY_UPS_ARCHIVE_SERVER_PATH = "ups.server.archive.path";
    private static final String PROPERTY_UPS_ARCHIVE_AUTH_PATH = "ups.auth.archive.path";
    private static final String PROPERTY_UPS_SERVER_TYPE = "ups.server.type";
    // This value is visible automatically if this is executed via MavenExecutor or if user
    // is modifying settings.xml for MavenResolver from command line
    private static final String PROPERTY_UPS_SETTINGS_FILE = "org.apache.maven.user-settings";

    private static final String PROPERTY_UPS_DISABLE_REBUILD = "ups.disable.rebuild";

    private static final String UPS_SOURCE_REMOTE = "remote";
    private static final String UPS_SOURCE_LOCAL = "local";
    private static final String UPS_SOURCE_ARCHIVE = "archive";

    private static final String UPS_MINIMUM_VERSION = "[0.10.0,)";

    private static final String UPS_SOURCE_DEFAULT = UPS_SOURCE_LOCAL;
    private static final String UPS_REMOTE_URL_DEFAULT = "http://dl.bintray.com/aerogear/AeroGear-UnifiedPush/";
    private static final String UPS_LOCAL_POM_DEFAULT = "aerogear-unifiedpush-server/pom.xml";
    private static final String UPS_SERVER_TYPE_DEFAULT = "as7";

    private static final AtomicBoolean mavenBuildInvoked = new AtomicBoolean(false);

    private Deployments() {
        throw new UnsupportedOperationException("No instantiation.");
    }

    /**
     * Gets WebArchive of Unified Push Server with replaced persistence.xml files. The source of the server can be
     * configured
     * and defaults to release.
     */
    public static WebArchive unifiedPushServer() {
        String upsSource = getUpsSource();

        WebArchive war;
        if (upsSource.equalsIgnoreCase(UPS_SOURCE_REMOTE)) {
            war = remoteUnifiedPushServer();
        } else if (upsSource.equalsIgnoreCase(UPS_SOURCE_LOCAL)) {
            war = localUnfiedPushServer();
        } else if (upsSource.equalsIgnoreCase(UPS_SOURCE_ARCHIVE)) {
            war = archiveUnifiedPushServer();
        } else {
            throw new IllegalArgumentException("Unsupported source of Unified Push Server WAR: " + upsSource + "!");
        }

        // FIXME only >= 0.11.0 will be supported
        // try to figure out whether UPS was modularized already
        String upsVersion = System.getProperty(PROPERTY_UPS_VERSION);
        if (upsVersion != null && upsVersion.length() != 0 && upsVersion.startsWith("0.10")) {
            replacePersistenceInWarPreModularization(war);
        } else {
            replacePersistenceInWar(war);
        }

        return war;
    }

    /**
     * Gets WebArchive of Unified Push Server with replaced persistence.xml files,
     * custom sender libraries (GCM and APNS) and
     * bundled SenderStatisticsEndpoint for Message testing.
     *
     * @see Deployments#unifiedPushServer()
     */
    public static WebArchive unifiedPushServerWithCustomSenders() {
        WebArchive war = unifiedPushServer();

        Map<ArchivePath, Node> librariesToRemove = war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath path) {
                return path.get().startsWith("/WEB-INF/lib/gcm-server") && path.get().endsWith(".jar");

            }
        });

        for (ArchivePath archivePath : librariesToRemove.keySet()) {
            war.delete(archivePath);
        }

        war.addClasses(SenderStatisticsEndpoint.class, SenderStatistics.class,
                GCMForChromePushNotificationSender.class, Tokens.class);

        JavaArchive gcmJar = ShrinkWrap.create(JavaArchive.class, "gcm-server.jar").addClasses(Result.class,
                Message.class, MulticastResult.class, Message.class, Sender.class);
        war.addAsLibraries(gcmJar);

        Collection<JavaArchive> apnsLibs = war.getAsType(JavaArchive.class, new Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath path) {
                return path.get().startsWith("/WEB-INF/lib/apns") && path.get().endsWith(".jar");
            }
        });

        for (JavaArchive apnsLib : apnsLibs) {
            apnsLib.deleteClass(ApnsServiceImpl.class);
            apnsLib.addClass(ApnsServiceImpl.class);
        }

        PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

        // here we resolve mockito and json transitively, other artifact without transitivity
        File[] libs = resolver.resolve("com.jayway.restassured:rest-assured", "com.jayway.awaitility:awaitility")
                .withoutTransitivity().asFile();
        war.addAsLibraries(libs);
        libs = resolver.resolve("org.mockito:mockito-core", "org.json:json").withTransitivity().asFile();
        war = war.addAsLibraries(libs);

        return war;
    }

    /**
     * Removes original persistence.xml files from unfiedpush-model-jpa JAR and from the war and replaces them with
     * custom ones.
     * This way we change what kind of storage is used.
     *
     * @param war WebArchive to be modified.
     */
    private static void replacePersistenceInWar(WebArchive war) {
        Collection<JavaArchive> jpaModels = war.getAsType(JavaArchive.class, new Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath path) {
                return path.get().startsWith("/WEB-INF/lib/unifiedpush-model-jpa") && path.get().endsWith(".jar");
            }
        });

        for (JavaArchive jpaModel : jpaModels) {
            jpaModel.delete("/META-INF/persistence.xml");
            jpaModel.addAsResource("META-INF/persistence-model-jpa.xml", "META-INF/persistence.xml");
        }
    }

    /**
     * Removes original persistence.xml files from unfiedpush-model-jpa JAR and from the war and replaces them with
     * custom ones.
     * This way we change what kind of storage is used.
     *
     * @param war WebArchive to be modified.
     */
    private static void replacePersistenceInWarPreModularization(WebArchive war) {
        // replace original persistence.xml with testing one
        war.delete("/WEB-INF/classes/META-INF/persistence.xml");
        // testing persistence
        war.addAsResource("META-INF/persistence-pre-mod.xml", "META-INF/persistence.xml");
    }

    /**
     * @return ups server as already built distribution war file
     */
    private static WebArchive archiveUnifiedPushServer() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, getUpsArchiveServer());
    }

    /**
     * Gets Unified Push Server from remote repository. If no version has been specified,
     * latest version in repository will be
     * used.
     */
    private static WebArchive remoteUnifiedPushServer() {
        final String upsCanonicalCoordinate = "org.jboss.aerogear.unifiedpush:unifiedpush-server:war:%s";

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("remote_ups", getUpsRemoteUrl(),
                        "default"))
                .withMavenCentralRepo(false);

        MavenCoordinate upsCoordinate;
        String upsVersion = System.getProperty(PROPERTY_UPS_VERSION);
        if (upsVersion == null || upsVersion.length() == 0) {
            upsCoordinate = resolver
                    .resolveVersionRange(String.format(upsCanonicalCoordinate, UPS_MINIMUM_VERSION))
                    .getHighestVersion();

            LOGGER.log(Level.INFO, "Unified Push Server version not specified. Using repository''s latest version " +
                    "\"{0}\". You can override it by -D{1}", new Object[] { upsCoordinate.getVersion(),
                    PROPERTY_UPS_VERSION });
        } else {
            upsCoordinate = MavenCoordinates.createCoordinate(String.format(upsCanonicalCoordinate, upsVersion));
        }

        LOGGER.log(Level.INFO, "Resolving UnifiedPush Server using coordinates: {0}", upsCoordinate.toCanonicalForm());

        File warFile = resolver
                .resolve(upsCoordinate.toCanonicalForm())
                .withoutTransitivity()
                .asSingleFile();

        // https://issues.jboss.org/browse/WFK2-61
        return ShrinkWrap.create(ZipImporter.class, "ag-push.war").importFrom(warFile).as(WebArchive.class);
    }

    /**
     * Compiles and returns Unified Push Server from local filesystem. The location of the pom.xml can be altered.
     */
    private static WebArchive localUnfiedPushServer() {
        buildLocalServerIfNeeded();

        File[] serverWarFiles = getUpsServerWarFiles();
        if (serverWarFiles == null || serverWarFiles.length == 0) {
            throw new IllegalStateException("No war file found in directory '" +
                    getUpsServerTargetDirectory().getAbsolutePath() + "'. Please check that 'mvn clean package' " +
                    "inside the ups server directory will result in creation of .war file.");
        }

        return ShrinkWrap.create(ZipImporter.class, "ag-push.war").importFrom(serverWarFiles[0]).as(WebArchive.class);
    }

    public static WebArchive authServer() {
        String upsSource = getUpsSource();
        WebArchive war;
        if (upsSource.equalsIgnoreCase(UPS_SOURCE_REMOTE)) {
            war = remoteAuthServer();
        } else if (upsSource.equalsIgnoreCase(UPS_SOURCE_LOCAL)) {
            war = localAuthServer();
        } else if (upsSource.equalsIgnoreCase(UPS_SOURCE_ARCHIVE)) {
            war = archiveAuthServer();
        } else {
            throw new IllegalArgumentException("Unsupported source of Unified Push Server WAR: " + upsSource + "!");
        }

        replaceAuthServerPersistence(war);
        fixAerogearAuthServerConfiguration(war);

        return war;
    }

    private static void replaceAuthServerPersistence(WebArchive war) {
        Node keycloakServerJson = war.get("WEB-INF/classes/META-INF/keycloak-server.json");

        JSONTokener tokener = new JSONTokener(keycloakServerJson.getAsset().openStream());

        JSONObject keycloakServerConfig = new JSONObject(tokener);

        JSONObject connectionsJpa = keycloakServerConfig.optJSONObject("connectionsJpa");
        JSONObject defaultJpaConnection = connectionsJpa.optJSONObject("default");

        defaultJpaConnection.put("dataSource", "java:jboss/datasources/ExampleDS");
        defaultJpaConnection.remove("user");
        defaultJpaConnection.remove("password");

        war.add(new StringAsset(keycloakServerConfig.toString()), keycloakServerJson.getPath());
    }

    private static void fixAerogearAuthServerConfiguration(WebArchive war) {
        Node upsRealm = war.get("WEB-INF/ups-realm.json");

        JSONTokener tokener = new JSONTokener(upsRealm.getAsset().openStream());

        JSONObject config = new JSONObject(tokener);

        // Enable Direct Grant API
        config.put("passwordCredentialGrantAllowed", true);

        // Make sure the session won't expire even when the testing runs very slow
        config.put("accessTokenLifespan", 3600);
        config.put("accessCodeLifespan", 3600);
        config.put("accessCodeLifespanUserAction", 3600);
        config.put("ssoSessionIdleTimeout", 3600);


        // Any required action would prevent us to login
        JSONArray users = config.optJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);

            user.remove("requiredActions");
        }

        if (!config.has("scopeMappings")) {
            config.put("scopeMappings", new JSONArray());
        }

        JSONArray scopeMappings = config.optJSONArray("scopeMappings");

        JSONObject integrationTestsScopeMapping = new JSONObject()
                .put("client", "integration-tests")
                .put("roles", new String[] { "user", "admin" });

        scopeMappings.put(integrationTestsScopeMapping);

        if (!config.has("oauthClients")) {
            config.put("oauthClients", new JSONArray());
        }

        JSONArray oauthClients = config.optJSONArray("oauthClients");

        JSONObject integrationTestsClient = new JSONObject()
                .put("name", "integration-tests")
                .put("enabled", true)
                .put("publicClient", true)
                .put("directGrantsOnly", true)
                .put("claims", new JSONObject().put("username", true));

        oauthClients.put(integrationTestsClient);

        System.out.println(config.toString(1));

        war.add(new StringAsset(config.toString()), upsRealm.getPath());
    }

    private static WebArchive remoteAuthServer() {
        final String authServerCanonicalCoordinate = "org.jboss.aerogear.unifiedpush:unifiedpush-auth-server:war:%s";

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("remote_ups", getUpsRemoteUrl(),
                        "default"))
                .withMavenCentralRepo(false);

        MavenCoordinate upsCoordinate;
        String upsVersion = System.getProperty(PROPERTY_UPS_VERSION);
        if (upsVersion == null || upsVersion.length() == 0) {
            upsCoordinate = resolver
                    .resolveVersionRange(String.format(authServerCanonicalCoordinate, UPS_MINIMUM_VERSION))
                    .getHighestVersion();

            LOGGER.log(Level.INFO, "Unified Push Server version not specified. Using repository''s latest version " +
                    "\"{0}\". You can override it by -D{1}", new Object[] { upsCoordinate.getVersion(),
                    PROPERTY_UPS_VERSION });
        } else {
            upsCoordinate = MavenCoordinates.createCoordinate(String.format(authServerCanonicalCoordinate, upsVersion));
        }

        LOGGER.log(Level.INFO, "Resolving UnifiedPush Auth Server using coordinates: {0}",
                upsCoordinate.toCanonicalForm());

        File warFile = resolver
                .resolve(upsCoordinate.toCanonicalForm())
                .withoutTransitivity()
                .asSingleFile();

        // https://issues.jboss.org/browse/WFK2-61
        return ShrinkWrap.create(ZipImporter.class, "auth-server.war").importFrom(warFile).as(WebArchive.class);
    }

    private static WebArchive localAuthServer() {
        buildLocalServerIfNeeded();

        File[] authServerWarFiles = getAuthServerWarFiles();
        if (authServerWarFiles == null || authServerWarFiles.length == 0) {
            throw new IllegalStateException("No war file found in directory '" + getAuthServerTargetDirectory()
                    .getAbsolutePath() + "'. Please check that 'mvn clean package' inside the ups auth-server " +
                    "directory will result in creation of .war file.");
        }

        return ShrinkWrap.create(ZipImporter.class, "auth-server.war").importFrom(authServerWarFiles[0])
                .as(WebArchive.class);
    }

    /**
     * @return ups auth server as already built distribution war file
     */
    private static WebArchive archiveAuthServer() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, getUpsArchiveAuth());
    }

    private static boolean isTravis() {
        return System.getenv("TRAVIS") != null;
    }

    private static void buildLocalServerIfNeeded() {
        if (isUpsBuildNeeded()) {
            LOGGER.log(Level.INFO, "Building UnifiedPush Server from sources at: {0}",
                    getUpsParentDirectory().getAbsolutePath());

            try {
                CommandTool command = Tasks.prepare(CommandTool.class)
                        .workingDir(getUpsParentDirectory().getAbsolutePath())
                        .programName("mvn")
                        .parameters("clean", "package", "-DskipTests", "-Dmaven.javadoc.skip=true")
                        .parameter(getActiveProfilesAsMavenParameter())
                        .splitToParameters(getUpsSettings())
                        .interaction(new ProcessInteractionBuilder()
                                .outputPrefix("ups-maven-build: ").when(".*").printToOut());

                if (isTravis()) {
                    command.parameters("-q", "-B");
                }

                command.execute().await();
            } catch (ExecutionException e) {
                LOGGER.log(Level.WARNING, "Could not package UnifiedPush Server WAR. It is possible that you do not " +
                        "have Maven on PATH. Assuming you did compile UnifiedPush yourself and resuming tests.", e);
            }
            mavenBuildInvoked.set(true);
        }
    }

    /**
     * Returns REST web application that uses Java Client
     * to test push using the same trust store
     */
    public static WebArchive javaSenderTest() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "javaSender.war");
        war.addClass(JavaSenderTestEndpoint.class);
        war.addClass(JavaSenderTestRestApplication.class);
        
        File[] javaSenderJAR = Maven.resolver().resolve("org.jboss.aerogear:unifiedpush-java-client:0.8.0").withTransitivity().asFile();
        war.addAsLibraries(javaSenderJAR);
        
        return war;
    }
    
    /**
     * Returns an array of profile names for maven build. This is currently only to pass in code-coverage profile for
     * ups build.
     */
    private static String[] getActiveProfiles() {
        List<String> activeProfiles = new ArrayList<String>();

        if (isCodeCoverageActive()) {
            activeProfiles.add("code-coverage");
        }

        return activeProfiles.toArray(new String[activeProfiles.size()]);
    }

    private static String getUpsSettings() {
        StringBuilder settingsBuilder = new StringBuilder();

        String settingsFile = System.getProperty(PROPERTY_UPS_SETTINGS_FILE);

        if (settingsFile != null && !settingsFile.isEmpty()) {
            settingsBuilder.append("-s ");
            settingsBuilder.append(settingsFile);
        }

        return settingsBuilder.toString();
    }

    private static String getActiveProfilesAsMavenParameter() {
        StringBuilder profileBuilder = new StringBuilder("-P");
        int i = 0;
        for (String profile : getActiveProfiles()) {
            if (i++ > 0) {
                profileBuilder.append(',');
            }
            profileBuilder.append(profile);
        }

        return i > 0 ? profileBuilder.toString() : "";
    }

    private static String getUpsSource() {
        String upsSource = System.getProperty(PROPERTY_UPS_SOURCE);
        if (upsSource == null || upsSource.length() == 0) {
            // FIXME what should be the default behavior?
            upsSource = UPS_SOURCE_DEFAULT;
            LOGGER.log(Level.INFO, "Unified Push Server WAR source not specified. Using default source \"{0}\". You " +
                    "can override it by -D{1}", new Object[] { upsSource, PROPERTY_UPS_SOURCE });
        }
        return upsSource;
    }

    private static String getUpsRemoteUrl() {
        String remoteRepository = System.getProperty(PROPERTY_UPS_REMOTE_URL);
        if (remoteRepository == null || remoteRepository.length() == 0) {
            remoteRepository = UPS_REMOTE_URL_DEFAULT;
            LOGGER.log(Level.INFO, "Unified Push Server remote repository url not specified. Using default \"{0}\". " +
                    "You can override it by -D{1}", new Object[] { remoteRepository, PROPERTY_UPS_REMOTE_URL });
        }
        return remoteRepository;
    }

    private static File getUpsLocalPomFile() {
        String upsLocalPomPath = System.getProperty(PROPERTY_UPS_LOCAL_POM, UPS_LOCAL_POM_DEFAULT);
        return new File(upsLocalPomPath);
    }

    private static File getUpsParentDirectory() {
        return getUpsLocalPomFile().getParentFile();
    }

    private static File getUpsServersDirectory() {
        return new File(getUpsParentDirectory(), "servers");
    }

    private static File getUpsServerDirectory() {
        return new File(getUpsServersDirectory(), "ups-" + getUpsServerType());
    }

    private static File getUpsServerTargetDirectory() {
        return new File(getUpsServerDirectory(), "target");
    }

    private static File[] getUpsServerWarFiles() {
        return getUpsServerTargetDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".war") && pathname.isFile() && pathname.canRead();
            }
        });
    }

    private static File getAuthServerDirectory() {
        return new File(getUpsServersDirectory(), "auth-server");
    }

    private static File getAuthServerTargetDirectory() {
        return new File(getAuthServerDirectory(), "target");
    }

    private static File[] getAuthServerWarFiles() {
        return getAuthServerTargetDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".war") && pathname.isFile() && pathname.canRead();
            }
        });
    }

    private static File getUpsArchiveServer() {
        String upsArchiveServerPath = System.getProperty(PROPERTY_UPS_ARCHIVE_SERVER_PATH);

        if (upsArchiveServerPath == null || upsArchiveServerPath.isEmpty()) {
            throw new IllegalStateException("ups.server.archive.path is null or empty string");
        }

        return new File(upsArchiveServerPath);
    }

    private static File getUpsArchiveAuth() {
        String upsArchiveAuthPath = System.getProperty(PROPERTY_UPS_ARCHIVE_AUTH_PATH);

        if (upsArchiveAuthPath == null || upsArchiveAuthPath.isEmpty()) {
            throw new IllegalStateException("ups.auth.archive.path is null object or empty string.");
        }

        return new File(upsArchiveAuthPath);
    }

    private static String getUpsServerType() {
        String serverType = System.getProperty(PROPERTY_UPS_SERVER_TYPE);
        if (serverType == null || serverType.length() == 0) {
            serverType = UPS_SERVER_TYPE_DEFAULT;
            LOGGER.log(Level.INFO, "UnifiedPush Server type not specified. Using default \"{0}\". " +
                    "You can override it by -D{1}", new Object[] { serverType, PROPERTY_UPS_SERVER_TYPE });
        }
        return serverType;

    }

    private static boolean isUpsBuildNeeded() {
        Boolean upsDisableRebuild = Boolean.getBoolean(PROPERTY_UPS_DISABLE_REBUILD);
        File[] serverWarFiles = getUpsServerWarFiles();
        return !mavenBuildInvoked.get() && !(upsDisableRebuild && serverWarFiles != null && serverWarFiles.length > 0);

    }

    /**
     * @return True if code-coverage.active property is set to 'true', false otherwise.
     */
    private static boolean isCodeCoverageActive() {
        String codeCoverageActive = System.getProperty("code-coverage.active");
        return codeCoverageActive != null && codeCoverageActive.equals("true");
    }
}
