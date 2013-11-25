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

import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.wsserver.TestState;

public class ExamPoolManagerProvider implements MessageListener {

	protected static final String JMS_RESPONSE_STATUS_PROPERTY_NAME = "response_status";
	protected static final String JMS_RESPONSE_STATUS_OK = "ok";
	protected static final String JMS_RESPONSE_STATUS_PARSE_EXCEPTION = "parse_exception";
	protected static final String JMS_RESPONSE_STATUS_QUERY_EXCEPTION = "query_exception";
	protected static final String JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION = "service_not_available";

	private final OLog log = Tracing.createLoggerFor(ExamPoolManagerProvider.class);
	private final ExamPoolManager examPoolManager;
	private TaskExecutorManager taskExecutorManager;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Queue examControlQueue;
	private Session session;
	private MessageConsumer consumer;
	private final LinkedList<Session> sessions = new LinkedList<Session>();

	public ExamPoolManagerProvider(ExamPoolManager examPoolManager) {
		this.examPoolManager = examPoolManager;
	}

	@Override
	public void onMessage(Message message) {
		try {
			
			log.debug("Got message : " + (message != null ? message.getClass() : "null"));
			
			final String correlationID = message.getJMSCorrelationID();
			final Destination replyTo = message.getJMSReplyTo();
			if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				final JMSExamMessage examMessage = (JMSExamMessage) objectMessage.getObject();
				taskExecutorManager.execute(new Runnable() {
					@Override
					public void run() {
						handleRemoteMessage(examMessage, correlationID, replyTo);
					}

				});
			}
		} catch (JMSException e) {
			log.error("error when receiving jms messages", e);
		} finally {
			releaseSession(session);
			DBFactory.getInstance().commitAndCloseSession();
		}

	}

	private void handleRemoteMessage(JMSExamMessage examMessage, String correlationID, Destination replyTo) {
		if (log.isDebug()) {
			log.debug("onSearchMessage, correlationID=" + correlationID + " , replyTo=" + replyTo + " , searchRequest=" + examMessage);
		}

		JMSExamMessageCommand command = examMessage.getCommand();

		Session localSession = null;
		try {
			ExamPool pool = null;

			switch (command) {
				case GET_EXAMPOOL: {
					if (examMessage.getTestSessionId() != null) {
						pool = examPoolManager.getExamPool(examMessage.getTestSessionId());
					} else {
						Long courseId = examMessage.getCourseId();
						String nodeIdent = examMessage.getNodeIdent();
						ICourse course = CourseFactory.loadCourse(courseId);
						CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
						pool = examPoolManager.getExamPool(course, courseNode);
					}
					break;
				}
				case CONTROLL_EXAM: {
					examPoolManager.controllExam(examMessage.getTestSessionId(), examMessage.getStudents(), examMessage.getTestState());
					break;
				}
				case ADD_STUDENT: {
					Long courseId = examMessage.getCourseId();
					String nodeIdent = examMessage.getNodeIdent();
					ICourse course = CourseFactory.loadCourse(courseId);
					CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
					TestState state = examMessage.getTestState();
					List<Identity> students = examMessage.getStudents();
					List<QTIResultSet> results = examMessage.getResults();
					Identity student = students.get(0);
					QTIResultSet resultSet = results.get(0);
					examPoolManager.addStudentToExamPool(course, courseNode, student, state, resultSet);
					break;
				}
				case CHANGE_STATE: {
					Long testSessionId = examMessage.getTestSessionId();
					TestState state = examMessage.getTestState();
					List<Identity> identities = examMessage.getStudents();
					examPoolManager.changeExamState(testSessionId, identities, state);
					break;
				}
				default:
					break;
			}
			

			localSession = acquireSession();
			Message responseMessage = localSession.createObjectMessage(pool);
			responseMessage.setJMSCorrelationID(correlationID);
			responseMessage.setStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME, JMS_RESPONSE_STATUS_OK);
			MessageProducer producer = localSession.createProducer(replyTo);
			if (log.isDebug()) {
				log.debug("onSearchMessage, send ResponseMessage=" + responseMessage + " to replyTo=" + replyTo);
			}
			producer.send(responseMessage);
			producer.close();
			return;

		} catch (JMSException e) {
			log.error("error when receiving jms messages", e);
			// do not throw exceptions here throw new OLATRuntimeException();
			//		} catch (ServiceNotAvailableException sex) {
			//			sendErrorResponse(JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION, correlationID, replyTo);
			//		} catch (ParseException pex) {
			//			sendErrorResponse(JMS_RESPONSE_STATUS_PARSE_EXCEPTION, correlationID, replyTo);
			//		} catch (QueryException qex) {
			//			sendErrorResponse(JMS_RESPONSE_STATUS_QUERY_EXCEPTION, correlationID, replyTo);
		} catch (Throwable th) {
			log.error("error at ClusteredSearchProvider.receive()", th);
			return;// signal search not available
			// do not throw exceptions throw new OLATRuntimeException();
		} finally {
			releaseSession(localSession);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}


	public void setConnectionFactory(ConnectionFactory conFac) {
		connectionFactory = conFac;
	}

	public void setSearchQueue(Queue examControlQueue) {
		this.examControlQueue = examControlQueue;
	}

	/**
	 * [used by Spring]
	 */
	public void setTaskExecutorManager(TaskExecutorManager taskExecutorManager) {
		this.taskExecutorManager = taskExecutorManager;
	}

	public void springInit() throws JMSException {
		connection = connectionFactory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(examControlQueue);
		consumer.setMessageListener(this);
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory=" + connectionFactory);
	}
	

	public void springStop() throws JMSException {
		closeQueue();
	}
	
	private void closeQueue() {
		if(consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}

	private synchronized Session acquireSession() throws JMSException {
		if (sessions.size() == 0) {
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} else {
			return sessions.getFirst();
		}
	}

	private synchronized void releaseSession(Session currentSession) {
		if (currentSession == null) {
			return;
		}
		sessions.addLast(currentSession);
	}

	private void sendErrorResponse(String jmsResponseStatus, String correlationID, Destination replyTo) {
		Session currentSession = null;
		try {
			currentSession = acquireSession();
			Message responseMessage = currentSession.createObjectMessage();
			responseMessage.setJMSCorrelationID(correlationID);
			responseMessage.setStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME, jmsResponseStatus);
			MessageProducer producer = currentSession.createProducer(replyTo);
			if (log.isDebug()) {
				log.debug("onSearchMessage, send ResponseMessage=" + responseMessage + " to replyTo=" + replyTo);
			}
			producer.send(responseMessage);
			producer.close();
			return;

		} catch (JMSException e) {
			log.error("error when receiving jms messages", e);
			return; //signal search not available
		} finally {
			releaseSession(currentSession);
		}
	}
}
/*
history:

$Log: ExamPoolManagerProvider.java,v $
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