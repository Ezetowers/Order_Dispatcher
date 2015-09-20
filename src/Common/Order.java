package common;

import common.Product;

import java.io.Serializable;
import java.util.UUID;

public class Order implements Serializable {
    public Order(Product productType, Long amount) {
        clientName_ = "JON SKEET";
        productType_ = productType;
        amount_ = amount;
    }

    public UUID id() {
        return uuid_;
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
        aux += "Client Name: " + clientName_ + " - ";
        // Reduce the size of the UUID to better log size comprehension
        aux += "Order ID: " + uuid_.toString().substring(0,6) + " - ";
        aux += "Product Type: " + productType_.toString() + " - ";
        aux += "Amount: " + amount_;
        return aux;
    }

    private String clientName_;
    private final UUID uuid_ = UUID.randomUUID();
    // FIXME: Create a enum or something like that to represent this
    private Product productType_;
    private Long amount_;
    public static final long serialVersionUID = 123L;
}