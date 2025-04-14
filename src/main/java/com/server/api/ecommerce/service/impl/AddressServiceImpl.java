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
    public AddressDto createAddress(AddressDto addressDto) {
        String country = addressDto.getCountry();
        String state = addressDto.getState();
        String city = addressDto.getCity();
        String pincode = addressDto.getPincode();
        String street = addressDto.getStreet();
        String buildingName = addressDto.getBuildingName();

        Address addressFromDB = addressRepository.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country,
                state, city, pincode, street, buildingName);

        if (addressFromDB != null) {
            throw new APIException("Address already exists with addressId: " + addressFromDB.getId());
        }

        Address address = modelMapper.map(addressDto, Address.class);
        Address savedAddress = addressRepository.save(address);
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
    public AddressDto updateAddress(Long id, Address address) {
        Address addressFromDB = addressRepository.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                address.getCountry(), address.getState(), address.getCity(), address.getPincode(), address.getStreet(),
                address.getBuildingName());

        if (addressFromDB == null) {
            addressFromDB = addressRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", id));

            addressFromDB.setCountry(address.getCountry());
            addressFromDB.setState(address.getState());
            addressFromDB.setCity(address.getCity());
            addressFromDB.setPincode(address.getPincode());
            addressFromDB.setStreet(address.getStreet());
            addressFromDB.setBuildingName(address.getBuildingName());

            Address updatedAddress = addressRepository.save(addressFromDB);

            return modelMapper.map(updatedAddress, AddressDto.class);
        } else {
            List<User> users = userRepository.findByAddress(id);
            final Address a = addressFromDB;

            users.forEach(user -> user.getAddresses().add(a));

            deleteAddress(id);

            return modelMapper.map(addressFromDB, AddressDto.class);
        }
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
