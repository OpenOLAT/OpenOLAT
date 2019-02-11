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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.commons.calendar.notification;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Implementation for NotificationHandler of calendars.
 * 
 * For more information see JIRA ticket OLAT-3861.
 * 
 * <P>
 * Initial Date: 22.12.2008 <br>
 * 
 * @author bja
 */
@Service
public class CalendarNotificationHandler implements NotificationsHandler {

	private static final OLog log = Tracing.createLoggerFor(CalendarNotificationHandler.class);
	private static final String CSS_CLASS_CALENDAR_ICON = "o_calendar_icon";
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
			Long id = p.getResId();
			String type = p.getSubidentifier();

			try {
				Translator translator = Util.createPackageTranslator(CalendarModule.class, locale);
				
				String calType = null;
				String title = null;
				if (type.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
					RepositoryEntry re = repositoryManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance("CourseModule", id), false);
					if(re == null || re.getEntryStatus().decommissioned()) {
						return notificationsManager.getNoSubscriptionInfo();
					}
					String displayName = re.getDisplayname();
					calType = CalendarManager.TYPE_COURSE;
					title = translator.translate("cal.notifications.header.course", new String[]{displayName});
				} else if (type.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
					BusinessGroup group = businessGroupDao.load(id);
					calType = CalendarManager.TYPE_GROUP;
					if(group == null) {
						return notificationsManager.getNoSubscriptionInfo();
					}
					title = translator.translate("cal.notifications.header.group", new String[]{ group.getName() });
				}

				if (calType != null) {
					Formatter form = Formatter.getInstance(locale);
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(title, CSS_CLASS_CALENDAR_ICON), null);
					
					String bPath;
					if(StringHelper.containsNonWhitespace(p.getBusinessPath())) {
						bPath = p.getBusinessPath();
					} else if("CalendarManager.course".equals(p.getResName())) {
						try {
							OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.getCourseTypeName(), p.getResId());
							RepositoryEntry re = repositoryManager.lookupRepositoryEntry(ores, true);
							bPath = "[RepositoryEntry:" + re.getKey() + "]";//Fallback
						} catch (Exception e) {
							log.error("Error processing calendar notifications of publisher:" + p.getKey(), e);
							return notificationsManager.getNoSubscriptionInfo();
						}
					} else {
						//cannot make link without business path
						return notificationsManager.getNoSubscriptionInfo();
					}
	
					Kalendar cal = calendarManager.getCalendar(calType, id.toString());
					Collection<KalendarEvent> calEvents = cal.getEvents();
					for (KalendarEvent kalendarEvent : calEvents) {
						if (showEvent(compareDate, kalendarEvent)) {
							log.debug("found a KalendarEvent: " + kalendarEvent.getSubject() + " with time: " + kalendarEvent.getBegin()
									+ " modified before: " + compareDate.toString(), null);
							// found a modified event in this calendar
							Date modDate = null;
							if(kalendarEvent.getLastModified() > 0) {
								modDate = new Date(kalendarEvent.getLastModified());
							} else if(kalendarEvent.getCreated() > 0) {
								modDate = new Date(kalendarEvent.getCreated());
							} else if(kalendarEvent.getBegin() != null) {
								modDate = kalendarEvent.getBegin();
							}
							
							String subject = kalendarEvent.getSubject();
							String author = kalendarEvent.getCreatedBy();
							if(author == null) author = "";

							String location = "";
							if(StringHelper.containsNonWhitespace(kalendarEvent.getLocation())) {
								location = kalendarEvent.getLocation() == null ? "" : translator.translate("cal.notifications.location",
									new String[] { kalendarEvent.getLocation() });
							}
							String dateStr;
							if (kalendarEvent.isAllDayEvent()) {
								dateStr = form.formatDate(kalendarEvent.getBegin());
							} else {
								dateStr = form.formatDate(kalendarEvent.getBegin()) + " - " + form.formatDate(kalendarEvent.getEnd());
							}
							String desc = translator.translate("cal.notifications.entry", new String[] { subject, dateStr, location, author });
							String businessPath = bPath + "[path=" + kalendarEvent.getID() + ":0]";
							String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
							SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, CSS_CLASS_CALENDAR_ICON);
							si.addSubscriptionListItem(subListItem);
						}
					}
				}
			} catch (Exception e) {
				log.error("Unexpected exception", e);
				checkPublisher(p);
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} else {
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if(CalendarController.ACTION_CALENDAR_GROUP.equals(p.getSubidentifier())) {
				BusinessGroup bg = businessGroupDao.load(p.getResId());
				if(bg == null) {
					log.info("deactivating publisher with key; " + p.getKey(), null);
					notificationsManager.deactivate(p);
				}
			} else if (CalendarController.ACTION_CALENDAR_COURSE.equals(p.getSubidentifier())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; " + p.getKey(), null);
					notificationsManager.deactivate(p);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private boolean showEvent(Date compareDate, KalendarEvent kalendarEvent) {
		if(kalendarEvent.getLastModified() > 0) {
			return compareDate.getTime() < kalendarEvent.getLastModified();
		}
		if(kalendarEvent.getCreated() > 0) {
			return compareDate.getTime() < kalendarEvent.getCreated();
		}
		return false;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Translator translator = Util.createPackageTranslator(CalendarManager.class, locale);
			String title = null;
			Long id = subscriber.getPublisher().getResId();
			String type = subscriber.getPublisher().getSubidentifier();
			if (type.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
				String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(id);
				title = translator.translate("cal.notifications.header.course", new String[]{displayName});
			} else if (type.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
				BusinessGroup group = businessGroupDao.load(id);
				title = translator.translate("cal.notifications.header.group", new String[]{group.getName()});
			}
			return title;
		} catch (Exception e) {
			log.error("Error while creating calendar notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	@Override
	public String getType() {
		return "CalendarManager";
	}

}