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
package org.olat.modules.appointments.ui;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonErrorHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicsRunController extends BasicController implements Activateable2 {

	private static final String CMD_OPEN = "open";
	private static final String CMD_JOIN = "join";
	private static final String CMD_EMAIL = "email";
	private static final String CMD_RECORDING = "recording";

	private final VelocityContainer mainVC;

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private AppointmentListSelectionController topicRunCtrl;
	private OrganizerMailController mailCtrl;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final AppointmentsSecurityCallback secCallback;

	private List<TopicWrapper> topics;
	private int counter;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;
	

	public TopicsRunController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("topics_run");
		
		refresh();
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (topics.size() == 1
				&& (topics.get(0).getSelectedAppointments() == null
					|| topics.get(0).getSelectedAppointments().intValue() == 0)) {
			doOpenTopic(ureq, topics.get(0).getTopic());
		}
	}
	
	private void refresh() {
		mainVC.clear();
		topics = loadTopicWrappers();
		mainVC.contextPut("topics", topics);
	}

	private List<TopicWrapper> loadTopicWrappers() {
		List<Topic> topics = appointmentsService.getRestictedTopic(entry, subIdent, getIdentity());
		
		ParticipationSearchParams myParticipationsParams = new ParticipationSearchParams();
		myParticipationsParams.setEntry(entry);
		myParticipationsParams.setSubIdent(subIdent);
		myParticipationsParams.setIdentity(getIdentity());
		myParticipationsParams.setFetchAppointments(true);
		myParticipationsParams.setFetchTopics(true);
		List<Participation> participations = appointmentsService.getParticipations(myParticipationsParams);
		Map<Long, List<Participation>> topicKeyToMyEnrollmentParticipation = participations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getTopic().getKey()));
		
		// Add topics with participations even if participant has no access anymore.
		participations.stream()
				.map(p -> p.getAppointment().getTopic())
				.distinct()
				.filter(topic -> !topics.contains(topic))
				.forEach(topic -> topics.add(topic));
		
		List<Topic> topicsFinding = topics.stream()
				.filter(topic -> Type.finding == topic.getType())
				.collect(Collectors.toList());
		
		AppointmentSearchParams confirmedFindingsParams = new AppointmentSearchParams();
		confirmedFindingsParams.setTopics(topicsFinding);
		confirmedFindingsParams.setStatus(Status.confirmed);
		Set<Long> findingConfirmedKeys = appointmentsService
				.getAppointments(confirmedFindingsParams).stream()
				.map(a -> a.getTopic().getKey())
				.collect(Collectors.toSet());
		
		AppointmentSearchParams freeAppointmentsParams = new AppointmentSearchParams();
		freeAppointmentsParams.setTopics(topics);
		Map<Long, Long> topicKeyToAppointmentCount = appointmentsService.getTopicKeyToAppointmentCount(freeAppointmentsParams, true);
		
		Map<Long, List<Organizer>> topicKeyToOrganizer = appointmentsService
				.getOrganizers(entry, subIdent).stream()
				.collect(Collectors.groupingBy(o -> o.getTopic().getKey()));

		topics.sort((t1, t2) -> t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase()));
		List<TopicWrapper> wrappers = new ArrayList<>(topics.size());
		for (Topic topic : topics) {
			TopicWrapper wrapper = new TopicWrapper(topic);
			List<Organizer> organizers = topicKeyToOrganizer.getOrDefault(topic.getKey(), emptyList());
			wrapOrganizers(wrapper, organizers);
			wrapAppointment(wrapper, topicKeyToAppointmentCount, findingConfirmedKeys, topicKeyToMyEnrollmentParticipation);
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
		organizerNames.sort(String.CASE_INSENSITIVE_ORDER);
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

	private void wrapAppointment(TopicWrapper wrapper, Map<Long, Long> topicKeyToAppointmentCount,
			Set<Long> findingConfirmedKeys,
			Map<Long, List<Participation>> topicKeyToMyEnrollmentParticipation) {
		
		Topic topic = wrapper.getTopic();
		Long freeAppointments = topicKeyToAppointmentCount.getOrDefault(topic.getKey(), Long.valueOf(0));
		wrapper.setFreeAppointments(freeAppointments);
		
		List<Participation> myTopicParticipations = topicKeyToMyEnrollmentParticipation.getOrDefault(topic.getKey(), emptyList());
		wrapper.setSelectedAppointments(Integer.valueOf(myTopicParticipations.size()));
		
		if (Type.finding == topic.getType()) {
			wrapFindindAppointment(wrapper, myTopicParticipations);
		} else {
			wrapEnrollmentAppointment(wrapper, myTopicParticipations);
		}
		
		if (topic.isMultiParticipation()) {
			wrapOpenLink(wrapper, topic, "appointments.select");
		} else {
			wrapOpenLink(wrapper, topic, "appointment.select");
		}
		
		wrapMessage(wrapper, findingConfirmedKeys);
	}

	private void wrapFindindAppointment(TopicWrapper wrapper, List<Participation> myTopicParticipations) {
		Optional<Appointment> firstAppointment = myTopicParticipations.stream()
				.map(Participation::getAppointment)
				.filter(a -> a.getStatus() == Status.confirmed)
				.findFirst();
		if (firstAppointment.isPresent()) {
			Appointment appointment = firstAppointment.get();
			
			ParticipationSearchParams allParticipationParams = new ParticipationSearchParams();
			allParticipationParams.setAppointment(appointment);
			List<Participation> appointmentParticipations = appointmentsService.getParticipations(allParticipationParams);
			
			wrapAppointmentView(wrapper, appointment, appointmentParticipations);
		}
	}

	private void wrapEnrollmentAppointment(TopicWrapper wrapper, List<Participation> myTopicParticipations) {
		if (!myTopicParticipations.isEmpty()) {
			Date now = new Date();
			Optional<Appointment> nextAppointment = myTopicParticipations.stream()
					.map(Participation::getAppointment)
					.filter(a -> appointmentsService.isEndAfter(a, now))
					.sorted((a1, a2) -> a1.getStart().compareTo(a2.getStart()))
					.findFirst();
			Appointment appointment = nextAppointment.isPresent()
					? nextAppointment.get() // Next appointment ...
					: myTopicParticipations.stream()
						.map(Participation::getAppointment)
						.sorted((a1, a2) -> a2.getStart().compareTo(a1.getStart()))
						.findFirst().get(); // ... or the most recent one.
			wrapper.setFuture(Boolean.valueOf(appointment.getStart().after(now)));
			
			ParticipationSearchParams allParticipationParams = new ParticipationSearchParams();
			allParticipationParams.setAppointment(appointment);
			List<Participation> appointmentParticipations = appointmentsService.getParticipations(allParticipationParams);
			
			wrapAppointmentView(wrapper, appointment, appointmentParticipations);
		
			if (wrapper.getTopic().isParticipationVisible()) {
				List<String> participants = appointmentParticipations.stream()
						.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
						.sorted(String.CASE_INSENSITIVE_ORDER)
						.collect(Collectors.toList());
				wrapper.setParticipants(participants);
				
			}
		}
	}

	private void wrapAppointmentView(TopicWrapper wrapper, Appointment appointment, List<Participation> appointmentParticipations) {
		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String date2 = null;
		String time = null;
		
		boolean sameDay = DateUtils.isSameDay(begin, end);
		boolean sameTime = DateUtils.isSameTime(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder timeSb = new StringBuilder();
			if (sameTime) {
				timeSb.append(translate("full.day"));
			} else {
				timeSb.append(startTime);
				timeSb.append(" - ");
				timeSb.append(endTime);
			}
			time = timeSb.toString();
		} else {
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			date = dateSbShort1.toString();
			StringBuilder dateSb2 = new StringBuilder();
			dateSb2.append(endDate);
			dateSb2.append(" ");
			dateSb2.append(endTime);
			date2 = dateSb2.toString();
		}
		
		wrapper.setDate(date);
		wrapper.setDate2(date2);
		wrapper.setTime(time);
		wrapper.setLocation(AppointmentsUIFactory.getDisplayLocation(getTranslator(), appointment));
		wrapper.setDetails(appointment.getDetails());
		
		String dayName = "day_" + counter++;
		DateComponentFactory.createDateComponentWithYear(dayName, appointment.getStart(), mainVC);
		wrapper.setDayName(dayName);
		
		wrapper.setStatus(appointment.getStatus());
		wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
		wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		
		if (appointmentsService.isBigBlueButtonEnabled()) {
			if (secCallback.canJoinMeeting(appointment.getMeeting(), wrapper.getOrganizers(), appointmentParticipations)) {
				wrapMeeting(wrapper, appointment);
			}
			if (secCallback.canWatchRecording(wrapper.getOrganizers(), appointmentParticipations)) {
				List<BigBlueButtonRecordingReference> recordingReferences = appointmentsService
						.getRecordingReferences(Collections.singletonList(appointment))
						.getOrDefault(appointment.getKey(), Collections.emptyList());
				wrapRecordings(wrapper, recordingReferences);
			}
		}
	}

	private void wrapMeeting(TopicWrapper wrapper, Appointment appointment) {
		BigBlueButtonMeeting meeting = appointment.getMeeting();
		boolean disabled = isDisabled(meeting);
		if (disabled) {
			wrapper.setServerWarning(translate("error.serverDisabled"));
		}
		
		Link joinButton = LinkFactory.createCustomLink("join" + counter++, CMD_JOIN, "meeting.join.button", Link.BUTTON_LARGE, mainVC, this);
		joinButton.setTarget("_blank");
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		joinButton.setEnabled(!disabled);
		joinButton.setPrimary(joinButton.isEnabled());
		joinButton.setUserObject(appointment);
		wrapper.setJoinLinkName(joinButton.getComponentName());
	}
	
	private void wrapRecordings(TopicWrapper wrapper, List<BigBlueButtonRecordingReference> recordingReferences) {
		recordingReferences.sort((r1, r2) -> r1.getStartDate().compareTo(r2.getStartDate()));
		List<String> recordingLinkNames = new ArrayList<>(recordingReferences.size());
		for (int i = 0; i < recordingReferences.size(); i++) {
			BigBlueButtonRecordingReference recording = recordingReferences.get(i);
			Link link = LinkFactory.createCustomLink("rec_" + counter++, CMD_RECORDING, null, Link.NONTRANSLATED, mainVC, this);
			String name = translate("recording");
			if (recordingReferences.size() > 1) {
				name = name + " " + (i+1);
			}
			name = name + "  ";
			link.setCustomDisplayText(name);
			link.setIconLeftCSS("o_icon o_icon_lg o_vc_icon");
			link.setNewWindow(true, true);
			link.setUserObject(recording);
			recordingLinkNames.add(link.getComponentName());
		}
		wrapper.setRecordingLinkNames(recordingLinkNames);
	}

	private boolean isDisabled(BigBlueButtonMeeting meeting) {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void wrapOpenLink(TopicWrapper wrapper, TopicRef topic, String i18n) {
		Link openLink = LinkFactory.createCustomLink("open" + counter++, CMD_OPEN, i18n, Link.LINK, mainVC, this);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setUserObject(topic);
		wrapper.setOpenLinkName(openLink.getComponentName());
	}
	
	private void wrapMessage(TopicWrapper wrapper, Set<Long> findingConfirmedKeys) {
		Topic topic = wrapper.getTopic();
		int selectedAppointments = wrapper.getSelectedAppointments() != null
				? wrapper.getSelectedAppointments().intValue()
				: 0;
		Long freeAppointments = wrapper.getFreeAppointments();
		Status status = wrapper.getStatus();
		
		List<String> messages = new ArrayList<>(2);
		
		if (selectedAppointments == 0) {
			if (Type.finding != topic.getType()) {
				if (freeAppointments != null) {
					if (freeAppointments.longValue() == 1) {
						messages.add(translate("appointments.free.one"));
					} else if (freeAppointments.longValue() > 1) {
						messages.add(translate("appointments.free", new String[] { freeAppointments.toString() }));
					}
				}
			}
			
			if (Type.finding == topic.getType() && findingConfirmedKeys.contains(topic.getKey())) {
				messages.add(translate("appointments.finding.confirmed"));
			} else if (freeAppointments != null && freeAppointments.longValue() == 0) {
				messages.add(translate("appointments.free.no"));
			} else if (topic.isMultiParticipation()) {
				messages.add(translate("appointments.select.multi.message"));
			} else {
				messages.add(translate("appointments.select.one.message"));
			}
		} 
		
		if (selectedAppointments > 0) {
			if (Type.finding == topic.getType()) {
				if (status == null) {
					messages.add(translate("appointments.selected.not.confirmed"));
				}
			} else {
				if (topic.isMultiParticipation()) {
					if (selectedAppointments > 1) {
						messages.add(translate("appointments.selected", new String[] { String.valueOf(selectedAppointments) }));
					}
				}
			}
		}
		
		String message = messages.isEmpty()? null: messages.stream().collect(Collectors.joining("<br>"));
		wrapper.setMessage(message);
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
			} else if (CMD_EMAIL.equals(cmd)) {
				TopicWrapper wrapper = (TopicWrapper)link.getUserObject();
				doOrganizerEmail(ureq, wrapper.getTopic(), wrapper.getOrganizers());
			} else if (CMD_JOIN.equals(cmd)) {
				Appointment appointment = (Appointment)link.getUserObject();
				doJoin(ureq, appointment);
			} else if (CMD_RECORDING.equals(cmd)) {
				BigBlueButtonRecordingReference recordingReference = (BigBlueButtonRecordingReference)link.getUserObject();
				doOpenRecording(ureq, recordingReference);
			}
		}
	}

	private void doOpenTopic(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(topicRunCtrl);
		
		topicRunCtrl = new AppointmentListSelectionController(ureq, getWindowControl(), topic, secCallback);
		listenTo(topicRunCtrl);
		
		String title = topic.getTitle();
		String panelTitle = title.length() > 50? title.substring(0, 50) + "...": title;
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

	private void doJoin(UserRequest ureq, Appointment appointment) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		params.setFetchTopic(true);
		params.setFetchMeetings(true);
		List<Appointment> appointments = appointmentsService.getAppointments(params);
		Appointment reloadedAppointment = null;
		if (!appointments.isEmpty()) {
			reloadedAppointment = appointments.get(0);
		}
		
		if (reloadedAppointment == null || reloadedAppointment.getMeeting() == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
			return;
		}
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		String meetingUrl = appointmentsService.joinMeeting(reloadedAppointment, getIdentity(), errors);
		redirectTo(ureq, meetingUrl, errors);
	}
	
	private void redirectTo(UserRequest ureq, String meetingUrl, BigBlueButtonErrors errors) {
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else if(StringHelper.containsNonWhitespace(meetingUrl)) {
			MediaResource redirect = new RedirectMediaResource(meetingUrl);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} else {
			showWarning("warning.no.access");
		}
	}

	private void doOpenRecording(UserRequest ureq, BigBlueButtonRecordingReference recordingReference) {
		String url = appointmentsService.getRecordingUrl(ureq.getUserSession(), recordingReference);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
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
		private Boolean future;
		private String dayName;
		private String date;
		private String date2;
		private String time;
		private String location;
		private String details;
		private Appointment.Status status;
		private String translatedStatus;
		private String statusCSS;
		private String message;
		private Long freeAppointments;
		private Integer selectedAppointments;
		private String openLinkName;
		private String joinLinkName;
		private String serverWarning;
		private List<String> recordingLinkNames;

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

		public Boolean getFuture() {
			return future;
		}

		public void setFuture(Boolean future) {
			this.future = future;
		}
		
		public String getDayName() {
			return dayName;
		}

		public void setDayName(String dayName) {
			this.dayName = dayName;
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

		public Appointment.Status getStatus() {
			return status;
		}

		public void setStatus(Appointment.Status status) {
			this.status = status;
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

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
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

		public String getJoinLinkName() {
			return joinLinkName;
		}

		public void setJoinLinkName(String joinLinkName) {
			this.joinLinkName = joinLinkName;
		}

		public String getServerWarning() {
			return serverWarning;
		}

		public void setServerWarning(String serverWarning) {
			this.serverWarning = serverWarning;
		}

		public List<String> getRecordingLinkNames() {
			return recordingLinkNames;
		}

		public void setRecordingLinkNames(List<String> recordingLinkNames) {
			this.recordingLinkNames = recordingLinkNames;
		}
		
	}

}
