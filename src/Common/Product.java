package common;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Random;

public enum Product {
    TYPE_1,
    TYPE_2,
    TYPE_3,
    TYPE_4,
    TYPE_5;

    private static final List<Product> VALUES =
        Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Product randomProduct() {
    return VALUES.get(RANDOM.nextInt(SIZE));
    }

}




