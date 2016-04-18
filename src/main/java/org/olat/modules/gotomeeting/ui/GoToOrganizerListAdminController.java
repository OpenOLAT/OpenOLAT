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

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.ui.GoToOrganizerTableModel.OrganizerCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToOrganizerListAdminController extends FormBasicController {
	
	private FormLink addOrganizerButton;
	private FlexiTableElement tableEl;
	private GoToOrganizerTableModel tableModel;

	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveOrganizer;
	private EditOrganizerController addOrganizerController;
	private EditOrganizerController updateOrganizerController;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToOrganizerListAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "organizers");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addOrganizerButton = uifactory.addFormLink("add.organizer", formLayout, Link.BUTTON);
		addOrganizerButton.setDomReplacementWrapperRequired(false);
		addOrganizerButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.key.i18nHeaderKey(), OrganizerCols.key.ordinal(), true, OrganizerCols.key.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.firstName.i18nHeaderKey(), OrganizerCols.firstName.ordinal(), true, OrganizerCols.firstName.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.lastName.i18nHeaderKey(), OrganizerCols.lastName.ordinal(), true, OrganizerCols.lastName.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.email.i18nHeaderKey(), OrganizerCols.email.ordinal(), true, OrganizerCols.email.name()));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("owner", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.owner.i18nHeaderKey(), OrganizerCols.owner.ordinal(), "owner",
				true, OrganizerCols.owner.name(), renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.renewDate.i18nHeaderKey(), OrganizerCols.renewDate.ordinal(), true, OrganizerCols.renewDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("renew.organizer", translate("renew.organizer"), "renew"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.remove.i18nHeaderKey(), OrganizerCols.remove.ordinal(), "remove",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("remove"), "remove"), null)));
		
		tableModel = new GoToOrganizerTableModel(columnsModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "organizerList", tableModel, getTranslator(), formLayout);
		updateModel();
	}
	
	private void updateModel() {
		List<GoToOrganizer> organizers = meetingMgr.getOrganizers();
		tableModel.setObjects(organizers);
		tableEl.reloadData();
		tableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addOrganizerController == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(updateOrganizerController == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRemoveOrganizer == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				GoToOrganizer organizer = (GoToOrganizer)confirmRemoveOrganizer.getUserObject();
				doRemoveOrganizer(organizer);
				updateModel();
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(updateOrganizerController);
		removeAsListenerAndDispose(addOrganizerController);
		removeAsListenerAndDispose(confirmRemoveOrganizer);
		removeAsListenerAndDispose(cmc);
		updateOrganizerController = null;
		addOrganizerController = null;
		confirmRemoveOrganizer = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addOrganizerButton == source) {
			doAddOrganizer(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("renew".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doRenewOrganizer(ureq, organizer);
				} else if("remove".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doConfirmRemove(ureq, organizer);
				} else if("owner".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doOpenOwner(ureq, organizer);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddOrganizer(UserRequest ureq) {
		if(addOrganizerController != null) return;
		
		addOrganizerController = new EditOrganizerController(ureq, getWindowControl());
		listenTo(addOrganizerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addOrganizerController.getInitialComponent(),
				true, translate("add.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doRenewOrganizer(UserRequest ureq, GoToOrganizer organizer) {
		if(updateOrganizerController != null) return;
		
		updateOrganizerController = new EditOrganizerController(ureq, getWindowControl(), organizer);
		listenTo(updateOrganizerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), updateOrganizerController.getInitialComponent(),
				true, translate("renew.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmRemove(UserRequest ureq, GoToOrganizer organizer) {
		String confirmDeleteText = translate("confirm.remove.organizer", new String[]{ organizer.getOrganizerKey() });
		confirmRemoveOrganizer = activateYesNoDialog(ureq, translate("remove"), confirmDeleteText, confirmRemoveOrganizer);
		confirmRemoveOrganizer.setUserObject(organizer);
	}
	
	private void doRemoveOrganizer(GoToOrganizer organizer) {
		if(!meetingMgr.removeOrganizer(organizer)) {
			showWarning("warning.remove.organizer");
		}
	}
	
	private void doOpenOwner(UserRequest ureq, GoToOrganizer organizer) {
		if(organizer.getOwner() != null) {
			String businessPath = "[Identity:" + organizer.getOwner().getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
}
