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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: <br>
 * create SubscriptionInfo for a folder.
 * <P>
 * 
 * Initial Date: 25.10.2004 <br>
 * 
 * @author Felix Jost
 */
@Service
public class FolderNotificationsHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(FolderNotificationsHandler.class);
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private VFSVersionModule versionModule;
	@Autowired
	private List<DocEditor> editors;
	
	@Override
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		String genericBusinessPath = p.getBusinessPath() + "[path=";
		
		SubscriptionInfo si;
		// there could be news for me, investigate deeper
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				if("CourseModule".equals(p.getResName())) {
					RepositoryEntry re = repositoryManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
					if(re == null || re.getEntryStatus().decommissioned()) {
						return notificationsManager.getNoSubscriptionInfo();
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
					VFSMetadata metaInfo = fi.getMetaInfo();
					String iconCssClass =  null;
					if (metaInfo != null) {
						if (StringHelper.containsNonWhitespace(metaInfo.getTitle())) {
							title += " (" + metaInfo.getTitle() + ")";
						}
						iconCssClass = metaInfo.getIconCssClass();
					}
					Long identityKey = fi.getAuthorIdentityKey();
					Date modDate = fi.getLastModified();

					// Documents may be edited with a document editor, so author is not the modifier
					boolean anonymous = versionModule.getMaxNumberOfVersions() < 1 && hasDocumentEditor(fi.getMetaInfo());
					String desc = anonymous
							? translator.translate("notifications.entry.anonymous", new String[] { title })
							: translator.translate("notifications.entry", new String[] { title, NotificationHelper.getFormatedName(identityKey) });
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
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating folder's notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	private boolean hasDocumentEditor(VFSMetadata metaInfo) {
		String suffix = FileUtils.getFileSuffix(metaInfo.getFilename());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isSupportingFormat(suffix, Mode.EDIT, true))
				.findFirst()
				.isPresent();
	}

	private void checkPublisher(Publisher p) {
		try {
			if("BusinessGroup".equals(p.getResName())) {
				BusinessGroup bg = businessGroupService.loadBusinessGroup(p.getResId());
				if(bg == null) {
					log.info("deactivating publisher with key; {}", p.getKey());
					notificationsManager.deactivate(p);
				}
			} else if ("CourseModule".equals(p.getResName())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; {}", p.getKey());
					notificationsManager.deactivate(p);
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
				BusinessGroup bg = businessGroupService.loadBusinessGroup(p.getResId());
				title = translator.translate("notifications.header.group", new String[]{bg.getName()});
			} else if("CourseModule".equals(resName)) {
				String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(p.getResId());
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