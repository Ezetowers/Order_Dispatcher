package common;

import common.OrderState;
import common.Product;

import java.io.Serializable;
import java.util.UUID;


public class Order implements Serializable {
    public Order(Product productType, Long amount) {
        state_ = OrderState.TO_BE_PROCESSED;
        productType_ = productType;
        amount_ = amount;
        uuid_ = UUID.randomUUID();
    }

    public Order(UUID uuid,
                 Product productType,
                 Long amount,
                 OrderState state) {
        uuid_ = uuid;
        productType_ = productType;
        amount_ = amount;
        state_ = state;
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

    /**
     * @brief Return a representation of the order just with a truncated 
     * UUID and the state of the order
     */
    public String toStringShort() {
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
    private final UUID uuid_;
    // FIXME: Create a enum or something like that to represent this
    private Product productType_;
    private Long amount_;
    public static final long serialVersionUID = 123L;
}