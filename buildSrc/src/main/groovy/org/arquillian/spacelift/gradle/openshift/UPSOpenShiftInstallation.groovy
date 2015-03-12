package org.arquillian.spacelift.gradle.openshift

import org.arquillian.spacelift.gradle.CertificateGenerator
import org.arquillian.spacelift.gradle.keytool.KeyTool

import java.io.File
import java.lang.reflect.Array
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.BaseContainerizableObject
import org.arquillian.spacelift.gradle.DefaultGradleTask
import org.arquillian.spacelift.gradle.DeferredValue;
import org.arquillian.spacelift.gradle.GradleTask
import org.arquillian.spacelift.gradle.InheritanceAwareContainer
import org.arquillian.spacelift.gradle.Installation
import org.arquillian.spacelift.gradle.git.GitAddTool
import org.arquillian.spacelift.gradle.git.GitCommitTool
import org.arquillian.spacelift.gradle.git.GitPushTool
import org.arquillian.spacelift.gradle.git.GitSshFileTask
import org.arquillian.spacelift.process.ProcessInteractionBuilder
import org.arquillian.spacelift.process.ProcessResult
import org.arquillian.spacelift.task.TaskRegistry
import org.arquillian.spacelift.task.io.FileSelector
import org.arquillian.spacelift.task.io.WriteToFileTool
import org.arquillian.spacelift.task.os.CommandTool
import org.gradle.api.Project
import org.slf4j.Logger

import com.google.common.io.Files
import com.jayway.awaitility.Awaitility
import com.jayway.restassured.RestAssured
import com.jayway.restassured.response.Response

import groovy.lang.Closure
import groovy.transform.CompileStatic

@CompileStatic
class UPSOpenShiftInstallation extends BaseContainerizableObject<UPSOpenShiftInstallation> implements Installation {

    DeferredValue<String> product = DeferredValue.of(String.class).from("openshift")

    DeferredValue<String> version = DeferredValue.of(String.class).from("1.0.2")

    DeferredValue<Boolean> isInstalled = DeferredValue.of(Boolean.class).from(false)

    DeferredValue<Boolean> turnProxyOn = DeferredValue.of(Boolean.class).from(false)

    DeferredValue<Void> postActions = DeferredValue.of(Void.class)

    DeferredValue<String> openShiftAppName = DeferredValue.of(String.class).from("upsintegrationtests")

    DeferredValue<String> openShiftNamespace = DeferredValue.of(String.class).from("mobileqa")

    DeferredValue<String> openShiftGearSize = DeferredValue.of(String.class).from("small")

    DeferredValue<String> openShiftUsername = DeferredValue.of(String.class)

    DeferredValue<String> openShiftPassword = DeferredValue.of(String.class)

    DeferredValue<String> openShiftToken = DeferredValue.of(String.class)

    DeferredValue<List> openShiftCartridges = DeferredValue.of(List.class).from([ 'https://raw.githubusercontent.com/jboss-mobile/jboss-unified-push-openshift-cartridge/master/metadata/manifest.yml' ])

    DeferredValue<Boolean> openShiftScale = DeferredValue.of(Boolean.class).from(false)

    DeferredValue<String> openShiftServer = DeferredValue.of(String.class)

    DeferredValue<Boolean> openShiftIgnoreIfExists = DeferredValue.of(Boolean.class).from(false)

    DeferredValue<Boolean> openShiftForce = DeferredValue.of(Boolean.class).from(true)

    DeferredValue<File> gitSsh = DeferredValue.of(File.class).from({Spacelift.task(GitSshFileTask).execute().await()})

    DeferredValue<Boolean> openShiftCheckout = DeferredValue.of(Boolean.class).from(true)

    DeferredValue<File> openShiftRepository = DeferredValue.of(File.class)

    DeferredValue<Integer> openShiftHttpProxyPort = DeferredValue.of(Integer.class).from(16000)

    DeferredValue<Integer> openShiftHttpsProxyPort = DeferredValue.of(Integer.class).from(16000)

    DeferredValue<Integer> gcmMockServerPort = DeferredValue.of(Integer.class).from(16001)

    DeferredValue<File> aerogearGcmMockCertificate = DeferredValue.of(File.class)

