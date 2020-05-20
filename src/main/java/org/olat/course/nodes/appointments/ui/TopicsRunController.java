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
package org.olat.course.nodes.appointments.ui;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicsRunController extends BasicController {

	private static final String CMD_OPEN = "open";
	private static final String CMD_EMAIL = "email";

	private final VelocityContainer mainVC;

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private TopicRunController topicRunCtrl;
	private OrganizerMailController mailCtrl;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final Configuration config;

	private int counter;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public TopicsRunController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, Configuration config) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.config = config;
		
		mainVC = createVelocityContainer("topics_run");
		
		refresh();
		putInitialPanel(mainVC);
	}
	
	private void refresh() {
		removeButtons();
		List<TopicWrapper> topics = loadTopicWrappers();
		mainVC.contextPut("topics", topics);
	}

	private void removeButtons() {
		List<String> componentNames = new ArrayList<>();
		for (Component component : mainVC.getComponents()) {
			if (!"infoSubscription".equals(component.getComponentName())) {
				componentNames.add(component.getComponentName());
			}
		}
		for (String componentName : componentNames) {
			mainVC.remove(componentName);
		}
	}

	private List<TopicWrapper> loadTopicWrappers() {
		List<Topic> topics = appointmentsService.getTopics(entry, subIdent);
		Map<Long, List<Organizer>> topicKeyToOrganizer = appointmentsService
				.getOrganizers(entry, subIdent).stream()
				.collect(Collectors.groupingBy(o -> o.getTopic().getKey()));
		
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setEntry(entry);
		aParams.setSubIdent(subIdent);
		Map<Long, List<Appointment>> topicKeyToAppointments = appointmentsService
				.getAppointments(aParams).stream()
				.collect(Collectors.groupingBy(a -> a.getTopic().getKey()));
		
		ParticipationSearchParams myParticipationsParams = new ParticipationSearchParams();
		myParticipationsParams.setEntry(entry);
		myParticipationsParams.setSubIdent(subIdent);
		myParticipationsParams.setIdentity(getIdentity());
		myParticipationsParams.setFetchAppointments(true);
		List<Participation> myParticipations = appointmentsService.getParticipations(myParticipationsParams);
		Map<Long, List<Participation>> topicKeyToMyParticipation = myParticipations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getTopic().getKey()));
		Map<Long, List<Participation>> appointmentsToMyParticipation = myParticipations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		Set<Long> myAppointmentKeys = appointmentsToMyParticipation.keySet();
		ParticipationSearchParams allParticipationParams = new ParticipationSearchParams();
		allParticipationParams.setAppointmentKeys(myAppointmentKeys);
		Map<Long, List<Participation>> appointmentKeyToAllParticipations = appointmentsService
				.getParticipations(allParticipationParams).stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		List<TopicWrapper> wrappers = new ArrayList<>(topics.size());
		for (Topic topic : topics) {
			TopicWrapper wrapper = new TopicWrapper(topic);
			List<Organizer> organizers = topicKeyToOrganizer.getOrDefault(topic.getKey(), emptyList());
			wrapOrganizers(wrapper, organizers);
			List<Appointment> appointments = topicKeyToAppointments.getOrDefault(topic.getKey(), emptyList());
			List<Participation> topicParticipations = topicKeyToMyParticipation.getOrDefault(topic.getKey(), emptyList());
			wrapParticpations(wrapper, topic, topicParticipations, appointments, appointmentsToMyParticipation,
					appointmentKeyToAllParticipations);
			wrappers.add(wrapper);
		}
		return wrappers;
	}

	private void wrapOrganizers(TopicWrapper wrapper, List<Organizer> organizers) {
		List<String> organizerNames = new ArrayList<>(organizers.size());
		for (Organizer organizer : organizers) {
			String name = userManager.getUserDisplayName(organizer.getIdentity().getKey());
			organizerNames.add(name);
		}
		wrapper.setOrganizerNames(organizerNames);
		wrapper.setOrganizers(organizers);
		if (!organizers.isEmpty()) {
			Link link = LinkFactory.createCustomLink("email" + counter++, CMD_EMAIL, null, Link.NONTRANSLATED, mainVC, this);
			link.setIconLeftCSS("o_icon o_icon_mail");
			link.setElementCssClass("o_mail");
			link.setUserObject(wrapper);
			wrapper.setEmailLinkName(link.getComponentName());
		}
	}

	private void wrapParticpations(TopicWrapper wrapper, Topic topic, List<Participation> participations,
			List<Appointment> appointments, Map<Long, List<Participation>> appointmentKeyToParticipation,
			Map<Long, List<Participation>> appointmentKeyToAllParticipations) {
		if (participations.size() == 1) {
			Appointment appointment = participations.get(0).getAppointment();
			
			Locale locale = getLocale();
			Date begin = appointment.getStart();
			Date end = appointment.getEnd();
			String date = null;
			String date2 = null;
			String time = null;
			
			boolean sameDay = DateUtils.isSameDay(begin, end);
			if (sameDay) {
				StringBuilder dateSb = new StringBuilder();
				dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
				date = dateSb.toString();
				StringBuilder timeSb = new StringBuilder();
				timeSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
				timeSb.append(" - ");
				timeSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
				time = timeSb.toString();
			} else {
				StringBuilder dateSb = new StringBuilder();
				dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
				dateSb.append(" - ");
				date = dateSb.toString();
				StringBuilder dateSb2 = new StringBuilder();
				dateSb2.append(StringHelper.formatLocaleDateFull(end.getTime(), locale));
				dateSb2.append(" ");
				dateSb2.append(StringHelper.formatLocaleTime(end.getTime(), locale));
				date2 = dateSb2.toString();
			}
			
			wrapper.setDate(date);
			wrapper.setDate2(date2);
			wrapper.setTime(time);
			wrapper.setLocation(appointment.getLocation());
			wrapper.setDetails(appointment.getDetails());
			
			wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
			
			List<String> participants = appointmentKeyToAllParticipations
					.getOrDefault(appointment.getKey(), Collections.emptyList()).stream()
					.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.collect(Collectors.toList());
			wrapper.setParticipants(participants);
			
			if (Appointment.Status.planned == appointment.getStatus()) {
				wrapOpenLink(wrapper, topic, "appointments.change");
			}
		} else if (participations.size() == 0) {
			long freeAppointments = appointments.stream()
					.filter(a -> Appointment.Status.planned == a.getStatus())
					.filter(a -> hasFreeParticipations(a, appointmentKeyToParticipation))
					.count();
			wrapper.setFreeAppointments(Long.valueOf(freeAppointments));
			if (freeAppointments > 0) {
				wrapOpenLink(wrapper, topic, "appointments.select");
			}
		} else {
			wrapper.setSelectedAppointments(Integer.valueOf(participations.size()));
			wrapOpenLink(wrapper, topic, "appointments.select");
		}
	}

	private void wrapOpenLink(TopicWrapper wrapper, Topic topic, String i18n) {
		Link openLink = LinkFactory.createCustomLink("open" + counter++, CMD_OPEN, i18n, Link.LINK, mainVC, this);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setUserObject(topic);
		wrapper.setOpenLinkName(openLink.getComponentName());
	}

	private boolean hasFreeParticipations(Appointment appointment, Map<Long, List<Participation>> appointmentKeyToParticipation) {
		if (appointment.getMaxParticipations() == null) return true;
		
		List<Participation> participations = appointmentKeyToParticipation.getOrDefault(appointment.getKey(), emptyList());
		return appointment.getMaxParticipations().intValue() > participations.size();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == topicRunCtrl) {
			if (event == Event.DONE_EVENT) {
				refresh();
			}
			stackPanel.popUpToRootController(ureq);
			cleanUp();
		} else if (source == mailCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(topicRunCtrl);
		removeAsListenerAndDispose(mailCtrl);
		removeAsListenerAndDispose(cmc);
		topicRunCtrl = null;
		mailCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if (CMD_OPEN.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doOpenTopic(ureq, topic);
			} else if(CMD_EMAIL.equals(CMD_EMAIL)) {
				TopicWrapper wrapper = (TopicWrapper)link.getUserObject();
				doOrganizerEmail(ureq, wrapper.getTopic(), wrapper.getOrganizers());
			} 
		}
	}

	private void doOpenTopic(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(topicRunCtrl);
		
		topicRunCtrl = new TopicRunController(ureq, getWindowControl(), topic, config);
		listenTo(topicRunCtrl);
		
		String title = topic.getTitle();
		String panelTitle = title.length() > 50? title.substring(0, 50) + "...": title;;
		stackPanel.pushController(panelTitle, topicRunCtrl);
	}

	private void doOrganizerEmail(UserRequest ureq, Topic topic, Collection<Organizer> organizers) {
		mailCtrl = new OrganizerMailController(ureq, getWindowControl(), topic, organizers);
		listenTo(mailCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mailCtrl.getInitialComponent(), true,
				translate("email.title"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class TopicWrapper {

		private final Topic topic;
		private Collection<Organizer> organizers;
		private List<String> organizerNames;
		private String emailLinkName;
		private List<String> participants;
		private String date;
		private String date2;
		private String time;
		private String location;
		private String details;
		private String translatedStatus;
		private String statusCSS;
		private Long freeAppointments;
		private Integer selectedAppointments;
		private String openLinkName;

		public TopicWrapper(Topic topic) {
			this.topic = topic;
		}

		public Topic getTopic() {
			return topic;
		}
		
		public String getTitle() {
			return topic.getTitle();
		}
		
		public String getDescription() {
			return topic.getDescription();
		}
		
		public Collection<Organizer> getOrganizers() {
			return organizers;
		}

		public void setOrganizers(Collection<Organizer> organizers) {
			this.organizers = organizers;
		}

		public List<String> getOrganizerNames() {
			return organizerNames;
		}
		
		public void setOrganizerNames(List<String> organizerNames) {
			this.organizerNames = organizerNames;
		}

		public String getEmailLinkName() {
			return emailLinkName;
		}

		public void setEmailLinkName(String emailLinkName) {
			this.emailLinkName = emailLinkName;
		}

		public List<String> getParticipants() {
			return participants;
		}

		public void setParticipants(List<String> participants) {
			this.participants = participants;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getDate2() {
			return date2;
		}

		public void setDate2(String date2) {
			this.date2 = date2;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public String getTranslatedStatus() {
			return translatedStatus;
		}

		public void setTranslatedStatus(String translatedStatus) {
			this.translatedStatus = translatedStatus;
		}

		public String getStatusCSS() {
			return statusCSS;
		}

		public void setStatusCSS(String statusCSS) {
			this.statusCSS = statusCSS;
		}

		public Long getFreeAppointments() {
			return freeAppointments;
		}

		public void setFreeAppointments(Long freeAppointments) {
			this.freeAppointments = freeAppointments;
		}

		public Integer getSelectedAppointments() {
			return selectedAppointments;
		}

		public void setSelectedAppointments(Integer selectedAppointments) {
			this.selectedAppointments = selectedAppointments;
		}

		public String getOpenLinkName() {
			return openLinkName;
		}

		public void setOpenLinkName(String openLinkName) {
			this.openLinkName = openLinkName;
		}
		
	}

}
