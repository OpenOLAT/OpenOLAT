/**
 * <a href="http://www.openolat.org">
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.wsserver.TestState;

public class ExamPoolManagerProxy extends ExamPoolManager {

	protected static final String JMS_RESPONSE_STATUS_PROPERTY_NAME = "response_status";
	protected static final String JMS_RESPONSE_STATUS_OK = "ok";
	protected static final String JMS_RESPONSE_STATUS_PARSE_EXCEPTION = "parse_exception";
	protected static final String JMS_RESPONSE_STATUS_QUERY_EXCEPTION = "query_exception";
	protected static final String JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION = "service_not_available";

	private final AtomicLong messageCount = new AtomicLong(0); //counter for this cluster node
	private ConnectionFactory connectionFactory;
	private Queue examControllQueue;
	private long receiveTimeout = 45000;
	private long timeToLive = 45000;
	private Connection connection;
	private final LinkedList<Destination> tempQueues = new LinkedList<Destination>();
	private final LinkedList<Session> sessions = new LinkedList<Session>();

	private final static int DEFAULT_SLEEPTIME = 25;

	public ExamPoolManagerProxy() {
		super();
		log.info("Created new ExamPoolManagerProxy");
	}

	public void setConnectionFactory(ConnectionFactory conFac) {
		connectionFactory = conFac;
	}

	public void setSearchQueue(Queue searchQueue) {
		this.examControllQueue = searchQueue;
	}

	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void springInit() throws JMSException {
		connection = connectionFactory.createConnection();
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory=" + connectionFactory);
	}
	
	public void springStop() throws JMSException {
		closeQueue();
	}
	
	private void closeQueue() {
		if(connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}

	/**
	 * Uses Request/reply mechanism for synchronous operation.
	 * 
	 * This method should be called basically in all overriden methods /
	 * whenever a operation needs to make changes cluster-wide changes or to
	 * reload changes in the cluster [signaled by a recived {@link ExamEvent}]
	 * 
	 * @param message
	 *            the message to send
	 * @return the new or updated {@link ExamPool}
	 */
	private ExamPool callJMSExamPoolManager(JMSExamMessage message) {
		long begin = System.currentTimeMillis();
		ExamPool result = null;
		if (log.isDebug()) {
			log.debug("RequestMessage=" + message);
		}
		Session session = null;
		try {
			session = acquireSession();
			if (log.isDebug()) {
				log.debug("poolManagerSession=" + session);
			}
			Message requestMessage = session.createObjectMessage(message);
			Message returnedMessage = callJMXExamPoolManagerProvider(session, requestMessage);
			messageCount.incrementAndGet();
			if (returnedMessage != null) {
				String responseStatus = returnedMessage.getStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME);
				if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_OK)) {
					result = (ExamPool) ((ObjectMessage) returnedMessage).getObject();
				} else {
					log.warn("poolManager: receive unkown responseStatus=" + responseStatus);
				}
			} else {
				//null returnedMessage is ok ?!?
			}
		} catch (JMSException e) {
			log.error("Unable to communicate in cluster", e);
		} finally {
			releaseSession(session);
		}
		if(log.isDebug()){			
			log.debug("Request took: " + (System.currentTimeMillis() - begin));
		}

		return result;
	}

	private Message callJMXExamPoolManagerProvider(Session session, Message message) throws JMSException {
		Destination replyQueue = acquireTempQueue(session);
		if (log.isDebug()) {
			log.debug("doSearchRequest replyQueue=" + replyQueue);
		}
		try {
			MessageConsumer responseConsumer = session.createConsumer(replyQueue);

			message.setJMSReplyTo(replyQueue);
			String correlationId = createRandomString();
			message.setJMSCorrelationID(correlationId);

			MessageProducer producer = session.createProducer(examControllQueue);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			producer.setTimeToLive(timeToLive);
			if (log.isDebug()) {
				log.debug("Sending search request message with correlationId=" + correlationId);
			}
			producer.send(message);
			producer.close();

			Message returnedMessage = null;
			final long start = System.currentTimeMillis();
			while (true) {
				final long diff = (start + receiveTimeout) - System.currentTimeMillis();
				if (diff <= 0) {
					// timeout
					log.info("Timeout in search. Remaining time zero or negative.");
					break;
				}
				if (log.isDebug()) {
					log.debug("doSearchRequest: call receive with timeout=" + diff);
				}
				returnedMessage = responseConsumer.receive(diff);
				if (returnedMessage == null) {
					// timeout case, we're stopping now with a reply...
					log.info("Timeout in search. Repy was null.");
					break;
				} else if (!correlationId.equals(returnedMessage.getJMSCorrelationID())) {
					// we got an old reply from a previous search request
					log.info("Got a response with a wrong correlationId. Ignoring and waiting for the next");
					try {
						Thread.sleep(DEFAULT_SLEEPTIME);
					} catch (InterruptedException e) {
						log.warn("Waiting for message interrupted.", e);
					}
					continue;
				} else {
					// we got a valid reply
					break;
				}
			}
			responseConsumer.close();
			if (log.isDebug()) {
				log.debug("doSearchRequest: returnedMessage=" + returnedMessage);
			}
			return returnedMessage;
		} finally {
			releaseTempQueue(replyQueue);
		}
	}

	private String createRandomString() {
		Random random = new Random(System.currentTimeMillis());
		long randomLong = random.nextLong();
		return Long.toHexString(randomLong);
	}

	private synchronized Destination acquireTempQueue(Session session) throws JMSException {
		if (tempQueues.size() == 0) {
			if (session == null) {
				Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination tempQ = s.createTemporaryQueue();
				s.close();
				return tempQ;
			}
			return session.createTemporaryQueue();
		} else {
			return tempQueues.removeFirst();
		}
	}

	private synchronized Session acquireSession() throws JMSException {
		if (sessions.size() == 0) {
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} else {
			return sessions.removeFirst();
		}
	}

	private synchronized void releaseTempQueue(Destination tempQueue) {
		if (tempQueue == null) {
			return;
		}
		tempQueues.addLast(tempQueue);
	}

	private synchronized void releaseSession(Session session) {
		if (session == null) {
			return;
		}
		sessions.addLast(session);
	}

	@Override
	protected ExamPool getExamPool(ICourse course, CourseNode courseNode) {
		ExamPool pool = null;
		JMSExamMessage message = new JMSExamMessage(JMSExamMessageCommand.GET_EXAMPOOL, null, course.getResourceableId(), courseNode.getIdent(), null, null,
				null);
		pool = callJMSExamPoolManager(message);
		return pool;
	}

	@Override
	protected ExamPool getExamPool(Long testSessionId) {
		ExamPool pool = null;
		JMSExamMessage message = new JMSExamMessage(JMSExamMessageCommand.GET_EXAMPOOL, testSessionId, null, null, null, null, null);
		pool = callJMSExamPoolManager(message);
		return pool;
	}

	@Override
	public Long addStudentToExamPool(ICourse course, CourseNode courseNode, Identity student, TestState state, QTIResultSet resultSet) {
		List<Identity> students = new ArrayList<Identity>();
		students.add(student);
		List<QTIResultSet> results = new ArrayList<QTIResultSet>();
		results.add(resultSet);
		JMSExamMessage message = new JMSExamMessage(JMSExamMessageCommand.ADD_STUDENT, null, course.getResourceableId(), courseNode.getIdent(), students,
				results, state);
		callJMSExamPoolManager(message);
		return null;
	}

	@Override
	public void controllExam(Long testSessionId, List<Identity> selectedIdentities, TestState state) {
		JMSExamMessage message = new JMSExamMessage(JMSExamMessageCommand.CONTROLL_EXAM, testSessionId, null, null, selectedIdentities, null, state);
		callJMSExamPoolManager(message);
	}

	@Override
	public void changeExamState(Long testSessionId, List<Identity> identities, TestState state) {
		JMSExamMessage message = new JMSExamMessage(JMSExamMessageCommand.CHANGE_STATE, testSessionId, null, null, identities, null, state);
		callJMSExamPoolManager(message);

	}

}
/*
history:

$Log: ExamPoolManagerProxy.java,v $
Revision 1.4  2012-06-13 10:18:46  blaw
OLATCE-2007
OLATCE-2290
OLATCE-2189
OLATCE-1425
* improved performance and logging for exam-control

Revision 1.3  2012-04-10 13:37:02  blaw
OLATCE-1425
* more logging

Revision 1.2  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/