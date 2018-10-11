package net.cyrusbuilt.cyrushab.daemon;


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

public final class Configuration {
    private Configuration() {}

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static File _configFile = null;
    private static File _thingRegistry = null;
    private static String _mqttBroker = StringUtils.EMPTY;
    private static long _port = 1883;
    private static String _username = StringUtils.EMPTY;
    private static String _password = StringUtils.EMPTY;
    private static List<Thing> _allThings = null;

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

    public static void reloadConfig() {
        logger.info("Reading configuration...");
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(_configFile.getAbsolutePath()));
            JSONObject jsonObject = (JSONObject)obj;
            _mqttBroker = (String)jsonObject.get("broker");
            _port = (long)jsonObject.get("port");
            _username = (String)jsonObject.get("username");
            _password = (String)jsonObject.get("password");
        }
        catch (Exception ex) {
            logger.error("Failed to read config: " + ex.getMessage());
        }
    }

    public static void saveConfig() {
        JSONObject obj = new JSONObject();
        obj.put("broker", _mqttBroker);
        obj.put("port", _port);
        obj.put("username", _username);
        obj.put("password", _password);

        try {
            try (FileWriter file = new FileWriter(_configFile.getAbsolutePath())) {
                file.write(obj.toJSONString());
            }
        }
        catch (IOException ex) {
            logger.error("Failed to save configuration!: " + ex.getMessage());
        }
    }

    @Nullable
    private static Thing parseThingFromFile(@NotNull File thingFile) {
        JSONParser parser = new JSONParser();
        try {
            Thing result = null;
            Object obj = parser.parse(new FileReader(thingFile.getAbsolutePath()));
            JSONObject jsonObject = (JSONObject)obj;
            String name = (String)jsonObject.get("name");
            String controlTopicName = (String)jsonObject.get("controlTopic");
            String statusTopicName = (String)jsonObject.get("statusTopic");
            boolean enabled = (boolean)jsonObject.get("enabled");
            boolean readonly = (boolean)jsonObject.get("readonly");

            int typeVal = (int)(long)jsonObject.get("type");
            ThingType type = ThingType.UNKNOWN.getType(typeVal);
            switch (type) {
                case SWITCH:
                    Switch newSwitch = new Switch() {
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
                        public void setEnabled(boolean enabled) {
                            super.setEnabled(enabled);
                        }

                        @Override
                        public void setIsReadonly(boolean readonly) {
                            super.setIsReadonly(readonly);
                        }
                    };

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

    public static String mqttBroker() {
        return _mqttBroker;
    }

    public static void setMqttBroker(String broker) {
        _mqttBroker = broker;
    }

    public static long port() {
        return _port;
    }

    public static void setPort(long port) {
        _port = port;
    }

    public static String username() {
        return _username;
    }

    public static void setUsername(String username) {
        _username = username;
    }

    public static String password() {
        return _password;
    }

    public static void setPassword(String password) {
        _password = password;
    }

    public static List<Thing> getThingRegistry() {
        return _allThings;
    }
}
