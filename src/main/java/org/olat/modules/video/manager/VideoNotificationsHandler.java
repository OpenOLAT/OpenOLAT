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
package org.olat.modules.video.manager;

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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoNotificationsHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(VideoNotificationsHandler.class);
	
	private static final String NOTIFICATIONS_HEADER_COURSE = "notifications.header.course";
	private static final String NOTIFICATIONS_HEADER = "notifications.header";
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private CommentAndRatingService commentAndRatingService;
	
	@Override
	public String getType() {
		return VideoFileResource.TYPE_NAME;
	}

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		
		SubscriptionInfo si;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		try {
		 	final Translator translator = Util.createPackageTranslator(VideoDisplayController.class, locale);
		 	if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {

				RepositoryEntry re = repositoryManager.lookupRepositoryEntry(
						OresHelper.createOLATResourceableInstance(p.getResName(), p.getResId()), false);
				if (re.getEntryStatus().decommissioned()) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				String title;
				String commentTitle;
				String displayName = re.getDisplayname();
				if("CourseModule".equals(p.getResName())) {
					ICourse course = CourseFactory.loadCourse(re);
					CourseNode node = course.getRunStructure().getNode(p.getSubidentifier());
					if(node == null) {
						notificationsManager.deactivate(p);
						return notificationsManager.getNoSubscriptionInfo();
					}
					if (!course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed()) {
						return notificationsManager.getNoSubscriptionInfo();
					}
					
					commentTitle = node.getShortTitle();
					if(!StringHelper.containsNonWhitespace(commentTitle)) {
						commentTitle = node.getLongTitle();
					}
					re = ((VideoCourseNode)node).getReferencedRepositoryEntry();// comments are always linked to the video resource
					title = translator.translate(NOTIFICATIONS_HEADER_COURSE,  new String[]{displayName});
				} else {
					commentTitle = displayName;
					title = translator.translate(NOTIFICATIONS_HEADER,  new String[]{displayName});
				}
				
				String businessPath = p.getBusinessPath();
				String urlToSend = BusinessControlFactory.getInstance()
							.getURLFromBusinessPathString(businessPath);
				
				List<SubscriptionListItem> items = new ArrayList<>();
				List<UserComment> comments = commentAndRatingService.getComments(re.getOlatResource(), p.getSubidentifier());
				for (UserComment comment : comments) {
					if (compareDate.before(comment.getCreationDate())) {
						String desc;
						String modifier = userManager.getUserDisplayName(comment.getCreator().getKey());
						if(StringHelper.containsNonWhitespace(modifier)) {
							desc = translator.translate("notifications.video.commented", new String[] { commentTitle, modifier });
						} else {
							desc = translator.translate("notifications.video.commented", new String[] { commentTitle, "???" });
						}
						items.add(new SubscriptionListItem(desc, urlToSend, businessPath, comment.getCreationDate(), "o_video_icon"));
					}
				}

				si = new SubscriptionInfo(subscriber.getKey(), p.getType(),	new TitleItem(title, "o_video_icon"), items);
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

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(VideoDisplayController.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}
	
	protected TitleItem getTitleItem(Publisher p, Translator translator) {
		String title = "";
		try {
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(p.getResId());
			title = translator.translate(NOTIFICATIONS_HEADER, new String[] { displayName });
		} catch (Exception e) {
			log.error("", e);
		}
		return new TitleItem(title, "o_video_icon");
	}
}
