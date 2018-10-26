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

public abstract class NotificationsManager {
	
	
	protected static NotificationsManager INSTANCE = null;
	
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
	public static NotificationsManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Return all the subscription items for a defined user and restricted to a
	 * single type of publisher
	 * @param identity
	 * @param publisherType
	 * @return
	 */
	public abstract List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType);

	/**
	 * Notify all users by email with their notifications
	 */
	public abstract void notifyAllSubscribersByEmail();

	/**
	 * Send an email to a single user with the given set of subscription items
	 * @param curIdent
	 * @param items
	 * @param translator
	 * @param subscribersToUpdate
	 * @return
	 */
	public abstract boolean sendMailToUserAndUpdateSubscriber(Identity curIdent, List<SubscriptionItem> items, Translator translator, List<Subscriber> subscribersToUpdate);

	/**
	 * @param key
	 * @return the subscriber with this key or null if not found
	 */
	public abstract Subscriber getSubscriber(Long key);

	/**
	 * Get the publisher or return null if not exists
	 * @param subsContext
	 * @return the publisher belonging to the given context or null
	 */
	public abstract Publisher getPublisher(SubscriptionContext subsContext);
	
	/**
	 * Get or create the publisher
	 * @param scontext
	 * @param pdata
	 * @return
	 */
	public abstract Publisher getOrCreatePublisher(final SubscriptionContext scontext, final PublisherData pdata);
	
	public abstract List<Publisher> getAllPublisher();

	/**
	 * deletes all publishers of the given olatresourceable. e.g. ores =
	 * businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of
	 * Forum(toolforum)
	 * 
	 * @param ores
	 */
	public abstract int deletePublishersOf(OLATResourceable ores);


	/**
	 * @param identity
	 * @param publisher
	 * @return a Subscriber object belonging to the identity and listening to the
	 *         given publisher
	 */
	public abstract Subscriber getSubscriber(IdentityRef identity, Publisher publisher);
	
	/**
	 * Delete the subscriber with the specified primary key.
	 * 
	 * @param subscriberKey
	 * @return True if something was deleted.
	 */
	public abstract boolean deleteSubscriber(Long subscriberKey);
	
	/**
	 * Return all subscribers of a publisher
	 * @param publisher
	 * @return
	 */
	public abstract List<Subscriber> getSubscribers(Publisher publisher);
	
	/**
	 * Return identities of all subscribers of the publisher
	 * @param publisher
	 * @return
	 */
	public abstract List<Identity> getSubscriberIdentities(Publisher publisher);

	/**
	 * sets the latest visited date of the subscription to 'now' .assumes the
	 * identity is already subscribed to the publisher
	 * 
	 * @param identity
	 * @param subsContext
	 */
	public abstract void markSubscriberRead(Identity identity, SubscriptionContext subsContext);

	/**
	 * call this method to indicate that there is news for the given
	 * subscriptionContext
	 * 
	 * @param subscriptionContext
	 * @param ignoreNewsFor
	 */
	public abstract void markPublisherNews(SubscriptionContext subscriptionContext, Identity ignoreNewsFor, boolean sendEvent);
	
	public abstract void markPublisherNews(String publisherType, String data, Identity ignoreNewsFor, boolean sendEvent);

	public abstract void registerAsListener(GenericEventListener gel, Identity ident);

	public abstract void deregisterAsListener(GenericEventListener gel);
	
	/**
	 * get interval of identity.
	 * @param ident
	 * @return interval string or defaultinterval if not valid or not set
	 */
	public abstract String getUserIntervalOrDefault(Identity ident);
	
	/**
	 * calculate a Date from the past with given interval (now - interval)
	 * @param interval
	 * @return
	 */
	public abstract Date getCompareDateFromInterval(String interval);

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @return true if this user is subscribed
	 */
	public abstract boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext);

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
	public abstract void delete(SubscriptionContext scontext);
	
	/**
	 * delete the publisher and all subscribers to this publisher.
	 * @param publisher
	 */
	public abstract void deactivate(Publisher publisher);

	/**
	 * @param pub
	 * @return true if the publisher is valid (that is: has not been marked as
	 *         deleted)
	 */
	public abstract boolean isPublisherValid(Publisher pub);

	/**
	 * @param subscriber
	 * @param locale
	 * @param mimeType text/html or text/plain
	 * @return the item or null if there is currently no news for this subscription
	 */
	public abstract SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent);
	
	/**
	 * @param subscriber
	 * @param locale
	 * @param mimeType
	 * @param lowerDateBoundary The date from which the news should be collected
	 * @return
	 */
	public abstract SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent, Date lowerDateBoundary);

	/**
	 * Create a subscription item from info without reloading all the subscription
	 * @param subsInfo
	 * @param subscriber
	 * @param locale
	 * @param mimeTypeTitle
	 * @param mimeTypeContent
	 * @return
	 */
	public abstract SubscriptionItem createSubscriptionItem(SubscriptionInfo subsInfo, Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent);

	public abstract SubscriptionInfo getNoSubscriptionInfo();


	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person)
	 * 
	 * @param identity
	 * @return List of Subscriber Objects which belong to the identity
	 */
	public abstract List<Subscriber> getSubscribers(Identity identity);
	
	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person) restricted to the list of specified types
	 * @param identity
	 * @param types
	 * @return
	 */
	public abstract List<Subscriber> getSubscribers(IdentityRef identity, List<String> types);

	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person) restricted to the specified types
	 * 
	 * @param identity
	 * @return List of Subscriber Objects which belong to the identity
	 */
	public abstract List<Subscriber> getSubscribers(IdentityRef identity, long resId);
	
	/**
	 * @param identity
	 * @return a list of all subscribers which belong to the identity and which
	 *         publishers are valid
	 */
	public abstract List<Subscriber> getValidSubscribers(Identity identity);
	
	/**
	 * @param publisher
	 * @return
	 */
	public abstract List<Subscriber> getValidSubscribersOf(Publisher publisher);

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @param publisherData
	 */
	public abstract void subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);
	
	/**
	 * The method is equivalent to the method above but is done through the JMS server.
	 * 
	 * @param identity The identity which subscribe
	 * @param subscriptionContext The context
	 * @param publisherData The additional data
	 */
	public abstract void asyncSubscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);
	
	public abstract void subscribe(List<Identity> identities, SubscriptionContext subscriptionContext, PublisherData publisherData);

	public abstract void unsubscribe(Subscriber s);
	
	public abstract void unsubscribeAllForIdentityAndResId(IdentityRef identity, Long resId);

	/**
	 * @param identity
	 * @param subscriptionContext
	 */
	public abstract void unsubscribe(Identity identity, SubscriptionContext subscriptionContext);
	
	/**
	 * 
	 * @param identities
	 * @param subscriptionContext
	 */
	public abstract void unsubscribe(List<Identity> identities, SubscriptionContext subscriptionContext);
	
	/**
	 * @return the handler for the type
	 */
	public abstract NotificationsHandler getNotificationsHandler(Publisher publisher);

	/**
	 * @return the notification intervals
	 */
	public abstract List<String> getEnabledNotificationIntervals();

	/**
	 * @return the default notification interval
	 */
	public abstract String getDefaultNotificationInterval();

	/**
	 * @param subscriptionContext
	 * @param publisherData new data to write
	 */
	public abstract void updatePublisherData(SubscriptionContext subscriptionContext, PublisherData publisherData);
}