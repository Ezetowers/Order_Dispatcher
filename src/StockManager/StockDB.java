package stockManager;

import java.lang.System;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.util.Map;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;
import java.io.File;
import java.util.HashMap;

import common.Product;
import logger.Logger;
import logger.LogLevel;

public class StockDB {
    public StockDB(String dbFilePath) throws IOException {
        logger_ = Logger.getInstance();
        map_ = new HashMap<Product, Long>();

        File file = new File(dbFilePath);
        if (! file.exists()) {
            logger_.log(LogLevel.WARNING, 
                "StockDB file doesn't exists." 
                + " Proceed to create it. StockDB file: " + dbFilePath);
            file.createNewFile();

            file_ = new RandomAccessFile(dbFilePath, "rwd");
            this.createEmptyStockFile();
        }
        else {
            file_ = new RandomAccessFile(dbFilePath, "rwd");
        }
    }


    private void createEmptyStockFile() throws IOException {
        for (Product product : Product.values()) {
            map_.put(product, new Long(14871213));
        }

        FileLock lock = file_.getChannel().lock();
        for (Map.Entry<Product, Long> entry : map_.entrySet()) {
            String key = String.format("%-10s", entry.getKey().toString());
            file_.write(key.getBytes());

            ByteBuffer b = ByteBuffer.allocate(8);
            b.putLong(entry.getValue());
            file_.write(b.array());
        }

        file_.getFD().sync();
        lock.release();
    }

    
    private void refreshStockFile() throws IOException {
        byte[] buffer = new byte[PRODUCT_KEY_MAX_SIZE];
        file_.seek(0);

        try {
            while(true) {
                file_.read(buffer, 0, PRODUCT_KEY_MAX_SIZE);
                Product key = Product.valueOf(new String(buffer).trim());
                Long value = new Long(file_.readLong());

                logger_.log(LogLevel.TRACE, "KEY: " + key.toString() 
                    + " - VALUE: " + value);
                map_.put(key, value);
            }
        }
        catch (EOFException e) {
            // EOF found
        }
    }

    public boolean decreaseStock(Product product, Long amount) 
    throws IOException {
        // Refresh stock before doing something. Maybe the Providers
        // added a batch
        FileLock lock = file_.getChannel().lock();
        this.refreshStockFile();
        boolean stockUpdated = false;

        Long productStock = map_.get(product);
        if (productStock >= amount) {
            Long newProductStock = productStock - amount;
            this.setProductStock(product, newProductStock);

            logger_.log(LogLevel.DEBUG, "Decreasing stock of product " 
                + product.toString() + ". PreviousStock: " 
                + productStock + " - UpdatedStock: " + newProductStock);
            stockUpdated = true;
        }
        else {
            logger_.log(LogLevel.WARNING, "Order cannot be accepted. " 
                + "Not enough stock of product " + product.toString() 
                + ". ProductStock: " + productStock 
                + " - OrderAmount: " + amount);
        }
        lock.release();
        return stockUpdated;
    }

    public boolean increaseStock(Product product, Long amount) 
    throws IOException {
        // Refresh stock before doing something. Maybe the Providers
        // added a batch
        FileLock lock = file_.getChannel().lock();
        this.refreshStockFile();

        Long productStock = map_.get(product);
        Long newProductStock = productStock + amount;
        this.setProductStock(product, newProductStock);

        logger_.log(LogLevel.NOTICE, "Increasing stock of product " 
            + product.toString() + ". PreviousStock: " 
            + productStock + " - UpdatedStock: " + newProductStock);
        lock.release();

        // TODO: Check if the product is greater than a constant
        return true;
    }

    private void setProductStock(Product product, Long amount) 
    throws IOException {
        byte[] buffer = new byte[PRODUCT_KEY_MAX_SIZE];
        file_.seek(0);

        try {
            while(true) {
                file_.read(buffer, 0, PRODUCT_KEY_MAX_SIZE);
                Product key = Product.valueOf(new String(buffer).trim());
                if (key != product) {
                    // Jump to the next entry
                    file_.skipBytes(8);
                    continue;
                }

                // We found the product, update it
                ByteBuffer b = ByteBuffer.allocate(8);
                b.putLong(amount);
                file_.write(b.array());
                file_.getFD().sync();
                break;
            }

        }
        catch (EOFException e) {
            // If this happen, then the product does not exists and we have
            // a bug in the system. ABORT!
            logger_.log(LogLevel.ERROR, "Product does not exists. Product: " 
                + product.toString());
            System.exit(-1);
        }
    }

    private Logger logger_;
    private HashMap<Product, Long> map_;
    private RandomAccessFile file_;
    private static final int PRODUCT_KEY_MAX_SIZE = 10;
}