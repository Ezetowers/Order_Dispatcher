package logger;

import java.io.*;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Date;

import logger.LogLevel;



public class Logger {
    private Logger() {}

    public static Logger getInstance() {
        if (logger_ == null) {
            logger_ = new Logger();
        }

        return logger_;
    }

    public void init(String filePath, LogLevel verbosity) {
        verbosity_ = verbosity;
        dateFormat_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        lock_ = new ReentrantLock();

        try {
            // Open file in append mode
            fstream_ = new FileOutputStream(filePath, true);
        }
        catch(IOException e) {
            System.err.println("[LOGGER] Error calling init() method.");
            System.err.println(e);
        }
    }

    public void setPrefix(String prefix) {
        try {
            lock_.lock();
            prefix_ = prefix;
        }
        finally {
            lock_.unlock();
        }
    }

    public void terminate() throws IOException {
        FileLock lock = null;
        try {
            lock = fstream_.getChannel().lock();
            fstream_.close();
        }
        catch(IOException e) {
            System.err.println("[LOGGER] Error calling terminate() method.");
            System.err.println(e);
            System.exit(-1);
        }
        finally {
            lock.release();
        }
    }

    public void log(LogLevel verbosity, String msg) {
        if (verbosity_.level() >= verbosity.level()) {
            this.write(verbosity_.prefix(verbosity) + " " + msg);
        }
    }

    private void write(String msg) {
        try {
            Date date = new Date();
            msg = dateFormat_.format(date) + " " + prefix_ + " " + msg + "\n";
            
            FileLock lock = fstream_.getChannel().lock();
            fstream_.write(msg.getBytes());
            fstream_.flush();
            // Remove sync to enhace performance
            // fstream_.getFD().sync();
            lock.release();
        }
        catch(IOException e) {
            System.err.println("[LOGGER] Error calling write() method.");
            System.err.println(e);
            System.exit(-1);
        }
    }

    private static Logger logger_ = null;
    private DateFormat dateFormat_;
    private LogLevel verbosity_;
    private FileOutputStream fstream_;
    private Lock lock_;
    private String prefix_;
}
