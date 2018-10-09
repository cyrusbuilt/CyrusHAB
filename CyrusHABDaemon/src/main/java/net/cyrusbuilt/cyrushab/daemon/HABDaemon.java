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

    }
}
