package com.parsa.miniSnappFood.dto;

import com.parsa.miniSnappFood.model.BankInfo;
import com.parsa.miniSnappFood.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private Role role;
    private String address;
    private String profileImageBase64;
    private BankInfo bankInfo;
}