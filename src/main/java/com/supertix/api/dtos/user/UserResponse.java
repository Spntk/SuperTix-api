package com.supertix.api.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String role;
    private String status;
    private String createdAt;
}
