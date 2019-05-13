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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.searcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

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
import javax.jms.TextMessage;

import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.search.QueryException;
import org.olat.search.SearchResults;
import org.olat.search.ServiceNotAvailableException;


/**
 * 
 * Description:<br>
 * This is a client side search proxy - delegates the search to the remote searcher.
 * 
 * <P>
 * Initial Date:  03.06.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class SearchClientProxy implements SearchClient {
	
	private static final Logger log = Tracing.createLoggerFor(SearchClientProxy.class);
	
	protected static final String JMS_RESPONSE_STATUS_PROPERTY_NAME = "response_status";
	protected static final String JMS_RESPONSE_STATUS_OK = "ok";
	protected static final String JMS_RESPONSE_STATUS_PARSE_EXCEPTION = "parse_exception";
	protected static final String JMS_RESPONSE_STATUS_QUERY_EXCEPTION = "query_exception";
	protected static final String JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION = "service_not_available";
	
	private long queryCount_ = 0; //counter for this cluster node
	private ConnectionFactory connectionFactory_;
	private Queue searchQueue_;
	private long receiveTimeout_ = 45000;
	private long timeToLive_ = 45000;
	private Connection connection_;
	private LinkedList<Destination> tempQueues_ = new LinkedList<>();
	private LinkedList<Session> sessions_ = new LinkedList<>();
	
	/**
	 * [used by spring]	 
	 */
	public SearchClientProxy() {
		//default constructor		
	}
	
	public void setConnectionFactory(ConnectionFactory conFac) {
		connectionFactory_ = conFac;
	}
	
	public void setSearchQueue(Queue searchQueue) {
		this.searchQueue_ = searchQueue;
	}

	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout_ = receiveTimeout;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive_ = timeToLive;
	}

	public void springInit() throws JMSException {
		connection_ = connectionFactory_.createConnection();
		connection_.start();
		log.info("springInit: JMS connection started with connectionFactory=" + connectionFactory_);
	}
	
	private synchronized Destination acquireTempQueue(Session session) throws JMSException {
		if (tempQueues_.isEmpty()) {
			if (session==null) {
				Session s = connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination tempQ = s.createTemporaryQueue();
				s.close();
				return tempQ;
			}
			return session.createTemporaryQueue();
		} else {
			return tempQueues_.removeFirst();
		}
	}
	
	private synchronized Session acquireSession() throws JMSException {
		if (sessions_.isEmpty()) {
			return connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} else {
			return sessions_.removeFirst();
		}
	}
	
	private synchronized void releaseTempQueue(Destination tempQueue) {
		if (tempQueue==null) {
			return;
		}
		tempQueues_.addLast(tempQueue);
	}
	
	private synchronized void releaseSession(Session session) {
		if (session==null) {
			return;
		}
		sessions_.addLast(session);
	}

	/**
	 * Uses Request/reply mechanism for synchronous operation.
	 * @see org.olat.search.service.searcher.OLATSearcher#doSearch(java.lang.String, org.olat.core.id.Identity, org.olat.core.id.Roles, boolean)
	 */
	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, boolean doHighlighting) throws ServiceNotAvailableException, ParseException, QueryException {
		boolean isDebug = log.isDebugEnabled();
		if(isDebug){
			log.debug("STARTqueryString=" + queryString);
		}
		SearchRequest searchRequest = new SearchRequest(queryString, condQueries, identity.getKey(), roles, locale, firstResult, maxResults, doHighlighting);
		Session session = null;
		try {
			session = acquireSession();
			if(isDebug){
				log.debug("doSearch session=" + session);
			}
			Message requestMessage = session.createObjectMessage(searchRequest);
			Message returnedMessage = doSearchRequest(session, requestMessage);
			queryCount_++;
			if(returnedMessage!=null){
				String responseStatus = returnedMessage.getStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME);
				if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_OK)) {
				  SearchResults searchResult = (SearchResults)((ObjectMessage)returnedMessage).getObject();
				  if(isDebug){
					  log.debug("ENDqueryString=" + queryString);
					}
				  return searchResult;							
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_PARSE_EXCEPTION)) {
					throw new ParseException("can not parse query=" + queryString);
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_QUERY_EXCEPTION)) {
					throw new QueryException("invalid query=" + queryString);
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION)) {
					throw new ServiceNotAvailableException("Remote search service not available" + queryString);
				} else {
					log.warn("doSearch: receive unkown responseStatus=" + responseStatus);
					return null;
				}
			} else {
				//null returnedMessage
				throw new ServiceNotAvailableException("communication error with JMS - cannot receive messages!!!");
			}				
		} catch (JMSException e) {
			log.error("Search failure I",e);
			throw new ServiceNotAvailableException("communication error with JMS - cannot send messages!!!");
		} finally {
			releaseSession(session);
		}
	}
	
	@Override
	public List<Long> doSearch(String queryString, List<String> condQueries,
			Identity identity, Roles roles, Locale locale, int firstResult, int maxResults,
			SortKey... orderBy) throws ServiceNotAvailableException, ParseException,
			QueryException {
		// only goes local
		return Collections.emptyList();
	}

	/**
	 * Uses Request/reply mechanism for synchronous operation.
	 * @see org.olat.search.service.searcher.OLATSearcher#spellCheck(java.lang.String)
	 */
	public Set<String> spellCheck(String query) throws ServiceNotAvailableException	{
		Session session = null;
		try {
			session = acquireSession();
			TextMessage requestMessage = session.createTextMessage(query);
			Message returnedMessage = doSearchRequest(session, requestMessage);			
			if(returnedMessage!=null){
				@SuppressWarnings("unchecked")
				List<String> spellStringList = (List<String>)((ObjectMessage)returnedMessage).getObject();	
				return new HashSet<>(spellStringList);		
			} else {
				//null returnedMessage
				throw new ServiceNotAvailableException("spellCheck, communication error with JMS - cannot receive messages!!!");
			}				
		} catch (JMSException e) {
			throw new ServiceNotAvailableException("spellCheck, communication error with JMS - cannot send messages!!!");
		} catch (ServiceNotAvailableException e) {			
			throw e;
		} catch (Throwable th) {			
			throw new OLATRuntimeException("ClusteredSearchRequester.spellCheck() error!!!", th);
		} finally{
			releaseSession(session);
		}
	}
	
	private String createRandomString() {
		Random random = new Random(System.currentTimeMillis());
		long randomLong = random.nextLong();
		return Long.toHexString(randomLong);
	}

	/**
	 * Returns the queryCount number for this cluster node.
	 * @see org.olat.search.service.searcher.OLATSearcher#getQueryCount()
	 */
	public long getQueryCount() {		
		return queryCount_;
	}

	public void stop() {
		try {
			for (Iterator<Session> iterator = sessions_.iterator(); iterator.hasNext();) {
				Session session = iterator.next();
				session.close();
			}
			connection_.close();
			log.info("ClusteredSearchRequester stopped");
		} catch (JMSException e) {
			log.error("Exception in stop ClusteredSearchRequester, ",e);
		}
	}
	
	private Message doSearchRequest(Session session, Message message) throws JMSException {
		Destination replyQueue = acquireTempQueue(session);
		if(log.isDebugEnabled()){
			log.debug("doSearchRequest replyQueue=" + replyQueue);
		}
		try{
			MessageConsumer responseConsumer = session.createConsumer(replyQueue);
			
			message.setJMSReplyTo(replyQueue);
			String correlationId = createRandomString();
			message.setJMSCorrelationID(correlationId);
	  
			MessageProducer producer = session.createProducer(searchQueue_);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			producer.setTimeToLive(timeToLive_);
			if (log.isDebugEnabled()) {
				log.debug("Sending search request message with correlationId="+correlationId);
			}
			producer.send(message);
			producer.close();
	
			Message returnedMessage = null;
			final long start = System.currentTimeMillis();
			while(true) {
				final long diff = (start + receiveTimeout_) - System.currentTimeMillis(); 
				if (diff<=0) {
					// timeout
					log.info("Timeout in search. Remaining time zero or negative.");
					break;
				}
				if (log.isDebugEnabled()) {
					log.debug("doSearchRequest: call receive with timeout=" + diff);
				}
				returnedMessage = responseConsumer.receive(diff);
				if (returnedMessage==null) {
					// timeout case, we're stopping now with a reply...
					log.info("Timeout in search. Repy was null.");
					break;
				} else if (!correlationId.equals(returnedMessage.getJMSCorrelationID())) {
					// we got an old reply from a previous search request
					log.info("Got a response with a wrong correlationId. Ignoring and waiting for the next");
					continue;
				} else {
					// we got a valid reply
					break;
				}
			}
			responseConsumer.close();
			if (log.isDebugEnabled()) {
				log.debug("doSearchRequest: returnedMessage=" + returnedMessage);
			}
			return returnedMessage;
		} finally {
			releaseTempQueue(replyQueue);
		}
	}
}