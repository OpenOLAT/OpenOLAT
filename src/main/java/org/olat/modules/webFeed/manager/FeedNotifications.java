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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.ui.FeedMainController;
/**
*
* Initial date: 27.04.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class FeedNotifications {
	
	private static final Logger log = Tracing.createLoggerFor(FeedNotifications.class);
	
	private final Date compareDate;
	private final Subscriber subscriber;
	
	private final Translator translator;
	
	private final List<SubscriptionListItem> items = new ArrayList<>();
	
	private NotificationsManager notificationsManager;
	
	private FeedManager feedManager;
	
	
	public FeedNotifications(Subscriber subscriber, Locale locale, Date compareDate, NotificationsManager notificationsManager) {
		this.subscriber = subscriber;
		this.compareDate = compareDate;
		this.notificationsManager = notificationsManager;
		this.feedManager = FeedManager.getInstance();
		translator = Util.createPackageTranslator(FeedMainController.class, locale);
	}
	
	public List<SubscriptionListItem> getItems() throws Exception {
		try {
			Publisher p = subscriber.getPublisher();
			String data = p.getData();
			Date latestNews = p.getLatestNewsDate();
	
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				String resName = p.getResName();
				Long resId = p.getResId();
				Feed feed;
				if ("CourseModule".equals(resName)) {
					OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, Long.parseLong(data));
					feed = feedManager.loadFeed(ores);
				} else {
					OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
					feed = feedManager.loadFeed(ores);
				}
				List<Item> listItems = feedManager.loadItems(feed);
				for (Item item : listItems) {
					createSubscriptionItem(item, p);
				}
			}
		} catch (Exception e) {
			log.error("error in Feed notification",e);
		}
		return items;					
	}
	
	private void createSubscriptionItem(Item item, Publisher p){
		Date modDate = item.getPublishDate();
		if (item.isPublished() && compareDate.before(modDate)) {
			String title = item.getTitle();
			String author = item.getAuthor();
			String desc = translator.translate("notifications.entry", new String[] { title, author });
			String businessPath = p.getBusinessPath();
			String urlToSend = BusinessControlFactory.getInstance()
					.getURLFromBusinessPathString(businessPath);
			String iconCssClass = item.extraCSSClass();
			SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass);
			items.add(subListItem);
		}
	}

}
