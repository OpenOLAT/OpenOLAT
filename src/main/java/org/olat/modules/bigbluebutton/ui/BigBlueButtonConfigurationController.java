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

import java.util.Collection;
import java.util.List;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.manager.BigBlueButtonNativeRecordingsHandler;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonConfigurationServersTableModel.ConfigServerCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonConfigurationController extends FormBasicController {

	private static final String FOR_COURSES_KEY = "courses";
	private static final String FOR_APPOINTMENTS_KEY = "appointments";
	private static final String FOR_GROUPS_KEY = "groups";
	private static final String FOR_CHATEXAMS_KEY = "chatexams";
	private static final String FOR_LECTURES_KEY = "lectures";
	
	private static final String[] ENABLED_KEY = new String[]{ "on" };
	
	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private MultipleSelectionElement permanentForEl;
	private MultipleSelectionElement avatarEl;
	private SingleSelection recordingsHandlerEl;
	private MultipleSelectionElement recordingPermanentEl;
	private FormLayoutContainer meetingDeletionCont;
	private TextElement meetingDeletionDaysEl;
	private TextElement slidesUploadLimitEl;
	
	private FormLink addServerButton;
	private FlexiTableElement serversTableEl;
	private BigBlueButtonConfigurationServersTableModel serversTableModel;
	
	private CloseableModalController cmc;
	private EditBigBlueButtonServerController editServerCtlr; 
	private ConfirmDeleteServerController confirmDeleteServerCtrl;
	
	private final List<BigBlueButtonRecordingsHandler> recordingsHandlers;
	
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		recordingsHandlers = bigBlueButtonManager.getRecordingsHandlers();
		
		initForm(ureq);
		updateUI();
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("bigbluebutton.title");
		setFormInfo("bigbluebutton.intro");
		setFormContextHelp("manual_admin/administration/BigBlueButton_module/#tab_config");
		formLayout.setElementCssClass("o_sel_bbb_admin_configuration");
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("bigbluebutton.module.enabled", formLayout, ENABLED_KEY, enabledValues);
		moduleEnabled.select(ENABLED_KEY[0], bigBlueButtonModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues forPK = new SelectionValues();
		forPK.add(SelectionValues.entry(FOR_COURSES_KEY, translate("bigbluebutton.module.enabled.for.courses")));
		forPK.add(SelectionValues.entry(FOR_LECTURES_KEY, translate("bigbluebutton.module.enabled.for.lectures")));
		forPK.add(SelectionValues.entry(FOR_APPOINTMENTS_KEY, translate("bigbluebutton.module.enabled.for.appointments")));
		forPK.add(SelectionValues.entry(FOR_GROUPS_KEY, translate("bigbluebutton.module.enabled.for.groups")));
		forPK.add(SelectionValues.entry(FOR_CHATEXAMS_KEY, translate("bigbluebutton.module.enabled.for.chat.exam")));
		enabledForEl = uifactory.addCheckboxesVertical("bigbluebutton.module.enabled.for", formLayout, forPK.keys(), forPK.values(), 1);
		enabledForEl.select(FOR_COURSES_KEY, bigBlueButtonModule.isCoursesEnabled());
		enabledForEl.select(FOR_LECTURES_KEY, bigBlueButtonModule.isLecturesEnabled());
		enabledForEl.select(FOR_APPOINTMENTS_KEY, bigBlueButtonModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_GROUPS_KEY, bigBlueButtonModule.isGroupsEnabled());
		enabledForEl.select(FOR_CHATEXAMS_KEY, bigBlueButtonModule.isChatExamsEnabled());
		
		permanentForEl = uifactory.addCheckboxesHorizontal("enable.permanent.meeting", formLayout, ENABLED_KEY, enabledValues);
		permanentForEl.select(ENABLED_KEY[0], bigBlueButtonModule.isPermanentMeetingEnabled());
		
		avatarEl = uifactory.addCheckboxesHorizontal("enable.avatar", formLayout, ENABLED_KEY, enabledValues);
		avatarEl.select(ENABLED_KEY[0], bigBlueButtonModule.isAvatarEnabled());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigServerCols.url));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigServerCols.enabled));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigServerCols.manualOnly));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		serversTableModel = new BigBlueButtonConfigurationServersTableModel(columnsModel, getLocale());
		
		serversTableEl = uifactory.addTableElement(getWindowControl(), "servers", serversTableModel, 10, false, getTranslator(), formLayout);
		serversTableEl.setCustomizeColumns(false);
		serversTableEl.setNumOfRowsEnabled(false);
		serversTableEl.setLabel("bigbluebutton.servers", null);
		serversTableEl.setEmptyTableMessageKey("bigbluebutton.servers.empty");
		
		addServerButton = uifactory.addFormLink("add.server", formLayout, Link.BUTTON);
		
		SelectionValues handlersKeyPairs = new SelectionValues();
		for(BigBlueButtonRecordingsHandler recordingsHandler:recordingsHandlers) {
			handlersKeyPairs.add(SelectionValues.entry(recordingsHandler.getId(), recordingsHandler.getName(getLocale())));
		}
		recordingsHandlerEl = uifactory.addDropdownSingleselect("bigbluebutton.recording.handler", formLayout,
				handlersKeyPairs.keys(), handlersKeyPairs.values());
		recordingsHandlerEl.addActionListener(FormEvent.ONCHANGE);
		String selectedHandlerId = bigBlueButtonModule.getRecordingHandlerId();
		if(handlersKeyPairs.containsKey(selectedHandlerId)) {
			recordingsHandlerEl.select(selectedHandlerId, true);
		} else {
			recordingsHandlerEl.select(BigBlueButtonNativeRecordingsHandler.NATIVE_RECORDING_HANDLER_ID, true);
		}
		
		recordingPermanentEl = uifactory.addCheckboxesHorizontal("permanent.rec", "bigbluebutton.recording.permanent", formLayout,
				ENABLED_KEY, new String[] { "" });
		if(bigBlueButtonModule.isRecordingsPermanent()) {
			recordingPermanentEl.select(ENABLED_KEY[0], true);
		}
		
		meetingDeletionCont = FormLayoutContainer.createButtonLayout("auto.delete", getTranslator());
		meetingDeletionCont.setLabel("meeting.deletion.auto", null);
		meetingDeletionCont.setElementCssClass("o_inline_cont");
		meetingDeletionCont.setRootForm(mainForm);
		formLayout.add(meetingDeletionCont);
		String meetingDeletionDays = bigBlueButtonModule.getMeetingDeletionDays() != null? bigBlueButtonModule.getMeetingDeletionDays().toString(): null;
		meetingDeletionDaysEl = uifactory.addTextElement("meeting.deletion.auto", null, 5, meetingDeletionDays, meetingDeletionCont);
		meetingDeletionDaysEl.setDisplaySize(5);
		
		StaticTextElement autoDeletionDaysEl = uifactory.addStaticTextElement("meeting.deletion.days", null, translate("meeting.deletion.days"),meetingDeletionCont);
		autoDeletionDaysEl.setDomWrapperElement(DomWrapperElement.span);
		
		Integer maxSize = bigBlueButtonModule.getMaxUploadSize();
		String maxSizeStr = maxSize == null ? null : maxSize.toString();
		slidesUploadLimitEl = uifactory.addTextElement("slides.upload.limit", 8, maxSizeStr, formLayout);
		slidesUploadLimitEl.setMandatory(true);

		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		permanentForEl.setVisible(enabled);
		enabledForEl.setVisible(enabled);
		serversTableEl.setVisible(enabled);
		addServerButton.setVisible(enabled);
		recordingsHandlerEl.setVisible(enabled);
		slidesUploadLimitEl.setVisible(enabled);
		
		boolean allowPermanentRecordings = false;
		for(BigBlueButtonRecordingsHandler recordingsHandler:recordingsHandlers) {
			if(recordingsHandler.getId().equals(recordingsHandlerEl.getSelectedKey())) {
				allowPermanentRecordings = recordingsHandler.allowPermanentRecordings();
			}
		}
		recordingPermanentEl.setVisible(enabled && allowPermanentRecordings);
	}
	
	private void loadModel() {
		List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
		serversTableModel.setObjects(servers);
		serversTableEl.reset(true, true, true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		serversTableEl.clearError();
		recordingsHandlerEl.clearError();
		if(moduleEnabled.isAtLeastSelected(1)) {
			if(serversTableModel.getRowCount() == 0) {
				serversTableEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
			
			if(!recordingsHandlerEl.isOneSelected()) {
				recordingsHandlerEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		meetingDeletionCont.clearError();
		if(StringHelper.containsNonWhitespace(meetingDeletionDaysEl.getValue())) {
			try {
				int meetingDeletionDays = Integer.parseInt(meetingDeletionDaysEl.getValue());
				if (meetingDeletionDays < 0) {
					meetingDeletionCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				meetingDeletionCont.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		slidesUploadLimitEl.clearError();
		if(StringHelper.containsNonWhitespace(slidesUploadLimitEl.getValue())) {
			try {
				Integer.parseInt(slidesUploadLimitEl.getValue());
			} catch (NumberFormatException e) {
				slidesUploadLimitEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		} else {
			slidesUploadLimitEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editServerCtlr == source || confirmDeleteServerCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteServerCtrl);
		removeAsListenerAndDispose(editServerCtlr);
		removeAsListenerAndDispose(cmc);
		confirmDeleteServerCtrl = null;
		editServerCtlr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == moduleEnabled || recordingsHandlerEl == source) {
			updateUI();
		} else if(addServerButton == source) {
			addServer(ureq);
		} else if(serversTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditServer(ureq, serversTableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, serversTableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isSelected(0);
		bigBlueButtonModule.setEnabled(enabled);
		// update collaboration tools list
		if(enabled) {
			Collection<String> selectedFor = enabledForEl.getSelectedKeys();
			bigBlueButtonModule.setCoursesEnabled(selectedFor.contains(FOR_COURSES_KEY));
			bigBlueButtonModule.setLecturesEnabled(selectedFor.contains(FOR_LECTURES_KEY));
			bigBlueButtonModule.setAppointmentsEnabled(selectedFor.contains(FOR_APPOINTMENTS_KEY));
			bigBlueButtonModule.setGroupsEnabled(selectedFor.contains(FOR_GROUPS_KEY));
			bigBlueButtonModule.setChatExamsEnabled(selectedFor.contains(FOR_CHATEXAMS_KEY));
			bigBlueButtonModule.setAvatarEnabled(avatarEl.isAtLeastSelected(1));
			bigBlueButtonModule.setPermanentMeetingEnabled(permanentForEl.isAtLeastSelected(1));
			bigBlueButtonModule.setRecordingHandlerId(recordingsHandlerEl.getSelectedKey());
			Integer meetingDeletionDays = StringHelper.containsNonWhitespace(meetingDeletionDaysEl.getValue())
					? Integer.valueOf(meetingDeletionDaysEl.getValue())
					: null;
			bigBlueButtonModule.setMeetingDeletionDays(meetingDeletionDays);
			bigBlueButtonModule.setMaxUploadSize(Integer.valueOf(slidesUploadLimitEl.getValue()));
			bigBlueButtonModule.setRecordingsPermanent(recordingPermanentEl.isVisible() && recordingPermanentEl.isAtLeastSelected(1));
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
	
	private void addServer(UserRequest ureq) {
		if(guardModalController(editServerCtlr)) return;

		editServerCtlr = new EditBigBlueButtonServerController(ureq, getWindowControl());
		listenTo(editServerCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editServerCtlr.getInitialComponent(),
				true, translate("add.server"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditServer(UserRequest ureq, BigBlueButtonServer server) {
		if(guardModalController(editServerCtlr)) return;

		editServerCtlr = new EditBigBlueButtonServerController(ureq, getWindowControl(), server);
		listenTo(editServerCtlr);
		
		String title = translate("edit.server", server.getUrl());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editServerCtlr.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonServer server) {
		if(guardModalController(confirmDeleteServerCtrl)) return;

		confirmDeleteServerCtrl = new ConfirmDeleteServerController(ureq, getWindowControl(), server);
		listenTo(confirmDeleteServerCtrl);
		
		String title = translate("confirm.delete.server.title", server.getUrl());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteServerCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
