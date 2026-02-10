package hr.tvz.cyberpunkunfolds.net.protocol;

import hr.tvz.cyberpunkunfolds.engine.command.*;
import hr.tvz.cyberpunkunfolds.model.world.NodeId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class DtoMapper {
    private DtoMapper() { }

    public static CommandDto toDto(GameCommand command, String roomId) {
        long now = Instant.now().toEpochMilli();
        return switch (command) {
            case MoveCommand(UUID playerId, NodeId v) ->
                    new CommandDto("MOVE", playerId.toString(), v.name(), now, roomId);

            case InspectCommand(UUID playerId) -> new CommandDto("INSPECT", playerId.toString(), "", now, roomId);

            case SolvePuzzleCommand(UUID playerId, String answer) ->
                    new CommandDto("SOLVE", playerId.toString(), answer, now, roomId);

            case EndTurnCommand(UUID playerId) -> new CommandDto("END_TURN", playerId.toString(), "", now, roomId);

            case null -> new CommandDto("UNKNOWN", "?", "?", now, roomId);
        };
    }

    public static GameCommand fromDto(CommandDto dto) {
        Objects.requireNonNull(dto, "dto");
        UUID pid = parsePlayerId(dto.playerId());
        String type = dto.type();

        return switch (type) {
            case "MOVE" -> new MoveCommand(pid, parseNodeId(dto.payload()));
            case "INSPECT" -> new InspectCommand(pid);
            case "SOLVE" -> new SolvePuzzleCommand(pid, dto.payload());
            case "END_TURN" -> new EndTurnCommand(pid);
            default -> throw new IllegalArgumentException("Unknown command type: " + type);
        };
    }

    private static UUID parsePlayerId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing player id.");
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid player id: " + raw, e);
        }
    }

    private static NodeId parseNodeId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing node id.");
        }
        try {
            return NodeId.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid node id: " + raw, e);
        }
    }
}
