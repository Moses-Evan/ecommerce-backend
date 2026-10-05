package com.ecommerce.niorra.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.niorra.enums.OrderStatus;
import com.ecommerce.niorra.enums.PaymentMethod;
import com.ecommerce.niorra.enums.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String orderNumber;

	// Customer Details
	private String customerName;

	private String customerEmail;

	private String customerPhone;

	// Shipping Address
	@Column(length = 500)
	private String shippingAddress;

	private String shippingCity;

	private String shippingState;

	private String shippingZip;

	private String shippingCountry;

	// Amount Details
	private Double subtotal;

	private Double discount;

	private Double shippingCharge;

	private Double tax;

	private Double totalAmount;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "varchar(32)")
	private PaymentMethod paymentMethod;

	private String paypalOrderId;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderItem> orderItems = new ArrayList<>();

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();

		if (orderStatus == null)
			orderStatus = OrderStatus.PENDING;

		if (paymentStatus == null)
			paymentStatus = PaymentStatus.PENDING;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

}
