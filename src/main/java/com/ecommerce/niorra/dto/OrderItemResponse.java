package com.ecommerce.niorra.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {

    private Long productId;

    private String productName;

    private String productImage;

    private Double price;

    private Integer quantity;

    private Double subtotal;

}
