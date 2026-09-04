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
package org.olat.modules.teams.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.teams.TeamsAttendee;
import org.olat.modules.teams.TeamsDispatcher;
import org.olat.modules.teams.TeamsLoggingAction;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingStatusEnum;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.ui.TeamsRecordingTableModel.TeamsRecordingsCols;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingController extends FormBasicController implements GenericEventListener {
	
	private static final String CMD_OPEN_RECORDINGS = "open-recording";
	private static final String CMD_PUBLISH = "publish";
	private static final String START_RECORDING_KEY = "start-rec";
	
	private FormLink joinButton;
	private MultipleSelectionElement acknowledgeRecordingEl;
	private FlexiTableElement tableEl;
	private TeamsRecordingTableModel recordingTableModel;
	private MultipleSelectionElement startRecordingEl;
	
	private TeamsMeeting meeting;
	private final boolean guest;
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private final OLATResourceable meetingOres;

	private boolean withPublish = false;
	
	private final User graphUser;
	private final OAuth2Tokens oauth2Tokens;
	private final Formatter formatter;
	
	private Controller docEditorCtrl;
	private ToolsController toolsCtrl;
	private CloseableModalController cmc; 
	private PublishRecordingController publishCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableCalloutWindowController publishCalloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public TeamsMeetingController(UserRequest ureq, WindowControl wControl, TeamsMeeting meeting,
			boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.readOnly = readOnly;
		this.meeting = meeting;
		this.moderator = moderator;
		this.administrator = administrator;
		formatter = Formatter.getInstance(getLocale());
		UserSession usess = ureq.getUserSession();
		guest = usess.getRoles().isGuestOnly();
		TeamsErrors errors = new TeamsErrors();
		oauth2Tokens = usess.getOAuth2Tokens();
		graphUser = teamsService.lookupMe(getIdentity(), oauth2Tokens, errors);
		meetingOres = OresHelper.createOLATResourceableInstance(TeamsMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(meeting));
		
		initForm(ureq);
		updateButtonsAndStatus();
		loadModel();
		if(errors.hasErrors()) {
			getWindowControl().setError(TeamsUIHelper.formatErrors(getTranslator(), errors));
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean ended = isEnded();
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			initFormInformations(layoutCont);
		}
		
		joinButton = uifactory.addFormLink("meeting.join.button", translate("meeting.join.button"), null,
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		joinButton.setElementCssClass("o_sel_teams_join");
		joinButton.setNewWindow(true, true, true);
		joinButton.setVisible(!ended);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		
		SelectionValues acknowledgeKeyValue = new SelectionValues();
		acknowledgeKeyValue.add(SelectionValues.entry("agree", translate("meeting.acknowledge.recording.agree")));
		acknowledgeRecordingEl = uifactory.addCheckboxesHorizontal("meeting.acknowledge.recording", null, formLayout,
				acknowledgeKeyValue.keys(), acknowledgeKeyValue.values());
		acknowledgeRecordingEl.setVisible(!ended);
		Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
		boolean isConform = teamsService.getUserConformanceDecisionById(meeting.getKey(), guiPreferences);
		acknowledgeRecordingEl.select("agree", isConform);
		
		SelectionValues startPK = new SelectionValues();
		startPK.add(SelectionValues.entry(START_RECORDING_KEY, translate("start.recording")));
		startRecordingEl = uifactory.addCheckboxesHorizontal("start.recording", null, formLayout,
				startPK.keys(), startPK.values());
		startRecordingEl.setVisible(!ended);
		startRecordingEl.select(START_RECORDING_KEY, meeting.isRecordAutoStart());
		
		initRecordings(formLayout);
	}
	
	private void initFormInformations(FormLayoutContainer layoutCont) {
		layoutCont.contextPut("subject", meeting.getSubject());
		if(meeting.getStartDate() != null) {
			String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
			layoutCont.contextPut("start", start);
		}
		if(meeting.getEndDate() != null) {
			String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
			layoutCont.contextPut("end", end);
		}
		
		String descr = meeting.getDescription();
		if(StringHelper.containsNonWhitespace(descr)) {
			if(!StringHelper.isHtml(descr)) {
				descr = Formatter.escWithBR(descr).toString();
			}
			layoutCont.contextPut("description", descr);
		}
		
		if((administrator || moderator) && StringHelper.containsNonWhitespace(meeting.getReadableIdentifier())) {
			String url = TeamsDispatcher.getMeetingUrl(meeting.getReadableIdentifier());
			layoutCont.contextPut("externalUrl", url);
		}
		
		if(graphUser != null && oauth2Tokens != null
				&& StringHelper.containsNonWhitespace(graphUser.getDisplayName())) {
			layoutCont.contextPut("asUser", translate("as.user", graphUser.getDisplayName()));
		} else {
			layoutCont.contextPut("asUser", translate("as.user.guest"));
		}
	}
	
	private void initRecordings(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeamsRecordingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeamsRecordingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeamsRecordingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.recording.open", TeamsRecordingsCols.open.ordinal(), CMD_OPEN_RECORDINGS,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.recording.open"), CMD_OPEN_RECORDINGS, true, true), null)));
		
		if(administrator) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeamsRecordingsCols.publish));
			withPublish = true;
		
			columnsModel.addFlexiColumnModel(new ActionsColumnModel(TeamsRecordingsCols.tools));
		}
		
		recordingTableModel = new TeamsRecordingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "recordings", recordingTableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("no.recordings")
				.build());
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isAccessible() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return true;
		}

		Date now = new Date();
		Date start = (administrator || moderator) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}
	
	private void reloadButtonsAndStatus() {
		meeting = teamsService.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		joinButton.setEnabled(!readOnly && accessible);
		
		boolean running = teamsService.isMeetingRunning(meeting);
		boolean accountWarning = (!running && (graphUser == null || oauth2Tokens == null)
				&& (moderator || administrator || meeting.isParticipantsCanOpen()));
		flc.contextPut("microsoftAccountWarning", Boolean.valueOf(accountWarning));

		boolean start = false;
		if(graphUser != null && oauth2Tokens != null && (moderator || administrator || meeting.isParticipantsCanOpen())) {
			flc.contextPut("notStarted", Boolean.FALSE);
			if(!running) {
				joinButton.setI18nKey(translate("meeting.start.button"));
				start = true;
			} else {
				joinButton.setI18nKey(translate("meeting.join.button"));
			}
		} else if(!running) {
			flc.contextPut("notStarted", Boolean.TRUE);
			joinButton.setEnabled(false);
		} else {
			flc.contextPut("notStarted", Boolean.FALSE);
			joinButton.setEnabled(!readOnly);
		}

		joinButton.setPrimary(joinButton.isEnabled());
		boolean recordingEnabled = meeting.isRecord()
				&& joinButton.isVisible() && joinButton.isEnabled();
		acknowledgeRecordingEl.setVisible(recordingEnabled);
		boolean wasVisible = startRecordingEl.isVisible();
		startRecordingEl.setVisible(start && recordingEnabled);
		if(!wasVisible && startRecordingEl.isVisible()) {
			startRecordingEl.select(START_RECORDING_KEY, meeting.isRecordAutoStart());
		}
	}
	
	private void loadModel() {
		TeamsAttendee attendee = teamsService.getAttendee(getIdentity(), meeting);
		boolean hasRecording = teamsModule.isRecordingsEnabled() && meeting.isRecord();
		flc.contextPut("hasRecordings", Boolean.valueOf(hasRecording));
		if(hasRecording) {
			TeamsErrors errors = new TeamsErrors();
			List<TeamsRecording> recordings = teamsService.getRecordings(meeting, oauth2Tokens, getIdentity(), errors);
			List<TeamsRecordingRow> rows = new ArrayList<>(recordings.size());
			for(TeamsRecording recording:recordings) {
				if(recording.getStatus() == TeamsRecordingStatusEnum.DELETED) continue;
				
				TeamsRecordingRow row = forgeRow(recording, attendee);
				rows.add(row);
			}
			recordingTableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
	
	private TeamsRecordingRow forgeRow(TeamsRecording recording, TeamsAttendee attendee) {
		boolean published = recording.getStatus() == TeamsRecordingStatusEnum.AVAILABLE
				&& isPublishedForMe(recording, attendee);
		String name = meeting.getSubject();
		if(recording.getStartDate() != null) {
			name += " " + formatter.formatDate(recording.getStartDate());
		}
		TeamsRecordingRow row = new TeamsRecordingRow(name, recording, published);
		
		if(withPublish && administrator) {
			String recId = recording.getKey().toString();
			FormLink publishLink = uifactory.addFormLink("publish-".concat(recId),
					"publish", "publish.recording", tableEl);
			row.setPublishLink(publishLink);
			publishLink.setUserObject(row);
			publishLink.setAriaDialogOpener();
		}
		
		if(administrator) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		
		return row;
	}
	
	private boolean isPublishedForMe(TeamsRecording recording, TeamsAttendee attendee) {
		if(recording == null) return false;
		
		TeamsRecordingsPublishedRoles[] publishTo = recording.getPublishToEnum();
		if(TeamsRecordingsPublishedRoles.has(publishTo, TeamsRecordingsPublishedRoles.none)) {
			return false;
		}
		if(guest) {
			return TeamsRecordingsPublishedRoles.has(publishTo, TeamsRecordingsPublishedRoles.guest);
		}
		if(attendee != null && TeamsRecordingsPublishedRoles.has(publishTo, TeamsRecordingsPublishedRoles.all)) {
			return true;
		}
		if(administrator || moderator) {
			return TeamsRecordingsPublishedRoles.has(publishTo, TeamsRecordingsPublishedRoles.coach);
		}
		return TeamsRecordingsPublishedRoles.has(publishTo, TeamsRecordingsPublishedRoles.participant);
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof TeamsMeetingEvent ace && ace.getMeetingKey() != null
				&& ace.getMeetingKey().equals(meeting.getKey())) {
			reloadButtonsAndStatus();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(acknowledgeRecordingEl != null) {
			acknowledgeRecordingEl.clearError();
			if(acknowledgeRecordingEl.isVisible()
					&& acknowledgeRecordingEl.isEnabled() && !acknowledgeRecordingEl.isAtLeastSelected(1)) {
				acknowledgeRecordingEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deleteConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT
					&& deleteConfirmationCtrl.getUserObject() instanceof TeamsRecordingRow row) {
				doDeleteRecording(row);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(publishCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			publishCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source
				|| publishCalloutCtrl == source || docEditorCtrl == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(publishCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(publishCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		publishCalloutCtrl = null;
		toolsCalloutCtrl = null;
		docEditorCtrl = null;
		publishCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(joinButton == source) {
			if(validateFormLogic(ureq)) {
				doJoin(ureq);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(CMD_OPEN_RECORDINGS.equals(se.getCommand())) {
					TeamsRecordingRow row = recordingTableModel.getObject(se.getIndex());
					doOpenRecording(ureq, row);
				}
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof TeamsRecordingRow row) {
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, link, row);
			} else if(CMD_PUBLISH.equals(link.getCmd())) {
				doPublish(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doJoin(UserRequest ureq) {
		TeamsErrors errors = new TeamsErrors();
		
		Identity id = guest ? null : getIdentity();
		boolean presenter = (administrator || moderator);
		Boolean autoStartRecording = null;
		if(teamsModule.isRecordingsEnabled() && meeting.isRecord() && startRecordingEl.isVisible()) {
			autoStartRecording = startRecordingEl.isAtLeastSelected(1);
		}
		meeting = teamsService.joinMeeting(meeting, id, presenter, guest, autoStartRecording, oauth2Tokens, errors);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
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
        	CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, meetingOres);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(joinUrl));
			
			if (!guest) {
				Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
				teamsService.setUserConformanceDecisionById(meeting.getKey(), guiPrefs, acknowledgeRecordingEl.isKeySelected("agree"));
			}
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}
	
	private void doOpenRecording(UserRequest ureq, TeamsRecordingRow row) {
		VFSMetadata recordingMetadata = row.getRecording().getRecordingMetadata();
		if(recordingMetadata == null) return;
		
		VFSItem recordingItem = vfsRepositoryService.getItemFor(recordingMetadata);
		if(recordingItem instanceof VFSLeaf recordingLeaf) {
		
			DocEditorConfigs configs = DocEditorConfigs.builder()
					.withMode(DocEditor.Mode.VIEW)
					.withDownloadEnabled(true)
					.build(recordingLeaf);
	
			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW).getController();
			listenTo(docEditorCtrl);
		}
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, TeamsRecordingRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl); 

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doTogglePermanent(TeamsRecordingRow recordingRow) {
		TeamsRecording recording = teamsService.getRecording(recordingRow.getRecording().getKey());
		if(recording != null) {
			recording.setPermanent(recording.getPermanent() == null || !recording.getPermanent().booleanValue());
			teamsService.updateRecording(recording);
			dbInstance.commit();
		}
		loadModel();
	}
	
	private void doPublish(UserRequest ureq, FormLink link, TeamsRecordingRow row) {
		publishCtrl = new PublishRecordingController(ureq, getWindowControl(), row);
		listenTo(publishCtrl); 

		publishCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				publishCtrl.getInitialComponent(), link, "", true, "");
		listenTo(publishCalloutCtrl);
		publishCalloutCtrl.activate();
	}
	
	private void doConfirmDeleteRecording(UserRequest ureq, TeamsRecordingRow row) {
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("confirm.delete.recording.message"), null, translate("delete"), ButtonType.danger);
		deleteConfirmationCtrl.setUserObject(row);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("confirm.delete.recording"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteRecording(TeamsRecordingRow row) {
		teamsService.deleteRecording(row.getRecording());
		dbInstance.commit();
		loadModel();
		
		ThreadLocalUserActivityLogger.log(TeamsLoggingAction.TEAMS_RECORDING_DELETE, getClass(), LoggingResourceable.wrap(row.getRecording()));
	}
	
	private class ToolsController extends BasicController {

		private final Link openLink;
		private final Link deleteLink;
		private final Link permanentLink;
		
		private final TeamsRecordingRow recordingRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, TeamsRecordingRow recordingRow) {
			super(ureq, wControl);
			this.recordingRow = recordingRow;
			
			VelocityContainer mainVC = createVelocityContainer("recording_tools");
			
			openLink = LinkFactory.createLink("open", "open", getTranslator(), mainVC, this, Link.LINK);
			openLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_play");
			
			Boolean permanent = recordingRow.getPermanent();
			boolean flagged = permanent == null || !permanent.booleanValue();
			String permanentI18nKey = flagged ? "mark.as.permanent" : "mark.as.not.permanent";
			permanentLink = LinkFactory.createLink(permanentI18nKey, "permanent", getTranslator(), mainVC, this, Link.LINK);
			permanentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(openLink == source) {
				doOpenRecording(ureq, recordingRow);
			} else if(permanentLink == source) {
				doTogglePermanent(recordingRow);
			} else if(deleteLink == source) {
				doConfirmDeleteRecording(ureq, recordingRow);
			}
		}
	}
}
