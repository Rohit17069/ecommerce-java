package com.bootcamp.ecommerce_rohit.entities;

import com.bootcamp.ecommerce_rohit.enums.Statuses;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderStatus extends AuditableEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;
    private Date transitionDate;
    private String transitionNotesComments;
    @Enumerated(EnumType.STRING)
    private Statuses fromStatus;
    @Enumerated(EnumType.STRING)
    private Statuses toStatus;


    public OrderStatus(Date transitionDate, String transitionNotesComments, Statuses fromStatus, Statuses toStatus, OrderProduct orderProduct) {
        this.transitionDate = transitionDate;
        this.transitionNotesComments = transitionNotesComments;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.orderProduct = orderProduct;
    }
}
