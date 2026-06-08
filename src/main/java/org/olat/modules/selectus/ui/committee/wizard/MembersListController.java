/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;
import org.olat.modules.selectus.ui.committee.wizard.MembersListDataModel.MCols;

/**
 * 
 * Initial date: 23 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersListController extends FormBasicController {

	private FlexiTableElement tableEl;
	private MembersListDataModel tableModel;

	private CloseableModalController cmc;
	private MemberEditController memberCtrl;
	
	private final String[] availableRoles;
	private List<CommitteeMember> membersToComplete;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public MembersListController(UserRequest ureq, WindowControl wControl, Form rootForm, List<CommitteeMember> membersToComplete, String[] availableRoles) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_list", rootForm);
		setTranslator(userManager.getPropertyHandlerTranslator(Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale(), getTranslator())));
		this.availableRoles = availableRoles;
		this.membersToComplete = new ArrayList<>(membersToComplete);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PositionCommitteeController.formIdentifyer, true);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MCols.ok, new CommitteeMemberStatusCellRenderer()));
		DefaultFlexiColumnModel nameCol = new DefaultFlexiColumnModel(MCols.name);
		nameCol.setCellRenderer(new ValidationMandatoryRenderer());
		columnsModel.addFlexiColumnModel(nameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MCols.role));

		RecruitingTableOption userPropertiesOption = recruitingModule.getTableCommitteeUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			boolean visible = userPropertiesOption == RecruitingTableOption.enabled;
			
			int i = 0;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null) {
					continue;
				}

				int colIndex = PositionCommitteeController.USER_PROP_OFFSET + i++;
				boolean isMandatoryField = userManager.isMandatoryUserProperty(PositionCommitteeController.formIdentifyer, userPropertyHandler);
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, true, userPropertyHandler.getName());
				col.setCellRenderer(new ValidationUserPropertyRenderer(userPropertyHandler, isMandatoryField, getLocale(), dbInstance));
				columnsModel.addFlexiColumnModel(col);
			}
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MCols.institution));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("skip", MCols.skip.ordinal(), "toggleskip",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("skip"), "skip"),
						new StaticFlexiCellRenderer(translate("unskip"), "unskip"))));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", MCols.edit.ordinal(), "edit",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("edit"), "edit"),
						null)));
		
		tableModel = new MembersListDataModel(userPropertyHandlers, columnsModel, getTranslator(), getLocale());
		tableModel.setObjects(membersToComplete);
		tableModel.validateRows();
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeListener(this);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(CommitteeMember member:tableModel.getObjects()) {
			if(member.getStatus() == null || member.getStatus() == CommitteeMemberStatus.notValid) {
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(memberCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				CommitteeMember member = memberCtrl.getCommitteeMember();
				tableModel.validate(member);
				tableEl.reset(false, false, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(memberCtrl);
		removeAsListenerAndDispose(cmc);
		memberCtrl = null;
		cmc = null;
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("skip".equals(se.getCommand())) {
					CommitteeMember member = tableModel.getObject(se.getIndex());
					member.setStatus(CommitteeMemberStatus.skipped);
					tableEl.reloadData();
				} else if("unskip".equals(se.getCommand())) {
					CommitteeMember member = tableModel.getObject(se.getIndex());
					member.setStatus(null);
					tableModel.validate(member);
					tableEl.reloadData();
				} else if("edit".equals(se.getCommand())) {
					CommitteeMember member = tableModel.getObject(se.getIndex());
					doEdit(ureq, member);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEdit(UserRequest ureq, CommitteeMember member) {
		if(guardModalController(memberCtrl)) return;
		
		memberCtrl = new MemberEditController(ureq, getWindowControl(), member, tableModel, availableRoles);
		listenTo(memberCtrl);
		cmc = new CloseableModalController(getWindowControl(), "c", memberCtrl.getInitialComponent(), translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
}