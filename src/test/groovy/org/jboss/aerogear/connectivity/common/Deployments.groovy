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
package org.jboss.aerogear.connectivity.common

import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter

import org.jboss.aerogear.connectivity.common.AndroidVariantUtils
import org.jboss.aerogear.connectivity.common.AuthenticationUtils
import org.jboss.aerogear.connectivity.common.InstallationUtils
import org.jboss.aerogear.connectivity.common.PushApplicationUtils
import org.jboss.aerogear.connectivity.common.PushNotificationSenderUtils
import org.jboss.aerogear.connectivity.common.SimplePushVariantUtils
import org.jboss.aerogear.connectivity.common.iOSVariantUtils

import com.google.android.gcm.server.Message
import com.google.android.gcm.server.MulticastResult
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService
import com.notnoop.apns.ApnsServiceBuilder
import com.notnoop.apns.PayloadBuilder
import com.notnoop.apns.internal.ApnsServiceImpl
import com.notnoop.exceptions.NetworkIOException

class Deployments {

    def static WebArchive unifiedPushServer() {

        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "aerogear-unified-push-server/pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class)

        // replace original persistence.xml with testing one
        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        // testing persistence
        war.addAsResource("META-INF/persistence.xml")
        return war
    }

    def static WebArchive unifiedPushServerWithClasses(Class<?>... clazz) {

        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "aerogear-unified-push-server/pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class)

        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        war.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")

        war.addClasses(clazz)

        File[] asm = Maven.resolver().resolve("org.ow2.asm:asm:4.1").withoutTransitivity().asFile()
        war = war.addAsLibraries(asm)

        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.mockito:mockito-core").withTransitivity().asFile()
        war = war.addAsLibraries(libs)

        return war
    }

    def static WebArchive customUnifiedPushServerWithClasses(Class<?>... clazz) {
        def unifiedPushServerPom = System.getProperty("unified.push.server.location", "aerogear-unified-push-server/pom.xml")

        WebArchive war = ShrinkWrap.create(MavenImporter.class).loadPomFromFile(unifiedPushServerPom).importBuildOutput()
                .as(WebArchive.class)

        war.delete("/WEB-INF/lib/gcm-server-1.0.2.jar")

        war.delete("/WEB-INF/classes/META-INF/persistence.xml")
        war.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")

        war.addClasses(
                AuthenticationUtils.class,
                PushApplicationUtils.class,
                AndroidVariantUtils.class,
                SimplePushVariantUtils.class,
                InstallationUtils.class,
                iOSVariantUtils.class,
                PushNotificationSenderUtils.class
                )

        war.addClasses(clazz)

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "gcm-server-1.0.2.jar")
                .addClasses(
                Result.class,
                Message.class,
                MulticastResult.class,
                Message.class,
                Sender.class
                )
        war.addAsLibraries(jar)

        war.delete("/WEB-INF/lib/apns-0.2.3.jar")

        JavaArchive apnsJar = ShrinkWrap.create(JavaArchive.class, "apns-0.2.3.jar")
                .addClasses(
                NetworkIOException.class,
                ApnsService.class,
                ApnsServiceImpl.class,
                ApnsServiceBuilder.class,
                PayloadBuilder.class,
                APNS.class
                )
        war.addAsLibraries(apnsJar)

        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "com.jayway.restassured:rest-assured",
                "org.mockito:mockito-core",
                "com.jayway.awaitility:awaitility-groovy").withTransitivity().asFile()
        war = war.addAsLibraries(libs)

        return war
    }
}

