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

import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.model.DBMailLight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mailNotificationsHandler")
public class MailNotificationsHandler implements NotificationsHandler  {

	private static final Logger log = Tracing.createLoggerFor(MailNotificationsHandler.class);
	
	@Autowired
	private MailModule mailModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) { 
		if(!mailModule.isInternSystem()) {
			return notificationsManager.getNoSubscriptionInfo();
		}
		
		String realMail = subscriber.getIdentity().getUser().getPreferences().getReceiveRealMail();
		if("true".equals(realMail)) {
			//receive real e-mails
			return notificationsManager.getNoSubscriptionInfo();
		} else if (!StringHelper.containsNonWhitespace(realMail) && mailModule.isReceiveRealMailUserDefaultSetting()) {
			//user has no settings, check the default setting
			return notificationsManager.getNoSubscriptionInfo();
		}
		
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
			try {
				List<DBMailLight> inbox = mailManager.getInbox(subscriber.getIdentity(), Boolean.TRUE, Boolean.FALSE, compareDate, 0, -1);
				if(!inbox.isEmpty()) {
					Translator translator = Util.createPackageTranslator(MailModule.class, locale);
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(translator.translate("mail.notification.type"), "o_co_icon"), null);
					for (DBMailLight mail : inbox) {
						String subject = mail.getSubject();
						String businessPath = "[Inbox:0][Inbox:0][DBMail:" + mail.getKey() + "]";
						String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						SubscriptionListItem subListItem = new SubscriptionListItem(subject, urlToSend, businessPath, mail.getCreationDate(), "o_co_icon");
						si.addSubscriptionListItem(subListItem);
					}
				}
			} catch(Exception ex) {
				log.error("", ex);
			}
		}
		
		if( si == null) {
			si = notificationsManager.getNoSubscriptionInfo();
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