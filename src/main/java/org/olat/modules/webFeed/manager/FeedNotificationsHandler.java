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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.feed.blog.BlogToolController;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 6 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class FeedNotificationsHandler implements NotificationsHandler {

	private static final Logger log = Tracing.createLoggerFor(FeedNotificationsHandler.class);

	private static final String NOTIFICATIONS_HEADER_COURSE = "notifications.header.course";
	protected static final String NOTIFICATIONS_HEADER = "notifications.header";

	@Autowired
	private FeedManager feedManager;
	@Autowired
	private RepositoryManager repoManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		try {
		 	final Translator translator = Util.createPackageTranslator(FeedMainController.class, locale);
		 	if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {

				RepositoryEntry re = repoManager.lookupRepositoryEntry(
						OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
				if (re.getEntryStatus().decommissioned()) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				String title;
				String displayName = re.getDisplayname();
				if("CourseModule".equals(p.getResName())) {
					ICourse course = CourseFactory.loadCourse(re);
					CourseNode node = course.getRunStructure().getNode(p.getSubidentifier());
					if(node == null && !BlogToolController.SUBSCRIPTION_SUBIDENTIFIER.equals(p.getSubidentifier())) {
						notificationsManager.deactivate(p);
						return notificationsManager.getNoSubscriptionInfo();
					}
					if (!course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed()) {
						return notificationsManager.getNoSubscriptionInfo();
					}
					title = translator.translate(NOTIFICATIONS_HEADER_COURSE,  new String[]{displayName});
				} else {
					title = getHeader(translator, displayName);
				}

				OLATResourceable feedOres = OresHelper.createOLATResourceableInstance(p.getType(), Long.valueOf(p.getData()));
				Feed feed = feedManager.loadFeed(feedOres);
				List<Item> listItems = feedManager.loadItems(feed);
				List<SubscriptionListItem> items = new ArrayList<>();
				for (Item item : listItems) {
					if (!item.isDraft()) {
						appendSubscriptionItem(item, p, compareDate, translator, items);
					}
				}
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(),	new TitleItem(title, getCssClassIcon()), items);
		 	} else {
				//no news
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Unknown Exception", e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private void appendSubscriptionItem(Item item, Publisher p, Date compareDate, Translator translator, List<SubscriptionListItem> items) {
		String title = item.getTitle();
		String author = item.getAuthor();
		String businessPath = p.getBusinessPath();
		String urlToSend = BusinessControlFactory.getInstance()
					.getURLFromBusinessPathString(businessPath);
		String iconCssClass = item.extraCSSClass();
		if(!StringHelper.containsNonWhitespace(iconCssClass)) {
			iconCssClass = getCssClassIcon();
		}
		
		Date publishDate = item.getPublishDate();
		if(item.isPublished()) {
			if(compareDate.before(publishDate)) {
				String desc = translator.translate("notifications.entry.published", new String[] { title, author });
				items.add(new SubscriptionListItem(desc, urlToSend, businessPath, publishDate, iconCssClass));
			}

			// Internal items are modified when the modifier key is present.
			// External items are modified when the creation date is unequal the
			// last modified date and the published date is after the last
			// modified date.
			if (item.getModifierKey() != null
					|| (item.getFeed().isExternal() && !item.getCreationDate().equals(item.getLastModified()) && item.getPublishDate().before(item.getLastModified()))) {
				Date modDate = item.getLastModified();
				if(compareDate.before(modDate)) {
					String desc;
					String modifier = item.getModifier();
					if(StringHelper.containsNonWhitespace(modifier)) {
						desc = translator.translate("notifications.entry.modified", new String[] { title, modifier });
					} else {
						desc = translator.translate("notifications.entry.modified", new String[] { title, "???" });
					}
					items.add(new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass));
				}
			}

			List<UserComment> comments = commentAndRatingService.getComments(item.getFeed(), item.getGuid());
			for (UserComment comment : comments) {
				if (compareDate.before(comment.getCreationDate())) {
					String desc;
					String modifier = UserManager.getInstance().getUserDisplayName(comment.getCreator().getKey());
					if(StringHelper.containsNonWhitespace(modifier)) {
						desc = translator.translate("notifications.entry.commented", new String[] { title, modifier });
					} else {
						desc = translator.translate("notifications.entry.commented", new String[] { title, "???" });
					}
					items.add(new SubscriptionListItem(desc, urlToSend, businessPath, comment.getCreationDate(), iconCssClass));
				}
			}
		}
	}

	protected abstract String getCssClassIcon();

	protected abstract String getHeader(Translator translator, String title);

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(FeedMainController.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}

	protected TitleItem getTitleItem(Publisher p, Translator translator) {
		String title;
		try {
			String displayName = repoManager.lookupDisplayNameByOLATResourceableId(p.getResId());
			title = getHeader(translator,  displayName);
		} catch (Exception e) {
			log.error("", e);
			checkPublisher(p);
			title = translator.translate(NOTIFICATIONS_HEADER);
		}
		return new TitleItem(title, CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
	}

	protected void checkPublisher(Publisher p) {
		try {
			RepositoryEntry entry = repoManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
			if (entry == null) {
				notificationsManager.deactivate(p);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}