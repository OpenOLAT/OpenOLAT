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
package org.olat.course.nodes.gta.manager;

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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTANotificationsHandler implements NotificationsHandler  {
	
	private static final Logger log = Tracing.createLoggerFor(GTANotificationsHandler.class);
	protected static final String CSS_CLASS_ICON = "o_gta_icon";
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentEntryDAO courseNodeAssessmentDao;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();
	
		SubscriptionInfo si;
		// there could be news for me, investigate deeper
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				GTANotifications notifications = new GTANotifications(subscriber, false, locale, compareDate,
						repositoryService, gtaManager, businessGroupService, userManager, courseNodeAssessmentDao);
				List<SubscriptionListItem> items = notifications.getItems();
				if(items.isEmpty()) {
					si = notificationsManager.getNoSubscriptionInfo();
				} else {
					String title = notifications.getNotifificationHeader();
					TitleItem titleItem = new TitleItem(title, CSS_CLASS_ICON);
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), titleItem, items);
				}
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Cannot create gtask notifications for subscriber: " + subscriber.getKey(), e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		String title;
		try {
			Translator translator = Util.createPackageTranslator(GTARunController.class, locale);
			Long resId = subscriber.getPublisher().getResId();
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(resId);
			title = translator.translate("notifications.header", new String[]{ displayName });
		} catch (Exception e) {
			log.error("Error while creating task notifications for subscriber: " + subscriber.getKey(), e);
			title = "-";
		}
		return title;
	}

	@Override
	public String getType() {
		return "GroupTask";
	}
}
