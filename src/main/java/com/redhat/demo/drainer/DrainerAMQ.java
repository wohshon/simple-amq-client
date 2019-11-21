package com.redhat.demo.drainer;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.protocol.core.impl.CoreProtocolManagerFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.apache.qpid.jms.JmsConnectionFactory;

/**
 * DrainerAMQ
 */
public class DrainerAMQ {

    public static void main(String[] args) {

        SecurityConfiguration securityConfig = new SecurityConfiguration();
        securityConfig.addUser("admin", "admin");
        securityConfig.addRole("admin", "amq");
        securityConfig.setDefaultUser("admin");
        ActiveMQJAASSecurityManager securityManager = new ActiveMQJAASSecurityManager(InVMLoginModule.class.getName(),
                securityConfig);
        ActiveMQServer server = null;
        try {
            server = ActiveMQServers.newActiveMQServer("broker.xml", null, securityManager);
            server.start();
            System.out.println("started server");

            InitialContext initialContext = null;
            // Step 3. Create an initial context to perform the JNDI lookup.
            initialContext = new InitialContext();

            // Step 4. Look-up the JMS queue
            Queue queue = (Queue) initialContext.lookup("queue/exampleQueue");

            // Step 5. Look-up the JMS connection factory
            ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

            Connection connection = cf.createConnection("admin","admin");
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            String mode=System.getProperty("mode");
            if ("SEND".equals(mode)) {
                MessageProducer producer = session.createProducer(queue);
                TextMessage message = session.createTextMessage("Hello sent at " + new Date());
                System.out.println(">>>>>>>Sending message: " + message.getText());
                producer.send(message);
            }   
            else if ("RECV".equals(mode)) {
                MessageConsumer messageConsumer = session.createConsumer(queue);
                connection.start();
                TextMessage messageReceived = (TextMessage) messageConsumer.receive(0);
                System.out.println(">>>>>>>Received message:" + messageReceived.getText());
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } finally {
            try {
                if (server.isActive())
                     server.isStarted();
                System.out.println("stopped server");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}