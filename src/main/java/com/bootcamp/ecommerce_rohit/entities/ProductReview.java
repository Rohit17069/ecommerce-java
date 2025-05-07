package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class ProductReview extends AuditableEntity {
    @EmbeddedId
    private ProductReviewId id;


@ManyToOne(cascade = CascadeType.ALL)
@MapsId("userId")
    @JoinColumn(name = "customer_user_id")
  private   Customer customer;

    @ManyToOne(cascade=CascadeType.ALL)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
   private Product product;

    private String review;
    private Integer rating;

    public ProductReview(Customer customer, Product product, String review, Integer rating) {
        this.customer = customer;
        this.product = product;
        this.review = review;
        this.rating = rating;
    }
}
