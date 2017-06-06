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
package org.olat.modules.webFeed.manager;

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
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
*
* Initial date: 11.05.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
@Service
public class PodcastNotificationsHandler implements NotificationsHandler {
	
	private static final String NOTIFICATIONS_HEADER_COURSE = "notifications.header.course";
	private static final String NOTIFICATIONS_HEADER = "notifications.header";
	private static final String NOTIFICATIONS_HEADER_PODCAST = "notifications.header.podcast";
	private static final String CSS_CLASS_ICON_PODCAST = "o_podcast_icon";
	
	private static final OLog log = Tracing.createLoggerFor(PodcastNotificationsHandler.class);

	
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryManager repoManager;
	

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		
		try {
		 	final Translator translator = Util.createPackageTranslator(FeedMainController.class, locale);
			
		 	FeedNotifications notifications = new FeedNotifications(subscriber, locale, compareDate, notificationsManager);
		 	List<SubscriptionListItem> items = notifications.getItems();
			
			if (items.isEmpty()) {
				si = notificationsManager.getNoSubscriptionInfo();
			} else {
				String title;
				try {
					RepositoryEntry re = repoManager.lookupRepositoryEntry(
							OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
					String displayName = re.getDisplayname();
					if("CourseModule".equals(p.getResName())) {
						if (re.getRepositoryEntryStatus().isClosed() || re.getRepositoryEntryStatus().isUnpublished()) {
							return notificationsManager.getNoSubscriptionInfo();
						} else {
							title = translator.translate(NOTIFICATIONS_HEADER_COURSE,  new String[]{displayName});
						}
					} else {
						title = translator.translate(NOTIFICATIONS_HEADER_PODCAST,  new String[]{displayName});
					}
				} catch (Exception e) {
					log.error("Unknown Exception", e);
					title = translator.translate(NOTIFICATIONS_HEADER);
				}
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(),	new TitleItem(title, CSS_CLASS_ICON_PODCAST), items);				
			}
		} catch (Exception e) {
			log.error("Unknown Exception", e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(FeedMainController.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return "FileResource.PODCAST";
	}
	
	private TitleItem getTitleItem(Publisher p, Translator translator) {
		String title;
		try {
			String displayName = repoManager.lookupDisplayNameByOLATResourceableId(p.getResId());
			title = translator.translate(NOTIFICATIONS_HEADER_PODCAST,  new String[]{displayName});
		} catch (Exception e) {
			log.error("", e);
			checkPublisher(p);
			title = translator.translate(NOTIFICATIONS_HEADER);
		}
		return new TitleItem(title, CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
	}
	
	private void checkPublisher(Publisher p) {
		try {
			RepositoryEntry entry = repoManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
			if (entry == null) { 
				notificationsManager.deactivate(p);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
