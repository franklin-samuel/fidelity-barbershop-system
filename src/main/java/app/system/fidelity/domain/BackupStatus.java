package app.system.fidelity.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BackupStatus(
        State state,
        String trigger,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String fileName,
        Long sizeBytes,
        String driveFileId,
        String error
) {

    public enum State {
        IDLE,
        RUNNING,
        SUCCESS,
        FAILED
    }

    public static BackupStatus idle() {
        return new BackupStatus(State.IDLE, null, null, null, null, null, null, null);
    }

    public static BackupStatus running(final String trigger, final LocalDateTime startedAt) {
        return new BackupStatus(State.RUNNING, trigger, startedAt, null, null, null, null, null);
    }

    public BackupStatus succeeded(final LocalDateTime finishedAt,
                                  final String fileName,
                                  final long sizeBytes,
                                  final String driveFileId) {
        return new BackupStatus(State.SUCCESS, trigger, startedAt, finishedAt, fileName, sizeBytes, driveFileId, null);
    }

    public BackupStatus failed(final LocalDateTime finishedAt, final String fileName, final String error) {
        return new BackupStatus(State.FAILED, trigger, startedAt, finishedAt, fileName, null, null, error);
    }
}
