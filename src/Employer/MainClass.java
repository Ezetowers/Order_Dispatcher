package employer;

// Program includes
import java.lang.Thread;
import java.lang.Runtime;
import configParser.ConfigParser;
import logger.Logger;
import logger.LogLevel;
import requestDispatcher.RequestDispatcher;

// External libraries includes
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

// Java includes
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainClass extends Thread {
    public MainClass() {
        logger_ = Logger.getInstance();
    }

    public static void main(String[] argv) {
        ConfigParser config = ConfigParser.getInstance();                                        
        Logger logger = Logger.getInstance();

        try {
            MainClass app = new MainClass();
            Runtime.getRuntime().addShutdownHook(app);

            config.init(argv[1]);
            app.initLogger(config, argv[0]);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(config.get("MAIN", "server-address", "localhost"));
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            String deliveryQueue = config.get("QUEUES", "delivery-queue");
            // To secure fairness between the processes
            channel.basicQos(1);
            channel.queueDeclare(deliveryQueue, 
                                 false, 
                                 false, 
                                 false, 
                                 null);

            Consumer consumer = new Employer(channel, deliveryQueue);
            channel.basicConsume(deliveryQueue, true, consumer);
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
        logger.setPrefix("[EMPLOYER " + processNumber + "]");
        logger.log(LogLevel.DEBUG, "Process started");
    }

    public void run() {
        logger_.log(LogLevel.NOTICE, "Program finished by signal.");
    }

    private Logger logger_;
}