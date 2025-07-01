package com.lucasdavi.quizz.enums;

import lombok.Getter;

@Getter
public enum SessionStatus {
    IN_PROGRESS(" IN_PROGRESS", "Em andamento"),
    COMPLETED("COMPLETED", "Concluído"),
    INTERRUPTED("INTERRUPTED", "Interrompido");

    private final String value;
    private final String description;

    SessionStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == INTERRUPTED;
    }

    public boolean isActive() {
        return this == IN_PROGRESS;
    }
}