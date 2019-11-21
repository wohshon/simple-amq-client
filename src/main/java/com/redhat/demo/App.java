package com.redhat.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // System.out.println("Hello World!");
        // mvn exec:java -Dexec.mainClass="com.redhat.demo.App"
        // -D broker.url="failover://(amqp://192.168.0.110:5672)" -Dsend.queue=queue1
        // -Dsend.msg=helloworld! -Dsend.mode=SEND/RECV
        App client1 = new App(System.getProperty("broker.url"));
        // SEND or RECV
        String mode = System.getProperty("send.mode");

        client1.connect(mode);
    }

    Logger log = LoggerFactory.getLogger(this.getClass());
    private String brokerUrl;

    public App(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        log.info("connecting to " + brokerUrl);
    }

    private void send(Session session, String message, Queue queue) throws JMSException {
        MessageProducer sender = session.createProducer(queue);
        sender.setDeliveryMode(DeliveryMode.PERSISTENT);
        // for (int i = 0; i < 3; i++) {
        int i = 0;
        while (true) {
            try {
                // sender.send(session.createTextMessage(message));
                String text = message + "-"+(i++);
                sender.send(session.createTextMessage(text));
                log.info("Sent msg " + i + ": " + text);
                log.info("sleep for 2 s");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        //sender.send(session.createTextMessage("END"));
        //log.info("Sent msg END");
    }

    private void receive(Session session, Queue queue) throws JMSException {
        MessageConsumer consumer = session.createConsumer(queue);
        while (true) {
            TextMessage m = (TextMessage) consumer.receive(0);
            log.info(m.getText());
            if ("END".equals(m.getText())) {
                log.info("END");
                break;
            }
            if (m != null) {
                log.info("received message ="+m.getText() + " "+m.getJMSDestination().toString());
                //m.acknowledge();
            }
        }
    }
    private void connect(String mode) {
        Connection connection = null;

        ConnectionFactory connectionFactory = new JmsConnectionFactory("admin","admin",this.brokerUrl);

        try {
            connection = connectionFactory.createConnection();
            //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Session session = null;
            Queue queue = null;
            //String mode = System.getProperty("send.mode");
            if ("SEND".equals(mode)) {
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                queue = session.createQueue(System.getProperty("send.queue"));
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSS");
                Date dt = new Date();
                String message=System.getProperty("send.msg")+"-"+sdf.format(dt);
                    send(session, message, queue);
            }
            if ("RECV".equals(mode)) {
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                queue = session.createQueue(System.getProperty("send.queue"));
                connection.start();
                receive(session, queue);
            }
            //all done
            session.close();
            
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
               // Step 9. close the connection
                try {
                    connection.close();
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
         }

    }
}
