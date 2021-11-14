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

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.ui.AdobeConnectMeetingTableModel.ACMeetingsCols;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectAdminMeetingsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private AdobeConnectMeetingTableModel tableModel;

	private DialogBoxController confirmDelete;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectAdminMeetingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "meetings_admin");
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.name));
		if(!adobeConnectModule.isSingleMeetingMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.end));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("resource", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACMeetingsCols.resource.i18nHeaderKey(), ACMeetingsCols.resource.ordinal(), "resource",
				true, ACMeetingsCols.resource.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));

		tableModel = new AdobeConnectMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "adobe-connect-admin-meetings-list");
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				AdobeConnectMeeting meeting = (AdobeConnectMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDelete);
		confirmDelete = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("resource".equals(se.getCommand())) {
					doOpenResource(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateModel() {
		List<AdobeConnectMeeting> meetings = adobeConnectManager.getAllMeetings();
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenResource(UserRequest ureq, AdobeConnectMeeting meeting) {
		if(meeting.getEntry() != null) {
			String businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath += "[CourseNode:" + meeting.getSubIdent() + "]";
			}
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if(meeting.getBusinessGroup() != null) {
			String businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
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
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		}
	}
}
