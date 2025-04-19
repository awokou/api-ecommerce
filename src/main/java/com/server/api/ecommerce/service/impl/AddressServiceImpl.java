package com.server.api.ecommerce.service.impl;

import com.server.api.ecommerce.dto.AddressDto;
import com.server.api.ecommerce.entity.Address;
import com.server.api.ecommerce.entity.User;
import com.server.api.ecommerce.exceptions.APIException;
import com.server.api.ecommerce.exceptions.ResourceNotFoundException;
import com.server.api.ecommerce.repository.AddressRepository;
import com.server.api.ecommerce.repository.UserRepository;
import com.server.api.ecommerce.service.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AddressDto createAddress(Address address) {
        Address savedAddress = addressRepository.findByCountry(address.getCountry());
        if (savedAddress != null) {
            throw new APIException("Address already exists with addressId: " + address.getCountry());
        }
        savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDto.class);
    }

    @Override
    public List<AddressDto> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(address -> modelMapper.map(address, AddressDto.class)).toList();
    }

    @Override
    public AddressDto getAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", id));

        return modelMapper.map(address, AddressDto.class);
    }

    @Override
    public AddressDto updateAddress(Long addressId, Address address) {
         addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        address.setId(addressId);
        return modelMapper.map(addressRepository.save(address), AddressDto.class);
    }

    @Override
    public String deleteAddress(Long id) {
        Address addressFromDB = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", id));
        List<User> users = userRepository.findByAddress(id);
        users.forEach(user -> {
            user.getAddresses().remove(addressFromDB);
            userRepository.save(user);
        });

        addressRepository.deleteById(id);

        return "Address deleted succesfully with addressId: " + id;
    }
}
