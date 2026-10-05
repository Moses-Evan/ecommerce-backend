package com.ecommerce.niorra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayPalCheckoutResponse {

    private String paypalOrderId;
    private String approvalUrl;
}