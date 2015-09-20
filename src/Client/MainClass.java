package client;

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
import common.Product;
import configParser.ConfigParser;
import logger.Logger;
import logger.LogLevel;

public class MainClass {
    public MainClass() {
        randomGenerator_ = new Random(System.currentTimeMillis());
    }
    
    public static void main(String[] argv) {
        ConfigParser config = ConfigParser.getInstance();                                        
        Logger logger = Logger.getInstance();

        try {
            // Create the instance to avoid calling the mukry, 
            // obscure and infame static methods of Java
            MainClass app = new MainClass();
            config.init(argv[1]);
            app.initLogger(config, argv[0]);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(config.get("MAIN", "server-address", "localhost"));
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            String clientQueue = config.get("QUEUES", "client-queue");
            channel.queueDeclare(clientQueue, 
                                 false, 
                                 false, 
                                 false, 
                                 null);

            Order order = app.generateRandomOrder();
            byte[] data = SerializationUtils.serialize(order);

            logger.log(LogLevel.DEBUG, "Sending order: " + order.stringID());
            channel.basicPublish("", clientQueue, null, data);

            channel.close();
            connection.close();
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

    private void initLogger(ConfigParser config, String processNumber) 
    throws IllegalArgumentException {
        String logFileName = config.get("MAIN", "log-file");
        String logLevel = config.get("MAIN", "log-level");

        Logger logger = Logger.getInstance();
        logger.init(logFileName, LogLevel.parse(logLevel));
        logger.setPrefix("[CLIENT " + processNumber + "]");
        logger.log(LogLevel.DEBUG, "Process started");
    }

    public Order generateRandomOrder() {
        Long amount = new Long(randomGenerator_.nextLong() % 10 + 1);
        return new Order(Product.randomProduct(), amount);
    }

    private Random randomGenerator_; 
}