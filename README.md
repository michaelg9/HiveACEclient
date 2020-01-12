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
- Create a client certificate and register it with the broker's trust store
- Run any of the use cases provided

### How to create server and client side TLS keys

The detailed steps can be found [here](https://www.hivemq.com/docs/4.2/hivemq/howtos.html).
**Note:** Change the password from changeme from the below steps.

- Create a private key for the broker in the java key store format:
    ```bash
    keytool -genkey -keyalg RSA -alias hivemq -keystore hivemq.jks -storepass changeme -validity 360 -keysize 2048
    ```

- Export the broker's certificate from the key store:
    ```bash
    keytool -export -keystore hivemq.jks -alias hivemq -storepass your-keystore-password -file hivemq-server.crt
    ```

- Create a universal (usable by any client) client trust store that trusts the broker:
    ```bash
    keytool -import -file hivemq-server.crt -alias HiveMQ -keystore mqtt-client-trust-store.jks -storepass changeme
    ```

- Generate a per client private key as a java key store:
    ```bash
    keytool -genkey -keyalg RSA -alias mqtt-paho-client-1 -keystore mqtt-paho-client-1.jks -storepass changeme -validity 360 -keysize 4096
    ```

- Make the per client certificate trusted from the broker:
    1. Export the per client certificate from the key store:
        ```bash
        keytool -export -keystore mqtt-paho-client-1.jks -alias mqtt-paho-client-1 -storepass your-client-keystore-password -file mqtt-paho-client-1.crt
        ```
   2. Add the per client certificate in the broker's trust store
        ```bash
        keytool -import -file mqtt-paho-client-1.crt -alias client1 -keystore hivemq-trust-store.jks -storepass changeme
        ```
