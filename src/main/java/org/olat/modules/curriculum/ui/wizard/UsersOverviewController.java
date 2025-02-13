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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.modules.curriculum.ui.wizard.UsersOverviewTableModel.UserOverviewCols;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersOverviewController extends StepFormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private FlexiTableElement tableEl;
	private UsersOverviewTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final String avatarMapperBaseURL;
	private final MembersContext membersContext;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public UsersOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_overview");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.membersContext = membersContext;
		
		detailsVC = createVelocityContainer("member_details");
		avatarMapperBaseURL = registerCacheableMapper(ureq, "imp-cur-avatars", avatarMapper);

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AbstractMembersController.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = AbstractMembersController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String name = userPropertyHandler.getName();
			String action = UserConstants.NICKNAME.equals(name) || UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name)
					? TOGGLE_DETAILS_CMD : null;
			boolean visible = userManager.isMandatoryUserProperty(AbstractMembersController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, action, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.role));
				
		tableModel = new UsersOverviewTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void loadModel() {
		List<Identity> identities = membersContext.getSearchedIdentities();
		List<UserRow> rows = identities == null ? List.of() : identities.stream()
				.map(id -> new UserRow(id, userPropertyHandlers, getLocale()))
				.toList();
		
		loadMemberships(identities, rows);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		if(rows.size() == 1) {
			tableEl.setMultiSelectedIndex(Set.of(Integer.valueOf(0)));
		}
	}
	
	private void loadMemberships(List<Identity> identities, List<UserRow> rows) {
		List<CurriculumElementMembership> memberships = curriculumService
				.getCurriculumElementMemberships(membersContext.getAllCurriculumElements(), identities);
		Map<Long,EnumSet<CurriculumRoles>> identityToRoles = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			List<CurriculumRoles> roles = membership.getRoles();
			if(!roles.isEmpty()) {
				identityToRoles.computeIfAbsent(membership.getIdentityKey(), id -> EnumSet.noneOf(CurriculumRoles.class))
					.addAll(roles);
			}
		}
		
		for(UserRow row:rows) {
			Set<CurriculumRoles> rolesSet = identityToRoles.get(row.getIdentityKey());
			String roles;
			if(rolesSet == null || rolesSet.isEmpty()) {
				roles = null;
			} else if(rolesSet.size() == 1) {
				roles = toString(rolesSet);
			} else {
				List<CurriculumRoles> rolesList = new ArrayList<>(rolesSet);
				Collections.sort(rolesList);
				roles = toString(rolesList);
			}
			row.setRoles(roles);
		}
	}
	
	private String toString(Collection<CurriculumRoles> roles) {
		StringBuilder sb = new StringBuilder(32);
		for(CurriculumRoles role:roles) {
			if(sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(translate("role.".concat(role.name())));
		}
		return sb.toString();
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof UserRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					UserRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				UserRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenMemberDetails(ureq, row);
				} else {
					doCloseMemberDetails(row);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if((membersContext.getSelectedIdentities() == null || membersContext.getSelectedIdentities().isEmpty())
				&& tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		Set<Integer> indexes = tableEl.getMultiSelectedIndex();
		List<Identity> selectedIdentities = indexes.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(UserRow::getIdentity)
				.toList();
		membersContext.setSelectedIdentities(selectedIdentities);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private final void doOpenMemberDetails(UserRequest ureq, UserRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		List<CurriculumElement> elements = new ArrayList<>(membersContext.getDescendants());
		elements.add(membersContext.getCurriculumElement());
		Curriculum curriculum = membersContext.getCurriculum();
		
		UserInfoProfileConfig profileConfig = createProfilConfig();
		List<CurriculumRoles> rolesToSee = membersContext.getRoleToModify() == null
				? List.of() : List.of(membersContext.getRoleToModify());
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToSee, false, false, false, false, false,
				false, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, membersContext.getCurriculumElement(), elements, row.getIdentity(), config);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	protected final void doCloseMemberDetails(UserRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private final UserInfoProfileConfig createProfilConfig() {
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		return profileConfig;
	}

}
