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
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.ButtonSize;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
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
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonErrorHelper;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
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

	private final VelocityContainer mainVC;
	private Link createButton;

	private final BreadcrumbedStackedPanel stackPanel;
	private CloseableModalController cmc;
	private AppointmentCreateController topicCreateCtrl;
	private TopicEditController topicEditCtrl;
	private TopicGroupsController topicGroupsCtrl;
	private DialogBoxController confirmDeleteTopicCrtl;
	private AppointmentListEditController topicRunCtrl;
	private ContextualSubscriptionController subscriptionCtrl;
	private StepsMainRunController wizard;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final AppointmentsSecurityCallback secCallback;
	private int counter;
	private Set<Topic> showAllParticipations = new HashSet<>();
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public TopicsRunCoachController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, String subIdent, AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.subIdent = subIdent;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("topics_run_coach");
		
		if (secCallback.canSubscribe()) {
			PublisherData publisherData = appointmentsService.getPublisherData(entry, subIdent);
			SubscriptionContext subContext = appointmentsService.getSubscriptionContext(entry, subIdent);
			subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, publisherData, true);
			listenTo(subscriptionCtrl);
			mainVC.put("infoSubscription", subscriptionCtrl.getInitialComponent());
		}
		
		refresh();
		putInitialPanel(mainVC);
	}
	
	private void refresh() {
		clearVC();
		
		if (secCallback.canCreateTopic()) {
			createButton = LinkFactory.createButton("create.topic", mainVC, this);
			createButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		
		List<TopicWrapper> topics = loadTopicWrappers();
		mainVC.contextPut("topics", topics);
	}
	
	private void clearVC() {
		List<String> componentNames = new ArrayList<>();
		for (Component component : mainVC.getComponents()) {
			if (!"infoSubscription".equals(component.getComponentName()) && 
					!"create.topic".equals(component.getComponentName())) {
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
		
		topics.sort((t1, t2) -> t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase()));
		List<TopicWrapper> wrappers = new ArrayList<>(topics.size());
		for (Topic topic : topics) {
			TopicWrapper wrapper = new TopicWrapper(topic);
			List<Organizer> organizers = topicKeyToOrganizer.getOrDefault(topic.getKey(), emptyList());
			if (secCallback.canViewAppointment(organizers)) {
				wrapOrganizers(wrapper, organizers);
				List<Appointment> appointments = topicKeyToAppointments.getOrDefault(topic.getKey(), emptyList());
				List<Participation> topicParticipations = topicKeyToParticipations.getOrDefault(topic.getKey(), emptyList());
				wrapParticpations(wrapper, topicParticipations, appointments, appointmentKeyToParticipations);
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}

	private void wrapOrganizers(TopicWrapper wrapper, List<Organizer> organizers) {
		wrapper.setOrganizers(organizers);
		List<String> organizerNames = new ArrayList<>(organizers.size());
		for (Organizer organizer : organizers) {
			String name = userManager.getUserDisplayName(organizer.getIdentity().getKey());
			organizerNames.add(name);
		}
		organizerNames.sort(String.CASE_INSENSITIVE_ORDER);
		wrapper.setOrganizerNames(organizerNames);
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
		
		boolean hasMeetings = appointments.stream().anyMatch(appointment -> appointment.getMeeting() != null);
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
		long limit = showAllParticipations.contains(wrapper.getTopic())? Long.MAX_VALUE: PARTICIPANTS_RENDER_LIMIT;
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.limit(limit)
				.collect(Collectors.toList());
		wrapper.setParticipants(participants);
		
		if (participations.size() > PARTICIPANTS_RENDER_LIMIT) {
			String name = "more_" + wrapper.getTopic().getKey();
			Link showMoreLink = LinkFactory.createCustomLink(name, CMD_MORE, "", Link.LINK+ Link.NONTRANSLATED, mainVC, this);
			
			long hiddenParticipations = participations.size() - PARTICIPANTS_RENDER_LIMIT;
			String displayText = showAllParticipations.contains(wrapper.getTopic())
					? translate("show.less")
					: translate("show.more", new String[] { String.valueOf(hiddenParticipations)} );
			showMoreLink.setCustomDisplayText(displayText);
			showMoreLink.setUserObject(wrapper.getTopic());
			wrapper.setShowMoreLinkName(showMoreLink.getComponentName());
		}
	}
	
	private void forgeAppointmentView(TopicWrapper wrapper, Appointment appointment, List<Participation> participations) {
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
		
		if (appointmentsService.isBigBlueButtonEnabled()) {
			if (secCallback.canJoinMeeting(appointment.getMeeting(), wrapper.getOrganizers(), participations)) {
				wrapMeeting(wrapper, appointment);
			}
			if (secCallback.canWatchRecording(wrapper.getOrganizers(), participations)) {
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

	private void wrapOpenLink(TopicWrapper wrapper) {
		Link openLink = LinkFactory.createCustomLink("open" + counter++, CMD_OPEN, "appointments.open", Link.LINK, mainVC, this);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setUserObject(wrapper.getTopic());
		wrapper.setOpenLinkName(openLink.getComponentName());
	}
	
	private void wrapTools(TopicWrapper wrapper, boolean hasMeetings) {
		String toolsName = "tools_" + counter++;
		Dropdown dropdown =  new Dropdown(toolsName, null, false, getTranslator());
		dropdown.setEmbbeded(true);
		dropdown.setCarretIconCSS("o_icon o_icon_tool");
		dropdown.setButton(true);
		dropdown.setButtonSize(ButtonSize.small);
		dropdown.setOrientation(DropdownOrientation.right);
		dropdown.setExpandContentHeight(true); 
		mainVC.put(toolsName, dropdown);
		wrapper.setToolsName(toolsName);
		
		Link editorLink = LinkFactory.createCustomLink("edit_" + counter++, CMD_EDIT, "edit.topic", Link.LINK, mainVC, this);
		editorLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editorLink.setUserObject(wrapper.getTopic());
		dropdown.addComponent(editorLink);
		
		Link groupLink = LinkFactory.createCustomLink("group_" + counter++, CMD_GROUPS, "edit.groups", Link.LINK, mainVC, this);
		groupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		groupLink.setUserObject(wrapper.getTopic());
		dropdown.addComponent(groupLink);
		
		Link exportLink = LinkFactory.createCustomLink("export_" + counter++, CMD_EXPORT, "export.participations", Link.LINK, mainVC, this);
		exportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		exportLink.setUserObject(wrapper.getTopic());
		dropdown.addComponent(exportLink);
		
		if (hasMeetings) {
			Link syncRecordingsLink = LinkFactory.createCustomLink("sync_" + counter++, CMD_SYNC, "sync.recordings", Link.LINK, mainVC, this);
			syncRecordingsLink.setIconLeftCSS("o_icon o_icon-fw o_vc_icon");
			syncRecordingsLink.setUserObject(wrapper.getTopic());
			dropdown.addComponent(syncRecordingsLink);
		}
		
		Link duplicateLink = LinkFactory.createCustomLink("dup_" + counter++, CMD_DUPLICATE, "duplicate.topic", Link.LINK, mainVC, this);
		duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
		duplicateLink.setUserObject(wrapper.getTopic());
		dropdown.addComponent(duplicateLink);
		
		Link deleteLink = LinkFactory.createCustomLink("delete_" + counter++, CMD_DELETE, "delete.topic", Link.LINK, mainVC, this);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		deleteLink.setUserObject(wrapper.getTopic());
		dropdown.addComponent(deleteLink);
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == createButton) {
			doAddTopic(ureq);
		} else if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
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
				Appointment appointment = (Appointment)link.getUserObject();
				doJoin(ureq, appointment);
			} else if (CMD_RECORDING.equals(cmd)) {
				BigBlueButtonRecordingReference recordingReference = (BigBlueButtonRecordingReference)link.getUserObject();
				doOpenRecording(ureq, recordingReference);
			}
		}
	}
	
	private void doAddTopic(UserRequest ureq) {
		topicCreateCtrl = new AppointmentCreateController(ureq, getWindowControl(), entry, subIdent);
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
		topicEditCtrl = new TopicEditController(ureq, getWindowControl(), topic);
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
		wizard = new StepsMainRunController(ureq, getWindowControl(), new DuplicateTopic1Step(ureq, topic),
				new DuplicateTopicCallback(), null, translate("duplicate.topic.title"), "");
		listenTo(wizard);
		getWindowControl().pushAsModalDialog(wizard.getInitialComponent());
	}

	private void doToggleShowMoreParticipations(Topic topic) {
		if (showAllParticipations.contains(topic)) {
			showAllParticipations.remove(topic);
		} else {
			showAllParticipations.add(topic);
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
	
	private void doSyncRecordings(Topic topic) {
		appointmentsService.syncRecorings(topic);
		refresh();
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
		private String message;
		
		//next appointment
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
		
		private String openLinkName;
		private String toolsName;
		
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

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
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
