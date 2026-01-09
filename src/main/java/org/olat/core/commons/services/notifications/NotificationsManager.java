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
package org.olat.core.commons.services.notifications;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.GenericEventListener;

/**
 * Description:<br>
 * Notification Module gets started by the framework (spring) and initializes the
 * scheduler by value you specified in the
 * spring config file. When the time has come the notification manager gets
 * called and it searches for pending notifications by checking all the
 * subscriber objects which are persisted in the database (table: o_noti_sub).
 * For each subscription a user has he has an subscriber object. Inside the
 * subscriber object the times the user recently got informed are saved and by
 * comparing those dates to the value "latestNews" field each publisher
 * (o_noti_pub) has we can find out what we have to send to each users
 * subscriptions. To add a new publisher see classes implementing the
 * 
 * @see org.olat.core.commons.services.notifications.NotificationsHandler and the corresponding code
 *      in such as
 * @see org.olat.modules.fo.ui.ForumController where you add the subscription stuff
 *      to the constructor and for the action that creates a new entry you have
 *      to inform the
 * @see org.olat.core.commons.services.notifications.manager.NotificationsManagerImpl#markPublisherNews(SubscriptionContext,
 *      Identity)
 * Retrieve an instance of the notifications manager
 * @return
 */
public interface NotificationsManager {
	
