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
* <p>
*/ 

package org.olat.search.service.searcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

import org.apache.lucene.queryParser.ParseException;
import org.olat.core.commons.services.search.QueryException;
import org.olat.core.commons.services.search.SearchResults;
import org.olat.core.commons.services.search.ServiceNotAvailableException;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;


/**
 * 
 * Description:<br>
 * This is a client side search proxy - delegates the search to the remote searcher.
 * 
 * <P>
 * Initial Date:  03.06.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class SearchClientProxy {
	
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
	private LinkedList<Destination> tempQueues_ = new LinkedList<Destination>();
	private LinkedList<Session> sessions_ = new LinkedList<Session>();
	
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
		Tracing.logInfo("springInit: JMS connection started with connectionFactory=" + connectionFactory_, SearchClientProxy.class);
	}
	
	private synchronized Destination acquireTempQueue(Session session) throws JMSException {
		if (tempQueues_.size()==0) {
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
		if (sessions_.size()==0) {
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
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, int firstResult, int maxResults, boolean doHighlighting) throws ServiceNotAvailableException, ParseException, QueryException {
		boolean isDebug = Tracing.isDebugEnabled(SearchClientProxy.class);
		if(isDebug){
			Tracing.logDebug("STARTqueryString=" + queryString,SearchClientProxy.class);
		}
		SearchRequest searchRequest = new SearchRequest(queryString, condQueries, identity.getKey(), roles, firstResult, maxResults, doHighlighting);
		Session session = null;
		try {
			session = acquireSession();
			if(isDebug){
				Tracing.logDebug("doSearch session=" + session,SearchClientProxy.class);
			}
			Message requestMessage = session.createObjectMessage(searchRequest);
			Message returnedMessage = (Message) doSearchRequest(session, requestMessage);
			queryCount_++;
			if(returnedMessage!=null){
				String responseStatus = returnedMessage.getStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME);
				if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_OK)) {
				  SearchResults searchResult = (SearchResults)((ObjectMessage)returnedMessage).getObject();
				  if(isDebug){
					  Tracing.logDebug("ENDqueryString=" + queryString,SearchClientProxy.class);
					}
				  return searchResult;							
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_PARSE_EXCEPTION)) {
					throw new ParseException("can not parse query=" + queryString);
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_QUERY_EXCEPTION)) {
					throw new QueryException("invalid query=" + queryString);
				} else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION)) {
					throw new ServiceNotAvailableException("Remote search service not available" + queryString);
				} else {
					Tracing.logWarn("doSearch: receive unkown responseStatus=" + responseStatus, SearchClientProxy.class);
					return null;
				}
			} else {
				//null returnedMessage
				throw new ServiceNotAvailableException("communication error with JMS - cannot receive messages!!!");
			}				
		} catch (JMSException e) {
			Tracing.logError("Search failure I",e,SearchClientProxy.class);
			throw new ServiceNotAvailableException("communication error with JMS - cannot send messages!!!");
		} finally {
			releaseSession(session);
		}
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
			Message returnedMessage = (Message) doSearchRequest(session, requestMessage);			
			if(returnedMessage!=null){
				List<String> spellStringList = (List<String>)((ObjectMessage)returnedMessage).getObject();	
			  return new HashSet<String>(spellStringList);		
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
			for (Iterator iterator = sessions_.iterator(); iterator.hasNext();) {
				Session session = (Session) iterator.next();
				session.close();
			}
			connection_.close();
			Tracing.logInfo("ClusteredSearchRequester stopped",SearchClientProxy.class);
		} catch (JMSException e) {
			Tracing.logError("Exception in stop ClusteredSearchRequester, ",e,SearchClientProxy.class);
		}
	}
	
	private Message doSearchRequest(Session session, Message message) throws JMSException {
		Destination replyQueue = acquireTempQueue(session);
		if(Tracing.isDebugEnabled(SearchClientProxy.class)){
			Tracing.logDebug("doSearchRequest replyQueue=" + replyQueue,SearchClientProxy.class);
		}
		try{
			MessageConsumer responseConsumer = session.createConsumer(replyQueue);
			
			message.setJMSReplyTo(replyQueue);
			String correlationId = createRandomString();
			message.setJMSCorrelationID(correlationId);
	  
			MessageProducer producer = session.createProducer(searchQueue_);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			producer.setTimeToLive(timeToLive_);
			if (Tracing.isDebugEnabled(SearchClientProxy.class)) {
				Tracing.logDebug("Sending search request message with correlationId="+correlationId,SearchClientProxy.class);
			}
			producer.send(message);
			producer.close();
	
			Message returnedMessage = null;
			final long start = System.currentTimeMillis();
			while(true) {
				final long diff = (start + receiveTimeout_) - System.currentTimeMillis(); 
				if (diff<=0) {
					// timeout
					Tracing.logInfo("Timeout in search. Remaining time zero or negative.", SearchClientProxy.class);
					break;
				}
				if (Tracing.isDebugEnabled(SearchClientProxy.class)) {
					Tracing.logDebug("doSearchRequest: call receive with timeout=" + diff,SearchClientProxy.class);
				}
				returnedMessage = responseConsumer.receive(diff);
				if (returnedMessage==null) {
					// timeout case, we're stopping now with a reply...
					Tracing.logInfo("Timeout in search. Repy was null.", SearchClientProxy.class);
					break;
				} else if (!correlationId.equals(returnedMessage.getJMSCorrelationID())) {
					// we got an old reply from a previous search request
					Tracing.logInfo("Got a response with a wrong correlationId. Ignoring and waiting for the next", SearchClientProxy.class);
					continue;
				} else {
					// we got a valid reply
					break;
				}
			}
			responseConsumer.close();
			if (Tracing.isDebugEnabled(SearchClientProxy.class)) {
				Tracing.logDebug("doSearchRequest: returnedMessage=" + returnedMessage,SearchClientProxy.class);
			}
			return returnedMessage;
		} finally {
			releaseTempQueue(replyQueue);
		}
	}
	
}
