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
package org.olat.user.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * This is an implementation of the NotificationsHandler for newly created
 * users.
 * <P>
 * Initial Date: 18 august 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class NewUsersNotificationHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(NewUsersNotificationHandler.class);

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private UsersSubscriptionManager usersSubscriptionManager;
	
	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		SubscriptionInfo si;
		Translator translator = Util.createPackageTranslator(this.getClass(), locale);
		// there could be news for me, investigate deeper
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				Identity identity = subscriber.getIdentity();
				Roles roles = securityManager.getRoles(identity);
				List<Identity> identities = usersSubscriptionManager.getNewIdentityCreated(compareDate, subscriber.getIdentity(), roles);
				if (identities.isEmpty()) {
					si = notificationsManager.getNoSubscriptionInfo();
				} else {
					translator = Util.createPackageTranslator(this.getClass(), locale);
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(getItemTitle(identities, translator), CSSHelper.CSS_CLASS_GROUP), null);
					SubscriptionListItem subListItem;
					for (Identity newUser : identities) {
						String desc = translator.translate("notifications.entry", new String[] { NotificationHelper.getFormatedName(newUser) });
						String businessPath = "[Identity:" + newUser.getKey() + "]";
						String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						Date modDate = newUser.getCreationDate();
						subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, CSSHelper.CSS_CLASS_USER);
						si.addSubscriptionListItem(subListItem);
					}
				}
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating new identity's notifications for subscriber: " + subscriber.getKey(), e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private String getItemTitle(List<Identity> identities, Translator translator) {
		String numOfNewUsers = Integer.toString(identities.size());
		if (identities.size() > 1) { return translator.translate("notifications.title", new String[] { numOfNewUsers }); }
		return translator.translate("notifications.titleOne");
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(this.getClass(), locale);
		return translator.translate("notifications.table.title");
	}

	@Override
	public String getType() {
		return "User";
	}
}
