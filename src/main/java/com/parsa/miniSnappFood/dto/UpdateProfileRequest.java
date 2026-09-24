package com.parsa.miniSnappFood.dto;

import com.parsa.miniSnappFood.model.BankInfo;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String address;
    private String profileImageBase64;
    private BankInfo bankInfo;
}
