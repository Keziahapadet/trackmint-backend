package com.trackmint.app.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public AppException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    // Auth errors
    public static AppException emailAlreadyExists() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                "Email Already Exists",
                "Email already exists. Please use a different email or login."
        );
    }

    public static AppException nameAlreadyTaken() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                "Name Already Taken",
                "This name is already taken. Please use a different name."
        );
    }

    public static AppException passwordMismatch() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                "Password Mismatch",
                "Passwords do not match. Please try again."
        );
    }

    public static AppException invalidCredentials() {
        return new AppException(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                "Invalid email or password. Please try again."
        );
    }

    // User errors
    public static AppException userNotFound() {
        return new AppException(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                "No account found with this email."
        );
    }

    public static AppException notAuthorized() {
        return new AppException(
                HttpStatus.FORBIDDEN,
                "Not Authorized",
                "You are not authorized to perform this action."
        );
    }

    // Token errors
    public static AppException invalidToken() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                "Invalid Token",
                "Invalid or expired token. Please request a new one."
        );
    }

    public static AppException tokenExpired() {
        return new AppException(
                HttpStatus.UNAUTHORIZED,
                "Token Expired",
                "Your session has expired. Please login again."
        );
    }

    public static AppException tokenAlreadyUsed() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                "Token Already Used",
                "This reset link has already been used. Please request a new one."
        );
    }

    public static AppException tokenRevoked() {
        return new AppException(
                HttpStatus.UNAUTHORIZED,
                "Token Revoked",
                "Your session has been revoked. Please login again."
        );
    }

    public static AppException invalidRefreshToken() {
        return new AppException(
                HttpStatus.UNAUTHORIZED,
                "Invalid Refresh Token",
                "Invalid refresh token. Please login again."
        );
    }

    // Transaction errors
    public static AppException transactionNotFound() {
        return new AppException(
                HttpStatus.NOT_FOUND,
                "Transaction Not Found",
                "Transaction not found."
        );
    }

    // Email errors
    public static AppException emailSendFailed() {
        return new AppException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Email Send Failed",
                "Failed to send email. Please try again later."
        );
    }
}