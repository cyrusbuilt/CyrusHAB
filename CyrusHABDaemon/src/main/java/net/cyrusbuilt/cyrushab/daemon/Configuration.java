package net.cyrusbuilt.cyrushab.daemon;


import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import net.cyrusbuilt.cyrushab.core.things.switches.Switch;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Global configuration manager.
 */
public final class Configuration {
    /**
     * This is a static class, thus a private constructor.
     */
    private Configuration() {}

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final String CONFIG_CLIENT_ID = "client_id";
    private static final String CONFIG_BROKER = "broker";
    private static final String CONFIG_PORT = "port";
    private static final String CONFIG_USERNAME = "username";
    private static final String CONFIG_PASSWORD = "password";
    private static final String CONFIG_SYS_STATUS_TOPIC = "hab_status_topic";
    private static final String CONFIG_SYS_CONTROL_TOPIC = "hab_control_topic";
    private static final String CONFIG_THING_STATUS_TOPIC_BASE = "cyrushab/thing/status";
    private static final String CONFIG_THING_CONTROL_TOPIC_BASE = "cyrushab/thing/control";
    private static final String THING_NAME = "name";
    private static final String THING_CONTROL_TOPIC = "control_topic";
    private static final String THING_STATUS_TOPIC = "status_topic";
    private static final String THING_ENABLED = "enabled";
    private static final String THING_READONLY = "readonly";
    private static final String THING_ID = "id";
    private static final String THING_TYPE = "type";

    private static File _configFile = null;
    private static File _thingRegistry = null;
    private static String _clientID = StringUtils.EMPTY;
    private static String _mqttBroker = StringUtils.EMPTY;
    private static long _port = 1883;
    private static String _username = StringUtils.EMPTY;
    private static String _password = StringUtils.EMPTY;
    private static String _sysStatusTopic = StringUtils.EMPTY;
    private static String _sysControlTopic = StringUtils.EMPTY;
    private static String _thingStatusTopicBase = StringUtils.EMPTY;
    private static String _thingControlTopicBase = StringUtils.EMPTY;
    private static List<Thing> _allThings = null;

