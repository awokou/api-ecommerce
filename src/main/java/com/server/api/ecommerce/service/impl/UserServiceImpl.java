package com.server.api.ecommerce.service.impl;

import com.server.api.ecommerce.config.AppConstants;
import com.server.api.ecommerce.dto.AddressDto;
import com.server.api.ecommerce.dto.CartDto;
import com.server.api.ecommerce.dto.ProductDto;
import com.server.api.ecommerce.dto.UserDto;
import com.server.api.ecommerce.dto.reponse.UserResponse;
import com.server.api.ecommerce.entity.*;
import com.server.api.ecommerce.exceptions.APIException;
import com.server.api.ecommerce.exceptions.ErrorMessages;
import com.server.api.ecommerce.exceptions.ResourceNotFoundException;
import com.server.api.ecommerce.repository.AddressRepository;
import com.server.api.ecommerce.repository.RoleRepository;
import com.server.api.ecommerce.repository.UserRepository;
import com.server.api.ecommerce.service.CartService;
import com.server.api.ecommerce.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, AddressRepository addressRepository, CartService cartService, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.cartService = cartService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto registerUser(UserDto userDto) {
        try {
            User user = modelMapper.map(userDto, User.class);
            Cart cart = new Cart();
            user.setCart(cart);

            Role role = roleRepository.findById(AppConstants.USER_ID).get();
            user.getRoles().add(role);

            String country = userDto.getAddress().getCountry();
            String state = userDto.getAddress().getState();
            String city = userDto.getAddress().getCity();
            String pincode = userDto.getAddress().getPincode();
            String street = userDto.getAddress().getStreet();
            String buildingName = userDto.getAddress().getBuildingName();

            Address address = addressRepository.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);

            if (address == null) {
                address = new Address(country, state, city, pincode, street, buildingName);
                address = addressRepository.save(address);
            }
            user.setAddresses(List.of(address));
            User registeredUser = userRepository.save(user);
            cart.setUser(registeredUser);
            userDto = modelMapper.map(registeredUser, UserDto.class);
            userDto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDto.class));

            return userDto;
        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDto.getEmail());
        }
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<User> pageUsers = userRepository.findAll(pageDetails);
        List<User> users = pageUsers.getContent();
        if (users.isEmpty()) {
            throw new APIException("No User exists !!!");
        }
        List<UserDto> userDTOs = users.stream().map(user -> {
            UserDto dto = modelMapper.map(user, UserDto.class);
            if (!user.getAddresses().isEmpty()){
                dto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDto.class));
            }

            CartDto cart = modelMapper.map(user.getCart(), CartDto.class);
            List<ProductDto> products = user.getCart().getCartItems().stream()
                    .map(item -> modelMapper.map(item.getProduct(), ProductDto.class)).collect(Collectors.toList());
            dto.setCart(cart);
            dto.getCart().setProducts(products);

            return dto;

        }).collect(Collectors.toList());

        UserResponse userResponse = new UserResponse();
        userResponse.setContent(userDTOs);
        userResponse.setPageNumber(pageUsers.getNumber());
        userResponse.setPageSize(pageUsers.getSize());
        userResponse.setTotalElements(pageUsers.getTotalElements());
        userResponse.setTotalPages(pageUsers.getTotalPages());
        userResponse.setLastPage(pageUsers.isLast());

        return userResponse;
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER, ErrorMessages.USER_ID, userId));

        UserDto userDto = modelMapper.map(user, UserDto.class);
        userDto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDto.class));

        CartDto cart = modelMapper.map(user.getCart(), CartDto.class);
        List<ProductDto> products = user.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDto.class)).collect(Collectors.toList());

        userDto.setCart(cart);
        userDto.getCart().setProducts(products);

        return userDto;
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER, ErrorMessages.USER_ID, userId));

        String encodedPass = passwordEncoder.encode(userDto.getPassword());

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMobileNumber(userDto.getMobileNumber());
        user.setEmail(userDto.getEmail());
        user.setPassword(encodedPass);

        if (userDto.getAddress() != null) {
            String country = userDto.getAddress().getCountry();
            String state = userDto.getAddress().getState();
            String city = userDto.getAddress().getCity();
            String pincode = userDto.getAddress().getPincode();
            String street = userDto.getAddress().getStreet();
            String buildingName = userDto.getAddress().getBuildingName();
            Address address = addressRepository.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);
            if (address == null) {
                address = new Address(country, state, city, pincode, street, buildingName);
                address = addressRepository.save(address);
                user.setAddresses(List.of(address));
            }
        }
        userDto = modelMapper.map(user, UserDto.class);
        userDto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDto.class));
        CartDto cart = modelMapper.map(user.getCart(), CartDto.class);
        List<ProductDto> products = user.getCart().getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDto.class)).collect(Collectors.toList());

        userDto.setCart(cart);
        userDto.getCart().setProducts(products);

        return userDto;
    }

    @Override
    public String deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER, ErrorMessages.USER_ID, userId));

        List<CartItem> cartItems = user.getCart().getCartItems();
        Long cartId = user.getCart().getCartId();
        cartItems.forEach(item -> {
            Long productId = item.getProduct().getProductId();
            cartService.deleteProductFromCart(cartId, productId);
        });
        userRepository.delete(user);
        return "User with userId " + userId + " deleted successfully!!!";
    }
}
