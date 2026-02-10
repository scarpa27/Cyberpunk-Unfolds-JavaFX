package hr.tvz.cyberpunkunfolds.model;

import java.util.Objects;
import java.util.Optional;

public record Result<T>(T value, String error) {
    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> ok() {
        return new Result<>(null, null);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<>(null, Objects.requireNonNull(error, "error"));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public Optional<String> errorMessage() {
        return Optional.ofNullable(error);
    }
}
