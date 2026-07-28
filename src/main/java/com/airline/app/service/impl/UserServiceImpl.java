package com.airline.app.service.impl;

import com.airline.app.dto.*;
import com.airline.app.entity.User;
import com.airline.app.exception.BusinessException;
import com.airline.app.exception.ResourceNotFoundException;
import com.airline.app.repository.UserRepository;
import com.airline.app.service.UserService;
import com.airline.app.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Random random = new Random();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse register(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("User with this email already exists");
        }

        String emailOtp = String.format("%06d", random.nextInt(1000000));
        String phoneOtp = String.format("%06d", random.nextInt(1000000));

        System.out.println("[MOCK NOTIFICATION] OTP sent to email " + request.getEmail() + ": " + emailOtp);
        System.out.println("[MOCK NOTIFICATION] OTP sent to phone " + request.getPhone() + ": " + phoneOtp);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .passportOrId(request.getPassportOrId())
                .password(PasswordHasher.hash(request.getPassword()))
                .role(request.getRole())
                .emailOtp(emailOtp)
                .phoneOtp(phoneOtp)
                .emailVerified(false)
                .phoneVerified(false)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean verifiedAny = false;

        if (request.getEmailOtp() != null && request.getEmailOtp().equals(user.getEmailOtp())) {
            user.setEmailVerified(true);
            user.setEmailOtp(null);
            verifiedAny = true;
        }

        if (request.getPhoneOtp() != null && request.getPhoneOtp().equals(user.getPhoneOtp())) {
            user.setPhoneVerified(true);
            user.setPhoneOtp(null);
            verifiedAny = true;
        }

        if (!verifiedAny) {
            throw new BusinessException("Invalid OTP code(s)");
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getActive()) {
            throw new BusinessException("User account is deactivated");
        }

        if (!PasswordHasher.check(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        if (user.getMfaEnabled()) {
            if (request.getMfaCode() == null || !request.getMfaCode().equals("123456")) { // Mock MFA check
                throw new BusinessException("MFA code verification failed");
            }
        }

        // Generate session token
        user.setSessionToken(UUID.randomUUID().toString());

        if (Boolean.TRUE.equals(request.getRememberMe())) {
            user.setRememberMeToken(UUID.randomUUID().toString());
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public void logout(String sessionToken) {
        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        user.setSessionToken(null);
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String resetOtp = String.format("%06d", random.nextInt(1000000));
        user.setEmailOtp(resetOtp); // Re-use email OTP field for password reset code
        userRepository.save(user);

        System.out.println("[MOCK NOTIFICATION] Password reset OTP sent to email " + email + ": " + resetOtp);
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailOtp() == null || !user.getEmailOtp().equals(request.getOtp())) {
            throw new BusinessException("Invalid or expired OTP");
        }

        user.setPassword(PasswordHasher.hash(request.getNewPassword()));
        user.setEmailOtp(null);
        userRepository.save(user);
    }

    @Override
    public UserResponse enableMfa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // In a real system, we'd generate a secret for Google Authenticator.
        // We will mock the secret key generation.
        user.setMfaSecret("MOCK_SECRET_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse verifyMfa(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("123456".equals(code)) { // Mock verification code
            user.setMfaEnabled(true);
        } else {
            throw new BusinessException("Invalid verification code");
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getProfile(String sessionToken) {
        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateProfile(String sessionToken, UserResponse profileData) {
        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));

        user.setName(profileData.getName());
        user.setPhone(profileData.getPhone());
        user.setDateOfBirth(profileData.getDateOfBirth());
        user.setPassportOrId(profileData.getPassportOrId());
        
        // Preferences
        user.setMealPreference(profileData.getMealPreference());
        user.setSeatPreference(profileData.getSeatPreference());
        user.setSpecialAssistance(profileData.getSpecialAssistance());
        user.setEmergencyContactName(profileData.getEmergencyContactName());
        user.setEmergencyContactPhone(profileData.getEmergencyContactPhone());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        user.setSessionToken(null);
        user.setRememberMeToken(null);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .passportOrId(user.getPassportOrId())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .mfaEnabled(user.getMfaEnabled())
                .sessionToken(user.getSessionToken())
                .rememberMeToken(user.getRememberMeToken())
                .active(user.getActive())
                .mfaSecret(user.getMfaSecret())
                .mealPreference(user.getMealPreference())
                .seatPreference(user.getSeatPreference())
                .specialAssistance(user.getSpecialAssistance())
                .emergencyContactName(user.getEmergencyContactName())
                .emergencyContactPhone(user.getEmergencyContactPhone())
                .build();
    }
}
