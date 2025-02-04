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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipStatus;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.DualNumberCellRenderer;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.modules.curriculum.ui.member.MemberRolesDetailsRow;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ModificationCellRenderer;
import org.olat.modules.curriculum.ui.member.ModificationStatusSummary;
import org.olat.modules.curriculum.ui.wizard.UsersOverviewTableModel.UserOverviewCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
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
public class ReviewModificationsController extends StepFormBasicController implements FlexiTableComponentDelegate {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private FlexiTableElement tableEl;
	private UsersOverviewTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final String avatarMapperBaseURL;
	private final MembersContext membersContext;
	private final CurriculumRoles roleToModify;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public ReviewModificationsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_overview");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.membersContext = membersContext;
		roleToModify = membersContext.getRoleToModify();
		
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.modifications,
				new ModificationCellRenderer(getTranslator())));

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
		
		// Counts
		DefaultFlexiColumnModel numCol = new DefaultFlexiColumnModel(UserOverviewCols.numOfModifications,
				new DualNumberCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(numCol);
		
		tableModel = new UsersOverviewTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void loadModel() {
		List<Identity> identities = membersContext.getSelectedIdentities();
		List<MembershipModification> modifications = membersContext.getModifications();
		
		// Memberships, per user on at least an element of the tree of elements
		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculumElements, identities);
		Map<Long, List<CurriculumElementMembership>> membershipsMap = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			if(membership.getRoles().contains(membersContext.getRoleToModify())) {
				membershipsMap.computeIfAbsent(membership.getIdentityKey(), m -> new ArrayList<>())
					.add(membership);
			}
		}

		// Reservation, per user on at least a reservation on the tree of elements
		final String roleKeyWord = CurriculumService.RESERVATION_PREFIX.concat(membersContext.getRoleToModify().name());
		List<OLATResource> elementsResources = membersContext.getAllCurriculumElementResources();
		SearchReservationParameters searchParams = new SearchReservationParameters(elementsResources);
		List<ResourceReservation> reservations = acService.getReservations(searchParams);
		Map<Long, List<ResourceReservation>> reservationsMap = new HashMap<>();
		for(ResourceReservation reservation:reservations) {
			if(StringHelper.containsNonWhitespace(reservation.getType()) && reservation.getType().equals(roleKeyWord)) {
				reservationsMap.computeIfAbsent(reservation.getIdentity().getKey(), r -> new ArrayList<>())
					.add(reservation);
			}
		}

		List<UserRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			UserRow row = new UserRow(identity, userPropertyHandlers, getLocale());
			row.setModifications(modifications);
			
			final List<CurriculumElementMembership> userMemberships = membershipsMap
					.computeIfAbsent(identity.getKey(), m -> List.of());
			final List<ResourceReservation> userReservations = reservationsMap
					.computeIfAbsent(identity.getKey(), k -> List.of());
			ModificationStatusSummary summary = calculateModificationStatus(userMemberships, userReservations,
					membersContext.getModifications());
			row.setModificationSummary(summary);
			row.setModifications(membersContext.getModifications());
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
	}
	
	private ModificationStatusSummary calculateModificationStatus(List<CurriculumElementMembership> memberships,
			List<ResourceReservation> reservations, List<MembershipModification> modifications) {
		Map<Long,CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(CurriculumElementMembership::getCurriculumElementKey, c -> c, (u, v) -> u));
		Map<OLATResource,ResourceReservation> reservationsMap = reservations.stream()
				.collect(Collectors.toMap(ResourceReservation::getResource, r -> r, (u, v) -> u));
		
		boolean add = false;
		boolean modify = false;
		int numOfModifications = 0;
		for(MembershipModification modification:modifications) {
			GroupMembershipStatus memberStatus = modification.nextStatus();
			CurriculumElement curriculumElement = modification.curriculumElement();
			CurriculumElementMembership membership = membershipsMap.get(curriculumElement.getKey());
			ResourceReservation reservation = reservationsMap.get(curriculumElement.getResource());
			if(memberStatus == GroupMembershipStatus.active && membership == null && reservation == null) {
				add = true;
				numOfModifications++;
			} else if((memberStatus == GroupMembershipStatus.reservation && membership == null && reservation == null)
					|| (memberStatus == GroupMembershipStatus.active && membership == null && reservation != null)) {
				modify = true;
				numOfModifications++;
			}
		}
		return new ModificationStatusSummary(modify, add, false, numOfModifications);
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
	protected void formNext(UserRequest ureq) {
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
		List<CurriculumRoles> rolesToModify = List.of(membersContext.getRoleToModify());
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToModify, false, false, false, true, true,
				false, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, membersContext.getCurriculumElement(), elements, row.getIdentity(), config);
		
		// Add relevant modifications to the details view
		List<MemberRolesDetailsRow> currentDetailsRows = detailsCtrl.getRolesDetailsRows();
		Map<Long,MemberRolesDetailsRow> rowsMap = currentDetailsRows.stream()
				.collect(Collectors.toMap(MemberRolesDetailsRow::getKey, r -> r, (u, v) -> u));
		
		List<MembershipModification> modifications = new ArrayList<>(row.getModifications());
		for(Iterator<MembershipModification> it=modifications.iterator(); it.hasNext(); ) {
			MembershipModification modification = it.next();
			MemberRolesDetailsRow detailsRow = rowsMap.get(modification.curriculumElement().getKey());
			if(detailsRow != null) {
				GroupMembershipStatus currentStatus = detailsRow.getStatus(roleToModify);
				if((currentStatus == GroupMembershipStatus.active)
					|| (currentStatus == GroupMembershipStatus.reservation
						&& modification.nextStatus() == GroupMembershipStatus.reservation)) {
					it.remove();
				}
			}
		}
		detailsCtrl.setModifications(modifications);
		
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
