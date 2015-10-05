package stockProvider;

import java.util.Iterator;
import java.util.UUID;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.System;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.util.Random;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.SerializationUtils;

import stockManager.StockDB;
import common.Product;
import configParser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import stockManager.UnknownProductException;

public class MainClass {
    public MainClass(String[] argv) throws IllegalArgumentException, 
                                           IOException {
        config_ = ConfigParser.getInstance();
        logger_ = Logger.getInstance();

        config_.init(argv[1]);
        this.initLogger(argv[0]);

        String stockDBFile = config_.get("STOCK", "stock-db-file");
        stockDB_ = new StockDB(stockDBFile);
    }

    public static void main(String[] argv) {
        ConfigParser config = ConfigParser.getInstance();
        Logger logger = Logger.getInstance();
        try {
            MainClass app = new MainClass(argv);
            app.increaseStock();
        }
        catch (IllegalArgumentException e) {
            // We couldn't open the logger. Just exit
            System.out.println(e);
            System.exit(-1);
        }
        catch (IOException e) {
            logger.log(LogLevel.ERROR, e.toString());
        }
    }

    private void initLogger(String processNumber) 
    throws IllegalArgumentException {
        String logFileName = config_.get("MAIN", "log-file");
        String logLevel = config_.get("MAIN", "log-level");

        Logger logger = Logger.getInstance();
        logger.init(logFileName, LogLevel.parse(logLevel));
        logger.setPrefix("[STOCK_PROVIDER " + processNumber + "]");
        logger.log(LogLevel.DEBUG, "Process started");
    }

    public void increaseStock() throws IllegalArgumentException,
                                       IOException {
        long globalIncrease = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                         "global-increase"));
        long type1Increase = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                        "type-1-increase"));
        long type2Increase = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                        "type-2-increase"));
        long type3Increase = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                        "type-3-increase"));
        long type4Increase = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                        "type-4-increase"));
        long type5Increase = Long.parseLong(config_.get("STOCK-PROVIDER", 
                                                        "type-5-increase"));
        try {
            stockDB_.increaseStock(Product.TYPE_1, 
                                   globalIncrease + type1Increase);
            stockDB_.increaseStock(Product.TYPE_2, 
                                   globalIncrease + type2Increase);
            stockDB_.increaseStock(Product.TYPE_3, 
                                   globalIncrease + type3Increase);
            stockDB_.increaseStock(Product.TYPE_4, 
                                   globalIncrease + type4Increase);
            stockDB_.increaseStock(Product.TYPE_5, 
                                   globalIncrease + type5Increase);
        }
        catch (UnknownProductException e) {}
    }

    private Logger logger_;
    private ConfigParser config_;
    private StockDB stockDB_;
}