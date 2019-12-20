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
package org.olat.modules.portfolio.manager;

 import static org.apache.commons.lang.time.DateUtils.isSameDay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityNames;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioNotificationsHandler implements NotificationsHandler {

	public static final String TYPE_NAME = Binder.class.getSimpleName();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher publisher = subscriber.getPublisher();
		Binder binder = binderDao.loadByKey(publisher.getResId());
		if (isInkoveValid(binder, compareDate, publisher)) {
			BinderSecurityCallback secCallback = null;
			Identity identity = subscriber.getIdentity();
			if(binderDao.isMember(binder, identity, PortfolioRoles.owner.name())) {
				secCallback = BinderSecurityCallbackFactory.getCallbackForOwnedBinder(binder);
			} else {
				List<AccessRights> rights = portfolioService.getAccessRights(binder, identity);
				if(!rights.isEmpty()) {
					secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(binder, rights);
				}
			}
			
			if(secCallback != null) {
				si = new SubscriptionInfo(subscriber.getKey(), publisher.getType(), getTitleItemForBinder(binder), null);
				List<SubscriptionListItem> allItems = getAllItems(binder, secCallback, compareDate, locale);
				for (SubscriptionListItem item : allItems) {
					//only a type of icon
					SubscriptionListItem clonedItem = new SubscriptionListItem(item.getDescription(), item.getDescriptionTooltip(),
							item.getLink(), item.getBusinessPath(), item.getDate(), "o_ep_icon");
					si.addSubscriptionListItem(clonedItem);
				}
			}
		}

		if (si == null) {
			// no info, return empty
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private boolean isInkoveValid(Binder binder, Date compareDate, Publisher publisher) {
		return (binder != null && compareDate != null && notificationsManager.isPublisherValid(publisher));
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		TitleItem title = getTitleItemForPublisher(subscriber.getPublisher());
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return TYPE_NAME;
	}
	
	public List<SubscriptionListItem> getAllItems(Binder binder, BinderSecurityCallback secCallback, Date compareDate, Locale locale) {
		String rootBusinessPath = "[Binder:" + binder.getKey() + "]";
		if(binder.getOlatResource() != null) {
			RepositoryEntry re = repositoryService.loadByResourceKey(binder.getOlatResource().getKey());
			rootBusinessPath = "[RepositoryEntry:" + re.getKey() + "]";
		} else {
			rootBusinessPath = "[Binder:" + binder.getKey() + "]";
		}

		Translator translator = Util.createPackageTranslator(PortfolioHomeController.class, locale);

		List<SubscriptionListItem> items = new ArrayList<>();
		items.addAll(getCommentNotifications(binder, secCallback, compareDate, rootBusinessPath, translator));
		items.addAll(getPageNotifications(binder, secCallback, compareDate, rootBusinessPath, translator));
		items.addAll(getSectionNotifications(binder, secCallback, compareDate, rootBusinessPath, translator));
		items.addAll(getEvaluationNotifications(binder, secCallback, compareDate, rootBusinessPath, translator));
		Collections.sort(items, new PortfolioNotificationComparator());
		return items;
	}
	
	public List<SubscriptionListItem> getSectionNotifications(Binder binder, BinderSecurityCallback secCallback,
			Date compareDate, String rootBusinessPath, Translator translator) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select section")
		  .append(" from pfsection as section")
		  .append(" inner join fetch section.binder as binder")
		  .append(" where binder.key=:binderKey and section.lastModified>=:compareDate");
		
		List<Section> sections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Section.class)
				.setParameter("binderKey", binder.getKey())
				.setParameter("compareDate", compareDate)
				.getResultList();

		Set<Long> uniqueSectionKeys = new HashSet<>();
		Set<Long> uniqueCreateSectionKeys = new HashSet<>();

		List<SubscriptionListItem> items = new ArrayList<>(sections.size());
		for (Section section:sections) {
			//section
			Long sectionKey = section.getKey();
			String sectionTitle = section.getTitle();
			Date sectionCreationDate = section.getCreationDate();
			Date sectionLastModified = section.getLastModified();
			
			if(secCallback.canViewElement(section)) {
				if(isSameDay(sectionCreationDate, sectionLastModified)) {
					if(!uniqueCreateSectionKeys.contains(sectionKey)) {
						uniqueCreateSectionKeys.add(sectionKey);
						SubscriptionListItem item = sectionCreateItem(sectionKey, sectionTitle,
								sectionCreationDate, rootBusinessPath, translator);
						items.add(item);
					}
				} else if(!uniqueSectionKeys.contains(sectionKey)) {
					uniqueSectionKeys.add(sectionKey);
					SubscriptionListItem item = sectionModifiedItem(sectionKey, sectionTitle,
							sectionLastModified, rootBusinessPath, translator);
					items.add(item);
				}
			}
		}
		return items;
	}
	
	/**
	 * Query the changes in the binder from the section to the page part.
	 * 
	 * 
	 * @param binder
	 * @param compareDate
	 * @param rootBusinessPath
	 * @param translator
	 * @return
	 */
	public List<SubscriptionListItem> getPageNotifications(Binder binder, BinderSecurityCallback secCallback,
			Date compareDate, String rootBusinessPath, Translator translator) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select page,")
		  .append("  pagepart.lastModified as pagepartLastModified")
		  .append(" from pfpage as page")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch section.binder as binder")
		  .append(" left join pfpagepart as pagepart on (pagepart.body.key = page.body.key)")
		  .append(" where binder.key=:binderKey and (pagepart.lastModified>=:compareDate or page.lastModified>=:compareDate)");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("binderKey", binder.getKey())
				.setParameter("compareDate", compareDate)
				.getResultList();

		Map<Long,SubscriptionListItem> uniquePartKeys = new HashMap<>();
		Map<Long,SubscriptionListItem> uniqueCreatePageKeys = new HashMap<>();
		
		List<SubscriptionListItem> items = new ArrayList<>(objects.size());
		for (Object[] object : objects) {
			//page
			Page page = (Page)object[0];
			Long pageKey = page.getKey();
			String pageTitle = page.getTitle();
			Date pageCreationDate = page.getCreationDate();
			Date pageLastModified = page.getLastModified();
			//page part
			Date partLastModified = (Date)object[1];
			
			Section section = page.getSection();
			if(secCallback.canViewElement(page) && secCallback.canViewElement(section)) {
				// page created
				if(isSameDay(pageCreationDate, pageLastModified) && pageCreationDate.compareTo(compareDate) >= 0) {
					if(!uniqueCreatePageKeys.containsKey(pageKey)) {
						SubscriptionListItem item = pageCreateItem(pageKey, pageTitle,
								pageCreationDate, rootBusinessPath, translator);
						uniqueCreatePageKeys.put(pageKey, item);
					}
				} else {
					
					if(uniquePartKeys.containsKey(pageKey)) {
						SubscriptionListItem item = uniquePartKeys.get(pageKey);
						SubscriptionListItem potentitalItem = pageModifiedItem( pageKey, pageTitle,
									pageLastModified, partLastModified, rootBusinessPath, translator);
						if(item.getDate().before(potentitalItem.getDate())) {
							uniquePartKeys.put(pageKey, potentitalItem);
						}
					} else if(pageLastModified.compareTo(compareDate) >= 0
								|| (partLastModified != null && partLastModified.compareTo(compareDate) >= 0)) {
						SubscriptionListItem item = pageModifiedItem( pageKey, pageTitle,
								 pageLastModified, partLastModified, rootBusinessPath, translator);
						
						boolean overlapCreate = false;
						if(uniqueCreatePageKeys.containsKey(pageKey)) {
							SubscriptionListItem createItem = uniqueCreatePageKeys.get(pageKey);
							overlapCreate = isSameDay(item.getDate(), createItem.getDate());
						}
						if(!overlapCreate) {
							uniquePartKeys.put(pageKey, item);
						}
					}
				}
			}
		}
		
		items.addAll(uniquePartKeys.values());
		items.addAll(uniqueCreatePageKeys.values());
		return items;
	}
	
	public List<SubscriptionListItem> getEvaluationNotifications(Binder binder, BinderSecurityCallback secCallback,
			Date compareDate, String rootBusinessPath, Translator translator) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select page, evasession")
		  .append(" from pfpage as page")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch section.binder as binder")
		  .append(" left join evaluationformsession as evasession on (page.body.key = evasession.pageBody.key)")
		  .append(" where binder.key=:binderKey and evasession.status='done' and evasession.submissionDate>=:compareDate");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("binderKey", binder.getKey())
				.setParameter("compareDate", compareDate)
				.getResultList();
		
		List<SubscriptionListItem> items = new ArrayList<>(objects.size());
		for (Object[] object : objects) {
			//page
			Page page = (Page)object[0];
			Long pageKey = page.getKey();
			String pageTitle = page.getTitle();
			//session
			EvaluationFormSession evaluationSession = (EvaluationFormSession)object[1];
			Date submissionDate = evaluationSession.getSubmissionDate();
			Date firstSubmissionDate = evaluationSession.getFirstSubmissionDate();
			
			if(submissionDate != null && secCallback.canViewElement(page)) {
				if(submissionDate.compareTo(firstSubmissionDate) == 0) {
				
					SubscriptionListItem item = evaluationNewItem(pageKey, pageTitle,
							submissionDate, rootBusinessPath, translator);
					items.add(item);
				} else {
					SubscriptionListItem item = evaluationModifiedItem(pageKey, pageTitle,
							submissionDate, rootBusinessPath, translator);
					items.add(item);
				}
			}
		}
		return items;
	}
	
	private SubscriptionListItem sectionCreateItem(Long sectionKey, String sectionTitle,
			Date sectionCreationDate, String rootBusinessPath, Translator translator) {
		String title = translator.translate("notifications.new.section", new String[]{ sectionTitle });
		String bPath = rootBusinessPath + "[Section:" + sectionKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, sectionCreationDate, "o_icon_pf_section");
		item.setUserObject(sectionKey);
		return item;
	}
	
	private SubscriptionListItem sectionModifiedItem(Long sectionKey, String sectionTitle,
			Date sectionLastModified, String rootBusinessPath, Translator translator) {
		String title = translator.translate("notifications.modified.section", new String[]{ sectionTitle });

		String bPath = rootBusinessPath + "[Section:" + sectionKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, sectionLastModified, "o_icon_pf_section");
		item.setUserObject(sectionKey);
		return item;
	}
	
	private SubscriptionListItem pageCreateItem(Long pageKey, String pageTitle,
			Date pageCreationDate, String rootBusinessPath, Translator translator) {
		String title = translator.translate("notifications.new.page", new String[]{ pageTitle });
		String bPath = rootBusinessPath + "[Page:" + pageKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, pageCreationDate, "o_icon_pf_page");
		item.setUserObject(pageKey);
		return item;
	}

	private SubscriptionListItem pageModifiedItem(Long pageKey, String pageTitle,
			Date pageLastModified, Date partLastModified,
			String rootBusinessPath, Translator translator) {

		String title = translator.translate("notifications.modified.page", new String[]{ pageTitle });
		Date date;
		if(partLastModified != null && partLastModified.compareTo(pageLastModified) > 0) {
			date = partLastModified;
		} else {
			date = pageLastModified;
		}

		String bPath = rootBusinessPath + "[Page:" + pageKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, date, "o_icon_pf_page");
		item.setUserObject(pageKey);
		return item;
	}
	
	private SubscriptionListItem evaluationNewItem(Long pageKey, String pageTitle,
			Date pageCreationDate, String rootBusinessPath, Translator translator) {
		String title = translator.translate("notifications.new.evaluation", new String[]{ pageTitle });
		String bPath = rootBusinessPath + "[Page:" + pageKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, pageCreationDate, "o_icon_pf_page");
		item.setUserObject(pageKey);
		return item;
	}
	
	private SubscriptionListItem evaluationModifiedItem(Long pageKey, String pageTitle,
			Date pageCreationDate, String rootBusinessPath, Translator translator) {
		String title = translator.translate("notifications.modified.evaluation", new String[]{ pageTitle });
		String bPath = rootBusinessPath + "[Page:" + pageKey + "]";
		String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
		SubscriptionListItem item = new SubscriptionListItem(title, linkUrl, bPath, pageCreationDate, "o_icon_pf_page");
		item.setUserObject(pageKey);
		return item;
	}
	
	public List<SubscriptionListItem> getCommentNotifications(Binder binder, BinderSecurityCallback secCallback,
			Date compareDate, String rootBusinessPath, Translator translator) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select")
		  .append(" comment.id as commentId,")
		  .append(" comment.creationDate as commentDate,")
		  .append(" author.key as authorKey,")
		  .append(" author.name as authorName,")
		  .append(" authorUser.firstName as authorFirstName,")
		  .append(" authorUser.lastName as authorLastName, ")
		  .append(" page")
		  .append(" from usercomment as comment")
		  .append(" inner join comment.creator as author")
		  .append(" inner join author.user as authorUser")
		  .append(" inner join pfpage as page on (comment.resId=page.key and comment.resName='Page')")
		  .append(" inner join fetch pfsection as section on (section.key = page.section.key)")
		  .append(" inner join fetch pfbinder as binder on (binder.key=section.binder.key)")
		  .append(" where binder.key=:binderKey and comment.creationDate>=:compareDate");
	
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey())
			.setParameter("compareDate", compareDate)
			.getResultList();
		
		List<SubscriptionListItem> items = new ArrayList<>(objects.size());
		for (Object[] object : objects) {
			Long commentId = (Long)object[0];
			Date commentDate = (Date)object[1];
			NotificationIdentityNames author = getIdentityNames(object, 2);
			Page page = (Page)object[6];
			Long pageKey = page.getKey();
			String pageTitle = page.getTitle();
			
			if(secCallback.canViewElement(page)) {
				String bPath = rootBusinessPath + "[Page:" + pageKey + "][Comment:" + commentId + "]";
				String linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(bPath);
				String[] title = new String[] { pageTitle, userManager.getUserDisplayName(author) };
				SubscriptionListItem item = new SubscriptionListItem(translator.translate("notifications.new.comment", title), linkUrl, bPath, commentDate, "o_icon_comments");
				item.setUserObject(pageKey);
				items.add(item);
			}
		}
		return items;
	}
	
	private NotificationIdentityNames getIdentityNames(Object[] object, int startIndex) {
		Long key = (Long)object[startIndex++];
		String name = (String)object[startIndex++];
		String firstName = (String)object[startIndex++];
		String lastName = (String)object[startIndex++];
		return new NotificationIdentityNames(key, name, firstName, lastName);
	}
	
	/**
	 * returns a TitleItem instance for the given Publisher p If you already
	 * have a reference to the map, use
	 * <code>getTitleItemForMap(EPAbstractMap amap)</code>
	 * 
	 * @param p
	 * @return
	 */
	private TitleItem getTitleItemForPublisher(Publisher p) {
		Binder binder = binderDao.loadByKey(p.getResId());
		return getTitleItemForBinder(binder);
	}

	/**
	 * returns a TitleItem instance for the given AbstractMap
	 * 
	 * @param amap
	 * @return
	 */
	private TitleItem getTitleItemForBinder(Binder binder) {
		StringBuilder sb = new StringBuilder();
		if (binder != null) {
			sb.append(StringHelper.escapeHtml(binder.getTitle()));
			List<Identity> owners = binderDao.getMembers(binder, PortfolioRoles.owner.name());
					
			if(owners.size() > 0) {
				sb.append(" (");
				for(int i=0; i<owners.size(); i++) {
					if(i > 0) sb.append(", ");
					String fullname = userManager.getUserDisplayName(owners.get(0));
					sb.append(StringHelper.escapeHtml(fullname));
				}
				sb.append(")");
			}
		}
		return new TitleItem(sb.toString(), "o_icon_pf_binder");
	}
	
	public static class NotificationIdentityNames implements IdentityNames {
		
		private final Long key;
		private final String name;
		private final String firstName;
		private final String lastName;
		
		public NotificationIdentityNames(Long key, String name, String firstName, String lastName) {
			this.key = key;
			this.name = name;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getFirstName() {
			return firstName;
		}

		@Override
		public String getLastName() {
			return lastName;
		}
	}
	
	public static class PortfolioNotificationComparator implements Comparator<SubscriptionListItem> {
		
		@Override
		public int compare(SubscriptionListItem o1, SubscriptionListItem o2) {
			return -o1.getDate().compareTo(o2.getDate());
		}

	}
}
