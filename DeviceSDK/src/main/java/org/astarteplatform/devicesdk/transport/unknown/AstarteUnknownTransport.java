package org.astarteplatform.devicesdk.transport.unknown;

import org.astarteplatform.devicesdk.crypto.AstarteCryptoException;
import org.astarteplatform.devicesdk.protocol.AstarteAggregateDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteDatastreamInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterface;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceDatastreamMapping;
import org.astarteplatform.devicesdk.protocol.AstarteInterfaceMappingNotFoundException;
import org.astarteplatform.devicesdk.protocol.AstarteProtocolType;
import org.astarteplatform.devicesdk.transport.AstarteTransport;
import org.astarteplatform.devicesdk.transport.AstarteTransportException;
import org.astarteplatform.devicesdk.util.AstartePayload;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.joda.time.DateTime;

import java.util.Map;

public class AstarteUnknownTransport extends AstarteTransport {
    private final String baseTopic;

    public AstarteUnknownTransport(UnknownTransportConnectionInfo connectionInfo) {
        super(AstarteProtocolType.UNKNOWN_PROTOCOL);

        baseTopic = connectionInfo.getClientId();
    }

    @Override
    public void connect() throws AstarteTransportException, AstarteCryptoException {

    }

    @Override
    public void disconnect() throws AstarteTransportException {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void sendIntrospection() throws AstarteTransportException {
        // we won't do nothing
    }

    @Override
    public void sendEmptyCache() throws AstarteTransportException {
        // we won't do nothing
    }

    @Override
    public void resendAllProperties() throws AstarteTransportException {
        // we won't do nothing
    }

    @Override
    public void sendIndividualValue(AstarteInterface astarteInterface, String path, Object value, DateTime timestamp) throws AstarteTransportException {
        AstarteInterfaceDatastreamMapping mapping = null;
        int qos = 2;

        if (astarteInterface instanceof AstarteDatastreamInterface) {
            try {
                // Find a matching mapping
                mapping = (AstarteInterfaceDatastreamMapping) astarteInterface.findMappingInInterface(path);
            } catch (AstarteInterfaceMappingNotFoundException e) {
                throw new AstarteTransportException("Mapping not found", e);
            }

            qos = qosFromReliability(mapping);
        }

        String topic = baseTopic + "/" + astarteInterface.getInterfaceName() + path;
        byte[] payload =
                AstartePayload.serialize(value, (timestamp != null) ? timestamp.toDate() : null);

        if (astarteInterface instanceof AstarteDatastreamInterface) {
            handleDatastreamFailedPublish(mapping, topic, payload, qos);
        } else {
            handlePropertiesFailedPublish(topic, payload, qos);
        }
    }

    @Override
    public void sendAggregate(
            AstarteAggregateDatastreamInterface astarteInterface,
            String path,
            Map<String, Object> value,
            DateTime timestamp)
            throws AstarteTransportException {
        int qos;
        AstarteInterfaceDatastreamMapping mapping;
        try {
            // Find a matching mapping
            mapping =
                    (AstarteInterfaceDatastreamMapping) astarteInterface.getMappings().values().toArray()[0];
            qos = qosFromReliability(mapping);
        } catch (Exception e) {
            throw new AstarteTransportException("Mapping not found", e);
        }

        String topic = baseTopic + "/" + astarteInterface.getInterfaceName() + path;
        byte[] payload = AstartePayload.serialize(value, timestamp.toDate());

        // Aggregate can only be Datastream
        handleDatastreamFailedPublish(mapping, topic, payload, qos);
    }

    private void handlePropertiesFailedPublish(String topic, byte[] payload, int qos)
            throws AstarteTransportException {
        // We store everything since we are not connected to a proper transport
        m_failedMessageStorage.insertStored(topic, payload, qos);
    }

    private void handleDatastreamFailedPublish(
            AstarteInterfaceDatastreamMapping mapping,
            String topic,
            byte[] payload,
            int qos)
            throws AstarteTransportException {
        int expiry = mapping.getExpiry();

        switch (mapping.getRetention()) {
            case DISCARD:
                // Message won't be retried, so we throw to notify the user
                // FIXME we expect to discard messages and we will just drop them eventually a log is enough
                // FIXME replace with log
                throw new AstarteTransportException("Cannot send value");

            case VOLATILE:
            {
                if (expiry > 0) {
                    m_failedMessageStorage.insertVolatile(topic, payload, qos, expiry);
                } else {
                    m_failedMessageStorage.insertVolatile(topic, payload, qos);
                }
                break;
            }

            case STORED:
            {
                if (expiry > 0) {
                    m_failedMessageStorage.insertStored(topic, payload, qos, expiry);
                } else {
                    m_failedMessageStorage.insertStored(topic, payload, qos);
                }
                break;
            }
        }
    }

    // FIXME merge this function with the one in MqttV1Transport
    private int qosFromReliability(AstarteInterfaceDatastreamMapping mapping) {
        switch (mapping.getReliability()) {
            case UNIQUE:
                return 2;
            case GUARANTEED:
                return 1;
            case UNRELIABLE:
                return 0;
        }

        return 0;
    }

}
