package com.ace.mqtt.examples;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.nimbusds.jose.JOSEException;

public class Main {
    public static void main(final String[] args)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException, InterruptedException {
        if (args.length < 2) System.exit(1);
        final String usecase = args[0];
        final String configFile = args[1];
        switch (usecase) {
            case "challenge":
                if (args.length == 3) com.ace.mqtt.examples.challenge.Example.run(configFile, Integer.parseInt(args[2]));
                com.ace.mqtt.examples.challenge.Example.run(configFile);
                break;
            case "publisher":
                if (args.length == 3) com.ace.mqtt.examples.publisher.Example.run(configFile, Integer.parseInt(args[2]));
                else com.ace.mqtt.examples.publisher.Example.run(configFile);
                break;
            case "simplev3":
                if (args.length == 3) com.ace.mqtt.examples.simplev3.Example.run(configFile, Integer.parseInt(args[2]));
                com.ace.mqtt.examples.simplev3.Example.run(configFile);
                break;
            case "simplev5":
                if (args.length == 3) com.ace.mqtt.examples.simplev5.Example.run(configFile, Integer.parseInt(args[2]));
                com.ace.mqtt.examples.simplev5.Example.run(configFile);
                break;
            case "subscriber":
                com.ace.mqtt.examples.subscriber.Example.run(configFile);
                break;
            default:
            case "unsecure":
                com.ace.mqtt.examples.unsecure.Example.run(configFile);
                break;
        }
    }
}
