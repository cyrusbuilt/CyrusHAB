package net.cyrusbuilt.cyrushab.daemon;

import net.cyrusbuilt.cyrushab.core.mqtt.HABMqttException;
import net.cyrusbuilt.cyrushab.core.mqtt.MqttManager;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemControlPacket;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatus;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatusPacket;
import net.cyrusbuilt.cyrushab.core.things.BasicThingUtils;
import net.cyrusbuilt.cyrushab.core.things.MinimalThingInfo;
import net.cyrusbuilt.cyrushab.core.things.thermostat.Thermostat;
import net.cyrusbuilt.cyrushab.core.things.thermostat.ThermostatControlPacket;
import net.cyrusbuilt.cyrushab.core.things.thermostat.ThermostatStatusPacket;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class HABDaemon implements Daemon, MqttManager.MqttEventListener {
    private static final Logger logger = LoggerFactory.getLogger(HABDaemon.class);
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    private static HABDaemon _instance;
    private Thread _mainThread = null;
    private Thread _inboundEventProcessor = null;
    private Thread _outboundEventProcessor = null;
    private Queue<MqttManager.MqttEvent> _inboundEventQueue;
    private Queue<MqttManager.MqttEvent> _outboundEventQueue;
    private int _reconnects = 0;
    private volatile SystemStatus _status = SystemStatus.DISABLED;
    private volatile int _eventID = 0;

    public HABDaemon() {
        super();
        _instance = this;
        _inboundEventQueue = new ArrayDeque<>();
        _outboundEventQueue = new ArrayDeque<>();
    }

    public static HABDaemon getInstance() {
        return _instance;
    }

    private synchronized void setSystemStatus(@NotNull final SystemStatus status) {
        logger.info("Setting system status: " + status.name());
        _status = status;
    }

    private synchronized SystemStatus getStatus() {
        return _status;
    }

    public synchronized boolean isRunning() {
        return _status != SystemStatus.SHUTDOWN;
    }

    public synchronized boolean isDisabled() {
        return _status == SystemStatus.DISABLED;
    }

    public synchronized int getNextEventID() {
        return _eventID++;
    }

    private void initMqttManager() {
        logger.info("Initializing MQTT manager...");
        String brokerUrl = "tcp://" + Configuration.mqttBroker() + ":" + Configuration.port();

        MqttManager mgr = MqttManager.getInstance();
        mgr.setBrokerUrl(brokerUrl);
        mgr.setCleanSession(true);
        mgr.setClientID(Configuration.clientID());
        mgr.setUsername(Configuration.username());
        mgr.setPassword(Configuration.password());
        mgr.addListener(this);
        try {
            mgr.initialize();
            logger.info("Subscribing to topic: " + Configuration.systemControlTopic());
            mgr.subscribe(Configuration.systemControlTopic());
            logger.info("Subscribing to topic: " + Configuration.thingStatusTopicBase());
            mgr.subscribe(Configuration.thingStatusTopicBase());
            logger.info("Subscribing to topic: " + Configuration.thingControlTopicBase());
            mgr.subscribe(Configuration.thingControlTopicBase());
            logger.info("Subscribing to topic: " + Configuration.applicationTopic());
            mgr.subscribe(Configuration.applicationTopic());
        }
        catch (HABMqttException ex) {
            logger.error("MQTT Manager initialization failure: " + ex.getMessage());
            mgr.shutdown();
        }
    }

    private void loop() {
        // TODO anything to do here?
        try {
            //logger.info("Zzzzz...");
            Thread.sleep(500);
        }
        catch (InterruptedException ignored) {

        }
    }

    private synchronized void processInboundEventQueue() {
        // NOTE we need to continue processing incoming messages even when the system is disabled.
        // Just in case we get a shutdown or enable command.
        MqttManager.MqttEvent event = dequeueInboundEvent();
        if (event != null) {
            logger.info("Processing inbound event ID " + event.messageId() + " from topic: " + event.topic());
            processMqttMessage(event);
        }
        try {
            Thread.sleep(50);
        }
        catch (InterruptedException ignored) {
        }
    }

    private synchronized void processOutboundEventQueue() {
        if (!isDisabled()) {
            MqttManager.MqttEvent event = dequeueOutboundEvent();
            if (event != null) {
                logger.info("Processing outbound event ID " + event.messageId() + " for topic " + event.topic());
                processMqttMessage(event);
            }
        }

        try {
            Thread.sleep(50);
        }
        catch (InterruptedException ignored) {
        }
    }

    private void publishThingControlMessage(String topic, String message) {
        try {
            logger.info("Publishing thing control message. Topic: " + topic + ", Message: " + message);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (HABMqttException e) {
            logger.error("Failed to publish thing control message: " + e.getMessage());
        }
    }

    private void processSystemCommand(SystemControlPacket packet) {
        logger.info("Received system command : " + packet.getCommand().name() +
                " from client ID: " + packet.getClientID() +
                " at " + packet.getTimestamp().toString());
        switch (packet.getCommand()) {
            case ENABLE:
                if (isDisabled()) {
                    logger.info("Re-enabling system...");
                    setSystemStatus(SystemStatus.NORMAL);
                    publishSystemStatus();
                }
                else {
                    logger.info("System already enabled. Ignoring command.");
                }
                break;
            case DISABLE:
                if (isDisabled()) {
                    logger.info("System already disabled. Ignoring command.");
                }
                else {
                    logger.warn("Disabling system...");
                    setSystemStatus(SystemStatus.DISABLED);
                    publishSystemStatus();
                }
                break;
            case RESTART:
                logger.warn("Restarting the system...");
                try {
                    stop();
                    Thread.sleep(500);
                    start();
                }
                catch (Exception e) {
                    logger.error("Failed to restart: " + e.getMessage());
                }

            case SHUTDOWN:
                destroy();
                break;

            case UNKNOWN:
            default:
                logger.warn("Ignoring unknown system command.");
        }
    }

    private void processThermostatControlPacket(@NotNull ThermostatControlPacket packet) {
        logger.info("Received thermostat control message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        Thermostat thermostat = (Thermostat)Configuration.getThingFromRegistry(packet.getID());
        if (thermostat == null) {
            logger.warn("Thermostat not found in thing registry. DEV_ID:  " + packet.getID());
            return;
        }

        logger.info("Thermostat mode: " + packet.getMode().name());
        logger.info("Thermostat enable: " + packet.isEnabled());
        logger.info("Thermostat readonly: " + packet.isReadonly());
        logger.info("Thermostat Thing ID: " + packet.getID());
        logger.info("Thermostat name: " + thermostat.name());
        packet.setClientID(Configuration.clientID());
        packet.setTimestamp(Util.getCurrentTimestamp());

        String message = packet.toJsonString();
        String topic = Configuration.thingControlTopicBase() + "/" + thermostat.id();
        MqttManager.MqttEvent event = new MqttManager.MqttEvent(topic, message, getNextEventID());
        enqueueOutboundEvent(event);
    }

    private void processThermostatStatusPacket(@NotNull ThermostatStatusPacket packet) {
        Thermostat thermostat = (Thermostat) Configuration.getThingFromRegistry(packet.getID());
        if (thermostat == null) {
            logger.warn("Thermostat not found in thing registry. DEV_ID:  " + packet.getID());
            return;
        }

        try {
            thermostat.mapFromStatusPacket(packet);
            packet.setClientID(Configuration.clientID());
            packet.setTimestamp(Util.getCurrentTimestamp());
            String message = packet.toJsonString();
            String topic = Configuration.applicationTopic();

            logger.info("Publishing thermostat status to topic " + topic);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (Exception e) {
            logger.error("Error publishing status: " + e.getMessage());
        }
    }

    private void processMqttMessage(@NotNull MqttManager.MqttEvent event) {
        String topic = event.topic();
        String message = event.message();
        boolean isControl = StringUtils.equalsIgnoreCase(topic, Configuration.thingControlTopicBase());
        try {
            MinimalThingInfo info = BasicThingUtils.parseMinimalThingInfoFromJson(message);
            if (info != null) {
                switch (info.getThingType()) {
                    case UNKNOWN:
                        logger.error("Unknown thing type: " + info.getThingType().getValue() +
                                        ", DEV_ID: " + info.getID() + ", CLIENT_ID: " + info.getClientID());
                        break;
                    case SYSTEM:
                        isControl = StringUtils.equalsIgnoreCase(topic, Configuration.systemControlTopic());
                        if (isControl) {
                            // We received a system control message.
                            SystemControlPacket sysCtrl = SystemControlPacket.fromJsonString(message);
                            if (sysCtrl != null) {
                                processSystemCommand(sysCtrl);
                            }
                        }
                        break;
                    case DIMMABLE_LIGHT:
                        // TODO process dimmable light packet.
                        break;
                    case MOTION_SENSOR:
                        // TODO process motion sensor.
                        break;
                    case THERMOSTAT:
                        if (isControl) {
                            // We received a thermostat control message on the main control topic.
                            ThermostatControlPacket thermoCtrl = ThermostatControlPacket.fromJsonString(message);
                            if (thermoCtrl != null) {
                                processThermostatControlPacket(thermoCtrl);
                            }
                        }
                        else {
                            // A processed control message is ready to publish.
                            if (topic.startsWith(Configuration.thingControlTopicBase())) {
                                publishThingControlMessage(topic, message);
                            }

                            // We received a status message from a Thing.
                            if (topic.equalsIgnoreCase(Configuration.thingStatusTopicBase())) {
                                ThermostatStatusPacket tstatus = ThermostatStatusPacket.fromJsonString(message);
                                if (tstatus != null) {
                                    processThermostatStatusPacket(tstatus);
                                }
                            }
                        }
                        break;
                }
            }
        }
        catch (Exception e) {
            logger.error("Event processing error: " + e.toString());
            e.printStackTrace();
        }
    }

    private synchronized void enqueueInboundEvent(MqttManager.MqttEvent event) {
        logger.info("Enqueueing inbound event ID " + event.messageId());
        _inboundEventQueue.add(event);
    }

    private synchronized void enqueueOutboundEvent(MqttManager.MqttEvent event) {
        logger.info("Enqueueing outbound event ID " + event.messageId());
        _outboundEventQueue.add(event);
    }

    @Nullable
    private synchronized MqttManager.MqttEvent dequeueInboundEvent() {
        if (!_inboundEventQueue.isEmpty()) {
            return _inboundEventQueue.remove();
        }
        return null;
    }

    @Nullable
    private synchronized MqttManager.MqttEvent dequeueOutboundEvent() {
        if (!_outboundEventQueue.isEmpty()) {
            return _outboundEventQueue.remove();
        }
        return null;
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        _reconnects++;
        logger.error("MQTT connection lost! Cause: " + cause.toString());
        cause.printStackTrace();
        logger.error("Attempting reconnect (attempt " + _reconnects + " of " + MAX_RECONNECT_ATTEMPTS + ") ...");
        try {
            if (_reconnects < MAX_RECONNECT_ATTEMPTS) {
                setSystemStatus(SystemStatus.RECONNECTING);
                MqttManager.getInstance().connect();
                publishSystemStatus();
                setSystemStatus(SystemStatus.NORMAL);
            }
            else {
                logger.error("Failed to reconnect to MQTT host.");
                _reconnects = 0;
            }
        }
        catch (HABMqttException ex) {
            // TODO Start a timer and wait a while before retrying again.
            setSystemStatus(SystemStatus.DISCONNECTED);
            logger.error("Failed to re-establish connection to MQTT broker: " + Configuration.mqttBroker() +
                    ", Reason: " + ex.getMessage());
        }
    }

    @Override
    public void onMessageReceived(MqttManager.MqttEvent event) {
        logger.info("MQTT message received.\nMessage ID: " + event.messageId() +
                "\nTopic: " + event.topic() +
                "\nMessage: " + event.message());

        // Queue actionable events for processing on a separate thread.
        enqueueInboundEvent(event);
    }

    @Override
    public void onMessageDelivered(MqttManager.MqttEvent event) {
        logger.info("Message delivered to topic: " + event.topic() +
                "\nMessage ID: " + event.messageId() +
                "\nMessage: " + event.message());
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        final Object[] args = context.getArguments();
        logger.info("HABDaemon initialized.");
        logger.info("Initializing configuration...");
        try {
            Configuration.initialize();
        }
        catch (Exception ex) {
            logger.error("Cannot read configuration: " + ex.getMessage());
            System.exit(1);
        }

        _mainThread = new Thread() {
            private final Object syncLock = new Object();
            private volatile long lastTick = 0;

            @Override
            public synchronized void start() {
                HABDaemon.this.setSystemStatus(SystemStatus.NORMAL);
                super.start();
            }

            @Override
            public void run() {
                synchronized (syncLock) {
                    while (isRunning()) {
                        long now = System.currentTimeMillis();
                        if ((now - lastTick) >= 1000) {
                            lastTick = now;
                        }

                        HABDaemon.this.loop();
                    }
                }
            }
        };

        _inboundEventProcessor = new Thread() {
            private final Object syncLock = new Object();

            @Override
            public void run() {
                synchronized (syncLock) {
                    while (isRunning()) {
                        HABDaemon.this.processInboundEventQueue();
                    }
                }
            }
        };
        _inboundEventProcessor.setName("CyrusHAB_InboundEventProcessor");


        _outboundEventProcessor = new Thread() {
            private final Object syncLock = new Object();

            @Override
            public void run() {
                synchronized (syncLock) {
                    while (isRunning()) {
                        HABDaemon.this.processOutboundEventQueue();
                    }
                }
            }
        };
        _outboundEventProcessor.setName("CyrusHAB_OutboundEventProcessor");
    }

    @Override
    public void start() throws Exception {
        logger.info("Loading configuration...");
        try {
            Configuration.reloadConfig();
            Configuration.reloadThingRegistry();
        }
        catch (Exception e) {
            logger.error("Cannot read configuration: " + e.getMessage());
            System.exit(1);
        }

        initMqttManager();

        logger.info("HABDaemon starting...");
        _mainThread.start();

        logger.info("Starting inbound event queue processor...");
        _inboundEventProcessor.start();

        logger.info("Starting outbound event queue processor...");
        _outboundEventProcessor.start();

        logger.info("Startup complete.");
    }

    @Override
    public void stop() throws Exception {
        // Let everyone know we are shutting down first.
        logger.info("Stop requested.");
        setSystemStatus(SystemStatus.SHUTDOWN);
        publishSystemStatus();

        logger.info("Stopping MQTT manager...");
        MqttManager.getInstance().shutdown();

        try {
            logger.info("Stopping inbound event queue processor...");
            _inboundEventProcessor.join(1000);
            logger.info("Stopping outbound event queue processor...");
            _outboundEventProcessor.join(1000);
            logger.info("Stopping HABDaemon...");
            _mainThread.join(1000);
            logger.info("HABDaemon stopped.");
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
            //throw ex;
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
        _inboundEventProcessor = null;
        _inboundEventQueue.clear();
        _inboundEventQueue = null;
        _outboundEventProcessor = null;
        _outboundEventQueue.clear();
        _outboundEventQueue = null;
        logger.info("HABDaemon destroyed.");
    }

    private void publishSystemStatus() {
        // We publish system status no-matter-what, so we don't go through the outbound event queue.
        // As long as the MQTT manager is still alive, we send the status.
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
