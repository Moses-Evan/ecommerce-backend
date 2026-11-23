package com.ecommerce.niorra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String name;
    private String contactNumber;
    private String address;

    private String city;

    private String state;

    private String zip;

    private String country;

    private String label;
}