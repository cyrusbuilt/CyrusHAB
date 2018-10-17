package net.cyrusbuilt.cyrushab.core.mqtt;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The MQTT communication manager. This class is used to connect (and maintain a connection) to an MQTT broker, and
 * provide facilities for publishing and/or subscribing to topics. This class is event-driven. Events are fired on a
 * separate thread when messages are received/delivered and if a connection loss occurs.
 */
public final class MqttManager {
    /**
     * Used to transfer info about an MQTT event.
     */
    public class MqttEvent {
        private String _topic = StringUtils.EMPTY;
        private String _message = StringUtils.EMPTY;
        private int _id = -1;

        /**
         * Creates a new instance of MqttEvent with the topic, message, and message ID related to the event.
         * @param topic The topic the message was delivered to or received from.
         * @param message The message delivered/received.
         * @param id The message ID.
         */
        public MqttEvent(String topic, String message, int id) {
            _topic = topic;
            _message = message;
            _id = id;
        }

        /**
         * The topic the message was received from or delivered to.
         * @return The message topic.
         */
        public String topic() {
            return _topic;
        }

        /**
         * The message that was delivered or received.
         * @return The message.
         */
        public String message() {
            return _message;
        }

        /**
         * The ID of the message.
         * @return The message ID.
         */
        public int messageId() {
            return _id;
        }
    }

    /**
     * An MQTT event listener.
     */
    public interface MqttEventListener {
        /**
         * Fired with a connection loss event occurs.
         * @param cause The throwable/exception that may have caused the connection loss.
         */
        void onConnectionLost(Throwable cause);

        /**
         * Fired when an MQTT message is received on a subscribed topic.
         * @param event The event info.
         */
        void onMessageReceived(MqttEvent event);

        /**
         * Fired when an MQTT message is successfully published to a topic.
         * @param event The event info.
         */
        void onMessageDelivered(MqttEvent event);
    }

