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

package org.olat.core.commons.services.notifications.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.SubscriptionItem;
import org.olat.core.commons.services.notifications.model.NoSubscriptionInfo;
import org.olat.core.commons.services.notifications.model.PublisherImpl;
import org.olat.core.commons.services.notifications.model.SubscriberImpl;
import org.olat.core.commons.services.notifications.ui.NotificationSubscriptionController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.event.EventFactory;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.resource.OresHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;

/**
 * Description: <br>
 * see org.olat.core.commons.services.notifications.NotificationsManager
 * 
 * Initial Date: 21.10.2004 <br>
 * @author Felix Jost
 */
public class NotificationsManagerImpl extends NotificationsManager implements UserDataDeletable, UserDataExportable {
	private static final OLog log = Tracing.createLoggerFor(NotificationsManagerImpl.class);

	private static final int PUB_STATE_OK = 0;
	private static final int PUB_STATE_NOT_OK = 1;
	private static final int BATCH_SIZE = 500;
	private static final String LATEST_EMAIL_USER_PROP = "noti_latest_email";
	private static final SubscriptionInfo NOSUBSINFO = new NoSubscriptionInfo();

	private final OLATResourceable oresMyself = OresHelper.lookupType(NotificationsManagerImpl.class);

	private Map<String, NotificationsHandler> notificationHandlers;
	
	private List<String> notificationIntervals;
	private String defaultNotificationInterval;
	private static final Map<String, Integer> INTERVAL_DEF_MAP = buildIntervalMap();
	private Object lockObject = new Object();
	
	private DB dbInstance;
	private BaseSecurity securityManager;
	private PropertyManager propertyManager;
	
	/**
	 * [used by spring]
	 * @param userDeletionManager
	 */
	private NotificationsManagerImpl() {
		// private since singleton
		INSTANCE = this;
	}

	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/**
	 * [user by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}
	
	/**
	 * [used by Spring]
	 * @param propertyManager
	 */
	public void setPropertyManager(PropertyManager propertyManager) {
		this.propertyManager = propertyManager;
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
		pi.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(pi);
		return pi;
	}

