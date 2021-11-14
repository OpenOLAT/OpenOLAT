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
package org.olat.user.ui.admin.bulk.move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.SearchMemberParameters;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.UserSearchTableController;
import org.olat.user.ui.admin.bulk.move.OverviewMoveTableModel.MoveUserCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewMoveController extends StepFormBasicController {

	private static final String USER_PROPS_ID = UserSearchTableController.USER_PROPS_ID;// reuse it
	protected static final int USER_PROPS_OFFSET = 500;
	
	private OverviewMoveTableModel tableModel;
	
	private final UserBulkMove userBulkMove;
	
	private final Roles roles;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OverviewMoveController(UserRequest ureq, WindowControl wControl, UserBulkMove userBulkMove,
			Form form, StepsRunContext stepsRunContext) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_CUSTOM, "overview");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		this.userBulkMove = userBulkMove;
		
		roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MoveUserCols.id));
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, null, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MoveUserCols.role));
		
		tableModel = new OverviewMoveTableModel(columnsModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("error.no.user.found", null, "o_icon_user");
		tableEl.setAndLoadPersistedPreferences(ureq, "overview_user_search_table-v2");
	}
	
	private void loadModel() {
		List<Identity> identities = userBulkMove.getIdentities();
		List<OverviewIdentityRow> rows = new ArrayList<>(identities.size());
		Map<Identity, OverviewIdentityRow> rowMap = new HashMap<>();
		for(Identity identity:identities) {
			OverviewIdentityRow row = new OverviewIdentityRow(identity, userPropertyHandlers, getLocale());
			rows.add(row);
			rowMap.put(identity, row);
		}
		
		Set<String> roleMap = userBulkMove.getRoles().stream().map(OrganisationRoles::name).collect(Collectors.toSet());
		SearchMemberParameters params = new SearchMemberParameters();
		List<OrganisationMember> members = organisationService.getMembers(userBulkMove.getOrganisation(), params);
		for(OrganisationMember member:members) {
			OverviewIdentityRow row = rowMap.get(member.getIdentity());
			if(row != null && roleMap.contains(member.getRole())) {
				String role = translate("role.".concat(member.getRole()));
				row.addRole(role);
			}
		}
		
		List<OverviewIdentityRow> selectedRows = rows.stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getRoles()))
				.collect(Collectors.toList());
		tableModel.setObjects(selectedRows);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Identity> identities = tableModel.getObjects().stream()
				.map(OverviewIdentityRow::getIdentity)
				.collect(Collectors.toList());
		userBulkMove.setIdentitiesToMove(identities);
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
