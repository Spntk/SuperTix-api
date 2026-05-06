package com.supertix.api.dtos.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdatePasswordRequest {

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "New password is required")
    private String getNewPassword;
}
