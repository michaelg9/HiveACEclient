# HiveMQ MQTT Client for ACE
This client supports the mqtt-tls profile for ACE (Authentication and Authorization for Constrained Environments) described [here](https://art.tools.ietf.org/html/draft-sengul-ace-mqtt-tls-profile-04). 

## Dependencies

The client needs to be registered with an ACE AS server and requires an MQTT broker that supports ACE authentication. There are existing implementations:
- A modified version of the HiveMQ CE because currently the official broker doesn't expose the extended authentication features to extensions. There is an implementation [here](https://github.com/michaelg9/hivemq-community-edition)
- A fully v5 compliant MQTT client that supports ACE, which can be found [here](https://github.com/michaelg9/HiveACEclient)
- An ACE authorization server running OAuth2, which can be found [here](https://github.com/nominetresearch/ace-mqtt-mosquitto)

## How to use
This library mainly provides the three different implementations of Mqtt5EnhancedAuthMechanism, the basic interface for providing extended authentication support for a HiveMQ MQTT client:
- EnhancedNoAuthDataMechanism: Includes no data in the in the payload of the CONNECTv5 packet, in order to discover the ACE AS server IP address from the broker and disconnect.
- EnhancedAuthDataMechanism: Includes both the token and the POP key in the payload of the CONNECTv5 packet, in order to authenticate with the broker without a challenge response.
- EnhancedAuthDataMechanismWithAuth: Includes only the token in the payload of the CONNECTv5 packet, in order to authenticate with the broker using a challenge AUTH.

Each of the above has a use case implemented in the examples directory.

## Example use cases provided
- DiscoverAS: Uses EnhancedNoAuthDataMechanism to discover the AS Server IP address and then authenticates using EnhancedAuthDataMechanism to the broker.
- ExtendedAuthentication: Uses EnhancedAuthDataMechanism or EnhancedAuthDataMechanismWithAuth according to the value of ExtendendAuthWithChallenge in the config.properties file.

## How to run the use cases
You need to configure the client and then just run the examples

- Register a client with the ACE AS server. Take a note of the client id and secret
- Record the client id and secret in the config.properties file under examples/src/main/resources, along with the details for the AS and RS server.
- Give appropriate policies to the client to subscribe or publish into a specific topic, using the ACE AS server API
- Run any of the use cases provided
