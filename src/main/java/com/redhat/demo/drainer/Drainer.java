package com.redhat.demo.drainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.apache.activeio.journal.Journal;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.journal.JournalPersistenceAdapter;
import org.apache.activemq.store.journal.JournalPersistenceAdapterFactory;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;

/**
 * Drainer
 */
public class Drainer {

    public static void main(final String[] args) {
        final Drainer drainer = new Drainer();
        drainer.init();
    }

    private final BrokerService broker;
    private ConnectionFactory sourceConnectionFactory, targetConnectionFactory;
    private Connection sourceConnection, targetConnection;

    public Drainer() {

        broker = new BrokerService();

        //final PersistenceAdapter persistenceAdapter = new KahaDBPersistenceAdapter();
        // persistenceAdapter.setDirectory(new File(System.getProperty("kahadb.dir")));
        final PersistenceAdapter persistenceAdapter = new MemoryPersistenceAdapter();
        //JournalPersistenceAdapter jpa = new JournalPersistenceAdapter();
        //jpa.setDirectory(new File("/home/virtuser/amq-broker-7.5.0/brokers/broker1/data/journal"));
        broker.setDataDirectory("/home/virtuser/amq-broker-7.5.0/brokers/broker1/data");
        
        persistenceAdapter.setDirectory(new File("/home/virtuser/amq-broker-7.5.0/brokers/broker1/data/journal"));

        try {
            broker.addConnector("tcp://192.168.0.110:7672");
            broker.setPersistenceAdapter(persistenceAdapter);

        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void init() {
        try {
            System.out.println(broker.getTransportConnectors());
            targetConnectionFactory = new ActiveMQConnectionFactory(
                    broker.getTransportConnectorByScheme("tcp").getConnectUri().toString());
            broker.start();
            targetConnection = targetConnectionFactory.createConnection();
            
            final Session targetSession = targetConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            targetConnection.start();
            System.out.println(broker.getBrokerDataDirectory());
            ActiveMQDestination[] destinations= broker.getBroker().getDestinations();
            System.out.println("number of dest: "+destinations.length);
            System.out.println("dest "+destinations[0].getDestinationTypeAsString());
            System.out.println("dest "+destinations[1].getQualifiedName());
            //targetSession.crea
            System.out.println(targetSession);
            
        } catch (IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}