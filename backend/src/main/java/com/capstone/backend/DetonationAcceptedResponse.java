package com.capstone.backend;

public record DetonationAcceptedResponse(String status, String message, Long runId) {

    public static DetonationAcceptedResponse of(Long runId, String message) {
        return new DetonationAcceptedResponse("success", message, runId);
    }
}
