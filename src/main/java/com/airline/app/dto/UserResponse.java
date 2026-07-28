package com.airline.app.dto;

import com.airline.app.enums.UserRole;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String passportOrId;
    private UserRole role;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean mfaEnabled;
    private String sessionToken;
    private String rememberMeToken;
    private Boolean active;
    private String mfaSecret;
    
    // Profile preferences
    private String mealPreference;
    private String seatPreference;
    private String specialAssistance;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
