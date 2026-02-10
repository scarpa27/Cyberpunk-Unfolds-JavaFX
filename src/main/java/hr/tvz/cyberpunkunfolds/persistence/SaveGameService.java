package hr.tvz.cyberpunkunfolds.persistence;

import hr.tvz.cyberpunkunfolds.config.Config;
import hr.tvz.cyberpunkunfolds.model.GameState;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class SaveGameService {
    private final Config cfg = Config.load();

    public Path save(GameState state, String fileName) {
        try {
            Files.createDirectories(cfg.saveDir());
            Path p = cfg.saveDir().resolve(fileName);
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(p))) {
                out.writeObject(state);
            }
            log.info("Saved game to {}", p);
            return p;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save game", e);
        }
    }

    public GameState load(String fileName) {
        try {
            Path p = cfg.saveDir().resolve(fileName);
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(p))) {
                Object obj = in.readObject();
                return (GameState) obj;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load game", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class not found while loading game", e);
        }
    }
}
