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

package org.olat.core.util.mail.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.notifications.items.TitleItem;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailNotificationsHandler extends LogDelegator implements NotificationsHandler  {
	
	private MailModule mailModule;
	
	public MailNotificationsHandler() {
		//
	}
	
	/**
	 * [user by Spring]
	 * @param mailModule
	 */
	public void setMailModule(MailModule mailModule) {
		this.mailModule = mailModule;
	}



	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) { 
		if(!mailModule.isInternSystem()) {
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		
		String realMail = subscriber.getIdentity().getUser().getPreferences().getReceiveRealMail();
		if("true".equals(realMail)) {
			//receive real e-mails
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		} else if (!StringHelper.containsNonWhitespace(realMail) && mailModule.isReceiveRealMailUserDefaultSetting()) {
			//user has no settings, check the default setting
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
			try {
				List<DBMail> inbox = MailManager.getInstance().getInbox(subscriber.getIdentity(), Boolean.TRUE, Boolean.FALSE, compareDate, 0, 0);
				if(!inbox.isEmpty()) {
					Translator translator = Util.createPackageTranslator(MailModule.class, locale);
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(translator.translate("mail.notification.type"), "o_co_icon"), null);
					for (DBMail mail : inbox) {
						String subject = mail.getSubject();
						String businessPath = "[Inbox:" + mail.getKey() + "]";
						String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, businessPath);
						SubscriptionListItem subListItem = new SubscriptionListItem(subject, urlToSend, mail.getCreationDate(), "o_co_icon");
						si.addSubscriptionListItem(subListItem);
					}
				}
			} catch(Exception ex) {
				logError("", ex);
			}
		}
		
		if( si == null) {
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		return si;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(MailModule.class, locale);
		return translator.translate("mail.notification.type");
	}

	@Override
	public String getType() {
		return "Inbox";
	}
}