    /**
     * Initialize the configuration. This will attempt to read the local config file. If not found, then the default
     * config file will be copied from an internal resource to the config directory which is a subdirectory of
     * application's execution directory and loaded from there. If the config directory does not exist, it will be
     * created first.
     * @throws FileNotFoundException if unable to create the config directory or unable to retrieve the default config
     * from the internal resource, or unable to copy the default config file to the config directory, or unable to
     * create the thing directory.
     */
    public static void initialize() throws FileNotFoundException {
        _allThings = new ArrayList<>();

        File execDir = Util.getExecutionDir();
        if (execDir != null) {
            String configPath = execDir.getAbsolutePath() + File.separator + "config";
            File configDir = new File(configPath);
            if (!configDir.exists()) {
                // Probably first run. Create the directory and copy the default config.
                logger.warn("Configuration directory does not exist. Creating directory: " + configDir.getAbsolutePath());
                if (!configDir.mkdir()) {
                    throw new FileNotFoundException("Failed to create directory: " + configDir.getAbsolutePath());
                }

                logger.info("Locating default configuration...");
                ClassLoader classLoader = HABDaemon.getInstance().getClass().getClassLoader();
                try {
                    try (InputStream inStream = classLoader.getResourceAsStream("config.json")) {
                        if (inStream == null) {
                            throw new FileNotFoundException("Cannot get resource: config.json from jar file.");
                        }

                        int readBytes = 0;
                        byte[] buffer = new byte[4096];
                        String dest = configDir.getAbsolutePath() + File.separator + "config.json";
                        File destFile = new File(dest);

                        logger.info("Copying resource config.json to " + destFile.getAbsolutePath());
                        try (OutputStream outStream = new FileOutputStream(destFile.getAbsolutePath())) {
                            while ((readBytes = inStream.read(buffer)) > 0) {
                                outStream.write(buffer, 0, readBytes);
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    throw new FileNotFoundException("Configuration failed to copy: " + ex.getMessage());
                }
            }

            File configFile = new File(configDir.getAbsolutePath() + File.separator + "config.json");
            if (!configFile.exists()) {
                throw new FileNotFoundException(configFile.getAbsolutePath() + " not found!!");
            }

            _configFile = configFile;

            logger.info("Locating things registry...");
            String thingsPath = execDir.getAbsolutePath() + File.separator + "things";
            File thingRegistry = new File(thingsPath);
            if (!thingRegistry.exists()) {
                if (!thingRegistry.mkdir()) {
                    throw new FileNotFoundException("Unable to create directory: " + thingRegistry.getAbsolutePath());
                }
            }

            _thingRegistry = thingRegistry;
        }
    }

    /**
     * Reloads the system configuration values from the config file.
     */
    public static void reloadConfig() {
        logger.info("Reading configuration...");
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(_configFile.getAbsolutePath()));
            JSONObject jsonObject = (JSONObject)obj;
            _clientID = (String)jsonObject.get(CONFIG_CLIENT_ID);
            _mqttBroker = (String)jsonObject.get(CONFIG_BROKER);
            _port = (long)jsonObject.get(CONFIG_PORT);
            _username = (String)jsonObject.get(CONFIG_USERNAME);
            _password = (String)jsonObject.get(CONFIG_PASSWORD);
            _sysStatusTopic = (String)jsonObject.get(CONFIG_SYS_STATUS_TOPIC);
            _sysControlTopic = (String)jsonObject.get(CONFIG_SYS_CONTROL_TOPIC);
            _thingStatusTopicBase = (String)jsonObject.get(CONFIG_THING_STATUS_TOPIC_BASE);
            _thingControlTopicBase = (String)jsonObject.get(CONFIG_THING_CONTROL_TOPIC_BASE);
        }
        catch (Exception ex) {
            logger.error("Failed to read config: " + ex.getMessage());
        }
    }

    /**
     * Saves configuration changes to the config file.
     */
    public static void saveConfig() {
        JSONObject obj = new JSONObject();
        obj.put(CONFIG_BROKER, _mqttBroker);
        obj.put(CONFIG_PORT, _port);
        obj.put(CONFIG_USERNAME, _username);
        obj.put(CONFIG_PASSWORD, _password);

        try {
            try (FileWriter file = new FileWriter(_configFile.getAbsolutePath())) {
                file.write(obj.toJSONString());
            }
        }
        catch (IOException ex) {
            logger.error("Failed to save configuration!: " + ex.getMessage());
        }
    }

    /**
     * Parses a thing from a thing descriptor file.
     * @param thingFile The thing file.
     * @return The thing that was parsed from the descriptor file.
     */
    @Nullable
    private static Thing parseThingFromFile(@NotNull File thingFile) {
        JSONParser parser = new JSONParser();
        try {
            Thing result = null;
            Object obj = parser.parse(new FileReader(thingFile.getAbsolutePath()));
            JSONObject jsonObject = (JSONObject)obj;
            String name = (String)jsonObject.get(THING_NAME);
            String controlTopicName = (String)jsonObject.get(THING_CONTROL_TOPIC);
            String statusTopicName = (String)jsonObject.get(THING_STATUS_TOPIC);
            boolean enabled = (boolean)jsonObject.get(THING_ENABLED);
            boolean readonly = (boolean)jsonObject.get(THING_READONLY);
            int id = (int)(long)jsonObject.get(THING_ID);

            // What kind of thing is this?
            int typeVal = (int)(long)jsonObject.get(THING_TYPE);
            ThingType type = ThingType.UNKNOWN.getType(typeVal);
            switch (type) {
                case SWITCH:
                    // Its a switch. So build a switch thing.
                    Switch newSwitch = new Switch() {
                        @Override
                        public void setThingID(int id) {
                            super.setThingID(id);
                        }

                        @Override
                        public void setName(String name) {
                            super.setName(name);
                        }

                        @Override
                        public void setMqttControlTopic(String topicName) {
                            super.setMqttControlTopic(topicName);
                        }

                        @Override
                        public void setMqttStatusTopic(String topicName) {
                            super.setMqttStatusTopic(topicName);
                        }

                        @Override
                        public void setEnabled(boolean enabled) throws ObjectDisposedException {
                            super.setEnabled(enabled);
                        }

                        @Override
                        public void setIsReadonly(boolean readonly) {
                            super.setIsReadonly(readonly);
                        }
                    };

                    newSwitch.setThingID(id);
                    newSwitch.setName(name);
                    newSwitch.setMqttControlTopic(controlTopicName);
                    newSwitch.setMqttStatusTopic(statusTopicName);
                    newSwitch.setEnabled(enabled);
                    newSwitch.setIsReadonly(readonly);
                    result = newSwitch;
                    break;
                case THERMOSTAT:
                    // TODO load thermostat
                    break;
                case MOTION_SENSOR:
                    // TODO load motion sensor
                    break;
                case DIMMABLE_LIGHT:
                    // TODO load dimmable light
                    break;
                case UNKNOWN:
                    // TODO what to do here?
                    break;
            }

            return result;
        }
        catch (Exception ex) {
            logger.error("Unable parse thing from file: " + thingFile.getAbsolutePath());
            logger.error(ex.toString());
        }

        return null;
    }

    /**
     * Reloads the thing registry by reading all the thing descriptors from the thing directory.
     */
    public static void reloadThingRegistry() {
        logger.info("Reading thing registry...");
        File[] files = _thingRegistry.listFiles((dir, name) -> name.endsWith(".thing"));
        if (files != null) {
            // LOAD ALL THE THINGS!!!
            logger.info("Located " + files.length + " items in thing registry.");
            _allThings.clear();
            for (File thing : files) {
                Thing newThing = parseThingFromFile(thing);
                if (newThing != null) {
                    _allThings.add(newThing);
                    logger.info("Loaded thing. Name: " + newThing.name() + ", type: " + newThing.type().name());
                }
            }

            logger.info("Finished loading " + _allThings.size() + " things.");
        }
    }

    /**
     * Gets the MQTT client ID.
     * @return The client ID.
     */
    public static String clientID() {
        return _clientID;
    }

    /**
     * Sets the MQTT client ID.
     * @param clientID The client ID.
     */
    public static void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Gets the MQTT broker host.
     * @return The broker.
     */
    public static String mqttBroker() {
        return _mqttBroker;
    }

    /**
     * Sets the MQTT broker host.
     * @param broker The broker.
     */
    public static void setMqttBroker(String broker) {
        _mqttBroker = broker;
    }

    /**
     * Gets the MQTT port.
     * @return The port.
     */
    public static long port() {
        return _port;
    }

    /**
     * Sets the MQTT port.
     * @param port The port.
     */
    public static void setPort(long port) {
        _port = port;
    }

    /**
     * Gets the MQTT username.
     * @return The username.
     */
    public static String username() {
        return _username;
    }

    /**
     * Sets the MQTT username.
     * @param username The username.
     */
    public static void setUsername(String username) {
        _username = username;
    }

    /**
     * Gets the MQTT password.
     * @return The password.
     */
    public static String password() {
        return _password;
    }

    /**
     * Sets the MQTT password.
     * @param password The password.
     */
    public static void setPassword(String password) {
        _password = password;
    }

    /**
     * Gets the thing registry, which is a list of things.
     * @return The thing registry.
     */
    public static List<Thing> getThingRegistry() {
        return _allThings;
    }

    /**
     * Gets the system status MQTT topic.
     * @return The system status topic.
     */
    public static String systemStatusTopic() {
        return _sysStatusTopic;
    }

    /**
     * Gets the system control MQTT topic.
     * @return The control topic.
     */
    public static String systemControlTopic() {
        return _sysControlTopic;
    }

    /**
     * Gets the thing status topic base.
     * @return The status topic base.
     */
    public static String thingStatusTopicBase() {
        return _thingStatusTopicBase;
    }

    /**
     * Gets the thing control topic base.
     * @return The control topic.
     */
    public static String thingControlTopicBase() {
        return _thingControlTopicBase;
    }

    /**
     * Gets a thing from the thing registry by ID.
     * @param thingID The thing ID.
     * @return The thing with a matching ID or null if not found.
     */
    @Nullable
    public static Thing getThingFromRegistry(int thingID) {
        Thing result = null;
        for (Thing theThing : _allThings) {
            if (theThing.id() == thingID) {
                result = theThing;
                break;
            }
        }
        return result;
    }
}
