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

package org.olat.notifications;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.olat.ControllerFactory;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.CoreBeanTypes;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.event.EventFactory;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.SubscriptionItem;
import org.olat.core.util.resource.OresHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;

/**
 * Description: <br>
 * see org.olat.core.util.notifications.NotificationsManager
 * 
 * Initial Date: 21.10.2004 <br>
 * @author Felix Jost
 */
public class NotificationsManagerImpl extends NotificationsManager implements UserDataDeletable {
	private static final OLog log = Tracing.createLoggerFor(NotificationsManagerImpl.class);

	private static final int PUB_STATE_OK = 0;
	private static final int PUB_STATE_NOT_OK = 1;
	private static final String LATEST_EMAIL_USER_PROP = "noti_latest_email";
	private final SubscriptionInfo NOSUBSINFO = new NoSubscriptionInfo();

	private final OLATResourceable oresMyself = OresHelper.lookupType(NotificationsManagerImpl.class);

	private Map<String, NotificationsHandler> notificationHandlers;
	
	private List<String> notificationIntervals;
	private String defaultNotificationInterval;
	private static final Map<String, Integer> INTERVAL_DEF_MAP = buildIntervalMap();
	private Object lockObject = new Object();
	
	/**
	 * [used by spring]
	 * @param userDeletionManager
	 */
	private NotificationsManagerImpl(UserDeletionManager userDeletionManager) {
		// private since singleton
		userDeletionManager.registerDeletableUserData(this);
		INSTANCE = this;
	}

	/**
	 * @param resName
	 * @param resId
	 * @param subidentifier
	 * @param type
	 * @param data
	 * @return a persisted publisher with ores/subidentifier as the composite
	 *         primary key
	 */
	private Publisher createAndPersistPublisher(String resName, Long resId, String subidentifier, String type, String data, String businessPath) {
		if (resName == null || resId == null || subidentifier == null) throw new AssertException(
				"resName, resId, and subidentifier must not be null");
		
		if(businessPath != null && businessPath.length() > 230) {
			log.error("Businesspath too long for publisher: " + resName + " with business path: " + businessPath);
			businessPath = businessPath.substring(0, 230);
		}
		PublisherImpl pi = new PublisherImpl(resName, resId, subidentifier, type, data, businessPath, new Date(), PUB_STATE_OK);
		DBFactory.getInstance().saveObject(pi);
		return pi;
	}

