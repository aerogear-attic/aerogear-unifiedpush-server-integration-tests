package org.arquillian.spacelift.gradle.openshift

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

    // getters

    public String getOpenShiftAppName() {
        openShiftAppName.resolve()
    }

    public String getOpenShiftNamespace() {
        openShiftNamespace.resolve()
    }

    public String getOpenShiftGearSize() {
        openShiftGearSize.resolve()
    }

    public String getOpenShiftUsername() {
        openShiftUsername.resolve()
    }

    public String getOpenShiftPassword() {
        openShiftPassword.resolve()
    }

    public String getOpenShiftToken() {
        openShiftToken.resolve()
    }

    public CharSequence[] getOpenShiftCartridges() {
        openShiftCartridges.resolve().toArray(new CharSequence[0])
    }

    public Boolean getOpenShiftScale() {
        openShiftScale.resolve()
    }

    public String getOpenShiftServer() {
        openShiftServer.resolve()
    }

    public boolean getOpenShiftIgnoreIfExists() {
        openShiftIgnoreIfExists.resolve()
    }

    public boolean getOpenShiftForce() {
        openShiftForce.resolve()
    }

    public File getGitSsh() {
        gitSsh.resolve()
    }

    public boolean getOpenShiftCheckout() {
        openShiftCheckout.resolve()
    }

    public File getOpenShiftRepository() {
        openShiftRepository.resolve()
    }

    public int getOpenShiftHttpProxyPort() {
        openShiftHttpProxyPort.resolve()
    }

    public int getOpenShiftHttpsProxyPort() {
        openShiftHttpsProxyPort.resolve()
    }

    public int getGcmMockServerPort() {
        gcmMockServerPort.resolve()
    }

    public int getAerogearApnsPushPort() {
        aerogearApnsPushPort.resolve()
    }

    public int getAerogearApnsFeedbackPort() {
        aerogearApnsFeedbackPort.resolve()
    }

    public String getOpenShiftAppDirectory() {
        openShiftAppDirectory.resolve()
    }

    public String getUnifiedPushTestExtensionVersion() {
        unifiedPushTestExtensionVersion.resolve()
    }

    @Override
    public void install(Logger logger) {

        println 'Creating UPS OpenShift cartridge.'

        File repositoryFile = null

        File repository = getOpenShiftRepository()

        if (repository == null || !repository.exists() || !repository.isDirectory()) {
            repositoryFile = File.createTempDir()
        } else {
            repositoryFile = repository
        }

        println "UPS OpenShift repository will be cloned to ${repositoryFile.canonicalPath}"

        Spacelift.task(CreateOpenShiftCartridge)
                .server(getOpenShiftServer())
                .named(getOpenShiftAppName())
                .at(getOpenShiftNamespace())
                .sized(getOpenShiftGearSize())
                .username(getOpenShiftUsername())
                .password(getOpenShiftPassword())
                .token(getOpenShiftToken())
                .cartridges(getOpenShiftCartridges())
                .scale(getOpenShiftScale())
                .ignoreIfExists(getOpenShiftIgnoreIfExists())
                .force(getOpenShiftForce())
                .checkout(getOpenShiftCheckout())
                .repo(repositoryFile.canonicalPath)
                .gitSsh(getGitSsh())
                .execute().await()

        ProcessResult ipGetResult = Spacelift.task(CommandTool).programName("rhc")
                .parameters('ssh')
                .parameters('--command', 'echo $OPENSHIFT_UNIFIED_PUSH_IP$OPENSHIFT_AEROGEAR_PUSH_IP$OPENSHIFT_JBOSS_UNIFIED_PUSH_IP$MYTESTIP_1')
                .parameters('-a', getOpenShiftAppName())
                .parameters('-n', getOpenShiftNamespace())
                .parameters('-l', getOpenShiftUsername())
                .parameters('-p', getOpenShiftPassword())
                .execute().await()

        String ip = ipGetResult.output().last()

        if (turnProxyOn) {

            StringBuilder sb = new StringBuilder()

            String SPACE = " "

            sb.append("JAVA_OPTS_EXT=")
                    .append("-Dhttp.proxyHost=$ip").append(SPACE)
                    .append("-Dhttp.proxyPort=" + getOpenShiftHttpProxyPort()).append(SPACE)
                    .append("-Dhttps.proxyHost=$ip").append(SPACE)
                    .append("-Dhttps.proxyPort=" + getOpenShiftHttpsProxyPort()).append(SPACE)
                    .append("-Dgcm.mock.server.port=" + getGcmMockServerPort()).append(SPACE)
                    .append("-Dcustom.aerogear.apns.push.host=$ip").append(SPACE)
                    .append("-Dcustom.aerogear.apns.push.port=" + getAerogearApnsPushPort()).append(SPACE)
                    .append("-Dcustom.aerogear.apns.feedback.host=$ip").append(SPACE)
                    .append("-Dcustom.aerogear.apns.feedback.port=" + getAerogearApnsFeedbackPort()).append(SPACE)

            String JAVA_OPTS_EXT = sb.toString()

            println JAVA_OPTS_EXT

            Spacelift.task(CommandTool).programName("rhc")
                    .parameters('set-env', JAVA_OPTS_EXT)
                    .parameters('-a', getOpenShiftAppName())
                    .parameters('-n', getOpenShiftNamespace())
                    .parameters('-l', getOpenShiftUsername())
                    .parameters('-p', getOpenShiftPassword())
                    .execute().await()
        }

        def deployFile = "mv ~/app-root/repo/unifiedpush-test-extension-server.war ~/" + getOpenShiftAppDirectory() + "/standalone/deployments/unifiedpush-test-extension-server.war"
        def actionHooksDir = new File(repositoryFile.canonicalPath, '.openshift/action_hooks/')
        actionHooksDir.mkdirs()

        List<File> addedFiles = Spacelift.task(WriteToFileTool)
                .write(deployFile)
                .to(new File(actionHooksDir, 'deploy'))
                .execute().await()

        String testExtensionWarPath = new StringBuilder()
                .append(((File) parent['workspace']).canonicalPath)
                .append("/.repository/org/jboss/aerogear/test/unifiedpush-test-extension-server/").append(getUnifiedPushTestExtensionVersion())
                .append("/unifiedpush-test-extension-server-").append(getUnifiedPushTestExtensionVersion()).append(".war")
                .toString()

        def warFileSource = new File(testExtensionWarPath)
        def warFileTarget = new File(repositoryFile, 'unifiedpush-test-extension-server.war')

        Files.copy(warFileSource, warFileTarget)

        Spacelift.task(repositoryFile, GitAddTool)
                .add(addedFiles)
                .add(warFileTarget)
                .execute().await()

        addedFiles.each { addedFile ->
            println addedFile.canonicalPath
            Spacelift.task(CommandTool)
                    .workingDirectory(repositoryFile.canonicalPath)
                    .programName("git")
                    .parameters('update-index', '--chmod=+x', addedFile.canonicalPath)
                    .interaction(new ProcessInteractionBuilder().when(".*").printToOut())
                    .execute().await()
        }

        println 'Added files to OpenShift repository'

        println 'Committing unifiedpush-test-extension-server.war into the OpenShift repository.'

        Spacelift.task(repositoryFile, GitCommitTool).message('Add test extension war.').execute().await()

        File gitSshFile = getGitSsh()

        println 'Pushing added unifiedpush-test-extension-server.war into the OpenShift repository'

        Spacelift.task(repositoryFile, GitPushTool).gitSsh(gitSshFile).execute().await()

        final String baseUri = "https://" + getOpenShiftAppName() + "-" + getOpenShiftNamespace() + ".rhcloud.com/unifiedpush-test-extension-server"

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
                .parameters('-a', getOpenShiftAppName())
                .parameters('-n', getOpenShiftNamespace())
                .parameters('-l', getOpenShiftUsername())
                .parameters('-p', getOpenShiftPassword())
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

        if (turnProxyOn) {
            println "Trying to activate proxy."

            Awaitility.await().atMost(5, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                        @Override
                        Boolean call() throws Exception {
                            Response response = RestAssured.given().baseUri(baseUri).get("/proxy/activate")

                            return response.statusCode == 200
                        }
                    })

            println "Proxy was activated."
        }
    }
}
