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
package org.olat.modules.adobeconnect.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.group.BusinessGroup;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.ui.AdobeConnectMeetingTableModel.ACMeetingsCols;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectEditMeetingsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink addMeetingButton;
	private AdobeConnectMeetingTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private EditAdobeConnectMeetingController editMeetingCtlr;
	
	private final boolean readOnly;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final AdobeConnectMeetingDefaultConfiguration configuration;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectEditMeetingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group,
			AdobeConnectMeetingDefaultConfiguration configuration, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.readOnly = readOnly;
		this.entry = entry;
		this.subIdent = subIdentifier;
		this.businessGroup = group;
		this.configuration = configuration;
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!readOnly) {
			addMeetingButton = uifactory.addFormLink("add.meeting", formLayout, Link.BUTTON);
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.name));
		if(!adobeConnectModule.isSingleMeetingMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.end));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new AdobeConnectMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("no.meeting.configured", null, "o_icon_calendar");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "adobe-connect-edit-meetings-list");
	}
	
	public void updateModel() {
		List<AdobeConnectMeeting> meetings = adobeConnectManager.getMeetings(entry, subIdent, businessGroup);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);	
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editMeetingCtlr) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			} else if(event instanceof AdobeConnectErrorEvent) {
				updateModel();
				showError(((AdobeConnectErrorEvent)event).getErrors());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				AdobeConnectMeeting meeting = (AdobeConnectMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMeetingCtlr);
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(cmc);
		editMeetingCtlr = null;
		confirmDelete = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMeetingButton == source) {
			doAddMeeting(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditMeeting(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddMeeting(UserRequest ureq) {
		if(guardModalController(editMeetingCtlr)) return;
		
		editMeetingCtlr = new EditAdobeConnectMeetingController(ureq, getWindowControl(), entry, subIdent, businessGroup, configuration);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditMeeting(UserRequest ureq, AdobeConnectMeeting meeting) {
		if(guardModalController(editMeetingCtlr)) return;
		
		editMeetingCtlr = new EditAdobeConnectMeetingController(ureq, getWindowControl(), meeting, configuration);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, AdobeConnectMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getName() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(AdobeConnectMeeting meeting) {
		AdobeConnectErrors errors = new AdobeConnectErrors();
		adobeConnectManager.deleteMeeting(meeting, errors);
		updateModel();
		
		if(errors.hasErrors()) {
			showError(errors);
		} else {
			showInfo("meeting.deleted");
		}
	}
	
	private void showError(AdobeConnectErrors errors) {
		getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
	}
}
