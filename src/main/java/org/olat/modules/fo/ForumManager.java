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

package org.olat.modules.fo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.TemporalType;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Felix Jost
 */
@Service
public class ForumManager {
	private static final OLog log = Tracing.createLoggerFor(ForumManager.class);
	
	private static ForumManager INSTANCE;
	@Autowired
	private TextService txtService;
	@Autowired
	private DB dbInstance;

	/**
	 * [spring]
	 */
	private ForumManager() {
		INSTANCE = this;
	}

	/**
	 * @return the singleton
	 */
	public static ForumManager getInstance() {
		return INSTANCE;
	}
	
	public int countThread(Long msgid) {
		StringBuilder query = new StringBuilder();
		query.append("select count(msg) from ").append(MessageImpl.class.getName()).append(" as msg")
		     .append(" where (msg.key=:messageKey or msg.threadtop.key=:messageKey) ");

		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setLong("messageKey", msgid);
		Number totalCount = (Number)dbQuery.uniqueResult();
		return totalCount.intValue();
	}

	/**
	 * @param msgid msg id of the topthread
	 * @return List messages
	 */
	public List<Message> getThread(Long msgid) {
		return getThread(msgid, 0, -1, Message.OrderBy.creationDate, true); 
	}
	
	public List<Message> getThread(Long msgid, int firstResult, int maxResults, Message.OrderBy orderBy, boolean asc) {
		long rstart = 0;
		if (log.isDebug()){
			rstart = System.currentTimeMillis();
		}
		
		StringBuilder query = new StringBuilder();
		query.append("select msg from ").append(MessageImpl.class.getName()).append(" as msg")
		     .append(" inner join fetch msg.creator as creator")
		     .append(" where (msg.key=:messageKey or msg.threadtop.key=:messageKey) ");
		if(orderBy != null) {
			query.append(" order by msg.").append(orderBy.name()).append(asc ? " ASC " : " DESC ");
		}
		
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setLong("messageKey", msgid);
		dbQuery.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		
		List<Message> messages = dbQuery.list();
		if (log.isDebug()){
			long rstop = System.currentTimeMillis();
			log.debug("time to fetch thread with topmsg_id " + msgid + " :" + (rstop - rstart), null);
		}
		return messages;
	}

	public List<Long> getAllForumKeys(){
		List<Long> tmpRes = DBFactory.getInstance().find("select key from org.olat.modules.fo.ForumImpl");
		return tmpRes;
	}

	/**
	 * 
	 * @param forum_id
	 * @return
	 */
	public int countThreadsByForumID(Long forum_id) {
		return countMessagesByForumID(forum_id, true);
	}
	
	/**
	 * 
	 * @param forum_id
	 * @param start
	 * @param limit
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	public List<Message> getThreadsByForumID(Long forum_id, int firstResult, int maxResults, Message.OrderBy orderBy, boolean asc) {
		return getMessagesByForumID(forum_id, firstResult, maxResults, true, orderBy, asc);
	}
	
	/**
	 * 
	 * @param forum
	 * @return
	 */
	public List<Message> getMessagesByForum(Forum forum){
		if (forum == null) return new ArrayList<Message>(0); // fxdiff: while indexing it can somehow occur, that forum is null!
		return getMessagesByForumID(forum.getKey(),  0, -1, null, true);
	}
	
	/**
	 * @param forum
	 * @return List messages
	 */
	public List<Message> getMessagesByForumID(Long forum_id) {
		return getMessagesByForumID(forum_id, 0, -1, false, null, true);
	}
	
