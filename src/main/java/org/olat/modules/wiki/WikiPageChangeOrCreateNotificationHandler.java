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

package org.olat.modules.wiki;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.nodes.wiki.WikiEditController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.types.WikiResource;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.ForumNotificationsHandler;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * To inform users whether a page has been recently changed or created user can subscribe a wiki
 * a this class evaluates whether something new is available or not.
 * <P>
 * Initial Date: Jun 26, 2006 <br>
 * 
 * @author guido
 */
@Service
public class WikiPageChangeOrCreateNotificationHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(WikiPageChangeOrCreateNotificationHandler.class);

	private static final String CSS_CLASS_WIKI_PAGE_CHANGED_ICON = "o_wiki_icon";
	protected String businessControlString;
	
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, final Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();

		final Date latestNews = p.getLatestNewsDate();
		Long resId = p.getResId();
		SubscriptionInfo si;
		final boolean debug = log.isDebugEnabled();
		
		// there could be news for me, investigate deeper
		if(debug) log.debug("compareDate=" + compareDate + " ; latestNews=" + latestNews);
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				OLATResourceable ores = null;
				if (p.getResName().equals( CourseModule.getCourseTypeName() ) ) {
					// resId = CourseResourceableId           p.getSubidentifier() = wikiCourseNode.getIdent()
					ICourse course = CourseFactory.loadCourse(resId);
					if(!courseStatus(course)) {
						return notificationsManager.getNoSubscriptionInfo();
					}
					CourseEnvironment cenv = course.getCourseEnvironment();
					CourseNode courseNode = cenv.getRunStructure().getNode(p.getSubidentifier());
					if(courseNode == null){
						//OLAT-3356 because removing wikicoursenodes was not propagated to 
						// disable subcriptions, we may end up here with a NULL wikicoursenode
						// Best we can do here -> return noSubsInfo and clean up
						notificationsManager.deactivate(p);
						// return nothing available
						return notificationsManager.getNoSubscriptionInfo();
					}
					ModuleConfiguration config = ((WikiCourseNode)courseNode).getModuleConfiguration();
					RepositoryEntry re = WikiEditController.getWikiRepoReference(config, true);
					resId = re.getOlatResource().getResourceableId();
					if(debug)  log.debug("resId=" + resId);
					ores = OresHelper.createOLATResourceableInstance(WikiResource.TYPE_NAME, resId);
					businessControlString = p.getBusinessPath() + "[path=";
				} else {
					// resName = 'BusinessGroup' or 'FileResource.WIKI'
					if(debug) log.debug("p.getResName()=" + p.getResName());
					ores = OresHelper.createOLATResourceableInstance(p.getResName(), resId);
					businessControlString = p.getBusinessPath() + "[path=";
				}
				
				Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
				final List<WikiPage> pages = wiki.getPagesByDate();
				Translator translator = Util.createPackageTranslator(WikiPageChangeOrCreateNotificationHandler.class, locale);
				Translator forumTranslator = Util.createPackageTranslator(ForumNotificationsHandler.class, locale);
				
				TitleItem title = getTitleItem(p, translator);
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), title, null);
					
				for (Iterator<WikiPage> it = pages.listIterator(); it.hasNext();) {						
					WikiPage element = it.next();
					
					// do only show entries newer then the ones already seen
					Date modDate = new Date(element.getModificationTime());
					if(debug) log.debug("modDate=" + modDate + " ; compareDate=" + compareDate);
					if (modDate.after(compareDate)) {
						if((element.getPageName().startsWith("O_") || element.getPageName().startsWith(WikiPage.WIKI_MENU_PAGE))
								&& (element.getModifyAuthor() <= 0)) {
								//theses pages are created sometimes automatically. Check if this is the case
								continue;
						}

						//build Businesscontrol-Path						
						String businessPath = null;						
						String urlToSend = null;
						if(p.getBusinessPath() != null) {
							businessPath = businessControlString + element.getPageName() + "]";		
							urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						}
						
						// string[] gets filled into translation key by adding {0...n} to
						// the string
						Identity ident = BaseSecurityManager.getInstance().loadIdentityByKey(Long.valueOf(element.getModifyAuthor()));
						String desc = translator.translate("notifications.entry", new String[] { element.getPageName(), NotificationHelper.getFormatedName(ident)});							
						SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath,  modDate, CSS_CLASS_WIKI_PAGE_CHANGED_ICON);
						si.addSubscriptionListItem(subListItem);
					}
					
					long forumKey = element.getForumKey();
					List<Message> mInfos = forumManager.getNewMessageInfo(forumKey, compareDate);
					
					for (Message mInfo : mInfos) {
						String messageTitle = mInfo.getTitle();
						Identity creator = mInfo.getCreator();
						Identity modifier = mInfo.getModifier();
						Date messageModDate = mInfo.getLastModified();
						
						String name;
						if(modifier != null) {
							name = NotificationHelper.getFormatedName(modifier);
						} else {
							name = NotificationHelper.getFormatedName(creator);
						}
						final String descKey = "notifications.entry" + (mInfo.getCreationDate().equals(messageModDate) ? "" : ".modified");
						final String desc = forumTranslator.translate(descKey, new String[] { messageTitle, name });
						String urlToSend = null;
						String businessPath = null;
						if(p.getBusinessPath() != null) {
							businessPath = businessControlString  + element.getPageName() + "][message:" + mInfo.getKey().toString() + "]";
							urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						}
						
						SubscriptionListItem subListItem =
								new SubscriptionListItem(desc, urlToSend, businessPath, messageModDate, CSS_CLASS_WIKI_PAGE_CHANGED_ICON);
						si.addSubscriptionListItem(subListItem);
					}
				}
			} else {
				//no news
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating wiki's notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(p);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	private boolean courseStatus(ICourse course) {
		return course != null
				&& course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed();
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
			} else {
				if(!NotificationsUpgradeHelper.checkOLATResourceable(p)) {
					log.info("deactivating publisher with key; {}", p.getKey());
					notificationsManager.deactivate(p);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private TitleItem getTitleItem(Publisher p, Translator translator) {
		Long resId = p.getResId();
		String type = p.getResName();
		String title;
		if("BusinessGroup".equals(type)) {
			BusinessGroup bg = businessGroupService.loadBusinessGroup(resId);
			title = translator.translate("notifications.header.group", new String[]{bg.getName()});
		} else if (CourseModule.getCourseTypeName().equals(type)) {
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(resId);
			title = translator.translate("notifications.header.course", new String[]{displayName});
		} else {
			title = translator.translate("notifications.header");
		}

		return new TitleItem(title, Wiki.CSS_CLASS_WIKI_ICON);
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Translator translator = Util.createPackageTranslator(WikiPageChangeOrCreateNotificationHandler.class, locale);
			TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
			return title.getInfoContent("text/plain");
		} catch (Exception e) {
			log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	@Override
	public String getType() {
		return "WikiPage";
	}
}
