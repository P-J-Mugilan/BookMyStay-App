package com.airline.app.entity;

import com.airline.app.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "passport_or_id")
    private String passportOrId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "phone_otp")
    private String phoneOtp;

    @Builder.Default
    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "remember_me_token")
    private String rememberMeToken;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // Profile preferences & emergency info
    @Column(name = "meal_preference")
    private String mealPreference;

    @Column(name = "seat_preference")
    private String seatPreference;

    @Column(name = "special_assistance")
    private String specialAssistance;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
}
