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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;


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
	
	private static final OLog log = Tracing.createLoggerFor(NotificationHelper.class);

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
		Map<Subscriber, SubscriptionInfo> subToSubInfo = new HashMap<Subscriber, SubscriptionInfo>();
		// calc subscriptioninfo for all subscriptions and, if only those with news
		// are to be shown, remove the other ones
		for (Iterator<Subscriber> it_subs = subs.iterator(); it_subs.hasNext();) {
			Subscriber subscriber = it_subs.next();
			Publisher pub = subscriber.getPublisher();
			SubscriptionInfo subsInfo;
			if (man.isPublisherValid(pub)) {
				NotificationsHandler notifHandler = man.getNotificationsHandler(pub);
				if (notifHandler!=null) {
					subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
				} else {
					// OLAT-5647
					log.error("getSubscriptionMap: No notificationhandler for valid publisher: "+pub+", resname: "+pub.getResName()+", businesspath: "+pub.getBusinessPath()+", subscriber: "+subscriber);
					subsInfo = man.getNoSubscriptionInfo();
				}
			} else {
				subsInfo = man.getNoSubscriptionInfo();
			}
			if (subsInfo.hasNews() || !showWithNewsOnly) {
				subToSubInfo.put(subscriber, subsInfo);
			}
		}
		return subToSubInfo;
	}
	
	/**
	 * returns "firstname lastname" or a translated "user unknown" for a given
	 * identity
	 * 
	 * @param ident
	 * @return
	 */
	public static String getFormatedName(Identity ident) {
		Translator trans;
		User user = null;
		if (ident == null) {
			trans = Util.createPackageTranslator(NotificationHelper.class, I18nManager.getInstance().getLocaleOrDefault(null));
		} else {
		 trans = Util.createPackageTranslator(NotificationHelper.class, I18nManager.getInstance().getLocaleOrDefault(
				ident.getUser().getPreferences().getLanguage()));
		 user = ident.getUser();
		}
		if (user == null) return trans.translate("user.unknown");
		return user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
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
