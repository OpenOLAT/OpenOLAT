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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRecordingTableModel.BRecordingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingController extends FormBasicController implements GenericEventListener {
	
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private BigBlueButtonMeeting meeting;
	
	private final boolean guest;
	private final boolean moderatorStartMeeting;
	private final OLATResourceable meetingOres;

	private Link joinButton;
	private ExternalLink guestJoinButton;
	private FlexiTableElement tableEl;
	private BigBlueButtonRecordingTableModel recordingTableModel;

	private PublishRecordingController publishCtrl;
	private DialogBoxController confirmDeleteRecordingDialog;
	private CloseableCalloutWindowController publishCalloutCtrl;

	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeeting meeting, BigBlueButtonMeetingDefaultConfiguration configuration,
			boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.meeting = meeting;
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = administrator;
		UserSession usess = ureq.getUserSession();
		guest = usess.getRoles().isGuestOnly();
		meetingOres = OresHelper.createOLATResourceableInstance(BigBlueButtonMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		moderatorStartMeeting = configuration.isModeratorStartMeeting();
		
		initForm(ureq);
		updateButtonsAndStatus();
		loadRecordingsModel();
		
		if(guest) {
			usess.putEntryInNonClearedStore("meeting-" + meeting.getKey(), Boolean.TRUE);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initJoinForm(formLayout);
		initRecordings(formLayout);
	}
	
	private void initJoinForm(FormItemContainer formLayout) {
		boolean ended = isEnded();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", meeting.getName());
			if(StringHelper.containsNonWhitespace(meeting.getDescription())) {
				layoutCont.contextPut("description", meeting.getDescription());
			}
			if(meeting.getStartDate() != null) {
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
				layoutCont.contextPut("start", start);
			}
			if(meeting.getEndDate() != null) {
				String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
				layoutCont.contextPut("end", end);
			}
			
			if((administrator || moderator) && StringHelper.containsNonWhitespace(meeting.getReadableIdentifier())) {
				String url = BigBlueButtonDispatcher.getMeetingUrl(meeting.getReadableIdentifier());
				layoutCont.contextPut("externalUrl", url);
			}
			
			if(StringHelper.containsNonWhitespace(meeting.getMainPresenter())) {
				layoutCont.contextPut("mainPresenter", meeting.getMainPresenter());
			}
		}
		
		joinButton = LinkFactory.createButtonLarge("meeting.join.button", flc.getFormItemComponent(), this);
		joinButton.setElementCssClass("o_sel_bbb_join");
		joinButton.setTarget("_blank");
		joinButton.setVisible(!ended && !guest);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		
		String url = Settings.getServerContextPathURI() + "/bigbluebutton/" + meeting.getIdentifier();
		guestJoinButton = LinkFactory.createExternalLink("meeting.guest.join.button", "meeting.guest.join.button", url);
		guestJoinButton.setElementCssClass("btn btn-lg btn-default o_sel_bbb_guest_join");
		guestJoinButton.setName(translate("meeting.guest.join.button"));
		guestJoinButton.setTarget("_blank");
		guestJoinButton.setVisible(!ended && guest);
		flc.getFormItemComponent().put("meeting.guest.join.button", guestJoinButton);
	}
	
	private void initRecordings(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.type, new RecordingTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.recording.open", BRecordingsCols.open.ordinal(), "open-recording",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.recording.open"), "open-recording"), null)));
		if(administrator) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.publish));
			if(bigBlueButtonManager.getRecordingsHandler().canDeleteRecordings()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
			}
		}
		
		recordingTableModel = new BigBlueButtonRecordingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "recordings", recordingTableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.recordings");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadRecordingsModel() {
		BigBlueButtonAttendee attendee = bigBlueButtonManager.getAttendee(getIdentity(), meeting);
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		List<BigBlueButtonRecordingWithReference> recordings = bigBlueButtonManager.getRecordingAndReferences(meeting, errors);
		List<BigBlueButtonRecordingRow> rows = new ArrayList<>(recordings.size());
		for(BigBlueButtonRecordingWithReference recording:recordings) {
			BigBlueButtonRecordingRow row = forgeRow(recording, attendee);
			if(row != null) {
				rows.add(row);
			}
		}
		recordingTableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.contextPut("hasRecordings", Boolean.valueOf(!recordings.isEmpty()));
	}
	
	private BigBlueButtonRecordingRow forgeRow(BigBlueButtonRecordingWithReference recording, BigBlueButtonAttendee attendee) {
		boolean pusblished = isPublishedForMe(recording.getReference(), attendee);
		BigBlueButtonRecordingRow row = new BigBlueButtonRecordingRow(recording, pusblished);
		if(administrator || moderator) {
			FormLink publishLink = uifactory.addFormLink("publish-" + recording.getRecording().getRecordId(),
					"publish", "publish.recording", tableEl);
			row.setPublishLink(publishLink);
			publishLink.setUserObject(row);
		}
		return row;
	}
	
	private boolean isPublishedForMe(BigBlueButtonRecordingReference reference, BigBlueButtonAttendee attendee) {
		if(reference == null) return false;
		
		BigBlueButtonRecordingsPublishedRoles[] publishTo = reference.getPublishToEnum();
		if(BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.none)) {
			return false;
		}
		if(guest) {
			return BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.guest);
		}
		if(attendee != null && BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.all)) {
			return true;
		}
		return ((administrator || moderator) && BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.coach))
				|| BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.participant);
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isAccessible() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return bigBlueButtonModule.isPermanentMeetingEnabled();
		}

		Date now = new Date();
		Date start = (administrator || moderator) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}
	
	private void reloadButtonsAndStatus() {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private boolean isDisabled() {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		boolean disabled = isDisabled();
		flc.contextPut("disabled", Boolean.valueOf(disabled));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		// only change from invisible to visible
		if(!joinButton.isVisible()) {
			joinButton.setVisible(accessible && !disabled && !guest);
		}
		joinButton.setEnabled(!readOnly && accessible && !disabled && !guest);
		
		if(!guestJoinButton.isVisible()) {
			guestJoinButton.setVisible(accessible && !disabled && guest);
		}
		guestJoinButton.setEnabled(!readOnly && accessible && !disabled && guest);
			
		if(accessible && !disabled) {
			boolean running = bigBlueButtonManager.isMeetingRunning(meeting);
			if(moderator || administrator) {
				flc.contextPut("notStarted", Boolean.FALSE);
				if(!running && moderatorStartMeeting) {
					joinButton.setCustomDisplayText(translate("meeting.start.button"));
				} else {
					joinButton.setCustomDisplayText(translate("meeting.join.button"));
				}
			} else if(!running && moderatorStartMeeting) {
				flc.contextPut("notStarted", Boolean.TRUE);
				joinButton.setEnabled(false);
				guestJoinButton.setEnabled(false);
			} else {
				flc.contextPut("notStarted", Boolean.FALSE);
				joinButton.setEnabled(!readOnly && !guest);
				guestJoinButton.setEnabled(!readOnly && guest);
			}
		} else if(isEnded) {
			flc.contextPut("notStarted", Boolean.FALSE);
		}

		// update button style to indicate that the user must now press to start
		if (guestJoinButton.isEnabled()) {
			guestJoinButton.setElementCssClass("btn btn-lg btn-primary o_sel_bbb_guest_join");			
		} else {			
			guestJoinButton.setElementCssClass("btn btn-lg btn-default o_sel_bbb_guest_join");			
		}
		joinButton.setPrimary(joinButton.isEnabled());
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
	}

	@Override
	public void event(Event event) {
		if(event instanceof BigBlueButtonEvent) {
			BigBlueButtonEvent ace = (BigBlueButtonEvent)event;
			if(ace.getMeetingKey() != null && ace.getMeetingKey().equals(meeting.getKey())) {
				reloadButtonsAndStatus();
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(joinButton == source) {
			doJoin(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteRecordingDialog == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonRecording recording = (BigBlueButtonRecording)confirmDeleteRecordingDialog.getUserObject();
				doDeleteRecording(recording);
			}
			cleanUp();
		} else if(publishCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadRecordingsModel();
			}
			publishCalloutCtrl.deactivate();
			cleanUp();
		} else if(publishCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteRecordingDialog);
		removeAsListenerAndDispose(publishCalloutCtrl);
		removeAsListenerAndDispose(publishCtrl);
		confirmDeleteRecordingDialog = null;
		publishCalloutCtrl = null;
		publishCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					doConfirmDeleteRecording(ureq, recordingTableModel.getObject(se.getIndex()).getRecording());
				} else if("open-recording".equals(se.getCommand())) {
					doOpenRecording(recordingTableModel.getObject(se.getIndex()).getRecording());
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("publish".equals(link.getCmd()) && link.getUserObject() instanceof BigBlueButtonRecordingRow) {
				doPublish(ureq, link, (BigBlueButtonRecordingRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPublish(UserRequest ureq, FormLink link, BigBlueButtonRecordingRow row) {
		publishCtrl = new PublishRecordingController(ureq, getWindowControl(), row);
		listenTo(publishCtrl); 

		publishCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				publishCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(publishCalloutCtrl);
		publishCalloutCtrl.activate();
	}

	private void doJoin(UserRequest ureq) {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
			return;
		}
		
		String meetingUrl = null;
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		if(moderator || administrator) {
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, BigBlueButtonAttendeeRoles.moderator, null, errors);
			delayEvent(new BigBlueButtonEvent(meeting.getKey(), getIdentity().getKey()));
		} else if(!moderatorStartMeeting) {
			BigBlueButtonAttendeeRoles role = guest ? BigBlueButtonAttendeeRoles.guest : BigBlueButtonAttendeeRoles.external;
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, role, null, errors);
		} else if(bigBlueButtonManager.isMeetingRunning(meeting)) {
			BigBlueButtonAttendeeRoles role = guest ? BigBlueButtonAttendeeRoles.guest : BigBlueButtonAttendeeRoles.viewer;
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, role, Boolean.TRUE, errors);
		}
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
	
	private void doOpenRecording(BigBlueButtonRecording recording) {
		String url = bigBlueButtonManager.getRecordingUrl(recording);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
	}
	
	private void doConfirmDeleteRecording(UserRequest ureq, BigBlueButtonRecording recording) {
		String confirmDeleteTitle = translate("confirm.delete.recording.title", new String[]{ recording.getName() });
		String confirmDeleteText = translate("confirm.delete.recording", new String[]{ recording.getName() });
		confirmDeleteRecordingDialog = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDeleteRecordingDialog);
		confirmDeleteRecordingDialog.setUserObject(recording);
	}
	
	private void doDeleteRecording(BigBlueButtonRecording recording) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteRecording(recording, meeting, errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		}
		loadRecordingsModel();
	}
	
	private void delayEvent(BigBlueButtonEvent openEvent) {
		final EventTask task = new EventTask(openEvent, meetingOres);
		taskExecutorManager.schedule(task , 10000);
	}
	
	private static class EventTask extends TimerTask {
		
		private final BigBlueButtonEvent event;
		private final OLATResourceable ores;
		
		public EventTask(BigBlueButtonEvent event, OLATResourceable ores) {
			this.event = event;
			this.ores = OresHelper.clone(ores);
		}

		@Override
		public void run() {
        	CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
		}
	}
}
