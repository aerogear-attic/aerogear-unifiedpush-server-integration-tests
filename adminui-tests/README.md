# AeroGear Unified Push Server Admin UI Tests [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui-tests.png?branch=master)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui-tests)
This project contains the functional and acceptance tests for the aerogear-unified-push-server-admin-ui.

The [Arquillian](http://arquillian.org/) testing platform is used to enable the testing automation. Arquillian integrates transparently with the testing framework which is JUnit in this case.

## Functional Test Content
The Functional Test defines the three core aspects needed for the execution of an [Arquillian](http://arquillian.org/) test case:

- container — the runtime environment
- deployment — the process of dispatching an artifact to a container
- archive — a packaged assembly of code, configuration and resources

The container's configuration resides in the [Arquillian XML](https://github.com/aerogear/aerogear-unifiedpush-server-admin-ui-tests/blob/master/src/test/resources/arquillian.xml) configuration file while the deployment and the archive are defined in the [Deployments](https://github.com/aerogear/aerogear-unifiedpush-server-admin-ui-tests/blob/master/src/test/java/org/jboss/aerogear/unifiedpush/admin/ui/test/Deployments.java) file.

The test case is dispatched to the container's environment through coordination with ShrinkWrap, which is used to declaratively define a custom Java EE archive that encapsulates the test class and its dependent resources. Arquillian packages the ShrinkWrap defined archive at runtime and deploys it to the target container. It then negotiates the execution of the test methods and captures the test results using remote communication with the server. Finally, Arquillian undeploys the test archive.

The [POM](https://github.com/aerogear/aerogear-unifiedpush-server-admin-ui-tests/blob/master/pom.xml) file contains following profiles:

* arq-jboss-managed — managed container 
* arq-jboss-remote — remote container
* arq-openshift - represents a test against OpenShift cartridge

By default the arq-jboss-managed (managed container) profile is active. An Arquillian managed container is a remote container whose lifecycle is managed by Arquillian. The specific profile is also configured to download and unpack the JBoss Application Server 7 distribution zip from the Maven Central repository.

## Development approach/methodologies
The development approach is driven from the desire to decouple the testing algorithmic steps / scenarios from the implementation which is tied to a specific DOM structure. For that reason the Page Objects and Page Fragments patterns are used. The Page Objects pattern is used to encapsulate the tested page's structure into one class which contains all the page's parts together with all methods which you will find useful while testing it. The Page Fragments pattern encapsulates parts of the tested page into reusable pieces across all your tests.

## Functional Test Execution
The execution of the functional test is done through Maven:

    mvn clean test

Note, if you need to run tests against different directory, just specify following:

    mvn test -Dunified.push.server.location=/path/to/pom.xml/that/should/be/build/instead

You can also run tests agains a remote maven repository using (it will use the latest version in the repository,
unless you specify `-Dups.version=x.y.z` where `x.y.z` is your desired version to run against):

    mvn test -Dups.source=remote -Dups.remote.url=http://remote.maven/repository

Or you can activate one of two profiles, `test-staging` or `test-release` which will run the test against the latest
version in the default repositories. You can also override the version using `-Dups.version=x.y.z`.

### OpenShift test case

OpenShift deployment is tested using a special container that pretends that deployment was deployed to specific location. In order to prepare deployment, you need to execute following command:

    help-file/create_openshift_app.sh <login> <password> <appname> <github-repository-and-commit>

Then, making sure that *baseURI* and *contextRootRemap* represent created application in arquillian.xml, you run:

    mvn clean test -Parq-openshift

### Setup URI of your app

You can set these system properties in order to build your custom application URL

    openshift.app (by default agpush)
    openshift.namespace (by default mobileqa)
    openshift.brooker (by default rhcloud.com)

It will be transformed into this:

    https://${openshift.app}-${openshift.namespace}.${openshift.brooker}

## Documentation

* [Arquillian Guides](http://arquillian.org/guides/)
