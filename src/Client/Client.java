package client;

import java.util.concurrent.TimeoutException;
import java.io.IOException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.SerializationUtils;

import common.Order;

public class Client {
    private static final String QUEUE_NAME = "FIRST_QUEUE";
    public static void main(String[] argv) throws IOException, 
                                                  TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World!";
        Order order = new Order(2, 10);
        byte[] data = SerializationUtils.serialize(order);

        channel.basicPublish("", QUEUE_NAME, null, data);
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}