package com.example.be.service;

import com.example.be.dto.UserAddressDTO;

import java.util.List;

public interface AddressService {
    List<UserAddressDTO> getAddressesByUserId(Long userId);

    UserAddressDTO getAddressById(Long id);

    UserAddressDTO createAddress(Long userId, UserAddressDTO addressDTO);

    UserAddressDTO updateAddress(Long id, UserAddressDTO addressDTO);

    void deleteAddress(Long id);

    UserAddressDTO setDefaultAddress(Long userId, Long addressId);
}
