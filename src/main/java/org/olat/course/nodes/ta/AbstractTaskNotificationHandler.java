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

package org.olat.course.nodes.ta;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic abstract notification-handler for all task-notification-handler. 
 * @author guretzki
 */
public abstract class AbstractTaskNotificationHandler {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractTaskNotificationHandler.class);

	@Autowired
	private NotificationsManager notificationsManager;
	
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();
	
		SubscriptionInfo si;
	
		// there could be news for me, investigate deeper
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				String folderRoot = p.getData();
				
				boolean logDebug = log.isDebugEnabled();
				if (logDebug){
					log.debug("folderRoot=" + folderRoot);
				}
				final List<FileInfo> fInfos = FolderManager.getFileInfos(folderRoot, compareDate);
				final Translator translator = Util.createPackageTranslator(AbstractTaskNotificationHandler.class, locale);
				
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance("CourseModule", p.getResId()), false);
				if(re == null) {
					if(!checkPublisher(p)) {
						return notificationsManager.getNoSubscriptionInfo();
					}
				} else if(re.getEntryStatus().decommissioned()) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				String displayName = re == null ? "" : re.getDisplayname();
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(translator.translate(getNotificationHeaderKey(), new String[]{displayName}), getCssClassIcon() ), null);
				SubscriptionListItem subListItem;
				for (Iterator<FileInfo> it_infos = fInfos.iterator(); it_infos.hasNext();) {
					FileInfo fi = it_infos.next();
					VFSMetadata metaInfo = fi.getMetaInfo();
					String filePath = fi.getRelPath();
					if(logDebug) log.debug("filePath=" + filePath);
					String fullUserName = getUserNameFromFilePath(metaInfo, filePath);
							
					Date modDate = fi.getLastModified();
					String desc = translator.translate(getNotificationEntryKey(), new String[] { filePath, fullUserName });
					String businessPath = p.getBusinessPath();
					String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
							
					String iconCssClass =  null;
					if (metaInfo != null) {
						iconCssClass = metaInfo.getIconCssClass();
					}
					subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass);
					si.addSubscriptionListItem(subListItem);						
				}
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Cannot create task notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(p);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	/**
	 * Extract username form file-path and return the firstname and lastname.
	 * @param filePath E.g. '/username/abgabe.txt'
	 * @return 'firstname lastname'
	 */
	protected String getUserNameFromFilePath(VFSMetadata info, String filePath) {
		// remove first '/'
		try {
			if(info != null) {
				return NotificationHelper.getFormatedName(info.getFileInitializedBy());
			}

			String path = filePath.substring(1);
			int slashIndex = path.indexOf('/');
			if (slashIndex != -1) {
				String userName = path.substring(0, slashIndex);
				Identity identity = BaseSecurityManager.getInstance().findIdentityByName(userName);
				return NotificationHelper.getFormatedName(identity);
			}
			return "";
		} catch (Exception e) {
			log.warn("Can not extract user from path={}", filePath);
			return "";
		}
	}

	private boolean checkPublisher(Publisher p) {
		try {
			if ("CourseModule".equals(p.getResName()) && !NotificationsUpgradeHelper.checkCourse(p)) {
				log.info("deactivating publisher with key; {}", p.getKey());
				notificationsManager.deactivate(p);
				return false;
			}
		} catch (Exception e) {
			log.error("Could not check Publisher", e);
		}
		return true;
	}
	
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Translator translator = Util.createPackageTranslator(AbstractTaskNotificationHandler.class, locale);
			Long resId = subscriber.getPublisher().getResId();
			String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
			return translator.translate(getNotificationHeaderKey(), new String[]{displayName});
		} catch (Exception e) {
			log.error("Error while creating task notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	public static ContextualSubscriptionController createContextualSubscriptionController(UserRequest ureq, WindowControl wControl, String folderPath,
			SubscriptionContext subsContext, Class<?> callerClass) {
		String businessPath = wControl.getBusinessControl().getAsString();
		PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(callerClass), folderPath, businessPath);
		return new ContextualSubscriptionController(ureq, wControl, subsContext, pdata);
	}

	// Abstract methods
	////////////////////
	protected abstract String getCssClassIcon();
	
	protected abstract String getNotificationHeaderKey();
	
	protected abstract String getNotificationEntryKey();
}