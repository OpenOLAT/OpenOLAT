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
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.model.notification.EPArtefactNotification;
import org.olat.portfolio.model.notification.EPCommentNotification;
import org.olat.portfolio.model.notification.EPNotification;
import org.olat.portfolio.model.notification.EPRatingNotification;
import org.olat.portfolio.model.notification.EPStructureElementNotification;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("epNotificationManager")
public class EPNotificationManager {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;

	public List<SubscriptionListItem> getPageSubscriptionListItem(Long mapKey, String rootBusinessPath, Date compareDate, Translator translator) {
		List<EPStructureElementNotification> notifications = getPageNotifications(mapKey, compareDate);
		List<SubscriptionListItem> items = new ArrayList<>();
		for (EPNotification notification : notifications) {
			SubscriptionListItem item = null;	
			String[] title = new String[] { StringHelper.escapeHtml(notification.getTitle()) };
			if ("page".equals(notification.getType())) {
				String bPath = rootBusinessPath + "[EPPage:" + notification.getPageKey() + "]";
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
				item = new SubscriptionListItem(translator.translate("li.newpage", title), linkUrl, bPath, notification.getCreationDate(), "o_ep_icon_page");
				item.setUserObject(notification.getPageKey());
			} else {
				String bPath = rootBusinessPath;
				if (notification.getPageKey() != null) {
					bPath = rootBusinessPath + "[EPPage:" + notification.getPageKey() + "]";
				}
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
				item = new SubscriptionListItem(translator.translate("li.newstruct", title), linkUrl, bPath, notification.getCreationDate(), "o_ep_icon_struct");
				item.setUserObject(notification.getPageKey());
			}
			if(item != null) {
				items.add(item);
			}
		}
		return items;
	}
	
	public List<SubscriptionListItem> getArtefactNotifications(List<Long> mapKey, String rootBusinessPath, Date compareDate, Translator translator) {
		List<EPArtefactNotification> links = getArtefactNotifications(mapKey, compareDate);
		List<SubscriptionListItem> items = new ArrayList<>();
		for (EPArtefactNotification link : links) {
			Long pageKey =  link.getPageKey();
			String targetTitle= link.getStructureTitle();
			String[] title = new String[] {
					StringHelper.escapeHtml(userManager.getUserDisplayName(link.getAuthor())),
					StringHelper.escapeHtml(link.getArtefactTitle()),
					StringHelper.escapeHtml(targetTitle)
			};

			String bPath = rootBusinessPath + "[EPPage:" + pageKey + "]";
			String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
			SubscriptionListItem item = new SubscriptionListItem(translator.translate("li.newartefact", title), linkUrl, bPath, link.getCreationDate(), "o_icon_eportfolio_link");
			item.setUserObject(pageKey);
			items.add(item);
		}
		return items;
	}
	
	public List<SubscriptionListItem> getRatingNotifications(List<Long> mapKey, String rootBusinessPath, Date compareDate, Translator translator) {
		List<EPRatingNotification> ratings = getRatingNotifications(mapKey, compareDate);
		List<SubscriptionListItem> items = new ArrayList<>();

		for (EPRatingNotification rating : ratings) {
			if(rating.getPageKey() == null) {
				String[] title = new String[] { rating.getMapTitle(), userManager.getUserDisplayName(rating.getAuthor()) };
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(rootBusinessPath);
				if (rating.getLastModified() != null) {
					// there is a modified date, also add this as a listitem
					items.add(new SubscriptionListItem(translator.translate("li.changerating", title), linkUrl, rootBusinessPath, rating.getLastModified(), "o_icon_rating_on"));
				}
				items.add(new SubscriptionListItem(translator.translate("li.newrating", title), linkUrl, rootBusinessPath, rating.getCreationDate(), "o_icon_rating_on"));
			} else {
				String bPath = rootBusinessPath + "[EPPage:" + rating.getPageKey() + "]";
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
				String[] title = new String[] { rating.getTitle(), userManager.getUserDisplayName(rating.getAuthor()) };
				if (rating.getLastModified() != null) {
					// there is a modified date, also add this as a listitem
					SubscriptionListItem item = new SubscriptionListItem(translator.translate("li.changerating", title ), linkUrl, bPath, rating.getLastModified(), "o_icon_rating_on");
					item.setUserObject(rating.getPageKey());
					items.add(item);
				}
				SubscriptionListItem item = new SubscriptionListItem(translator.translate("li.newrating", title), linkUrl, bPath, rating.getCreationDate(), "o_icon_rating_on");
				item.setUserObject(rating.getPageKey());
				items.add(item);
			}
		}
		return items;
	}
	
