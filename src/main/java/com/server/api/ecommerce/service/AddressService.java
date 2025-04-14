package com.server.api.ecommerce.service;

import java.util.List;
import com.server.api.ecommerce.dto.AddressDto;
import com.server.api.ecommerce.entity.Address;

public interface AddressService {
    AddressDto createAddress(AddressDto addressDto);
    List<AddressDto> getAddresses();
    AddressDto getAddress(Long id);
    AddressDto updateAddress(Long id, Address address);
    String deleteAddress(Long id);
}
