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

package org.olat.modules.dialog;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
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
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Notification handler for course node dialog. Subscribers get informed about
 * new uploaded file in the dialog table.
 * <P>
 * Initial Date: 23.11.2005 <br>
 * 
 * @author guido
 */
public class FileUploadNotificationHandler implements NotificationsHandler {
	private static final OLog log = Tracing.createLoggerFor(FileUploadNotificationHandler.class);
	private static final String CSSS_CLASS_UPLOAD_ICON = "o_dialog_icon";

	public FileUploadNotificationHandler() {
	//
	}

	/**
	 * @see org.olat.core.commons.services.notifications.NotificationsHandler#createSubscriptionInfo(org.olat.core.commons.services.notifications.Subscriber,
	 *      java.util.Locale, java.util.Date)
	 */
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		final Date latestNews = p.getLatestNewsDate();

		SubscriptionInfo si;
		// there could be news for me, investigate deeper
		try {
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
				if(re == null) {
					if(!checkPublisher(subscriber.getPublisher())) {
						return NotificationsManager.getInstance().getNoSubscriptionInfo();
					}
				} else if(re.getRepositoryEntryStatus().isClosed() || re.getRepositoryEntryStatus().isUnpublished()) {
					return NotificationsManager.getInstance().getNoSubscriptionInfo();
				}

				String displayname = re.getDisplayname();
				
				DialogElementsPropertyManager mgr = DialogElementsPropertyManager.getInstance();
				DialogPropertyElements elements = mgr.findDialogElements(p.getResId(), p.getSubidentifier());
				final List<DialogElement> dialogElements = elements.getDialogPropertyElements();
				final Translator translator = Util.createPackageTranslator(FileUploadNotificationHandler.class, locale);

				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(translator.translate("notifications.header", new String[]{displayname}), CSSS_CLASS_UPLOAD_ICON), null);
				SubscriptionListItem subListItem;
				for (DialogElement element : dialogElements) {
					// do only show entries newer then the ones already seen
					if (element.getDate().after(compareDate)) {
						String filename = element.getFilename();
						String creator = element.getAuthor();
						Identity ident = BaseSecurityManager.getInstance().findIdentityByName(creator);
						Date modDate = element.getDate();

						String desc = translator.translate("notifications.entry", new String[] { filename, NotificationHelper.getFormatedName(ident) });
						String businessPath = p.getBusinessPath();
						String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						String cssClass = CSSHelper.createFiletypeIconCssClassFor(filename);

						subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, cssClass);
						si.addSubscriptionListItem(subListItem);
					}
				}
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating file upload's notifications for subscriber: " + subscriber.getKey(), e);
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		return si;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Translator translator = Util.createPackageTranslator(FileUploadNotificationHandler.class, locale);
			String displayname = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(subscriber.getPublisher().getResId());
			if(displayname == null) {
				checkPublisher(subscriber.getPublisher());
			}
			return translator.translate("notifications.header", new String[]{displayname});
		} catch (Exception e) {
			log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}
	
	private boolean checkPublisher(Publisher p) {
		try {
			if ("CourseModule".equals(p.getResName())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; " + p.getKey(), null);
					NotificationsManager.getInstance().deactivate(p);
					return false;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return true;
	}

	@Override
	public String getType() {
		return "DialogElement";
	}
}
