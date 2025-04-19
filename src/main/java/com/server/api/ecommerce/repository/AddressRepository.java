package com.server.api.ecommerce.repository;

import com.server.api.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findByCountry(String country);
    Address findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(String country, String state, String city, String pincode, String street, String buildingName);
}
