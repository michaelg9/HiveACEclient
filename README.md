# HiveMQ MQTT Client for ACE
This client supports the mqtt-tls profile for ACE (Authentication and Authorization for Constrained Environments) described [here](https://art.tools.ietf.org/html/draft-sengul-ace-mqtt-tls-profile-04). 

## Dependencies

The client needs to be registered with an ACE AS server and requires an MQTT broker that supports ACE authentication. There are existing implementations:
- A modified version of the HiveMQ CE because currently the official broker doesn't expose the extended authentication features to extensions. There is an implementation [here](https://github.com/michaelg9/hivemq-community-edition)
- A fully v5 compliant MQTT client that supports ACE, which can be found [here](https://github.com/michaelg9/HiveACEclient)
- An ACE authorization server running OAuth2, which can be found [here](https://github.com/nominetresearch/ace-mqtt-mosquitto)
