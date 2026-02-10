package hr.tvz.cyberpunkunfolds.rmi.server;

import hr.tvz.cyberpunkunfolds.engine.GameRules;
import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.net.tcp.GameServerManager;
import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.rmi.protocol.RoomEventService;
import lombok.extern.slf4j.Slf4j;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

@Slf4j
public final class RmiBootstrap {
    public static void main(String[] args) {
        Config cfg = Config.load();
        int rmiPort = cfg.getInt("rmi.port", 1099);
        int tcpPort = cfg.getInt("tcp.port", 9999);
        String worldConfigPath = cfg.get("world.config", "world/world.xml");
        GameRules rulesTemplate = new GameRules(cfg.getInt("game.alarm.max", 18));

        try (GameServerManager gameServerManager = new GameServerManager(tcpPort, worldConfigPath, rulesTemplate)) {
            gameServerManager.startAsync();
            log.info("GameServerManager started on TCP port {}", tcpPort);

            // start RMI services -pass gameServerManager to the lobby
            Registry registry = LocateRegistry.createRegistry(rmiPort);

            RoomEventService roomEventsImpl = new RoomEventServiceImpl();
            LobbyService lobbyImpl = new LobbyServiceImpl(roomEventsImpl, gameServerManager);

            LobbyService lobbyStub = (LobbyService) UnicastRemoteObject.exportObject(lobbyImpl, 0);
            RoomEventService roomEventsStub = (RoomEventService) UnicastRemoteObject.exportObject(roomEventsImpl, 0);

            registry.rebind(LobbyService.BIND_NAME, lobbyStub);
            registry.rebind(RoomEventService.BIND_NAME, roomEventsStub);

            log.info("RMI registry started on port {}", rmiPort);
            log.info("Bound: {}", LobbyService.BIND_NAME);
            log.info("Bound: {}", RoomEventService.BIND_NAME);

            // keep alive
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("RMI server interrupted", e);
        } catch (Exception e) {
            log.error("Failed to start RMI server", e);
        }
    }
}
