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

package org.olat.modules.fo.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.login.LoginModule;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumChangedEvent;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.MessageRef;
import org.olat.modules.fo.Pseudonym;
import org.olat.modules.fo.QuoteAndTagFilter;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.model.ForumImpl;
import org.olat.modules.fo.model.ForumThread;
import org.olat.modules.fo.model.ForumUserStatistics;
import org.olat.modules.fo.model.MessageImpl;
import org.olat.modules.fo.model.MessageLightImpl;
import org.olat.modules.fo.model.MessageStatistics;
import org.olat.modules.fo.model.PseudonymImpl;
import org.olat.modules.fo.model.PseudonymStatistics;
import org.olat.modules.fo.model.ReadMessageImpl;
import org.olat.modules.fo.ui.MessagePeekview;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Felix Jost
 */
@Service
public class ForumManager {
	private static final Logger log = Tracing.createLoggerFor(ForumManager.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private TextService txtService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MarkingService markingService;
	
	public int countThread(Long messageKey) {
		String query = "select count(msg) from fomessage as msg where msg.key=:messageKey or msg.threadtop.key=:messageKey";
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Number.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}

	/**
	 * @param msgid The message id of the top thread
	 * @return List messages
	 */
	public List<Message> getThread(Long msgid) {
		return getThread(msgid, 0, -1, Message.OrderBy.creationDate, true); 
	}
	
	public List<Message> getThread(Long msgid, int firstResult, int maxResults, Message.OrderBy orderBy, boolean asc) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" where msg.key=:messageKey or msg.threadtop.key=:messageKey");
		if(orderBy != null) {
			query.append(" order by msg.").append(orderBy.name()).append(asc ? " ASC " : " DESC ");
		}
		
