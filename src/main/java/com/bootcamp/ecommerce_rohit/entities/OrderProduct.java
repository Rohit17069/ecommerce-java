package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderProduct extends AuditableEntity  {
@Id
@GeneratedValue
private String  id;

@ManyToOne
@JoinColumn(name = "order_id")
private OrderTable orderTable;

private Integer quantity;
private double price;
@ManyToOne
@JoinColumn(name = "product_variation_id")
private ProductVariation productVariation;

@OneToMany(mappedBy = "orderProduct")
private List<OrderStatus>orderStatuses;

    public OrderProduct(OrderTable orderTable, Integer quantity, double price, ProductVariation productVariation, List<OrderStatus> orderStatuses) {
        this.orderTable = orderTable;
        this.quantity = quantity;
        this.price = price;
        this.productVariation = productVariation;
        this.orderStatuses = orderStatuses;
    }
}
