package auditLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang3.SerializationUtils;

import common.Order;
import logger.Logger;
import logger.LogLevel;

import java.io.IOException;
import java.io.FileWriter;


public class AuditLogger extends DefaultConsumer {
    public AuditLogger(Channel channel, 
                       String auditLogFile) throws IOException {
        super(channel);
        logger_ = Logger.getInstance();
        // Open log file in append mode
        File file = new File(auditLogFile);
        dateFormat_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if (! file.exists()) {
            logger_.log(LogLevel.WARNING, 
                "Audit log doesn't exists." 
                + " Proceed to create it. AuditLogFile: " + auditLogFile);
            file.createNewFile();
        }
        writer_ = new FileWriter(auditLogFile, true);
    }

    @Override
    public void handleDelivery(String consumerTag, 
                               Envelope envelope, 
                               AMQP.BasicProperties properties, 
                               byte[] body) throws IOException {
        Order newOrder = (Order) SerializationUtils.deserialize(body);
        logger_.log(LogLevel.DEBUG, "Order received: " + newOrder.stringID());
        writer_.write(this.generateAuditEntry(newOrder) + "\n");
        writer_.flush();
    }

    private String generateAuditEntry(Order order) {
        Date date = new Date();
        String entry = dateFormat_.format(date) + " - ";
        entry += "Order ID: " + order.stringID();
        return entry;
    }

    private Logger logger_;
    private FileWriter writer_;
    private DateFormat dateFormat_;
}