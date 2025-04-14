package com.server.api.ecommerce.dto;

import lombok.Data;

@Data
public class JWTAuthRequest {
    private String username;  // email
    private String password;
}
