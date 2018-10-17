package net.cyrusbuilt.cyrushab.daemon;

import net.cyrusbuilt.cyrushab.core.mqtt.HABMqttException;
import net.cyrusbuilt.cyrushab.core.mqtt.MqttManager;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatus;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatusPacket;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class HABDaemon implements Daemon, MqttManager.MqttEventListener {
    private static final Logger logger = LoggerFactory.getLogger(HABDaemon.class);

    private static HABDaemon _instance;
    private volatile boolean _stopped = false;
    private Thread _mainThread = null;
    private Thread _queueProcessor = null;
    private Queue<MqttManager.MqttEvent> _eventQueue;
    private volatile SystemStatus _status = SystemStatus.DISABLED;

    public HABDaemon() {
        super();
        _instance = this;
        _eventQueue = new ArrayDeque<>();
    }

    public static HABDaemon getInstance() {
        return _instance;
    }

    private synchronized void setSystemStatus(final SystemStatus status) {
        _status = status;
    }

    private synchronized SystemStatus getStatus() {
        return _status;
    }

    private synchronized void setStopped(final boolean isStopped) {
        _stopped = isStopped;
    }

    public synchronized boolean isRunning() {
        return !_stopped;
    }

    private void initMqttManager() {
        logger.info("Initializing MQTT manager...");
        String brokerUrl = "tcp://" + Configuration.mqttBroker() + ":" + Configuration.port();
        MqttManager.getInstance().setBrokerUrl(brokerUrl);
        MqttManager.getInstance().setCleanSession(true);
        MqttManager.getInstance().setClientID(Configuration.clientID());
        MqttManager.getInstance().setUsername(Configuration.username());
        MqttManager.getInstance().setPassword(Configuration.password());
        try {
            MqttManager.getInstance().initialize();
        }
        catch (HABMqttException ex) {
            logger.error("MQTT Manager initialization failure: " + ex.getMessage());
        }
    }

    private void loop() {

    }

    private void processEventQueue() {
        while (!_stopped) {
            MqttManager.MqttEvent event = dequeueEvent();
            if (event != null) {

            }
        }
    }

    private void processMqttMessage(String message) {
        JSONParser parser = new JSONParser();
        try {
            // First, what kind of message is this
            Object obj = parser.parse(message);
            JSONObject jsonObject = (JSONObject)obj;


//            int thingID = (int)(long)jsonObject.get("id");
//            Thing theThing = Configuration.getThingFromRegistry(thingID);
//            if (theThing != null) {
//                // Ok, we found the thing in question. Now what are we doing with it?
//
//            }
        }
        catch (Exception ex) {

        }
    }

    private synchronized void enqueueEvent(MqttManager.MqttEvent event) {
        _eventQueue.add(event);
    }

    @Nullable
    private synchronized MqttManager.MqttEvent dequeueEvent() {
        if (!_eventQueue.isEmpty()) {
            return _eventQueue.remove();
        }
        return null;
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        logger.error("MQTT connection lost! Cause: " + cause.getMessage());
        logger.error("Attempting reconnect...");
        try {
            setSystemStatus(SystemStatus.RECONNECTING);
            MqttManager.getInstance().connect();
            publishSystemStatus();
            setSystemStatus(SystemStatus.NORMAL);
        }
        catch (HABMqttException ex) {
            setSystemStatus(SystemStatus.DISCONNECTED);
            logger.error("Failed to re-establish connection to MQTT broker: " + Configuration.mqttBroker() +
                    "Reason: " + ex.getMessage());
        }
    }

    @Override
    public void onMessageReceived(MqttManager.MqttEvent event) {
        logger.info("MQTT message received.\nMessage ID: " + event.messageId() +
                "\nTopic: " + event.topic() +
                "\nMessage: " + event.message());

        // Queue actionable events for processing on a separate thread.
        enqueueEvent(event);
    }

    @Override
    public void onMessageDelivered(MqttManager.MqttEvent event) {
        logger.info("Message delivered to topic: " + event.topic() +
                "Message ID: " + event.messageId() +
                "Message: " + event.message());
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        final Object[] args = context.getArguments();
        logger.info("HABDaemon initialized.");
        logger.info("Initializing configuration...");
        try {
            Configuration.initialize();
            Configuration.reloadConfig();
            Configuration.reloadThingRegistry();
        }
        catch (Exception ex) {
            logger.error("Cannot read configuration: " + ex.getMessage());
            System.exit(1);
        }

        initMqttManager();

        setSystemStatus(SystemStatus.NORMAL);
        _mainThread = new Thread() {
            private final Object syncLock = new Object();
            private volatile long lastTick = 0;

            @Override
            public synchronized void start() {
                HABDaemon.this.setStopped(false);
                super.start();
            }

            @Override
            public void run() {
                synchronized (syncLock) {
                    while (!_stopped) {
                        long now = System.currentTimeMillis();
                        if ((now - lastTick) >= 1000) {
                            lastTick = now;
                        }

                        loop();
                    }
                }
            }
        };

        _queueProcessor = new Thread(this::processEventQueue);
    }

    @Override
    public void start() throws Exception {
        logger.info("HABDaemon starting...");
        _mainThread.start();

        logger.info("Starting event queue processor...");
        _queueProcessor.start();
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping MQTT manager...");
        MqttManager.getInstance().shutdown();

        setStopped(true);
        setSystemStatus(SystemStatus.DISABLED);
        try {
            logger.info("Stopping event queue processor...");
            _queueProcessor.join(1000);
            logger.info("Stopping HABDaemon...");
            _mainThread.join(1000);
        }
        catch (InterruptedException ex) {
            logger.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void destroy() {
        if (isRunning()) {
            try {
                stop();
            }
            catch (Exception ignored) {
            }
        }

        _mainThread = null;
        _queueProcessor = null;
        _eventQueue.clear();
        _eventQueue = null;
        logger.info("HABDaemon destroyed.");
    }

    private void publishSystemStatus() {
        String topic = Configuration.systemStatusTopic() + "/" + Configuration.clientID();
        SystemStatusPacket packet = new SystemStatusPacket.Builder()
                .setClientID(Configuration.clientID())
                .setStatus(getStatus())
                .setTimestamp(Util.getCurrentTimestamp())
                .build();

        try {
            logger.info("Publishing system status: " + getStatus().name() + " to " + topic);
            MqttManager.getInstance().publish(topic, packet.toJsonString());
        }
        catch (HABMqttException e) {
            logger.error("Failed to publish system status: " + e.getMessage());
        }
    }
}
