package common;

import java.io.Serializable;
import java.util.UUID;

public class Order implements Serializable {
    public Order(int productType, long amount) {
        productType_ = productType;
        amount_ = amount;
    }

    public UUID id() {
        return uuid_;
    }

    public int productType() {
        return productType_;
    }

    public long amount() {
        return amount_;
    }

    public String toString() {
        String aux = "";
        aux += uuid_.toString() + " - ";
        aux += productType_ + " - " + amount_;
        return aux;
    }

    private final UUID uuid_ = UUID.randomUUID();
    // FIXME: Create a enum or something like that to represent this
    private int productType_;
    private long amount_;
    public static final long serialVersionUID = 123L;
}