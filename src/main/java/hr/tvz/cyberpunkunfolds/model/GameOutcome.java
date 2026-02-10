package hr.tvz.cyberpunkunfolds.model;

public enum GameOutcome {
    WIN_DATACORE_SOLVED("Victory! DATACORE accessed."),
    LOSE_ALARM_MAX("Defeat! Security detected the intrusion.");

    private final String message;

    GameOutcome(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
