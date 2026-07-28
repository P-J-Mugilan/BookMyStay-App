package com.airline.app.service;

import com.airline.app.dto.*;

public interface UserService {
    UserResponse register(UserRegistrationRequest request);
    UserResponse verifyOtp(OtpVerificationRequest request);
    UserResponse login(UserLoginRequest request);
    void logout(String sessionToken);
    void requestPasswordReset(String email);
    void resetPassword(PasswordResetRequest request);
    UserResponse enableMfa(Long userId);
    UserResponse verifyMfa(Long userId, String code);
    UserResponse getProfile(String sessionToken);
    UserResponse updateProfile(String sessionToken, UserResponse profileData);
    void deactivateUser(Long userId);
}
