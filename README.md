# AeroGear Unified Push Server Integration Tests [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests.svg?branch=master)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests)

This project contains the integration tests and functional tests for the [Aerogear UnifiedPush Server](https://github.com/aerogear/aerogear-unified-push-server) project.

## Running the tests

We switched to gradle for running our test suite. To run the test suite now, you just run the following from your commandline:

`./gradlew -Pprofile={selected profile} test`

Just replace the `{selected profile}` with a profile you want to run. You can find the list of profiles below. You can also override one of many properties by adding `-P{property name}={property value}` at the end of the command (without the curly brackets).

To learn more go to [https://github.com/arquillian/arquillian-spacelift-gradle-plugin](https://github.com/arquillian/arquillian-spacelift-gradle-plugin).

## Profiles

### WildFly 8 profiles

Runs the integration tests against WildFly 8 instance. It is recommended to use one of these profiles because they need little to no configuration.

#### wildfly8UpsProvided

You need to provide the WAR files that will be deployed using properties:

* `unifiedPushServerWar`  
    Path to the WAR file of UnifiedPush Server for WildFly 8.
    
* `unifiedPushAuthServerWar`  
    Path to the WAR filr of UnifiedPush Auth Server.
    
* `runMigrator` and `unifiedPushMigratorDist`  
    If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.

#### wildfly8UpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### wildfly8UpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### EAP 6 profiles

Runs the integration tests against EAP6 instance. You need to provide the download URL for the EAP.

#### eap6UpsProvided

You need to provide the WAR files that will be deployed using properties:

* `unifiedPushServerWar`  
    Path to the WAR file of UnifiedPush Server for JBoss AS7.
    
* `unifiedPushAuthServerWar`  
    Path to the WAR filr of UnifiedPush Auth Server.
    
* `runMigrator` and `unifiedPushMigratorDist`  
    If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.

#### eap6UpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### eap6UpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### Any application server profiles

Runs the integration tests against external application server instance. This means you need to provide the jboss home with a configured application server using the property `jbossHome`.

#### anyUpsProvided

You need to provide the WAR files that will be deployed using properties:

* `unifiedPushServerWar`  
    Path to the WAR file of UnifiedPush Server for the application server you want to run on. 
    
* `unifiedPushAuthServerWar`  
    Path to the WAR filr of UnifiedPush Auth Server.
    
* `runMigrator` and `unifiedPushMigratorDist`  
    If you set the former property to `true`, you need to provide path to the migrator distribution file using the latter.


#### anyUpsFromSource

The tested UnifiedPush Server will be downloaded and built from GitHub repository.

#### anyUpsFromMavenRepository

The tested UnifiedPush Server will be downloaded from Maven repository.

### openshiftUpsFromCart

Runs the integration tests against an OpenShift gear.

#### Required properties:

##### openShiftUsername
Your login name for OpenShift. Used to create the gear under your account.

##### openShiftPassword
Your password for OpenShift. Used to create the gear under your account.

#### Optional properties:

##### openShiftNamespace
*default*: `mobileqa`  
This is the OpenShift namespace in which the gear will be created.

##### openShiftAppName
*default*: `upsintegrationtests`  
The name which will be used for the created gear.

##### openShiftGearSize
*default*: `medium`  
Size of the gear created. If you use wildfly-based UPS cartridge, you will need medium-sized gear or larger.

##### openShiftCartridge
*default*: `-- add community cartridge url --`  
Url to the cartridge which will be used to create the gear. If you change this, be sure to have `openShiftAppDirectory` set right.

##### openShiftAppDirectory
*default*: `aerogear-push`  
This should be the name of the cartridge. It is needed for the tests to know where to deploy test extension archive.

##### openShiftRecreate
*default*: `'true'`  
Each time you run the tests the cartridge gets recreated, unless you set this property to `'false'`.


### helloWorldCordovaQuickstart

#### Required properties

##### googleProjectNumber

##### googleKey

##### quickstartPushApplicationId

##### quickstartPushApplicationMasterSecret

### shootAndShareCordovaQuickstart

### adminUIOpenshift
Runs Admin UI functional tests against OpenShift.

You can use this optional property to with this profile:
* `browser`
    Browser to be used. Can be one of `chrome`, `firefox`, `internetExplorer`, `phantomjs`, `safari`. Firefox will be used if the property is not specified.
