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

import jakarta.servlet.http.HttpSession;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.oauth.GetToResource;
import org.olat.modules.gotomeeting.oauth.GoToProvider;
import org.olat.modules.gotomeeting.ui.GoToOrganizerTableModel.OrganizerCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToOrganizerListAdminController extends FormBasicController implements Activateable2 {
	
	private FormLink reLogButton;
	private FormLink addOrganizerButton;
	private FlexiTableElement tableEl;
	private GoToOrganizerTableModel tableModel;

	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveOrganizer;
	private LoginOrganizerController loginOrganizerController;
	private EditOrganizerNameController editOrganizerNameController;
	
	@Autowired
	private GoToProvider goToProvider;
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
		reLogButton = uifactory.addFormLink("relog.organizer", formLayout, Link.BUTTON);
		reLogButton.setDomReplacementWrapperRequired(false);
		reLogButton.setIconLeftCSS("o_icon o_icon-fw o_icon_login");
		
		addOrganizerButton = uifactory.addFormLink("add.organizer", formLayout, Link.BUTTON);
		addOrganizerButton.setDomReplacementWrapperRequired(false);
		addOrganizerButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.firstName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.lastName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.owner, "owner"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.type, new GoToOrganizerTypeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.renewDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganizerCols.refresh.i18nHeaderKey(), OrganizerCols.refresh.ordinal(), "refresh",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("refresh.organizer"), "refresh"), null)));
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		Object returnVal = ureq.getUserSession().removeEntryFromNonClearedStore("GETGO_STATUS");
		if(returnVal instanceof Boolean) {
			processReturnValue((Boolean)returnVal);
		}
	}
	
	private void processReturnValue(Boolean returnVal) {
		if(Boolean.TRUE.equals(returnVal)) {
			showInfo("token.refreshed");
		} else {
			showWarning("error.code.unkown");
		}
		updateModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editOrganizerNameController == source || loginOrganizerController == source) {
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
		removeAsListenerAndDispose(editOrganizerNameController);
		removeAsListenerAndDispose(loginOrganizerController);
		removeAsListenerAndDispose(confirmRemoveOrganizer);
		removeAsListenerAndDispose(cmc);
		editOrganizerNameController = null;
		loginOrganizerController = null;
		confirmRemoveOrganizer = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addOrganizerButton == source) {
			doAddOrganizer(ureq);
		} else if(reLogButton == source) {
			doAuthenticateOrganizer(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("remove".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doConfirmRemove(ureq, organizer);
				} else if("owner".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doOpenOwner(ureq, organizer);
				} else if("refresh".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doRefresh(organizer);
				} else if("renew".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doRenewOrganizer(ureq, organizer);
				}  else if("edit".equals(se.getCommand())) {
					GoToOrganizer organizer = tableModel.getObject(se.getIndex());
					doEdit(ureq, organizer);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEdit(UserRequest ureq, GoToOrganizer organizer) {
		if(guardModalController(editOrganizerNameController)) return;
		
		editOrganizerNameController = new EditOrganizerNameController(ureq, getWindowControl(), organizer);
		listenTo(editOrganizerNameController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editOrganizerNameController.getInitialComponent(),
				true, translate("edit.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddOrganizer(UserRequest ureq) {
		if(guardModalController(loginOrganizerController)) return;
		
		loginOrganizerController = new LoginOrganizerController(ureq, getWindowControl());
		listenTo(loginOrganizerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), loginOrganizerController.getInitialComponent(),
				true, translate("add.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doRenewOrganizer(UserRequest ureq, GoToOrganizer organizer) {
		if(guardModalController(loginOrganizerController)) return;
		
		loginOrganizerController = new LoginOrganizerController(ureq, getWindowControl(), organizer);
		listenTo(loginOrganizerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), loginOrganizerController.getInitialComponent(),
				true, translate("renew.organizer"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doRefresh(GoToOrganizer organizer) {
		if(meetingMgr.refreshToken(organizer)) {
			logAudit("GoToOrganizer refreshed: " + organizer);
			showInfo("token.refreshed");
			updateModel();
		} else {
			showWarning("error.code.unkown");
		}
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
	
	private void doAuthenticateOrganizer(UserRequest ureq) {
		HttpSession session = ureq.getHttpReq().getSession();
		MediaResource redirectResource = new GetToResource(goToProvider, session);
		ureq.getDispatchResult().setResultingMediaResource(redirectResource);
	}
}
