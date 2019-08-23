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

package org.olat.modules.fo;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Initial Date: 25.10.2004 <br>
 * 
 * @author Felix Jost
 */
public class ForumNotificationsHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(ForumNotificationsHandler.class);

	public ForumNotificationsHandler() {
	// nothing to do
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsHandler#createSubscriptionInfo(org.olat.core.commons.services.notifications.Subscriber,
	 *      java.util.Locale, java.util.Date)
	 */
	@Override
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, Locale locale, Date compareDate) {
		try {
			Publisher p = subscriber.getPublisher();
			Date latestNews = p.getLatestNewsDate();
			
			SubscriptionInfo si;
			// there could be news for me, investigate deeper
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				String businessControlString = "";
				Long forumKey = Long.valueOf(0);
				try {
					forumKey = Long.parseLong(p.getData());
				} catch (NumberFormatException e) {
					log.error("Could not parse forum key!", e);
					NotificationsManager.getInstance().deactivate(p);
					return NotificationsManager.getInstance().getNoSubscriptionInfo();
				}
				
				if("CourseModule".equals(p.getResName())) {
					RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
					if(re == null || re.getEntryStatus().decommissioned()) {
						return NotificationsManager.getInstance().getNoSubscriptionInfo();
					}
				}
				
				final List<Message> mInfos = CoreSpringFactory.getImpl(ForumManager.class).getNewMessageInfo(forumKey, compareDate);
				final Translator translator = Util.createPackageTranslator(ForumNotificationsHandler.class, locale);
				
				businessControlString = p.getBusinessPath() + "[Message:";
				
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItem(p, translator), null);
				for (Message mInfo : mInfos) {
					String title = mInfo.getTitle();
					Identity creator = mInfo.getCreator();
					Identity modifier = mInfo.getModifier();
					Date modDate = mInfo.getLastModified();
					
					String name;
					if(modifier != null) {
						if(modifier.equals(creator) && StringHelper.containsNonWhitespace(mInfo.getPseudonym())) {
							name = mInfo.getPseudonym();
						} else {
							name = NotificationHelper.getFormatedName(modifier);
						}
					} else if(StringHelper.containsNonWhitespace(mInfo.getPseudonym())) {
						name = mInfo.getPseudonym();
					} else if(mInfo.isGuest()) {
						name = translator.translate("anonymous.poster");
					} else {
						name = NotificationHelper.getFormatedName(creator);
					}
					final String descKey = "notifications.entry" + (mInfo.getCreationDate().equals(mInfo.getLastModified()) ? "" : ".modified");
					final String desc = translator.translate(descKey, new String[] { title, name });
					String urlToSend = null;
					String businessPath = null;
					if(p.getBusinessPath() != null) {
						businessPath = businessControlString + mInfo.getKey().toString() + "]";
						urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
					}
					
					SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, ForumUIFactory.CSS_ICON_CLASS_MESSAGE);
					si.addSubscriptionListItem(subListItem);
				}
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Error while creating forum's notifications from publisher with key:" + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if("BusinessGroup".equals(p.getResName())) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(p.getResId());
				if(bg == null) {
					log.info("deactivating publisher with key; " + p.getKey());
					NotificationsManager.getInstance().deactivate(p);
				}
			} else if ("CourseModule".equals(p.getResName())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; " + p.getKey());
					NotificationsManager.getInstance().deactivate(p);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(ForumNotificationsHandler.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}
	
	private TitleItem getTitleItem(Publisher p, final Translator translator) {
		Long resId = p.getResId();
		String type = p.getResName();
		String title;
		try {
			if("BusinessGroup".equals(type)) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(resId);
				title = translator.translate("notifications.header.group", new String[]{bg.getName()});
			} else if ("CourseModule".equals(type)) {
				String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
				CourseNode node = CourseFactory.loadCourse(p.getResId()).getRunStructure().getNode(p.getSubidentifier());
				String shortName = (node != null ? node.getShortName() : "");
				title = translator.translate("notifications.header.course", new String[]{displayName, shortName});
			} else {
				title = translator.translate("notifications.header");
			}
		} catch (Exception e) {
			log.error("Error while creating assessment notifications for publisher: " + p.getKey(), e);
			checkPublisher(p);
			title = translator.translate("notifications.header");
		}
		return new TitleItem(title, ForumUIFactory.CSS_ICON_CLASS_FORUM);
	}

	@Override
	public String getType() {
		return "Forum";
	}
}
