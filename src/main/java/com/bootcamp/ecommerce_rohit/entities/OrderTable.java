package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderTable extends AuditableEntity {
    @Id
    @GeneratedValue
    private String id;
    @ManyToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;
    private Double amountPaid;
    @Temporal(TemporalType.DATE)
    private Date dateCreated;
    private String paymentMethod;
    private String customerAddressCity;
    private String customerAddressState;
    private String customerAddressCountry;
    private String customerAddressLine;
    private String customerAddressZipCode;
    private String customerAddressLabel;

    @OneToMany(mappedBy = "orderTable")
    private List<OrderProduct> orderProducts=new ArrayList<>();

    public OrderTable(Customer customer, Double amountPaid, Date dateCreated, String paymentMethod, String customerAddressCity, String customerAddressState, String customerAddressCountry, String customerAddressLine, String customerAddressLabel, String customerAddressZipCode, List<OrderProduct> orderProducts) {
        this.customer = customer;
        this.amountPaid = amountPaid;
        this.dateCreated = dateCreated;
        this.paymentMethod = paymentMethod;
        this.customerAddressCity = customerAddressCity;
        this.customerAddressState = customerAddressState;
        this.customerAddressCountry = customerAddressCountry;
        this.customerAddressLine = customerAddressLine;
        this.customerAddressLabel = customerAddressLabel;
        this.customerAddressZipCode = customerAddressZipCode;
        this.orderProducts = orderProducts;
    }
}
