package hr.tvz.cyberpunkunfolds.rmi.client;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

public final class RmiClientFactory {
    private RmiClientFactory() { }

    public static LobbyService lobby(Config cfg) {
        return (LobbyService) lookup(cfg, LobbyService.BIND_NAME);
    }

    public static RoomEventService roomEvents(Config cfg) {
        return (RoomEventService) lookup(cfg, RoomEventService.BIND_NAME);
    }

    private static Object lookup(Config cfg, String name) {
        try {
            String host = cfg.get("rmi.host", "localhost");
            int port = cfg.getInt("rmi.port", 1099);

            Context ctx = new InitialContext(jndiEnv(host, port));
            try {
                return ctx.lookup(name);
            } finally {
                closeQuietly(ctx);
            }
        } catch (Exception e) {
            throw new IllegalStateException("RMI lookup failed for " + name, e);
        }
    }

    @SuppressWarnings("java:S1149") // JNDI InitialContext requires a Hashtable in the constructor.
    private static Hashtable<String, String> jndiEnv(String host, int port) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://" + host + ":" + port);
        return env;
    }

    private static void closeQuietly(Context ctx) {
        if (ctx == null) {
            return;
        }
        try {
            ctx.close();
        } catch (Exception _) {
            // already closed
        }
    }
}