	public List<SubscriptionListItem> getCommentNotifications(List<Long> mapKey, String rootBusinessPath, Date compareDate, Translator translator) {
		List<EPCommentNotification> comments = getCommentNotifications(mapKey, compareDate);
		List<SubscriptionListItem> items = new ArrayList<>();

		for (EPCommentNotification comment : comments) {
			SubscriptionListItem item;
			if(comment.getPageKey() == null) {
				String[] title = new String[] { comment.getMapTitle(), userManager.getUserDisplayName(comment.getAuthor()) };
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(rootBusinessPath);
				item = new SubscriptionListItem(translator.translate("li.newcomment", title), linkUrl, rootBusinessPath, comment.getCreationDate(), "o_info_icon");
			} else {
				String bPath = rootBusinessPath + "[EPPage:" + comment.getPageKey() + "]";
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
				String[] title = new String[] { comment.getTitle(), userManager.getUserDisplayName(comment.getAuthor()) };
				item = new SubscriptionListItem(translator.translate("li.newcomment", title), linkUrl, bPath, comment.getCreationDate(), "o_info_icon");
				item.setUserObject(comment.getPageKey());
			}
			items.add(item);
		}

		return items;
	}
	
	private List<EPStructureElementNotification> getPageNotifications(Long mapKey, Date compareDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select notification from ").append(EPStructureElementNotification.class.getName()).append(" as notification");
		sb.append(" where notification.creationDate>=:currentDate and (notification.key=:mapKey or notification.rootMapKey=:mapKey)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EPStructureElementNotification.class)
				.setParameter("currentDate", compareDate, TemporalType.TIMESTAMP)
				.setParameter("mapKey", mapKey)
				.getResultList();
	}

	private List<EPArtefactNotification> getArtefactNotifications(List<Long> mapKey, Date compareDate) {
		if(mapKey == null || mapKey.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select notification from ").append(EPArtefactNotification.class.getName()).append(" as notification")
	    .append(" inner join fetch notification.author")
		  .append(" where notification.creationDate>=:currentDate and (notification.key in (:mapKey) or notification.rootMapKey in (:mapKey))");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EPArtefactNotification.class)
				.setParameter("currentDate", compareDate, TemporalType.TIMESTAMP)
				.setParameter("mapKey", mapKey)
				.getResultList();
	}
	
	private List<EPRatingNotification> getRatingNotifications(List<Long> mapKey, Date compareDate) {
		if(mapKey == null || mapKey.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select notification from ").append(EPRatingNotification.class.getName()).append(" as notification")
		  .append(" inner join fetch notification.author")
		  .append(" where notification.creationDate>=:currentDate and notification.mapKey in (:mapKey)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EPRatingNotification.class)
				.setParameter("currentDate", compareDate, TemporalType.TIMESTAMP)
				.setParameter("mapKey", mapKey)
				.getResultList();
	}

	private List<EPCommentNotification> getCommentNotifications(List<Long> mapKey, Date compareDate) {
		if(mapKey == null || mapKey.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select notification from ").append(EPCommentNotification.class.getName()).append(" as notification")
	    .append(" inner join fetch notification.author")
		  .append(" where notification.creationDate>=:currentDate and notification.mapKey in (:mapKey)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EPCommentNotification.class)
				.setParameter("currentDate", compareDate, TemporalType.TIMESTAMP)
				.setParameter("mapKey", mapKey)
				.getResultList();
	}

}