	/**
	 * 
	 * @param forum_id
	 * @param start
	 * @param limit
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	public List<Message> getMessagesByForumID(Long forum_id, int firstResult, int maxResults, Message.OrderBy orderBy, boolean asc) {
		return getMessagesByForumID(forum_id, firstResult, maxResults, false, orderBy, asc);
	}
	
	/**
	 * 
	 * @param forum_id
	 * @param start
	 * @param limit
	 * @param onlyThreads
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	private List<Message> getMessagesByForumID(Long forum_id, int firstResult, int maxResults, boolean onlyThreads, Message.OrderBy orderBy, boolean asc) {
		long rstart = 0;
		if(log.isDebug()){
			rstart = System.currentTimeMillis();
		}
		
		StringBuilder query = new StringBuilder();
		query.append("select msg from ").append(MessageImpl.class.getName()).append(" as msg")
		     .append(" inner join fetch msg.creator as creator")
		     .append(" where msg.forum.key=:forumId ");
		if(onlyThreads) {
			query.append(" and msg.parent is null");
		}
		if(orderBy != null) {
			query.append(" order by msg.").append(orderBy.name()).append(asc ? " ASC" : " DESC");
		}
		
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setLong("forumId", forum_id);
		dbQuery.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		
		List<Message> messages = dbQuery.list();
		if(log.isDebug()){
			long rstop = System.currentTimeMillis();
			log.debug("time to fetch forum with forum_id " + forum_id + " :" + (rstop - rstart), null);
		}
		return messages;
	}
	
	private int countMessagesByForumID(Long forumId, boolean onlyThreads) {
		StringBuilder query = new StringBuilder();
		query.append("select count(msg) from ").append(MessageImpl.class.getName()).append(" as msg")
		     .append(" where msg.forum.key=:forumId ");
		if(onlyThreads) {
			query.append(" and msg.parent is null");
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("forumId", forumId)
				.getSingleResult()
				.intValue();
	}
	
	/**
	 * 
	 * @param forumkey
	 * @return the count of all messages by this forum
	 */
	public Integer countMessagesByForumID(Long forum_id) {
		return countMessagesByForumID(forum_id, false);
	}
	
	/**
	 * Implementation with one entry per message.
	 * @param identity
	 * @param forumkey
	 * @return number of read messages
	 */
	public int countReadMessagesByUserAndForum(Identity identity, Long forumkey) {
		StringBuilder query = new StringBuilder();
		query.append("select count(msg) from ").append(ReadMessageImpl.class.getName()).append(" as msg ")
		     .append(" where msg.identity=:ident and msg.forum=:forumId");

		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setLong("forumId", forumkey);
		dbQuery.setLong("ident", identity.getKey());

		return ((Number)dbQuery.uniqueResult()).intValue();
	}

	/**
	 * @param forumKey
	 * @param latestRead
	 * @return a List of Object[] with a key(Long), title(String), a creator(Identity), and
	 *         the lastmodified(Date) of the messages of the forum with the given
	 *         key and with last modification after the "latestRead" Date
	 */
	public List<Message> getNewMessageInfo(Long forumKey, Date latestRead) {
		// FIXME:fj: lastModified has no index -> test performance with forum with
		// 200 messages
		StringBuilder query = new StringBuilder();
		query.append("select msg from ").append(MessageImpl.class.getName()).append(" as msg ")
		     .append(" inner join fetch msg.creator as creator")
		     .append(" where msg.forum.key =:forumKey and msg.lastModified>:latestRead order by msg.lastModified desc");

		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("forumKey", forumKey.longValue())
				.setParameter("latestRead", latestRead, TemporalType.TIMESTAMP)
				.getResultList();
	}

	/**
	 * @return the newly created and persisted forum
	 */
	public Forum addAForum() {
		Forum fo = createForum();
		saveForum(fo);
		return fo;
	}

	/**
	 * @param forumKey
	 * @return the forum with the given key
	 */
	public Forum loadForum(Long forumKey) {
		ForumImpl fo = DBFactory.getInstance().loadObject(ForumImpl.class, forumKey);
		return fo;
	}

	private Forum saveForum(Forum forum) {
		DB db = DBFactory.getInstance();
		db.saveObject(forum);
		return forum;
	}

	/**
	 * @param forumKey
	 */
	public void deleteForum(Long forumKey) {
		Forum foToDel = loadForum(forumKey);
		if (foToDel == null) throw new AssertException("forum to delete was not found: key=" + forumKey);
		// delete properties, messages and the forum itself
		doDeleteForum(foToDel);
		// delete directory for messages with attachments
		deleteForumContainer(forumKey);
	}

