package com.server.api.ecommerce.service;

import com.server.api.ecommerce.dto.UserDto;
import com.server.api.ecommerce.dto.reponse.UserResponse;

public interface UserService {
    UserDto registerUser(UserDto userDto);
    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    UserDto getUserById(Long userId);
    UserDto updateUser(Long userId, UserDto userDto);
    String deleteUser(Long userId);
}
