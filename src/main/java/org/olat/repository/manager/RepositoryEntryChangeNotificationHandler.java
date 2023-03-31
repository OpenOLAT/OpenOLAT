/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuditLog;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: Mär 15, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service("repositoryEntryChangeNotificationHandler")
public class RepositoryEntryChangeNotificationHandler implements NotificationsHandler {

	public static final String TYPE = "LearningRes";
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryChangeNotificationHandler.class);
	@Autowired
	private UserManager userManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryService repositoryService;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si;
		Publisher publisher = subscriber.getPublisher();

		try {
			Date latestNews = publisher.getLatestNewsDate();
			Identity identity = subscriber.getIdentity();
			Translator translator = Util.createPackageTranslator(RepositoryService.class, locale);

			if (notificationsManager.isPublisherValid(publisher) && compareDate.before(latestNews)) {
				List<SubscriptionListItem> items = new ArrayList<>();
				addNewRepoEntryStatusChanges(items, translator, identity);

				if (items.isEmpty()) {
					si = notificationsManager.getNoSubscriptionInfo();
				} else {
					String title = translator.translate("notifications.header");
					TitleItem titleItem = new TitleItem(title, null);
					si = new SubscriptionInfo(subscriber.getKey(), publisher.getType(), titleItem, items);
				}
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error while creating repositoryEntryStatusChange notifications", e);
			return notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private void addNewRepoEntryStatusChanges(List<SubscriptionListItem> items,
											  Translator translator,
											  Identity identity) {
		List<RepositoryEntryAuditLog> auditLogs = repositoryService.getAuditLogs(identity);
		for (RepositoryEntryAuditLog auditLog : auditLogs) {
			RepositoryEntry repositoryEntry = repositoryService.loadByKey(auditLog.getEntryKey());
			if (repositoryEntry != null
					&& repositoryService.hasRole(identity, repositoryEntry, GroupRoles.owner.name())) {
				SubscriptionListItem item = createNewRepositoryEntryStatusChangeItem(translator, auditLog, repositoryEntry);
				if (item != null) {
					items.add(item);
				}
			}
		}
	}

	private SubscriptionListItem createNewRepositoryEntryStatusChangeItem(
			Translator translator, RepositoryEntryAuditLog auditLog, RepositoryEntry repositoryEntry) {
		try {
			// TODO: log also finally deleted entries, so repositoryEntry can't be null
			RepositoryEntry auditBeforeRe = repositoryService.toAuditRepositoryEntry(auditLog.getBefore());
			RepositoryEntry auditAfterRe = repositoryService.toAuditRepositoryEntry(auditLog.getAfter());
			String desc = translator.translate("notification.new.status.change",
					repositoryEntry.getDisplayname(),
					userManager.getUserDisplayName(auditLog.getAuthorKey()),
					auditBeforeRe == null ? "unknown" : translator.translate("cif.status." + auditBeforeRe.getStatus()),
					auditAfterRe == null ? "unknown" : translator.translate("cif.status." + auditAfterRe.getStatus()));

			String businessPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			Date dateInfo = auditLog.getCreationDate();
			return new SubscriptionListItem(desc, url, businessPath, dateInfo, RepositoyUIFactory.getIconCssClass(repositoryEntry));
		} catch (Exception e) {
			log.error("Error while creating repositoryEntryStatusChange notifications", e);
			return null;
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		return "-";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Publisher publisher) {
		return "-";
	}

	@Override
	public String getIconCss() {
		// No icon desired for learning resource
		return null;
	}

	@Override
	public String getAdditionalDescriptionI18nKey(Locale locale) {
		Translator translator = Util.createPackageTranslator(RepositoryService.class, locale);
		return translator.translate("notification.additional.desc");
	}

}
