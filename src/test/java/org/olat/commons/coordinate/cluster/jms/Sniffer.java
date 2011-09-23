package org.olat.commons.coordinate.cluster.jms;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

public class Sniffer {

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) throws JMSException {
		String brokerUrl = null;
		String topicName = null;
		for(int i=0; i<args.length; i++) {
			if (args[i].equals("--brokerUrl")) {
				if (i+1>=args.length) {
					usage("No brokerUrl provided with --brokerUrl");
				}
				brokerUrl = args[++i];
			} else if (args[i].equals("--topicName")) {
				if (i+1>=args.length) {
					usage("No topicName provided with --topicName");
				}
				topicName = args[++i];
			} else {
				usage("Unknown/Unsupported parameter: "+args[i]);
			}
		}
		if (brokerUrl==null || brokerUrl.length()==0) {
			usage("No brokerUrl provided");
		}
		if (topicName==null || topicName.length()==0) {
			usage("No topicName provided");
		}
		go(brokerUrl, topicName);
	}
	
	private static void usage(String errorMsg) {
		System.out.println("Error: "+errorMsg);
		System.out.println("Usage: java org.olat.tools.jms.Sniffer --brokerUrl brokerUrl --topicName topicName [--topicName ...]");
		System.out.println("");
		System.out.println(" Hint: brokerUrl is something like tcp://localhost:61616");
		System.exit(1);
	}

	private static void go(String brokerUrl, String topicName) throws JMSException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(brokerUrl);
		Connection connection = connectionFactory.createConnection();
		ActiveMQTopic topic = new ActiveMQTopic(topicName);
		MessageConsumer consumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE).createConsumer(topic);
		connection.start();
		while(true) {
			Message m = consumer.receive();
			if (!(m instanceof ObjectMessage)) {
				System.out.println("Received a non-ObjectMessage: "+m);
			} else {
				ObjectMessage objMsg = (ObjectMessage)m;
				Object o = objMsg.getObject();
				if (!(o instanceof JMSWrapper)) {
					System.out.println("Received a non-JMSWrapper: "+o+", msg: "+m);
				} else {
					JMSWrapper jmsWrapper = (JMSWrapper)o;
					System.out.println("Received a JMSWrapper: time="+new Date()+", msgid="+jmsWrapper.getMsgId()+", nodeId="+jmsWrapper.getNodeId()+", event="+jmsWrapper.getMultiUserEvent()+", msg: "+m);
				}
			}
		}
	}

}
