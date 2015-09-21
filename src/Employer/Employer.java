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
import java.util.concurrent.TimeoutException;

public class Employer extends DefaultConsumer {
    public Employer(Channel channel, 
                    String channelName) throws IllegalArgumentException,
                                               IOException {    
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        this.initQueues();

        channelName_ = channelName;
        channelClosed_ = false;

        amountOrdersToProcess_ = 
            Integer.parseInt(config_.get("EMPLOYER", 
                                         "amount-orders-to-process"));
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        if (channelClosed_) {
            return;
        }

        Order newOrder = (Order) SerializationUtils.deserialize(body);
        // Change the state a we should have process the order and be ready
        // to deliver it
        newOrder.state(OrderState.DELIVERED);
        body = SerializationUtils.serialize(newOrder);

        logger_.log(LogLevel.DEBUG, "Order delivered: " + newOrder.stringID());
        this.getChannel().basicPublish("", orderManagerQueueName_, null, body);

        --amountOrdersToProcess_;
        if (amountOrdersToProcess_ == 0) {
            logger_.log(LogLevel.NOTICE, "Closing Employer.");
            this.getChannel().basicCancel(channelName_);
            channelClosed_ = true;
        }
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
    private String channelName_;
    private int amountOrdersToProcess_;
    private boolean channelClosed_;
}