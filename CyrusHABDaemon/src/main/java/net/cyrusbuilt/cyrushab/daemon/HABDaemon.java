package net.cyrusbuilt.cyrushab.daemon;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HABDaemon implements Daemon {
    private static final Logger logger = LoggerFactory.getLogger(HABDaemon.class);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean _stopped = false;
    private Thread _mainThread = null;

    public HABDaemon() {
        super();
    }

    public static void main(String[] args) {

    }

    private synchronized void setStopped(final boolean isStopped) {
        _stopped = isStopped;
    }

    public synchronized boolean isRunning() {
        return !_stopped;
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        final Object[] args = context.getArguments();
        logger.info("HABDaemon initialized with arguments {}.", args);
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
