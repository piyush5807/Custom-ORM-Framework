package com.hibernate.reflections.entities;

import com.hibernate.reflections.annotations.Column;
import com.hibernate.reflections.annotations.Id;

public class UserOrder {

    @Id
    @Column("id")
    private Long orderId;

    @Column
    private String createdBy;

    @Column
    private Double amount;

    @Column("cancel")
    private Boolean isCancellable;

    @Column("items")
    private Integer totalItems;

    public UserOrder() {
    }

    public UserOrder(Long orderId, String createdBy, Double amount, Boolean isCancellable, Integer totalItems) {
        this.orderId = orderId;
        this.createdBy = createdBy;
        this.amount = amount;
        this.isCancellable = isCancellable;
        this.totalItems = totalItems;
    }

    @Override
    public String toString() {
        return "UserOrder{" +
                "orderId=" + orderId +
                ", createdBy='" + createdBy + '\'' +
                ", amount=" + amount +
                ", isCancellable=" + isCancellable +
                ", totalItems=" + totalItems +
                '}';
    }
}
