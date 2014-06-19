# AeroGear Unified Push Server Integration Tests [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests.png?branch=master)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-integration-tests)

This project contains the integration tests for the [aerogear-unified-push-server](https://github.com/aerogear/aerogear-unified-push-server) project.

The [Arquillian](http://arquillian.org/) testing platform is used to enable the testing automation. Arquillian integrates with the testing framework which is JUnit in this case.

## Integration Tests Content
Each Test defines the three core aspects needed for the execution of an [Arquillian](http://arquillian.org/) test case:

- container — the runtime environment
- deployment — the process of dispatching an artifact to a container
- archive — a packaged assembly of code, configuration and resources

The container's configuration resides in the [Arquillian XML](https://github.com/aerogear/aerogear-unifiedpush-server-integration-tests/blob/master/src/test/resources/arquillian.xml) configuration file while the deployment and the archive are defined in the [Deployments](https://github.com/aerogear/aerogear-unifiedpush-server-integration-tests/blob/master/src/test/groovy/org/jboss/aerogear/unifiedpush/common/Deployments.groovy) file.

The test case is dispatched to the container's environment through coordination with ShrinkWrap, which is used to declaratively define a custom archive that encapsulates the test class and its dependent resources. Arquillian packages the ShrinkWrap defined archive at runtime and deploys it to the target container. It then negotiates the execution of the test methods and captures the test results using remote communication with the server. Finally, Arquillian undeploys the test archive.

## Execution
Navigate to the project's root folder and execute:

    ./setup/setup.sh

This step does the following:

* Grabs AS 7.1.1 installation from jboss.org site
* Adds HTTPS connector to server configuration
* Clones repository of aerogear-unifiedpush-server into aerogear-unifiedpush-server directory

Navigate to the project's root folder and setup the `JBOSS_HOME` environment variable:

    export JBOSS_HOME=`pwd`/jboss-as-7.1.1.Final

The integration tests execution is done through Maven. Navigate to the project's root folder and execute:

    mvn test

If you want to execute tests using domain mode, just run:

    mvn test -Pas711-domain-managed
    
Profile will automatically activate different arquillian.xml and data initialization per session.

Note, if you need to run tests against different directory, just specify following:

    mvn test -Dunified.push.server.location=/path/to/pom.xml/that/should/be/build/instead

You can also run tests agains a remote maven repository using (it will use the latest version in the repository,
unless you specify `-Dups.version=x.y.z` where `x.y.z` is your desired version to run against):

    mvn test -Dups.source=remote -Dups.remote.url=http://remote.maven/repository

Or you can activate one of two profiles, `test-staging` or `test-release` which will run the test against the latest
version in the default repositories. You can also override the version using `-Dups.version=x.y.z`.

## Documentation

* [Arquillian Guides](http://arquillian.org/guides/)
