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
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.olat.resource.accesscontrol.ui.BillingAddressCellRenderer;
import org.olat.resource.accesscontrol.ui.BillingAddressSelectionController;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.IdentityOrganisationsCellRenderer;
import org.olat.user.ui.organisation.OrganisationsSmallListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReviewModificationsController extends StepFormBasicController implements FlexiTableComponentDelegate {

	private static final String CMD_TOOLS = "odtools";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private FlexiTableElement tableEl;
	private UsersOverviewTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtrl;
	private BillingAddressSelectionController addressSelectionCtrl;
	private OrganisationsSmallListController organisationsSmallListCtrl;
	
	private final MembersContext membersContext;
	private final CurriculumRoles roleToModify;
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
	
	public ReviewModificationsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_overview");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.membersContext = membersContext;
		roleToModify = membersContext.getRoleToModify();
		
		detailsVC = createVelocityContainer("member_details");

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AbstractMembersController.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel();
		validateFormLogic(ureq);
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
		
		if (membersContext.isNeedBillingAddress()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.organisation, new IdentityOrganisationsCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.billingAddress, new BillingAddressCellRenderer(getLocale(), true)));
		}
		
		DefaultFlexiColumnModel numCol = new DefaultFlexiColumnModel(UserOverviewCols.numOfModifications,
				new DualNumberCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(numCol);
		
		if (membersContext.isNeedBillingAddress()) {
			ActionsColumnModel toolsColumn = new ActionsColumnModel(UserOverviewCols.tools);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		tableModel = new UsersOverviewTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_curriculum_element_member_overview");
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
			if (membersContext.isNeedBillingAddress() && row.getModificationSummary().addition()) {
				row.setOrganisations(membersContext.getIdentityKeyToUserOrganisations().get(identity.getKey()));
				setBillingAddress(row);
				
				forge(row);
			}
			
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
			if((memberStatus == GroupMembershipStatus.active || memberStatus == GroupMembershipStatus.reservation)
					&& membership == null && reservation == null) {
				add = true;
				numOfModifications++;
			} else if(memberStatus == GroupMembershipStatus.active && membership == null && reservation != null) {
				modify = true;
				numOfModifications++;
			}
		}
		return new ModificationStatusSummary(modify, add, false, numOfModifications);
	}

	private void setBillingAddress(UserRow row) {
		if (!row.getModificationSummary().addition()) {
			return;
		}
		
		BillingAddress uniqueUserBillingAddress = null;
		BillingAddressSearchParams baSearchParams = new BillingAddressSearchParams();
		baSearchParams.setOrganisations(row.getOrganisations());
		List<BillingAddress> billingAddresses = acService.getBillingAddresses(baSearchParams);
		if (billingAddresses.size() == 1) {
			uniqueUserBillingAddress = billingAddresses.get(0);
		} else if (billingAddresses.size() > 1) {
			row.setMultiBillingAddressAvailable(true);
		} else if (billingAddresses.size() == 0) {
			row.setNoBillingAddressAvailable(true);
		}
		
		BillingAddress billingAddress = null;
		if (membersContext.getIdentityKeyToBillingAddress() != null) {
			billingAddress = membersContext.getIdentityKeyToBillingAddress().get(row.getIdentity().getKey());
		}
		if (billingAddress == null) {
			billingAddress = membersContext.getBillingAddress();
		}
		if (billingAddress == null && uniqueUserBillingAddress != null) {
			billingAddress = uniqueUserBillingAddress;
			membersContext.getIdentityKeyToBillingAddress().put(row.getIdentity().getKey(), uniqueUserBillingAddress);
		}
		row.setBillingAddress(billingAddress);
	}
	
	private void forge(UserRow row) {
		if (membersContext.isNeedBillingAddress() && row.getModificationSummary().addition()
				&& (row.isNoBillingAddressAvailable() || row.isMultiBillingAddressAvailable())) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), CMD_TOOLS);
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addressSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (addressSelectionCtrl.getUserObject() instanceof UserRow row) {
					updateBillingAddress(ureq, row, addressSelectionCtrl.getBillingAddress());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (organisationsSmallListCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();	
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(organisationsSmallListCtrl);
		removeAsListenerAndDispose(addressSelectionCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		organisationsSmallListCtrl = null;
		addressSelectionCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
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
				} else if(IdentityOrganisationsCellRenderer.CMD_OTHER_ORGANISATIONS.equals(cmd)) {
					String targetId = IdentityOrganisationsCellRenderer.getOtherOrganisationsId(se.getIndex());
					UserRow row = tableModel.getObject(se.getIndex());
					doShowOrganisations(ureq, targetId, row);
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				UserRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenMemberDetails(ureq, row);
				} else {
					doCloseMemberDetails(row);
				}
			}
	 } else if(source instanceof FormLink link && CMD_TOOLS.equals(link.getCmd())
				&& link.getUserObject() instanceof UserRow row) {
			doOpenTools(ureq, row, link);
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if (membersContext.isNeedBillingAddress()) {
			boolean missingBillingAddress = tableModel.getObjects().stream()
					.anyMatch(row -> row.getModificationSummary().addition() && row.getBillingAddress() == null);
			if (missingBillingAddress) {
				allOk &= false;
				tableEl.setErrorKey("error.missing.billing.address");
			}
		}
		
		return allOk;
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
		
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		List<CurriculumRoles> rolesToModify = List.of(membersContext.getRoleToModify());
		boolean withConfirmation = membersContext.hasModificationsWithConfirmation();
		MemberDetailsConfig config = new MemberDetailsConfig(profileConfig, rolesToModify, false, false, false, true, withConfirmation,
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
	
	private void doShowOrganisations(UserRequest ureq, String elementId, UserRow row) {
		List<OrganisationWithParents> organisations = row.getOrganisations();
		organisationsSmallListCtrl = new OrganisationsSmallListController(ureq, getWindowControl(), organisations);
		listenTo(organisationsSmallListCtrl);
		
		String title = translate("num.of.organisations", Integer.toString(organisations.size()));
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), organisationsSmallListCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doSelectBillingAddress(UserRequest ureq, UserRow row) {
		if (guardModalController(addressSelectionCtrl)) return;
		
		addressSelectionCtrl = new BillingAddressSelectionController(ureq, getWindowControl(), true, false, false,
				false, row.getIdentity(), row.getBillingAddress());
		addressSelectionCtrl.setUserObject(row);
		listenTo(addressSelectionCtrl);
		
		String title = translate("select.billing.address");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addressSelectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void updateBillingAddress(UserRequest ureq, UserRow row, BillingAddress billingAddress) {
		row.setBillingAddress(billingAddress);
		membersContext.getIdentityKeyToBillingAddress().put(row.getIdentityKey(), billingAddress);
		tableEl.reset(false, false, true);
		validateFormLogic(ureq);
	}
	
	private void doOpenTools(UserRequest ureq, UserRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private class ToolsController extends BasicController {

		private Link billingAddressLink;

		private final VelocityContainer mainVC;
		
		private final UserRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, UserRow row) {
			super(ureq, wControl, Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
			this.row = row;
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(1);
			billingAddressLink = addLink("select.billing.address", "select.billing.address", "o_icon o_icon-fw o_icon_billing_address", links);
			
			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(billingAddressLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doSelectBillingAddress(ureq, row);
			}
		}
	}
	
}
