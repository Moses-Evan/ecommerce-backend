package com.ecommerce.niorra.entity;

import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    
    private String productType;
    
    private boolean productBestSeller;
    
    private String productSubType;

    private String productBrand;

    private String productColor;
    
    private String productColorCode;

    @Column(length = 5000)
    private String productDescription;

    private Integer productDiscount;

    private String productFabricType;

    @ElementCollection
    private List<String> productImages;

    private Double productMrp;

    private String productName;

    private List<String> productOccasion;

    private String productSaleReason;

    private Double productSellingPrice;

    private String productSku;

    private Integer productStock;
    
    private Timestamp createdAt;
    
    private Timestamp updatedAt;
}