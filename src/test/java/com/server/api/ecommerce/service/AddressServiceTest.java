package com.server.api.ecommerce.service;

import com.server.api.ecommerce.dto.AddressDto;
import com.server.api.ecommerce.entity.Address;
import com.server.api.ecommerce.repository.AddressRepository;
import com.server.api.ecommerce.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AddressDto addressDto = new AddressDto();
        addressDto.setCountry("Country");
        addressDto.setState("State");
        addressDto.setCity("City");
        addressDto.setPincode("12345");
        addressDto.setStreet("Street");
        addressDto.setBuildingName("Building");

        Address address = new Address();
        address.setId(1L);
        address.setCountry("Country");
        address.setState("State");
        address.setCity("City");
        address.setPincode("12345");
        address.setStreet("Street");
        address.setBuildingName("Building");
    }

    @Test
    void testCreateAddress_Success() {}

    @Test
    void testCreateAddress_ThrowsAPIException() {}
}
