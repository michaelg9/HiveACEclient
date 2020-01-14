# How to create server and client side TLS keys

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