    DeferredValue<File> aerogearGcmMockKey = DeferredValue.of(File.class)

    DeferredValue<Integer> aerogearApnsPushPort = DeferredValue.of(Integer.class).from(16002)

    DeferredValue<Integer> aerogearApnsFeedbackPort = DeferredValue.of(Integer.class).from(16003)

    DeferredValue<String> openShiftAppDirectory = DeferredValue.of(String.class).from("unified-push")

    DeferredValue<String> unifiedPushTestExtensionVersion = DeferredValue.of(String.class)


    // tools provided by this installation
    InheritanceAwareContainer<GradleTask, DefaultGradleTask> tools

    UPSOpenShiftInstallation(String name, Object parent) {
        super(name, parent)
        this.tools = new InheritanceAwareContainer(this, GradleTask, DefaultGradleTask)
    }

    UPSOpenShiftInstallation(String name, UPSOpenShiftInstallation other) {
        super(name, other)

        this.product = other.@product.copy()
        this.version = other.@version.copy()
        this.isInstalled = other.@isInstalled.copy()
        this.turnProxyOn = other.@turnProxyOn.copy()
        this.postActions = other.@postActions.copy()
        this.openShiftAppName = other.@openShiftAppName.copy()
        this.openShiftNamespace  = other.@openShiftNamespace.copy()
        this.openShiftGearSize = other.@openShiftGearSize.copy()
        this.openShiftUsername = other.@openShiftUsername.copy()
        this.openShiftPassword = other.@openShiftPassword.copy()
        this.openShiftToken = other.@openShiftToken.copy()
        this.openShiftCartridges = other.@openShiftCartridges.copy()
        this.openShiftScale = other.@openShiftScale.copy()
        this.openShiftServer = other.@openShiftServer.copy()
        this.openShiftIgnoreIfExists = other.@openShiftIgnoreIfExists.copy()
        this.openShiftForce = other.@openShiftForce.copy()
        this.gitSsh = other.@gitSsh.copy()
        this.openShiftCheckout = other.@openShiftCheckout.copy()
        this.openShiftRepository = other.@openShiftRepository.copy()
        this.openShiftAppDirectory = other.@openShiftAppDirectory.copy()
        this.unifiedPushTestExtensionVersion = other.@unifiedPushTestExtensionVersion.copy()
        this.tools = (InheritanceAwareContainer<GradleTask, DefaultGradleTask>) other.@tools.clone()

        this.aerogearGcmMockCertificate = other.@aerogearGcmMockCertificate.copy()
        this.aerogearGcmMockKey = other.@aerogearGcmMockKey.copy()

        // ports

        this.openShiftHttpProxyPort = other.@openShiftHttpProxyPort.copy()
        this.openShiftHttpsProxyPort =other.@openShiftHttpsProxyPort.copy()
        this.gcmMockServerPort = other.@gcmMockServerPort.copy()
        this.aerogearApnsPushPort = other.@aerogearApnsPushPort.copy()
        this.aerogearApnsFeedbackPort = other.@aerogearApnsFeedbackPort.copy()
    }

    @Override
    public UPSOpenShiftInstallation clone(String name) {
        return new UPSOpenShiftInstallation(name, this)
    }

    @Override
    public String getProduct() {
        product.resolve()
    }

    @Override
    public String getVersion() {
        version.resolve()
    }

    @Override
    public File getHome() {
        return (File) parent['workspace']
    }

    @Override
    public boolean isInstalled() {
        isInstalled.resolve()
    }

    @Override
    public void registerTools(TaskRegistry registry) {
        // intentionally empty
    }

    public boolean turnProxyOn() {
        turnProxyOn.resolve()
    }

    public CharSequence[] getOpenShiftCartridges() {
        openShiftCartridges.resolve().toArray(new CharSequence[0])
    }

