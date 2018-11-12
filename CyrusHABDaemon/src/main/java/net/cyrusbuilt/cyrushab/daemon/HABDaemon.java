package net.cyrusbuilt.cyrushab.daemon;

import net.cyrusbuilt.cyrushab.core.application.HeartBeatPacket;
import net.cyrusbuilt.cyrushab.core.application.ThingInventoryPacket;
import net.cyrusbuilt.cyrushab.core.mqtt.HABMqttException;
import net.cyrusbuilt.cyrushab.core.mqtt.MqttManager;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemControlPacket;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatus;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatusPacket;
import net.cyrusbuilt.cyrushab.core.things.BasicThingUtils;
import net.cyrusbuilt.cyrushab.core.things.MinimalThingInfo;
import net.cyrusbuilt.cyrushab.core.things.Packet;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.dimmablelight.DimmableLight;
import net.cyrusbuilt.cyrushab.core.things.dimmablelight.DimmableLightControlPacket;
import net.cyrusbuilt.cyrushab.core.things.dimmablelight.DimmableLightStatusPacket;
import net.cyrusbuilt.cyrushab.core.things.door.Door;
import net.cyrusbuilt.cyrushab.core.things.door.DoorControlPacket;
import net.cyrusbuilt.cyrushab.core.things.door.DoorStatusPacket;
import net.cyrusbuilt.cyrushab.core.things.motionsensor.MotionSensor;
import net.cyrusbuilt.cyrushab.core.things.motionsensor.MotionSensorStatusPacket;
import net.cyrusbuilt.cyrushab.core.things.switches.Switch;
import net.cyrusbuilt.cyrushab.core.things.switches.SwitchControlPacket;
import net.cyrusbuilt.cyrushab.core.things.switches.SwitchStatusPacket;
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

import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The main class of this daemon. Responsible for processing control and status messages for client devices as well as
 * system control messages sent via MQTT.
 */
public class HABDaemon implements Daemon, MqttManager.MqttEventListener {
    private static final Logger logger = LoggerFactory.getLogger(HABDaemon.class);
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    private static HABDaemon _instance;
    private DaemonContext _thisContext;
    private Thread _mainThread = null;
    private Thread _inboundEventProcessor = null;
    private Thread _outboundEventProcessor = null;
    private Queue<MqttManager.MqttEvent> _inboundEventQueue;
    private Queue<MqttManager.MqttEvent> _outboundEventQueue;
    private BlockingDeque<Runnable> _systemQueue;
    private int _reconnects = 0;
    private TimerTask _reconnectTask;
    private Timer _reconnectTimer;
    private volatile SystemStatus _status = SystemStatus.DISABLED;
    private volatile int _eventID = 0;

    /**
     * Constructs a new instance of {@link HABDaemon}.
     */
    public HABDaemon() {
        super();
        _instance = this;
        _inboundEventQueue = new ArrayDeque<>();
        _outboundEventQueue = new ArrayDeque<>();
        _systemQueue = new LinkedBlockingDeque<>();
        _reconnectTimer = new Timer("MQTTReconnectTimer");
    }

    /**
     *
     * @return
     */
    public static HABDaemon getInstance() {
        return _instance;
    }

    /**
     * Sets the system status.
     * @param status The current system status.
     */
    private synchronized void setSystemStatus(@NotNull final SystemStatus status) {
        if (_status != status) {
            logger.info("Setting system status: " + status.name());
            _status = status;
        }
    }

    /**
     * Gets the system's current status.
     * @return The current system status.
     */
    private synchronized SystemStatus getStatus() {
        return _status;
    }

    /**
     * Gets whether or not the system is in a running state.
     * @return true if the system is running; Otherwise, false.
     */
    public synchronized boolean isRunning() {
        return _status != SystemStatus.SHUTDOWN;
    }

    /**
     * Gets whether the system is disabled (not processing device messages).
     * @return true if disabled; Otherwise, false.
     */
    public synchronized boolean isDisabled() {
        return _status == SystemStatus.DISABLED;
    }

    public synchronized int getNextEventID() {
        return _eventID++;
    }

