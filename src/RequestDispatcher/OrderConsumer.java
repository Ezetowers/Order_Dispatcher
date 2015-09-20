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
import logger.Logger;
import logger.LogLevel;

import java.io.IOException;


public class OrderConsumer extends DefaultConsumer {
    public OrderConsumer(Channel channel) throws IllegalArgumentException {
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        auditLogQueueName_ = config_.get("QUEUES", "audit-log-queue");
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.DEBUG, "Order received: " + newOrder.stringID());

        // Send the order received to the auditLog
        this.getChannel().basicPublish("", auditLogQueueName_, null, body);
    }

    private Logger logger_;
    private ConfigParser config_;
    private String auditLogQueueName_;
}