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
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.member.ChangeApplyToEnum;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.modules.curriculum.ui.member.ConfirmationMembershipEnum;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.wizard.RightsCurriculumElementsTableModel.RightsElementsCols;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RightsController extends StepFormBasicController {

	private static final String CMD_ADD = "add";
	
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	private DateChooser confirmUntilEl;
	private SingleSelection confirmationByEl;
	private SingleSelection confirmationTypeEl;
	
	private FlexiTableElement tableEl;
	private RightsCurriculumElementsTableModel tableModel;
	
	private final CurriculumRoles roleToModify;
	private final MembersContext membersContext;
	private final List<CurriculumElement> curriculumElements;

	private CloseableCalloutWindowController calloutCtrl;
	private AddMembershipCalloutController addMembershipCtrl;

	public RightsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.membersContext = membersContext;
		roleToModify = membersContext.getRoleToModify();
		curriculumElements = new ArrayList<>(membersContext.getDescendants());
		curriculumElements.add(0, membersContext.getCurriculumElement());
		
		initForm(ureq);
		loadModel();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initSettingsForm(formLayout);
		initTableForm(formLayout);
	}
	
	private void initSettingsForm(FormItemContainer formLayout) {
		SelectionValues confirmationPK = new SelectionValues();
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITH.name(), translate("confirmation.membership.with"),
				translate("confirmation.membership.with.desc"), "o_icon o_icon_timelimit_end", null, true));
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITHOUT.name(), translate("confirmation.membership.without"),
				translate("confirmation.membership.without.desc"), "o_icon o_icon_activate", null, true));
		confirmationTypeEl = uifactory.addCardSingleSelectHorizontal("confirmation.membership", formLayout,
				confirmationPK.keys(), confirmationPK.values(), confirmationPK.descriptions(), confirmationPK.icons());
		confirmationTypeEl.addActionListener(FormEvent.ONCLICK);
		confirmationTypeEl.select(ConfirmationMembershipEnum.WITH.name(), true);
		
		// confirmation by
		SelectionValues confirmationByPK = new SelectionValues();
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("confirmation.membership.by.admin")));
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.PARTICIPANT.name(), translate("confirmation.membership.by.participant")));
		confirmationByEl = uifactory.addRadiosVertical("confirmation.membership.by", formLayout,
				confirmationByPK.keys(), confirmationByPK.values());
		confirmationByEl.select(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), true);
		
		// confirmation until
		confirmUntilEl = uifactory.addDateChooser("confirmation.until", "confirmation.until", null, formLayout);
		
		// apply to
		SelectionValues applyToPK = new SelectionValues();
		String applyContainedI18n = membersContext.getDescendants().size() <= 1
				? "apply.membership.to.contained.element" : "apply.membership.to.contained.elements";
		String applyContained = translate(applyContainedI18n,
				membersContext.getCurriculumElement().getDisplayName(), Integer.toString(membersContext.getDescendants().size()));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), applyContained));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.individual.elements")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to", "apply.membership.to", formLayout, applyToPK.keys(), applyToPK.values());
		applyToEl.addActionListener(FormEvent.ONCHANGE);
		applyToEl.select(ChangeApplyToEnum.CONTAINED.name(), true);
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
	}
	
	private void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RightsElementsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RightsElementsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RightsElementsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RightsElementsCols.externalId));

		String i18nLabel = "role.".concat(roleToModify.name());
		GroupMembershipStatusRenderer statusRenderer = new GroupMembershipStatusRenderer(getLocale());
		DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, RightsElementsCols.roleToModify.ordinal(), null, false, null, statusRenderer);
		col.setDefaultVisible(true);
		col.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(col);

		tableModel = new RightsCurriculumElementsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "editRolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
		tableEl.setFormLayout("0_12");
	}
	
	private void updateUI() {
		boolean withConfirmation = confirmationTypeEl.isOneSelected()
				&& ConfirmationMembershipEnum.WITH.name().equals(confirmationTypeEl.getSelectedKey());
		confirmationByEl.setVisible(withConfirmation);
		confirmUntilEl.setVisible(withConfirmation);
		
		boolean individualElements = applyToEl.isOneSelected()
				&& ChangeApplyToEnum.valueOf(applyToEl.getSelectedKey()) == ChangeApplyToEnum.CURRENT;
		adminNoteEl.setVisible(!individualElements);
		tableEl.setVisible(individualElements);
		
		GroupMembershipStatus nextStatus = getNextStatus();
		List<RightsCurriculumElementRow> rows = tableModel.getObjects();
		for(RightsCurriculumElementRow row:rows) {
			MembershipModification mod = row.getModification();
			if(mod != null) {
				MembershipModification cMod = new MembershipModification(roleToModify, row.getCurriculumElement(), nextStatus,
						null, null, null, false, mod.adminNote());
				row.setModification(cMod);
			}
		}
	}
	
	private void loadModel() {
		List<RightsCurriculumElementRow> rows = new ArrayList<>();
		Map<Long,RightsCurriculumElementRow> rowsMap = new HashMap<>();
		
		for(CurriculumElement element:curriculumElements) {
			RightsCurriculumElementRow row = new RightsCurriculumElementRow(element);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			forgeLinks(row);
		}
		
		for(RightsCurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(rowsMap.get(row.getParentKey()));
			}
		}
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeLinks(RightsCurriculumElementRow row) {
		String id = "model-" + roleToModify + "-" + row.getKey();
		flc.remove(id);
		
		FormLink addLink = uifactory.addFormLink(id, CMD_ADD, "add", tableEl, Link.LINK);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_plus");
		addLink.setUserObject(row);
		row.setAddButton(addLink);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setModification(addMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addMembershipCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		addMembershipCtrl = null;
		calloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(confirmationTypeEl == source || applyToEl == source) {
			updateUI();
		} else if(source instanceof FormLink link && CMD_ADD.equals(link.getCmd())
				&& link.getUserObject() instanceof RightsCurriculumElementRow row) {
			doAddMembership(ureq, link, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationTypeEl.clearError();
		if(!confirmationTypeEl.isOneSelected()) {
			confirmationTypeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		confirmationByEl.clearError();
		if(!confirmationByEl.isOneSelected()) {
			confirmationByEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		applyToEl.clearError();
		if(!applyToEl.isOneSelected()) {
			applyToEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		ConfirmationMembershipEnum confirmation = ConfirmationMembershipEnum.valueOf(confirmationTypeEl.getSelectedKey()); 
		GroupMembershipStatus nextStatus = getNextStatus();
		
		Date confirmationUntil = null;
		ConfirmationByEnum confirmationBy = null;
		if(confirmation == ConfirmationMembershipEnum.WITH) {
			confirmationUntil = confirmUntilEl.getDate();
			confirmationBy = ConfirmationByEnum.valueOf(confirmationByEl.getSelectedKey());
		}
		
		String adminNote = adminNoteEl.isVisible() ? adminNoteEl.getValue() : null;

		// Apply a modification per element
		List<MembershipModification> modifications = new ArrayList<>();
		if(tableEl.isVisible()) {
			List<RightsCurriculumElementRow> rows = tableModel.getObjects();
			for(RightsCurriculumElementRow row:rows) {
				MembershipModification partialModification = row.getModification();
				if(partialModification != null) {
					MembershipModification modification = new MembershipModification(partialModification.role(), partialModification.curriculumElement(),
							nextStatus, confirmation, confirmationBy, confirmationUntil,
							false, partialModification.adminNote());
					modifications.add(modification);
				}
			}
		} else {
			List<CurriculumElement> elements = membersContext.getAllCurriculumElements();
			for(CurriculumElement element:elements) {
				MembershipModification modification = new MembershipModification(roleToModify, element,
						nextStatus, confirmation, confirmationBy, confirmationUntil,
						false, adminNote);
				modifications.add(modification);
			}
		}
		membersContext.setModifications(modifications);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private GroupMembershipStatus getNextStatus() {
		ConfirmationMembershipEnum confirmation = ConfirmationMembershipEnum.valueOf(confirmationTypeEl.getSelectedKey());
		return (confirmation == ConfirmationMembershipEnum.WITH)
				? GroupMembershipStatus.reservation : GroupMembershipStatus.active;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddMembership(UserRequest ureq, FormLink link, RightsCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		addMembershipCtrl = new AddMembershipCalloutController(ureq, getWindowControl(), roleToModify, curriculumElement);
		listenTo(addMembershipCtrl);
		
		String title = translate("add.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void setModification(MembershipModification mod) {
		GroupMembershipStatus nextStatus = getNextStatus();
		MembershipModification modification = new MembershipModification(mod.role(), mod.curriculumElement(), nextStatus,
				null, null, null, mod.toDescendants(), mod.adminNote());

		RightsCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		row.setModification(modification);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				RightsCurriculumElementRow obj = tableModel.getObject(i);
				if(tableModel.isParentOf(row, obj)) {
					obj.setModification(modification);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}
}
