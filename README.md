## RECV
mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="failover://(amqp://192.168.0.110:5672)" -Dsend.queue=queue2 -Dsend.mode=RECV

## SEND

mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="failover://(amqp://192.168.0.110:5672)" -Dsend.queue=queue2 -Dsend.msg=helloworld! -Dsend.mode=SEND

## with SSL:

    mvn exec:java -Dexec.mainClass="com.redhat.demo.App" -D broker.url="amqps://192.168.0.110:5673?transport.trustStoreLocation=./client.ts&transport.trustStorePassword=password&transport.verifyHost=false" -Dsend.queue=queue2 -Dsend.msg=sslHello -Dsend.mode=SEND


## Setting up

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
