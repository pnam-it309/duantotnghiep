package com.example.be.service.impl;

import com.example.be.dto.UserAddressDTO;
import com.example.be.entity.User;
import com.example.be.entity.UserAddress;
import com.example.be.repository.AddressRepository;
import com.example.be.repository.UserRepository;
import com.example.be.service.AddressService;
import com.example.be.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Override
    public List<UserAddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(dtoMapper::toUserAddressDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressDTO getAddressById(Long id) {
        UserAddress address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return dtoMapper.toUserAddressDTO(address);
    }

    @Override
    @Transactional
    public UserAddressDTO createAddress(Long userId, UserAddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAddress address = dtoMapper.toUserAddressEntity(addressDTO);
        address.setUser(user);

        // If this is the first address, make it default
        List<UserAddress> existingAddresses = addressRepository.findByUserId(userId);
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            // Unset other defaults
            unsetDefaultAddress(userId);
        }

        UserAddress savedAddress = addressRepository.save(address);
        return dtoMapper.toUserAddressDTO(savedAddress);
    }

    @Override
    @Transactional
    public UserAddressDTO updateAddress(Long id, UserAddressDTO addressDTO) {
        UserAddress address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setReceiverName(addressDTO.getReceiverName());
        address.setReceiverPhone(addressDTO.getReceiverPhone());
        address.setProvinceId(addressDTO.getProvinceId());
        address.setProvinceName(addressDTO.getProvinceName());
        address.setDistrictId(addressDTO.getDistrictId());
        address.setDistrictName(addressDTO.getDistrictName());
        address.setWardCode(addressDTO.getWardCode());
        address.setWardName(addressDTO.getWardName());
        address.setSpecificAddress(addressDTO.getSpecificAddress());

        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            unsetDefaultAddress(address.getUser().getId());
            address.setIsDefault(true);
        }

        UserAddress savedAddress = addressRepository.save(address);
        return dtoMapper.toUserAddressDTO(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserAddressDTO setDefaultAddress(Long userId, Long addressId) {
        unsetDefaultAddress(userId);
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        address.setIsDefault(true);
        return dtoMapper.toUserAddressDTO(addressRepository.save(address));
    }

    private void unsetDefaultAddress(Long userId) {
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        for (UserAddress addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}
