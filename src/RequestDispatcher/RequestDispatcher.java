package requestDispatcher;

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

import java.io.IOException;


public class RequestDispatcher extends DefaultConsumer {
    public RequestDispatcher(Channel channel) throws IllegalArgumentException,
                                                     IOException {    
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        this.initQueues();
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);

        newOrder.state(OrderState.RECEIVED);
        body = SerializationUtils.serialize(newOrder);
        
        this.getChannel().basicPublish("", orderManagerQueueName_, null, body);
        logger_.log(LogLevel.DEBUG, "Order received: " + newOrder.stringID());

        // Send the order received to the auditLog
        this.getChannel().basicPublish("", auditLogQueueName_, null, body);

        // Send the order to the Stock Manager
        this.getChannel().basicPublish("", stockManagerQueueName_, null, body);
    }

    /**
     * @brief Declare the queues. This is necessary because maybe they have not
     * been created yet
     */
    private void initQueues() throws IOException {
        Channel channel = this.getChannel();
        channel.basicQos(1);

        auditLogQueueName_ = config_.get("QUEUES", "audit-log-queue");
        channel.queueDeclare(auditLogQueueName_, 
                             false, 
                             false, 
                             false, 
                             null);

        orderManagerQueueName_ = config_.get("QUEUES", "order-manager-queue");
        channel.queueDeclare(orderManagerQueueName_, 
                             false, 
                             false, 
                             false, 
                             null);

        stockManagerQueueName_ = config_.get("QUEUES", "stock-manager-queue");
        channel.queueDeclare(stockManagerQueueName_, 
                             false, 
                             false, 
                             false, 
                             null);
    }

    private Logger logger_;
    private ConfigParser config_;
    private String auditLogQueueName_;
    private String stockManagerQueueName_;
    private String orderManagerQueueName_;
}