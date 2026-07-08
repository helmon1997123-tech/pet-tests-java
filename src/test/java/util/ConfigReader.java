package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Читает base.url из classpath-конфига.
 *
 * Профиль выбирается через -Denv=<name> -> src/test/resources/config-<name>.properties.
 * По умолчанию (без -Denv) используется config.properties.
 * Точечный оверрайд без правки файлов: -Dbase.url=https://example.com — приоритет выше конфига.
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    public static String baseUrl() {
        String override = System.getProperty("base.url");
        if (override != null && !override.isBlank()) {
            return normalize(override);
        }
        return normalize(PROPERTIES.getProperty("base.url"));
    }

    private static String normalize(String url) {
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private static Properties load() {
        String env = System.getProperty("env");
        String fileName = (env == null || env.isBlank())
                ? "config.properties"
                : "config-" + env + ".properties";

        Properties props = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found on classpath: " + fileName);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config file: " + fileName, e);
        }
        return props;
    }
}