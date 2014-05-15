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
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.exceptions.NetworkIOException;
import org.jboss.aerogear.test.api.sender.SenderStatistics;
import org.jboss.aerogear.unifiedpush.message.sender.GCMForChromePushNotificationSender;
import org.jboss.aerogear.unifiedpush.utils.Constants;
import org.jboss.aerogear.unifiedpush.utils.SenderStatisticsEndpoint;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Deployments {

    private static final Logger LOGGER = Logger.getLogger(Deployments.class.getName());

    private static final String PROPERTY_UPS_SOURCE = "ups.source";
    private static final String PROPERTY_UPS_VERSION = "ups.version";
    private static final String PROPERTY_UPS_REMOTE_URL = "ups.remote.url";
    private static final String PROPERTY_UPS_LOCAL_POM = "ups.local.pom";

    private static final String UPS_SOURCE_REMOTE = "remote";
    private static final String UPS_SOURCE_LOCAL = "local";

    private static final String UPS_MINIMUM_VERSION = "[0.10.0,)";

    private static final String UPS_SOURCE_DEFAULT = UPS_SOURCE_LOCAL;
    private static final String UPS_REMOTE_URL_DEFAULT = "http://dl.bintray.com/aerogear/AeroGear-UnifiedPush/";
    private static final String UPS_LOCAL_POM_DEFAULT = "aerogear-unifiedpush-server/server/pom.xml";

    private Deployments() {
        throw new UnsupportedOperationException("No instantiation.");
    }

    /**
     * Gets WebArchive of Unified Push Server with replaced persistence.xml files. The source of the server can be
     * configured and defaults to release.
     */
    public static WebArchive unifiedPushServer() {
        String upsSource = getUpsSource();

        WebArchive war;
        if (upsSource.equalsIgnoreCase(UPS_SOURCE_REMOTE)) {
            war = remoteUnifiedPushServer();
        } else if (upsSource.equalsIgnoreCase(UPS_SOURCE_LOCAL)) {
            war = localUnfiedPushServer();
        } else {
            throw new IllegalArgumentException("Unsupported source of Unified Push Server WAR: " + upsSource + "!");
        }

        // try to figure out whether UPS was modularized already
        String upsVersion = System.getProperty(PROPERTY_UPS_VERSION);
        if (upsVersion != null && upsVersion.length() != 0 && upsVersion.startsWith("0.10")) {
            replacePersistenceInWarPreModularization(war);
        }
        else {
            replacePersistenceInWar(war);
        }

        return war;
    }

    /**
     * Gets WebArchive of Unified Push Server with replaced persistence.xml files, custom sender libraries (GCM
     * and APNS) and bundled SenderStatisticsEndpoint for Message testing.
     *
     * @see Deployments#unifiedPushServer()
     */
    public static WebArchive unifiedPushServerWithCustomSenders() {
        WebArchive war = unifiedPushServer();

        Map<ArchivePath, Node> librariesToRemove = war.getContent(new Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath path) {
                return (path.get().startsWith("/WEB-INF/lib/apns") ||
                    path.get().startsWith("/WEB-INF/lib/gcm-server")) && path.get().endsWith(".jar");

            }
        });

        for (ArchivePath archivePath : librariesToRemove.keySet()) {
            war.delete(archivePath);
        }

        war.addClasses(SenderStatisticsEndpoint.class, SenderStatistics.class,
            GCMForChromePushNotificationSender.class);

        JavaArchive gcmJar = ShrinkWrap.create(JavaArchive.class, "gcm-server.jar").addClasses(Result.class,
            Message.class,
            MulticastResult.class, Message.class, Sender.class);
        JavaArchive apnsJar = ShrinkWrap.create(JavaArchive.class, "apns.jar").addClasses(NetworkIOException
            .class,
            ApnsService.class, ApnsServiceImpl.class, ApnsServiceBuilder.class, PayloadBuilder.class, APNS.class,
            Constants.class, ApnsNotification.class, EnhancedApnsNotification.class
            );
        war.addAsLibraries(gcmJar, apnsJar);

        PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

        // here we resolve mockito transitively, other artifact without transitivity
        File[] libs = resolver.resolve("com.jayway.restassured:rest-assured", "com.jayway.awaitility:awaitility")
            .withoutTransitivity().asFile();
        war.addAsLibraries(libs);
        libs = resolver.resolve("org.mockito:mockito-core").withTransitivity().asFile();
        war = war.addAsLibraries(libs);

        return war;
    }

    /**
     * Removes original persistence.xml files from unfiedpush-model-jpa JAR and from the war and replaces them with
     * custom ones. This way we change what kind of storage is used.
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
            jpaModel.addAsResource("META-INF/persistence-ups.xml", "META-INF/persistence.xml");
        }

        war.delete("/WEB-INF/classes/META-INF/persistence.xml");
        war.addAsResource("META-INF/persistence-pl.xml", "META-INF/persistence.xml");
    }

    /**
     * Removes original persistence.xml files from unfiedpush-model-jpa JAR and from the war and replaces them with
     * custom ones. This way we change what kind of storage is used.
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
     * Gets Unified Push Server from remote repository. If no version has been specified,
     * latest version in repository will be used.
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
        String upsLocalPom = System.getProperty(PROPERTY_UPS_LOCAL_POM, UPS_LOCAL_POM_DEFAULT);

        return ShrinkWrap.create(MavenImporter.class)
            .loadPomFromFile(upsLocalPom, getActiveProfiles())
            .importBuildOutput()
            .as(WebArchive.class);
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

    /**
     * @return True if code-coverage.active property is set to 'true', false otherwise.
     */
    private static boolean isCodeCoverageActive() {
        String codeCoverageActive = System.getProperty("code-coverage.active");
        return codeCoverageActive != null && codeCoverageActive.equals("true");
    }
}
