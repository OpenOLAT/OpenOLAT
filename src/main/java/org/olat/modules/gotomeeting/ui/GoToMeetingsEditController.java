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
package org.olat.modules.gotomeeting.ui;

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
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingsEditController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink addTrainingButton;
	private GoToMeetingTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private EditTrainingController editTrainingController;
	
	private final boolean readOnly;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToMeetingsEditController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.entry = entry;
		this.readOnly = readOnly;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!readOnly) {
			addTrainingButton = uifactory.addFormLink("add.training", formLayout, Link.BUTTON);
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.name.i18nHeaderKey(), MeetingsCols.name.ordinal(), true, MeetingsCols.name.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start.i18nHeaderKey(), MeetingsCols.start.ordinal(), true, MeetingsCols.start.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end.i18nHeaderKey(), MeetingsCols.end.ordinal(), true, MeetingsCols.end.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.organizer.i18nHeaderKey(), MeetingsCols.organizer.ordinal(), true, MeetingsCols.organizer.name()));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new GoToMeetingTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		updateModel();
	}
	
	private void updateModel() {
		List<GoToMeeting> meetings = meetingMgr.getMeetings(GoToType.training, entry, subIdent, businessGroup);
		tableModel.setObjects(meetings);
		tableEl.reloadData();
		tableEl.reset();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editTrainingController == source) {
			if(event == Event.DONE_EVENT) {
				showInfo("training.scheduled");
			} else if(event instanceof GoToErrorEvent) {
				GoToErrorEvent gto = (GoToErrorEvent) event;
				showError(gto.getError());
			} 
			updateModel();
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				GoToMeeting meeting = (GoToMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void showError(GoToError error) {
		if(error.getError() == null) {
			showWarning("error.training.schedule.failed");
		} else {
			String i18nKey = error.getError().i18nKey();
			if(StringHelper.containsNonWhitespace(i18nKey)) {
				showWarning(i18nKey);
			} else {
				showWarning("error.training.schedule.failed");
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editTrainingController);
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(cmc);
		editTrainingController = null;
		confirmDelete = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTrainingButton == source) {
			doAddTraining(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					GoToMeeting meeting = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, meeting);
				} else if("edit".equals(se.getCommand())) {
					GoToMeeting meeting = tableModel.getObject(se.getIndex());
					doEditTraining(ureq, meeting);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, GoToMeeting meeting) {
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, translate("delete"), confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doDelete(GoToMeeting meeting) {
		meetingMgr.delete(meeting);
		updateModel();
	}

	private void doAddTraining(UserRequest ureq) {
		if(guardModalController(editTrainingController)) return;
		
		editTrainingController = new EditTrainingController(ureq, getWindowControl(), entry, subIdent, businessGroup);
		listenTo(editTrainingController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editTrainingController.getInitialComponent(),
				true, translate("add.training"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditTraining(UserRequest ureq, GoToMeeting meeting) {
		if(guardModalController(editTrainingController)) return;
		
		editTrainingController = new EditTrainingController(ureq, getWindowControl(), meeting);
		listenTo(editTrainingController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editTrainingController.getInitialComponent(),
				true, translate("add.training"));
		cmc.activate();
		listenTo(cmc);
	}
}
