package com.ecommerce.niorra.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
	private String action;
	private String productBrand;
	private String productColor;
	private String productColorCode;
	private String productDescription;
	private Integer productDiscount;
	private String productFabricType;
	private List<String> productImages;
	private Double productMrp;
	private String productName;
	private List<String> productOccasion;
	private String productSaleReason;
	private Double productSellingPrice;
	private String productSku;
	private Integer productStock;
	private String productType;
	private String productSubType;
	private String productCategory;
	private List<String> productBadges;
}