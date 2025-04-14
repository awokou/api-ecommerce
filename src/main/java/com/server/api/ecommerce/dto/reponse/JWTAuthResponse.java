package com.server.api.ecommerce.dto.reponse;

import com.server.api.ecommerce.dto.UserDto;
import lombok.Data;

@Data
public class JWTAuthResponse {
    private String token;
    private UserDto user;
}
