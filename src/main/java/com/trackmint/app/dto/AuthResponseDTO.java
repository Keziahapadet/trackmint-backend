package com.trackmint.app.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String token;
    private String fullName;
    private String email;

    public static AuthResponseDTO ok(String token, String fullName,
                                     String email, String message) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setSuccess(true);
        response.setMessage(message);
        response.setToken(token);
        response.setFullName(fullName);
        response.setEmail(email);
        return response;
    }
}