    private class HandlerCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            notifyConnectionLost(cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            MqttEvent evt = new MqttEvent(topic, new String(message.getPayload()), message.getId());
            notifyMessageReceived(evt);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                String topic = token.getTopics()[0];
                String message = new String(token.getMessage().getPayload());
                int id = token.getMessage().getId();
                MqttEvent evt = new MqttEvent(topic, message, id);
                notifyMessageDelivered(evt);
            }
            catch (MqttException ex) {
                // TODO need better handling here.
                ex.printStackTrace();
            }
        }
    }

    private static MqttManager _instance = null;
    private String _brokerUrl;
    private boolean _clean;
    private String _username;
    private String _password;
    private String _clientID;
    private MqttConnectOptions _connOpts;
    private MqttClient _client;
    private HandlerCallback _handlerCallback;
    private List<MqttEventListener> _listeners;
    private ExecutorService _service;

    /**
     * Private default constructor.
     */
    private MqttManager() {
        _handlerCallback = new HandlerCallback();
        _listeners = new ArrayList<>();
        _service = Executors.newSingleThreadExecutor();
    }

    /**
     * Factory method for getting a singleton instance reference.
     * @return The MQTT manager instance.
     */
    public static MqttManager getInstance() {
        if (_instance == null) {
            _instance = new MqttManager();
        }

        return _instance;
    }

    /**
     * Adds an event listener if it is not already registered.
     * @param listener The event listener to add.
     */
    public void addListener(@NotNull MqttEventListener listener) {
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    /**
     * Removes all registered event listeners.
     */
    public void removeAllListeners() {
        _listeners.clear();
    }

    /**
     * Notifies registered event listeners that a message was received on a subscribed topic.
     * @param event The event info.
     */
    public void notifyMessageReceived(MqttEvent event) {
        for (MqttEventListener listener : _listeners) {
            _service.execute(() -> listener.onMessageReceived(event));
        }
    }

    /**
     * Notifies registered event listeners that a message was delivered to a topic.
     * @param event The event info.
     */
    public void notifyMessageDelivered(MqttEvent event) {
        for (MqttEventListener listener : _listeners) {
            _service.execute(() -> listener.onMessageDelivered(event));
        }
    }

    /**
     * Notifies registered listeners that the connection to the MQTT broker was lost.
     * @param cause The cause of the connection loss.
     */
    public void notifyConnectionLost(Throwable cause) {
        for (MqttEventListener listener : _listeners) {
            _service.execute(() -> listener.onConnectionLost(cause));
        }
    }

    /**
     * Sets the MQTT broker URL.
     * @param brokerUrl The URL to the MQTT broker (server).
     */
    public void setBrokerUrl(String brokerUrl) {
        _brokerUrl = brokerUrl;
    }

    /**
     * Sets a flag indicating that we should use a clean MQTT session.
     * @param cleanSession Set true to use a clean session.
     */
    public void setCleanSession(boolean cleanSession) {
        _clean = cleanSession;
    }

    /**
     * Sets the MQTT username if the connection requires authentication.
     * @param username The name of the user to authenticate.
     */
    public void setUsername(String username) {
        _username = username;
    }

    /**
     * Sets the MQTT password if the connection requires authentication.
     * @param password The password to authenticate with.
     */
    public void setPassword(String password) {
        _password = password;
    }

    /**
     * Sets the MQTT client ID.
     * @param clientID The client ID.
     */
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Initializes the MQTT manager. This does not make a connection to the broker. This will also configure a 60 second
     * "keep alive" interval which will guarantee that messages are delivered every minute and if there are no messages
     * to deliver, then a "ping" message will be sent to let the broker know we still need the connection.
     * @throws HABMqttException if MQTT initialization failed.
     */
    public void initialize() throws HABMqttException {
        // TODO make data storage dir configurable so we can put queued messages somewhere else (ie subdir of daemon).
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
        try {
            _connOpts = new MqttConnectOptions();
            _connOpts.setCleanSession(_clean);
            _connOpts.setKeepAliveInterval(60);
            if (StringUtils.isNotBlank(_password)) {
                _connOpts.setPassword(_password.toCharArray());
            }

            if (StringUtils.isNotBlank(_username)) {
                _connOpts.setUserName(_username);
            }

            _client = new MqttClient(_brokerUrl, _clientID, dataStore);
            _client.setCallback(_handlerCallback);
        }
        catch (MqttException ex) {
            throw new HABMqttException(ex);
        }
    }

    /**
     * Gets whether or not there is a connection to the MQTT broker established.
     * @return true if a connection is established; Otherwise, false.
     */
    public boolean isConnected() {
        return _client != null && _client.isConnected();
    }

    /**
     * Manually makes a connection the MQTT broker.
     * @throws HABMqttException if the connection fails.
     */
    public void connect() throws HABMqttException {
        if (!isConnected()) {
            try {
                _client.connect(_connOpts);
            }
            catch (MqttException ex) {
                throw new HABMqttException(ex);
            }
        }
    }

    /**
     * Publishes the specified message to the specified topic. If not already connected to the broker, then a connection
     * will be established first.
     * @param topicName The name of the topic to publish to. If the topic does not already exist, it will be created.
     * @param message The message to publish.
     * @throws HABMqttException if unable to connect to the broker or if publishing the message fails.
     */
    public void publish(@NotNull String topicName, @NotNull String message) throws HABMqttException {
        try {
            connect();
            final MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(2);
            _client.publish(topicName, msg);
        }
        catch (MqttException ex) {
            throw new HABMqttException(ex);
        }
    }

    /**
     * Subscribes to the specified topic. This allows us to receive messages that are published to it. If not already
     * connected to the broker, then a connection will be established first.
     * @param topicName The topic to subscribe to.
     * @throws HABMqttException if unable to connect to the broker or if subscription fails.
     */
    public void subscribe(@NotNull String topicName) throws HABMqttException {
        try {
            connect();
            _client.subscribe(topicName, 2);
        }
        catch (MqttException ex) {
            throw new HABMqttException(ex);
        }
    }

    /**
     * Disconnects from the MQTT broker.
     */
    public void disconnect() {
        if (isConnected()) {
            _client.setCallback(null);
            try {
                _client.disconnect();
            }
            catch (MqttException ignored) {
            }
        }
    }

    /**
     * Shuts down the MQTT manager. This automatically calls removeAllListeners() and disconnect(), so calling them
     * first is redundant.
     */
    public void shutdown() {
        removeAllListeners();
        disconnect();

        if (_service != null) {
            _service.shutdown();
            try {
                if (!_service.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    _service.shutdownNow();
                }
            }
            catch (InterruptedException e) {
                _service.shutdownNow();
            }
        }
    }
}
