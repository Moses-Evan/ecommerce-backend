package com.ecommerce.niorra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.niorra.dto.AddressRequest;
import com.ecommerce.niorra.dto.AddressResponse;
import com.ecommerce.niorra.entity.Address;
import com.ecommerce.niorra.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressResponse saveAddress(AddressRequest request, String userEmail) {
        Address savedAddress = addressRepository.save(Address.builder()
                .name(request.getName())
                .contactNumber(request.getContactNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zip(request.getZip())
                .country(request.getCountry())
                .userEmail(userEmail)
                .label(request.getLabel())
                .build());

        return toResponse(savedAddress);
    }

    public List<AddressResponse> getAddresses(String userEmail) {
        return addressRepository.findAllByUserEmailOrderByIdDesc(userEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getId(), address.getName(), address.getContactNumber(),
                address.getAddress(), address.getCity(), address.getState(), address.getZip(), address.getCountry(), address.getLabel());
    }
}