	/**
	 * @param persistedPublisher
	 * @param listener
	 * @param subscriptionContext the context of the object we subscribe to
	 * @return a subscriber with a db key
	 */
	private Subscriber doCreateAndPersistSubscriber(Publisher persistedPublisher, Identity listener) {
		SubscriberImpl si = new SubscriberImpl(persistedPublisher, listener);
		si.setLastModified(new Date());
		si.setLatestEmailed(new Date());
		DBFactory.getInstance().saveObject(si);
		return si;
	}

	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person)
	 * 
	 * @param identity
	 * @return List of Subscriber Objects which belong to the identity
	 */
	public List<Subscriber> getSubscribers(Identity identity) {
		return getSubscribers(identity, Collections.<String>emptyList());
	}

	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person) restricted to the specified types
	 * 
	 * @param identity
	 * @return List of Subscriber Objects which belong to the identity
	 */
	@Override
	public List<Subscriber> getSubscribers(Identity identity, List<String> types) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sub from ").append(SubscriberImpl.class.getName()).append(" as sub ")
		  .append("inner join fetch sub.publisher as publisher ")
		  .append("where sub.identity = :anIdentity");
		if(types != null && !types.isEmpty()) {
			sb.append(" and publisher.type in (:types)");
		}
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setEntity("anIdentity", identity);
		if(types != null && !types.isEmpty()) {
			query.setParameterList("types", types);
		}
		@SuppressWarnings("unchecked")
		List<Subscriber> res = query.list();
		return res;
	}

	/**
	 * @param identity
	 * @return a list of all subscribers which belong to the identity and which
	 *         publishers are valid
	 */
	@SuppressWarnings("unchecked")
	public List<Subscriber> getValidSubscribers(Identity identity) {
		//pub.getState() == PUB_STATE_OK;
		DB db = DBFactory.getInstance();
		String q = "select sub from org.olat.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher as pub"
				+ " where sub.identity = :anIdentity" + " and pub.state = :aState";
		DBQuery query = db.createQuery(q);
		query.setEntity("anIdentity", identity);
		query.setLong("aState", PUB_STATE_OK);
		List<Subscriber> res = query.list();
		return res;
	}

	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getValidSubscribersOf(org.olat.core.util.notifications.Publisher)
	 */
	@SuppressWarnings("unchecked")
	public List<Subscriber> getValidSubscribersOf(Publisher publisher) {
		DB db = DBFactory.getInstance();
		String q = "select sub from org.olat.notifications.SubscriberImpl sub inner join fetch sub.identity" 
				+ " where sub.publisher = :publisher"
				+ " and sub.publisher.state = "+PUB_STATE_OK;
		DBQuery query = db.createQuery(q);
		query.setEntity("publisher", publisher);
		List<Subscriber> res = query.list();
		return res;
	}
	
	
	/**
	 * @return a list of subscribers ordered by the name of the identity of the
	 *         subscription
	 */
	@SuppressWarnings("unchecked")
	private List<Subscriber> getAllValidSubscribers() {
		DB db = DBFactory.getInstance();
		String q = "select sub from org.olat.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher as pub"
				+ " where pub.state = :aState" + " order by sub.identity.name";
		DBQuery query = db.createQuery(q);
		query.setLong("aState", PUB_STATE_OK);
		List<Subscriber> res = query.list();
		return res;
	}
	
	@Override
	public List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sub from ").append(SubscriberImpl.class.getName()).append(" sub")
			.append(" inner join fetch sub.publisher as pub")
			.append(" where sub.identity=:identity and pub.type=:type and pub.state=:aState");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setLong("aState", PUB_STATE_OK);
		query.setString("type", publisherType);
		query.setEntity("identity", identity);
		
		@SuppressWarnings("unchecked")
		List<Subscriber> subscribers = query.list();
		if(subscribers.isEmpty()) {
			return Collections.emptyList();
		}

		Locale locale = new Locale(identity.getUser().getPreferences().getLanguage());
		Date compareDate = getDefaultCompareDate();
		List<SubscriptionInfo> sis = new ArrayList<SubscriptionInfo>();
		for(Subscriber subscriber : subscribers){
			Publisher pub = subscriber.getPublisher();
			NotificationsHandler notifHandler = getNotificationsHandler(pub);
			// do not create subscription item when deleted
			if (isPublisherValid(pub)) {
				SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
				if (subsInfo.hasNews()) {
					sis.add(subsInfo);
				}
			}
		}
		return sis;
	}

	public void notifyAllSubscribersByEmail() {
		logAudit("starting notification cronjob for email sending", null);
		List<Subscriber> subs = getAllValidSubscribers();
		// ordered by identity.name!
		
		List<SubscriptionItem> items = new ArrayList<SubscriptionItem>();
		List<Subscriber> subsToUpdate = null;
		StringBuilder mailLog = new StringBuilder();
		StringBuilder mailErrorLog = new StringBuilder();
		
		boolean veto = false;
		Subscriber latestSub = null;
		Identity ident = null;
		Translator translator = null;
		Locale locale = null;
		
		Date defaultCompareDate = getDefaultCompareDate();
		long start = System.currentTimeMillis();
		
		// loop all subscriptions, as its ordered by identity, they get collected for each identity
		for(Subscriber sub : subs){
			try {
				ident = sub.getIdentity();

				if (latestSub == null || (!ident.equalsByPersistableKey(latestSub.getIdentity()))) { 
					// first time or next identity => prepare for a new user and send old data.
					
					// send a mail
					notifySubscribersByEmail(latestSub, items, subsToUpdate, translator, start, veto, mailLog, mailErrorLog);
					
					// prepare for new user
					start = System.currentTimeMillis();
					locale = I18nManager.getInstance().getLocaleOrDefault(ident.getUser().getPreferences().getLanguage());
					translator = Util.createPackageTranslator(NotificationsManagerImpl.class, locale);
					items = new ArrayList<SubscriptionItem>();
					subsToUpdate = new ArrayList<Subscriber>();
					latestSub = sub;
					veto = false;
					
					PropertyManager pm = PropertyManager.getInstance();
				  Property p = pm.findProperty(ident, null, null, null, LATEST_EMAIL_USER_PROP);
				  if(p != null) {
				  	Date latestEmail = new Date(p.getLongValue());
						String userInterval = getUserIntervalOrDefault(ident);
				  	Date compareDate = getCompareDateFromInterval(userInterval);
				  	if(latestEmail.after(compareDate)) {
				  		veto = true;
				  	}
				  }
				}
				
				if(veto) {
					continue;
				}
				// only send notifications to active users
				if(ident.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) >= 0) {
					continue;
				}
				// this user doesn't want notifications
				String userInterval = getUserIntervalOrDefault(ident);
				if("never".equals(userInterval)) {
					continue;
				}
				
				// find out the info that happened since the date the last email was sent. Only those infos need to be emailed. 
				// mail is only sent if users interval is over.
				Date compareDate = getCompareDateFromInterval(userInterval);
				Date latestEmail = sub.getLatestEmailed();
				
				SubscriptionItem subsitem = null;
				if (latestEmail == null || compareDate.after(latestEmail)){
					// no notif. ever sent until now
					if (latestEmail == null) {
						latestEmail = defaultCompareDate;
					}	else if (latestEmail.before(defaultCompareDate)) {
						//no notification older than a month
						latestEmail = defaultCompareDate;
					}
					subsitem = createSubscriptionItem(sub, locale, SubscriptionInfo.MIME_PLAIN, SubscriptionInfo.MIME_PLAIN, latestEmail);
				}	else if(latestEmail != null && latestEmail.after(compareDate)) {
					//already send an email within the user's settings interval
					veto = true;
				}
				if (subsitem != null) {
					items.add(subsitem);
					subsToUpdate.add(sub);
				}
			} catch(Error er) {
				logError("Error in NotificationsManagerImpl.notifyAllSubscribersByEmail, ", er);
				throw er;
			} catch(RuntimeException re) {
				logError("RuntimeException in NotificationsManagerImpl.notifyAllSubscribersByEmail,", re);
				throw re;
			} catch(Throwable th) {
				logError("Throwable in NotificationsManagerImpl.notifyAllSubscribersByEmail,", th);
			}
		} // for
		
		// done, purge last entry
		notifySubscribersByEmail(latestSub, items, subsToUpdate, translator, start, veto, mailLog, mailErrorLog);
		
		// purge logs
		if (mailErrorLog.length() > 0) {
			logAudit("error sending email to the following identities: "+mailErrorLog.toString(),null);
		}
		logAudit("sent email to the following identities: "+mailLog.toString(), null);
	}
	
	private void notifySubscribersByEmail(Subscriber latestSub, List<SubscriptionItem> items, List<Subscriber> subsToUpdate, Translator translator, long start, boolean veto, StringBuilder mailLog, StringBuilder mailErrorLog) {
		if(veto) {
			if(latestSub != null) {
				mailLog.append(latestSub.getIdentity().getName()).append(" already received email within prefs interval, ");
			}
		} else if (items.size() > 0) {
			Identity curIdent = latestSub.getIdentity();
			boolean sentOk = sendMailToUserAndUpdateSubscriber(curIdent, items, translator, subsToUpdate);
			if (sentOk) {
				PropertyManager pm = PropertyManager.getInstance();
			  Property p = pm.findProperty(curIdent, null, null, null, LATEST_EMAIL_USER_PROP);
			  if(p == null) {
			  	p = pm.createUserPropertyInstance(curIdent, null, LATEST_EMAIL_USER_PROP, null, null, null, null);
			  	p.setLongValue(new Date().getTime());
				  pm.saveProperty(p);
			  } else {
			  	p.setLongValue(new Date().getTime());
				  pm.updateProperty(p);
			  }

				mailLog.append(curIdent.getName()).append(' ')
					.append(items.size()).append(' ')
					.append((System.currentTimeMillis() - start)).append("ms, ");
			} else {
				mailErrorLog.append(curIdent.getName()).append(", ");
			}
		}
		//collecting the SubscriptionItem can potentially make a lot of DB calls
		DBFactory.getInstance().intermediateCommit();
	}

	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getCompareDateFromInterval(java.lang.String)
	 */
	public Date getCompareDateFromInterval(String interval){
		Calendar calNow = Calendar.getInstance();
		// get hours to subtract from now
		Integer diffHours = INTERVAL_DEF_MAP.get(interval);
		calNow.add(Calendar.HOUR_OF_DAY, -diffHours);
		Date compareDate = calNow.getTime();
		return compareDate;		
	}
	
	/**
	 * Needs to correspond to notification-settings. 
	 * all available configs should be contained in the map below!
	 * @return
	 */
	private static final Map<String, Integer> buildIntervalMap(){
		Map<String, Integer> intervalDefMap = new HashMap<String, Integer>();		
		intervalDefMap.put("never", 0);
		intervalDefMap.put("monthly", 720);
		intervalDefMap.put("weekly", 168);
		intervalDefMap.put("daily", 24);
		intervalDefMap.put("half-daily", 12);
		intervalDefMap.put("four-hourly", 4);
		intervalDefMap.put("two-hourly", 2);
		return intervalDefMap;
	}
	
	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getUserIntervalOrDefault(org.olat.core.id.Identity)
	 */
	public String getUserIntervalOrDefault(Identity ident){
		String userInterval = ident.getUser().getPreferences().getNotificationInterval();
		if (!StringHelper.containsNonWhitespace(userInterval)) userInterval = getDefaultNotificationInterval();
		List<String> avIntvls = getEnabledNotificationIntervals();
		if (!avIntvls.contains(userInterval)) {
			logWarn("User " + ident.getName() + " has an invalid notification-interval (not found in config): " + userInterval, null);
			userInterval = getDefaultNotificationInterval();
		}
		return userInterval;
	}
	
	
	public boolean sendMailToUserAndUpdateSubscriber(Identity curIdent, List<SubscriptionItem> items, Translator translator, List<Subscriber> subscribersToUpdate) {
		boolean sentOk = sendEmail(curIdent, translator.translate("rss.title", new String[] { NotificationHelper.getFormatedName(curIdent) }), items);
		// save latest email sent date for the subscription just emailed
		// do this only if the mail was successfully sent
		if (sentOk) {
			for (Iterator<Subscriber> it_subs = subscribersToUpdate.iterator(); it_subs.hasNext();) {
				Subscriber subscriber = it_subs.next();
				subscriber.setLatestEmailed(new Date());
				updateSubscriber(subscriber);
			}
		}
		return sentOk;
	}
	
	private boolean sendEmail(Identity to, String title, List<SubscriptionItem> subItems) {
		StringBuilder plaintext = new StringBuilder();
		for (Iterator<SubscriptionItem> it_subs = subItems.iterator(); it_subs.hasNext();) {
			SubscriptionItem subitem = it_subs.next();
			plaintext.append(subitem.getTitle());
			if(StringHelper.containsNonWhitespace(subitem.getLink())) {
				plaintext.append("\n");
				plaintext.append(subitem.getLink());
			}
			plaintext.append("\n");
			if(StringHelper.containsNonWhitespace(subitem.getDescription())) {
				plaintext.append(subitem.getDescription());
			}
			plaintext.append("\n\n");
		}
		String mailText = plaintext.toString();
		MailTemplate mailTempl = new MailTemplate(title, mailText, null) {

			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
			// nothing to do
			}
		};
		
		MailerResult result = null;
		try {
			// fxdiff VCRP-16: intern mail system
			result = MailerWithTemplate.getInstance().sendRealMail(to, mailTempl);
		} catch (Exception e) {
			// FXOLAT-294 :: sending the mail will throw nullpointer exception if To-Identity has no
			// valid email-address!, catch it...
		} 
		if (result == null || result.getReturnCode() > 0) {
			if(result!=null)
				log.warn("Could not send email to identity " + to.getName() + ". (returncode=" + result.getReturnCode() + ", to=" + to + ")");
			else
				log.warn("Could not send email to identity " + to.getName() + ". (returncode = null) , to=" + to + ")");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param key
	 * @return the subscriber with this key or null if not found
	 */
	@SuppressWarnings("unchecked")
	public Subscriber getSubscriber(Long key) {
		DB db = DBFactory.getInstance();
		String q = "select sub from org.olat.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher " + " where sub.key = :aKey";
		DBQuery query = db.createQuery(q);
		query.setLong("aKey", key.longValue());
		List<Subscriber> res = query.list();
		int cnt = res.size();
		if (cnt == 0) return null;
		if (cnt > 1) throw new AssertException("more than one subscriber for key " + key);
		return res.get(0);
	}

	/**
	 * @param scontext
	 * @param pdata
	 * @return the publisher
	 */
	private Publisher findOrCreatePublisher(final SubscriptionContext scontext, final PublisherData pdata) {
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(scontext.getResName() + "_" + scontext.getSubidentifier(),scontext.getResId());
		//o_clusterOK by:cg
		//fxdiff VCRP-16:prevent nested doInSync
		Publisher pub = getPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier());
		if(pub != null) {
			return pub;
		}
		
		Publisher publisher = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Publisher>(){
			public Publisher execute() {
				Publisher p = getPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier());
				// if not found, create it
				if (p == null) {
					p = createAndPersistPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier(), pdata.getType(), pdata
							.getData(), pdata.getBusinessPath());
				}
				if (p.getData() == null || !p.getData().startsWith("[")) {
					//update silently the publisher
					if(pdata.getData() != null) {
						//updatePublisher(p, pdata.getData());
					}
				}
				return p;
			}
		});
		return publisher;
	}

	/**
	 * @param subsContext
	 * @return the publisher belonging to the given context or null
	 */
	public Publisher getPublisher(SubscriptionContext subsContext) {
		return getPublisher(subsContext.getResName(), subsContext.getResId(), subsContext.getSubidentifier());
	}
	
	public List<Publisher> getAllPublisher() {
		DB db = DBFactory.getInstance();
		String q = "select pub from org.olat.notifications.PublisherImpl pub";
		DBQuery query = db.createQuery(q);
		return query.list();
	}

	/**
	 * return the publisher for the given composite primary key ores +
	 * subidentifier.
	 */
	@SuppressWarnings("unchecked")
	private Publisher getPublisher(String resName, Long resId, String subidentifier) {
		DB db = DBFactory.getInstance();
		String q = "select pub from org.olat.notifications.PublisherImpl pub" + " where pub.resName = :resName" + " and pub.resId = :resId"
				+ " and pub.subidentifier = :subidentifier";
		DBQuery query = db.createQuery(q);
		query.setString("resName", resName);
		query.setLong("resId", resId.longValue());
		query.setString("subidentifier", subidentifier);
		List<Publisher> res = query.list();
		if (res.size() == 0) return null;
		if (res.size() != 1) throw new AssertException("only one subscriber per person and publisher!!");
		Publisher p = res.get(0);
		return p;
	}

	/**
	 * @param resName
	 * @param resId
	 * @return a list of publishers belonging to the resource
	 */
	@SuppressWarnings("unchecked")
	private List<Publisher> getPublishers(String resName, Long resId) {
		DB db = DBFactory.getInstance();
		String q = "select pub from org.olat.notifications.PublisherImpl pub" + " where pub.resName = :resName" + " and pub.resId = :resId";
		DBQuery query = db.createQuery(q);
		query.setString("resName", resName);
		query.setLong("resId", resId.longValue());
		List<Publisher> res = query.list();
		return res;
	}

	/**
	 * deletes all publishers of the given olatresourceable. e.g. ores =
	 * businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of
	 * Forum(toolforum)
	 * 
	 * @param ores
	 */
	public void deletePublishersOf(OLATResourceable ores) {
		String type = ores.getResourceableTypeName();
		Long id = ores.getResourceableId();
		if (type == null || id == null) throw new AssertException("type/id cannot be null! type:" + type + " / id:" + id);
		List<Publisher> pubs = getPublishers(type, id);
		for (Iterator<Publisher> it_pub = pubs.iterator(); it_pub.hasNext();) {
			Publisher pub = it_pub.next();
			
			//grab all subscribers to the publisher and delete them
			List<Subscriber> subs = getValidSubscribersOf(pub);
			for (Iterator<Subscriber> iterator = subs.iterator(); iterator.hasNext();) {
				Subscriber sub = iterator.next();
				unsubscribe(sub);
			}
			deletePublisher(pub);
		}
	}

	/**
	 * @param identity
	 * @param publisher
	 * @return a Subscriber object belonging to the identity and listening to the
	 *         given publisher
	 */
	@SuppressWarnings("unchecked")
	public Subscriber getSubscriber(Identity identity, Publisher publisher) {
		String q = "select sub from org.olat.notifications.SubscriberImpl sub where sub.publisher = :publisher"
				+ " and sub.identity = :identity";
		DBQuery query = DBFactory.getInstance().createQuery(q);
		query.setEntity("publisher", publisher);
		query.setEntity("identity", identity);
		List<Subscriber> res = query.list();
		if (res.size() == 0) return null;
		if (res.size() != 1) throw new AssertException("only one subscriber per person and publisher!!");
		Subscriber s = res.get(0);
		return s;
	}
	
	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getSubscriber(org.olat.core.util.notifications.Publisher)
	 */
	@Override
	public List<Subscriber> getSubscribers(Publisher publisher) {
		String q = "select sub from org.olat.notifications.SubscriberImpl sub where sub.publisher = :publisher";
		DBQuery query = DBFactory.getInstance().createQuery(q);
		query.setEntity("publisher", publisher);
		List<Subscriber> res = query.list();
		return res;
	}
	
	
	/**
	 * 
	 * @see org.olat.core.util.notifications.NotificationsManager#getSubscriberIdentities(org.olat.core.util.notifications.Publisher)
	 */
	@Override
	public List<Identity> getSubscriberIdentities(Publisher publisher) {
		String q = "select sub.identity from org.olat.notifications.SubscriberImpl sub where sub.publisher = :publisher";
		DBQuery query = DBFactory.getInstance().createQuery(q);
		query.setEntity("publisher", publisher);
		List<Identity> res = query.list();
		return res;
	}

	/**
	 * @return the handler for the type
	 */
	public NotificationsHandler getNotificationsHandler(Publisher publisher) {
		String type = publisher.getType();
		if (notificationHandlers == null) {
			synchronized(lockObject) {
				if (notificationHandlers == null) { // check again in synchronized-block, only one may create list
					notificationHandlers = new HashMap<String,NotificationsHandler>();
					Map<String, Object> notificationsHandlerMap = CoreSpringFactory.getBeansOfType(CoreBeanTypes.notificationsHandler);
					Collection<Object> notificationsHandlerValues = notificationsHandlerMap.values();
					for (Object object : notificationsHandlerValues) {
						NotificationsHandler notificationsHandler = (NotificationsHandler) object;
						log.debug("initNotificationUpgrades notificationsHandler=" + notificationsHandler);
						notificationHandlers.put(notificationsHandler.getType(), notificationsHandler);
					}
				}
			}	
		}
		return notificationHandlers.get(type);		
	}

	/**
	 * @param subscriber
	 */
	private void deleteSubscriber(Subscriber subscriber) {
		DB db = DBFactory.getInstance();
		db.deleteObject(subscriber);
	}

	/**
	 * @param subscriber
	 */
	private void updateSubscriber(Subscriber subscriber) {
		subscriber.setLastModified(new Date());
		DBFactory.getInstance().updateObject(subscriber);
	}

	/**
	 * @param publisher
	 */
	public void updatePublisher(Publisher publisher) {
		DBFactory.getInstance().updateObject(publisher);
	}
	
	/**
	 * @param publisher
	 */
	public void deletePublisher(Publisher publisher) {
		DBFactory.getInstance().deleteObject(publisher);
	}

	/**
	 * sets the latest visited date of the subscription to 'now' .assumes the
	 * identity is already subscribed to the publisher
	 * 
	 * @param identity
	 * @param subsContext
	 */
	public void markSubscriberRead(Identity identity, SubscriptionContext subsContext) {
		Publisher p = getPublisher(subsContext.getResName(), subsContext.getResId(), subsContext.getSubidentifier());
		if (p == null) throw new AssertException("cannot markRead for identity " + identity.getName()
				+ ", since the publisher for the given subscriptionContext does not exist: subscontext = " + subsContext);
		Subscriber sub = getSubscriber(identity, p);
		if (sub == null) throw new AssertException("cannot markRead, since identity " + identity.getName()
				+ " is not subscribed to subscontext " + subsContext);
		updateSubscriber(sub);
	}

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @param publisherData
	 */
	public void subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData) {
		// no need to sync, since an identity only has one gui thread / one mouse
		Publisher p = findOrCreatePublisher(subscriptionContext, publisherData);
		Subscriber s = getSubscriber(identity, p);
		if (s == null) {
			// no subscriber -> create.
			// s.latestReadDate >= p.latestNewsDate == no news for subscriber when no
			// news after subscription time
			doCreateAndPersistSubscriber(p, identity);
		}
	}

	/**
	 * call this method to indicate that there is news for the given
	 * subscriptionContext
	 * 
	 * @param subscriptionContext
	 * @param ignoreNewsFor
	 */
	public void markPublisherNews(final SubscriptionContext subscriptionContext, Identity ignoreNewsFor) {
		// to make sure: ignore if no subscriptionContext
		if (subscriptionContext == null) return;
		final Date now = new Date();

		// two threads with both having a publisher they want to update
		//o_clusterOK by:cg
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(subscriptionContext.getResName() + "_" + subscriptionContext.getSubidentifier(),subscriptionContext.getResId());
		Publisher publisher = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Publisher>(){
			public Publisher execute() {
				Publisher p = getPublisher(subscriptionContext);
				// if no publisher yet, ignore
				//TODO: check if that can be null
				if (p == null) return null;
	
				// force a reload from db loadObject(..., true) by evicting it from
				// hibernates session
				// cache to catch up on a different thread having commited the update of
				// this object
	
				// not needed, since getPublisher()... always loads from db???
				//p = (Publisher) DB.getInstance().loadObject(p, true);

				p.setLatestNewsDate(now);
				updatePublisher(p);
				return p;
			}
		});
		if (publisher == null) {//TODO: check if that can be null
			return; 
		}
		
		// no need to sync, since there is only one gui thread at a time from one
		// user
		if (ignoreNewsFor != null) {
			Subscriber sub = getSubscriber(ignoreNewsFor, publisher);
			if (sub != null) { // mark as read if subscribed
				updateSubscriber(sub);
			}
		}
		
		
		// channel-notify all interested listeners (e.g. the pnotificationsportletruncontroller)
		// 1. find all subscribers which can be affected
		List<Subscriber> subscribers = getValidSubscribersOf(publisher);
		
		Set<Long> subsKeys = new HashSet<Long>();
		// 2. collect all keys of the affected subscribers
		for (Iterator<Subscriber> it_subs = subscribers.iterator(); it_subs.hasNext();) {
			Subscriber su = it_subs.next();
			subsKeys.add(su.getKey());
		}
		// fire the event
		MultiUserEvent mue = EventFactory.createAffectedEvent(subsKeys);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, oresMyself);
		
	}
	
	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#registerAsListener(org.olat.core.util.event.GenericEventListener, org.olat.core.id.Identity)
	 */
	public void registerAsListener(GenericEventListener gel, Identity ident) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, ident, oresMyself);
	}
	
	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#deregisterAsListener(org.olat.core.util.event.GenericEventListener)
	 */
	public void deregisterAsListener(GenericEventListener gel) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, oresMyself);
	}

	
	/**
	 * @param identity
	 * @param subscriptionContext
	 */
	public void unsubscribe(Identity identity, SubscriptionContext subscriptionContext) {
		// no need to sync, since an identity only has one gui thread / one mouse
		Publisher p = getPublisher(subscriptionContext);
		// if no publisher yet.
		//TODO: check race condition: can p be null at all?
		if (p == null) return;
		Subscriber s = getSubscriber(identity, p);
		if (s != null) {
			deleteSubscriber(s);
		} else {
			logWarn("could not unsubscribe " + identity.getName() + " from publisher:" + p.getResName() + ","	+ p.getResId() + "," + p.getSubidentifier(), null);
		}
	}

	/**
	 * 
	 * @see org.olat.core.util.notifications.NotificationsManager#unsubscribe(org.olat.core.util.notifications.Subscriber)
	 */
	public void unsubscribe(Subscriber s) {
		Subscriber foundSub = getSubscriber(s.getKey());
		if (foundSub != null) {
			deleteSubscriber(foundSub);
		} else {
			logWarn("could not unsubscribe " + s.getIdentity().getName() + " from publisher:" + s.getPublisher().getResName() + ","	+ s.getPublisher().getResId() + "," + s.getPublisher().getSubidentifier(), null);
		}
	}

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @return true if this user is subscribed
	 */
	@SuppressWarnings("unchecked")
	public boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext) {
		DB db = DBFactory.getInstance();
		String q = "select count(sub) from org.olat.notifications.SubscriberImpl sub inner join sub.publisher as pub "
				+ " where sub.identity = :anIdentity and pub.resName = :resName and pub.resId = :resId"
				+ " and pub.subidentifier = :subidentifier group by sub";
		DBQuery query = db.createQuery(q);
		query.setEntity("anIdentity", identity);
		query.setString("resName", subscriptionContext.getResName());
		query.setLong("resId", subscriptionContext.getResId().longValue());
		query.setString("subidentifier", subscriptionContext.getSubidentifier());
		List res = query.list();
		// must return one result or null
		if (res.isEmpty()) return false;
		long cnt = ( (Long)res.get(0) ).longValue();
		if (cnt == 0) return false;
		else if (cnt == 1) return true;
		else throw new AssertException("more than once subscribed!" + identity + ", " + subscriptionContext);
	}

	/**
	 * delete publisher and subscribers
	 * 
	 * @param scontext the subscriptioncontext
	 */
	public void delete(SubscriptionContext scontext) {
		Publisher p = getPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier());
		// if none found, no one has subscribed yet and therefore no publisher has
		// been generated lazily.
		// -> nothing to do
		if (p == null) return;
		//first delete all subscribers
		List<Subscriber> subscribers = getValidSubscribersOf(p);
		for (Object susbscriberObj : subscribers) {
			deleteSubscriber((Subscriber)susbscriberObj);
		}
		// else:
		deletePublisher(p);
	}
	
	/**
	 * delete publisher and subscribers
	 * 
	 * @param publisher the publisher to delete
	 */
	public void deactivate(Publisher publisher) {
		publisher.setState(PUB_STATE_NOT_OK);
		updatePublisher(publisher);
	}

	/**
	 * @param pub
	 * @return true if the publisher is valid (that is: has not been marked as
	 *         deleted)
	 */
	public boolean isPublisherValid(Publisher pub) {
		return pub.getState() == PUB_STATE_OK;
	}

	/**
	 * no match if: a) not the same publisher b) a deleted publisher
	 * 
	 * @param p
	 * @param subscriptionContext
	 * @return true when the subscriptionContext refers to the publisher p
	 */
	public boolean matches(Publisher p, SubscriptionContext subscriptionContext) {
		// if the publisher has been deleted in the meantime, return no match
		if (!isPublisherValid(p)) return false;
		boolean ok = (p.getResName().equals(subscriptionContext.getResName()) && p.getResId().equals(subscriptionContext.getResId()) && p
				.getSubidentifier().equals(subscriptionContext.getSubidentifier()));
		return ok;
	}

	/**
	 * @param subscriber
	 * @param locale
	 * @param mimeType text/html or text/plain
	 * @return the item or null if there is currently no news for this subscription
	 */
	public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent) {
		// calculate the item based on subscriber.getLastestReadDate()
		// used for rss-feed, no longer than 1 month
		Date compareDate = getDefaultCompareDate(); 		
		return createSubscriptionItem(subscriber, locale, mimeTypeTitle, mimeTypeContent, compareDate);
	}

	/**
	 * if no compareDate is selected, cannot be calculated by user-interval, or no latestEmail is available => use this to get a Date 30d in the past.
	 * 
	 * maybe the latest user-login could also be used.
	 * @return Date
	 */
	private Date getDefaultCompareDate() {
		Calendar calNow = Calendar.getInstance();
		calNow.add(Calendar.DAY_OF_MONTH, -30);
		Date compareDate = calNow.getTime();
		return compareDate;
	}
	
	/**
	 * 
	 * @param subscriber
	 * @param locale
	 * @param mimeType
	 * @param latestEmailed needs to be given! SubscriptionInfo is collected from then until latestNews of publisher
	 * @return null if the publisher is not valid anymore (deleted), or if there are no news
	 */
	public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent, Date latestEmailed) {
		if (latestEmailed == null) throw new AssertException("compareDate may not be null, use a date from history");
		
		try {
			SubscriptionItem si = null;
			Publisher pub = subscriber.getPublisher();
			NotificationsHandler notifHandler = getNotificationsHandler(pub);
			if (isLogDebugEnabled()) logDebug("create subscription with handler: " + notifHandler.getClass().getName());
			// do not create subscription item when deleted
			if (isPublisherValid(pub)) {
				if (isLogDebugEnabled()) logDebug("NotifHandler: " + notifHandler.getClass().getName() + " compareDate: " + latestEmailed.toString() + " now: " + new Date().toString(), null);
				SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, latestEmailed);
				if (subsInfo.hasNews()) {
					si = createSubscriptionItem(subsInfo, subscriber, locale, mimeTypeTitle, mimeTypeContent);
				}
			}
			return si;
		} catch (Exception e) {
			log.error("Cannot generate a subscription item.", e);
			return null;
		}
	}
	
	@Override
	public SubscriptionItem createSubscriptionItem(SubscriptionInfo subsInfo, Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent) {
		Publisher pub = subscriber.getPublisher();
		String title = getFormatedTitle(subsInfo, subscriber, locale, mimeTypeTitle); 
		
		String itemLink = null;
		if(subsInfo.getCustomUrl() != null) {
			itemLink = subsInfo.getCustomUrl();
		}
		if(itemLink == null && pub.getBusinessPath() != null) {
			itemLink = BusinessControlFactory.getInstance().getURLFromBusinessPathString(pub.getBusinessPath());
		}
		
		String description = subsInfo.getSpecificInfo(mimeTypeContent, locale);
		return new SubscriptionItem(title, itemLink, description);
	}
	
	/**
	 * format the type-title and title-details
	 * @param subscriber
	 * @param locale
	 * @param mimeType
	 * @return
	 */
	private String getFormatedTitle(SubscriptionInfo subsInfo, Subscriber subscriber, Locale locale, String mimeType){
		Publisher pub = subscriber.getPublisher();
		String innerType = pub.getType();
		String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
		StringBuilder titleSb = new StringBuilder();
		titleSb.append(typeName);
		
		String title = subsInfo.getTitle(mimeType);
		if (StringHelper.containsNonWhitespace(title)) {
			titleSb.append(": ").append(title);
		} else {
			NotificationsHandler notifHandler = getNotificationsHandler(pub);
			String titleInfo = notifHandler.createTitleInfo(subscriber, locale);
			if (StringHelper.containsNonWhitespace(titleInfo)) {
				titleSb.append(": ").append(titleInfo);
			}
		}
		
		return titleSb.toString(); 
	}
	
	/**
	 * 
	 * @see org.olat.core.util.notifications.NotificationsManager#getNoSubscriptionInfo()
	 */
	public SubscriptionInfo getNoSubscriptionInfo() {
		return NOSUBSINFO;
	}

	/**
	 * Delete all subscribers for certain identity.
	 * @param identity
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<Subscriber> subscribers = getSubscribers(identity);
		for (Iterator<Subscriber> iter = subscribers.iterator(); iter.hasNext();) {
			deleteSubscriber( iter.next() );
		}
		logDebug("All notification-subscribers deleted for identity=" + identity, null);
	}

	/**
	 * Spring setter method
	 * 
	 * @param notificationIntervals
	 */
	public void setNotificationIntervals(Map<String, Boolean> intervals) {
		notificationIntervals = new ArrayList<String>();
		for(String key : intervals.keySet()) {
			if (intervals.get(key)) {
				if(key.length() <= 16) {
					notificationIntervals.add(key);
				} else {
					log.error("Interval notification cannot be more than 16 characters wide: " + key);
				}
			}
		}
	}

	/**
	 * Spring setter method
	 * 
	 * @param defaultNotificationInterval
	 */
	public void setDefaultNotificationInterval(String defaultNotificationInterval) {
		this.defaultNotificationInterval = defaultNotificationInterval;
	}

	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getDefaultNotificationInterval()
	 */
	public String getDefaultNotificationInterval() {
		return defaultNotificationInterval;
	}

	/**
	 * @see org.olat.core.util.notifications.NotificationsManager#getNotificationIntervals()
	 */
	public List<String> getEnabledNotificationIntervals() {
		return notificationIntervals;
	}
}

