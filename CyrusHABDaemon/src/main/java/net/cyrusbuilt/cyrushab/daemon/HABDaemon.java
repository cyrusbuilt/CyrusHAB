package net.cyrusbuilt.cyrushab.daemon;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HABDaemon implements Daemon {
    private static final Logger logger = LoggerFactory.getLogger(HABDaemon.class);
    private static HABDaemon _instance;
    private volatile boolean _stopped = false;
    private Thread _mainThread = null;

    public HABDaemon() {
        super();
        _instance = this;
    }

    public static HABDaemon getInstance() {
        return _instance;
    }

    private synchronized void setStopped(final boolean isStopped) {
        _stopped = isStopped;
    }

    public synchronized boolean isRunning() {
        return !_stopped;
    }

    private void loop() {

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
    }

    @Override
    public void start() throws Exception {
        logger.info("HABDaemon starting...");
        _mainThread.start();
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping HABDaemon...");
        setStopped(true);
        try {
            _mainThread.join(1000);
        }
        catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
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
    }
}
