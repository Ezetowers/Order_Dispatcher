package common;

import java.nio.ByteBuffer;
import java.util.UUID;
import common.Order;
import common.OrderState;
import common.Product;

/**
 * DB ENTRY serialization structure:
 * UUID - 16 bytes
 * PRODUCT_TYPE - 10 bytes (pad with spaces)
 * AMOUNT - 8 bytes (long type)
 * ORDER_STATE - 15 bytes (pad with spaces)
 */

public class OrderDBEntry {
    public OrderDBEntry(Order order) {
        order_ = order;
    }

    /**
     * @brief Receives a DB entry in bytes, and deserialize it to get an Order
     */
    public OrderDBEntry(byte[] entry) {
        ByteBuffer bb = ByteBuffer.wrap(entry);

        // UUID;
        UUID uuid = new UUID(bb.getLong(), bb.getLong());

        // Product
        byte[] productBuffer = new byte[PRODUCT_SIZE];
        bb.get(productBuffer, 0, PRODUCT_SIZE);
        Product product = Product.valueOf(new String(productBuffer).trim());

        // Amount 
        long amount = bb.getLong();

        // Order State
        byte[] stateBuffer = new byte[STATE_SIZE];
        bb.get(stateBuffer, 0, STATE_SIZE);
        OrderState state = OrderState.valueOf(new String(stateBuffer).trim()); 

        order_ = new Order(uuid, product, amount, state);
    }

    /**
     * @brief Serialiazes the Order stored as it should be stored in the DB
     * @return The order serialized as it would be stored in the DB
     */
    public byte[] getBytes() {
        ByteBuffer bb = ByteBuffer.allocate(ENTRY_SIZE);

        // UUID
        bb.putLong(order_.id().getMostSignificantBits());
        bb.putLong(order_.id().getLeastSignificantBits());

        // Product
        String product = String.format("%-10s", 
                                       order_.productType().toString());
        bb.put(product.getBytes());

        // Amount
        bb.putLong(order_.amount());

        // Order State
        String state = String.format("%-15s", order_.state().toString());
        bb.put(state.getBytes());

        return bb.array();
    }

    public Order order() {
        return order_;
    }

    private Order order_;
    // 16 = UUID size (This shouldn't change)
    public static final int UUID_SIZE = 16;
    public static final int PRODUCT_SIZE = 10;
    // 8 = Long size (I don't expect this to change)
    private static final int AMOUNT_SIZE = 8;
    private static final int STATE_SIZE = 15;
    public static final int ENTRY_SIZE = PRODUCT_SIZE + 
                                         STATE_SIZE + 
                                         UUID_SIZE + 
                                         AMOUNT_SIZE;
}