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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.modules.curriculum.ui.member.MemberRolesDetailsTableModel.MemberDetailsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRolesDetailsController extends FormBasicController {
	
	protected static final int ROLES_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private MemberRolesDetailsTableModel tableModel;
	
	private final Identity member;
	private final Curriculum curriculum;
	private final List<CurriculumElement> elements;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public MemberRolesDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Curriculum curriculum, List<CurriculumElement> elements, Identity member) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_roles", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.elements = new ArrayList<>(elements);
		this.curriculum = curriculum;
		this.member = member;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("membership");
		
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberDetailsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberDetailsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberDetailsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberDetailsCols.externalId));
		
		for(CurriculumRoles role:CurriculumRoles.values()) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null,
					new GroupMembershipStatusRenderer(getLocale()));
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
		}

		tableModel = new MemberRolesDetailsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "rolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
	}
	
	protected void loadModel() {
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, member);
		Map<Long, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, u -> u, (u, v) -> u));

		
		List<MemberRolesDetailsRow> rows = new ArrayList<>();
		Map<Long, MemberRolesDetailsRow> rowsMap = new HashMap<>();
		EnumMap<CurriculumRoles,Boolean> usedRoles = new EnumMap<>(CurriculumRoles.class);
		for(CurriculumElement element:elements) {
			List<CurriculumRoles> elementRoles = new ArrayList<>();
			MemberRolesDetailsRow row = new MemberRolesDetailsRow(element, elementRoles);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			
			CurriculumElementMembership membership = membershipsMap.get(element.getKey());
			if(membership != null && membership.hasMembership()) {
				for(CurriculumRoles role:membership.getRoles()) {
					row.addStatus(role, GroupMembershipStatus.active);
					usedRoles.put(role, Boolean.TRUE);
				}
			}
		}
		
		for(MemberRolesDetailsRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(rowsMap.get(row.getParentKey()));
			}
		}
		
		// Update columns visibility
		for(CurriculumRoles role:CurriculumRoles.values()) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, usedRoles.containsKey(role));
			}
		}
		
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
