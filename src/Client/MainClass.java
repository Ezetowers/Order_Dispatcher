package client;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.lang.Thread;
import java.lang.Runtime;
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

import common.Order;
import common.OrderState;
import common.Product;
import configParser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class MainClass extends Thread {
    public MainClass(String[] argv) {
        randomGenerator_ = new Random(System.currentTimeMillis());
        config_ = ConfigParser.getInstance();
        logger_ = Logger.getInstance();
        lock_ = new ReentrantLock();
        ordersKeys_ = new ArrayList<UUID>();

        config_.init(argv[1]);
        this.initLogger(argv[0]);
    }

    public static void main(String[] argv) throws InterruptedException {
        ConfigParser config = ConfigParser.getInstance();
        Logger logger = Logger.getInstance();
        try {
            MainClass app = new MainClass(argv);
            Runtime.getRuntime().addShutdownHook(app);

            app.initRabbit();
            logger.log(LogLevel.INFO, 
                "Proceed to create and send orders");
            app.sendOrders();

            int sleepTime = Integer.parseInt(config.get("CLIENT", 
                "sleep-between-orders-and-queries", "0"));

            if (sleepTime > 0) {
                logger.log(LogLevel.INFO, 
                    "Proceed to sleep before send queries to the system");
                Thread.sleep(sleepTime * 1000);
            }

            logger.log(LogLevel.INFO, 
                "Proceed to send queries associated with the orders created");
            app.queryOrders();
            app.terminate();
        }
        catch (IllegalArgumentException e) {
            // We couldn't open the logger. Just exit
            System.out.println(e);
            System.exit(-1);
        }
        catch (TimeoutException e) {
            logger.log(LogLevel.ERROR, e.toString());
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
        logger.setPrefix("[CLIENT " + processNumber + "]");
        logger.log(LogLevel.DEBUG, "Process started");
    }

    public void initRabbit() throws IOException,
                                    TimeoutException,
                                    IllegalArgumentException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config_.get("MAIN", "server-address", "localhost"));
        connection_ = factory.newConnection();
        channel_ = connection_.createChannel();

        clientQueue_ = config_.get("QUEUES", "client-queue");
        channel_.queueDeclare(clientQueue_, 
                              false, 
                              false, 
                              false, 
                              null);

        queryQueue_ = config_.get("QUEUES", "query-queue");
        channel_.queueDeclare(queryQueue_, 
                              false, 
                              false, 
                              false,
                              null);
    }

    public void terminate() throws IOException, TimeoutException {
        channel_.close();
        connection_.close();
    }

    public void sendOrders() throws IOException {
        int ordersToCreate = 
            Integer.parseInt(config_.get("CLIENT", 
                                         "amount-orders-to-simulate",
                                         "1"));
        logger_.log(LogLevel.DEBUG, "Orders to simulate: " 
            + ordersToCreate);

        for (int i = 0; i < ordersToCreate; ++i) {
            Order order = this.generateRandomOrder();
            byte[] data = SerializationUtils.serialize(order);

            // Store the UUID generated to then make a query to the system
            ordersKeys_.add(order.id());

            logger_.log(LogLevel.DEBUG, "Sending order: " + order.stringID());
            channel_.basicPublish("", clientQueue_, null, data);
        }
    }

    /**
     * @brief Sends as much queries as the parameter amount-queries-to-simulate
     * @details If amount-queries-to-simulate is bigger than 
     * amount-orders-to-simulate, a round-robin algorithm is used to keep 
     * querying orders
     */
    public void queryOrders() throws IOException {
        int amountQueries = 
            Integer.parseInt(config_.get("CLIENT", 
                                         "amount-queries-to-simulate",
                                         "1"));

        Iterator<UUID> it = ordersKeys_.iterator();
        while (amountQueries > 0) {
            --amountQueries;

            UUID key = it.next();
            byte[] data = SerializationUtils.serialize(key);
            logger_.log(LogLevel.DEBUG, "Querying order: " + key.toString());
            channel_.basicPublish("", queryQueue_, null, data);

            if (! it.hasNext()) {
                it = ordersKeys_.iterator();
            }
        }
    }

    private Order generateRandomOrder() {
        long amount = Math.abs(randomGenerator_.nextInt() % 10) + 1;
        return new Order(Product.randomProduct(), amount);
    }

    public void run() {
        try {
            this.terminate();
        }
        catch (TimeoutException e) {
        }
        catch(IOException e) {
        }
    }

    private Logger logger_;
    private ConfigParser config_;
    private Random randomGenerator_;
    private Channel channel_;
    private Connection connection_;
    private String clientQueue_;
    private String queryQueue_;
    private Lock lock_;
    private ArrayList<UUID> ordersKeys_;
}