package auditLogger;

import java.lang.Runtime;
import java.lang.Thread;
import java.util.concurrent.TimeoutException;
import java.io.IOException;

// External libraries includes
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang3.SerializationUtils;

import auditLogger.AuditLogger;
import common.Order;
import configParser.ConfigParser;
import logger.Logger;
import logger.LogLevel;


public class MainClass {
    public static void main(String[] argv) {
        ConfigParser config = ConfigParser.getInstance();                                        
        Logger logger = Logger.getInstance();

        try {
            // Create the instance to avoid calling the mukry, 
            // obscure and infame static methods of Java
            MainClass app = new MainClass();
            config.init(argv[1]);
            app.initLogger(config, argv[0]);

            Channel channel = app.initRabbit(config);
            String auditLogFile = config.get("AUDIT", "audit-log-file");
            String auditLogQueue = config.get("QUEUES", "audit-log-queue");
            Consumer consumer = new AuditLogger(channel, auditLogFile);
            channel.basicConsume(auditLogQueue, true, consumer);
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
        logger.setPrefix("[AUDIT_LOG " + processNumber + "]");
        logger.log(LogLevel.DEBUG, "Process started");
    }

    private Channel initRabbit(ConfigParser config)
    throws IllegalArgumentException,
           IOException,
           TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.get("MAIN", "server-address", "localhost"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String auditLogQueue = config.get("QUEUES", "audit-log-queue");
        // To secure fairness between the processes
        channel.basicQos(1);
        channel.queueDeclare(auditLogQueue, 
                             false, 
                             false, 
                             false, 
                             null);

        return channel;
    }
}