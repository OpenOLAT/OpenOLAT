package org.olat.test.util;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class MessageOrderingTest implements MessageListener {

	private int counter_ = 0;
	
	public static void main(String[] args) throws Exception {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL("tcp://localhost:61616");
		
		Connection connection = connectionFactory.createQueueConnection();
		Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Session session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Destination destination = new ActiveMQQueue("/atestqueue");
		
		MessageProducer producer = session1.createProducer(destination);
		
		MessageConsumer consumer = session2.createConsumer(destination);
		
		consumer.setMessageListener(new MessageOrderingTest());
		connection.start();
		
		for(int i=0; i<10000; i++) {
			MapMessage message = session1.createMapMessage();
			message.setInt("Counter", i);
			producer.send(message);
			System.out.println("Sent counter="+i);
		}
	}

	public void onMessage(Message arg0) {
		try{
			if (!(arg0 instanceof MapMessage)) {
				new Exception("Wrong message type: "+arg0).printStackTrace(System.out);
				System.exit(1);
			}
			MapMessage message = (MapMessage)arg0;
			int receivedCounter = message.getInt("Counter");
			System.out.println("Received counter="+receivedCounter);
			if (receivedCounter!=counter_) {
				new Exception("Out of order, expected "+counter_+", but got "+receivedCounter).printStackTrace(System.out);
				System.exit(1);
			}
			counter_++;
		} catch(JMSException e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}
}
