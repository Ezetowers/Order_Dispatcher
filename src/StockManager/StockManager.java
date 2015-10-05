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
import stockManager.UnknownProductException;

import java.io.IOException;


public class StockManager extends DefaultConsumer {
    public StockManager(Channel channel) throws IllegalArgumentException,
                                                IOException {
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        this.initQueues();

        String stockDBFile = config_.get("STOCK", "stock-db-file");
        stockDB_ = new StockDB(stockDBFile);
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.TRACE, "Order received: " + newOrder.toString());


        elapsedTime_ = System.currentTimeMillis();
        boolean enoughStock = false;
        try {
            enoughStock = stockDB_.decreaseStock(newOrder.productType(), 
                                                 newOrder.amount());
        }
        catch (UnknownProductException e) {
            // Product doesn't exists. Treat this case as a not enough stock
            // (Reject the order)
            enoughStock = false;
            return;
        }

        elapsedTime_ = System.currentTimeMillis() - elapsedTime_;
        logger_.log(LogLevel.NOTICE, "decreaseStock. Time: " 
            + elapsedTime_ + " ms.");

        if (enoughStock) {
            newOrder.state(OrderState.ACCEPTED);
        }
        else {
            newOrder.state(OrderState.REJECTED);    
        }

        logger_.log(LogLevel.INFO, "Order processed: " 
            + newOrder.toStringShort() + ". Sending it to the OrderManager.");

        body = SerializationUtils.serialize(newOrder);
        this.getChannel().basicPublish("", orderManagerQueueName_, null, body);
    }

    /**
     * @brief Declare the queues. This is necessary because maybe they have not
     * been created yet
     */
    private void initQueues() throws IOException {
        Channel channel = this.getChannel();

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
    private StockDB stockDB_;

    // For performance stats
    private long elapsedTime_;
}