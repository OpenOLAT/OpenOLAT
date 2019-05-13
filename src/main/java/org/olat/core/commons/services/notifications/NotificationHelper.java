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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */ 
package org.olat.core.commons.services.notifications;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.ui.NotificationNewsController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;


/**
 * 
 * Description:<br>
 * Helper for some tasks with notifications
 * 
 * <P>
 * Initial Date:  01.12.2009 <br>
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */

public class NotificationHelper {
	
	private static final Logger log = Tracing.createLoggerFor(NotificationHelper.class);
	private static CacheWrapper<Long,String> userPropertiesCache;
	
	static {
		userPropertiesCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(NotificationHelper.class.getSimpleName(), "userPropertiesCache");			
	}
	
	public static Map<Subscriber, SubscriptionInfo> getSubscriptionMap(Identity identity, Locale locale, boolean showWithNewsOnly, Date compareDate) {
		return getSubscriptionMap(identity, locale, showWithNewsOnly, compareDate, Collections.<String>emptyList());
	}
	
	public static Map<Subscriber, SubscriptionInfo> getSubscriptionMap(Identity identity, Locale locale, boolean showWithNewsOnly, Date compareDate, List<String> types) {
		NotificationsManager man = NotificationsManager.getInstance();
		List<Subscriber> subs = man.getSubscribers(identity, types);
		return getSubscriptionMap(locale, showWithNewsOnly, compareDate, subs);	
	}
	
	public static Map<Subscriber, SubscriptionInfo> getSubscriptionMap(Locale locale, boolean showWithNewsOnly, Date compareDate, List<Subscriber> subs) {		
		NotificationsManager man = NotificationsManager.getInstance();
		Map<Subscriber, SubscriptionInfo> subToSubInfo = new HashMap<>();
		// calculate subscription info for all subscriptions and, if only those with news
		// are to be shown, remove the other ones
		for (Subscriber subscriber:subs) {
			Publisher pub = subscriber.getPublisher();
			SubscriptionInfo subsInfo;
			if (man.isPublisherValid(pub)) {
				NotificationsHandler notifHandler = man.getNotificationsHandler(pub);
				if (notifHandler != null) {
					subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
				} else {
					log.error("getSubscriptionMap: No notificationhandler for valid publisher: "+pub+", resname: "+pub.getResName()+", businesspath: "+pub.getBusinessPath()+", subscriber: "+subscriber);
					subsInfo = man.getNoSubscriptionInfo();
				}
			} else {
				subsInfo = man.getNoSubscriptionInfo();
			}
			if (subsInfo != null && subsInfo.hasNews() || !showWithNewsOnly) {
				subToSubInfo.put(subscriber, subsInfo);
			}
		}
		return subToSubInfo;
	}
	

	public static String getFormatedName(Long identityKey) {
		String formattedName;
		if (identityKey == null) {
			Translator trans = Util.createPackageTranslator(NotificationNewsController.class, I18nManager.getInstance().getLocaleOrDefault(null));
			return trans.translate("user.unknown");
		} else {
			// Optimize: use from cache to not re-calculate user properties over and over again
			formattedName = userPropertiesCache.get(identityKey);
			if (formattedName != null) {
				return formattedName;
			}
		}
		
		Identity identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		return getFormatedName(identity);
	}
	
	/**
	 * returns "firstname lastname" or a translated "user unknown" for a given
	 * identity
	 * 
	 * @param ident
	 * @return
	 */
	public static String getFormatedName(Identity ident) {
		String formattedName;
		if (ident == null) {
			Translator trans = Util.createPackageTranslator(NotificationNewsController.class, I18nManager.getInstance().getLocaleOrDefault(null));
			return trans.translate("user.unknown");
		} else {
			// Optimize: use from cache to not re-calculate user properties over and over again
			formattedName = userPropertiesCache.get(ident.getKey());
			if (formattedName != null) {
				return formattedName;
			}
		}
		
		Translator trans = Util.createPackageTranslator(NotificationNewsController.class, I18nManager.getInstance().getLocaleOrDefault(
				ident.getUser().getPreferences().getLanguage()));
		User user = ident.getUser();
		if (user == null) {
			formattedName =  trans.translate("user.unknown");
		} else {
			// grap user properties from context
			List<UserPropertyHandler> propertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(NotificationHelper.class.getName(), false);
			String[] properties = new String[propertyHandlers.size()];
			for (int i = 0; i < propertyHandlers.size(); i++) {
				UserPropertyHandler propHandler = propertyHandlers.get(i);
				String prop = propHandler.getUserProperty(user, trans.getLocale());
				if(StringHelper.containsNonWhitespace(prop)) {
					properties[i] = prop;
				} else {
					properties[i] = "-";
				}
			}
			formattedName = trans.translate("user.formatted", properties);
		}
		// put formatted name in cache, times out after 5 mins
		userPropertiesCache.put(ident.getKey(), formattedName);
		return formattedName;
	}

	/**
	 * @param mimeType
	 * @param titleSb
	 */
	public static void appendLineBreak(String mimeType, StringBuilder titleSb) {
		if (mimeType.equals(SubscriptionInfo.MIME_HTML)) {
			titleSb.append("<br/>"); 
		} else {
			titleSb.append("\n");
		}
	}
	
	
	
}
