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

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.dropdown.Dropdown.ButtonSize;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
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
 * Initial date: 29 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicsRunCoachController extends FormBasicController {

	private static final long PARTICIPANTS_RENDER_LIMIT = 3;
	private static final String CMD_MORE = "more";
	private static final String CMD_OPEN = "open";
	private static final String CMD_JOIN = "join";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DUPLICATE = "duplicate";
	private static final String CMD_EXPORT = "export";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_GROUPS = "group";
	private static final String CMD_RECORDING = "recording";
	private static final String CMD_SYNC = "sync";

	private FormLink createButton;

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private AppointmentCreateController topicCreateCtrl;
	private TopicEditController topicEditCtrl;
	private TopicGroupsController topicGroupsCtrl;
	private DialogBoxController confirmDeleteTopicCrtl;
	private AppointmentListEditController topicRunCtrl;
	private ContextualSubscriptionController subscriptionCtrl;
	private StepsMainRunController wizard;
	
	private String avatarUrl;
	private final RepositoryEntry entry;
	private final String subIdent;
	private final AppointmentsSecurityCallback secCallback;
	private final OrganizerCandidateSupplier organizerCandidateSupplier;
	private Set<Long> acknowlededRecordings = new HashSet<>();
	private int counter;
	private Set<Long> showAllParticipationsTopicKeys = new HashSet<>();
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;

	public TopicsRunCoachController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, AppointmentsSecurityCallback secCallback,
			OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl, "topics_run_coach");
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.secCallback = secCallback;
		this.organizerCandidateSupplier = organizerCandidateSupplier;
		
		List<Organizer> organizers = appointmentsService.getOrganizers(entry, subIdent);
		if (secCallback.canSubscribe(organizers)) {
			PublisherData publisherData = appointmentsService.getPublisherData(entry, subIdent);
			SubscriptionContext subContext = appointmentsService.getSubscriptionContext(entry, subIdent);
			subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, publisherData, true);
			listenTo(subscriptionCtrl);
			flc.put("infoSubscription", subscriptionCtrl.getInitialComponent());
		}
		
		initForm(ureq);
		refresh();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (secCallback.canCreateTopic()) {
			createButton = uifactory.addFormLink("create.topic", formLayout, Link.BUTTON);
			createButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			createButton.setElementCssClass("o_sel_app_add_topic");
		}
	}
	
	private void refresh() {
		List<TopicWrapper> topics = loadTopicWrappers();
		flc.contextPut("topics", topics);
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
		
		topics.sort((t1, t2) -> t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase()));
		List<TopicWrapper> wrappers = new ArrayList<>(topics.size());
		for (Topic topic : topics) {
			TopicWrapper wrapper = new TopicWrapper(topic);
			List<Organizer> organizers = topicKeyToOrganizer.getOrDefault(topic.getKey(), emptyList());
			wrapper.setOrganizers(organizers);
			if (secCallback.canViewAppointment(organizers)) {
				wrapper.setOrganizerNames(AppointmentsUIFactory.formatOrganizers(organizers));
				List<Appointment> appointments = topicKeyToAppointments.getOrDefault(topic.getKey(), emptyList());
				List<Participation> topicParticipations = topicKeyToParticipations.getOrDefault(topic.getKey(), emptyList());
				wrapParticpations(wrapper, topicParticipations, appointments, appointmentKeyToParticipations);
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}

	private void wrapParticpations(TopicWrapper wrapper, List<Participation> participations, List<Appointment> appointments,
			Map<Long, List<Participation>> appointmentKeyToParticipations) {
		
		long numParticipants = participations.stream()
				.map(participation -> participation.getIdentity().getKey())
				.distinct()
				.count();
		long confirmableAppointmentsCount = appointments.stream()
				.filter(a -> isConfirmable(a, appointmentKeyToParticipations))
				.count();
		long numAppointmentsWithParticipations = participations.stream()
				.map(p -> p.getAppointment().getKey())
				.distinct()
				.count();
		wrapMessage(wrapper, appointments.size(), numParticipants, numAppointmentsWithParticipations, confirmableAppointmentsCount);
		
		Date now = new Date();
		Optional<Appointment> nextAppointment;
		if (Type.finding == wrapper.getTopic().getType()) {
			nextAppointment = appointments.stream()
					.filter(a -> Appointment.Status.confirmed == a.getStatus())
					.findFirst();
		} else {
			nextAppointment = appointments.stream()
					.filter(a -> Appointment.Status.confirmed == a.getStatus())
					.filter(a -> appointmentsService.isEndAfter(a, now))
					.sorted((a1, a2) -> a1.getStart().compareTo(a2.getStart()))
					.findFirst();
		}
		
		if (nextAppointment.isPresent()) {
			Appointment appointment = nextAppointment.get();
			
			List<Participation> appointmentParticipations = appointmentKeyToParticipations
					.getOrDefault(appointment.getKey(), Collections.emptyList());
			forgeAppointmentView(wrapper, appointment, appointmentParticipations);
			wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
			
			boolean showPrevNextHeader = Type.finding == wrapper.getTopic().getType()? false: true;
			if (showPrevNextHeader) {
				wrapper.setFuture(Boolean.TRUE);
			}
			
			wrapParticipants(wrapper, appointmentParticipations);
		}
		
		wrapOpenLink(wrapper);
		
		boolean hasMeetings = appointments.stream().anyMatch(appointment -> appointment.getBBBMeeting() != null);
		wrapTools(wrapper, hasMeetings);
	}

	private boolean isConfirmable(Appointment appointment, Map<Long, List<Participation>> appointmentKeyToParticipations) {
		return Appointment.Status.planned == appointment.getStatus() 
					&& appointmentKeyToParticipations.containsKey(appointment.getKey())
				? true
				: false;
	}
	
	private void wrapMessage(TopicWrapper wrapper, int totalAppointments, long numParticipants,
			long numAppointmentsWithParticipations, long confirmableAppointmentsCount) {
		List<String> messages = new ArrayList<>(2);
		if (totalAppointments == 0) {
			messages.add(translate("no.appointments"));
		} else {
			if (totalAppointments == 1) {
				messages.add(translate("appointments.total.one"));
			} else {
				messages.add(translate("appointments.total", new String[] { String.valueOf(totalAppointments) }));
			}
			if (numParticipants == 1 && numAppointmentsWithParticipations == 1) {
				messages.add(translate("participations.selected.one.one"));
			} else if (numParticipants == 1 && numAppointmentsWithParticipations > 1) {
				messages.add(translate("participations.selected.one.many", new String[] { String.valueOf(numAppointmentsWithParticipations) }));
			} else if (numParticipants > 1 && numAppointmentsWithParticipations == 1) {
				messages.add(translate("participations.selected.many.one", new String[] { String.valueOf(numParticipants) }));
			} else if (numParticipants > 1 && numAppointmentsWithParticipations > 1) {
				messages.add(translate("participations.selected.many.many", new String[] { String.valueOf(numParticipants), String.valueOf(numAppointmentsWithParticipations) }));
			} else {
				messages.add(translate("participations.selected.many.many", new String[] { String.valueOf(0), String.valueOf(0) }));
			}
			
			if (!wrapper.getTopic().isAutoConfirmation() && numAppointmentsWithParticipations > 0) {
				if (confirmableAppointmentsCount == 1) {
					messages.add(translate("appointments.confirmable.one"));
				} else if (confirmableAppointmentsCount > 1) {
					messages.add(translate("appointments.confirmable", new String[] { String.valueOf(confirmableAppointmentsCount) }));
				} else {
					messages.add(translate("appointments.confirmable.none"));
				}
			}
		}
		
		String message = messages.isEmpty()? null: messages.stream().collect(Collectors.joining("<br>"));
		wrapper.setMessage(message);
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
	
	private void forgeAppointmentView(TopicWrapper wrapper, Appointment appointment, List<Participation> participations) {
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
		
		if (appointmentsService.isBigBlueButtonEnabled()) {
			if (secCallback.canJoinBBBMeeting(appointment, wrapper.getOrganizers(), participations)) {
				wrapMBBBeeting(wrapper, appointment);
			}
			if (secCallback.canWatchRecording(wrapper.getOrganizers(), participations)) {
				List<BigBlueButtonRecordingReference> recordingReferences = appointmentsService
						.getBBBRecordingReferences(Collections.singletonList(appointment))
						.getOrDefault(appointment.getKey(), Collections.emptyList());
				wrapRecordings(wrapper, recordingReferences);
			}
		}
		
		if (appointmentsService.isTeamsEnabled()
				&& secCallback.canJoinTeamsMeeting(appointment, wrapper.getOrganizers(), participations)) {
			wrapTeamsMeeting(wrapper);
		}
	}
	
	private void wrapMBBBeeting(TopicWrapper wrapper, Appointment appointment) {
		wrapper.setBbb(true);
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
			if (acknowlededRecordings.contains(wrapper.getTopic().getKey())) {
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
	
	private boolean isServerDisabled(BigBlueButtonMeeting meeting) {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void wrapTeamsMeeting(TopicWrapper wrapper) {
		wrapper.setBbb(true);
		
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

	private void wrapOpenLink(TopicWrapper wrapper) {
		FormLink link = uifactory.addFormLink("open" + counter++, CMD_OPEN, "appointments.open", null, flc, Link.LINK);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setUserObject(wrapper.getTopic());
		wrapper.setOpenLinkName(link.getName());
	}
	
	private void wrapTools(TopicWrapper wrapper, boolean hasMeetings) {
		String toolsName = "tools_" + counter++;
		DropdownItem dropdown = uifactory.addDropdownMenu(toolsName, null, null, flc, getTranslator());
		dropdown.setCarretIconCSS("o_icon o_icon_tool");
		dropdown.setButtonSize(ButtonSize.small);
		dropdown.setOrientation(DropdownOrientation.right);
		dropdown.setExpandContentHeight(true); 
		wrapper.setToolsName(toolsName);
		
		FormLink editorLink = uifactory.addFormLink("edit_" + counter++, CMD_EDIT, "edit.topic", null, flc, Link.LINK);
		editorLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editorLink.setUserObject(wrapper.getTopic());
		dropdown.addElement(editorLink);
		
		FormLink groupLink = uifactory.addFormLink("group_" + counter++, CMD_GROUPS, "edit.groups", null, flc, Link.LINK);
		groupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		groupLink.setUserObject(wrapper.getTopic());
		dropdown.addElement(groupLink);
		
		FormLink exportLink = uifactory.addFormLink("export_" + counter++, CMD_EXPORT, "export.participations", null, flc, Link.LINK);
		exportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		exportLink.setUserObject(wrapper.getTopic());
		dropdown.addElement(exportLink);
		
		if (hasMeetings) {
			FormLink syncRecordingsLink = uifactory.addFormLink("sync_" + counter++, CMD_SYNC, "sync.recordings", null, flc, Link.LINK);
			syncRecordingsLink.setIconLeftCSS("o_icon o_icon-fw o_vc_icon");
			syncRecordingsLink.setUserObject(wrapper.getTopic());
			dropdown.addElement(syncRecordingsLink);
		}
		
		FormLink duplicateLink = uifactory.addFormLink("dup_" + counter++, CMD_DUPLICATE, "duplicate.topic", null, flc, Link.LINK);
		duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
		duplicateLink.setUserObject(wrapper.getTopic());
		dropdown.addElement(duplicateLink);
		
		FormLink deleteLink = uifactory.addFormLink("delete_" + counter++, CMD_DELETE, "delete.topic", null, flc, Link.LINK);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		deleteLink.setUserObject(wrapper.getTopic());
		dropdown.addElement(deleteLink);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == topicCreateCtrl) {
			if (event == Event.DONE_EVENT) {
				Topic topic = topicCreateCtrl.getTopic();
				cmc.deactivate();
				cleanUp();
				doOpenTopic(ureq, topic);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (topicEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				refresh();
			}
			cmc.deactivate();
			cleanUp();
		} else if (topicGroupsCtrl == source) {
			stackPanel.popUpToRootController(ureq);
			cleanUp();
		} else if (wizard == source) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					refresh();
				}
				cleanUp();
			}
		} else if (source == confirmDeleteTopicCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				TopicRef topic = (TopicRef)confirmDeleteTopicCrtl.getUserObject();
				doDeleteTopic(topic);
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
		removeAsListenerAndDispose(topicCreateCtrl);
		removeAsListenerAndDispose(topicGroupsCtrl);
		removeAsListenerAndDispose(topicRunCtrl);
		removeAsListenerAndDispose(wizard);
		removeAsListenerAndDispose(cmc);
		topicCreateCtrl = null;
		topicGroupsCtrl = null;
		topicRunCtrl = null;
		wizard = null;
		cmc = null;
	}
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createButton) {
			doAddTopic(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_MORE.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doToggleShowMoreParticipations(topic);
			} else if (CMD_OPEN.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doOpenTopic(ureq, topic);
			} else if (CMD_EDIT.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doEditTopic(ureq, topic);
			} else if (CMD_GROUPS.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doEditGroups(ureq, topic);
			} else if (CMD_EXPORT.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doExport(ureq, topic);
			} else if (CMD_DUPLICATE.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doDuplicateTopic(ureq, topic);
			} else if (CMD_DELETE.equals(cmd)) {
				TopicRef topic = (TopicRef)link.getUserObject();
				doConfirmDeleteTopic(ureq, topic);
			} else if (CMD_SYNC.equals(cmd)) {
				Topic topic = (Topic)link.getUserObject();
				doSyncRecordings(topic);
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
				acknowlededRecordings.add(topic.getKey());
			} else {
				acknowlededRecordings.remove(topic.getKey());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	private void doAddTopic(UserRequest ureq) {
		topicCreateCtrl = new AppointmentCreateController(ureq, getWindowControl(), entry, subIdent, organizerCandidateSupplier);
		listenTo(topicCreateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", topicCreateCtrl.getInitialComponent(), true,
				translate("create.topic.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteTopic(UserRequest ureq, TopicRef topic) {
		String title = translate("confirm.topic.delete.title");
		String text = translate("confirm.topic.delete");
		confirmDeleteTopicCrtl = activateYesNoDialog(ureq, title, text, confirmDeleteTopicCrtl);
		confirmDeleteTopicCrtl.setUserObject(topic);
	}

	private void doDeleteTopic(TopicRef topic) {
		appointmentsService.deleteTopic(topic);
		refresh();
	}

	private void doEditTopic(UserRequest ureq, Topic topic) {
		topicEditCtrl = new TopicEditController(ureq, getWindowControl(), topic, organizerCandidateSupplier);
		listenTo(topicEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", topicEditCtrl.getInitialComponent(), true,
				translate("edit.topic"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditGroups(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(topicGroupsCtrl);
		
		topicGroupsCtrl = new TopicGroupsController(ureq, getWindowControl(), topic);
		listenTo(topicGroupsCtrl);
		
		stackPanel.pushController(topic.getTitle(), topicGroupsCtrl);
	}
	
	private void doExport(UserRequest ureq, Topic topic) {
		ParticipationSearchParams searchParams = new ParticipationSearchParams();
		searchParams.setTopic(topic);
		ExcelExport export = new ExcelExport(ureq, searchParams, getExportName(topic));
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}

	private String getExportName(Topic topic) {
		return new StringBuilder()
				.append(translate("export.participations.file.prefix"))
				.append("_")
				.append(topic.getTitle())
				.append("_")
				.append(Formatter.formatDatetimeFilesystemSave(new Date()))
				.toString();
	}

	private void doDuplicateTopic(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(wizard);
		wizard = new StepsMainRunController(ureq, getWindowControl(), new DuplicateTopic1Step(ureq, topic, organizerCandidateSupplier),
				new DuplicateTopicCallback(), null, translate("duplicate.topic.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}

	private void doToggleShowMoreParticipations(Topic topic) {
		if (showAllParticipationsTopicKeys.contains(topic.getKey())) {
			showAllParticipationsTopicKeys.remove(topic.getKey());
		} else {
			showAllParticipationsTopicKeys.add(topic.getKey());
		}
		refresh();
	}
	
	private void doOpenTopic(UserRequest ureq, Topic topic) {
		removeAsListenerAndDispose(topicRunCtrl);
		
		topicRunCtrl = new AppointmentListEditController(ureq, getWindowControl(), topic, secCallback);
		listenTo(topicRunCtrl);
		
		String title = topic.getTitle();
		String panelTitle = title.length() > 50? title.substring(0, 50) + "...": title;
		stackPanel.pushController(panelTitle, topicRunCtrl);
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
		if (BigBlueButtonUIHelper.isRecord(appointment.getBBBMeeting()) && !acknowlededRecordings.contains(appointment.getTopic().getKey())) {
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
	
	private void doSyncRecordings(Topic topic) {
		appointmentsService.syncBBBRecorings(topic);
		refresh();
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
		private String message;
		
		//next appointment
		private Appointment appointment;
		private List<String> participants;
		private String showMoreLinkName;
		private String dayName;
		private String date;
		private String date2;
		private String time;
		private String location;
		private String details;
		private String translatedStatus;
		private String statusCSS;
		private Boolean future;
		
		private MultipleSelectionElement acknowledgeRecordingEl;
		private String openLinkName;
		private String toolsName;
		
		private boolean bbb;
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

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
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

		public Boolean getFuture() {
			return future;
		}

		public void setFuture(Boolean future) {
			this.future = future;
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

		public String getOpenLinkName() {
			return openLinkName;
		}

		public void setOpenLinkName(String openLinkName) {
			this.openLinkName = openLinkName;
		}

		public String getToolsName() {
			return toolsName;
		}

		public void setToolsName(String toolsName) {
			this.toolsName = toolsName;
		}

		public boolean isBbb() {
			return bbb;
		}

		public void setBbb(boolean bbb) {
			this.bbb = bbb;
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
