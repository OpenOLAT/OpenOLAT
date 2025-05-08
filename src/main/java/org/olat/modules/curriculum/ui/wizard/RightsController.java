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
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
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
import org.olat.modules.curriculum.ui.member.NoteCalloutController;
import org.olat.modules.curriculum.ui.wizard.MembersContext.AccessInfos;
import org.olat.modules.curriculum.ui.wizard.RightsCurriculumElementsTableModel.RightsElementsCols;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RightsController extends StepFormBasicController {

	private static final String CMD_ADD = "add";
	private static final String CMD_NOTE = "note";
	private static final String ASSIGN_KEY = "assign";
	private static final String DONT_ASSIGN_KEY = "dont-assign";
	
	private TextElement adminNoteEl;
	private SingleSelection applyToEl;
	private DateChooser confirmUntilEl;
	private SingleSelection confirmationByEl;
	private SingleSelection confirmationTypeEl;
	private SingleSelection teacherAssignmentEl;
	
	private FlexiTableElement tableEl;
	private FormLayoutContainer tableCont;
	private RightsCurriculumElementsTableModel tableModel;
	
	private final boolean confirmationPossible;
	private final boolean allCurriculumElements;
	private final CurriculumRoles roleToModify;
	private final MembersContext membersContext;
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> curriculumElements;

	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private AddMembershipCalloutController addMembershipCtrl;

	public RightsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.membersContext = membersContext;
		roleToModify = membersContext.getRoleToModify();
		curriculumElement = membersContext.getCurriculumElement();
		curriculumElements = membersContext.getAllCurriculumElements();

		allCurriculumElements = membersContext.getSelectedOffer() != null;
		confirmationPossible = (roleToModify == CurriculumRoles.participant)
				&& membersContext.getSelectedOffer() == null;
		
		initForm(ureq);
		loadModel();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer settingsCont = uifactory.addDefaultFormLayout("settings", null, formLayout);
		settingsCont.setElementCssClass("o_sel_curriculum_element_member_rights");
		initSettingsForm(settingsCont);
		tableCont = uifactory.addDefaultFormLayout("elements", null, formLayout);
		tableCont.setFormTitle(translate("wizard.member.rights.elements"));
		initTableForm(tableCont);
	}
	
	private void initSettingsForm(FormItemContainer formLayout) {
		SelectionValues confirmationPK = new SelectionValues();
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITHOUT.name(), translate("confirmation.membership.without"),
				translate("confirmation.membership.without.desc"), "o_icon o_icon_check", null, true));
		confirmationPK.add(SelectionValues.entry(ConfirmationMembershipEnum.WITH.name(), translate("confirmation.membership.with"),
				translate("confirmation.membership.with.desc"), "o_icon o_icon_timelimit_end", null, true));
		confirmationTypeEl = uifactory.addCardSingleSelectHorizontal("confirmation.membership", formLayout,
				confirmationPK.keys(), confirmationPK.values(), confirmationPK.descriptions(), confirmationPK.icons());
		confirmationTypeEl.addActionListener(FormEvent.ONCLICK);
		confirmationTypeEl.select(ConfirmationMembershipEnum.WITHOUT.name(), true);
		confirmationTypeEl.setVisible(confirmationPossible);
		
		// confirmation by
		SelectionValues confirmationByPK = new SelectionValues();
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), translate("confirmation.membership.by.admin")));
		confirmationByPK.add(SelectionValues.entry(ConfirmationByEnum.PARTICIPANT.name(), translate("confirmation.membership.by.participant")));
		confirmationByEl = uifactory.addRadiosVertical("confirmation.membership.by", formLayout,
				confirmationByPK.keys(), confirmationByPK.values());
		confirmationByEl.select(ConfirmationByEnum.ADMINISTRATIVE_ROLE.name(), true);
		confirmationByEl.setVisible(confirmationPossible);
		
		// confirmation until
		confirmUntilEl = uifactory.addDateChooser("confirmation.until", "confirmation.until", null, formLayout);
		confirmUntilEl.setVisible(roleToModify == CurriculumRoles.participant);
		
		SpacerElement spacerEl = uifactory.addSpacerElement("confirm_spacer", formLayout, false);
		spacerEl.setVisible(confirmationPossible);
		
		// apply to
		SelectionValues applyToPK = new SelectionValues();
		String applyContainedI18n = membersContext.getDescendants().size() <= 1
				? "apply.membership.to.contained.element" : "apply.membership.to.contained.elements";
		String applyContained = translate(applyContainedI18n,
				StringHelper.escapeHtml(membersContext.getCurriculumElement().getDisplayName()), Integer.toString(membersContext.getDescendants().size()));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CONTAINED.name(), applyContained));
		applyToPK.add(SelectionValues.entry(ChangeApplyToEnum.CURRENT.name(), translate("apply.membership.to.individual.elements")));
		applyToEl = uifactory.addRadiosVertical("apply.membership.to", "apply.membership.to", formLayout, applyToPK.keys(), applyToPK.values());
		applyToEl.addActionListener(FormEvent.ONCHANGE);
		applyToEl.select(ChangeApplyToEnum.CONTAINED.name(), true);
		applyToEl.setVisible(!membersContext.getDescendants().isEmpty() && !allCurriculumElements);
		
		StaticTextElement applyInfosEl = uifactory.addStaticTextElement("apply.membership.to.alt", "apply.membership.to", applyContained, formLayout);
		applyInfosEl.setVisible(!membersContext.getDescendants().isEmpty() && allCurriculumElements);
		
		SelectionValues assignmentPK = new SelectionValues();
		assignmentPK.add(SelectionValues.entry(ASSIGN_KEY, translate("teacher.assignments.assign")));
		assignmentPK.add(SelectionValues.entry(DONT_ASSIGN_KEY, translate("teacher.assignments.none")));
		teacherAssignmentEl = uifactory.addRadiosHorizontal("teacher.assignments", "teacher.assignments", formLayout,
				assignmentPK.keys(), assignmentPK.values());
		if(roleToModify == CurriculumRoles.coach) {
			teacherAssignmentEl.select(ASSIGN_KEY, true);
		} else {
			teacherAssignmentEl.setVisible(false);
		}
		
		adminNoteEl = uifactory.addTextAreaElement("admin.note", "admin.note", 2000, 4, 32, false, false, false, "", formLayout);
		adminNoteEl.setVisible(roleToModify == CurriculumRoles.participant);
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
		
		DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(RightsElementsCols.note);
		noteCol.setDefaultVisible(true);
		noteCol.setAlwaysVisible(false);
		noteCol.setIconHeader("o_icon o_icon-fw");// Dummy icon
		noteCol.setHeaderLabel(i18nLabel);
		columnsModel.addFlexiColumnModel(noteCol);

		tableModel = new RightsCurriculumElementsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "editRolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
		tableEl.setFormLayout("0_12");
		tableEl.setCssDelegate(new SelectedCurriculumElementCssDelegate());
	}
	
	private void updateUI() {
		boolean participant = roleToModify == CurriculumRoles.participant;
		boolean withConfirmation = participant && confirmationTypeEl.isOneSelected()
				&& ConfirmationMembershipEnum.WITH.name().equals(confirmationTypeEl.getSelectedKey());
		confirmationByEl.setVisible(withConfirmation && participant);
		confirmUntilEl.setVisible(withConfirmation && participant);
		
		boolean individualElements = applyToEl.isVisible() && applyToEl.isOneSelected()
				&& ChangeApplyToEnum.valueOf(applyToEl.getSelectedKey()) == ChangeApplyToEnum.CURRENT;
		adminNoteEl.setVisible(!individualElements);
		tableEl.setVisible(individualElements);
		tableCont.setVisible(individualElements);
		
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
		
		FormLink addLink = uifactory.addFormLink("add.".concat(id), CMD_ADD, "add", tableEl, Link.LINK);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_plus");
		addLink.setUserObject(row);
		row.setAddButton(addLink);
		
		FormLink noteLink = uifactory.addFormLink("note.".concat(id), CMD_NOTE, "", tableEl, Link.LINK | Link.NONTRANSLATED);
		noteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_notes");
		noteLink.setTitle(translate("note"));
		noteLink.setUserObject(row);
		noteLink.setVisible(false);
		row.setNoteButton(noteLink);
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
		} else if(source instanceof FormLink link
				&& link.getUserObject() instanceof RightsCurriculumElementRow row) {
			if(CMD_ADD.equals(link.getCmd())) {
				doAddMembership(ureq, link, row);
			} else if(CMD_NOTE.equals(link.getCmd())) {
				doOpenNote(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(confirmationTypeEl);
		allOk &= validateFormLogic(confirmationByEl);
		allOk &= validateFormLogic(applyToEl);
		allOk &= validateFormLogic(teacherAssignmentEl);
		
		tableEl.clearError();
		if(applyToEl.isVisible() && applyToEl.isOneSelected() && ChangeApplyToEnum.CURRENT.name().equals(applyToEl.getSelectedKey())
				&& !membersContext.hasModifications() && !tableModel.hasModifications()) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateFormLogic(SingleSelection el) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible() && !el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		boolean participant = roleToModify == CurriculumRoles.participant;
		ConfirmationMembershipEnum confirmation = participant
				? ConfirmationMembershipEnum.valueOf(confirmationTypeEl.getSelectedKey())
				: ConfirmationMembershipEnum.WITHOUT;
		GroupMembershipStatus nextStatus = participant ? getNextStatus() : GroupMembershipStatus.active;
		
		Date confirmationUntil = null;
		ConfirmationByEnum confirmationBy = null;
		if(participant && confirmation == ConfirmationMembershipEnum.WITH) {
			confirmationUntil = confirmUntilEl.getDate();
			confirmationBy = ConfirmationByEnum.valueOf(confirmationByEl.getSelectedKey());
		}
		
		String adminNote = adminNoteEl.isVisible() ? adminNoteEl.getValue() : null;
		membersContext.setAdminNote(adminNote);

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
		
		membersContext.setAddAsTeacher(teacherAssignmentEl.isVisible() && teacherAssignmentEl.isOneSelected()
				&& ASSIGN_KEY.equals(teacherAssignmentEl.getSelectedKey()));
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private GroupMembershipStatus getNextStatus() {
		AccessInfos offer = membersContext.getSelectedOffer();
		boolean participant = roleToModify == CurriculumRoles.participant;
		ConfirmationMembershipEnum confirmation = ConfirmationMembershipEnum.valueOf(confirmationTypeEl.getSelectedKey());
		if(participant) {
			if(offer != null) {
				return offer.offer().isConfirmationByManagerRequired()
						? GroupMembershipStatus.reservation
						: GroupMembershipStatus.active;
			}
			return confirmation == ConfirmationMembershipEnum.WITH
					? GroupMembershipStatus.reservation
					: GroupMembershipStatus.active;
		}
		return GroupMembershipStatus.active;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddMembership(UserRequest ureq, FormLink link, RightsCurriculumElementRow row) {
		boolean hasChildren = tableModel.getObjects().stream()
				.filter(obj -> row.equals(obj.getParent()))
				.count() > 0;
		addMembershipCtrl = new AddMembershipCalloutController(ureq, getWindowControl(),
				roleToModify, row.getCurriculumElement(), hasChildren);
		listenTo(addMembershipCtrl);
		
		String title = translate("add.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addMembershipCtrl.getInitialComponent(), link.getFormDispatchId(),
				title, true, "", new CalloutSettings(true, CalloutOrientation.bottomOrTop, false, title));
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void setModification(MembershipModification mod) {
		GroupMembershipStatus nextStatus = getNextStatus();
		MembershipModification modification = new MembershipModification(mod.role(), mod.curriculumElement(), nextStatus,
				null, null, null, mod.toDescendants(), mod.adminNote());

		RightsCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		setModification(row, modification);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				RightsCurriculumElementRow obj = tableModel.getObject(i);
				if(tableModel.isParentOf(row, obj)) {
					MembershipModification objModification = modification.copyFor(obj.getCurriculumElement());
					setModification(obj, objModification);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}
	
	private void setModification(RightsCurriculumElementRow obj, MembershipModification modification) {
		obj.getNoteButton().setVisible(StringHelper.containsNonWhitespace(modification.adminNote()));
		obj.setModification(modification);
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, RightsCurriculumElementRow row) {
		String adminNote = null;
		MembershipModification modification = row.getModification();		
		if(modification != null && StringHelper.containsNonWhitespace(modification.adminNote())) {
			adminNote = modification.adminNote();
		}
		
		StringBuilder sb = Formatter.stripTabsAndReturns(adminNote);
		String note = sb == null ? "" : sb.toString();
		noteCtrl = new NoteCalloutController(ureq, getWindowControl(), note);
		listenTo(noteCtrl);
		
		String title = translate("note");
		CalloutSettings settings = new CalloutSettings(title);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noteCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class SelectedCurriculumElementCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			RightsCurriculumElementRow row = tableModel.getObject(pos);
			if(curriculumElement.equals(row.getCurriculumElement())) {
				return "o_row_selected";
			}
			return null;
		}
	}
}
