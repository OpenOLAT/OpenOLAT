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
package org.olat.course.nodes.appointments.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.ui.AppointmentsRunController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AppointmentsNotificationsHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(AppointmentsNotificationsHandler.class);
	
	static final String TYPE = "appointments";

	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private UserManager userManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher publisher = subscriber.getPublisher();
		Date latestNews = publisher.getLatestNewsDate();
	
		SubscriptionInfo si;
		try {
			if (notificationsManager.isPublisherValid(publisher) && compareDate.before(latestNews)) {
				ICourse course = CourseFactory.loadCourse(publisher.getResId());
				if(!course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed()) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				String subIdent = publisher.getSubidentifier();
				CourseNode courseNode = course.getRunStructure().getNode(subIdent);
				if (courseNode == null) {
					return notificationsManager.getNoSubscriptionInfo();
				}
				
				Translator translator = Util.createPackageTranslator(AppointmentsRunController.class, locale);
				List<SubscriptionListItem> items = new ArrayList<>();
				
				RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				addNewParticipations(items, subscriber, publisher, compareDate, translator, entry, subIdent);
				
				if (items.isEmpty()) {
					si = notificationsManager.getNoSubscriptionInfo();
				} else {
					String title = translator.translate("notifications.header", new String[] { entry.getDisplayname() });
					TitleItem titleItem = new TitleItem(title, AppointmentsCourseNode.ICON_CSS);
					si = new SubscriptionInfo(subscriber.getKey(), publisher.getType(), titleItem, items);
				}
			} else {
				si = notificationsManager.getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Cannot create gtask notifications for subscriber: " + subscriber.getKey(), e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}

	private void addNewParticipations(List<SubscriptionListItem> items, Subscriber subscriber, Publisher publisher,
			Date compareDate, Translator translator, RepositoryEntry entry, String subIdent) {
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		params.setOrganizer(subscriber.getIdentity());
		params.setCreatedAfter(compareDate);
		params.setFetchAppointments(true);
		params.setFetchTopics(true);
		List<Participation> participations = appointmentsService.getParticipations(params);
		
		for (Participation participation : participations) {
			SubscriptionListItem item = createNewParticipationItem(publisher, translator, participation);
			if (item != null) {
				items.add(item);
			}
		}
	}

	private SubscriptionListItem createNewParticipationItem(Publisher publisher, Translator translator,
			Participation participation) {
		String topicTitle = participation.getAppointment().getTopic().getTitle();
		String desc = translator.translate("notification.new.participation",
				new String[] { 
						topicTitle,
						userManager.getUserDisplayName(participation.getIdentity().getKey()) 
					});
		
		String businessPath = publisher.getBusinessPath();
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		Date dateInfo = participation.getCreationDate();
		return new SubscriptionListItem(desc, url, businessPath, dateInfo, AppointmentsCourseNode.ICON_CSS);
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		String title;
		try {
			Long resId = subscriber.getPublisher().getResId();
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(resId);
			Translator trans = Util.createPackageTranslator(AppointmentsRunController.class, locale);
			title = trans.translate("notifications.title", new String[]{ displayName });
		} catch (Exception e) {
			log.error("Error while creating appointments notifications for subscriber: " + subscriber.getKey(), e);
			title = "-";
		}
		return title;
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
