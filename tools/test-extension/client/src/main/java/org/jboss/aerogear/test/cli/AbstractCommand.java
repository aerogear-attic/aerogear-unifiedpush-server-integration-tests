package org.jboss.aerogear.test.cli;

import io.airlift.airline.Option;

public abstract class AbstractCommand implements Runnable {

    @Option(name = {"-a", "--app-name"},
            title = "app-name",
            description = "Name of the application on OpenShift")
    public String appName;

    @Option(name = {"-n", "--namespace"},
            title = "namespace",
            description = "Namespace on OpenShift, default value: mobileqa")
    public String namespace = "mobileqa";

    @Option(name = {"--uri"},
            title = "uri",
            description = "Direct way how to specify URI of UPS extension server") 
    public String uri;

    protected final String getUnifiedpushTestExtensionUri() {

        if(uri != null && uri != "") {
            return uri;
        }
        return "https://" + appName + "-" + namespace + ".rhcloud.com/unifiedpush-test-extension-server";
    }

}
