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
* <p>
*/

package org.olat.core.commons.modules.bc.notifications;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
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
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description: <br>
 * create SubscriptionInfo for a folder.
 * <P>
 * 
 * Initial Date: 25.10.2004 <br>
 * 
 * @author Felix Jost
 */
public class FolderNotificationsHandler implements NotificationsHandler {
	private static final OLog log = Tracing.createLoggerFor(FolderNotificationsHandler.class);
	
	/**
	 * 
	 */
	public FolderNotificationsHandler() {
	//
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsHandler#createSubscriptionInfo(org.olat.core.commons.services.notifications.Subscriber,
	 *      java.util.Locale, java.util.Date)
	 */
	@Override
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		String genericBusinessPath = p.getBusinessPath() + "[path=";
		
		SubscriptionInfo si;
		// there could be news for me, investigate deeper
		try {
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				if("CourseModule".equals(p.getResName())) {
					RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
					if(re == null || re.getEntryStatus().decommissioned()) {
						return NotificationsManager.getInstance().getNoSubscriptionInfo();
					}
				}
				
				String folderRoot = p.getData();
				final List<FileInfo> fInfos = FolderManager.getFileInfos(folderRoot, compareDate);
				final Translator translator = Util.createPackageTranslator(FolderNotificationsHandler.class, locale);

				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItem(p, translator), null);
				SubscriptionListItem subListItem;
				for (Iterator<FileInfo> it_infos = fInfos.iterator(); it_infos.hasNext();) {
					FileInfo fi = it_infos.next();
					String title = fi.getRelPath();
					
					// don't show changes in meta-directories. first quick check
					// for any dot files and then compare with our black list of
					// known exclude prefixes
					if (title != null && title.indexOf("/.") != -1 && FileUtils.isMetaFilename(title)) {
						// skip this file, continue with next item in folder
						continue;
					}						
					MetaInfo metaInfo = fi.getMetaInfo();
					String iconCssClass =  null;
					if (metaInfo != null) {
						if (metaInfo.getTitle() != null) {
							title += " (" + metaInfo.getTitle() + ")";
						}
						iconCssClass = metaInfo.getIconCssClass();
					}
					Long identityKey = fi.getAuthorIdentityKey();
					Date modDate = fi.getLastModified();

					String desc = translator.translate("notifications.entry", new String[] { title, NotificationHelper.getFormatedName(identityKey) });
					String urlToSend = null;
					String businessPath = null;
					if(p.getBusinessPath() != null) {
						businessPath = genericBusinessPath + fi.getRelPath() + "]";
						urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
					}
					subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass);
					si.addSubscriptionListItem(subListItem);
				}
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating folder's notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		return si;
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if("BusinessGroup".equals(p.getResName())) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(p.getResId());
				if(bg == null) {
					log.info("deactivating publisher with key; " + p.getKey(), null);
					NotificationsManager.getInstance().deactivate(p);
				}
			} else if ("CourseModule".equals(p.getResName())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; " + p.getKey(), null);
					NotificationsManager.getInstance().deactivate(p);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private TitleItem getTitleItem(Publisher p, Translator translator) {
		String title;
		try {
			String resName = p.getResName();
			if("BusinessGroup".equals(resName)) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(p.getResId());
				title = translator.translate("notifications.header.group", new String[]{bg.getName()});
			} else if("CourseModule".equals(resName)) {
				String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(p.getResId());
				title = translator.translate("notifications.header.course", new String[]{displayName});
			} else {
				title = translator.translate("notifications.header");
			}
		} catch (Exception e) {
			log.error("", e);
			checkPublisher(p);
			title = translator.translate("notifications.header");
		}
		return new TitleItem(title, CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(FolderNotificationsHandler.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return "FolderModule";
	}
}