    /**
     * Initializes the MQTT manager and begins listening for MQTT events.
     */
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
        }
        catch (HABMqttException ex) {
            logger.error("MQTT Manager initialization failure: " + ex.getMessage());
            mgr.shutdown();
        }
    }

    /**
     * Main system loop. Processes tasks in the system queue.
     */
    private void loop() {
        try {
            _systemQueue.take().run();
            Thread.sleep(50);
        }
        catch (InterruptedException ignored) {

        }
    }

    /**
     * Processes any available events in the inbound queue.
     */
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

    /**
     * Processes any available events in the outbound queue if the system is enabled.
     */
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

    private void publishThingInventory(String clientID) {
        ThingInventoryPacket packet = new ThingInventoryPacket.Builder()
                .setClientID(clientID)
                .setHostID(Configuration.clientID())
                .setTimestamp(Util.getCurrentTimestamp())
                .setThingInventory(Configuration.getThingRegistry())
                .build();

        String message = packet.toJsonString();
        String topic = Configuration.applicationTopic();

        try {
            logger.info("Publishing thing inventory message to topic: " + topic);
            logger.debug("Inventory: " + message);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (HABMqttException e) {
            logger.error("Failed to publish thing inventory message: " + e.getMessage());
        }
    }

    private void publishHeartbeat(String clientID) {
        HeartBeatPacket packet = new HeartBeatPacket.Builder()
                .setClientID(clientID)
                .setHostID(Configuration.clientID())
                .setTimestamp(Util.getCurrentTimestamp())
                .setStatus(getStatus())
                .build();

        String message = packet.toJsonString();
        String topic = Configuration.applicationTopic();

        try {
            logger.info("Publishing system heartbeat message to topic: " + topic);
            logger.debug("Heartbeat: " + message);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (HABMqttException e) {
            logger.error("Failed to publish heartbeat message: " + e.getMessage());
        }
    }

    private void publishAllDeviceStatuses() {
        String topic = Configuration.applicationTopic();
        for (Thing thing : Configuration.getThingRegistry()) {
            Packet packet = null;
            switch (thing.type()) {
                case SWITCH:
                    Switch sw = (Switch)thing;
                    packet = new SwitchStatusPacket.Builder()
                            .setReadonly(sw.isReadonly())
                            .setEnabled(sw.isEnabled())
                            .setID(sw.id())
                            .setClientID(sw.clientID())
                            .setName(sw.name())
                            .setState(sw.state())
                            .setTimestamp(Util.getCurrentTimestamp())
                            .build();
                    break;
                case THERMOSTAT:
                    Thermostat t = (Thermostat)thing;
                    packet = new ThermostatStatusPacket.Builder()
                            .setClientID(t.clientID())
                            .setEnabled(t.isEnabled())
                            .setReadonly(t.isReadonly())
                            .setID(t.id())
                            .setMode(t.mode())
                            .setState(t.state())
                            .setTimestamp(Util.getCurrentTimestamp())
                            .build();
                    break;
                case MOTION_SENSOR:
                    MotionSensor ms = (MotionSensor)thing;
                    packet = new MotionSensorStatusPacket.Builder()
                            .setClientID(ms.clientID())
                            .setThingID(ms.id())
                            .setEnabled(ms.isEnabled())
                            .setState(ms.getState())
                            .setTimestamp(Util.getCurrentTimestamp())
                            .build();
                    break;
                case DIMMABLE_LIGHT:
                    DimmableLight dl = (DimmableLight)thing;
                    packet = new DimmableLightStatusPacket.Builder()
                            .setClientID(dl.clientID())
                            .setThingID(dl.id())
                            .setEnabled(dl.isEnabled())
                            .setReadonly(dl.isReadonly())
                            .setLevel(dl.level())
                            .setMinLevel(dl.minLevel())
                            .setMaxLevel(dl.maxLevel())
                            .setTimestamp(Util.getCurrentTimestamp())
                            .build();
                    break;
                case DOOR:
                    Door d = (Door)thing;
                    packet = new DoorStatusPacket.Builder()
                            .setThingID(d.id())
                            .setClientID(d.clientID())
                            .setEnabled(d.isEnabled())
                            .setReadonly(d.isReadonly())
                            .setState(d.getState())
                            .setLocked(d.isLocked())
                            .setTimestamp(Util.getCurrentTimestamp())
                            .build();
                    break;
                case UNKNOWN:
                default:
                    // TODO handle anything else.
                    break;
            }

            if (packet != null) {
                String message = packet.toJsonString();
                try {
                    logger.info("Publishing batch Thing status message to topic: " + topic + ", message: " + message);
                    MqttManager.getInstance().publish(topic, message);
                }
                catch (HABMqttException e) {
                    logger.error("Failed publishing Thing status message: " + e.getMessage());
                }
            }
        }
    }

    private void processSystemCommand(@NotNull SystemControlPacket packet) {
        logger.info("Received system command : " + packet.getCommand().name() +
                " from client ID: " + packet.getClientID() +
                " at " + packet.getTimestamp().toString());
        switch (packet.getCommand()) {
            case ENABLE:
                _systemQueue.add(() -> {
                    if (isDisabled()) {
                        logger.info("Re-enabling system...");
                        setSystemStatus(SystemStatus.NORMAL);
                        publishSystemStatus();
                    }
                    else {
                        logger.info("System already enabled. Ignoring command.");
                    }
                });
                break;

            case DISABLE:
                _systemQueue.add(() -> {
                    if (isDisabled()) {
                        logger.info("System already disabled. Ignoring command.");
                    }
                    else {
                        logger.warn("Disabling system...");
                        setSystemStatus(SystemStatus.DISABLED);
                        publishSystemStatus();
                    }
                });
                break;

            case RESTART:
                logger.warn("Restarting the system...");
                _systemQueue.add(() -> {
                    try {
                        doStop();
                        Thread.sleep(500);
                        init(_thisContext);
                        doStart();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Failed to restart.");
                    }
                });
                break;

            case SHUTDOWN:
                _systemQueue.add(this::destroy);
                break;

            case HEARTBEAT:
                _systemQueue.add(() -> publishHeartbeat(packet.getClientID()));
                break;

            case GET_SYS_STATUS:
                _systemQueue.add(this::publishSystemStatus);
                break;

            case GET_ALL_THE_THINGS:
                _systemQueue.add(() -> publishThingInventory(packet.getClientID()));
                break;

            case GET_ALL_DEVICE_STATUS:
                _systemQueue.add(this::publishAllDeviceStatuses);
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

        // NOTE: The enable and readonly flags should be handled between the client app and the client device.
        // We are essentially a middle-man, so it isn't our place to handle that stuff here. If the app requests the
        // to control the device, then the device should send back an error if it is disabled or read-only. The app
        // should also already be aware of the device's state and shouldn't attempt to control it without first
        // enabling it.

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
        logger.info("Received thermostat status message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
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

    private void processSwitchControlPacket(@NotNull SwitchControlPacket packet) {
        logger.info("Received Switch control message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        Switch sw = (Switch)Configuration.getThingFromRegistry(packet.getID());
        if (sw == null) {
            logger.warn("Switch not found in Thing registry. DEV_ID: " + packet.getID());
            return;
        }

        logger.info("Switch state: " + packet.getState().name());
        logger.info("Switch enabled: " + packet.isEnabled());
        logger.info("Switch readonly: " + packet.isReadonly());
        logger.info("Switch Thing ID: " + packet.getID());
        logger.info("Switch Client ID: " + packet.getClientID());
        packet.setClientID(Configuration.clientID());
        packet.setTimestamp(Util.getCurrentTimestamp());

        String message = packet.toJsonString();
        String topic = Configuration.thingControlTopicBase() + "/" + packet.getID();
        MqttManager.MqttEvent event = new MqttManager.MqttEvent(topic, message, getNextEventID());
        enqueueOutboundEvent(event);
    }

    private void processSwitchStatusPacket(@NotNull SwitchStatusPacket packet) {
        logger.info("Received Switch status message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        Switch sw = (Switch)Configuration.getThingFromRegistry(packet.getID());
        if (sw == null) {
            logger.warn("Switch not found in Thing registry: " + packet.getID());
            return;
        }

        try {
            sw.mapFromStatusPacket(packet);
            packet.setClientID(Configuration.clientID());
            packet.setTimestamp(Util.getCurrentTimestamp());
            String message = packet.toJsonString();
            String topic = Configuration.applicationTopic();

            logger.info("Publishing Switch status to topic: " + topic);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (Exception e) {
            logger.error("Error publishing status: " + e.getMessage());
        }
    }

    private void processDimmableLightControlPacket(@NotNull DimmableLightControlPacket packet) {
        logger.info("Received Dimmable Light control message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        DimmableLight dml = (DimmableLight)Configuration.getThingFromRegistry(packet.getThingID());
        if (dml == null) {
            logger.warn("Dimmable Light not found in registry: " + packet.getThingID());
            return;
        }

        logger.info("DimmableLight level: " + packet.getLevel());
        logger.info("DimmableLight min level: " + packet.getMinLevel());
        logger.info("DimmableLight max level: " + packet.getMaxLevel());
        logger.info("DimmableLight enable: " + packet.isEnabled());
        logger.info("DimmableLight readonly: " + packet.isReadonly());
        logger.info("DimmableLight Thing ID: " + packet.getThingID());
        logger.info("DimmableLight client ID: " + packet.getClientID());
        packet.setClientID(Configuration.clientID());
        packet.setTimestamp(Util.getCurrentTimestamp());

        String message = packet.toJsonString();
        String topic = Configuration.thingControlTopicBase() + "/" + packet.getThingID();
        MqttManager.MqttEvent event = new MqttManager.MqttEvent(topic, message, getNextEventID());
        enqueueOutboundEvent(event);
    }

    private void processDimmableLightStatusPacket(@NotNull DimmableLightStatusPacket packet) {
        logger.info("Received status message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        DimmableLight dml = (DimmableLight)Configuration.getThingFromRegistry(packet.getThingID());
        if (dml == null) {
            logger.warn("Dimmable light not found in Thing registry. DEV_ID: " + packet.getThingID());
            return;
        }

        try {
            dml.mapFromStatusPacket(packet);
            packet.setClientID(Configuration.clientID());
            packet.setTimestamp(Util.getCurrentTimestamp());
            String message = packet.toJsonString();
            String topic = Configuration.applicationTopic();

            logger.info("Publishing Dimmable light status to topic: " + topic);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (Exception e) {
            logger.error("Error publishing status: " + e.getMessage());
        }
    }

    private void processDoorStatusPacket(@NotNull DoorStatusPacket packet) {
        logger.info("Received status message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        Door d = (Door)Configuration.getThingFromRegistry(packet.getThingID());
        if (d == null) {
            logger.warn("Door not found in Thing registry. DEV_ID: " + packet.getThingID());
            return;
        }

        try {
            d.mapFromStatusPacket(packet);
            packet.setClientID(Configuration.clientID());
            packet.setTimestamp(Util.getCurrentTimestamp());
            String message = packet.toJsonString();
            String topic = Configuration.applicationTopic();

            logger.info("Publishing Door status to topic: " + topic);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (Exception e) {
            logger.error("Error publishing status: " + e.getMessage());
        }
    }

    private void processDoorControlPacket(@NotNull DoorControlPacket packet) {
        logger.info("Received door control message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        Door door = (Door)Configuration.getThingFromRegistry(packet.getThingID());
        if (door == null) {
            logger.warn("Door not found in registry. DEV_ID: " + packet.getThingID());
            return;
        }

        logger.info("Door command: " + packet.getCommand().name());
        logger.info("Door enable: " + packet.isEnabled());
        logger.info("Door lock enable: " + packet.isLockEnabled());
        logger.info("Door readonly: " + packet.isReadonly());
        logger.info("Door Thing ID: " + packet.getThingID());
        logger.info("Door client ID: " + packet.getClientID());
        packet.setClientID(Configuration.clientID());
        packet.setTimestamp(Util.getCurrentTimestamp());

        String message = packet.toJsonString();
        String topic = Configuration.thingControlTopicBase() + "/" + packet.getThingID();
        MqttManager.MqttEvent event = new MqttManager.MqttEvent(topic, message, getNextEventID());
        enqueueOutboundEvent(event);
    }

    private void processMotionSensorStatusPacket(@NotNull MotionSensorStatusPacket packet) {
        logger.info("Received status message from: " + packet.getClientID() + " at " + packet.getTimestamp().toString());
        MotionSensor sensor = (MotionSensor)Configuration.getThingFromRegistry(packet.getThingID());
        if (sensor == null) {
            logger.warn("MotionSensor not found in Thing registry. DEV_ID: " + packet.getThingID());
            return;
        }

        try {
            sensor.mapFromStatusPacket(packet);
            packet.setClientID(Configuration.clientID());
            packet.setTimestamp(Util.getCurrentTimestamp());
            String message = packet.toJsonString();
            String topic = Configuration.applicationTopic();

            logger.info("Publishing MotionSensor status to topic: " + topic);
            MqttManager.getInstance().publish(topic, message);
        }
        catch (Exception e) {
            logger.error("Error publishing status: " + e.getMessage());
        }
    }

    /**
     * Processes the specified MQTT event.
     * @param event The event to process.
     */
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
                    case APP:
                        // TODO process app commands.
                        break;
                    case DIMMABLE_LIGHT:
                        if (isControl) {
                            DimmableLightControlPacket dmlCtrl = DimmableLightControlPacket.fromJsonString(message);
                            if (dmlCtrl != null) {
                                processDimmableLightControlPacket(dmlCtrl);
                            }
                        }
                        else {
                            if (topic.startsWith(Configuration.thingControlTopicBase())) {
                                publishThingControlMessage(topic, message);
                            }

                            if (topic.equalsIgnoreCase(Configuration.thingStatusTopicBase())) {
                                DimmableLightStatusPacket dmlStatus = DimmableLightStatusPacket.fromJsonString(message);
                                if (dmlStatus != null) {
                                    processDimmableLightStatusPacket(dmlStatus);
                                }
                            }
                        }
                        break;
                    case DOOR:
                        if (isControl) {
                            DoorControlPacket doorCtrl = DoorControlPacket.fromJsonString(message);
                            if (doorCtrl != null) {
                                processDoorControlPacket(doorCtrl);
                            }
                        }
                        else {
                            if (topic.startsWith(Configuration.thingControlTopicBase())) {
                                publishThingControlMessage(topic, message);
                            }

                            if (topic.equalsIgnoreCase(Configuration.thingStatusTopicBase())) {
                                DoorStatusPacket dstatus = DoorStatusPacket.fromJsonString(message);
                                if (dstatus != null) {
                                    processDoorStatusPacket(dstatus);
                                }
                            }
                        }
                        break;
                    case MOTION_SENSOR:
                        if (isControl) {
                            logger.warn("Cannot send control packets to MotionSensor types as they are read-only.");
                        }
                        else {
                            if (topic.startsWith(Configuration.thingControlTopicBase())) {
                                logger.warn("Cannot send control packets to MotionSensor types as they are read-only.");
                            }

                            // We received a status message from a Thing.
                            if (topic.equalsIgnoreCase(Configuration.thingStatusTopicBase())) {
                                MotionSensorStatusPacket mstatus = MotionSensorStatusPacket.fromJsonString(message);
                                if (mstatus != null) {
                                    processMotionSensorStatusPacket(mstatus);
                                }
                            }
                        }
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
                    case SWITCH:
                        if (isControl) {
                            SwitchControlPacket switchCtrl = SwitchControlPacket.fromJsonString(message);
                            if (switchCtrl != null) {
                                processSwitchControlPacket(switchCtrl);
                            }
                        }
                        else {
                            if (topic.startsWith(Configuration.thingControlTopicBase())) {
                                publishThingControlMessage(topic, message);
                            }

                            if (topic.equalsIgnoreCase(Configuration.thingStatusTopicBase())) {
                                SwitchStatusPacket swStatus = SwitchStatusPacket.fromJsonString(message);
                                if (swStatus != null) {
                                    processSwitchStatusPacket(swStatus);
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

    /**
     * Enqueues an event in the inbound event queue for processing.
     * @param event The event to add to the inbound event queue.
     */
    private synchronized void enqueueInboundEvent(@NotNull MqttManager.MqttEvent event) {
        logger.info("Enqueueing inbound event ID " + event.messageId());
        _inboundEventQueue.add(event);
    }

    /**
     * Enqueues an event in the outbound event queue for processing.
     * @param event The event to add to the outbound event queue.
     */
    private synchronized void enqueueOutboundEvent(@NotNull MqttManager.MqttEvent event) {
        logger.info("Enqueueing outbound event ID " + event.messageId());
        _outboundEventQueue.add(event);
    }

    /**
     * Gets the next event in the inbound event queue and then removes it from the queue.
     * @return The next event in the inbound event queue.
     */
    @Nullable
    private synchronized MqttManager.MqttEvent dequeueInboundEvent() {
        if (_inboundEventQueue != null && !_inboundEventQueue.isEmpty()) {
            return _inboundEventQueue.remove();
        }
        return null;
    }

    /**
     * Gets the next event in the outbound event queue and then removes it from the queue.
     * @return The next event in the outbound event queue.
     */
    @Nullable
    private synchronized MqttManager.MqttEvent dequeueOutboundEvent() {
        if (_outboundEventQueue != null && !_outboundEventQueue.isEmpty()) {
            return _outboundEventQueue.remove();
        }
        return null;
    }

    /**
     * (non-javadoc)
     * @see net.cyrusbuilt.cyrushab.core.mqtt.MqttManager.MqttEventListener#onConnectionLost(Throwable)
     */
    @Override
    public void onConnectionLost(Throwable cause) {
        setSystemStatus(SystemStatus.DISCONNECTED);
        _reconnects++;
        logger.error("MQTT connection lost! Cause: " + cause.toString());
        logger.error("Attempting reconnect (attempt " + _reconnects + " of " + MAX_RECONNECT_ATTEMPTS + ") ...");
        try {
            if (_reconnects < MAX_RECONNECT_ATTEMPTS) {
                setSystemStatus(SystemStatus.RECONNECTING);
                MqttManager.getInstance().connect();
                logger.info("Successfully reconnected to " + Configuration.mqttBroker());
                setSystemStatus(SystemStatus.NORMAL);
                publishSystemStatus();
            } else {
                logger.error("Failed to reconnect to MQTT host.");
                Timestamp nextRun = new Timestamp(System.currentTimeMillis() + 30000L);
                logger.error("Will attempt again at " + nextRun.toLocalDateTime().toString());
                _reconnects = 0;
                try {
                    _reconnectTimer.scheduleAtFixedRate(_reconnectTask, 30000L, 30000L);
                }
                catch (IllegalStateException e) {
                    logger.warn(e.getMessage());
                }
            }
        }
        catch (HABMqttException ex) {
            logger.error("Failed to re-establish connection to MQTT broker: " + Configuration.mqttBroker() +
                    ", Reason: " + ex.getMessage());
            setSystemStatus(SystemStatus.DISCONNECTED);
            onConnectionLost(ex);
        }
    }

    /**
     * (non-javadoc)
     * @see MqttManager.MqttEventListener#onMessageReceived(MqttManager.MqttEvent)
     */
    @Override
    public void onMessageReceived(MqttManager.MqttEvent event) {
        logger.info("MQTT message received.\nMessage ID: " + event.messageId() +
                "\nTopic: " + event.topic() +
                "\nMessage: " + event.message());

        // Queue actionable events for processing on a separate thread.
        enqueueInboundEvent(event);
    }

    /**
     * (non-Javadoc)
     * @see MqttManager.MqttEventListener#onMessageDelivered(MqttManager.MqttEvent)
     */
    @Override
    public void onMessageDelivered(MqttManager.MqttEvent event) {
        logger.info("Message delivered to topic: " + event.topic() +
                "\nMessage ID: " + event.messageId() +
                "\nMessage: " + event.message());
    }

    /**
     * (non-Javadoc)
     * @see Daemon#init(DaemonContext)
     */
    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        final Object[] args = context.getArguments();
        logger.info("HABDaemon initialized.");
        logger.info("Initializing configuration...");
        _thisContext = context;
        try {
            // Init the configuration manager. Terminate on failure.
            Configuration.initialize();
        }
        catch (Exception ex) {
            logger.error("Cannot read configuration: " + ex.getMessage());
            System.exit(1);
        }

        // MQTT reconnect task. Should be scheduled when the connection to the MQTT host is lost.
        _reconnectTask = new TimerTask() {
            @Override
            public void run() {
                if (getStatus() == SystemStatus.DISCONNECTED || getStatus() == SystemStatus.RECONNECTING) {
                    logger.warn("MQTT still disconnected. Attempting periodic reconnect....");
                    onConnectionLost(new Throwable("Previous connection failure."));
                    if (getStatus() == SystemStatus.NORMAL) {
                        cancel();
                        _reconnectTimer.purge();
                    }
                }
                else {
                    logger.info("MQTT reconnected. Cancelling reconnect task.");
                    cancel();
                    _reconnectTimer.purge();
                }
            }
        };

        // Main system thread. Runs the main loop which processes tasks in the system queue.
        if (_mainThread == null) {
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
                        while (!isInterrupted()) {
                            long now = System.currentTimeMillis();
                            if ((now - lastTick) >= 1000) {
                                lastTick = now;
                            }

                            HABDaemon.this.loop();
                        }
                    }
                }
            };
            _mainThread.setName("CyrusHAB_MainLoop");
        }

        // Thread for processing events in the inbound queue.
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

        // Thread for processing events in the outbound queue.
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

    /**
     * Main startup routine. Reads configuration and Thing registry, then starts up the subsystems like MQTT and
     * inbound and outbound queue processors.
     */
    private void doStart() {
        // Load daemon config. Terminate on failure.
        logger.info("Loading configuration...");
        try {
            Configuration.reloadConfig();
            Configuration.reloadThingRegistry();
        }
        catch (Exception e) {
            logger.error("Cannot read configuration: " + e.getMessage());
            System.exit(1);
        }

        // Init the MQTT manager.
        initMqttManager();
        setSystemStatus(SystemStatus.NORMAL);

        // Start the processor threads.
        logger.info("Starting inbound event queue processor...");
        _inboundEventProcessor.start();

        logger.info("Starting outbound event queue processor...");
        _outboundEventProcessor.start();

        logger.info("Startup complete.");
    }

    /**
     * (non-Javadoc)
     * @see Daemon#start()
     */
    @Override
    public void start() throws Exception {
        // Start the main thread then call the internal start method.
        logger.info("HABDaemon starting...");
        _mainThread.start();

        // NOTE: We have our own private start method for starting subsystems that allows us to do a restart.
        // _mainThread is only started once and remains active until destroy() is called. So even when a call
        // is made to doStop() the main thread continues running to process system tasks like restarting the
        // subsystems. This allows us to "restart" internally without having to tear down the whole daemon and
        // relaunch it.
        doStart();
    }

    /**
     * Main shutdown routine. Publishes the shutdown if it can, then stops the inbound and outbound queue processors,
     * and then finally stops the MQTT manager.
     */
    private void doStop() {
        // Let everyone know we are shutting down first.
        logger.info("Stop requested.");
        setSystemStatus(SystemStatus.SHUTDOWN);
        publishSystemStatus();

        try {
            logger.info("Stopping inbound event queue processor...");
            _inboundEventProcessor.interrupt();
            logger.info("Stopping outbound event queue processor...");
            _outboundEventProcessor.interrupt();
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
            //throw ex;
        }

        logger.info("Stopping MQTT manager...");
        MqttManager.getInstance().shutdown();
    }

    /**
     * (non-Javadoc)
     * @see Daemon#stop()
     */
    @Override
    public void stop() throws Exception {
        // NOTE: Per the API docs, if you just call stop(), it will be immediately followed by a call to destroy().
        // So we have our own private stop method that we can call that won't result in destroy being called.
        doStop();
    }

    /**
     * (non-Javadoc)
     * @see Daemon#destroy()
     */
    @Override
    public void destroy() {
        if (isRunning()) {
            try {
                stop();
            }
            catch (Exception ignored) {
            }
        }

        logger.info("Stopping HABDaemon...");
        _mainThread.interrupt();
        _mainThread = null;
        logger.info("HABDaemon stopped.");
        _inboundEventProcessor = null;
        _inboundEventQueue.clear();
        _inboundEventQueue = null;
        _outboundEventProcessor = null;
        _outboundEventQueue.clear();
        _outboundEventQueue = null;
        logger.info("HABDaemon destroyed.");
        System.exit(0);
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
