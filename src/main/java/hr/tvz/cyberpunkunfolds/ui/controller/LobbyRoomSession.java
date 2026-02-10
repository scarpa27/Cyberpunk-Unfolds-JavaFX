package hr.tvz.cyberpunkunfolds.ui.controller;

import hr.tvz.cyberpunkunfolds.rmi.protocol.LobbyService;
import hr.tvz.cyberpunkunfolds.ui.service.LobbyChatService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class LobbyRoomSession {
    private final LobbyService lobby;
    private final LobbyChatService chatService;
    private final String myName;
    private String currentRoomId;

    LobbyRoomSession(LobbyService lobby, LobbyChatService chatService, String myName) {
        this.lobby = lobby;
        this.chatService = chatService;
        this.myName = myName;
    }

    String currentRoomId() {
        return currentRoomId;
    }

    boolean switchRoom(String newRoomId) {
        if (newRoomId == null || newRoomId.isBlank()) return false;

        if (!chatService.hasListener()) {
            chatService.append("Chat listener not initialized.");
            return false;
        }

        if (newRoomId.equals(currentRoomId)) {
            chatService.append("Already in room: " + newRoomId);
            return false;
        }

        String previousRoomId = currentRoomId;

        try {
            boolean ok = lobby.joinRoom(newRoomId, myName);
            if (!ok)
                return false;
            chatService.addListener(newRoomId);

            if (previousRoomId != null) {
                lobby.leaveRoom(previousRoomId, myName);
                chatService.removeListener(previousRoomId);
            }

            currentRoomId = newRoomId;
            chatService.append("Switched to room: " + currentRoomId);
            return true;
        }
        catch (Exception e) {
            chatService.append("Switch room failed: " + e.getMessage());
            return false;
        }
    }

    void cleanup() {
        try {
            chatService.shutdown();
            if (currentRoomId != null) {
                lobby.leaveRoom(currentRoomId, myName);
            }
        }
        catch (Exception _) {
            log.warn("Failed to clean up resources");
        }
    }
}
