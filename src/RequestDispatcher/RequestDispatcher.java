package requestDispatcher;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;

import java.util.concurrent.TimeoutException;
import java.io.IOException;

import common.Order;
import org.apache.commons.lang3.SerializationUtils;

public class RequestDispatcher {
    private static final String QUEUE_NAME = "FIRST_QUEUE";

    public static void main(String[] argv) throws IOException, 
                                                  TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, 
                                       Envelope envelope, 
                                       AMQP.BasicProperties properties, 
                                       byte[] body) throws IOException {

                Order newOrder = (Order) SerializationUtils.deserialize(body);
                System.out.println("Order: " + newOrder.toString());
            }
        };

        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}