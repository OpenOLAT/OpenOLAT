/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.notifications.manager;

import jakarta.annotation.Resource;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.QueueConnection;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.apache.logging.log4j.Logger;
import org.olat.commons.coordinate.cluster.jms.JMSCompletionListener;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.NotificationsPushService;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NotificationsPushServiceImpl implements NotificationsPushService, MessageListener,
InitializingBean, DisposableBean {

	private static final Logger log = Tracing.createLoggerFor(NotificationsPushServiceImpl.class);
	
	@Resource(name="notificationsTopic")
	private Topic destination;
	private Session notificationsConsumerSession;
	private MessageConsumer consumer;
	private Session notificationsProducerSession;
	private MessageProducer producer;
	@Resource(name="notificationsConnectionFactory")
	private ConnectionFactory connectionFactory;
	private QueueConnection connection;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		//start the queue
		try {
			startQueue();
		} catch (JMSException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void destroy() throws Exception {
		closeJms();
	}

	private void startQueue() throws JMSException {
		connection = (QueueConnection)connectionFactory.createConnection();
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory={}", connectionFactory);

		notificationsConsumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = notificationsConsumerSession.createConsumer(destination);
		consumer.setMessageListener(this);
		
		notificationsProducerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = notificationsProducerSession.createProducer(destination);
	}
	
	private void closeJms() {
		if(consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		if(connection != null) {
			try {
				notificationsProducerSession.close();
				connection.stop();
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}
	
	public Topic getNotificationsTopic() {
		return destination;
	}
	
	@Override
	public void sendMessage(SubscriptionContext context, PublisherData data, OLATResourceable object, String operation) {
		try {
			synchronized(notificationsProducerSession) {
				MapMessage message = notificationsProducerSession.createMapMessage();
				message.setStringProperty("operation", operation);
				if(context != null) {
					message.setStringProperty(CONTEXT_RESOURCE_NAME, context.getResName());
					message.setLongProperty(CONTEXT_RESOURCE_ID, context.getResId());
					if(StringHelper.containsNonWhitespace(context.getSubidentifier())) {
						message.setStringProperty(CONTEXT_RESOURCE_SUBIDENT, context.getSubidentifier());
					}
				}
				if(data != null) {
					message.setStringProperty(PUBLISHER_TYPE, data.getType());
					message.setStringProperty(PUBLISHER_DATA, data.getData());
				}
				if(object != null) {
					message.setStringProperty(OBJECT_TYPE, object.getResourceableTypeName());
					message.setLongProperty(OBJECT_ID, object.getResourceableId());
				}
				producer.send(message, DeliveryMode.NON_PERSISTENT, 3, 5000, new JMSCompletionListener());
			}
		} catch (JMSException e) {
			log.error("", e);
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			if(message instanceof MapMessage mm) {
				String publisherType = mm.getStringProperty(PUBLISHER_TYPE);
				String publisherData = message.getStringProperty(PUBLISHER_DATA);
				String operation = mm.getStringProperty("operation");
				log.info("On message publisher type:{} data: {} operation:{}", publisherType, publisherData, operation);
				notificationsManager.markPublisherNews(publisherType, publisherData, null, false);
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			try {
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
