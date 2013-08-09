# AeroGear Unified Push Server Integration Tests

This project contains the integration tests for the [aerogear-unified-push-server](https://github.com/aerogear/aerogear-unified-push-server) project.

The [Arquillian](http://arquillian.org/) testing platform is used to enable the testing automation. Arquillian integrates with the testing framework which is [Spock](https://code.google.com/p/spock/wiki/SpockBasics) in this case. Spock lets you write specifications that describe expected features (properties, aspects) exhibited by a system of interest.

## Integration Tests Content
Each Test defines the three core aspects needed for the execution of an [Arquillian](http://arquillian.org/) test case:

- container — the runtime environment
- deployment — the process of dispatching an artifact to a container
- archive — a packaged assembly of code, configuration and resources

The container's configuration resides in the [Arquillian XML](https://github.com/aerogear/aerogear-unifiedpush-server-integration-tests/blob/master//src/test/resources/arquillian.xml) configuration file while the deployment and the archive are defined in the [Deployments](https://github.com/aerogear/aerogear-unifiedpush-server-integration-tests/blob/master/src/test/groovy/org/jboss/aerogear/connectivity/common/Deployments.groovy) file.

The test case is dispatched to the container's environment through coordination with ShrinkWrap, which is used to declaratively define a custom archive that encapsulates the test class and its dependent resources. Arquillian packages the ShrinkWrap defined archive at runtime and deploys it to the target container. It then negotiates the execution of the test methods and captures the test results using remote communication with the server. Finally, Arquillian undeploys the test archive.

## Execution
Before executing the integration tests, you have to get the latest aerogear-unified-push-server source. Navigate to the project's root folder and execute:

    git submodule update --init --recursive
    git submodule foreach "git pull origin master && git checkout master"

The integration tests execution is done through Maven. Navigate to the project's root  folder and execute:

    mvn test

## Documentation

* [Arquillian Guides](http://arquillian.org/guides/)
