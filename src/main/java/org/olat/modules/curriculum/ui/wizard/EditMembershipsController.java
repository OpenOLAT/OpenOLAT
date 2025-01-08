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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
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
import org.olat.modules.curriculum.ui.CurriculumUIFactory;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.member.ChangeMembershipCalloutController;
import org.olat.modules.curriculum.ui.member.EditMemberCurriculumElementRow;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.NoteCalloutController;
import org.olat.modules.curriculum.ui.wizard.EditMembershipsTableModel.MembershipsCols;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMembershipsController extends StepFormBasicController {
	
	private static final CurriculumRoles[] ROLES = CurriculumRoles.curriculumElementsRoles();
	private static final GroupMembershipStatus[] MODIFIABLE = new GroupMembershipStatus[] {
			GroupMembershipStatus.reservation, GroupMembershipStatus.active,
			GroupMembershipStatus.declined, GroupMembershipStatus.removed
		};
	private static final String CMD_NOTE = "note";
	private static final String CMD_ADD_ROLE = "add.role";
	private static final String CMD_SELECT = "select.status";
	public static final int ROLES_OFFSET = 500;
	public static final int NOTES_OFFSET = 1000;
	
	private DropdownItem addRolesEl;
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private EditMembershipsTableModel tableModel;
	
	private int counter = 0;
	private EditMembersContext membersContext;
	private final List<CurriculumElement> curriculumElements;
	private final CurriculumElement selectedCurriculumElement;
	private final EnumSet<CurriculumRoles> usedRoles = EnumSet.noneOf(CurriculumRoles.class);
	
	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ChangeMembershipCalloutController changeMembershipCtrl;
			
	public EditMembershipsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			EditMembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "edit_memberships");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.membersContext = membersContext;
		selectedCurriculumElement = membersContext.getCurriculumElement();
		curriculumElements = membersContext.getAllCurriculumElements();
		usedRoles.addAll(membersContext.getBaseRoles());
		
		initForm(ureq);
		loadModel();
		updateRolesColumnsVisibility();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addRolesEl = uifactory.addDropdownMenu("assign.additional.roles", "assign.additional.roles", null, formLayout, getTranslator());
		for(CurriculumRoles role:CurriculumRoles.curriculumElementsRoles()) {
			String name = "role.".concat(role.name());
			FormLink addRoleButton = uifactory.addFormLink(name, CMD_ADD_ROLE, name, null, formLayout, Link.LINK);
			addRoleButton.setUserObject(role);
			addRolesEl.addElement(addRoleButton);
		}
		
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MembershipsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MembershipsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MembershipsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MembershipsCols.externalId));

		GroupMembershipStatusRenderer statusRenderer = new GroupMembershipStatusRenderer(getLocale());
		for(CurriculumRoles role:ROLES) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null, statusRenderer);
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
			
			DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(null, role.ordinal() + NOTES_OFFSET);
			noteCol.setDefaultVisible(true);
			noteCol.setAlwaysVisible(false);
			noteCol.setIconHeader("o_icon o_icon-fw");// Dummy icon
			noteCol.setHeaderLabel(i18nLabel);
			columnsModel.addFlexiColumnModel(noteCol);
		}
		
		tableModel = new EditMembershipsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "editRolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCssDelegate(new SelectedCurriculumElementCssDelegate());
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
	}
	
	private void loadModel() {
		List<EditMemberCurriculumElementRow> rows = new ArrayList<>();
		Map<Long, EditMemberCurriculumElementRow> rowsMap = new HashMap<>();
		for(CurriculumElement element:curriculumElements) {
			EditMemberCurriculumElementRow row = new EditMemberCurriculumElementRow(element);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			
			for(CurriculumRoles role:ROLES) {
				CurriculumElementAndRole cell = new CurriculumElementAndRole(element, role, row);
				FormLink selectLink = forgeLink(cell);
				row.addButton(role, selectLink);
				FormLink noteLink = forgeNoteLink(cell);
				row.addNoteButton(role, noteLink);
			}
		}
		
		for(EditMemberCurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(rowsMap.get(row.getParentKey()));
			}
		}
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private FormLink forgeLink(CurriculumElementAndRole cell) {
		String id = "select.status." + (++counter);
		FormLink link = uifactory.addFormLink(id, CMD_SELECT, "select.next.status", null, flc, Link.BUTTON_XSMALL);
		link.setDomReplacementWrapperRequired(false);
		link.setCustomEnabledLinkCSS("o_labeled_light o_gmembership_status_select");
		link.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		link.setUserObject(cell);
		return link;
	}
	
	private FormLink forgeNoteLink(CurriculumElementAndRole cell) {
		FormLink noteLink = uifactory.addFormLink("note_" + (++counter), CMD_NOTE, "", null, flc, Link.LINK | Link.NONTRANSLATED);
		noteLink.setDomReplacementWrapperRequired(false);
		noteLink.setIconLeftCSS("o_icon o_icon_notes");
		noteLink.setTitle(translate("note"));
		noteLink.setVisible(false);
		noteLink.setUserObject(cell);
		return noteLink;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(changeMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setModification(changeMembershipCtrl.getModification());
			}
			calloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(changeMembershipCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		changeMembershipCtrl = null;
		calloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link && CMD_ADD_ROLE.equals(link.getCmd())
				&& link.getUserObject() instanceof CurriculumRoles role)  {
			doAddRole(role);
		} else if(source instanceof FormLink link
				&& link.getUserObject() instanceof CurriculumElementAndRole cell)  {
			if(CMD_SELECT.equals(link.getCmd())) {
				doOpenCallout(ureq, link, cell);
			} else if(CMD_NOTE.equals(link.getCmd())) {
				doOpenNote(ureq, link, cell);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<MembershipModification> modifications = new ArrayList<>();
		List<EditMemberCurriculumElementRow> rows = tableModel.getObjects();
		for(EditMemberCurriculumElementRow row:rows) {
			modifications.addAll(row.getModifications());
		}
		membersContext.setModifications(modifications);
		membersContext.setRoles(new ArrayList<>(usedRoles));
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddRole(CurriculumRoles role) {
		usedRoles.add(role);
		updateRolesColumnsVisibility();
	}
	
	private void setModification(MembershipModification modification) {
		EditMemberCurriculumElementRow row = tableModel.getObject(modification.curriculumElement());
		row.addModification(modification.role(), modification);
		updateLink(row, modification);
		
		if(modification.toDescendants()) {
			int index = tableModel.getIndexOf(row);
			int numOfRows = tableModel.getRowCount();
			for(int i=index+1; i<numOfRows; i++) {
				EditMemberCurriculumElementRow obj = tableModel.getObject(i);
				if(tableModel.isParentOf(row, obj)) {
					obj.addModification(modification.role(), modification);
					updateLink(obj, modification);
				}
			}
		}
		
		tableEl.reset(false, false, true);
	}
	
	private void updateRolesColumnsVisibility() {
		// Update columns visibility
		for(CurriculumRoles role:ROLES) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, usedRoles.contains(role));
			}
		}
		
		for(FormItem item:addRolesEl.getFormItems()) {
			if(item instanceof FormLink link && link.getUserObject() instanceof CurriculumRoles role) {
				item.setVisible(!usedRoles.contains(role));
			}
		}
	}
	
	private void updateLink(EditMemberCurriculumElementRow row, MembershipModification modification) {
		FormLink link = row.getButton(modification.role());
		
		String cssClass = CurriculumUIFactory.getMembershipLabelCssClass(modification.nextStatus());
		cssClass = cssClass == null ? "o_gmembership_status_select" : cssClass;
		link.setCustomEnabledLinkCSS("o_labeled_light ".concat(cssClass));
		
		String iconCssClass = CurriculumUIFactory.getMembershipIconCssClass(modification.nextStatus());
		iconCssClass = iconCssClass == null ? null : "o_icon o_icon-fw ".concat(iconCssClass);
		link.setIconLeftCSS(iconCssClass);
		
		String i18nKey = getMembershipI18nKey(modification.nextStatus());
		i18nKey = i18nKey == null ? "select.next.status" : i18nKey;
		link.setI18nKey(i18nKey);
		
		row.getNoteButton(modification.role()).setVisible(StringHelper.containsNonWhitespace(modification.adminNote()));
	}
	
	public static final String getMembershipI18nKey(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "membership.pending";
			case active -> "membership.active";
			case cancel -> "membership.cancel";
			case cancelWithFee -> "membership.cancelWithFee";
			case declined -> "membership.declined";
			case resourceDeleted -> "membership.resourceDeleted";
			case finished -> "membership.finished";
			case removed -> "membership.removed";
			default -> null;
		};
	}
	
	private void doOpenCallout(UserRequest ureq, FormLink link, CurriculumElementAndRole cell) {
		CurriculumRoles role = cell.role();
		CurriculumElement curriculumElement = cell.curriculumElement();
		changeMembershipCtrl = new ChangeMembershipCalloutController(ureq, getWindowControl(), null, role, curriculumElement, MODIFIABLE);
		listenTo(changeMembershipCtrl);
		
		String title = translate("change.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				changeMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, CurriculumElementAndRole cell) {
		String adminNote = cell.row().getAdminNote();
		MembershipModification modification = cell.row().getModification(cell.role());		
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
	
	public record CurriculumElementAndRole(CurriculumElement curriculumElement, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		//
	}
	
	private class SelectedCurriculumElementCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			EditMemberCurriculumElementRow row = tableModel.getObject(pos);
			if(selectedCurriculumElement.equals(row.getCurriculumElement())) {
				return "o_row_selected";
			}
			return null;
		}
	}
}
