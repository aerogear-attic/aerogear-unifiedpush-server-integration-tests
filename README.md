# AeroGear Unified Push Server Integration Tests [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests.svg?branch=master)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests)

This project contains the integration tests and functional tests for the [Aerogear UnifiedPush Server](https://github.com/aerogear/aerogear-unified-push-server) project.

## Running the tests

We switched to gradle for running our test suite. To run the test suite now, you just run the following from your commandline:

`./gradlew :spacelift:test -Pprofile={selected profile}`

Just replace the `{selected profile}` with a profile you want to run. You can find the list of profiles below. You can also override one of many properties by adding `-P{property name}={property value}` at the end of the command (without the curly brackets).

The easiest way to get the tests running is the following command:

`./gradlew --stacktrace :spacelift:test -Pwildfly8UpsFromSource -Pdatasource=mysql -PrunMigrator=false`

The only requirement is that you have a running mysql instance with a `unifiedpush` and `keycloak` databases. The exact way to setup the database is described in the UnifiedPush Server help.

To learn more go to [https://github.com/arquillian/arquillian-spacelift-gradle-plugin](https://github.com/arquillian/arquillian-spacelift-gradle-plugin).

## Structure

This project consists of three main parts. Tools, tests and Spacelift build script.

### Tools

#### aerogear-ups-rest-api

Library made for easy access to UnifiedPush Server REST API. The code is located in module :tools:aerogear-ups-rest-api.

#### test-extension

This module contains three submodules: `:common`, `:client` and `:server`. The `:server` module is a WAR which you can deploy along with the UnifiedPush Server and it will provide you with some REST endpoints used to automate UPS testing suite. The `:client` module can be used to access the REST endpoints of the `:server` using a Java API. And the `:common` is basically just code shared between the `:client` and `:server`.

##### Server endpoints

* `GET /status`: Simply returns HTTP OK (200) response. It can be used to detect when the `:server` WAR file has been deployed into the container.

* `GET /cleanup/applications`: Loads all created Push Applications from the UnifiedPush Server datasource and deletes them. This will also delete all variants and installations.

* `GET /keycloak`: Reconfigures keycloak to allow direct grant access, which basically means authentication using REST api and disables requirement for password change as password cannot be changed using Keycloak's REST API.

* `GET /keycloak/realms`: Returns list of realms registered. This is used primarily to wait until the keycloak loads configuration from JSON into database.

* `POST /datagenerator`: Generates testing data in the UPS datasource, based on the DataGeneratorConfig which is required as a json body of the request.

* `GET /senderStats`: Returns current sender statistics. It represents the message and device tokens were requested to receive when the GCM and APNS proxy is used.

* `DELETE /senderStats`: Resets the sender statistics.

* `POST /javaSenderTest`: Sends a message over the UnifiedPush Server using the given parameters. The purpose of this endpoint is to test sending messages when having both WARs in the same container. It takes four form parameters:
    * `pushAppId`: ID of the application the message will be sent to.
    * `secret`: Secret for the application.
    * `serverUrl`: URL where the UnifiedPush Server is located.
    * `alert`: Text that will be sent as the `alert` parameter of the message.

* `GET /proxy/activate`: Activates the GCM and APNS simulators/proxy.

* `GET /proxy/deactivate`: Deactivates the GCM and APNS simulators/proxy.

### Tests

There are currently three test projects, `integration`, `admin-ui` and `performance` (although the `performance` are heavily outdated).

## Writing new tests and debugging

When writing new tests and debugging them it might be painful to run the whole suite over and over again as it takes about 4 minutes to complete the setup and run all the tests. It is however possible to start the container and run the tests yourself.

### Running the wildfly container

```
./spacelift/workspace/wildfly-8.2.0.Final/bin/standalone.sh \
    -b 0.0.0.0 \
    -c standalone-full.xml \
    -Dhttp.proxyHost=127.0.0.1 \
    -Dhttp.proxyPort=16000 \
    -Dhttps.proxyHost=127.0.0.1 \
    -Dhttps.proxyPort=16000 \
    -Djavax.net.ssl.trustStore=$(pwd)/spacelift/workspace/certs/aerogear.truststore \
    -Djavax.net.ssl.trustStorePassword=aerogear \
    -Dcustom.aerogear.apns.push.host=127.0.0.1 \
    -Dcustom.aerogear.apns.push.port=16002 \
    -Dcustom.aerogear.apns.feedback.host=127.0.0.1 \
    -Dcustom.aerogear.apns.feedback.port=16003 \
    -Dcustom.aerogear.apns.keystore.path=$(pwd)/spacelift/workspace/certs/apns_server.jks \
    -Dcustom.aerogear.apns.keystore.password=aerogear \
    -Dcustom.aerogear.apns.keystore.type=JKS \
    -Dgcm.mock.certificate.path=$(pwd)/spacelift/workspace/certs/gcm_mock.crt \
    -Dgcm.mock.certificate.password=$(pwd)/spacelift/workspace/certs/gcm_mock.key \
    -Dgcm.mock.server.port=16010
```
Run this from the root of the repository to fire up a WildFly container. If you have run the whole testsuite before, it should already have the UnifiedPush Server, Auth Server and Test Extension deployed. Before you run the tests you should open `http://localhost:8080/unifiedpush-test-extension-server/proxy/activate` to activate the GCM proxy and APNS simulator.

### Running the integration test suite agains the running container

`./gradlew :tests:integration:test -PcontainerUri=http://localhost:8080`

Running this command will run all the integration tests against a container running at `localhost` on port `8080`. It is also possible to run just one test class by adding `--tests org.jboss.aerogear.unifiedpush.test.MessageSendTest` or very similarly even a single test method `--tests org.jboss.aerogear.unifiedpush.test.MessageSendTest.selectiveSendWithInvalidTokens`.

## How to test new release

When a new release is staged, you need to open `build.gradle` and `spacelift/build.gradle` and update all the versions (this includes `aerogear-parent`, `keycloak` etc.) to the versions of the release. 

Then it should be as easy as running the command:

`./gradlew --stacktrace :spacelift:test -Pwildfly8UpsFromMavenRepository -Pdatasource=mysql -PrunMigrator=false`

This should run the integration tests which tests basically most of the internals through REST APIs. Then you should verify that the message delivery really does work. To do that you need to setup an application and create variants for the platforms you want to test. It is recommended to use the hellopush quickstart from https://github.com/jboss-mobile/unified-push-helloworld to test this as it is the easiest way. Once you deploy the hellopush apps onto the devices, just send a message from the UnifiedPush Server UI and verify that it did arrive on the device.

It is also recommended to do a thorough testing for each major and minor version (the versioning is `major.minor.point`), preferably when the first beta is staged. This testing is manual and consists of going through the UI and trying to click every button, switch every switch and so on. 

## Profiles

### WildFly 8 profiles

Runs the integration tests against WildFly 8 instance. It is recommended to use one of these profiles because they need little to no configuration.

#### wildfly8UpsProvided

You need to provide the WAR files that will be deployed using properties:

name | description
-----|-------------
`unifiedPushServerWar` | Path to the WAR file of UnifiedPush Server for WildFly 8.
`unifiedPushAuthServerWar` | Path to the WAR filr of UnifiedPush Auth Server.
`runMigrator` and `unifiedPushMigratorDist` | If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.

#### wildfly8UpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### wildfly8UpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### EAP 6 profiles

Runs the integration tests against EAP6 instance. You need to provide the download URL for the EAP.

#### eap6UpsProvided

You need to provide the WAR files that will be deployed using properties:

name | description
-----|------------
`unifiedPushServerWar` | Path to the WAR file of UnifiedPush Server for JBoss AS7.
`unifiedPushAuthServerWar` | Path to the WAR filr of UnifiedPush Auth Server.
`runMigrator` and `unifiedPushMigratorDist` | If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.

#### eap6UpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### eap6UpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### Any application server profiles

Runs the integration tests against external application server instance. This means you need to provide the jboss home with a configured application server using the property `jbossHome`.

#### anyUpsProvided

You need to provide the WAR files that will be deployed using properties:

name | description
-----|------------
`unifiedPushServerWar` | Path to the WAR file of UnifiedPush Server for the application server you want to run on.
`unifiedPushAuthServerWar` | Path to the WAR filr of UnifiedPush Auth Server.
`runMigrator` and `unifiedPushMigratorDist` | If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.


#### anyUpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### anyUpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### openshiftUpsFromCart

Runs the integration tests against an OpenShift gear.

#### Required properties:

name | description
-----|------------
`openShiftUsername` | Your login name for OpenShift. Used to create the gear under your account.
`openShiftPassword` | Your password for OpenShift. Used to create the gear under your account.

#### Optional properties:

name | default value | description
-----|---------------|---------------
`openShiftNamespace` | `mobileqa` | This is the OpenShift namespace in which the gear will be created.
`openShiftAppName`   | `upsintegrationtests` | The name which will be used for the created gear.
`openShiftGearSize`  | `medium` | Size of the gear created. If you use wildfly-based UPS cartridge, you will need medium-sized gear or larger.
`openShiftCartridge` | `-- add community cartridge url --` | Url to the cartridge which will be used to create the gear. If you change this, be sure to have `openShiftAppDirectory` set right.
`openShiftAppDirectory` | `aerogear-push` | This should be the name of the cartridge. It is needed for the tests to know where to deploy test extension archive.
`openShiftRecreate` | `'true'` | Each time you run the tests the cartridge gets recreated, unless you set this property to `'false'`.


### helloWorldCordovaQuickstart

#### Required properties

name | description
-----|-------------
`googleProjectNumber` |
`googleKey` |
`quickstartPushApplicationId` |
`quickstartPushApplicationMasterSecret` |

### shootAndShareCordovaQuickstart

### adminUIOpenshift
Runs Admin UI functional tests against OpenShift.

You can use this optional property to with this profile:

name | default value | description
-----|---------------|-------------
`browser` | `firefox` | Browser to be used. Can be one of `chrome`, `firefox`, `internetExplorer`, `phantomjs`, `safari`.
