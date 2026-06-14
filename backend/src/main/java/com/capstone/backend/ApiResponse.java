package com.capstone.backend;

public record ApiResponse(String status, String message) {

    public static ApiResponse success(String message) {
        return new ApiResponse("success", message);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse("error", message);
    }
}