	/**
	 * Return all the subscription items for a defined user and restricted to a
	 * single type of publisher
	 * @param identity
	 * @param publisherType
	 * @return
	 */
	public List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType);

	/**
	 * Notify all users by email with their notifications
	 */
	public void notifyAllSubscribersByEmail();

	/**
	 * Send an email to a single user with the given set of subscription items
	 * @param curIdent
	 * @param items
	 * @param translator
	 * @param subscribersToUpdate
	 * @return
	 */
	public boolean sendMailToUserAndUpdateSubscriber(Identity curIdent, List<SubscriptionItem> items, Translator translator, List<Subscriber> subscribersToUpdate);
	
	public boolean sendEmail(Identity to, Translator translator, List<SubscriptionItem> subItems);

	/**
	 * @param key
	 * @return the subscriber with this key or null if not found
	 */
	public Subscriber getSubscriber(Long key);

	/**
	 * Get the root publisher for the specified publication context
	 * or return null if not exists.
	 * 
	 * @param subsContext The context (course, resource, question pool)
	 * @return The root publisher belonging to the given context or null
	 */
	public Publisher getPublisher(SubscriptionContext subsContext);
	
	/**
	 * Get or create the publisher. 
	 * @param scontext
	 * @param pdata
	 * @return
	 */
	public Publisher getOrCreatePublisher(SubscriptionContext scontext, PublisherData pdata);
	
	/**
	 * Get or create a publisher hold by a parent one.
	 * 
	 * @param scontext The publication context
	 * @param pdata The publisher date
	 * @param parent The container publisher
	 * @return A new publisher
	 */
	public Publisher getOrCreatePublisherWithData(SubscriptionContext scontext, PublisherData pdata, Publisher parent, PublisherChannel channel);

	/**
	 * deletes all publishers of the given olatresourceable. e.g. ores =
	 * businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of
	 * Forum(toolforum)
	 * 
	 * @param ores
	 */
	public int deletePublishersOf(OLATResourceable ores);


	/**
	 * @param identity
	 * @param publisher
	 * @return a Subscriber object belonging to the identity and listening to the
	 *         given publisher
	 */
	public Subscriber getSubscriber(IdentityRef identity, Publisher publisher);
	
	/**
	 * 
	 * @param identity The identity which subscribes
	 * @param publishers A list of publishers
	 * @return List of subscriptions
	 */
	public List<Subscriber> getSubscribers(IdentityRef identity, List<Publisher> publishers);
	
	/**
	 * Has at least an enabled subscriber
	 * 
	 * @param identity The identity
	 * @param publishers A list of publishers
	 * @return true if at least the identity subscribed to a publisher
	 */
	public boolean hasSubscribers(IdentityRef identity, List<Publisher> publishers);
	
	/**
	 * Get the subscriber (enable or not) of a user by context. This is the root publisher
	 * the context.
	 * 
	 * @param identity The identity
	 * @param subscriptionContext The resource context
	 * @return A subscriber (publisher is not fetched)
	 */
	public Subscriber getSubscriber(Identity identity, SubscriptionContext subscriptionContext);
	

	public Subscriber getSubscriber(Identity identity, SubscriptionContext subscriptionContext, PublisherData data);
	
	/**
	 * Delete the subscriber with the specified primary key.
	 * 
	 * @param subscriberKey
	 * @return True if something was deleted.
	 */
	public boolean deleteSubscriber(Long subscriberKey);
	
	/**
	 * Return all subscribers of a publisher
	 * 
	 * @param publisher The publisher
	 * @param enabledOnly true if restrict to enabled subscribers
	 * @return A list of subscribers
	 */
	public List<Subscriber> getSubscribers(Publisher publisher, boolean enabledOnly);
	
	/**
	 * Return identities of all enabled subscribers of the publisher.
	 * 
	 * @param publisher The publisher
	 * @param enabledOnly true to exclude disabled subscriptions
	 * @return A list of identities
	 */
	public List<Identity> getSubscriberIdentities(Publisher publisher, boolean enabledOnly);

	/**
	 * sets the latest visited date of the subscription to 'now' .assumes the
	 * identity is already subscribed to the publisher
	 * 
	 * @param identity
	 * @param subsContext
	 */
	public void markSubscriberRead(Identity identity, SubscriptionContext subsContext);

	/**
	 * The method update only the root publisher and not the sub-publishers!
	 * 
	 * @param subscriptionContext The context
	 * @param ignoreNewsFor
	 */
	public void markPublisherNews(SubscriptionContext subscriptionContext, Identity ignoreNewsFor, boolean sendEvent);
	
	/**
	 * The method will update the specified publisher and eventually the parent publisher
	 * if the parent is PULL and the specified one not.
	 * 
	 * @param publisher The publisher
	 * @param ignoreNewsFor Ignore the news for this identity
	 * @param sendEvent Send multi-user events
	 */
	public void markPublisherNews(Publisher publisher, Identity ignoreNewsFor, boolean sendEvent);
	
	/**
	 * The method will update all publisher with the same type and data and eventually the parent publishers
	 * if the parent is PULL and these ones not.
	 * 
	 * @param publisherType The type of publisher
	 * @param data The data
	 * @param ignoreNewsFor Ignore the news for this identity
	 * @param sendEvent Send multi-user events
	 */
	public void markPublisherNews(String publisherType, String data, Identity ignoreNewsFor, boolean sendEvent);

	public void registerAsListener(GenericEventListener gel, Identity ident);

	public void deregisterAsListener(GenericEventListener gel);
	
	/**
	 * get interval of identity.
	 * @param ident
	 * @return interval string or defaultinterval if not valid or not set
	 */
	public String getUserIntervalOrDefault(Identity ident);
	
	/**
	 * calculate a Date from the past with given interval (now - interval)
	 * @param interval
	 * @return
	 */
	public Date getCompareDateFromInterval(String interval);
	
	public Date getNextMail(Date date, String interval);
	
	public void updateIntervalSubscriptionMail(Identity identity, String interval);

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @return true if this user is subscribed
	 */
	public boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext);

	/**
	 * marks the publisher as deleted. It cannot delete the publisher, since most
	 * often there are subscribers listening to the publisher. Instead, the
	 * subscriptioncontext of the publisher is set to null,null,null (so a new
	 * publisher with the name ores is possible) and the state set to
	 * PUB_STATE_DELETED <br>
	 * only the resName, resId, and subIdentifier of the subscriptioncontext are
	 * used in this method
	 * 
	 * @param scontext the subscriptioncontext
	 */
	public void delete(SubscriptionContext scontext);
	
	/**
	 * delete the publisher and all subscribers to this publisher.
	 * @param publisher
	 */
	public void deactivate(Publisher publisher);

	/**
	 * @param pub
	 * @return true if the publisher is valid (that is: has not been marked as
	 *         deleted)
	 */
	public boolean isPublisherValid(Publisher pub);

	/**
	 * @param subscriber
	 * @param locale
	 * @param mimeType text/html or text/plain
	 * @return the item or null if there is currently no news for this subscription
	 */
	public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent);
	
	/**
	 * @param subscriber
	 * @param locale
	 * @param mimeType
	 * @param lowerDateBoundary The date from which the news should be collected
	 * @return
	 */
	public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent, Date lowerDateBoundary);

	/**
	 * Create a subscription item from info without reloading all the subscription
	 * @param subsInfo
	 * @param subscriber
	 * @param locale
	 * @param mimeTypeTitle
	 * @param mimeTypeContent
	 * @return
	 */
	public SubscriptionItem createSubscriptionItem(SubscriptionInfo subsInfo, Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent);

	public SubscriptionInfo getNoSubscriptionInfo();
	
	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person) restricted to the list of specified types
	 * @param identity
	 * @param types
	 * @return
	 */
	public List<Subscriber> getSubscribers(IdentityRef identity, List<String> types, PublisherChannel channel, boolean enabledOnly, boolean withSubPublishers);

	
	/**
	 * @param identity
	 * @return a list of all subscribers which belong to the identity and which
	 *         publishers are valid
	 */
	public List<Subscriber> getValidSubscribers(Identity identity);
	
	/**
	 * @param publisher
	 * @return
	 */
	public List<Subscriber> getValidSubscribersOf(Publisher publisher);

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @param publisherData
	 * @return subscriber object, either a newly created or an already existing one
	 */
	Subscriber subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);
	

	Subscriber subscribe(Identity identity, Publisher publisher);
	
	/**
	 * The method is equivalent to the method above but is done through the JMS server.
	 * 
	 * @param identity The identity which subscribe
	 * @param subscriptionContext The context
	 * @param publisherData The additional data
	 */
	public void asyncSubscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);
	
	public void subscribe(List<Identity> identities, SubscriptionContext subscriptionContext, PublisherData publisherData);
	
	public void initialSubscription(List<Identity> identities, SubscriptionContext subscriptionContext, PublisherData publisherData);

	public void unsubscribe(Subscriber s);
	
	public void unsubscribeAllForIdentityAndResId(IdentityRef identity, Long resId);

	/**
	 * Unsuscribe all publishers of the specified context.
	 * 
	 * @param identity The identity
	 * @param subscriptionContext The context
	 */
	public void unsubscribeContext(Identity identity, SubscriptionContext subscriptionContext);
	
	/**
	 * Unsuscribe the specified publisher
	 * 
	 * @param identity The identity
	 * @param publisher The publisher
	 */
	public void unsubscribe(Identity identity, Publisher publisher);
	

	/**
	 * @return the handler for the type
	 */
	public NotificationsHandler getNotificationsHandler(Publisher publisher);

	/**
	 * @return the notification intervals
	 */
	public List<String> getEnabledNotificationIntervals();

	/**
	 * @return the default notification interval
	 */
	public String getDefaultNotificationInterval();
	
	public Date getDefaultCompareDate();

	/**
	 * @param subscriptionContext
	 * @param publisherData new data to write
	 */
	public Publisher updatePublisherData(SubscriptionContext subscriptionContext, PublisherData publisherData);

	/**
	 * update all subscribers regarding their enabled status for the given publisher
	 *
	 * @param publisher
	 * @param subscriptionStatus enabled/disabled
	 */
	void updateAllSubscribers(Publisher publisher, boolean subscriptionStatus);

	/**
	 * update subscriptionStatus (enabled/disabled) for a specific subscriber
	 *
	 * @param subscriber
	 * @param subscriptionStatus
	 */
	void updateSubscriber(Subscriber subscriber, boolean subscriptionStatus);

	/**
	 * Merge the state of the given entity into the current persistence context.
	 *
	 * @return Subscriber object
	 */
	Subscriber mergeSubscriber(Subscriber subscriber);

	/**
	 * Creates a new subscriber for the given identity and subscription context if one does not already exist.
	 * The new subscriber is persisted in a disabled state (i.e., not actively subscribed).
	 * If a subscriber already exists (enabled or disabled), it is returned as-is without any modifications.
	 * This method is useful for pre-initializing subscription data while deferring actual activation
	 * until the user explicitly opts in.
	 *
	 * @param identity The identity (user) for whom the subscriber should be created.
	 * @param context The subscription context that defines the publisher to subscribe to.
	 * @param data The data used to create the publisher if it does not already exist.
	 * @return The existing or newly created (disabled) subscriber.
	 */
	Subscriber createDisabledSubscriberIfAbsent(Identity identity, Publisher publisher);
}