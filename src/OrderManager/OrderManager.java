package orderManager;

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
import common.OrderDB;
import common.OrderState;
import logger.Logger;
import logger.LogLevel;

import java.io.IOException;


public class OrderManager extends DefaultConsumer {
    public OrderManager(Channel channel) throws IllegalArgumentException,
                                                IOException {    
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();

        orderDB_ = new OrderDB(config_.get("ORDER", "order-db-directory"));
        this.initQueues();

    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.DEBUG, "Order received: " + newOrder.stringID());

        OrderState state = newOrder.state();
        switch(newOrder.state()) {
            case RECEIVED:
                // Add the order to the DB
                orderDB_.add(newOrder);
                break;
            case ACCEPTED:
            case REJECTED:
            case DELIVERED:
                orderDB_.alter(newOrder);
                break;
        }       

        logger_.log(LogLevel.INFO, "Order processed: " + newOrder.stringID());
    }

    /**
     * @brief Declare the queues. This is necessary because maybe they have not
     * been created yet
     */
    private void initQueues() throws IOException {
        Channel channel = this.getChannel();
        channel.basicQos(1);

        /* auditLogQueueName_ = config_.get("QUEUES", "audit-log-queue");
        channel.queueDeclare(auditLogQueueName_, 
                             false, 
                             false, 
                             false, 
                             null);

        stockManagerQueue_ = config_.get("QUEUES", "stock-manager-queue");
        channel.queueDeclare(stockManagerQueue_, 
                             false, 
                             false, 
                             false, 
                             null);*/
    }

    private Logger logger_;
    private ConfigParser config_;
    private OrderDB orderDB_;
    // private String auditLogQueueName_;
}