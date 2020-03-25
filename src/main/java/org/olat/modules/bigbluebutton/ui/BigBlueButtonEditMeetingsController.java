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
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRoles;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonEditMeetingsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink addMeetingButton;
	private BigBlueButtonMeetingTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private EditBigBlueButtonMeetingController editMeetingCtlr;
	
	private final boolean readOnly;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;

	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public BigBlueButtonEditMeetingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.readOnly = readOnly;
		this.entry = entry;
		this.subIdent = subIdentifier;
		this.businessGroup = group;
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.name));
		if(bigBlueButtonModule.isPermanentMeetingEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.template));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new BigBlueButtonMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.meeting.configured");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "bigbluebutton-connect-edit-meetings-list");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void updateModel() {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(entry, subIdent, businessGroup);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);	
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editMeetingCtlr) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonMeeting meeting = (BigBlueButtonMeeting)confirmDelete.getUserObject();
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
	protected void formOK(UserRequest ureq) {
		//
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
	

	private void doAddMeeting(UserRequest ureq) {
		if(guardModalController(editMeetingCtlr)) return;

		List<BigBlueButtonRoles> editionRoles= getPermittedRoles(ureq);
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				entry, subIdent, businessGroup, editionRoles);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditMeeting(UserRequest ureq, BigBlueButtonMeeting meeting) {
		if(guardModalController(editMeetingCtlr)) return;
		
		List<BigBlueButtonRoles> editionRoles= getPermittedRoles(ureq);
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				meeting, editionRoles);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private List<BigBlueButtonRoles> getPermittedRoles(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		
		List<BigBlueButtonRoles> editionRoles = new ArrayList<>();
		if(businessGroup != null) {
			if(roles.isAdministrator() || roles.isSystemAdmin()) {
				editionRoles.add(BigBlueButtonRoles.administrator);
			}
			if(roles.isAuthor() || roles.isLearnResourceManager()) {
				// global authors / LR-managers can use author templates also in groups
				editionRoles.add(BigBlueButtonRoles.author);
			}
			if(businessGroupService.isIdentityInBusinessGroup(getIdentity(), businessGroup)) {
				// all group user can choose the group templates (if they are allowed to create group online-meetings)
				editionRoles.add(BigBlueButtonRoles.group);
			}
		} else if(entry != null) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), roles, entry);
			if(roles.isAdministrator() || roles.isSystemAdmin()) {
				editionRoles.add(BigBlueButtonRoles.administrator);
			}
			if(reSecurity.isAuthor() || roles.isLearnResourceManager()) {
				editionRoles.add(BigBlueButtonRoles.author);
			}
			if(reSecurity.isEntryAdmin()) {
				editionRoles.add(BigBlueButtonRoles.owner);
			}
			if(reSecurity.isCourseCoach()) {
				editionRoles.add(BigBlueButtonRoles.coach);
			}
		}
		return editionRoles;
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getName() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(BigBlueButtonMeeting meeting) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteMeeting(meeting, errors);
		updateModel();
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			showInfo("meeting.deleted");
		}
	}
}
