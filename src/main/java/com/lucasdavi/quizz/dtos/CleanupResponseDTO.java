package com.lucasdavi.quizz.dtos;

import java.time.LocalDateTime;

public record CleanupResponseDTO(
        String message,
        Integer deletedSessions,
        Integer finishedSessions,
        String operation,
        LocalDateTime timestamp,
        Object additionalInfo
) {

    public static CleanupResponseDTO forDeletion(String message, int deletedCount) {
        return new CleanupResponseDTO(
                message,
                deletedCount,
                null,
                "DELETE",
                LocalDateTime.now(),
                null
        );
    }

    public static CleanupResponseDTO forFinishing(String message, int finishedCount) {
        return new CleanupResponseDTO(
                message,
                null,
                finishedCount,
                "FINISH",
                LocalDateTime.now(),
                null
        );
    }

    public static CleanupResponseDTO withInfo(String message, int deletedCount, Object info) {
        return new CleanupResponseDTO(
                message,
                deletedCount,
                null,
                "DELETE_WITH_FILTER",
                LocalDateTime.now(),
                info
        );
    }
}