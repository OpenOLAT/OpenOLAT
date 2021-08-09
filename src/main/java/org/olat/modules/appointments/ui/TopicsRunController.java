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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.manager.AvatarMapper;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.ui.TeamsMeetingEvent;
import org.olat.modules.teams.ui.TeamsUIHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicsRunController extends FormBasicController implements Activateable2 {

	private static final long PARTICIPANTS_RENDER_LIMIT = 3;
	private static final String CMD_MORE = "more";
	private static final String CMD_OPEN = "open";
	private static final String CMD_JOIN = "join";
	private static final String CMD_EMAIL = "email";
	private static final String CMD_RECORDING = "recording";

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private AppointmentListSelectionController topicRunCtrl;
	private OrganizerMailController mailCtrl;
	
	private String avatarUrl;
	private final RepositoryEntry entry;
	private final String subIdent;
	private final AppointmentsSecurityCallback secCallback;

	private List<TopicWrapper> topics;
	private Set<Topic> acknowlededRecordings = new HashSet<>();
	private int counter;
	private final Set<Long> showAllParticipationsTopicKeys = new HashSet<>();
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;
	

	public TopicsRunController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, "topics_run");
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (topics.size() == 1
				&& (topics.get(0).getSelectedAppointments() == null
					|| topics.get(0).getSelectedAppointments().intValue() == 0)) {
			doOpenTopic(ureq, topics.get(0).getTopic());
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		topics = loadTopicWrappers();
		flc.contextPut("topics", topics);
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
		wrapper.setOrganizerNames(AppointmentsUIFactory.formatOrganizers(organizers));
		wrapper.setOrganizers(organizers);
		if (!organizers.isEmpty()) {
			FormLink link = uifactory.addFormLink("email" + counter++, CMD_EMAIL, "", null, flc, Link.NONTRANSLATED);
			link.setIconLeftCSS("o_icon o_icon_mail");
			link.setElementCssClass("o_mail");
			link.setUserObject(wrapper);
			wrapper.setEmailLinkName(link.getName());
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
				wrapParticipants(wrapper, appointmentParticipations);
			}
		}
	}

	private void wrapAppointmentView(TopicWrapper wrapper, Appointment appointment, List<Participation> appointmentParticipations) {
		wrapper.setAppointment(appointment);
		
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
		DateElement dateEl = DateComponentFactory.createDateElementWithYear(dayName, appointment.getStart());
		flc.add(dayName, dateEl);
		wrapper.setDayName(dayName);
		
		wrapper.setStatus(appointment.getStatus());
		wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
		wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		
		if (appointmentsService.isBigBlueButtonEnabled()) {
			if (secCallback.canJoinBBBMeeting(appointment, wrapper.getOrganizers(), appointmentParticipations)) {
				wrapBBBMeeting(wrapper, appointment);
			}
			if (secCallback.canWatchRecording(wrapper.getOrganizers(), appointmentParticipations)) {
				List<BigBlueButtonRecordingReference> recordingReferences = appointmentsService
						.getBBBRecordingReferences(Collections.singletonList(appointment))
						.getOrDefault(appointment.getKey(), Collections.emptyList());
				wrapRecordings(wrapper, recordingReferences);
			}
		}
		
		if (appointmentsService.isTeamsEnabled()
				&& secCallback.canJoinTeamsMeeting(appointment, wrapper.getOrganizers(), appointmentParticipations)) {
			wrapTeamsMeeting(wrapper);
		}
	}

	private void wrapBBBMeeting(TopicWrapper wrapper, Appointment appointment) {
		BigBlueButtonMeeting meeting = appointment.getBBBMeeting();
		boolean serverDisabled = isServerDisabled(meeting);
		if (serverDisabled) {
			wrapper.setServerWarning(translate("error.serverDisabled"));
		}
		boolean meetingOpen = secCallback.isBBBMeetingOpen(appointment, wrapper.getOrganizers());
		if (!serverDisabled && !meetingOpen) {
			wrapper.setMeetingWarning(translate("error.meeting.not.open"));
		}
		
		FormLink joinButton = uifactory.addFormLink("join" + counter++, CMD_JOIN, "meeting.join.button", null, flc, Link.BUTTON_LARGE);
		joinButton.setNewWindow(true, true, true);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		joinButton.setEnabled(!serverDisabled && meetingOpen);
		joinButton.setPrimary(joinButton.isEnabled());
		joinButton.setUserObject(wrapper);
		wrapper.setJoinLinkName(joinButton.getName());
		
		if (BigBlueButtonUIHelper.isRecord(meeting)) {
			KeyValues acknowledgeKeyValue = new KeyValues();
			acknowledgeKeyValue.add(KeyValues.entry("agree", translate("meeting.acknowledge.recording.agree")));
			MultipleSelectionElement acknowledgeRecordingEl = uifactory.addCheckboxesHorizontal("ack_" + counter++, null, flc,
					acknowledgeKeyValue.keys(), acknowledgeKeyValue.values());
			if (acknowlededRecordings.contains(wrapper.getTopic())) {
				acknowledgeRecordingEl.select(acknowledgeRecordingEl.getKey(0), true);
			}
			acknowledgeRecordingEl.addActionListener(FormEvent.ONCHANGE);
			acknowledgeRecordingEl.setUserObject(wrapper);
			wrapper.setAcknowledgeRecordingEl(acknowledgeRecordingEl);
		}
	}

	private void wrapRecordings(TopicWrapper wrapper, List<BigBlueButtonRecordingReference> recordingReferences) {
		recordingReferences.sort((r1, r2) -> r1.getStartDate().compareTo(r2.getStartDate()));
		List<String> recordingLinkNames = new ArrayList<>(recordingReferences.size());
		for (int i = 0; i < recordingReferences.size(); i++) {
			BigBlueButtonRecordingReference recording = recordingReferences.get(i);
			
			FormLink link = uifactory.addFormLink("rec_" + counter++, CMD_RECORDING, null, null, flc, Link.NONTRANSLATED);
			String name = translate("recording");
			if (recordingReferences.size() > 1) {
				name = name + " " + (i+1);
			}
			name = name + "  ";
			link.setLinkTitle(name);
			link.setIconLeftCSS("o_icon o_icon_lg o_vc_icon");
			link.setNewWindow(true, true, true);
			link.setUserObject(recording);
			recordingLinkNames.add(link.getName());
		}
		wrapper.setRecordingLinkNames(recordingLinkNames);
	}
	
	private void wrapTeamsMeeting(TopicWrapper wrapper) {
		boolean meetingOpen = secCallback.isTeamsMeetingOpen(wrapper.getAppointment(), wrapper.getOrganizers());
		if (!meetingOpen) {
			wrapper.setMeetingWarning(translate("error.meeting.not.open"));
		}
		
		FormLink joinButton = uifactory.addFormLink("join" + counter++, CMD_JOIN, "meeting.join.button", null, flc, Link.BUTTON_LARGE);
		joinButton.setNewWindow(true, true, true);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		joinButton.setEnabled(meetingOpen);
		joinButton.setPrimary(joinButton.isEnabled());
		joinButton.setUserObject(wrapper);
		wrapper.setJoinLinkName(joinButton.getName());
	}

	private boolean isServerDisabled(BigBlueButtonMeeting meeting) {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void wrapParticipants(TopicWrapper wrapper, List<Participation> participations) {
		long limit = showAllParticipationsTopicKeys.contains(wrapper.getTopic().getKey())? Long.MAX_VALUE: PARTICIPANTS_RENDER_LIMIT;
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.limit(limit)
				.collect(Collectors.toList());
		wrapper.setParticipants(participants);
		
		if (participations.size() > PARTICIPANTS_RENDER_LIMIT) {
			String name = "more_" + wrapper.getTopic().getKey();
			FormLink showMoreLink = uifactory.addFormLink(name, CMD_MORE, "", null, flc, Link.LINK + Link.NONTRANSLATED);
			
			long hiddenParticipations = participations.size() - PARTICIPANTS_RENDER_LIMIT;
			String displayText = showAllParticipationsTopicKeys.contains(wrapper.getTopic().getKey())
					? translate("show.less")
					: translate("show.more", new String[] { String.valueOf(hiddenParticipations)} );
			showMoreLink.setI18nKey(displayText);
			showMoreLink.setUserObject(wrapper.getTopic());
			wrapper.setShowMoreLinkName(showMoreLink.getName());
		}
	}
	
	private void wrapOpenLink(TopicWrapper wrapper, TopicRef topic, String i18n) {
		FormLink link = uifactory.addFormLink("open" + counter++, CMD_OPEN, i18n, null, flc,  Link.LINK);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setUserObject(topic);
		wrapper.setOpenLinkName(link.getName());
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
				initForm(ureq);
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_MORE.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doToggleShowMoreParticipations(ureq, topic);
			} else if (CMD_OPEN.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doOpenTopic(ureq, topic);
			} else if (CMD_EMAIL.equals(cmd)) {
				TopicWrapper wrapper = (TopicWrapper)link.getUserObject();
				doOrganizerEmail(ureq, wrapper.getTopic(), wrapper.getOrganizers());
			} else if (CMD_JOIN.equals(cmd)) {
				TopicWrapper wrapper = (TopicWrapper)link.getUserObject();
				doJoin(wrapper);
			} else if (CMD_RECORDING.equals(cmd)) {
				BigBlueButtonRecordingReference recordingReference = (BigBlueButtonRecordingReference)link.getUserObject();
				doOpenRecording(ureq, recordingReference);
			}
		} else if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement mse = (MultipleSelectionElement)source;
			Topic topic = ((TopicWrapper)mse.getUserObject()).getTopic();
			if (mse.isAtLeastSelected(1)) {
				acknowlededRecordings.add(topic);
			} else {
				acknowlededRecordings.remove(topic);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	private void doToggleShowMoreParticipations(UserRequest ureq, Topic topic) {
		if (showAllParticipationsTopicKeys.contains(topic.getKey())) {
			showAllParticipationsTopicKeys.remove(topic.getKey());
		} else {
			showAllParticipationsTopicKeys.add(topic.getKey());
		}
		initForm(ureq);
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

	private void doJoin(TopicWrapper wrapper) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(wrapper.getAppointment());
		params.setFetchTopic(true);
		params.setFetchMeetings(true);
		List<Appointment> appointments = appointmentsService.getAppointments(params);
		Appointment appointment = null;
		if (!appointments.isEmpty()) {
			appointment = appointments.get(0);
		}
		
		if (appointment == null || (appointment.getBBBMeeting() == null && appointment.getTeamsMeeting() == null)) {
			showWarning("warning.no.meeting");
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		}
		if (appointment.getBBBMeeting() != null) {
			doJoinBBBMeeting(wrapper, appointment);
		} else if (appointment.getTeamsMeeting() != null) {
			doJoinTeamsMeeting(appointment);
		}
	}

	private void doJoinBBBMeeting(TopicWrapper wrapper, Appointment appointment) {
		if (BigBlueButtonUIHelper.isRecord(appointment.getBBBMeeting()) && !acknowlededRecordings.contains(appointment.getTopic())) {
			wrapper.getAcknowledgeRecordingEl().setErrorKey("form.legende.mandatory", null);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		} else if (wrapper.getAcknowledgeRecordingEl() != null) {
			wrapper.getAcknowledgeRecordingEl().clearError();
		}
		
		if(avatarUrl == null && bigBlueButtonModule.isAvatarEnabled()) {
			File portraitFile = displayPortraitManager.getBigPortrait(getIdentity());
			if(portraitFile != null) {
				String rnd = "r" + getIdentity().getKey() + CodeHelper.getRAMUniqueID();
				avatarUrl = Settings.createServerURI()
						+ registerCacheableMapper(null, rnd, new AvatarMapper(portraitFile), 5 * 60 * 60)
						+ "/" + portraitFile.getName();
			}
		}
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		String meetingUrl = appointmentsService.joinBBBMeeting(appointment, getIdentity(), avatarUrl, errors);
		redirectTo(meetingUrl, errors);
	}
	
	private void redirectTo(String meetingUrl, BigBlueButtonErrors errors) {
		if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
		} else if(StringHelper.containsNonWhitespace(meetingUrl)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(meetingUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}

	private void doOpenRecording(UserRequest ureq, BigBlueButtonRecordingReference recordingReference) {
		String url = appointmentsService.getBBBRecordingUrl(ureq.getUserSession(), recordingReference);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
	}

	private void doJoinTeamsMeeting(Appointment appointment) {
		TeamsErrors errors = new TeamsErrors();
		TeamsMeeting meeting = appointmentsService.joinTeamsMeeting(appointment, getIdentity(), errors);
		
		if(meeting == null) {
			showWarning("warning.no.meeting");
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		} else if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			getWindowControl().setError(TeamsUIHelper.formatErrors(getTranslator(), errors));
			return;
		}
		
		String joinUrl = meeting.getOnlineMeetingJoinUrl();
		if(StringHelper.containsNonWhitespace(joinUrl)) {
			TeamsMeetingEvent event = new TeamsMeetingEvent(meeting.getKey(), getIdentity().getKey());
			OLATResourceable meetingOres = OresHelper.createOLATResourceableInstance(TeamsMeeting.class.getSimpleName(), meeting.getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, meetingOres);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(joinUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public static final class TopicWrapper {

		private final Topic topic;
		private Collection<Organizer> organizers;
		private String organizerNames;
		private String emailLinkName;
		private Appointment appointment;
		private List<String> participants;
		private String showMoreLinkName;
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
		private MultipleSelectionElement acknowledgeRecordingEl;
		private String openLinkName;
		private String joinLinkName;
		private String serverWarning;
		private String meetingWarning;
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

		public String getOrganizerNames() {
			return organizerNames;
		}
		
		public void setOrganizerNames(String organizerNames) {
			this.organizerNames = organizerNames;
		}

		public String getEmailLinkName() {
			return emailLinkName;
		}

		public void setEmailLinkName(String emailLinkName) {
			this.emailLinkName = emailLinkName;
		}

		public Appointment getAppointment() {
			return appointment;
		}

		public void setAppointment(Appointment appointment) {
			this.appointment = appointment;
		}

		public List<String> getParticipants() {
			return participants;
		}

		public void setParticipants(List<String> participants) {
			this.participants = participants;
		}

		public String getShowMoreLinkName() {
			return showMoreLinkName;
		}

		public void setShowMoreLinkName(String showMoreLinkName) {
			this.showMoreLinkName = showMoreLinkName;
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

		public MultipleSelectionElement getAcknowledgeRecordingEl() {
			return acknowledgeRecordingEl;
		}

		public void setAcknowledgeRecordingEl(MultipleSelectionElement acknowledgeRecordingEl) {
			this.acknowledgeRecordingEl = acknowledgeRecordingEl;
		}

		public String getAcknowledgeName() {
			return acknowledgeRecordingEl != null? acknowledgeRecordingEl.getName(): null;
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

		public String getMeetingWarning() {
			return meetingWarning;
		}

		public void setMeetingWarning(String meetingWarning) {
			this.meetingWarning = meetingWarning;
		}

		public List<String> getRecordingLinkNames() {
			return recordingLinkNames;
		}

		public void setRecordingLinkNames(List<String> recordingLinkNames) {
			this.recordingLinkNames = recordingLinkNames;
		}
		
	}
}
