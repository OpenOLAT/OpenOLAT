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

import java.util.List;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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

	private static final String[] FOR_KEYS = { "courses", "appointments", "groups" };
	private static final String[] ENABLED_KEY = new String[]{ "on" };
	
	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private MultipleSelectionElement permanentForEl;
	private MultipleSelectionElement avatarEl;
	private SingleSelection recordingsHandlerEl;
	private MultipleSelectionElement recordingPermanentEl;
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
		setFormContextHelp("manual_admin/administration/BigBlueButton_module/");
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("bigbluebutton.module.enabled", formLayout, ENABLED_KEY, enabledValues);
		moduleEnabled.select(ENABLED_KEY[0], bigBlueButtonModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String[] forValues = new String[] {
			translate("bigbluebutton.module.enabled.for.courses"), translate("bigbluebutton.module.enabled.for.appointments"),
			translate("bigbluebutton.module.enabled.for.groups")
		};
		enabledForEl = uifactory.addCheckboxesVertical("bigbluebutton.module.enabled.for", formLayout, FOR_KEYS, forValues, 1);
		enabledForEl.select(FOR_KEYS[0], bigBlueButtonModule.isCoursesEnabled());
		enabledForEl.select(FOR_KEYS[1], bigBlueButtonModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_KEYS[2], bigBlueButtonModule.isGroupsEnabled());
		
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
		serversTableModel = new BigBlueButtonConfigurationServersTableModel(columnsModel);
		
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
				serversTableEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(!recordingsHandlerEl.isOneSelected()) {
				recordingsHandlerEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		slidesUploadLimitEl.clearError();
		if(StringHelper.containsNonWhitespace(slidesUploadLimitEl.getValue())) {
			try {
				Integer.parseInt(slidesUploadLimitEl.getValue());
			} catch (NumberFormatException e) {
				slidesUploadLimitEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			slidesUploadLimitEl.setErrorKey("form.legende.mandatory", null);
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
			bigBlueButtonModule.setCoursesEnabled(enabledForEl.isSelected(0));
			bigBlueButtonModule.setAppointmentsEnabled(enabledForEl.isSelected(1));
			bigBlueButtonModule.setAvatarEnabled(avatarEl.isAtLeastSelected(1));
			bigBlueButtonModule.setGroupsEnabled(enabledForEl.isSelected(2));
			bigBlueButtonModule.setPermanentMeetingEnabled(permanentForEl.isAtLeastSelected(1));
			bigBlueButtonModule.setRecordingHandlerId(recordingsHandlerEl.getSelectedKey());
			bigBlueButtonModule.setMaxUploadSize(Integer.valueOf(slidesUploadLimitEl.getValue()));
			bigBlueButtonModule.setRecordingsPermanent(recordingPermanentEl.isVisible() && recordingPermanentEl.isAtLeastSelected(1));
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
	
	private void addServer(UserRequest ureq) {
		if(guardModalController(editServerCtlr)) return;

		editServerCtlr = new EditBigBlueButtonServerController(ureq, getWindowControl());
		listenTo(editServerCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editServerCtlr.getInitialComponent(),
				true, translate("add.server"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditServer(UserRequest ureq, BigBlueButtonServer server) {
		if(guardModalController(editServerCtlr)) return;

		editServerCtlr = new EditBigBlueButtonServerController(ureq, getWindowControl(), server);
		listenTo(editServerCtlr);
		
		String title = translate("edit.server", new String[] { server.getUrl() });
		cmc = new CloseableModalController(getWindowControl(), "close", editServerCtlr.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonServer server) {
		if(guardModalController(confirmDeleteServerCtrl)) return;

		confirmDeleteServerCtrl = new ConfirmDeleteServerController(ureq, getWindowControl(), server);
		listenTo(confirmDeleteServerCtrl);
		
		String title = translate("confirm.delete.server.title", new String[] { server.getUrl() });
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteServerCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
