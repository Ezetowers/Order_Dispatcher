package common;

import common.OrderState;
import common.Product;

import java.io.Serializable;
import java.util.UUID;


public class Order implements Serializable {
    public Order(Product productType, Long amount) {
        state_ = OrderState.TO_BE_PROCESSED;
        clientName_ = "JON SKEET";
        productType_ = productType;
        amount_ = amount;
    }

    public UUID id() {
        return uuid_;
    }

    public OrderState state() {
        return state_;
    }

    public void state(OrderState state) {
        state_ = state;
    }

    public String stringID() {
        return uuid_.toString();
    }

    public Product productType() {
        return productType_;
    }

    public Long amount() {
        return amount_;
    }

    public String toString() {
        String aux = "";
        // Reduce the size of the UUID to better log size comprehension
        aux += "Order ID: " + uuid_.toString().substring(0,6) + " - ";
        aux += "State: " + state_.toString() + " - ";
        aux += "Product Type: " + productType_.toString() + " - ";
        aux += "Amount: " + amount_;
        return aux;
    }

    public String toStringFull() {
        String aux = "";
        // Reduce the size of the UUID to better log size comprehension
        aux += "Order ID: " + uuid_.toString() + " - ";
        aux += "State: " + state_.toString() + " - ";
        aux += "Product Type: " + productType_.toString() + " - ";
        aux += "Amount: " + amount_;
        return aux;
    }

    private OrderState state_;
    private String clientName_;
    private final UUID uuid_ = UUID.randomUUID();
    // FIXME: Create a enum or something like that to represent this
    private Product productType_;
    private Long amount_;
    public static final long serialVersionUID = 123L;
}