    @Override
    public void install(Logger logger) {

        println 'Creating UPS OpenShift cartridge.'

        File repository = openShiftRepository.resolve()
        if (repository == null || !repository.exists() || !repository.isDirectory()) {
            repository = File.createTempDir()
        }

        println "UPS OpenShift repository will be cloned to ${repository.canonicalPath}"

        Spacelift.task(CreateOpenShiftCartridge)
                .server(openShiftServer.resolve())
                .named(openShiftAppName.resolve())
                .at(openShiftNamespace.resolve())
                .sized(openShiftGearSize.resolve())
                .username(openShiftUsername.resolve())
                .password(openShiftPassword.resolve())
                .token(openShiftToken.resolve())
                .cartridges(getOpenShiftCartridges())
                .scale(openShiftScale.resolve())
                .ignoreIfExists(openShiftIgnoreIfExists.resolve())
                .force(openShiftForce.resolve())
                .checkout(openShiftCheckout.resolve())
                .repo(repository.canonicalPath)
                .gitSsh(gitSsh.resolve())
                .execute().await()

        ProcessResult ipGetResult = Spacelift.task(CommandTool).programName("rhc")
                .parameters('ssh')
                .parameters('--command', 'echo $OPENSHIFT_UNIFIED_PUSH_IP$OPENSHIFT_AEROGEAR_PUSH_IP$OPENSHIFT_JBOSS_UNIFIED_PUSH_IP$MYTESTIP_1')
                .parameters('-a', openShiftAppName.resolve())
                .parameters('-n', openShiftNamespace.resolve())
                .parameters('-l', openShiftUsername.resolve())
                .parameters('-p', openShiftPassword.resolve())
                .execute().await()

        String ip = ipGetResult.output().last()

        ProcessResult homeDirGetResult = Spacelift.task(CommandTool).programName('rhc')
                .parameters('ssh')
                .parameters('--command', 'echo $HOME')
                .parameters('-a', openShiftAppName.resolve())
                .parameters('-n', openShiftNamespace.resolve())
                .parameters('-l', openShiftUsername.resolve())
                .parameters('-p', openShiftPassword.resolve())
                .execute().await()

        String openshiftHomeDir = homeDirGetResult.output().last()

        def apnsCertificateTarget = new File(repository, "apns_server.jks")
        def trustStoreTarget = new File(repository, "truststore.jks")

        CertificateGenerator certificateGenerator = Spacelift.task(CertificateGenerator)
            .apnsCertificate(apnsCertificateTarget)
            .gcmCertificate(aerogearGcmMockCertificate.resolve())
            .trustStore(trustStoreTarget)
            .commonName(ip)

        certificateGenerator.execute().await()

        def deployFile = "mv ~/app-root/repo/unifiedpush-test-extension-server.war ~/" + openShiftAppDirectory.resolve() + "/standalone/deployments/unifiedpush-test-extension-server.war"
        def actionHooksDir = new File(repository.canonicalPath, '.openshift/action_hooks/')
        actionHooksDir.mkdirs()

        List<File> addedFiles = Spacelift.task(WriteToFileTool)
                .write(deployFile)
                .to(new File(actionHooksDir, 'deploy'))
                .execute().await()


        if (turnProxyOn) {
            String[] JAVA_OPTS_EXT_PARAMETERS = [
                    "-Dhttp.proxyHost=$ip",
                    "-Dhttp.proxyPort=${openShiftHttpProxyPort.resolve()}",
                    "-Dhttps.proyHost=$ip",
                    "-Dhttps.proxyPort=${openShiftHttpsProxyPort.resolve()}",
                    "-Dgcm.mock.server.port=${gcmMockServerPort.resolve()}",
                    "-Dcustom.aerogear.apns.keystore.path=${openshiftHomeDir}app-root/repo/apns_server.jks",
                    "-Dcustom.aerogear.apns.keystore.password=${certificateGenerator.password()}",
                    '-Dcustom.aerogear.apns.keystore.type=JKS',
                    "-Dcustom.aerogear.apns.push.host=$ip",
                    "-Dcustom.aerogear.apns.push.port=${aerogearApnsPushPort.resolve()}",
                    "-Dcustom.aerogear.apns.feedback.host=$ip",
                    "-Dcustom.aerogear.apns.feedback.port=${aerogearApnsFeedbackPort.resolve()}",
                    "-Djavax.net.ssl.trustStore=${openshiftHomeDir}app-root/repo/truststore.jks",
                    "-Djavax.net.ssl.trustStorePassword=${certificateGenerator.password()}",
                    '-Djavax.net.debug=all']

            String JAVA_OPTS_EXT = "export JAVA_OPTS_EXT=\"${JAVA_OPTS_EXT_PARAMETERS.join(" ")}\""

            println JAVA_OPTS_EXT

            addedFiles.addAll(
                    Spacelift.task(WriteToFileTool)
                        .write(JAVA_OPTS_EXT)
                        .to(new File(actionHooksDir, "pre_start_${openShiftAppDirectory.resolve()}"))
                        .write(JAVA_OPTS_EXT)
                        .to(new File(actionHooksDir, "pre_restart_${openShiftAppDirectory.resolve()}"))
                        .execute().await())

        }

        String testExtensionWarPath = "${(parent['workspace'] as File).canonicalPath}" +
                "/.repository/org/jboss/aerogear/test/unifiedpush-test-extension-server/${unifiedPushTestExtensionVersion.resolve()}" +
                "/unifiedpush-test-extension-server-${unifiedPushTestExtensionVersion.resolve()}.war"

        def warFileSource = new File(testExtensionWarPath)
        def warFileTarget = new File(repository, 'unifiedpush-test-extension-server.war')

        Files.copy(warFileSource, warFileTarget)

        Spacelift.task(repository, GitAddTool)
                .add(addedFiles)
                .add(warFileTarget)
                .add(apnsCertificateTarget)
                .add(trustStoreTarget)
                .execute().await()

        addedFiles.each { addedFile ->
            println addedFile.canonicalPath
            Spacelift.task(CommandTool)
                    .workingDirectory(repository.canonicalPath)
                    .programName("git")
                    .parameters('update-index', '--chmod=+x', addedFile.canonicalPath)
                    .interaction(new ProcessInteractionBuilder().when(".*").printToOut())
                    .execute().await()
        }

        println 'Added files to OpenShift repository'

        println 'Committing unifiedpush-test-extension-server.war into the OpenShift repository.'

        Spacelift.task(repository, GitCommitTool).message('Add test extension war.').execute().await()

        File gitSshFile = gitSsh.resolve()

        println 'Pushing added unifiedpush-test-extension-server.war into the OpenShift repository'

        Spacelift.task(repository, GitPushTool).gitSsh(gitSshFile).execute().await()

        final String baseUri = "https://" + openShiftAppName.resolve() + "-" + openShiftNamespace.resolve() + ".rhcloud.com/unifiedpush-test-extension-server"

        println 'Waiting for unifiedpush-test-extension-server to be deployed. (max 5 minutes)'
        println "Expected deployment url: $baseUri"

        Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        Response response = RestAssured.given()
                                .baseUri(baseUri)
                                .get('/status')

                        return response.statusCode == 200
                    }
                })

        println 'The unifiedpush-test-extension-server.war was successfully deployed.'

        Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        Response response = RestAssured.given()
                                .baseUri(baseUri)
                                .get('/keycloak/realms')

                        response.then().log().all(true)

                        List<String> result = (List<String>) response.as(List.class)

                        return result.contains("aerogear")
                    }
                })

        println 'Reconfiguring KeyCloak.'
        RestAssured.given()
                .baseUri(baseUri)
                .get('/keycloak')
                .then()
                .log().all(true)
                .statusCode(200)

        println 'Restaring the cartridge.'
        Spacelift.task(CommandTool)
                .programName("rhc")
                .parameters('app', 'restart')
                .parameters('-a', openShiftAppName.resolve())
                .parameters('-n', openShiftNamespace.resolve())
                .parameters('-l', openShiftUsername.resolve())
                .parameters('-p', openShiftPassword.resolve())
                .execute().await()

        println 'Waiting for unifiedpush-test-extension-server to be deployed. (max 5 minutes)'
        println "Expected deployment url: $baseUri"

        Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        Response response = RestAssured.given()
                                .baseUri(baseUri)
                                .get('/status')

                        return response.statusCode == 200
                    }
                })

        println 'The unifiedpush-test-extension-server.war was successfully deployed.'

        if (turnProxyOn()) {
            println "Trying to activate proxy."

            Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                        @Override
                        Boolean call() throws Exception {
                            Response response = RestAssured.given().baseUri(baseUri).get("/proxy/activate")

                            println "returned status code: " + response.statusCode

                            return response.statusCode == 200
                        }
                    })

            println "Proxy was activated."
        }
    }
}
