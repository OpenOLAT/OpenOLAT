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
package org.olat.modules.webFeed.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.coordinate.cluster.jms.JMSHelper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.NotificationsPushService;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.manager.SubscriberDAO;
import org.olat.core.commons.services.notifications.ui.NotificationSubscriptionController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.PublisherSubscriber;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Initial date: 16 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class FeedNotificationsPushHandler implements InitializingBean, DisposableBean  {

	private static final Logger log = Tracing.createLoggerFor(FeedNotificationsPushHandler.class);
	
	@Resource(name="notificationsTopic")
	private Topic destination;
	private Session notificationsBlogsSession;
	private MessageConsumer notificationsBlogsConsumer;
	private Session notificationsPodcatsSession;
	private MessageConsumer notificationsPodcastsConsumer;
	private Session notificationsFeedItemsSession;
	private MessageConsumer notificationsFeedItemsConsumer;
	@Resource(name="notificationsConnectionFactory")
	private ConnectionFactory connectionFactory;
	private Connection notificationsConnection;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ItemDAO itemDao;
	@Autowired
	private FeedDAO feedDao;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private SubscriberDAO subscriberDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		notificationsConnection = connectionFactory.createConnection();
		notificationsConnection.start();
		log.info("springInit: JMS connection started with connectionFactory={}", connectionFactory);

		notificationsBlogsSession = notificationsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		notificationsBlogsConsumer = notificationsBlogsSession.createConsumer(destination, "publishertype='Comment.BLOG' AND operation LIKE 'comment-%'");
		notificationsBlogsConsumer.setMessageListener(message -> {
			processMessage(message);
		});
		
		notificationsPodcatsSession = notificationsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		notificationsPodcastsConsumer = notificationsPodcatsSession.createConsumer(destination, "publishertype='Comment.PODCAST' AND operation LIKE 'comment-%'");
		notificationsPodcastsConsumer.setMessageListener(message -> {
			processMessage(message);
		});
		
		notificationsFeedItemsSession = notificationsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		notificationsFeedItemsConsumer = notificationsFeedItemsSession.createConsumer(destination, "publishertype='Comment.FeedItem' AND operation LIKE 'comment-%'");
		notificationsFeedItemsConsumer.setMessageListener(message -> {
			processMessage(message);
		});
	}
	
	@Override
	public void destroy() throws Exception {
		JMSHelper.close(null, notificationsFeedItemsConsumer, notificationsFeedItemsSession);
		JMSHelper.close(null, notificationsPodcastsConsumer, notificationsPodcatsSession);
		JMSHelper.close(notificationsConnection, notificationsBlogsConsumer, notificationsBlogsSession);
		log.info("Feed notifications listener stopped");
	}
	
	private void processMessage(Message message) {
		try {
			long commentKey = message.getLongProperty(NotificationsPushService.OBJECT_ID);
			UserComment comment = userCommentsDao.getCommentByKey(commentKey);
			if(comment != null) {
				processComment(comment);
			}
		} catch (JMSException e) {
			log.error("", e);
		} finally {
			try {
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	protected void processComment(UserComment comment) {
		String feedResourceName = comment.getResName();
		Long feedResourceId = comment.getResId();
		String feedItemGuid = comment.getResSubPath();

		RepositoryEntry feedEntry = repositoryEntryDao.loadByResourceId(feedResourceName, feedResourceId);
		Item item = itemDao.loadItemByGuid(feedResourceName, feedResourceId, feedItemGuid);
		Feed feed = item.getFeed();
		
		List<Notification> notifications = collectIdentitiesToNotify(feedEntry, feed, item, comment);
		for(Notification notification:notifications) {
			if(!notification.veto() && notification.identity().getStatus().intValue() < Identity.STATUS_VISIBLE_LIMIT.intValue()) {
				notify(notification, item, comment, feedEntry);
			}
		}
	}
	
	/**
	 * Collect the uses to notify: the subscribers, if the feed is configured to
	 * notify the owners, the blog/podcats owners, the courses owners which reference
	 * it and at the end the creator of the precendent comment if this one is a reply.
	 * 
	 * @param feedEntry The repository entry of the blog/podcast
	 * @param feed The feed
	 * @param item The item commented
	 * @param comment The comment itself
	 * @return A list of user to notify with some indications how
	 */
	protected List<Notification> collectIdentitiesToNotify(RepositoryEntry feedEntry, Feed feed, Item item, UserComment comment) {
		// Don't notify the author of the comment
		Set<Identity> notifyList = new HashSet<>();
		notifyList.add(comment.getCreator());
		
		List<Notification> notifications = new ArrayList<>();
		
		// Subscribed publisher data -> every contexts, feedItems
		List<Subscriber> subscribersItem = subscriberDao
				.getSubscribers(new PublisherData("Comment.FeedItem", item.getKey().toString(), null), PublisherChannel.DIRECT_EMAIL);
		
		// Notify subscribers, they want it
		for(Subscriber subscriber:subscribersItem) {
			if(!notifyList.contains(subscriber.getIdentity())) {
				notifyList.add(subscriber.getIdentity());
				notifications.add(getBestTarget(subscriber.getIdentity(), subscribersItem, NotificationAs.OTHER));
			}
		}

		if(feed.isPushEmailComments() && feedEntry != null) {
			// Notify blog / podcast owners
			List<Identity> resourceOwners = repositoryEntryRelationDao.getMembers(List.of(feedEntry), RepositoryEntryRelationType.all, GroupRoles.owner.name());
			for(Identity owner:resourceOwners) {
				if(!notifyList.contains(owner)) {
					notifyList.add(owner);
					notifications.add(getBestTarget(owner, subscribersItem, NotificationAs.RESOURCE_OWNER));
				}
			}
			
			// Notify course owners
			List<RepositoryEntry> courseReferences = referenceManager.getRepositoryReferencesTo(feedEntry.getOlatResource());
			courseReferences = courseReferences.stream()
					.filter(ref -> RepositoryEntryStatusEnum.isInArray(ref.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed()))
					.toList();
			List<Identity> owners = repositoryEntryRelationDao.getMembers(courseReferences, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			for(Identity owner:owners) {
				if(!notifyList.contains(owner)) {
					notifyList.add(owner);
					notifications.add(getBestTarget(owner, subscribersItem, NotificationAs.COURSE_OWNER));
				}
			}
		}
		
		// Notify author of the blog post
		if(item.getAuthorKey() != null) {
			Identity itemAuthor = securityManager.loadIdentityByKey(item.getAuthorKey());
			if(!notifyList.contains(itemAuthor)) {
				notifyList.add(itemAuthor);
				notifications.add(getBestTarget(itemAuthor, subscribersItem, NotificationAs.OTHER));
			}
		}
		
		// Process reply
		if(item != null && comment.getParent() != null && comment.getParent().getCreator() != null) {
			Identity identityToNotify = comment.getParent().getCreator();
			if(!notifyList.contains(identityToNotify)) {
				notifyList.add(identityToNotify);
				notifications.add(getBestTarget(identityToNotify, subscribersItem, NotificationAs.OTHER));
			}
		}
		return notifications;
	}

	/**
	 * Send the mail.
	 * 
	 * @param toNotify The notification information
	 * @param item The blog/podcast item
	 * @param comment The comment
	 * @param feedEntry The repository entry of the blog/podcast
	 */
	private void notify(Notification toNotify, Item item, UserComment comment, RepositoryEntry feedEntry) {
		Identity identityToNotify = toNotify.identity();
		String resourcePath = getResourcePath(identityToNotify, feedEntry, toNotify.publishers(), toNotify.as());
		if(resourcePath == null) {
			log.debug("No business path found for: {}", identityToNotify.getKey());
			return;
		}
		
		Locale locale = i18nManager.getLocaleOrDefault(identityToNotify.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(FeedMainController.class, locale,
				Util.createPackageTranslator(NotificationSubscriptionController.class, locale));

		String businessPath = resourcePath + "[FeedItem:" + item.getKey() + "][Comment:" + comment.getKey() + "]";
		String urlToSend = BusinessControlFactory.getInstance()
					.getURLFromBusinessPathString(businessPath);
		
		String body = buildBody(item, comment, urlToSend, translator);
		
		MailBundle bundle = new MailBundle();
		bundle.setToId(toNotify.identity());
		bundle.setContent("Hello", body);
		mailManager.sendExternMessage(bundle, new MailerResult(), true);
	}
	
	private String buildBody(Item item, UserComment comment, String urlToSend, Translator translator) {
		String[] args = new String[] {
			item.getTitle(),
			userManager.getUserDisplayName(comment.getCreator()),
			Formatter.getInstance(translator.getLocale()).formatDateAndTime(comment.getCreationDate())
		};
		
		StringBuilder sb = new StringBuilder();
		sb.append("<p>").append(translator.translate("notifications.comment", args)).append("</p>")
		  .append("<div class='o_email_box'>").append(comment.getComment()).append("</div>")
		  .append("<div class='o_email_button_group'><a class='o_email_button' href='").append(urlToSend).append("'>").append(translator.translate("notifications.comment.view")).append("</a></div>");
		
		return sb.toString();
	}
	
	/**
	 * Retrieve a business path the person to notify can access. A blog can stay
	 * in several courses owners by different users.
	 * 
	 * @param identity The identity to notify
	 * @param feedEntry The repository entry of the blog or the podcast
	 * @param publishers A list of already found publishers
	 * @param asOwner Notified as owners or something else
	 * @return The business path
	 */
	private String getResourcePath(Identity identity, RepositoryEntry feedEntry, List<Publisher> publishers, NotificationAs asOwner) {
		if(publishers.size() > 0) {
			Publisher publisher = publishers.get(0);
			return getContextBusinessPath(publisher);
		}
		if(asOwner == NotificationAs.RESOURCE_OWNER) {
			return "[RepositoryEntry:" + feedEntry.getKey() + "]";
		}
		
		List<PublisherSubscriber> membersPublishers = null;
		if(asOwner == NotificationAs.COURSE_OWNER) {
			membersPublishers = getMembersPublishers(identity, feedEntry, List.of(GroupRoles.owner));
		}
		if(membersPublishers == null || membersPublishers.isEmpty()) {
			membersPublishers = getMembersPublishers(identity, feedEntry, List.of(GroupRoles.coach, GroupRoles.participant));
		}
		
		if(membersPublishers.size() > 0) {
			PublisherSubscriber publisher = membersPublishers.get(0);
			if(publisher.subscriber() == null || publisher.subscriber().isEnabled()) {
				return getContextBusinessPath(publisher.publisher());
			}
		}
		return null;
	}
	
	private List<PublisherSubscriber> getMembersPublishers(IdentityRef identity, RepositoryEntry feedEntry, List<GroupRoles> roles) {
		OLATResource resource = feedEntry.getOlatResource();
		String resourceName =  resource.getResourceableTypeName().replace("FileResource", "Comment");
		return feedDao.getPublisherByType(identity,
				resourceName, resource.getResourceableId().toString(), resource.getKey(), roles);
	}
	
	private String getContextBusinessPath(Publisher publisher) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(publisher.getBusinessPath());
		if("CourseModule".equals(publisher.getResName()) && entries.size() >= 2) {
			entries = entries.subList(0, 2);
		} else if(entries.size() >= 1) {
			entries = entries.subList(0, 1);
		}
		return BusinessControlFactory.getInstance().getBusinessControlString(entries);
	}
	
	private Notification getBestTarget(Identity identity, List<Subscriber> subscribers, NotificationAs as) {
		int countEnabled = 0;
		int countDisabled = 0;
		List<Publisher> publishers = new ArrayList<>();
		for(Subscriber subscriber:subscribers) {
			if(identity.equals(subscriber.getIdentity())) {
				if(subscriber.isEnabled()) {
					countEnabled++;
					publishers.add(subscriber.getPublisher());
				} else {
					countDisabled++;
				}
			}
		}
		
		return new Notification(identity, publishers, as, countEnabled == 0 && countDisabled > 0);
	}
	
	public record Notification(Identity identity, List<Publisher> publishers, NotificationAs as, boolean veto) {
		//
	}
	
	public enum NotificationAs {
		COURSE_OWNER,
		RESOURCE_OWNER,
		OTHER
	}
}
