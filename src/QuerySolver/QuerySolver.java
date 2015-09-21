package querySolver;

import java.util.UUID;
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
import common.OrderDB;
import logger.Logger;
import logger.LogLevel;

import java.io.IOException;


public class QuerySolver extends DefaultConsumer {
    public QuerySolver(Channel channel) throws IllegalArgumentException,
                                               IOException {    
        super(channel);
        logger_ = Logger.getInstance();
        config_ = ConfigParser.getInstance();
        orderDB_ = new OrderDB(config_.get("ORDER", "order-db-directory"));
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        UUID orderKey = (UUID) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.DEBUG, "Query received. Key: " 
            + orderKey.toString());

        Order order = orderDB_.get(orderKey);
        logger_.log(LogLevel.INFO, "Order " + orderKey.toString() 
            + " - State: " + order.state().toString());
    }

    private Logger logger_;
    private ConfigParser config_;
    private OrderDB orderDB_;
}