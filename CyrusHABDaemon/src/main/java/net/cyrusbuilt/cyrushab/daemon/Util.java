package net.cyrusbuilt.cyrushab.daemon;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;

/**
 * Contains common utility methods.
 */
public final class Util {
    private Util() {}

    /**
     * Gets the parent directory of the executing JAR.
     * @return The application's execution directory. This is typically the parent directory of the executing jar.
     * For example: If the executing jar is in /opt/my_app/lib/may_app.jar, then the directory returned will be
     * /opt/my_app. This is because typically Java app distributions put all the JARs in the 'lib' folder of the
     * application and a bootstrap script in the 'bin' sub-directory of application. The actual application directory
     * is the parent directory of both of those.
     */
    @Nullable
    public static File getExecutionDir() {
        String absPath = HABDaemon.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        absPath = absPath.substring(0, absPath.lastIndexOf(File.separator));
        try {
            absPath = URLDecoder.decode(absPath, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            absPath = null;
        }

        if (absPath != null) {
            File execDir = new File(absPath);
            execDir = execDir.getParentFile();
            if (execDir.exists() && execDir.canRead()) {
                return execDir;
            }
        }

        return null;
    }

    /**
     * Gets a timestamp representing the current local system time.
     * @return The current timestamp.
     */
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
