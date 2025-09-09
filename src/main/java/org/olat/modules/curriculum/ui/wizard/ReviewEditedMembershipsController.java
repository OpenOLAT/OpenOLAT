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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembershipHistory;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
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
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.component.DualNumberCellRenderer;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ModificationCellRenderer;
import org.olat.modules.curriculum.ui.member.ModificationStatusSummary;
import org.olat.modules.curriculum.ui.member.ResourceToRoleKey;
import org.olat.modules.curriculum.ui.wizard.ReviewEditedMembershipsTableModel.ReviewEditedMembershipsCols;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewEditedMembershipsController extends StepFormBasicController implements FlexiTableComponentDelegate {

	private static final CurriculumRoles[] ROLES = CurriculumRoles.curriculumElementsRoles();
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	public static final int USER_PROPS_OFFSET = 500;
	public static final int ROLES_OFFSET = 1500;
	
	private FlexiTableElement tableEl;
	private FlexiTableColumnModel columnsModel;
	private ReviewEditedMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final EditMembersContext membersContext;

	private final List<CurriculumRoles> rolesToReview;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
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
	
	public ReviewEditedMembershipsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			EditMembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "review_memberships");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(),
				userManager.getPropertyHandlerTranslator(getTranslator())));
		
		rolesToReview = membersContext.getRoles();
		this.membersContext = membersContext;
		
		detailsVC = createVelocityContainer("member_details");

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AbstractMembersController.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel();
		updateRolesColumnsVisibility();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewEditedMembershipsCols.modifications,
				new ModificationCellRenderer(getTranslator())));

		int colIndex = USER_PROPS_OFFSET;
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
		
		for(CurriculumRoles role:ROLES) {
			String i18nLabel = "table.header.num.of.".concat(role.name());
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(i18nLabel, role.ordinal() + ROLES_OFFSET,
					new DualNumberCellRenderer(getTranslator()));
			columnsModel.addFlexiColumnModel(col);
		}
		
		tableModel = new ReviewEditedMembershipsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof ReviewEditedMembershipsRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	private void updateRolesColumnsVisibility() {
		// Update columns visibility
		for(CurriculumRoles role:ROLES) {
			FlexiColumnModel col = columnsModel.getColumnModelByIndex(role.ordinal() + ROLES_OFFSET);
			if(col instanceof DefaultFlexiColumnModel) {
				tableEl.setColumnModelVisible(col, rolesToReview.contains(role));
			}
		}
	}
	
	private void loadModel() {
		List<Identity> identities = membersContext.getIdentities();
		List<ReviewEditedMembershipsRow> rows = identities == null ? List.of() : identities.stream()
				.map(id -> new ReviewEditedMembershipsRow(id, userPropertyHandlers, getLocale()))
				.toList();
		Map<Long,ReviewEditedMembershipsRow> identityKeyToRows = rows.stream()
				.collect(Collectors.toMap(ReviewEditedMembershipsRow::getIdentityKey, r -> r, (u, v) -> u));
		
		List<MembershipModification> modifications = membersContext.getModifications();
		for(ReviewEditedMembershipsRow row:rows) {
			row.setModifications(modifications);
		}
		
		// History
		loadStatusFromHistory(identities, identityKeyToRows);
		// Reservations
		loadStatusFromReservations(identityKeyToRows);
		// Memberships
		loadStatusFromMemberships(identities, identityKeyToRows);
		
		for(ReviewEditedMembershipsRow row:rows) {
			ModificationStatusSummary summaryStatus = evaluateModificationSummary(row);
			row.setModificationSummary(summaryStatus);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void loadStatusFromHistory(List<Identity> identities, Map<Long,ReviewEditedMembershipsRow> identityKeyToRows) {
		List<CurriculumRoles> roles = membersContext.getRoles();
		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();

		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(curriculumElements);
		searchParams.setIdentities(identities);
		List<CurriculumElementMembershipHistory> membershipsHistory = curriculumService
				.getCurriculumElementMembershipsHistory(searchParams);
		for(CurriculumElementMembershipHistory membershipHistory:membershipsHistory) {
			Long curriculumElementKey = membershipHistory.getCurriculumElementKey();
			ReviewEditedMembershipsRow row = identityKeyToRows.get(membershipHistory.getIdentityKey());
			if(row != null) {
				for(CurriculumRoles role:roles) {
					List<GroupMembershipHistory> roleHistory = membershipHistory.getHistory(role);
					if(roleHistory != null && !roleHistory.isEmpty()) {
						GroupMembershipHistory step = roleHistory.get(0);
						if(step.getStatus() != GroupMembershipStatus.active && step.getStatus() != GroupMembershipStatus.reservation) {
							row.addStatus(curriculumElementKey, role, step.getStatus());
						}
					}
				}
			}
		}
	}
	
	private void loadStatusFromReservations(Map<Long,ReviewEditedMembershipsRow> identityKeyToRows) {
		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		Map<OLATResource,CurriculumElement> resourceToCurriculumElements = curriculumElements.stream()
				.collect(Collectors.toMap(CurriculumElement::getResource, c -> c, (u, v) -> u));
		List<OLATResource> resources = membersContext.getAllCurriculumElementsResources();
		List<ResourceReservation> reservations = acService.getReservations(resources);
		for(ResourceReservation reservation:reservations) {
			CurriculumElement curriculumElement = resourceToCurriculumElements.get(reservation.getResource());
			ReviewEditedMembershipsRow row = identityKeyToRows.get(reservation.getIdentity().getKey());
			CurriculumRoles role = ResourceToRoleKey.reservationToRole(reservation.getType());
			if(row != null && role != null && curriculumElement != null) {
				row.addStatus(curriculumElement.getKey(), role, GroupMembershipStatus.reservation);
			}
		}
	}
	
	private void loadStatusFromMemberships(List<Identity> identities, Map<Long,ReviewEditedMembershipsRow> identityKeyToRows) {
		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculumElements, identities);
		for(CurriculumElementMembership membership:memberships) {
			ReviewEditedMembershipsRow row = identityKeyToRows.get(membership.getIdentityKey());
			if(row != null) {
				Long curriculumElementKey = membership.getCurriculumElementKey();
				for(CurriculumRoles role:membership.getRoles()) {
					row.addStatus(curriculumElementKey, role, GroupMembershipStatus.active);
				}
			}
		}
	}
	
	private ModificationStatusSummary evaluateModificationSummary(ReviewEditedMembershipsRow row) {
		boolean modification = false;
		boolean removal = false;
		boolean addition = false;
		int numOfModifications = 0;

		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		for(CurriculumElement curriculumElement:curriculumElements) {
			int hasElementAccessBefore = 0;
			int gainAccessAfter = 0;
			int looseAccessAfter = 0;
			
			for(CurriculumRoles role:rolesToReview) {
				GroupMembershipStatus currentStatus = row.getStatusBy(curriculumElement.getKey(), role);
				if(currentStatus == GroupMembershipStatus.active || currentStatus == GroupMembershipStatus.reservation) {
					++hasElementAccessBefore;
				}
			}

			for(CurriculumRoles role:rolesToReview) {
				GroupMembershipStatus currentStatus = row.getStatusBy(curriculumElement.getKey(), role);
				GroupMembershipStatus modificationStatus = row.getModification(curriculumElement.getKey(), role);
				if(currentStatus == GroupMembershipStatus.active || currentStatus == GroupMembershipStatus.reservation) {
					if(modificationStatus != null && (modificationStatus == GroupMembershipStatus.removed
							|| modificationStatus == GroupMembershipStatus.cancel
							||  modificationStatus == GroupMembershipStatus.cancelWithFee)) {
						looseAccessAfter++;
					}
				} else {
					if(modificationStatus != null && (modificationStatus == GroupMembershipStatus.active
							|| modificationStatus == GroupMembershipStatus.reservation)) {
						gainAccessAfter++;
					}
				}
			}
			
			if(hasElementAccessBefore == 0 && gainAccessAfter > 0) {
				addition |= true;
			} else if(hasElementAccessBefore > 0 && hasElementAccessBefore == looseAccessAfter && gainAccessAfter == 0) {
				removal |= true;
			} else if(hasElementAccessBefore > 0 && (gainAccessAfter > 0 || looseAccessAfter > 0)) {
				modification |= true;
			}
			numOfModifications += gainAccessAfter;
			numOfModifications += looseAccessAfter;
		}

		return new ModificationStatusSummary(modification, addition, removal, numOfModifications);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					ReviewEditedMembershipsRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				ReviewEditedMembershipsRow row = tableModel.getObject(toggleEvent.getRowIndex());
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
	protected void formFinish(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private final void doOpenMemberDetails(UserRequest ureq, ReviewEditedMembershipsRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		List<CurriculumElement> elements = membersContext.getAllCurriculumElements();
		Curriculum curriculum = membersContext.getCurriculum();
		
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToReview, false, false, false, true, false,
				false, false, false, false);
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculum, membersContext.getCurriculumElement(), elements, row.getIdentity(), config);
		listenTo(detailsCtrl);
		
		detailsCtrl.setModifications(membersContext.getModifications());
		
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	protected final void doCloseMemberDetails(ReviewEditedMembershipsRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
}
