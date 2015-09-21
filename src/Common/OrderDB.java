package common;

import java.lang.System;
import java.io.EOFException;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.io.RandomAccessFile;
import common.Order;
import common.OrderDBEntry;
import common.Product;
import logger.Logger;
import logger.LogLevel;

import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.lang.SecurityException;

public class OrderDB {
    public OrderDB(String dirPath) throws SecurityException, 
                                          IOException,
                                          IllegalArgumentException {
        logger_ = Logger.getInstance();

        dirPath_ = dirPath;
        File file = new File(dirPath);
        if (! file.isDirectory()) {
            // Use mkdirs instead of mkdir, to create intermediate 
            // directories if they does not exists
            file.mkdirs();
        }
    }

    public void add(Order order) throws IOException {
        RandomAccessFile file = this.getOrderFile(order, "rwd");
        FileLock lock = file.getChannel().lock();
        OrderDBEntry entry = new OrderDBEntry(order);

        // Go to the end of the file
        file.seek(file.length());
        file.write(entry.getBytes());
        lock.release();
        file.close();
    }

    public void alter(Order order) throws IOException {
        RandomAccessFile file = this.getOrderFile(order, "rwd");
        FileLock lock = file.getChannel().lock();
        long offset = this.getOffsetToEntry(file, order.id());

        // Sanity check
        if (offset == -1) {
            // This should not happen. Stop program
            logger_.log(LogLevel.ERROR, "Order doesn't exists in alter");
            System.exit(-1);
        }

        // Do a have to do this or the file is in the correct offset?
        file.seek(offset);
        OrderDBEntry entry = new OrderDBEntry(order);
        file.write(entry.getBytes());

        lock.release();
        file.close();
    }

    public Order get(UUID orderKey) throws IOException {
        RandomAccessFile file = this.getOrderFile(orderKey, "rwd");
        FileLock lock = file.getChannel().lock();
        long offset = this.getOffsetToEntry(file, orderKey);

        if (offset == -1) {
            lock.release();
            file.close();
            return null;
        }

        byte[] entryBuffer = new byte[OrderDBEntry.ENTRY_SIZE];
        file.seek(offset);
        file.read(entryBuffer, 0, OrderDBEntry.ENTRY_SIZE);
        OrderDBEntry entry = new OrderDBEntry(entryBuffer);

        lock.release();
        file.close();

        return entry.order();
    }

    private long getOffsetToEntry(RandomAccessFile file, UUID orderKey) 
    throws IOException {
        byte[] buffer = new byte[OrderDBEntry.UUID_SIZE];
        try {
            file.seek(0);
            while(true) {
                int readBytes = file.read(buffer, 0, OrderDBEntry.UUID_SIZE);
                if (readBytes == -1) {
                    // EOF reached
                    return -1;
                }

                // Create a UUID
                ByteBuffer bb = ByteBuffer.wrap(buffer);
                UUID uuid = new UUID(bb.getLong(), bb.getLong());

                if (uuid.equals(orderKey)) {
                    break;
                }

                // Jump to the next entry
                file.skipBytes(OrderDBEntry.ENTRY_SIZE - 
                               OrderDBEntry.UUID_SIZE);
            }
        }
        catch (EOFException e) {
            // If this happen, then the product does not exists and we have
            // a bug in the system. ABORT!
            logger_.log(LogLevel.ERROR, "Order does not exists in OrderDB. "
                + "Order key: " + orderKey.toString());
            System.exit(-1);
        }

        // We must sustract the key that was read in the last comparison
        return file.getFilePointer() - OrderDBEntry.UUID_SIZE;
    }

    /**
     * @brief Get the file where the order must be stored in the DB     
     **/
    private RandomAccessFile getOrderFile(Order order, String mode) 
    throws IOException {
        String subUuid = order.stringID().substring(0, 2);
        return this.getOrderFile(subUuid, mode);

    }

    private RandomAccessFile getOrderFile(UUID uuid, String mode) 
    throws IOException {
        String subUuid = uuid.toString().substring(0, 2);
        return this.getOrderFile(subUuid, mode);
    }

    private RandomAccessFile getOrderFile(String subUuid, String mode) 
    throws IOException {
        String fileName = dirPath_ + "/" + subUuid;

        // Again, we cannot check if the file exists. Just try to create it
        File orderFile = new File(fileName);
        orderFile.createNewFile();
        return new RandomAccessFile(fileName, mode);
    }

    private String dirPath_;
    private Logger logger_;
}