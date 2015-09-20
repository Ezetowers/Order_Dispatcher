package stockManager;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang3.SerializationUtils;

import configParser.ConfigParser;
import common.Order;
import common.OrderState;
import logger.Logger;
import logger.LogLevel;
import stockManager.StockDB;

import java.io.IOException;


public class StockManager extends DefaultConsumer {
    public StockManager(Channel channel) throws IllegalArgumentException,
                                                IOException {
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        // auditLogQueueName_ = config_.get("QUEUES", "audit-log-queue");

        String stockDBFile = config_.get("STOCK", "stock-db-file");
        stockDB_ = new StockDB(stockDBFile);
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.DEBUG, "Order received: " + newOrder.toString());

        boolean enoughStock = stockDB_.decreaseStock(newOrder.productType(), 
                                                     newOrder.amount());

        if (enoughStock) {
            newOrder.state(OrderState.ACCEPTED);
        }
        else {
            newOrder.state(OrderState.REJECTED);    
        }

        logger_.log(LogLevel.DEBUG, "Order processed: " + newOrder.toString());
    }

    private Logger logger_;
    private ConfigParser config_;
    private String auditLogQueueName_;
    private StockDB stockDB_;
}