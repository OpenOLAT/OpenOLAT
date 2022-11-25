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
package org.olat.modules.library.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.library.ui.LibraryMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * A notification handler for new document in library
 * <P>
 * Initial Date:  4 sept. 2009 <br>
 *
 * @author srosse
 */
@Service
public class LibraryNotificationHandler implements NotificationsHandler {

	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();
		
		String businessPath = p.getBusinessPath() + "[path=";
		
		SubscriptionInfo si;
		// there could be news for me, investigate deeper
		if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
			
			List<CatalogItem> items = libraryManager.getNewCatalogItems(compareDate, subscriber.getIdentity());
			if (items.isEmpty()) {
				si = notificationsManager.getNoSubscriptionInfo();
			} else {
				final Translator translator = Util.createPackageTranslator(LibraryMainController.class, locale);
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), getTitleItem(translator), null);
				
				SubscriptionListItem subListItem;
				for (CatalogItem ci:items) {
					String filePath = ci.getShortPath();
					VFSMetadata metaInfo = ci.getMetaInfo();
					
					Date modDate = null;
					Identity author = null;
					String iconCssClass =  null;
					if (metaInfo != null) {
						iconCssClass = metaInfo.getIconCssClass();
						author = metaInfo.getFileInitializedBy();
						modDate = metaInfo.getFileLastModified();
					}
	
					String desc = translator.translate("library.notifications.entry", new String[] { filePath, NotificationHelper.getFormatedName(author) });
					String urlToSend = null;
					String bPath = null;
					if(p.getBusinessPath() != null) {
						bPath = businessPath + filePath + "]";
						urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
					}
					subListItem = new SubscriptionListItem(desc, urlToSend, bPath, modDate, iconCssClass);
					si.addSubscriptionListItem(subListItem);
				}
			}
		} else {
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private TitleItem getTitleItem(Translator translator) {
		String title;
		title = translator.translate("library.notifications.header");
		return new TitleItem(title, LibraryMainController.ICON_CSS_CLASS);
	}
	
	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(LibraryMainController.class, locale);
		TitleItem title = getTitleItem(translator);
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return "LibrarySite";
	}
}