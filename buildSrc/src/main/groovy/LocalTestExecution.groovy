import com.jayway.awaitility.Awaitility
import com.jayway.restassured.RestAssured
import com.jayway.restassured.response.Response
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.BaseContainerizableObject
import org.arquillian.spacelift.gradle.DefaultTest
import org.arquillian.spacelift.gradle.DeferredValue
import org.arquillian.spacelift.gradle.Test
import org.arquillian.spacelift.task.InvalidTaskException
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration
import org.jboss.aerogear.test.container.spacelift.JBossCLI
import org.jboss.aerogear.test.container.spacelift.JBossStarter
import org.jboss.aerogear.test.container.spacelift.JBossStopper
import org.slf4j.Logger

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class LocalTestExecution extends BaseContainerizableObject<LocalTestExecution> implements Test {


    /** From DefaultTest */
    DeferredValue<Void> execute = DeferredValue.of(Void.class)

    DeferredValue<List> dataProvider = DeferredValue.of(List.class).from([null])

    DeferredValue<Void> beforeSuite = DeferredValue.of(Void.class)

    DeferredValue<Void> beforeTest = DeferredValue.of(Void.class)

    DeferredValue<Void> afterSuite = DeferredValue.of(Void.class)

    DeferredValue<Void> afterTest = DeferredValue.of(Void.class)

    def jbossManager

    DeferredValue<Map> database = DeferredValue.of(Map.class).from([jdbc_url: "", username: "", password: "", driver: ""])

    DeferredValue<Boolean> runMigrator = DeferredValue.of(Boolean.class).from(true)

    DeferredValue<File> unifiedPushBinariesDir = DeferredValue.of(File.class)

    DeferredValue<File> jbossHome = DeferredValue.of(File.class)

    DeferredValue<JBossManagerConfiguration> jbossManagerConfiguration = DeferredValue.of(JBossManagerConfiguration.class)

    DeferredValue<String> ipVersion = DeferredValue.of(String.class).from("IPv4")

    DeferredValue<String> localDomain = DeferredValue.of(String.class).from("127.0.0.1")

    DeferredValue<String> bindAddress = DeferredValue.of(String.class).from("127.0.0.1")

    DeferredValue<File> truststore = DeferredValue.of(File.class)

    DeferredValue<String> truststorePassword = DeferredValue.of(String.class)

    DeferredValue<File> keystore = DeferredValue.of(File.class)

    DeferredValue<String> keystorePassword = DeferredValue.of(String.class)

    DeferredValue<Integer> apnsPushPort = DeferredValue.of(Integer.class).from(16002)

    DeferredValue<Integer> apnsFeedbackPort = DeferredValue.of(Integer.class).from(16003)

    DeferredValue<File> apnsKeystore = DeferredValue.of(File.class)

    DeferredValue<String> apnsKeystorePassword = DeferredValue.of(String.class)

    DeferredValue<String> apnsKeystoreType = DeferredValue.of(String.class).from("JKS")

    DeferredValue<Integer> gcmPushPort = DeferredValue.of(Integer.class).from(16010)

    DeferredValue<File> gcmCertificate = DeferredValue.of(File.class)

    DeferredValue<File> gcmCertificateKey = DeferredValue.of(File.class)

    DeferredValue<Integer> httpProxyPort = DeferredValue.of(Integer.class).from(16000)

    DeferredValue<String> cleanTask = DeferredValue.of(String.class)

    DeferredValue<String> testTask = DeferredValue.of(String.class)

    DeferredValue<String> externalGradleParameters = DeferredValue.of(String.class)

    DeferredValue<List<String>> protocols = DeferredValue.of(List.class).from(['http'])

    LocalTestExecution(String testName, Object parent) {
        super(testName, parent)

        dataProvider.from({ protocols.resolve() as ArrayList })
        beforeSuite.from({ setupContainer() })
        execute.from({ protocol -> runTestExecution(protocol) })
        afterSuite.from({ shutdownContainer() })
    }

    LocalTestExecution(String testName, LocalTestExecution other) {
        super(testName, other)

        this.execute = other.@execute.copy()
        this.dataProvider = other.@dataProvider.copy()
        this.beforeSuite = other.@beforeSuite.copy()
        this.beforeTest = other.@beforeTest.copy()
        this.afterSuite = other.@afterSuite.copy()
        this.afterTest = other.@afterTest.copy()

        database = other.@database.copy()
        runMigrator = other.@runMigrator.copy()
        unifiedPushBinariesDir = other.@unifiedPushBinariesDir.copy()
        jbossHome = other.@jbossHome.copy()
        jbossManagerConfiguration = other.@jbossManagerConfiguration.copy()
        ipVersion = other.@ipVersion.copy()
        localDomain = other.@localDomain.copy()
        bindAddress = other.@bindAddress.copy()
        truststore = other.@truststore.copy()
        truststorePassword = other.@truststorePassword.copy()
        keystore = other.@keystore.copy()
        keystorePassword = other.@keystorePassword.copy()
        apnsPushPort = other.@apnsPushPort.copy()
        apnsFeedbackPort = other.@apnsFeedbackPort.copy()
        apnsKeystore = other.@apnsKeystore.copy()
        apnsKeystorePassword = other.@apnsKeystorePassword.copy()
        apnsKeystoreType = other.@apnsKeystoreType.copy()
        gcmPushPort = other.@gcmPushPort.copy()
        gcmCertificate = other.@gcmCertificate.copy()
        gcmCertificateKey = other.@gcmCertificateKey.copy()
        httpProxyPort = other.@httpProxyPort.copy()
        testTask = other.@testTask.copy()
        externalGradleParameters = other.@externalGradleParameters.copy()
        protocols = other.@protocols.copy()
    }

    void setupContainer() {
        final def baseUri = "http://${localDomain.resolve()}:8080/unifiedpush-test-extension-server"
        final def jbossManagerConfiguration = jbossManagerConfiguration.resolve()
        final def jbossHome = jbossHome.resolve()
        jbossManagerConfiguration.setJBossHome(jbossHome.canonicalPath)


        def javaOpts = [
                "-Dhttp.proxyHost=${bindAddress.resolve()}",
                "-Dhttp.proxyPort=${httpProxyPort.resolve()}",
                "-Dhttps.proxyHost=${bindAddress.resolve()}",
                "-Dhttps.proxyPort=${httpProxyPort.resolve()}",
                "-Djboss.bind.address=${bindAddress.resolve()}",
                "-Djavax.net.ssl.trustStore=${truststore.resolve().canonicalPath}",
                "-Djavax.net.ssl.trustStorePassword=${truststorePassword.resolve()}",
                "-Dcustom.aerogear.apns.push.host=${bindAddress.resolve()}",
                "-Dcustom.aerogear.apns.push.port=${apnsPushPort.resolve()}",
                "-Dcustom.aerogear.apns.feedback.host=${bindAddress.resolve()}",
                "-Dcustom.aerogear.apns.feedback.port=${apnsFeedbackPort.resolve()}",
                "-Dcustom.aerogear.apns.keystore.path=${apnsKeystore.resolve().canonicalPath}",
                "-Dcustom.aerogear.apns.keystore.password=${apnsKeystorePassword.resolve()}",
                "-Dcustom.aerogear.apns.keystore.type=${apnsKeystoreType.resolve()}",
                "-Dgcm.mock.certificate.path=${gcmCertificate.resolve().canonicalPath}",
                "-Dgcm.mock.certificate.password=${gcmCertificateKey.resolve().canonicalPath}",
                "-Dgcm.mock.server.port=${gcmPushPort.resolve()}"]

        if (ipVersion.resolve() == "IPv6") {
            javaOpts << "-Djava.net.preferIPv4Stack=false"
            javaOpts << "-Djava.net.preferIPv6Addresses=true"
        }

        // append javaOpts
        jbossManagerConfiguration.setJavaOpts(jbossManagerConfiguration.getJavaOpts() as String[])
        jbossManagerConfiguration.setJavaOpts(javaOpts as String[])

        jbossManager = Spacelift.task(JBossStarter)
                .configuration(jbossManagerConfiguration)
                .execute().await()

        // undeploy content
        ['auth-server.war', "ag-push.war", 'unifiedpush-test-extension-server.war'].each { war ->
            // We need the try-catch because there doesn't seem to be a way to ignore the 1 output from jboss-cli
            try {
                println "Undeploying ${war}"
                Spacelift.task(JBossCLI)
                        .environment("JBOSS_HOME", jbossHome.canonicalPath)
                        .connect()
                        .cliCommand("undeploy ${war}")
                        .execute().await()

                println "${war} undeployed successfully"
            } catch (Exception e) {
                println "${war} undeploy failed"
            }
        }

        // run migrator
        if (runMigrator.resolve()) {
            try {
                def db = database.resolve()

                Spacelift.task('migrator')
                        .parameter("--url=${db.jdbc_url}")
                        .parameter("--username=${db.username}")
                        .parameter("--password=${db.password}")
                        .parameter("--driver=${db.driver}")
                        .parameter("update")
                        .execute().await()
            }
            catch (InvalidTaskException e) {
                throw new RuntimeException("Migrator tool was not registered but set to run via project.unifiedPush.runMigrator flag." +
                        "Make sure that UPS installation provides migrator tool.", e)
            }
        }


        ['auth-server.war', "ag-push.war", 'unifiedpush-test-extension-server.war'].each { war ->
            println "Deploying ${war}"

            Spacelift.task(JBossCLI)
                    .environment("JBOSS_HOME", jbossHome.canonicalPath)
                    .connect()
                    .cliCommand("deploy ${new File(unifiedPushBinariesDir.resolve(), war).canonicalPath}")
                    .execute().await()
        }

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

        println 'Waiting for Keycloak to be initialized and ready'
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

        println 'Restarting container.'
        Spacelift.task(jbossManager, JBossStopper).execute().await()

        jbossManager = Spacelift.task(JBossStarter)
                .configuration(jbossManagerConfiguration)
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

        println 'Activating GCM and APNS proxy'

        RestAssured.given()
                .baseUri(baseUri)
                .get('/proxy/activate')
                .then().statusCode(200)
        println 'Proxy activated'
    }

    void runTestExecution(String protocol) {
        def port = protocol == 'https' ? 8443 : 8080
        def baseUri = "${protocol}://${localDomain.resolve()}:$port"

        println "Using external maven parameters: ${externalGradleParameters.resolve()}"
        println "Using base uri: $baseUri"

        // run tests
        def integrationTests = Spacelift.task('gradlew')
                .parameter(cleanTask.resolve())
                .parameter(testTask.resolve())
                .parameter("-PcontainerUri=$baseUri")
        // we need to propage keystore/truststore setup so test can confirm
        // authenticity of locally runing server
                .parameter("-Pkeystore=${keystore.resolve().canonicalPath}")
                .parameter("-PkeystorePass=${keystorePassword.resolve()}")
                .parameter("-Ptruststore=${truststore.resolve().canonicalPath}")
                .parameter("-PtruststorePass=${truststorePassword.resolve()}")
                .parameter("-PignoreTestFailures=true")

        externalGradleParameters.resolve().split().each {
            integrationTests.parameter("-P$it")
        }

        integrationTests.execute().await()
    }

    void shutdownContainer() {
        Spacelift.task(jbossManager, JBossStopper).execute().await()
    }

    @Override
    public LocalTestExecution clone(String name) {
        return new LocalTestExecution(name, this);
    }

    @Override
    void executeTest(Logger logger) {

        // before suite
        try {
            logger.info(":test:${name} before suite execution")
            beforeSuite.resolve()
        }
        catch(Exception e) {
            logger.error(":test:${name} failed before suite phase: ${e.getMessage()}")
            throw e
        }

        Exception cause = null

        // in case anything in this try block fails, we will still run the `after suite` in the finally block
        try {
            // iterate through beforeTest, execute and afterTest based on data provider
            dataProvider.resolve().each { data ->

                String dataString =  data ? " (${data})" : ""

                try {
                    logger.info(":test:${name} before test execution${dataString}")
                    beforeTest.resolveWith(this, data)
                }
                catch(Exception e) {
                    logger.error(":test:${name} failed before test phase: ${e.getMessage()}")
                    throw e
                }

                // in case anything in this try block fails, we will still run the `after test` in the finally block
                try {
                    logger.invokeMethod("lifecycle", ":test:${name}${dataString}")
                    execute.resolveWith(this, data)
                }
                catch(Exception e) {
                    logger.error(":test:${name} failed execute phase: ${e.getMessage()}")
                    cause = e
                }
                // clean up
                finally {
                    try {
                        logger.info(":test:${name} after test execution${dataString}")
                        afterTest.resolveWith(this, data)
                    }
                    catch(Exception e) {
                        logger.error(":test:${name} failed after test phase: ${e.getMessage()}")
                        if(cause==null) {
                            cause = e
                        }
                    }
                    if(cause) {
                        throw cause
                    }
                }
            }
        } finally {
            // after suite
            try {
                logger.info(":test:${name} after suite execution")
                afterSuite.resolve()
            }
            catch(Exception e) {
                logger.error(":test:${name} failed after suite phase: ${e.getMessage()}")
                if(cause==null) {
                    cause = e
                }
            }
            finally {
                if(cause) {
                    throw cause
                }
            }
        }
    }

}
