package org.jboss.aerogear.test.cli;

import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Help;

public class UpteCli {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CliBuilder<Runnable> builder = Cli.<Runnable> builder("upte")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, 
                        AppCreateCommand.class, 
                        DataGeneratorCommand.class, 
                        CleanupCommand.class);

        builder.build().parse(args).run();
    }

}