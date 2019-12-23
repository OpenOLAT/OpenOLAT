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

package org.olat.commons.info.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageManager;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("org.olat.commons.info.notification.InfoMessageNotificationHandler")
public class InfoMessageNotificationHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(InfoMessageNotificationHandler.class);

	private static final String CSS_CLASS_ICON = "o_infomsg_icon";
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private InfoMessageManager infoMessageManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
		
			try {
				final Long resId = subscriber.getPublisher().getResId();
				final String resName = subscriber.getPublisher().getResName();
				String resSubPath = subscriber.getPublisher().getSubidentifier();
				
				String displayName;
				String notificationtitle;
				if ("BusinessGroup".equals(resName)) {
					BusinessGroup group = businessGroupService.loadBusinessGroup(resId);
					displayName = group.getName();
					notificationtitle = "notification.title.group";
				} else {
					RepositoryEntry re = repositoryManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(resName, resId), false);
					if(re== null || re.getEntryStatus().decommissioned()) {
						return notificationsManager.getNoSubscriptionInfo();
					}					
					displayName = re.getDisplayname();	
					notificationtitle = "notification.title";
				}				

				Translator translator = Util.createPackageTranslator(this.getClass(), locale);
				String title = translator.translate(notificationtitle, new String[]{ displayName });
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(title, CSS_CLASS_ICON), null);
				
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
				List<InfoMessage> infos = infoMessageManager.loadInfoMessageByResource(ores, resSubPath, null, compareDate, null, 0, 0);
				for(InfoMessage info:infos) {
					Identity ident = info.getAuthor();
					String desc = translator.translate("notifications.entry", new String[] { info.getTitle(), NotificationHelper.getFormatedName(ident) });
					String tooltip = info.getMessage();
					String infoBusinessPath = info.getBusinessPath() + "[InfoMessage:" + info.getKey() + "]";
					String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(infoBusinessPath);
					Date dateInfo = info.getModificationDate() == null ? info.getCreationDate() : info.getModificationDate();
					SubscriptionListItem subListItem = new SubscriptionListItem(desc, tooltip, urlToSend, infoBusinessPath, dateInfo, CSS_CLASS_ICON);
					si.addSubscriptionListItem(subListItem);
				}
			} catch (Exception e) {
				log.error("Unexpected exception", e);
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} else {
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(this.getClass(), locale);
		String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(subscriber.getPublisher().getResId());
		return translator.translate("notification.title", new String[]{displayName});
	}
	
	@Override
	public String getType() {
		return "InfoMessage";
	}
}
