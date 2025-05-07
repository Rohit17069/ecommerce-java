package com.bootcamp.ecommerce_rohit.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart extends AuditableEntity {
    @EmbeddedId
    private CartValueId id;

@ManyToOne
@MapsId("userId")
    @JoinColumn(name = "customer_user_id")
private Customer customer;
private Integer quantity;
private Boolean isWishListItem;
@ManyToOne
@MapsId("productVariationId")
    @JoinColumn(name = "product_variation_id")
   private ProductVariation productVariation;

    public Cart(Customer customer, Integer quantity, Boolean isWishListItem, ProductVariation productVariation) {
        this.customer = customer;
        this.quantity = quantity;
        this.isWishListItem = isWishListItem;
        this.productVariation = productVariation;
    }
}
