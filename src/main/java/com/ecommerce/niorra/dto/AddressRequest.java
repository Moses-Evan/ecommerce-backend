package com.ecommerce.niorra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String contactNumber;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String zip;

    @NotBlank
    private String country;

    @NotBlank
    private String label;
}