		TypedQuery<Message> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("messageKey", msgid)
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		return  dbQuery.getResultList();
	}

	/**
	 * 
	 * @param forumId The forum ID
	 * @return The number of messages in the specified forum
	 */
	public int countThreadsByForumID(Long forumId) {
		return countMessagesByForumID(forumId, true);
	}
	
	/**
	 * 
	 * @param forumId
	 * @param start
	 * @param limit
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	public List<Message> getThreadsByForumID(Long forumId, int firstResult, int maxResults, Message.OrderBy orderBy, boolean asc) {
		return getMessagesByForumID(forumId, firstResult, maxResults, true, orderBy, asc);
	}
	
	/**
	 * 
	 * @param forum
	 * @return
	 */
	public List<Message> getMessagesByForum(Forum forum){
		if (forum == null) return new ArrayList<>(0); //while indexing it can somehow occur, that forum is null!
		return getMessagesByForumID(forum.getKey(),  0, -1, false, null, true);
	}
	
	/**
	 * 
	 * @param forumKey
	 * @param start
	 * @param limit
	 * @param onlyThreads
	 * @param orderBy
	 * @param asc
	 * @return
	 */
	private List<Message> getMessagesByForumID(Long forumKey, int firstResult, int maxResults, boolean onlyThreads, Message.OrderBy orderBy, boolean asc) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" where msg.forum.key=:forumKey ");
		if(onlyThreads) {
			query.append(" and msg.parent is null");
		}
		if(orderBy != null) {
			query.append(" order by msg.").append(orderBy.name()).append(asc ? " ASC" : " DESC");
		}
		
		TypedQuery<Message> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("forumKey", forumKey)
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		return dbQuery.getResultList();
	}
	
	public List<Message> getMessageChildren(Message parentMessage) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
		     .append(" inner join msg.parent as parentMsg")
		     .append(" where parentMsg.key=:parentKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("parentKey", parentMessage.getKey())
				.getResultList();
		
	}
	
	public List<Message> getTopMessageChildren(Message topMessage) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
	     .append(" left join fetch msg.creator as creator")
	     .append(" left join fetch msg.modifier as modifier")
	     .append(" where msg.threadtop.key=:messageKey")
	     .append(" order by msg.creationDate asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("messageKey", topMessage.getKey())
				.getResultList();		
	}	
	
	private int countMessagesByForumID(Long forumKey, boolean onlyThreads) {
		StringBuilder query = new StringBuilder();
		query.append("select count(msg) from fomessage as msg")
		     .append(" where msg.forum.key=:forumKey");
		if(onlyThreads) {
			query.append(" and msg.parent is null");
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Number.class)
				.setParameter("forumKey", forumKey)
				.getSingleResult()
				.intValue();
	}
	
	/**
	 * Return the title of a message of the forum.
	 */
	public int countMessagesByForumID(Long forumId) {
		return countMessagesByForumID(forumId, false);
	}
	
	public List<MessagePeekview> getPeekviewMessages(Forum forum, int maxResults) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fopeekviewmessage as msg where msg.forumKey=:forumKey order by msg.creationDate desc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessagePeekview.class)
				.setParameter("forumKey", forum.getKey())
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	public String getForumNameForLogging(Forum forum) {
		StringBuilder query = new StringBuilder();
		query.append("select msg.title from fomessage as msg where msg.forum.key=:forumKey");

		List<String> titles = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), String.class)
				.setParameter("forumKey", forum.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return titles == null || titles.isEmpty() || titles.get(0) == null ? null : titles.get(0);
	}
	
	
	public List<ForumThread> getForumThreads(Forum forum, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg ")
		  .append(" , (select count(replies.key) from fomessage as replies")
		  .append("  where replies.threadtop.key=msg.key and replies.forum.key=:forumKey")
		  .append(" ) as numOfMessages")
		  .append(" , (select max(replies.lastModified) from fomessage as replies")
		  .append("  where replies.threadtop.key=msg.key and replies.forum.key=:forumKey")
		  .append(" ) as lastModified");

		if(identity != null) {
			sb.append(" , (select count(read.key) from foreadmessage as read, fomessage as posts")
			  .append("  where (posts.threadtop.key=msg.key or posts.key=msg.key) and read.message.key=posts.key and read.identity.key=:identityKey")
			  .append(" ) as numOfReadMessages")
			  .append(" ,(select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark, fomessage as mposts ")
			  .append("   where mark.creator.key=:identityKey and mark.resId=:forumKey and (mposts.threadtop.key=msg.key or mposts.key=msg.key)")
			  .append("    and mposts.key=cast(mark.resSubPath as long) and mark.resName='Forum'")
			  .append(" ) as marks");
		}
		
		sb.append(" from fomessage as msg ")
		  .append(" left join fetch msg.creator as creator")
		  .append(" where msg.forum.key=:forumKey and msg.threadtop is null");

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("forumKey", forum.getKey());
		if(identity != null) {
			objectsQuery.setParameter("identityKey", identity.getKey());
		}

		List<Object[]> objects = objectsQuery.getResultList();
		List<ForumThread> threadList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Message msg = (Message)object[0];
			Number numOfMessagesLong = (Number)object[1];
			Date lastModified = (Date)object[2];
			int numOfMessages = numOfMessagesLong == null ? 1 : numOfMessagesLong.intValue() + 1;
			String creator = userManager.getUserDisplayName(msg.getCreator());
			ForumThread thread = new ForumThread(msg, creator, lastModified, numOfMessages);
			
			if(identity != null) {
				Number readMessages = (Number)object[3];
				int numOfReadMessages = readMessages == null ? 0 : readMessages.intValue();
				thread.setNewMessages(numOfMessages - numOfReadMessages);

				Number numOfMarkedMessagesLong = (Number)object[4];
				int numOfMarkedMessages = numOfMarkedMessagesLong == null ? 0 : numOfMarkedMessagesLong.intValue();
				thread.setMarkedMessages(numOfMarkedMessages);
			}
			
			threadList.add(thread);
		}
		return threadList;
	}
	
	public boolean existsMessageById(Long messageKey) {
		StringBuilder query = new StringBuilder();
		query.append("select msg.key from fomessage as msg")
		     .append(" where msg.key=:messageKey");
		
		List<Long> messages = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return messages == null || messages.isEmpty() || messages.get(0) == null
				? false : messages.get(0).longValue() > -1;
	}
	
	public Message getMessageById(Long messageKey) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as creator")
		     .append(" left join fetch msg.threadtop as threadtop")
		     .append(" where msg.key=:messageKey");
		
		List<Message> messages = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return messages == null || messages.isEmpty() ? null : messages.get(0);
	}
	
	public List<Message> getMessageByCreator(IdentityRef creator) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg")
		     .append(" inner join msg.creator as creator")
		     .append(" inner join fetch msg.forum as forum")
		     .append(" where msg.creator.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("identityKey", creator.getKey())
				.getResultList();
	}

	public boolean isPseudonymProtected(String pseudonym) {
		StringBuilder query = new StringBuilder();
		query.append("select pseudonym.key from fopseudonym as pseudo")
	     .append(" where lower(pseudo.pseudonym)=:pseudonym");
		
		List<Long> pseudonyms = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.setParameter("pseudonym", pseudonym.toLowerCase())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return pseudonyms != null && pseudonyms.size() > 0 && pseudonyms.get(0) != null;
	}
	
	public boolean isPseudonymInUseInForums(String pseudonym) {
		StringBuilder query = new StringBuilder();
		query.append("select msg.key from fomessage as msg")
		     .append(" where lower(msg.pseudonym)=:pseudonym");
		
		List<Long> pseudonyms = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.setParameter("pseudonym", pseudonym.toLowerCase())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return pseudonyms != null && pseudonyms.size() > 0 && pseudonyms.get(0) != null;
	}
	
	public List<Pseudonym> getPseudonyms(String pseudonym) {
		StringBuilder query = new StringBuilder();
		query.append("select pseudo from fopseudonym as pseudo")
		     .append(" where lower(pseudo.pseudonym)=:pseudonym");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Pseudonym.class)
				.setParameter("pseudonym", pseudonym.toLowerCase())
				.getResultList();
	}
	
	public boolean authenticatePseudonym(Pseudonym pseudonym, String password) {
		if(pseudonym.getAlgorithm() != null) {
			//check if update is needed
			Algorithm algorithm = Algorithm.valueOf(pseudonym.getAlgorithm());
			String credentials = Encoder.encrypt(password, pseudonym.getSalt(), algorithm);
			return credentials.equals(pseudonym.getCredential());
		}
		return false;
	}
	
	public Pseudonym createProtectedPseudonym(String pseudonym, String password) {
		PseudonymImpl pseudo = new PseudonymImpl();
		pseudo.setCreationDate(new Date());
		pseudo.setPseudonym(pseudonym);
		
		Algorithm algorithm = loginModule.getDefaultHashAlgorithm();
		String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
		String newCredentials = Encoder.encrypt(password, salt, algorithm);
		pseudo.setSalt(salt);
		pseudo.setCredential(newCredentials);
		pseudo.setAlgorithm(algorithm.name());
		
		dbInstance.getCurrentEntityManager().persist(pseudo);
		return pseudo;
	}
	

	public Pseudonym getPseudonymByKey(Long key) {
		StringBuilder query = new StringBuilder();
		query.append("select pseudo from fopseudonym as pseudo")
		     .append(" where pseudo.key=:pseudonymKey");
		
		List<Pseudonym> pseudonyms = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Pseudonym.class)
				.setParameter("pseudonymKey", key)
				.getResultList();
		return pseudonyms.size() > 0 ? pseudonyms.get(0) : null;
	}
	
	public void deletePseudonym(Pseudonym pseudonym) {
		Pseudonym reloadedPseudonym = dbInstance.getCurrentEntityManager()
			.getReference(PseudonymImpl.class, pseudonym.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedPseudonym);
	}
	
	public String getPseudonym(Forum forum, IdentityRef identity) {
		StringBuilder query = new StringBuilder();
		query.append("select msg.pseudonym from fomessage as msg")
		     .append(" where msg.creator.key=:identityKey and msg.forum.key=:forumKey and msg.pseudonym is not null");
		
		List<String> pseudonyms = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("forumKey", forum.getKey())
				.getResultList();
		return pseudonyms == null || pseudonyms.isEmpty() ? null : pseudonyms.get(0);
	}
	
	public List<PseudonymStatistics> getPseudonymStatistics(String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pseudo.key, pseudo.creationDate, pseudo.pseudonym, count(msg.key)")
		     .append(" from fopseudonym as pseudo")
		     .append(" left join fomessage as msg on (msg.pseudonym=pseudo.pseudonym)");
		if(StringHelper.containsNonWhitespace(searchString)) {
			sb.append(" where ");
			PersistenceHelper.appendFuzzyLike(sb, "pseudo.pseudonym", "pseudonym", dbInstance.getDbVendor());
		}
		sb.append(" group by pseudo.key, pseudo.creationDate, pseudo.pseudonym");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("pseudonym", PersistenceHelper.makeFuzzyQueryString(searchString));
		}

		List<Object[]> objects = query.getResultList();
		List<PseudonymStatistics> stats = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long key = (Long)object[0];
			Date creationDate = (Date)object[1];
			String pseudonym = (String)object[2];
			Long numOfMessages = PersistenceHelper.extractLong(object, 3);
			stats.add(new PseudonymStatistics(key, creationDate, pseudonym, numOfMessages));
		}
		return stats;
	}
	
	public List<MessageLight> getLightMessagesByForum(Forum forum) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from folightmessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" left join fetch msg.threadtop as threadtop")
		     .append(" where msg.forumKey=:forumKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageLight.class)
				.setParameter("forumKey", forum.getKey())
				.getResultList();
	}
	
	public List<MessageLight> getLightMessagesByThread(Forum forum, MessageRef thread) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from folightmessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" inner join fetch msg.threadtop as threadtop")
		     .append(" where msg.forumKey=:forumKey and threadtop.key=:threadKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageLight.class)
				.setParameter("forumKey", forum.getKey())
				.setParameter("threadKey", thread.getKey())
				.getResultList();
	}
	
	public List<MessageLight> getLightMessagesOfGuests(Forum forum) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from folightmessage as msg")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" left join fetch msg.threadtop as threadtop")
		     .append(" where msg.forumKey=:forumKey and msg.guest=true");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageLight.class)
				.setParameter("forumKey", forum.getKey())
				.getResultList();
	}
	
	/**
	 * Return the messages of a user written under it's own name
	 * (not under a pseudonym).
	 * 
	 * @param forum
	 * @param user
	 * @return
	 */
	public List<MessageLight> getLightMessagesByUser(Forum forum, IdentityRef user) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from folightmessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" left join fetch msg.threadtop as threadtop")
		     .append(" where msg.forumKey=:forumKey and msg.creator.key=:userKey and msg.pseudonym is null");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageLight.class)
				.setParameter("forumKey", forum.getKey())
				.setParameter("userKey", user.getKey())
				.getResultList();
	}
	
	/**
	 * Return the messages of a specified user under a specific
	 * pseudonym.
	 * 
	 * @param forum
	 * @param user
	 * @param pseudonym
	 * @return
	 */
	public List<MessageLight> getLightMessagesByUserUnderPseudo(Forum forum, IdentityRef user, String pseudonym) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from folightmessage as msg")
		     .append(" left join fetch msg.creator as creator")
		     .append(" left join fetch msg.modifier as modifier")
		     .append(" left join fetch msg.threadtop as threadtop")
		     .append(" where msg.forumKey=:forumKey and msg.creator.key=:userKey and msg.pseudonym=:pseudonym");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageLight.class)
				.setParameter("forumKey", forum.getKey())
				.setParameter("userKey", user.getKey())
				.setParameter("pseudonym", pseudonym)
				.getResultList();
	}
	
	public List<ForumUserStatistics> getForumUserStatistics(Forum forum) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessageforstatistics as msg")
		     .append(" left join msg.creator as creator")
		     .append(" where msg.forumKey=:forumKey");

		List<MessageStatistics> statistics = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), MessageStatistics.class)
				.setParameter("forumKey", forum.getKey())
				.getResultList();


		Map<StatisticsKey, ForumUserStatistics> identityKeyToStats = new HashMap<>();
		for(MessageStatistics statistic:statistics) {
			StatisticsKey key = new StatisticsKey(statistic);

			ForumUserStatistics userStats;
			if(identityKeyToStats.containsKey(key)) {
				userStats = identityKeyToStats.get(key);
			} else {
				userStats = new ForumUserStatistics(statistic.getCreator(), statistic.getPseudonym(), statistic.isGuest());
				identityKeyToStats.put(key, userStats);
			}

			userStats.addNumOfCharacters(statistic.getNumOfCharacters());
			userStats.addNumOfWords(statistic.getNumOfWords());
			if(statistic.getThreadtopKey() == null) {
				userStats.addNumOfThreads(1);
			} else {
				userStats.addNumOfReplies(1);
			}
			if(userStats.getLastModified() == null ||
					(statistic.getLastModified() != null
						&& statistic.getLastModified().after(userStats.getLastModified()))) {
				userStats.setLastModified(statistic.getLastModified());
			}
		}
		return new ArrayList<>(identityKeyToStats.values());
	}
	
	private static class StatisticsKey {
		
		private boolean guest;
		private String pseudonym;
		private Long identityKey;
		
		public StatisticsKey(MessageStatistics statistic) {
			guest = statistic.isGuest();
			pseudonym = statistic.getPseudonym();
			if(statistic.getCreator() != null) {
				identityKey = statistic.getCreator().getKey();
			}
		}

		@Override
		public int hashCode() {
			return guest ? 27534 :
				((identityKey == null ? 3467 : identityKey.hashCode()) + (pseudonym == null ? 567 : pseudonym.hashCode()));
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof StatisticsKey) {
				StatisticsKey key = (StatisticsKey)obj;
				if(guest && key.guest) {
					return (pseudonym == null && key.pseudonym == null) || (pseudonym != null && pseudonym.equals(key.pseudonym));
				}
				return identityKey != null && identityKey.equals(key.identityKey)
						&& ((pseudonym == null && key.pseudonym == null) || (pseudonym != null && pseudonym.equals(key.pseudonym)));
			}
			return false;
		}
	}
	
	/**
	 * Implementation with one entry per message.
	 * @param identity
	 * @param forumkey
	 * @return number of read messages
	 */
	public int countReadMessagesByUserAndForum(IdentityRef identity, Long forumkey) {
		String query = "select count(msg) from foreadmessage as msg where msg.identity.key=:identityKey and msg.forum.key=:forumKey";
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("forumKey", forumkey)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}

	/**
	 * @param forumKey
	 * @param latestRead
	 * @return a List of Object[] with a key(Long), title(String), a creator(Identity), and
	 *         the lastmodified(Date) of the messages of the forum with the given
	 *         key and with last modification after the "latestRead" Date
	 */
	public List<Message> getNewMessageInfo(Long forumKey, Date latestRead) {
		StringBuilder query = new StringBuilder();
		query.append("select msg from fomessage as msg ")
		     .append(" left join fetch msg.creator as creator")
		     .append(" where msg.forum.key=:forumKey and msg.lastModified>:latestRead order by msg.lastModified desc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Message.class)
				.setParameter("forumKey", forumKey)
				.setParameter("latestRead", latestRead, TemporalType.TIMESTAMP)
				.getResultList();
	}

	/**
	 * @return the newly created and persisted forum
	 */
	public Forum addAForum() {
		return addAForum(null);
	}

	public Forum addAForum(OLATResourceable refrence) {
		ForumImpl fo = new ForumImpl();
		fo.setCreationDate(new Date());
		fo.setReference(refrence);
		dbInstance.getCurrentEntityManager().persist(fo);
		return fo;
	}

	/**
	 * @param forumKey
	 * @return the forum with the given key
	 */
	public Forum loadForum(Long forumKey) {
		String q = "select fo from forum as fo where fo.key=:forumKey";
		List<Forum> forumList = dbInstance.getCurrentEntityManager()
				.createQuery(q, Forum.class)
				.setParameter("forumKey", forumKey)
				.getResultList();
		return forumList == null || forumList.isEmpty() ? null : forumList.get(0);
	}
	
	public Forum loadForum(OLATResourceable refrence) {
		String q = "select fo from forum as fo where fo.refResName=:refResName and fo.refResId=:refResId";
		List<Forum> forumList = dbInstance.getCurrentEntityManager()
				.createQuery(q, Forum.class)
				.setParameter("refResName", refrence.getResourceableTypeName())
				.setParameter("refResId", refrence.getResourceableId())
				.getResultList();
		return forumList == null || forumList.isEmpty() ? null : forumList.get(0);
	}

	/**
	 * @param forumKey
	 */
	public void deleteForum(Long forumKey) {
		Forum foToDel = loadForum(forumKey);
		if (foToDel != null) {
			// delete properties, messages and the forum itself
			doDeleteForum(foToDel);
			// delete directory for messages with attachments
			deleteForumContainer(forumKey);
		}
	}

	/**
	 * deletes all messages belonging to this forum and the forum entry itself
	 * 
	 * @param forum
	 */
	private void doDeleteForum(final Forum forum) {
		final Long forumKey = forum.getKey();
		//delete read messsages
		String deleteReadMessages = "delete from foreadmessage as rmsg where rmsg.forum.key=:forumKey";
		dbInstance.getCurrentEntityManager().createQuery(deleteReadMessages)
			.setParameter("forumKey", forumKey)
			.executeUpdate();
		// delete messages
		String messagesToDelete = "select msg from fomessage as msg where msg.forum.key=:forumKey and msg.threadtop.key is null";
		List<Message> threadsToDelete = dbInstance.getCurrentEntityManager()
					.createQuery(messagesToDelete, Message.class)
					.setParameter("forumKey", forumKey)
					.getResultList();
		for(Message threadToDelete:threadsToDelete) {
			deleteMessageTree(forumKey, threadToDelete);
			
			Message reloadedMessage = dbInstance.getCurrentEntityManager()
					.find(MessageImpl.class, threadToDelete.getKey());
			if(reloadedMessage != null) {
				dbInstance.getCurrentEntityManager().remove(threadToDelete);
			}
		}
		dbInstance.commit();
		
		// delete forum
		String deleteForum = "delete from forum as fo where fo.key=:forumKey";
		dbInstance.getCurrentEntityManager().createQuery(deleteForum)
			.setParameter("forumKey", forumKey)
			.executeUpdate();
		//delete all flags
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forum.getKey());
		markingService.getMarkManager().deleteMarks(ores);
	}

	/**
	 * sets the parent and threadtop of the message automatically
	 * 
	 * @param newMessage the new message which has title and body set
	 * @param creator
	 * @param replyToMessage
	 */
	public Message replyToMessage(Message newMessage, Message replyToMessage) {
		Message top = replyToMessage.getThreadtop();
		newMessage.setThreadtop((top != null ? top : replyToMessage));
		newMessage.setParent(replyToMessage);
		return saveMessage(newMessage);
	}

	/**
	 * @param creator
	 * @param forum
	 * @param topMessage
	 */
	public Message addTopMessage(Message topMessage) {
		topMessage.setParent(null);
		topMessage.setThreadtop(null);
		return saveMessage(topMessage);
	}

	/**
	 * @param messageKey
	 * @return the message with the given messageKey
	 */
	public Message loadMessage(Long messageKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from fomessage msg where msg.key=:messageKey");
		List<Message> messages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Message.class)
				.setParameter("messageKey", messageKey)
				.getResultList();
		return messages == null || messages.isEmpty() ? null : messages.get(0);
	}

	private Message saveMessage(Message m) {
		updateCounters(m);
		m.setLastModified(new Date());
		if(m.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(m);
		} else {
			m = dbInstance.getCurrentEntityManager().merge(m);
		}
		return m;
	}

	/**
	 * creates (in RAM only) a new Message<br>
	 * fill the values and use saveMessage to make it persistent
	 * 
	 * @return the message
	 * @see ForumManager#saveMessage(Message)
	 */
	public Message createMessage(Forum forum, Identity creator, boolean guest) {
		MessageImpl message = new MessageImpl();
		message.setForum(forum);
		if(guest) {
			message.setGuest(guest);
		} else {
			message.setCreator(creator);
		}
		return message;
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
	public Message updateMessage(Message message, boolean updateLastModifiedDate) {
		updateCounters(message);
		// OLAT-6295 Only update last modified for the operations edit(update), show, and open. 
		// Don't update the last modified date for the operations close, hide, move and split.
		if (updateLastModifiedDate) {
			message.setLastModified(new Date());
		}
		return dbInstance.getCurrentEntityManager().merge(message);
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
		deleteReadMessages(m.getKey());
		
		String query = "select msg from fomessage as msg where msg.parent.key=:parentKey";
		List<Message> messages = dbInstance.getCurrentEntityManager().createQuery(query, Message.class)
				.setParameter("parentKey", m.getKey()).getResultList();
		for (Message element:messages) {
			deleteMessageRecursion(forumKey, element);
		}

		// make sure the message is reloaded if it is not in the hibernate session cache
		Message reloadedMessage = dbInstance.getCurrentEntityManager().find(MessageImpl.class, m.getKey());
		if(reloadedMessage != null) {
			// delete all properties of one single message
			dbInstance.getCurrentEntityManager().remove(reloadedMessage);
			
			//delete all flags
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forumKey);
			markingService.getMarkManager().deleteMarks(ores, m.getKey().toString());
		}
		
		log.debug("Deleting message: {}", m.getKey());
	}

	/**
	 * @param m
	 * @return true if the message has children
	 */
	public boolean hasChildren(Message m) {
		String q = "select count(msg) from fomessage msg where msg.parent.key=:parentKey";
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(q, Number.class)
				.setParameter("parentKey", m.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null && count.get(0).longValue() > 0;
	}
	
	public int countMessageChildren(Long messageKey ) {
		String q = "select count(msg) from fomessage msg where msg.parent.key=:parentKey";
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(q, Number.class)
				.setParameter("parentKey", messageKey)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}
	
	public void countMessageChildrenRecursively(Message message, Set<Long> messageKeys) {
		List<Message> children = getMessageChildren(message);
		for (Message child : children) {
			messageKeys.add(child.getKey());
			if (hasChildren(child)){
				countMessageChildrenRecursively(child, messageKeys);
			}
		}	
	}

	/**
	 * deletes entry of one message
	 */
	private void deleteReadMessages(Long messageKey) {
		String query = "delete from foreadmessage as rmsg where rmsg.message.key=:messageKey";
		dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("messageKey", messageKey)
			.executeUpdate();
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
		log.error("The following message container is not a directory: {}", messageContainer);
		return null;
	}
	
	public File getMessageDirectory(Long forumKey, Long messageKey, boolean create) {
		File forumDir = getForumDirectory(forumKey);
		File messageDir = new File(forumDir, messageKey.toString());
		if(create && !messageDir.exists()) {
			messageDir.mkdirs();
		}
		return messageDir;
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
		mContainer.deleteSilently();
	}

	private void deleteForumContainer(Long forumKey) {
		VFSContainer fContainer = getForumContainer(forumKey);
		fContainer.deleteSilently();
	}

	public VFSContainer getForumContainer(Long forumKey) {
		VFSContainer fContainer = VFSManager.olatRootContainer("/forum", null);
		VFSItem forumContainer = fContainer.resolve(forumKey.toString());
		if(forumContainer == null) {
			return fContainer.createChildContainer(forumKey.toString());
		} else if(forumContainer instanceof VFSContainer) {
			return (VFSContainer)forumContainer;
		}
		log.error("The following forum container is not a directory: {}", forumContainer);
		return null;
	}
	
	public File getForumDirectory(Long forumKey) {
		File forumsDir = new File(FolderConfig.getCanonicalRoot(), "forum");
		File forumDir = new File(forumsDir,forumKey.toString());
		if(!forumDir.exists()) {
			forumDir.mkdirs();
		}
		return forumDir;
	}
	
	/**
	 * Splits the current thread starting from the current message.
	 * It updates the messages of the selected subthread by setting the Parent and the Threadtop.
	 * The method send a SPLIT event, and make a commit before sending it.
	 * 
	 * @param msgid
	 * @return the top message of the newly created thread.
	 */
	public Message splitThread(Message msg) {
		Message newTopMessage = null;
		if(msg.getThreadtop() == null) {
			newTopMessage = msg;
		} else {	
			//it only make sense to split a thread if the current message is not a threadtop message.	
			List<Message> threadList = getThread(msg.getThreadtop().getKey());
			List<Message> subthreadList = new ArrayList<>();
			getSubthread(msg, threadList, subthreadList);

			newTopMessage = getMessageById(msg.getKey());
			newTopMessage.setParent(null);
			newTopMessage.setThreadtop(null);
			newTopMessage = dbInstance.getCurrentEntityManager().merge(newTopMessage);

			for(Message message : subthreadList) {
				message.setThreadtop(newTopMessage);
				message = dbInstance.getCurrentEntityManager().merge(message);
			}

			dbInstance.commit();// before sending async event
			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.SPLIT, newTopMessage.getKey(), null, null);
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(event, newTopMessage.getForum());
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
		// one has to set a new parent for all children of the moved message
		// first message of sublist has to get the parent from the moved message
		List<Message> children = getMessageChildren(msg);
		for (Message child : children) {
			child.setParent(msg.getParent());
			dbInstance.getCurrentEntityManager().merge(child);
		}
		
		// now move the message to the chosen thread
		Message targetThread = topMsg.getThreadtop();
		if(targetThread == null) {
			targetThread = topMsg;
		}

		final Message oldMessage = getMessageById(msg.getKey());
		Message message = persistMessageInAnotherLocation(msg, oldMessage.getForum(), targetThread, topMsg);
	
		//move marks
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, msg.getForum().getKey());
		markingService.getMarkManager().moveMarks(ores, msg.getKey().toString(), message.getKey().toString());
		
		moveMessageContainer(oldMessage.getForum().getKey(), oldMessage.getKey(), message.getForum().getKey(), message.getKey());
		deleteMessageRecursion(oldMessage.getForum().getKey(), oldMessage);
		return message;
	}
	
	
	/**
	 * Collect message children recursively.
	 *
	 * @param oldParent
	 * @param setOfIdentity 
	 */
	public void collectThreadMembersRecursively(Message oldParent, Set<Identity> setOfIdentity, Map<Identity, String> pseudonymes) {
		List<Message> children = getMessageChildren(oldParent);
		children.sort(new Comparator<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				if (o1 == null) return 1;
				if (o2 == null) return -1;
				// move posts with pseudonyms toward low indices in list
				if (o1.getPseudonym() == null && o2.getPseudonym() != null) {
					return 1;
				} else if (o1.getPseudonym() != null && o2.getPseudonym() == null) {
					return -1;
				} else {
					return o1.getCreationDate().compareTo(o2.getCreationDate());
				}
			}
		});
		for (Message child : children) {
			if (!child.isGuest()) {
				Identity creator = child.getCreator();
				if (creator != null) {
					setOfIdentity.add(creator);
					String pseudonym = child.getPseudonym();
					if(pseudonym != null) {
						pseudonymes.put(creator, pseudonym);
					} else if (pseudonymes.containsKey(creator)) {
						// remove entry if thread also contains same identity without pseudonym 
						pseudonymes.remove(creator);
					}
				}
				Identity modifier = child.getModifier();
				if (creator != null && modifier != null) {
					setOfIdentity.add(modifier);
				}
			}
			if (hasChildren(child)) {
				collectThreadMembersRecursively(child, setOfIdentity, pseudonymes);
			}			
		}
	}

	/**
	 * Move thread to another forum recursively.
	 *
	 * @param oldParent the OLD parent message
	 * @param newParent the NEW parent message
	 * @param topMsg the top message
	 * @return the message
	 */
	private Message moveThreadToAnotherForumRecursively(Message oldParent, Message newParent, Message topMsg) {
		// 1) get direct children of the old top message
		List<Message> children = getMessageChildren(oldParent);
		Message message = null;
		// 2) iterate all first level children
		for (Message child : children) {
			Message oldMessage = getMessageById(child.getKey());
			topMsg = getMessageById(topMsg.getKey());
			message = persistMessageInAnotherLocation(oldMessage, topMsg.getForum(), topMsg, newParent);
			// 3) move the message container to a new destination
			moveMessageContainer(oldMessage.getForum().getKey(), oldMessage.getKey(), 
					message.getForum().getKey(), message.getKey());
			// 4) do recursion if children are available
			if (hasChildren(child)) {
				moveThreadToAnotherForumRecursively(child, message, topMsg);				
			}
		}
		return message;
	}	
	
	/**
	 * Creates new thread in another forum or appends selected thread to another thread in another forum.
	 *
	 * @param msg the OLD parent message
	 * @param the destination forum
	 * @param topMsg the top message
	 * @return the message
	 */
	public Message createOrAppendThreadInAnotherForum(Message msg, Forum forum, Message topMsg) {
		Message oldMessage = getMessageById(msg.getKey());
		Message message = persistMessageInAnotherLocation(oldMessage, forum, topMsg, topMsg);
		// reload message from database
		message = getMessageById(message.getKey());
		moveMessageContainer(oldMessage.getForum().getKey(), oldMessage.getKey(), 
				message.getForum().getKey(), message.getKey());
		
		if (hasChildren(oldMessage)) {
			if (topMsg != null) {
				// if added to an existing thread choose its top message
				message = moveThreadToAnotherForumRecursively(oldMessage, message, message.getThreadtop());
			} else {
				// if a new thread is created in a forum the parent message is also the top message
				message = moveThreadToAnotherForumRecursively(oldMessage, message, message);
			}
		}
		// deletes all children of the old top message recursively
		deleteMessageRecursion(oldMessage.getForum().getKey(), oldMessage);
		
		return message;
	}	
	

	/**
	 * Move single message to another forum.
	 *
	 * @param msg the OLD parent message
	 * @param topMsg the NEW top message
	 * @return the message
	 */
	public Message moveMessageToAnotherForum(Message msg, Forum forum, Message topMsg) {
		Message targetThread = null;
		if (topMsg != null) {
			targetThread = topMsg.getThreadtop();
			if (targetThread == null) {
				targetThread = topMsg;
			}
			targetThread = getMessageById(targetThread.getKey());
		}
		final Message oldParent = getMessageById(msg.getKey());
		// one has to set a new parent for all children of the moved message
		Message newParent = persistMessageInAnotherLocation(oldParent, forum, targetThread, topMsg);		
		moveMessageContainer(oldParent.getForum().getKey(), oldParent.getKey(), newParent.getForum().getKey(), newParent.getKey());
		targetThread = targetThread == null ? newParent : targetThread;
		if (hasChildren(oldParent)) {
			moveThreadToAnotherForumRecursively(oldParent, newParent, targetThread);
		}
		deleteMessageRecursion(oldParent.getForum().getKey(), oldParent);
		return newParent;
	}
	
	/**
	 * Persist message in another location.
	 */
	private Message persistMessageInAnotherLocation(Message oldMessage, Forum forum, Message top, Message parent) {
		// 1) take the new top messages forum to create a new child
		Message message = createMessage(forum, oldMessage.getCreator(), oldMessage.isGuest());
		((MessageImpl)message).setCreationDate(oldMessage.getCreationDate());
		message.setLastModified(oldMessage.getLastModified());
		message.setModifier(oldMessage.getModifier());
		message.setModificationDate(oldMessage.getModificationDate());
		message.setTitle(oldMessage.getTitle());
		message.setBody(oldMessage.getBody());
		message.setPseudonym(oldMessage.getPseudonym());
		// 2) set the thread top to the new top message
		message.setThreadtop(top);
		// 3) maintain the hierarchy, parent and top message can be equal 
		message.setParent(parent);
		Status status = Status.getStatus(oldMessage.getStatusCode());
		if (status != null){
			status.setMoved(true);
			message.setStatusCode(Status.getStatusCode(status));
		}
		// 4) save the new massage in the new destination
		message = saveMessage(message);
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
	public Set<Long> getReadSet(IdentityRef identity, Forum forum) {	
		StringBuilder query = new StringBuilder();
		query.append("select rmsg.message.key from foreadmessage as rmsg")
		     .append(" inner join rmsg.message as msg")
		     .append(" where rmsg.forum.key=:forumKey and rmsg.identity.key=:identityKey");
		List<Long> messageKeys = dbInstance.getCurrentEntityManager()
			.createQuery(query.toString(), Long.class)
			.setParameter("forumKey", forum.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return new HashSet<>(messageKeys);	
	}
	
	/**
	 * Optimized method to mark newly created messages as read.
	 * 
	 * @param identity
	 * @param forum
	 * @param msg
	 */
	public void markNewMessageAsRead(Identity identity, Forum forum, Message msg) {		
		//Check if the message was not already deleted
		ReadMessageImpl readMessage = new ReadMessageImpl();
		readMessage.setIdentity(identity);
		readMessage.setMessage(msg);
		readMessage.setForum(forum);
		dbInstance.getCurrentEntityManager().persist(readMessage);
	}
	
	/**
	 * Implementation with one entry per forum message.
	 * Adds a new entry into the ReadMessage for the input message and identity.
	 * @param msg
	 * @param identity
	 */
	public void markAsRead(Identity identity, Forum forum, MessageLight msg) {		
		//Check if the message was not already deleted
		Message retrievedMessage = loadMessage(msg.getKey());
		if(retrievedMessage != null) {
			ReadMessageImpl readMessage = new ReadMessageImpl();
			readMessage.setIdentity(identity);
			if(msg instanceof MessageLightImpl) {
				readMessage.setMessage(msg);
			} else {
				msg = dbInstance.getCurrentEntityManager().getReference(MessageLightImpl.class, msg.getKey());
				readMessage.setMessage(msg);
			}
			
			readMessage.setForum(forum);
			dbInstance.getCurrentEntityManager().persist(readMessage);
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
	
	public int mergeForums(Long masterForumKey, List<Long> forumsToMerge) {
		int rows = 0;
		if(forumsToMerge.size() > 0) {
			for(Long forumToMerge:forumsToMerge) {
				String updateMsg = "update fomessage set forum.key=:masterKey where forum.key=:mergerKey";
				rows += dbInstance.getCurrentEntityManager()
					.createQuery(updateMsg)
					.setParameter("masterKey", masterForumKey)
					.setParameter("mergerKey", forumToMerge)
					.executeUpdate();
				
				
				String updateReadMsg = "update foreadmessage set forum.key=:masterKey where forum.key=:mergerKey";
				rows += dbInstance.getCurrentEntityManager()
					.createQuery(updateReadMsg)
					.setParameter("masterKey", masterForumKey)
					.setParameter("mergerKey", forumToMerge)
					.executeUpdate();
			}
		}
		return rows;
	}
}
