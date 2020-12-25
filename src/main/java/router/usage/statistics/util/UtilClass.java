package router.usage.statistics.util;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

public class UtilClass {

    private UtilClass() { throw new IllegalStateException("Utility class"); }

    // Constants

    public static final String SERVER_PORT = "PORT";
    public static final String MONGODB_DATABASE = "DBNAME";
    public static final String MONGODB_USERNAME = "DBUSR";
    public static final String MONGODB_PASSWORD = "DBPWD";
    public static final String JSOUP_USERNAME = "JSUSR";
    public static final String JSOUP_PASSWORD = "JSPWD";


    // Common

    public static String getSystemEnvProperty(String keyName) {
        return getProperty(keyName) == null ? getenv(keyName) : getProperty(keyName);
    }

    public static String getLongerDate() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getShorterDate() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 10);
    }
}
