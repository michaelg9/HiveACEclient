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
        final int repeatIterations = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
        final int repeatDelay = args.length >= 4 ? Integer.parseInt(args[3]) : 1;
        final boolean withAuthentication = args.length < 5 || Boolean.parseBoolean(args[4]);
        switch (usecase) {
            case "challenge":
                com.ace.mqtt.examples.challenge.Example.run(configFile, repeatIterations, repeatDelay);
                break;
            case "publisher":
                com.ace.mqtt.examples.publisher.Example.run(configFile, repeatIterations, repeatDelay, withAuthentication);
                break;
            case "simplev3":
                com.ace.mqtt.examples.simplev3.Example.run(configFile, repeatIterations, repeatDelay, withAuthentication);
                break;
            case "simplev5":
                com.ace.mqtt.examples.simplev5.Example.run(configFile, repeatIterations, repeatDelay, withAuthentication);
                break;
            case "subscriber":
                com.ace.mqtt.examples.subscriber.Example.run(configFile, withAuthentication);
                break;
            default:
            case "unsecure":
                com.ace.mqtt.examples.unsecure.Example.run(configFile);
                break;
        }
    }
}
