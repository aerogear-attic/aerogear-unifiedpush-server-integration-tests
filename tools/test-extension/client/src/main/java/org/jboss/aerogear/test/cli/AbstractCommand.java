package org.jboss.aerogear.test.cli;

import io.airlift.airline.Option;

public abstract class AbstractCommand implements Runnable {

    @Option(name = {"-u", "--uri"},
            title = "uri",
            description = "URI of the test extension war, such as http://localhost:8080/server")
    public String uri = "http://localhost:8080/server";

}
