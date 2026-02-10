package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record RoomEvent(
        RoomEventType type,
        String roomId,
        String actor,
        String text,
        long epochMillis
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public RoomEvent {
        Objects.requireNonNull(type);
        Objects.requireNonNull(roomId);
        Objects.requireNonNull(actor);
    }

    public static RoomEvent message(String roomId, String from, String text) {
        return new RoomEvent(RoomEventType.MESSAGE, roomId, from, text, System.currentTimeMillis());
    }

    public static RoomEvent join(String roomId, String player) {
        return new RoomEvent(RoomEventType.JOIN, roomId, player, null, System.currentTimeMillis());
    }

    public static RoomEvent leave(String roomId, String player) {
        return new RoomEvent(RoomEventType.LEAVE, roomId, player, null, System.currentTimeMillis());
    }

    public static RoomEvent roomCreated(String roomId, String host) {
        return new RoomEvent(RoomEventType.ROOM_CREATED, roomId, host, null, System.currentTimeMillis());
    }

    public static RoomEvent roomClosed(String roomId, String actor) {
        return new RoomEvent(RoomEventType.ROOM_CLOSED, roomId, actor, null, System.currentTimeMillis());
    }

    public static RoomEvent gameStarted(String roomId, String host) {
        return new RoomEvent(RoomEventType.GAME_STARTED, roomId, host, null, System.currentTimeMillis());
    }

    public String toDisplayLine() {
        return switch (type) {
            case MESSAGE -> "[" + roomId + "] " + actor + ": " + safe(text);
            case JOIN -> "[" + roomId + "] " + actor + " joined.";
            case LEAVE -> "[" + roomId + "] " + actor + " left.";
            case ROOM_CREATED -> "[" + roomId + "] Room created by " + actor + ".";
            case ROOM_CLOSED -> "[" + roomId + "] Room closed.";
            case GAME_STARTED -> "[" + roomId + "] Game starting! Join now!";
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
