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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
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
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
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
 * Initial date: 29 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicsRunCoachController extends BasicController {

	private static final String CMD_OPEN = "open";

	private final VelocityContainer mainVC;
	private Link createButton;

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private TopicEditController topicEditCtrl;
	private TopicRunCoachController topicRunCtrl;
	private ContextualSubscriptionController subscriptionCtrl;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final AppointmentsSecurityCallback secCallback;
	private final Configuration config;
	private int counter;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public TopicsRunCoachController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, AppointmentsSecurityCallback secCallback, Configuration config) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.secCallback = secCallback;
		this.config = config;
		
		mainVC = createVelocityContainer("topics_run_coach");
		
		PublisherData publisherData = appointmentsService.getPublisherData(entry, subIdent);
		SubscriptionContext subContext = appointmentsService.getSubscriptionContext(entry, subIdent);
		subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, publisherData, true);
		listenTo(subscriptionCtrl);
		mainVC.put("infoSubscription", subscriptionCtrl.getInitialComponent());
		
		refresh();
		putInitialPanel(mainVC);
	}
	
	private void refresh() {
		clearVC();
		
		if (secCallback.canCreateTopic()) {
			createButton = LinkFactory.createButton("add.topic", mainVC, this);
			createButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		
		List<TopicWrapper> topics = loadTopicWrappers();
		mainVC.contextPut("topics", topics);
	}
	
	private void clearVC() {
		List<String> componentNames = new ArrayList<>();
		for (Component component : mainVC.getComponents()) {
			if (!"infoSubscription".equals(component.getComponentName()) && 
					!"add.topic".equals(component.getComponentName())) {
				componentNames.add(component.getComponentName());
			}
		}
		for (String componentName : componentNames) {
			mainVC.remove(componentName);
		}
	}
	
	private List<TopicWrapper> loadTopicWrappers() {
		Map<Long, List<Organizer>> topicKeyToOrganizer = appointmentsService
				.getOrganizers(entry, subIdent).stream()
				.collect(Collectors.groupingBy(o -> o.getTopic().getKey()));
		List<Topic> topics = appointmentsService.getTopics(entry, subIdent);
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setEntry(entry);
		aParams.setSubIdent(subIdent);
		Map<Long, List<Appointment>> topicKeyToAppointments = appointmentsService
				.getAppointments(aParams).stream()
				.collect(Collectors.groupingBy(a -> a.getTopic().getKey()));
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setEntry(entry);
		pParams.setSubIdent(subIdent);
		pParams.setFetchAppointments(true);
		List<Participation> participations = appointmentsService.getParticipations(pParams);
		Map<Long, List<Participation>> topicKeyToParticipations = participations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getTopic().getKey()));
		Map<Long, List<Participation>> appointmentKeyToParticipations = participations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		List<TopicWrapper> wrappers = new ArrayList<>(topics.size());
		for (Topic topic : topics) {
			TopicWrapper wrapper = new TopicWrapper(topic);
			List<Organizer> organizers = topicKeyToOrganizer.getOrDefault(topic.getKey(), emptyList());
			if (secCallback.canViewAppointment(organizers)) {
				wrapOrganizers(wrapper, organizers);
				List<Appointment> appointments = topicKeyToAppointments.getOrDefault(topic.getKey(), emptyList());
				List<Participation> topicParticipations = topicKeyToParticipations.getOrDefault(topic.getKey(), emptyList());
				wrapParticpations(wrapper, topic, topicParticipations, appointments, appointmentKeyToParticipations);
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}

	private void wrapOrganizers(TopicWrapper wrapper, List<Organizer> organizers) {
		List<String> organizerNames = new ArrayList<>(organizers.size());
		for (Organizer organizer : organizers) {
			String name = userManager.getUserDisplayName(organizer.getIdentity().getKey());
			organizerNames.add(name);
		}
		wrapper.setOrganizers(organizerNames);
	}

	private void wrapParticpations(TopicWrapper wrapper, Topic topic, List<Participation> participations,
			List<Appointment> appointments, Map<Long, List<Participation>> appointmentKeyToParticipations) {
		Integer totalAppointments = Integer.valueOf(appointments.size());
		wrapper.setTotalAppointments(totalAppointments);
		
		Integer selectedParticipations = Integer.valueOf(participations.size());
		wrapper.setSelectedParticipations(selectedParticipations);
		
		long confirmableAppointmentsCount = appointments.stream()
				.filter(a -> isConfirmable(a, appointmentKeyToParticipations))
				.count();
		wrapper.setConfirmableAppointments(confirmableAppointmentsCount > 0? Long.valueOf(confirmableAppointmentsCount): null);
		
		Date now = new Date();
		Optional<Appointment> nextAppointment = appointments.stream()
				.filter(a -> Appointment.Status.confirmed == a.getStatus())
				.filter(a1 -> now.before(a1.getEnd()))
				.sorted((a1, a2) -> a1.getStart().compareTo(a2.getStart()))
				.findFirst();
		
		if (nextAppointment.isPresent()) {
			Appointment appointment = nextAppointment.get();
			
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
			
			List<String> participants = appointmentKeyToParticipations
					.getOrDefault(appointment.getKey(), Collections.emptyList()).stream()
					.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.collect(Collectors.toList());
			wrapper.setParticipants(participants);
		}
		
		wrapOpenLink(wrapper, topic, "appointments.open");
	}

	private boolean isConfirmable(Appointment appointment, Map<Long, List<Participation>> appointmentKeyToParticipations) {
		return Appointment.Status.planned == appointment.getStatus() 
					&& appointmentKeyToParticipations.containsKey(appointment.getKey())
				? true
				: false;
	}

	private void wrapOpenLink(TopicWrapper wrapper, Topic topic, String i18n) {
		Link openLink = LinkFactory.createCustomLink("open" + counter++, CMD_OPEN, i18n, Link.LINK, mainVC, this);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setUserObject(topic);
		wrapper.setOpenLinkName(openLink.getComponentName());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == topicEditCtrl) {
			if (event == Event.DONE_EVENT) {
				Topic topic = topicEditCtrl.getTopic();
				cmc.deactivate();
				cleanUp();
				doOpenTopic(ureq, topic);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == topicRunCtrl) {
			if (event == Event.DONE_EVENT) {
				refresh();
			}
			stackPanel.popUpToRootController(ureq);
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(topicEditCtrl);
		removeAsListenerAndDispose(topicRunCtrl);
		removeAsListenerAndDispose(cmc);
		topicEditCtrl = null;
		topicRunCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == createButton) {
			doAddTopic(ureq);
		} else if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if (CMD_OPEN.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doOpenTopic(ureq, topic);
			}
		}
	}
	
	private void doAddTopic(UserRequest ureq) {
		topicEditCtrl = new TopicEditController(ureq, getWindowControl(), secCallback, entry, subIdent);
		listenTo(topicEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", topicEditCtrl.getInitialComponent(), true,
				translate("add.topic.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenTopic(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(topicRunCtrl);
		
		topicRunCtrl = new TopicRunCoachController(ureq, getWindowControl(), topic, secCallback, config);
		listenTo(topicRunCtrl);
		
		String title = topic.getTitle();
		String panelTitle = title.length() > 50? title.substring(0, 50) + "...": title;;
		stackPanel.pushController(panelTitle, topicRunCtrl);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class TopicWrapper {

		private final Topic topic;
		private List<String> organizers;
		private Integer selectedParticipations;
		private Integer totalAppointments;
		private Long confirmableAppointments;
		
		//next appointment
		private List<String> participants;
		private String date;
		private String date2;
		private String time;
		private String location;
		private String details;
		
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
		
		public List<String> getOrganizers() {
			return organizers;
		}
		
		public void setOrganizers(List<String> organizers) {
			this.organizers = organizers;
		}

		public Integer getSelectedParticipations() {
			return selectedParticipations;
		}

		public void setSelectedParticipations(Integer selectedParticipations) {
			this.selectedParticipations = selectedParticipations;
		}

		public Integer getTotalAppointments() {
			return totalAppointments;
		}

		public void setTotalAppointments(Integer totalAppointments) {
			this.totalAppointments = totalAppointments;
		}

		public Long getConfirmableAppointments() {
			return confirmableAppointments;
		}

		public void setConfirmableAppointments(Long confirmableAppointments) {
			this.confirmableAppointments = confirmableAppointments;
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

		public String getOpenLinkName() {
			return openLinkName;
		}

		public void setOpenLinkName(String openLinkName) {
			this.openLinkName = openLinkName;
		}
		
	}
}