	/**
	 * @param persistedPublisher
	 * @param listener
	 * @param subscriptionContext the context of the object we subscribe to
	 * @return a subscriber with a db key
	 */
	protected Subscriber doCreateAndPersistSubscriber(Publisher persistedPublisher, Identity listener) {
		SubscriberImpl si = new SubscriberImpl(persistedPublisher, listener);
		si.setCreationDate(new Date());
		si.setLastModified(new Date());
		si.setLatestEmailed(new Date());
		dbInstance.getCurrentEntityManager().persist(si);
		return si;
	}

	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person)
	 * 
	 * @param identity
	 * @return List of Subscriber Objects which belong to the identity
	 */
	@Override
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
	public List<Subscriber> getSubscribers(IdentityRef identity, List<String> types) {
		if(identity == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select sub from notisub as sub ")
		  .append("inner join fetch sub.publisher as publisher ")
		  .append("where sub.identity.key = :identityKey");
		if(types != null && !types.isEmpty()) {
			sb.append(" and publisher.type in (:types)");
		}
		TypedQuery<Subscriber> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Subscriber.class)
				.setParameter("identityKey", identity.getKey());
		if(types != null && !types.isEmpty()) {
			query.setParameter("types", types);
		}
		return query.getResultList();
	}

	/**
	 * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers
	 * belonging to this person) restricted to the specified Olat resourceable id
	 * 
	 * @param identity
	 * @param resId
	 * @return List of Subscriber Objects which belong to the identity
	 */
	@Override
	public List<Subscriber> getSubscribers(IdentityRef identity, long resId) {
		if(identity == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select sub from notisub as sub")
		  .append(" inner join fetch sub.publisher as publisher")
		  .append(" where sub.identity.key = :identityKey and publisher.resId = :resId");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Subscriber.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("resId", resId)
			.getResultList();
	}

	/**
	 * @param identity
	 * @return a list of all subscribers which belong to the identity and which
	 *         publishers are valid
	 */
	@Override
	public List<Subscriber> getValidSubscribers(Identity identity) {
		if(identity == null) return Collections.emptyList();
		
		StringBuilder q = new StringBuilder(256);
		q.append("select sub from notisub sub ")
		 .append(" inner join fetch sub.publisher as pub ")
		 .append(" where sub.identity.key=:anIdentityKey and pub.state=").append(PUB_STATE_OK);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("anIdentityKey", identity.getKey())
				.getResultList();
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getValidSubscribersOf(org.olat.core.commons.services.notifications.Publisher)
	 */
	@Override
	public List<Subscriber> getValidSubscribersOf(Publisher publisher) {
		StringBuilder q = new StringBuilder();
		q.append("select sub from notisub sub ")
		 .append(" inner join fetch sub.identity")
		 .append(" where sub.publisher = :publisher and sub.publisher.state=").append(PUB_STATE_OK);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("publisher", publisher)
				.getResultList();
	}
	
	private List<Subscriber> getValidSubscribersOf(String publisherType, String data) {
		StringBuilder q = new StringBuilder();
		q.append("select sub from notisub sub ")
		 .append(" inner join fetch sub.identity as ident")
		 .append(" inner join fetch sub.publisher as pub")
		 .append(" where pub.publisherType=:publisherType and pub.data=:data and sub.publisher.state=").append(PUB_STATE_OK);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("publisherType", publisherType)
				.setParameter("data", data)
				.getResultList();
	}
	
	@Override
	public List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sub from notisub sub")
			.append(" inner join fetch sub.publisher as pub")
			.append(" where sub.identity=:identity and pub.type=:type and pub.state=:aState");
		
		List<Subscriber> subscribers = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Subscriber.class)
				.setParameter("aState", PUB_STATE_OK)
				.setParameter("type", publisherType)
				.setParameter("identity", identity)
				.getResultList();
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
	
	@Override
	public void notifyAllSubscribersByEmail() {
		logAudit("starting notification cronjob to send email", null);
		WorkThreadInformations.setLongRunningTask("sendNotifications");
		
		int counter = 0;
		int closeConnection = 0;
		List<Identity> identities;
		do {
			identities = securityManager.loadVisibleIdentities(counter, BATCH_SIZE);
			for(Identity identity:identities) {
				if(identity.getName().startsWith("guest_")) {
					Roles roles = securityManager.getRoles(identity);
					if(roles.isGuestOnly()) {
						continue;
					}
				}
				closeConnection++;
				processSubscribersByEmail(identity);
				if(closeConnection % 20 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
			counter += identities.size();
			dbInstance.commitAndCloseSession();
		} while(identities.size() == BATCH_SIZE);
		
		// done, purge last entry
		WorkThreadInformations.unsetLongRunningTask("sendNotifications");
		logAudit("end notification cronjob to send email", null);
	}
	
	private void processSubscribersByEmail(Identity ident) {
		if(ident.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) >= 0) {
			return;//send only to active user
		}
		
		String userInterval = getUserIntervalOrDefault(ident);
		if("never".equals(userInterval)) {
			return;
		}

		long start = System.currentTimeMillis();
		Date compareDate = getCompareDateFromInterval(userInterval);
		Property p = propertyManager.findProperty(ident, null, null, null, LATEST_EMAIL_USER_PROP);
		if(p != null) {
		  	Date latestEmail = new Date(p.getLongValue());
		  	if(latestEmail.after(compareDate)) {
		  		return;//nothing to do
		  	}
		}

		Date defaultCompareDate = getDefaultCompareDate();
		List<Subscriber> subscribers = getSubscribers(ident);
		if(subscribers.isEmpty()) {
			return;
		}
		
		String langPrefs = null;
		if(ident.getUser() != null && ident.getUser().getPreferences() != null) {
			langPrefs = ident.getUser().getPreferences().getLanguage();
		}
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(langPrefs);
		
		boolean veto = false;
		Subscriber latestSub = null;
		List<SubscriptionItem> items = new ArrayList<>();
		List<Subscriber> subsToUpdate = new ArrayList<>();
		for(Subscriber sub:subscribers) {
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
				subsitem = createSubscriptionItem(sub, locale, SubscriptionInfo.MIME_HTML, SubscriptionInfo.MIME_HTML, latestEmail);
			}	else if(latestEmail != null && latestEmail.after(compareDate)) {
				//already send an email within the user's settings interval
				//veto = true;
			}
			if (subsitem != null) {
				items.add(subsitem);
				subsToUpdate.add(sub);
			}
			latestSub = sub;
		}
		
		Translator translator = Util.createPackageTranslator(NotificationSubscriptionController.class, locale);
		notifySubscribersByEmail(latestSub, items, subsToUpdate, translator, start, veto);
	}
	
	private void notifySubscribersByEmail(Subscriber latestSub, List<SubscriptionItem> items, List<Subscriber> subsToUpdate, Translator translator, long start, boolean veto) {
		if(veto) {
			if(latestSub != null) {
				logAudit(latestSub.getIdentity().getKey() + " already received notification email within prefs interval");
			}
		} else if (items.size() > 0) {
			Identity curIdent = latestSub.getIdentity();
			boolean sentOk = sendMailToUserAndUpdateSubscriber(curIdent, items, translator, subsToUpdate);
			if (sentOk) {
				Property p = propertyManager.findProperty(curIdent, null, null, null, LATEST_EMAIL_USER_PROP);
				if(p == null) {
					p = propertyManager.createUserPropertyInstance(curIdent, null, LATEST_EMAIL_USER_PROP, null, null, null, null);
					p.setLongValue(new Date().getTime());
					propertyManager.saveProperty(p);
				} else {
					p.setLongValue(new Date().getTime());
					propertyManager.updateProperty(p);
				}
			  
				StringBuilder mailLog = new StringBuilder();
				mailLog.append("Notifications mailed for ").append(curIdent.getKey()).append(' ').append(items.size()).append(' ').append((System.currentTimeMillis() - start)).append("ms");
				logAudit(mailLog.toString());
			} else {
				logAudit("Error sending notification email to : " + curIdent.getKey());
			}
		}
		//collecting the SubscriptionItem can potentially make a lot of DB calls
		dbInstance.intermediateCommit();
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getCompareDateFromInterval(java.lang.String)
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
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getUserIntervalOrDefault(org.olat.core.id.Identity)
	 */
	@Override
	public String getUserIntervalOrDefault(Identity ident){
		if(ident == null || ident.getUser() == null || ident.getUser().getPreferences() == null) {
			logWarn("Identity " + (ident == null ? "NULL" : ident.getKey()) + " has no preferences invalid", null);
			return getDefaultNotificationInterval();
		}
		
		String userInterval = ident.getUser().getPreferences().getNotificationInterval();
		if (!StringHelper.containsNonWhitespace(userInterval)) userInterval = getDefaultNotificationInterval();
		List<String> avIntvls = getEnabledNotificationIntervals();
		if (!avIntvls.contains(userInterval)) {
			logWarn("Identity " + ident.getKey() + " has an invalid notification-interval (not found in config): " + userInterval, null);
			userInterval = getDefaultNotificationInterval();
		}
		return userInterval;
	}
	
	@Override
	public boolean sendMailToUserAndUpdateSubscriber(Identity curIdent, List<SubscriptionItem> items, Translator translator, List<Subscriber> subscribersToUpdate) {
		boolean sentOk = sendEmail(curIdent, translator, items);
		// save latest email sent date for the subscription just emailed
		// do this only if the mail was successfully sent
		if (sentOk) {
			updateSubscriberLatestEmail(subscribersToUpdate);
		}
		return sentOk;
	}
	
	protected void updateSubscriberLatestEmail(List<Subscriber> subscribersToUpdate) {
		if(subscribersToUpdate == null || subscribersToUpdate.isEmpty()) {
			return;//nothing to do
		}
		
		StringBuilder q = new StringBuilder();	
		q.append("select sub from notisub sub ")
		 .append(" inner join fetch sub.publisher where sub.key in (:aKey)");
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<Long> keys = PersistenceHelper.toKeys(subscribersToUpdate);
		List<Subscriber> subscribers = em.createQuery(q.toString(), Subscriber.class)
				.setParameter("aKey", keys)
				.getResultList();
		
		for (Subscriber subscriber :subscribers) {
			subscriber.setLastModified(new Date());
			subscriber.setLatestEmailed(new Date());
			em.merge(subscriber);
		}
	}
	
	private boolean sendEmail(Identity to, Translator translator, List<SubscriptionItem> subItems) {
		String title = translator.translate("rss.title", new String[] { NotificationHelper.getFormatedName(to) });
		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<style>");
		htmlText.append(".o_m_sub h4 {margin: 0 0 10px 0;}");
		htmlText.append(".o_m_sub ul {padding: 0 0 5px 20px; margin: 0;}");
		htmlText.append(".o_m_sub ul li {padding: 0; margin: 1px 0;}");
		htmlText.append(".o_m_go {padding: 5px 0 0 0}");
		htmlText.append(".o_date {font-size: 90%; color: #888}");		
		htmlText.append(".o_m_footer {background: #FAFAFA; border: 1px solid #eee; border-radius: 5px; padding: 0 0.5em 0.5em 0.5em; margin: 1em 0 1em 0;' class='o_m_h'}");
		htmlText.append("</style>");
		
		for (Iterator<SubscriptionItem> it_subs = subItems.iterator(); it_subs.hasNext();) {
			SubscriptionItem subitem = it_subs.next();
			// o_m_wrap class for overriding styles in master mail template		
			htmlText.append("<div class='o_m_wrap'>");	 
			// add background here for gmail as they ignore classes. 
			htmlText.append("<div class='o_m_sub' style='background: #FAFAFA; padding: 5px 5px; margin: 10px 0;'>");			
			// 1: title
			htmlText.append(subitem.getTitle());				
			htmlText.append("\n");
			// 2: content
			String desc = subitem.getDescription();
			if(StringHelper.containsNonWhitespace(desc)) {
				htmlText.append(desc);
			}
			// 3: goto-link
			String link = subitem.getLink();
			if(StringHelper.containsNonWhitespace(link)) {
				htmlText.append("<div class='o_m_go'><a href=\"").append(link).append("\">");
				SubscriptionInfo subscriptionInfo = subitem.getSubsInfo();
				if (subscriptionInfo != null) {
					String innerType = subscriptionInfo.getType();
					String typeName = NewControllerFactory.translateResourceableTypeName(innerType, translator.getLocale());
					String open = translator.translate("resource.open", new String[] { typeName });
					htmlText.append(open);
					htmlText.append(" &raquo;</a></div>");
				}
			}

			htmlText.append("\n");
			htmlText.append("</div></div>");
		}
		String basePath =  Settings.getServerContextPathURI() + "/auth/HomeSite/" + to.getKey() + "/";
		htmlText.append("<div class='o_m_footer'>");
		htmlText.append(translator.translate("footer.notifications", new String[] {basePath + "mysettings/0", basePath += "notifications/0", basePath + "/tab/1"}));
		htmlText.append("</div>");

		MailerResult result = null;
		try {
			MailBundle bundle = new MailBundle();
			bundle.setToId(to);
			bundle.setContent(title, htmlText.toString());
			result = CoreSpringFactory.getImpl(MailManager.class).sendExternMessage(bundle, null, true);
		} catch (Exception e) {
			// FXOLAT-294 :: sending the mail will throw nullpointer exception if To-Identity has no
			// valid email-address!, catch it...
		} 
		if (result == null || result.getReturnCode() > 0) {
			if(result!=null)
				log.warn("Could not send email to identity " + to.getKey() + ". (returncode=" + result.getReturnCode() + ", to=" + to + ")");
			else
				log.warn("Could not send email to identity " + to.getKey() + ". (returncode = null) , to=" + to + ")");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param key
	 * @return the subscriber with this key or null if not found
	 */
	@Override
	public Subscriber getSubscriber(Long key) {
		StringBuilder q = new StringBuilder();
		q.append("select sub from notisub as sub")
		 .append(" inner join fetch sub.publisher ")
		 .append(" where sub.key=:aKey");

		List<Subscriber> res = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("aKey", key.longValue())
				.getResultList();
		if (res.isEmpty()) return null;
		if (res.size() > 1) throw new AssertException("more than one subscriber for key " + key);
		return res.get(0);
	}

	/**
	 * @param scontext
	 * @param pdata
	 * @return the publisher
	 */
	public Publisher getOrCreatePublisher(final SubscriptionContext scontext, final PublisherData pdata) {
		return findOrCreatePublisher(scontext, pdata);
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
		Publisher pub = getPublisher(scontext);
		if(pub != null) {
			return pub;
		}
		
		Publisher publisher = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Publisher>(){
			public Publisher execute() {
				Publisher p = getPublisher(scontext);
				// if not found, create it
				if (p == null) {
					p = createAndPersistPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier(), pdata.getType(), pdata
							.getData(), pdata.getBusinessPath());
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
	@Override
	public Publisher getPublisher(SubscriptionContext subsContext) {
		StringBuilder q = new StringBuilder();
		q.append("select pub from notipublisher pub ")
		 .append(" where pub.resName=:resName and pub.resId = :resId");
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			q.append(" and pub.subidentifier=:subidentifier");
		} else {
			q.append(" and (pub.subidentifier='' or pub.subidentifier is null)");
		}
		
		TypedQuery<Publisher> query = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Publisher.class)
				.setParameter("resName", subsContext.getResName())
				.setParameter("resId", subsContext.getResId());

		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			query.setParameter("subidentifier", subsContext.getSubidentifier());
		}
		List<Publisher> res = query.getResultList();
		if (res.isEmpty()) return null;
		if (res.size() != 1) throw new AssertException("only one subscriber per person and publisher!!");
		return res.get(0);
	}

	@Override
	public void updatePublisherData(SubscriptionContext subsContext, PublisherData data){
		Publisher publisher= getPublisherForUpdate(subsContext);
		if(publisher != null){
			publisher.setData(data.getData());
			dbInstance.getCurrentEntityManager().merge(publisher);
			dbInstance.commit();
		}
	}

	private Publisher getPublisherForUpdate(SubscriptionContext subsContext) {
		Publisher pub = getPublisher(subsContext);
		return getPublisherForUpdate(pub);
	}
	
	private Publisher getPublisherForUpdate(Publisher publisher) {
		if(publisher != null && publisher.getKey() != null) {
			//prevent optimistic lock issue
			dbInstance.getCurrentEntityManager().detach(publisher);
			publisher = dbInstance.getCurrentEntityManager()
					.find(PublisherImpl.class, publisher.getKey(), LockModeType.PESSIMISTIC_WRITE);
		}
		return publisher;
	}
	
	@Override
	public List<Publisher> getAllPublisher() {
		String q = "select pub from notipublisher pub";
		return dbInstance.getCurrentEntityManager().createQuery(q, Publisher.class)
				.getResultList();
	}

	/**
	 * @param resName
	 * @param resId
	 * @return a list of publishers belonging to the resource
	 */
	private List<Publisher> getPublishers(String resName, Long resId) {
		String q = "select pub from notipublisher pub where pub.resName=:resName and pub.resId= :resId";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Publisher.class)
				.setParameter("resName", resName)
				.setParameter("resId", resId.longValue())
				.getResultList();
	}
	
	private List<Publisher> getPublishers(String publisherType, String data) {
		String q = "select pub from notipublisher pub where pub.type=:publisherType and pub.data=:data";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Publisher.class)
				.setParameter("publisherType", publisherType)
				.setParameter("data", data)
				.getResultList();
	}

	/**
	 * deletes all publishers of the given olatresourceable. e.g. ores =
	 * businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of
	 * Forum(toolforum)
	 * 
	 * @param ores
	 */
	@Override
	public int deletePublishersOf(OLATResourceable ores) {
		String type = ores.getResourceableTypeName();
		Long id = ores.getResourceableId();
		if (type == null || id == null) throw new AssertException("type/id cannot be null! type:" + type + " / id:" + id);
		List<Publisher> pubs = getPublishers(type, id);
		if(pubs.isEmpty()) return 0;

		String q1 = "delete from notisub sub where sub.publisher in (:publishers)";
		Query query1 = dbInstance.getCurrentEntityManager().createQuery(q1);
		query1.setParameter("publishers", pubs);
		int rows = query1.executeUpdate();
		
		String q2 = "delete from notipublisher pub where pub in (:publishers)";
		Query query2 = dbInstance.getCurrentEntityManager().createQuery(q2);
		query2.setParameter("publishers", pubs);
		rows += query2.executeUpdate();
		return rows;
	}

	/**
	 * @param identity
	 * @param publisher
	 * @return a Subscriber object belonging to the identity and listening to the
	 *         given publisher
	 */
	@Override
	public Subscriber getSubscriber(Identity identity, Publisher publisher) {
		List<Subscriber> res = dbInstance.getCurrentEntityManager()
				.createNamedQuery("subscribersByPublisherAndIdentity", Subscriber.class)
				.setParameter("publisherKey", publisher.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();

		if (res.size() == 0) return null;
		if (res.size() != 1) throw new AssertException("only one subscriber per person and publisher!!");
		Subscriber s = res.get(0);
		return s;
	}
	
	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getSubscriber(org.olat.core.commons.services.notifications.Publisher)
	 */
	@Override
	public List<Subscriber> getSubscribers(Publisher publisher) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("subscribersByPublisher", Subscriber.class)
				.setParameter("publisher", publisher)
				.getResultList();
	}
	
	@Override
	public List<Identity> getSubscriberIdentities(Publisher publisher) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("identitySubscribersByPublisher", Identity.class)
				.setParameter("publisher", publisher)
				.getResultList();
	}

	/**
	 * @return the handler for the type
	 */
	@Override
	public NotificationsHandler getNotificationsHandler(Publisher publisher) {
		String type = publisher.getType();
		if (notificationHandlers == null) {
			synchronized(lockObject) {
				if (notificationHandlers == null) { // check again in synchronized-block, only one may create list
					notificationHandlers = new HashMap<>();
					Map<String, NotificationsHandler> notificationsHandlerMap = CoreSpringFactory.getBeansOfType(NotificationsHandler.class);
					Collection<NotificationsHandler> notificationsHandlerValues = notificationsHandlerMap.values();
					for (NotificationsHandler notificationsHandler : notificationsHandlerValues) {
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
		dbInstance.deleteObject(subscriber);
	}

	@Override
	public boolean deleteSubscriber(Long subscriberKey) {
		String sb = "delete from notisub sub where sub.key=:subscriberKey";
		int rows = dbInstance.getCurrentEntityManager()
				.createQuery(sb)
				.setParameter("subscriberKey", subscriberKey)
				.executeUpdate();
		return rows > 0;
	}

	/**
	 * sets the latest visited date of the subscription to 'now' .assumes the
	 * identity is already subscribed to the publisher
	 * 
	 * @param identity
	 * @param subsContext
	 */
	@Override
	public void markSubscriberRead(Identity identity, SubscriptionContext subsContext) {
		Publisher p = getPublisher(subsContext);
		if (p == null) throw new AssertException("cannot markRead for identity " + identity.getKey()
				+ ", since the publisher for the given subscriptionContext does not exist: subscontext = " + subsContext);

		markSubscriberRead(identity, p);
	}
	
	private Subscriber markSubscriberRead(Identity identity, Publisher p) {
		Subscriber sub = getSubscriber(identity, p);
		if(sub != null) {
			sub.setLastModified(new Date());
			sub = dbInstance.getCurrentEntityManager().merge(sub);
		}
		return sub;
	}

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @param publisherData
	 */
	@Override
	public void subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData) {
		//need to sync as opt-in is sometimes implemented
		Publisher toUpdate = getPublisherForUpdate(subscriptionContext);
		if(toUpdate == null) {
			//create the publisher
			findOrCreatePublisher(subscriptionContext, publisherData);
			//lock the publisher
			toUpdate = getPublisherForUpdate(subscriptionContext);
		}

		Subscriber s = getSubscriber(identity, toUpdate);
		if (s == null) {
			// no subscriber -> create.
			// s.latestReadDate >= p.latestNewsDate == no news for subscriber when no
			// news after subscription time
			doCreateAndPersistSubscriber(toUpdate, identity);
		}
		dbInstance.commit();
	}
	
	@Override
	public void subscribe(List<Identity> identities, SubscriptionContext subscriptionContext,
			PublisherData publisherData) {
		if(identities == null || identities.isEmpty()) return;
		
		Publisher toUpdate = getPublisherForUpdate(subscriptionContext);
		if(toUpdate == null) {
			//create the publisher
			findOrCreatePublisher(subscriptionContext, publisherData);
			//lock the publisher
			toUpdate = getPublisherForUpdate(subscriptionContext);
		}

		for(Identity identity:identities) {
			Subscriber s = getSubscriber(identity, toUpdate);
			if (s == null) {
				// no subscriber -> create.
				// s.latestReadDate >= p.latestNewsDate == no news for subscriber when no
				// news after subscription time
				doCreateAndPersistSubscriber(toUpdate, identity);
			}
		}
		dbInstance.commit();	
	}

	/**
	 * call this method to indicate that there is news for the given
	 * subscriptionContext
	 * 
	 * @param subscriptionContext
	 * @param ignoreNewsFor
	 */
	@Override
	public void markPublisherNews(final SubscriptionContext subscriptionContext, Identity ignoreNewsFor, boolean sendEvents) {
		// to make sure: ignore if no subscriptionContext
		if (subscriptionContext == null) return;

		Publisher toUpdate = getPublisherForUpdate(subscriptionContext);
		if(toUpdate == null) {
			return;
		}
		toUpdate.setLatestNewsDate(new Date());
		Publisher publisher = dbInstance.getCurrentEntityManager().merge(toUpdate);
		dbInstance.commit();//commit the select for update

		// no need to sync, since there is only one gui thread at a time from one
		// user
		if (ignoreNewsFor != null) {
			markSubscriberRead(ignoreNewsFor, publisher);
		}
		
		if(sendEvents) {
			//commit all things on the database
			dbInstance.commit();
			
			// channel-notify all interested listeners (e.g. the pnotificationsportletruncontroller)
			// 1. find all subscribers which can be affected
			List<Subscriber> subscribers = getValidSubscribersOf(publisher);
			
			Set<Long> subsKeys = new HashSet<Long>();
			// 2. collect all keys of the affected subscribers
			for (Subscriber subscriber:subscribers) {
				subsKeys.add(subscriber.getKey());
			}
			// fire the event
			MultiUserEvent mue = EventFactory.createAffectedEvent(subsKeys);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, oresMyself);
		}
	}
	
	
	
	@Override
	public void markPublisherNews(String publisherType, String data, Identity ignoreNewsFor, boolean sendEvents) {
		// to make sure: ignore if no subscriptionContext
		if (!StringHelper.containsNonWhitespace(publisherType) ||  !StringHelper.containsNonWhitespace(data)) return;

		List<Publisher> publisherToUpdates = getPublishers(publisherType, data);
		if(publisherToUpdates == null || publisherToUpdates.isEmpty()) {
			return;
		}
		
		List<Publisher> updatedPublishers = new ArrayList<>(publisherToUpdates.size());
		for(Publisher toUpdate:publisherToUpdates) {
			toUpdate = getPublisherForUpdate(toUpdate);
			toUpdate.setLatestNewsDate(new Date());
			Publisher publisher = dbInstance.getCurrentEntityManager().merge(toUpdate);
			dbInstance.commit();//commit the select for update
			updatedPublishers.add(publisher);
		}

		// no need to sync, since there is only one gui thread at a time from one
		// user
		if (ignoreNewsFor != null) {
			for(Publisher publisher: updatedPublishers) {
				markSubscriberRead(ignoreNewsFor, publisher);
			}
		}
		
		if(sendEvents) {
			//commit all things on the database
			dbInstance.commit();
			
			// channel-notify all interested listeners (e.g. the pnotificationsportletruncontroller)
			// 1. find all subscribers which can be affected
			List<Subscriber> subscribers = getValidSubscribersOf(publisherType, data);
			
			Set<Long> subsKeys = new HashSet<Long>();
			// 2. collect all keys of the affected subscribers
			for (Subscriber subscriber:subscribers) {
				subsKeys.add(subscriber.getKey());
			}
			// fire the event
			MultiUserEvent mue = EventFactory.createAffectedEvent(subsKeys);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, oresMyself);
		}
		
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#registerAsListener(org.olat.core.util.event.GenericEventListener, org.olat.core.id.Identity)
	 */
	@Override
	public void registerAsListener(GenericEventListener gel, Identity ident) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, ident, oresMyself);
	}
	
	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#deregisterAsListener(org.olat.core.util.event.GenericEventListener)
	 */
	@Override
	public void deregisterAsListener(GenericEventListener gel) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, oresMyself);
	}
	
	/**
	 * @param identity
	 * @param subscriptionContext
	 */
	@Override
	public void unsubscribe(Identity identity, SubscriptionContext subscriptionContext) {
		Publisher p = getPublisherForUpdate(subscriptionContext);
		if (p != null) {
			Subscriber s = getSubscriber(identity, p);
			if (s != null) {
				deleteSubscriber(s);
			} else {
				logWarn("could not unsubscribe " + identity.getKey() + " from publisher:" + p.getResName() + ","	+ p.getResId() + "," + p.getSubidentifier(), null);
			}
		}
		dbInstance.commit();
	}
	
	@Override
	public void unsubscribe(List<Identity> identities, SubscriptionContext subscriptionContext) {
		if(identities == null || identities.isEmpty()) return;

		Publisher p = getPublisherForUpdate(subscriptionContext);
		if (p != null) {
			for(Identity identity:identities) {
				Subscriber s = getSubscriber(identity, p);
				if (s != null) {
					deleteSubscriber(s);
				} else {
					logWarn("could not unsubscribe " + identity.getKey() + " from publisher:" + p.getResName() + ","	+ p.getResId() + "," + p.getSubidentifier(), null);
				}
			}
		}
		dbInstance.commit();
	}

	/**
	 * 
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#unsubscribe(org.olat.core.commons.services.notifications.Subscriber)
	 */
	@Override
	public void unsubscribe(Subscriber s) {
		Subscriber foundSub = getSubscriber(s.getKey());
		if (foundSub != null) {
			deleteSubscriber(foundSub);
		} else {
			logWarn("could not unsubscribe " + s.getIdentity().getKey() + " from publisher:" + s.getPublisher().getResName() + ","	+ s.getPublisher().getResId() + "," + s.getPublisher().getSubidentifier(), null);
		}
	}

	@Override
	public void unsubscribeAllForIdentityAndResId(IdentityRef identity, Long resId) {
		List<Subscriber> subscribers = getSubscribers(identity, resId.longValue());
		for (Subscriber sub:subscribers) {
			unsubscribe (sub);
		}
	}

	/**
	 * @param identity
	 * @param subscriptionContext
	 * @return true if this user is subscribed
	 */
	@Override
	public boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext) {
		StringBuilder q = new StringBuilder();		
		q.append("select count(sub) from notisub as sub ")
		 .append(" inner join sub.publisher as pub ")
		 .append(" where sub.identity.key=:anIdentityKey and pub.resName=:resName and pub.resId=:resId")
		 .append(" and pub.subidentifier=:subidentifier");
		
		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Number.class)
				.setParameter("anIdentityKey", identity.getKey())
				.setParameter("resName", subscriptionContext.getResName())
				.setParameter("resId", subscriptionContext.getResId().longValue())
				.setParameter("subidentifier", subscriptionContext.getSubidentifier())
				.getSingleResult();

		long cnt = count.longValue();
		if (cnt == 0) return false;
		else return true;
	}

	/**
	 * delete publisher and subscribers
	 * 
	 * @param scontext the subscriptioncontext
	 */
	@Override
	public void delete(SubscriptionContext scontext) {
		Publisher p = getPublisher(scontext);
		// if none found, no one has subscribed yet and therefore no publisher has
		// been generated lazily.
		// -> nothing to do
		if (p == null) return;
		//first delete all subscribers
		List<Subscriber> subscribers = getValidSubscribersOf(p);
		for (Subscriber subscriber : subscribers) {
			deleteSubscriber(subscriber);
		}
		// else:
		dbInstance.deleteObject(p);
	}

	/**
	 * delete publisher and subscribers
	 * 
	 * @param publisher the publisher to delete
	 */
	@Override
	public void deactivate(Publisher publisher) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		
		PublisherImpl toDeactivate = em.find(PublisherImpl.class, publisher.getKey(), LockModeType.PESSIMISTIC_WRITE);
		toDeactivate.setState(PUB_STATE_NOT_OK);
		em.merge(toDeactivate);
		dbInstance.commit();
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
	@Override
	public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent, Date latestEmailed) {
		if (latestEmailed == null) throw new AssertException("compareDate may not be null, use a date from history");
		
		try {
			boolean debug = isLogDebugEnabled();
			
			SubscriptionItem si = null;
			Publisher pub = subscriber.getPublisher();
			NotificationsHandler notifHandler = getNotificationsHandler(pub);
			if(debug) logDebug("create subscription with handler: " + notifHandler.getClass().getName());
			// do not create subscription item when deleted
			if (isPublisherValid(pub) && notifHandler != null) {
				if(debug) logDebug("NotifHandler: " + notifHandler.getClass().getName() + " compareDate: " + latestEmailed.toString() + " now: " + new Date().toString(), null);
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
		SubscriptionItem subscriptionItem =  new SubscriptionItem(title, itemLink, description);
		subscriptionItem.setSubsInfo(subsInfo);
		return subscriptionItem;
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
		StringBuilder titleSb = new StringBuilder();
		
		String title = subsInfo.getTitle(mimeType);
		if (StringHelper.containsNonWhitespace(title)) {
			titleSb.append(title);
		} else {
			NotificationsHandler notifHandler = getNotificationsHandler(pub);
			String titleInfo = notifHandler.createTitleInfo(subscriber, locale);
			if (StringHelper.containsNonWhitespace(titleInfo)) {
				titleSb.append(titleInfo);
			}
		}
		
		return titleSb.toString(); 
	}

	@Override
	public SubscriptionInfo getNoSubscriptionInfo() {
		return NOSUBSINFO;
	}

	/**
	 * Delete all subscribers for certain identity.
	 * @param identity
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<Subscriber> subscribers = getSubscribers(identity);
		for (Subscriber subscriber:subscribers) {
			deleteSubscriber(subscriber);
		}
		logDebug("All notification-subscribers deleted for identity=" + identity, null);
	}

	@Override
	public String getExporterID() {
		return "notifications";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File subscriptionsArchive = new File(archiveDirectory, "Subscriptions.xlsx");
		try(OutputStream out = new FileOutputStream(subscriptionsArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row row = sheet.newRow();
			row.addCell(0, "Type");
			row.addCell(1, "Type");
			row.addCell(2, "Creation date");
			row.addCell(3, "Title");
			
			List<Subscriber> subscribers = getSubscribers(identity);
			for(Subscriber subscriber:subscribers) {
				exportSubscriberData(subscriber, sheet, workbook, locale);
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}

		manifest.appendFile(subscriptionsArchive.getName());
	}

	private void exportSubscriberData(Subscriber subscriber, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook, Locale locale) {
		Row row = sheet.newRow();
		Publisher pub = subscriber.getPublisher();
		
		String innerType = NewControllerFactory.translateResourceableTypeName(pub.getType(), locale);
		row.addCell(0, innerType);
		String containerType = NewControllerFactory.translateResourceableTypeName(pub.getResName(), locale);
		row.addCell(1, containerType);
		row.addCell(2, subscriber.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		
		NotificationsHandler handler = getNotificationsHandler(pub);
		if(handler != null) {
			String title = handler.createTitleInfo(subscriber, locale);
			row.addCell(3, title);
		}
	}

	/**
	 * Spring setter method
	 * 
	 * @param notificationIntervals
	 */
	public void setNotificationIntervals(Map<String, Boolean> intervals) {
		notificationIntervals = new ArrayList<>();
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
		if (defaultNotificationInterval != null) {
			defaultNotificationInterval = defaultNotificationInterval.trim();
		}
		this.defaultNotificationInterval = defaultNotificationInterval;
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getDefaultNotificationInterval()
	 */
	public String getDefaultNotificationInterval() {
		return defaultNotificationInterval;
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsManager#getNotificationIntervals()
	 */
	public List<String> getEnabledNotificationIntervals() {
		return notificationIntervals;
	}
}