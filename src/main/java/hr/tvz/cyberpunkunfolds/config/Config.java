package hr.tvz.cyberpunkunfolds.config;

import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Properties;

@Slf4j
public final class Config {
    private final Properties props;

    private Config(Properties props) {
        this.props = props;
    }

    public static Config load() {
        Properties p = new Properties();

        // from classpath
        try (InputStream in = Config.class.getResourceAsStream("/conf.properties")) {
            if (in != null) {
                p.load(in);
                log.info("Loaded conf.properties from classpath.");
            } else {
                log.warn("conf.properties not found on classpath. using defaults");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading conf.properties", e);
        }

        // -Drmi.jndi.url=rmi://localhost:1099
        String jndiUrl = System.getProperty("rmi.jndi.url");
        if (jndiUrl != null && !jndiUrl.isBlank()) {
            try {
                Properties overrides = lookupJndiProperties(jndiUrl);
                p.putAll(overrides);
                log.info("Overridden JNDI: {}", jndiUrl);
            } catch (Exception e) {
                log.warn("Failed to override JNDI with {}", jndiUrl, e);
            }
        }

        return new Config(p);
    }

    @SuppressWarnings("java:S1149") // JNDI InitialContext requires a Hashtable in the constructor.
    private static Properties lookupJndiProperties(String providerUrl) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, providerUrl);

        Context ctx = new InitialContext(env);

        Properties result = new Properties();
        try {
            Object host = ctx.lookup("conf/rmi.host");
            Object port = ctx.lookup("conf/rmi.port");
            if (host != null) result.put("rmi.host", host.toString());
            if (port != null) result.put("rmi.port", port.toString());
        } finally {
            try { ctx.close(); } catch (Exception _) {
                // already closed
            }
        }
        return result;
    }

    public String get(String key, String def) {
        return props.getProperty(key, def);
    }

    public int getInt(String key, int def) {
        String v = props.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException _) {
            return def;
        }
    }

    public Path appHome() {
        String override = get("app.home", "").trim();
        if (!override.isBlank()) {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.home"), ".cyberpunk-unfolds");
    }

    public Path saveDir() {
        String override = get("app.saveDir", "").trim();
        if (!override.isBlank()) {
            return Path.of(override);
        }
        return appHome().resolve("save");
    }

    public Path xmlDir() {
        String override = get("app.xmlDir", "").trim();
        if (!override.isBlank()) {
            return Path.of(override);
        }
        return appHome().resolve("xml");
    }

    public Path docsDir() {
        String override = get("app.docsDir", "").trim();
        if (!override.isBlank()) {
            return Path.of(override);
        }
        return appHome().resolve("docs");
    }
}
