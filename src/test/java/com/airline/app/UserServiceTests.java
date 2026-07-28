package com.airline.app;

import com.airline.app.dto.*;
import com.airline.app.entity.User;
import com.airline.app.enums.UserRole;
import com.airline.app.exception.BusinessException;
import com.airline.app.repository.UserRepository;
import com.airline.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testUserRegistrationAndOtpVerification() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .passportOrId("A1234567")
                .password("securePassword123")
                .role(UserRole.PASSENGER)
                .build();

        UserResponse response = userService.register(request);

        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertFalse(response.getEmailVerified());

        // Verify record in database
        Optional<User> dbUserOpt = userRepository.findByEmail("john.doe@example.com");
        assertTrue(dbUserOpt.isPresent());
        User dbUser = dbUserOpt.get();
        assertNotEquals("securePassword123", dbUser.getPassword()); // password encryption check
        assertNotNull(dbUser.getEmailOtp());

        // OTP Verification
        OtpVerificationRequest verifyRequest = OtpVerificationRequest.builder()
                .email("john.doe@example.com")
                .emailOtp(dbUser.getEmailOtp())
                .build();

        UserResponse verifiedResponse = userService.verifyOtp(verifyRequest);
        assertTrue(verifiedResponse.getEmailVerified());
    }

    @Test
    void testUserLogin() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .dateOfBirth(LocalDate.of(1992, 8, 20))
                .passportOrId("B7654321")
                .password("myPassword")
                .role(UserRole.PASSENGER)
                .build();

        userService.register(request);

        // Login
        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .email("jane.smith@example.com")
                .password("myPassword")
                .rememberMe(true)
                .build();

        UserResponse loginResponse = userService.login(loginRequest);
        assertNotNull(loginResponse.getSessionToken());
        assertNotNull(loginResponse.getRememberMeToken());
    }

    @Test
    void testLoginWithInvalidCredentialsThrowsException() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Alice Brown")
                .email("alice.brown@example.com")
                .phone("5551234567")
                .password("alicePass")
                .role(UserRole.PASSENGER)
                .build();

        userService.register(request);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .email("alice.brown@example.com")
                .password("wrongPass")
                .build();

        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
    }
}
