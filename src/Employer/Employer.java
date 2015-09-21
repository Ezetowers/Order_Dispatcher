package employer;

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


public class Employer extends DefaultConsumer {
    public Employer(Channel channel) throws IllegalArgumentException,
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
        // Change the state a we should have process the order and be ready
        // to deliver it
        newOrder.state(OrderState.DELIVERED);
        body = SerializationUtils.serialize(newOrder);

        logger_.log(LogLevel.DEBUG, "Order delivered: " + newOrder.stringID());
        this.getChannel().basicPublish("", orderManagerQueueName_, null, body);
    }

    /**
     * @brief Declare the queues. This is necessary because maybe they have not
     * been created yet
     */
    private void initQueues() throws IOException {
        Channel channel = this.getChannel();
        channel.basicQos(1);

        orderManagerQueueName_ = config_.get("QUEUES", "order-manager-queue");
        channel.queueDeclare(orderManagerQueueName_, 
                             false, 
                             false, 
                             false, 
                             null);
    }

    private Logger logger_;
    private ConfigParser config_;
    private String orderManagerQueueName_;
}