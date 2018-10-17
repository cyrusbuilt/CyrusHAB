package net.cyrusbuilt.cyrushab.daemon;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;

public final class Util {
    private Util() {}

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

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