	/**
	 * deletes all messages belonging to this forum and the forum entry itself
	 * 
	 * @param forum
	 */
	private void doDeleteForum(final Forum forum) {
		Long forumKey = forum.getKey();
		DB db = DBFactory.getInstance();
		//delete read messsages
		db.delete("from readMsg in class org.olat.modules.fo.ReadMessageImpl where readMsg.forum = ? ", forumKey, StandardBasicTypes.LONG);
		// delete messages
		db.delete("from message in class org.olat.modules.fo.MessageImpl where message.forum = ?", forumKey, StandardBasicTypes.LONG);
		// delete forum
		db.delete("from forum in class org.olat.modules.fo.ForumImpl where forum.key = ?", forumKey, StandardBasicTypes.LONG);
		// delete properties
		
		//delete all flags
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forum.getKey());
		markingService.getMarkManager().deleteMarks(ores);
	}

	/**
	 * create (in RAM only) a new Forum
	 */
	private ForumImpl createForum() {
		return new ForumImpl();
	}

	/**
	 * sets the parent and threadtop of the message automatically
	 * 
	 * @param newMessage the new message which has title and body set
	 * @param creator
	 * @param replyToMessage
	 */
	public void replyToMessage(Message newMessage, Identity creator, Message replyToMessage) {
		newMessage.setForum(replyToMessage.getForum());
		Message top = replyToMessage.getThreadtop();
		newMessage.setThreadtop((top != null ? top : replyToMessage));
		newMessage.setParent(replyToMessage);
		newMessage.setCreator(creator);
		saveMessage(newMessage);
	}

	/**
	 * @param creator
	 * @param forum
	 * @param topMessage
	 */
	public void addTopMessage(Identity creator, Forum forum, Message topMessage) {
		topMessage.setForum(forum);
		topMessage.setParent(null);
		topMessage.setThreadtop(null);
		topMessage.setCreator(creator);

		saveMessage(topMessage);
	}

	/**
	 * @param messageKey
	 * @return the message with the given messageKey
	 */
	public Message loadMessage(Long messageKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(MessageImpl.class.getName()).append(" msg where msg.key=:messageKey");
		List<Message> messages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Message.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return messages == null || messages.isEmpty() ? null : messages.get(0);
	}

	private void saveMessage(Message m) {
		// TODO: think about where maxlenrestriction comes: manager or controller
		updateCounters(m);
		m.setLastModified(new Date());
		DBFactory.getInstance().saveObject(m);
	}

	/**
	 * creates (in RAM only) a new Message<br>
	 * fill the values and use saveMessage to make it persistent
	 * 
	 * @return the message
	 * @see ForumManager#saveMessage(Message)
	 */
	public Message createMessage() {
		return new MessageImpl();
	}

	/**
	 * Update message and fire MultiUserEvent, if any provided. If a not null
	 * ForumChangedEvent object is provided, then fire event to listeners.
	 * 
	 * @param m
	 * @param updateLastModifiedDate
	 *            true: the last modified date is updated to trigger a
	 *            notification; false: last modified date is not modified and no
	 *            notification is sent
	 * @param event
	 */
	public void updateMessage(final Message m, final boolean updateLastModifiedDate, final ForumChangedEvent event) {
		updateCounters(m);
		// OLAT-6295 Only update last modified for the operations edit(update), show, and open. 
		// Don't update the last modified date for the operations close, hide, move and split.
		if (updateLastModifiedDate) {
			m.setLastModified(new Date());
		}
		DBFactory.getInstance().updateObject(m);
		if (event!=null) {
	    CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new ForumChangedEvent("hide"), m.getForum());	   
		}
	}

	/**
	 * @param forumKey
	 * @param m
	 */
	public void deleteMessageTree(Long forumKey, Message m) {
		deleteMessageRecursion(forumKey, m);
	}

	private void deleteMessageRecursion(final Long forumKey, Message m) {
		deleteMessageContainer(forumKey, m.getKey());
		DB db = DBFactory.getInstance();
		Long message_id = m.getKey();
		List messages = db
				.find("select msg from msg in class org.olat.modules.fo.MessageImpl where msg.parent = ?", message_id, StandardBasicTypes.LONG);

		for (Iterator iter = messages.iterator(); iter.hasNext();) {
			Message element = (Message) iter.next();
			deleteMessageRecursion(forumKey, element);
		}

		/*
		 * if (! db.contains(m)){ log.debug("Message " + m.getKey() + " not in
		 * hibernate session, reloading before delete"); m =
		 * loadMessage(m.getKey()); }
		 */
		// make sure the message is reloaded if it is not in the hibernate session
		// cache
		m = (Message) db.loadObject(m);
    // delete all properties of one single message
		deleteMessageProperties(forumKey, m);
		db.deleteObject(m);
		
		//delete all flags
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forumKey);
		markingService.getMarkManager().deleteMarks(ores, m.getKey().toString());
		
		if(log.isDebug()){
			log.debug("Deleting message ", m.getKey().toString());
		}
	}

	/**
	 * @param m
	 * @return true if the message has children
	 */
	public boolean hasChildren(Message m) {
		boolean children = false;
		DB db = DBFactory.getInstance();
		Long message_id = m.getKey();
		String q = " select count(msg) from org.olat.modules.fo.MessageImpl msg where msg.parent = :input ";

		DBQuery query = db.createQuery(q);
		query.setLong("input", message_id.longValue());
		List result = query.list();
		int count = ((Long) result.get(0)).intValue();

		if (count > 0) {
			children = true;
		}

		return children;
	}

	/**
	 * deletes entry of one message
	 */
	private void deleteMessageProperties(Long forumKey, Message m) {
		DB db = DBFactory.getInstance();
		Long messageKey = m.getKey();

		StringBuilder query = new StringBuilder();		
		query.append("from readMsg in class org.olat.modules.fo.ReadMessageImpl ");
		query.append("where readMsg.forum = ? ");
		query.append("and readMsg.message = ? ");

		db.delete(query.toString(), new Object[] { forumKey, messageKey }, new Type[] { StandardBasicTypes.LONG, StandardBasicTypes.LONG });
	}

	/**
	 * @param forumKey
	 * @param messageKey
	 * @return the valid container for the attachments to place into
	 */
	public VFSContainer getMessageContainer(Long forumKey, Long messageKey) {
		VFSContainer forumContainer = getForumContainer(forumKey);
		VFSItem messageContainer = forumContainer.resolve(messageKey.toString());
		if(messageContainer == null) {
			return forumContainer.createChildContainer(messageKey.toString());
		} else if(messageContainer instanceof VFSContainer) {
			return (VFSContainer)messageContainer;
		}
		log.error("The following message container is not a directory: " + messageContainer);
		return null;
	}
	
	private void moveMessageContainer(Long fromForumKey, Long fromMessageKey, Long toForumKey, Long toMessageKey) {
		// copy message container
		VFSContainer toMessageContainer = getMessageContainer(toForumKey, toMessageKey);
		VFSContainer fromMessageContainer = getMessageContainer(fromForumKey, fromMessageKey);
		for (VFSItem vfsItem : fromMessageContainer.getItems()) {
			toMessageContainer.copyFrom(vfsItem);
		}
	}

	private void deleteMessageContainer(Long forumKey, Long messageKey) {
		VFSContainer mContainer = getMessageContainer(forumKey, messageKey);
		mContainer.delete();
	}

	private void deleteForumContainer(Long forumKey) {
		VFSContainer fContainer = getForumContainer(forumKey);
		fContainer.delete();
	}

	private VFSContainer getForumContainer(Long forumKey) {
		OlatRootFolderImpl fContainer = new OlatRootFolderImpl("/forum", null);
		VFSItem forumContainer = fContainer.resolve(forumKey.toString());
		if(forumContainer == null) {
			return fContainer.createChildContainer(forumKey.toString());
		} else if(forumContainer instanceof VFSContainer) {
			return (VFSContainer)forumContainer;
		}
		log.error("The following forum container is not a directory: " + forumContainer);
		return null;
	}
	
	/**
	 * Splits the current thread starting from the current message.
	 * It updates the messages of the selected subthread by setting the Parent and the Threadtop.
	 * 
	 * @param msgid
	 * @return the top message of the newly created thread.
	 */
	public Message splitThread(Message msg){
		Message newTopMessage = null;
		if(msg.getThreadtop()==null) {
			newTopMessage = msg;
		} else {	
		//it only make sense to split a thread if the current message is not a threadtop message.	
		List<Message> threadList = this.getThread(msg.getThreadtop().getKey());
			List<Message> subthreadList = new ArrayList<Message>();
			subthreadList.add(msg);
			getSubthread(msg, threadList, subthreadList);

			Iterator<Message> messageIterator = subthreadList.iterator();
			Message firstMessage = null;
			final DB db = DBFactory.getInstance();
			final boolean changeLastModifiedDate = false; // OLAT-6295
			if (messageIterator.hasNext()) {
				firstMessage = messageIterator.next();
				firstMessage = (Message) db.loadObject(firstMessage);
				firstMessage.setParent(null);
				firstMessage.setThreadtop(null);
				this.updateMessage(firstMessage, changeLastModifiedDate, new ForumChangedEvent("split"));
				newTopMessage = firstMessage;
			}
			while (firstMessage != null && messageIterator.hasNext()) {
				Message message = messageIterator.next();
				message = (Message) db.loadObject(message);
				message.setThreadtop(firstMessage);
				this.updateMessage(message, changeLastModifiedDate, null);
			}	
		}		
		return newTopMessage;
	}
	
	/**
	 * Moves the current message from the current thread in another thread.
	 * 
	 * @param msg
	 * @param topMsg
	 * @return the moved message
	 */
	public Message moveMessage(Message msg, Message topMsg) {
		DB db = DBFactory.getInstance();
		List<Message> oldThreadList = getThread(msg.getThreadtop().getKey());
		List<Message> subThreadList = new ArrayList<Message>();
		this.getSubthread(msg, oldThreadList, subThreadList);
		// one has to set a new parent for all childs of the moved message
		// first message of sublist has to get the parent from the moved message
		final boolean changeLastModifiedDate = false; // OLAT-6295
		for (Message childMessage : subThreadList) {
			childMessage = (Message) db.loadObject(childMessage);
			childMessage.setParent(msg.getParent());
			updateMessage(childMessage, changeLastModifiedDate, null);
		}
		// now move the message to the choosen thread
		final Message oldMessage = (Message) db.loadObject(msg);
		Message message = createMessage();
		message.setCreator(oldMessage.getCreator());
		message.setForum(oldMessage.getForum());
		message.setModifier(oldMessage.getModifier());
		message.setLastModified(oldMessage.getLastModified()); // OLAT-6295
		message.setTitle(oldMessage.getTitle());
		message.setBody(oldMessage.getBody());
		message.setThreadtop(topMsg);
		message.setParent(topMsg);
		Status status = Status.getStatus(oldMessage.getStatusCode());
		status.setMoved(true);
		message.setStatusCode(Status.getStatusCode(status));
		saveMessage(message);
		
		//move marks
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, msg.getForum().getKey());
		markingService.getMarkManager().moveMarks(ores, msg.getKey().toString(), message.getKey().toString());
		
		moveMessageContainer(oldMessage.getForum().getKey(), oldMessage.getKey(), message.getForum().getKey(), message.getKey());
		deleteMessageRecursion(oldMessage.getForum().getKey(), oldMessage);
		return message;
	}
	
	/**
	 * This is a recursive method. The subthreadList in an ordered list with all descendents of the input msg.
	 * @param msg
	 * @param threadList
	 * @param subthreadList
	 */
	private void getSubthread(Message msg, List<Message> threadList, List<Message> subthreadList) {		
		Iterator<Message> listIterator = threadList.iterator();
		while(listIterator.hasNext()) {
			Message currMessage = listIterator.next();
			if(currMessage.getParent()!=null && currMessage.getParent().getKey().equals(msg.getKey())) {
				subthreadList.add(currMessage);				
				getSubthread(currMessage, threadList, subthreadList);
			}
		}		
	}
	
	/**
	 * 
	 * @param identity
	 * @param forum
	 * @return a set with the read messages keys for the input identity and forum.  
	 */
	public Set<Long> getReadSet(Identity identity, Forum forum) {	
		StringBuilder query = new StringBuilder();
		query.append("select rmsg.message.key from ").append(ReadMessageImpl.class.getName()).append(" as rmsg")
		     .append(" inner join rmsg.message as msg")
		     .append(" where msg.forum.key=:forumId and rmsg.identity.key=:ident");

		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setLong("forumId", forum.getKey());
		dbQuery.setLong("ident", identity.getKey());
		List<Long> messageKeys = dbQuery.list();
		return new HashSet<Long>(messageKeys);	
	}
	
	/**
	 * Implementation with one entry per forum message.
	 * Adds a new entry into the ReadMessage for the input message and identity.
	 * @param msg
	 * @param identity
	 */
	public void markAsRead(Identity identity,Message msg) {		
		//Check if the message was not already deleted
		Message retrievedMessage = loadMessage(msg.getKey());
		if(retrievedMessage!=null) {
			ReadMessageImpl readMessage = new ReadMessageImpl();
			readMessage.setIdentity(identity);
			readMessage.setMessage(msg);
			readMessage.setForum(msg.getForum());
		  DBFactory.getInstance().saveObject(readMessage);
		}		
	}
	
	/**
	 * Update the counters for words and characters
	 * @param m the message
	 */
	public void updateCounters(Message m) {
		String body = m.getBody();
		String unQuotedBody = new QuoteAndTagFilter().filter(body);
		Locale suggestedLocale = txtService.detectLocale(unQuotedBody);
		m.setNumOfWords(txtService.wordCount(unQuotedBody, suggestedLocale));
		m.setNumOfCharacters(txtService.characterCount(unQuotedBody, suggestedLocale));
	}
}
