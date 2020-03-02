## RECV
mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="failover://(amqp://192.168.0.110:5672)" -Dsend.queue=queue2 -Dsend.mode=RECV

## SEND

mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="failover://(amqp://192.168.0.110:5672)" -Dsend.queue=queue2 -Dsend.msg=helloworld! -Dsend.mode=SEND

## with SSL:

    mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="amqps://192.168.0.110:5673?transport.trustStoreLocation=./client.ts&transport.trustStorePassword=password&transport.verifyHost=false" -Dsend.queue=queue2 -Dsend.msg=sslHello -Dsend.mode=SEND


## Setting up

TL;DR

1) Generate certs and ts ks

2) create secret based on the ts and ks 
    
    oc secret new ex-aao-amqp-secret broker.ks client.ts


3) deploy amq operator - broker , address 
(scaledown component will be auto deployed)

add to sa (sa will be created when operator is deployed)
    
    oc secrets add sa/amq-broker-operator secret/ex-aao-amqp-secret


4) create service

5) expose route and change to tls termination passthrough


## Details here:

Sample commands to generate cert... 


        keytool -genkey -alias broker -keyalg RSA -keystore broker.ks
        keytool -export -alias broker -keystore broker.ks -file broker_cert
        keytool -genkey -alias client -keyalg RSA -keystore client.ks
        keytool -import -alias broker -keystore client.ts -file broker_cert



broker.xml at the amq broker:

    <acceptor name="amqps">tcp://192.168.0.110:5673?sslEnabled=true;keyStorePath=etc/broker.ks;keyStorePassword=password;needClientAuth=false</acceptor>

To use SSL with failover uri; if you are using a self signed cert, you need to specify the CN as the domain name. e.g. dev.demo.com
use the hosts file if you have to 

Because when the amqps scheme is used to specify an SSL/TLS connection, the hostname segment from the URI can be used by the JVM’s TLS SNI (Server Name Indication) extension to communicate the desired server hostname during a TLS handshake. The SNI extension is automatically included if a Fully Qualified Domain Name (for example, "myhost.mydomain") is specified, but not when an unqualified name (for example, "myhost") or a bare IP address is used.

no need to set verifyHost to false if you have a proper domain name

    mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="failover:(amqps://dev.demo.com:5673?transport.trustStoreLocation=./client.ts&transport.trustStorePassword=psword)" -Dsend.queue=queue2 -Dsend.msg=sslHello -Dsend.mode=RECV


##OCP 


        oc secret new ex-aao-amqp-secret broker.ks client.ts

        oc secrets add sa/amq-broker-operator secret/ex-aao-amqp-secret


        mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="amqps://amqp-integration.apps.cluster-sgp-fa8b.sgp-fa8b.example.opentlc.com:443?transport.trustStoreLocation=./client.ts&transport.trustStorePassword=password&transport.keyStoreLocation=./client.ks&transport.keyStorePassword=password&transport.verifyHost=false" -Dsend.queue=myAddress0.myQueue0 -Dsend.msg=sslHello -Dsend.mode=RECV



## Sample yaml for CR

        spec:
        acceptors:
            - name: amqp
            needClientAuth: false
            port: 5671
            protocols: amqp
            sslEnabled: true
            sslSecret: ex-aao-amqp-secret
            verifyHost: false

## Service


        spec:
        clusterIP: None
        ports:
        - name: amqp
            port: 5671
            protocol: TCP
            targetPort: 5671
        publishNotReadyAddresses: true
        selector:
            ActiveMQArtemis: ex-aao
            application: ex-aao-app
        sessionAffinity: None
        type: ClusterIP
