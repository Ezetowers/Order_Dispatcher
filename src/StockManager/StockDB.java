package stockManager;

import java.nio.channels.OverlappingFileLockException;
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
        // Taken from the JavaDocs
        // http://docs.oracle.com/javase/7/docs/api/java/io/File.
        // html#createNewFile()
        // Atomically creates a new, empty file named by this abstract 
        // pathname if and only if a file with this name does not yet 
        // exist. The check for the existence of the file and the creation 
        // of the file if it does not exist are a single operation that is 
        // atomic with respect to all other filesystem activities that 
        // might affect the file.
        file.createNewFile();

        // http://docs.oracle.com/javase/7/docs/api/java/io/
        // RandomAccessFile.html#mode
        // The "rwd" mode can be used to reduce the number of I/O operations 
        // performed. Using "rwd" only requires updates to the file's content 
        // to be written to storage; using "rws" requires updates to both the 
        // file's content and its metadata to be written, which generally 
        // requires at least one more low-level I/O operation.
        file_ = new RandomAccessFile(dbFilePath, "rwd");
        FileLock lock = file_.getChannel().lock();

        if (file.length() == 0) {
            this.createEmptyStockFile();
            logger_.log(LogLevel.WARNING, 
                "StockDB file doesn't exists." 
                + " Proceed to create it. StockDB file: " + dbFilePath);
        }
        lock.release();
    }

    private void createEmptyStockFile() throws IOException {
        for (Product product : Product.values()) {
            map_.put(product, new Long(10000));
        }

        for (Map.Entry<Product, Long> entry : map_.entrySet()) {
            String key = String.format("%-10s", entry.getKey().toString());
            file_.write(key.getBytes());

            ByteBuffer b = ByteBuffer.allocate(8);
            b.putLong(entry.getValue());
            file_.write(b.array());
        }
    }
    
    /* private void refreshStockFile() throws IOException {
        byte[] buffer = new byte[PRODUCT_KEY_MAX_SIZE];
        file_.seek(0);

        try {
            while(true) {
                int readBytes = file_.read(buffer, 0, PRODUCT_KEY_MAX_SIZE);

                if (readBytes == -1) {
                    // EOF found
                    return;
                }

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
    }*/

    /* public boolean decreaseStock(Product product, Long amount) 
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

            logger_.log(LogLevel.TRACE, "Decreasing stock of product " 
                + product.toString() + ". PreviousStock: " 
                + productStock + " - UpdatedStock: " + newProductStock);
            stockUpdated = true;
        }
        else {
            logger_.log(LogLevel.TRACE, "Order cannot be accepted. " 
                + "Not enough stock of product " + product.toString() 
                + ". ProductStock: " + productStock 
                + " - OrderAmount: " + amount);
        }
        lock.release();
        return stockUpdated;
    }*/

    // This method is very long, but don't split it in functions because
    // to performance problems
    public boolean decreaseStock(Product product, Long amount) 
    throws IOException {
        byte[] buffer = new byte[PRODUCT_KEY_MAX_SIZE];
        file_.seek(0);

        try {
            while(true) {
                int readBytes = file_.read(buffer, 0, PRODUCT_KEY_MAX_SIZE);
                if (readBytes == -1) {
                    // lock.release();
                    return false;
                }

                Product key = Product.valueOf(new String(buffer).trim());
                if (key != product) {
                    // Jump to the next entry
                    file_.skipBytes(Long.BYTES);
                    continue;
                }

                // Product found, proceed to update value
                // Read the amount of the stock to update it, and go back to 
                // the same position
                file_.read(buffer, 0, Long.BYTES);
                file_.seek(file_.getFilePointer() - Long.BYTES);

                // Check if there is stock of the file
                ByteBuffer b = ByteBuffer.wrap(buffer);
                long productStock = b.getLong();
                if (productStock < amount) {
                    logger_.log(LogLevel.WARNING, "Order cannot be accepted. " 
                        + "Not enough stock of product " + product.toString() 
                        + ". ProductStock: " + productStock 
                        + " - OrderAmount: " + amount);
                    return false;
                }

                // There is stock, update the StockDB
                long newStock = productStock - amount;
                ByteBuffer longBuf = ByteBuffer.allocate(Long.BYTES);
                longBuf.putLong(newStock);

                // Just lock the part of the file to me modified
                FileLock lock = file_.getChannel().lock(file_.getFilePointer(),
                                                        Long.BYTES,
                                                        false);
                file_.write(longBuf.array());
                lock.release();

                logger_.log(LogLevel.DEBUG, "Decreasing stock of product " 
                    + product.toString() + ". PreviousStock: " 
                    + productStock + " - UpdatedStock: " + newStock);
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

        // lock.release();
        return true;
    }


    // This method is very long, but don't split it in functions because
    // to performance problems
    public boolean increaseStock(Product product, Long amount) 
    throws IOException {
        byte[] buffer = new byte[PRODUCT_KEY_MAX_SIZE];
        file_.seek(0);

        try {
            while(true) {
                int readBytes = file_.read(buffer, 0, PRODUCT_KEY_MAX_SIZE);
                if (readBytes == -1) {
                    // lock.release();
                    return false;
                }

                Product key = Product.valueOf(new String(buffer).trim());
                if (key != product) {
                    // Jump to the next entry
                    file_.skipBytes(Long.BYTES);
                    continue;
                }

                // Product found, proceed to update value
                // Read the amount of the stock to update it, and go back to 
                // the same position
                file_.read(buffer, 0, Long.BYTES);
                file_.seek(file_.getFilePointer() - Long.BYTES);

                // Check if there is stock of the file
                ByteBuffer b = ByteBuffer.wrap(buffer);
                long productStock = b.getLong();

                // There is stock, update the StockDB
                long newStock = productStock + amount;
                ByteBuffer longBuf = ByteBuffer.allocate(Long.BYTES);
                longBuf.putLong(newStock);

                // Just lock the part of the file to me modified
                FileLock lock = file_.getChannel().lock(file_.getFilePointer(),
                                                        Long.BYTES,
                                                        false);
                file_.write(longBuf.array());
                lock.release();

                logger_.log(LogLevel.NOTICE, "Increasing stock of product " 
                    + product.toString() + ". PreviousStock: " 
                    + productStock + " - UpdatedStock: " + newStock);
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

        // lock.release();
        return true;
    }

    // FIXME: Refactor this methods to as efficient as decreaseStock. Try
    // to reutilize code also
    /* public boolean increaseStock(Product product, Long amount) 
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
    }*/

    /* private void setProductStock(Product product, Long amount) 
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
    }*/

    private Logger logger_;
    private HashMap<Product, Long> map_;
    private RandomAccessFile file_;
    private static final int PRODUCT_KEY_MAX_SIZE = 10;
}