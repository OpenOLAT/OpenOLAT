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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
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
	
	private final boolean moderatorStartMeeting;
	private final OLATResourceable meetingOres;

	private Link joinButton;
	private FlexiTableElement tableEl;
	private BigBlueButtonRecordingTableModel recordingTableModel;

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
		meetingOres = OresHelper.createOLATResourceableInstance(BigBlueButtonMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		moderatorStartMeeting = configuration.isModeratorStartMeeting();
		
		initForm(ureq);
		updateButtonsAndStatus();
		loadRecordingsModel();
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
		}
		
		joinButton = LinkFactory.createButtonLarge("meeting.join.button", flc.getFormItemComponent(), this);
		joinButton.setTarget("_blank");
		joinButton.setVisible(!ended);
	}
	
	private void initRecordings(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.type, new RecordingTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.open, new RecordingUrlCellRenderer(getTranslator())));
		
		recordingTableModel = new BigBlueButtonRecordingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "recordings", recordingTableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.recordings");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadRecordingsModel() {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		List<BigBlueButtonRecording> recordings = bigBlueButtonManager.getRecordings(meeting, errors);
		recordingTableModel.setObjects(recordings);
		tableEl.reset(true, true, true);
		flc.contextPut("hasRecordings", Boolean.valueOf(!recordings.isEmpty()));
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isAccessible() {
		if(meeting.isPermanent()) {
			return bigBlueButtonModule.isPermanentMeetingEnabled();
		}

		Date now = new Date();
		Date start = meeting.getStartWithLeadTime();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}
	
	private void reloadButtonsAndStatus() {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		updateButtonsAndStatus();
	}
	
	private boolean isDisabled() {
		return meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		boolean disabled = isDisabled();
		flc.contextPut("disabled", Boolean.valueOf(disabled));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		joinButton.setVisible(accessible && !disabled);
		joinButton.setEnabled(!readOnly);
			
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
			} else {
				flc.contextPut("notStarted", Boolean.FALSE);
				joinButton.setEnabled(!readOnly);
			}
		} else if(isEnded) {
			flc.contextPut("notStarted", Boolean.FALSE);
		}
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
	protected void formOK(UserRequest ureq) {
		//
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
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), (administrator || moderator), false, null, errors);
			BigBlueButtonEvent openEvent = new BigBlueButtonEvent(meeting.getKey(), getIdentity().getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(openEvent, meetingOres);
		} else if(!moderatorStartMeeting) {
			boolean guest = ureq.getUserSession().getRoles().isGuestOnly();
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), false, guest, null, errors);
		} else if(bigBlueButtonManager.isMeetingRunning(meeting)) {
			boolean guest = ureq.getUserSession().getRoles().isGuestOnly();
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), false, guest, Boolean.TRUE, errors);
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
}
