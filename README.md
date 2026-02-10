# Cyberpunk Unfolds (JavaFX)
A JavaFX project for a **co-op board / escape-room simulator**.

## Prerequisites
- **JDK 25**
- **Maven 3.9+**

## Run the UI
### Maven:
```bash
mvn clean javafx:run
```
### IDE:
Run:
```
hr.tvz.cyberpunkunfolds.Launcher
```

## Run the RMI server (Lobby and Chat)
Start before opening the Lobby screen in the UI.
### Maven:
```bash
mvn exec:java@rmi-server
```
### IDE:
Run:
```
hr.tvz.cyberpunkunfolds.rmi.server.RmiBootstrap
```

## Local data files
The app writes in the current user's home directory:
- `~/.cyberpunk-unfolds/save/` — serialization saves
- `~/.cyberpunk-unfolds/xml/` — move logs / replay
- `~/.cyberpunk-unfolds/docs/` — generated docs
