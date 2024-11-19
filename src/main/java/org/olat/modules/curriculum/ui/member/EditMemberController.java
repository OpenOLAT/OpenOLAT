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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.EditMemberCurriculumElementTableModel.MemberElementsCols;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMemberController extends FormBasicController {

	protected static final int ROLES_OFFSET = 500;

	private static final String CMD_ADD = "add";
	private static final String CMD_ACTIVE = "active";
	
	private FormLink backButton;
	private FormLink resetButton;
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private EditMemberCurriculumElementTableModel tableModel;
	
	private int count = 0;
	private Identity member;
	private final Curriculum curriculum;
	private final UserInfoProfileConfig profileConfig;
	private final List<CurriculumElement> curriculumElements;

	private CloseableCalloutWindowController calloutCtrl;
	private AddMembershipCalloutController addMembershipCtrl;
	private ChangeMembershipCalloutController changeMembershipCtrl;
	
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private CurriculumService curriculumService;
	
	public EditMemberController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, List<CurriculumElement> curriculumElements,
			Identity member, UserInfoProfileConfig profileConfig) {
		super(ureq, wControl, "edit_member");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.member = member;
		this.curriculum = curriculum;
		this.profileConfig = profileConfig;
		this.curriculumElements = new ArrayList<>(curriculumElements);
		
		initForm(ureq);
		loadModel();
	}
	
	public Identity getMember() {
		return member;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backButton = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);

		// Profile
		UserInfoProfile memberConfig = userInfoService.createProfile(member);
		MemberUserDetailsController profile = new MemberUserDetailsController(ureq, getWindowControl(), mainForm,
				member, profileConfig, memberConfig);
		listenTo(profile);
		formLayout.add("profil", profile.getInitialFormItem());
		
		initTableForm(formLayout);
		
		uifactory.addFormSubmitButton("save", formLayout);
		resetButton = uifactory.addFormLink("reset", formLayout, Link.BUTTON);
		resetButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reset_data");
	}
	
	private void initTableForm(FormItemContainer formLayout) {
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberElementsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberElementsCols.displayName,
				new TreeNodeFlexiCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberElementsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MemberElementsCols.externalId));
		
		for(CurriculumRoles role:CurriculumRoles.values()) {
			String i18nLabel = "role.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(true, i18nLabel, role.ordinal() + ROLES_OFFSET, null, false, null);
			col.setDefaultVisible(true);
			col.setAlwaysVisible(false);
			columnsModel.addFlexiColumnModel(col);
		}
		
		tableModel = new EditMemberCurriculumElementTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "editRolesTable", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setFooter(true);
	}
	
	protected void loadModel() {
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, member);
		Map<Long, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, u -> u, (u, v) -> u));
		
		List<EditMemberCurriculumElementRow> rows = new ArrayList<>();
		Map<Long, EditMemberCurriculumElementRow> rowsMap = new HashMap<>();
		EnumMap<CurriculumRoles,Boolean> usedRoles = new EnumMap<>(CurriculumRoles.class);
		for(CurriculumElement element:curriculumElements) {
			EditMemberCurriculumElementRow row = new EditMemberCurriculumElementRow(element);
			rows.add(row);
			rowsMap.put(row.getKey(), row);
			
			CurriculumElementMembership membership = membershipsMap.get(element.getKey());
			forgeLinks(row, membership, usedRoles);
		}
		
		for(EditMemberCurriculumElementRow row:rows) {
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
	
	private void forgeLinks(EditMemberCurriculumElementRow row, CurriculumElementMembership membership,
			EnumMap<CurriculumRoles,Boolean> usedRoles) {
		List<CurriculumRoles> memberships = membership == null ? List.of() : membership.getRoles();
		for(CurriculumRoles role:CurriculumRoles.values()) {
			FormLink link;
			if(memberships.contains(role)) {
				link = forgeActiveLink();
				usedRoles.put(role, Boolean.TRUE);
			} else {
				link = forgeAddLink();
			}
			link.setUserObject(new RoleCell(role, row));
			row.addButton(role, link);
		}
	}

	private FormLink forgeActiveLink() {
		FormLink activeLink = uifactory.addFormLink("active_" + (++count), CMD_ACTIVE, "membership.active", null, flc, Link.BUTTON_XSMALL);
		activeLink.setIconLeftCSS("o_icon o_icon-fw o_membership_status_active");
		activeLink.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		return activeLink;
	}
	
	private FormLink forgeAddLink() {
		FormLink addLink = uifactory.addFormLink("add_" + (++count), CMD_ADD, "add", null, flc, Link.LINK);
		addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_plus");
		return addLink;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addMembershipCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(changeMembershipCtrl);
		removeAsListenerAndDispose(addMembershipCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		changeMembershipCtrl = null;
		addMembershipCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(source instanceof FormLink link) {
			if(CMD_ACTIVE.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doChangeMembership(ureq, link, cell.role(), cell.row());
			} else if(CMD_ADD.equals(link.getCmd()) && link.getUserObject() instanceof RoleCell cell) {
				doAddMembership(ureq, link, cell.role(), cell.row());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		addMembershipCtrl = new AddMembershipCalloutController(ureq, getWindowControl(), member, role, curriculumElement);
		listenTo(addMembershipCtrl);
		
		String title = translate("add.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doChangeMembership(UserRequest ureq, FormLink link, CurriculumRoles role, EditMemberCurriculumElementRow row) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		changeMembershipCtrl = new ChangeMembershipCalloutController(ureq, getWindowControl(), member, role, curriculumElement);
		listenTo(changeMembershipCtrl);
		
		String title = translate("change.membership");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				changeMembershipCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	public record Modification(CurriculumRoles role, EditMemberCurriculumElementRow row) {
		//
	}
	
	private record RoleCell(CurriculumRoles role, EditMemberCurriculumElementRow row) {
		//
	}
	
}
