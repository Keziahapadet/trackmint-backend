package com.trackmint.app.service;

import com.trackmint.app.dto.*;
import com.trackmint.app.entity.*;
import com.trackmint.app.exception.AppException;
import com.trackmint.app.repository.*;
import com.trackmint.app.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RevokedTokenRepository revokedTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
    }

    // Generate Refresh Token
    private String generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    // Register
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw AppException.emailAlreadyExists();
        }
        if (userRepository.findByFullName(dto.getFullName()).isPresent()) {
            throw AppException.nameAlreadyTaken();
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw AppException.passwordMismatch();
        }
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(dto.getEmail());
        String refreshToken = generateRefreshToken(user);

        return AuthResponseDTO.ok(accessToken, refreshToken,
                user.getFullName(), user.getEmail(),
                "Account created successfully");
    }

    // Login
    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(), dto.getPassword()));
        } catch (BadCredentialsException e) {
            throw AppException.invalidCredentials();
        }
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(AppException::userNotFound);

        String accessToken = jwtUtil.generateToken(dto.getEmail());
        String refreshToken = generateRefreshToken(user);

        return AuthResponseDTO.ok(accessToken, refreshToken,
                user.getFullName(), user.getEmail(),
                "Login successful");
    }

    // Refresh Token
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(dto.getRefreshToken())
                .orElseThrow(AppException::invalidRefreshToken);

        if (refreshToken.isRevoked()) {
            throw AppException.tokenRevoked();
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw AppException.tokenExpired();
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        return AuthResponseDTO.ok(newAccessToken, dto.getRefreshToken(),
                user.getFullName(), user.getEmail(),
                "Token refreshed successfully");
    }

    // Logout
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(accessToken);
        revokedToken.setRevokedAt(LocalDateTime.now());
        revokedTokenRepository.save(revokedToken);

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    // Forgot Password
    @Transactional
    public String forgotPassword(ForgotPasswordRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(AppException::userNotFound);

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
        return "Password reset link sent to your email";
    }

    // Reset Password
    @Transactional
    public String resetPassword(ResetPasswordRequestDTO dto) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(dto.getToken())
                .orElseThrow(AppException::invalidToken);

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw AppException.tokenExpired();
        }

        if (resetToken.isUsed()) {
            throw AppException.tokenAlreadyUsed();
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw AppException.passwordMismatch();
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return "Password reset successfully";
    }
}