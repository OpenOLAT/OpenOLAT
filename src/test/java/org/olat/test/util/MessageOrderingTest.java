/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
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
