package com.supertix.api.dtos.user;

import com.supertix.api.enums.UserRole;
import com.supertix.api.enums.UserStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;
    private String address;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Status is required")
    private UserStatus status;
}
