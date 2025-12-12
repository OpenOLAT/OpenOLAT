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
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramCandidate;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.certificationprogram.ui.CertificationProgramCertifiedMembersController;
import org.olat.modules.certificationprogram.ui.wizard.UsersOverviewTableModel.UserOverviewCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.IdentityOrganisationsCellRenderer;
import org.olat.user.ui.organisation.OrganisationsSmallListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersOverviewController extends StepFormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private UsersOverviewTableModel tableModel;
	
	private CloseableCalloutWindowController calloutCtrl;
	private OrganisationsSmallListController organisationsSmallListCtrl;
	
	private final AddProgramMembersContext membersContext;
	private final CertificationProgram certificationProgram;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	@Autowired
	private CertificationProgramService certificationProgramService;

	public UsersOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, AddProgramMembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_overview");
		setTranslator(Util.createPackageTranslator(CertificationProgramCertifiedMembersController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.membersContext = membersContext;
		certificationProgram = membersContext.getProgram();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.currentMembership,
				new UserMembershipStatusCellRenderer(getTranslator())));
		if (organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserOverviewCols.organisations,
					new IdentityOrganisationsCellRenderer()));
		}
		
		tableModel = new UsersOverviewTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel(UserRequest ureq) {
		List<Identity> identities = membersContext.getSearchedIdentities();
		List<UserRow> rows = identities == null ? List.of() : identities.stream()
				.map(id -> new UserRow(id, userPropertyHandlers, getLocale()))
				.toList();
		
		loadMemberships(ureq.getRequestTimestamp(), rows);
		identityPowerSearchQueries.appendOrganisations(rows);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		if(rows.size() == 1) {
			tableEl.setMultiSelectedIndex(Set.of(Integer.valueOf(0)));
		}
	}

	private void loadMemberships(Date referenceDate, List<UserRow> rows) {
		Map<Long,UserRow> rowsMap = rows.stream()
				.collect(Collectors.toMap(r -> r.getIdentityKey(), r -> r, (u, v) -> u));
		
		CertificationProgramMemberSearchParameters activeSearchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		activeSearchParams.setType(Type.CERTIFIED);
		setStatus(rowsMap, referenceDate, activeSearchParams, UserMembershipStatus.ACTIVE);
		
		CertificationProgramMemberSearchParameters removedSearchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		removedSearchParams.setType(Type.REMOVED);
		setStatus(rowsMap, referenceDate, removedSearchParams, UserMembershipStatus.ALUMNI);

		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		List<CertificationProgramCandidate> candidates = certificationProgramService.getCandidates(searchParams);
		for(CertificationProgramCandidate candidate:candidates) {
			UserRow row = rowsMap.get(candidate.identity().getKey());
			if(row != null && row.getMembershipStatus() == null) {
				row.setMembershipStatus(UserMembershipStatus.CANDIDATE);
			}
		}
	}
	
	private void setStatus(Map<Long,UserRow> rowsMap, Date referenceDate,
			CertificationProgramMemberSearchParameters searchParams, UserMembershipStatus status) {
		List<CertificationProgramMemberWithInfos> members = certificationProgramService.getMembers(searchParams, referenceDate, -1);
		for(CertificationProgramMemberWithInfos member:members) {
			UserRow row = rowsMap.get(member.identity().getKey());
			if(row != null && row.getMembershipStatus() == null) {
				row.setMembershipStatus(status);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (calloutCtrl == source || organisationsSmallListCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(organisationsSmallListCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		organisationsSmallListCtrl = null;
		calloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(IdentityOrganisationsCellRenderer.CMD_OTHER_ORGANISATIONS.equals(cmd)) {
					String targetId = IdentityOrganisationsCellRenderer.getOtherOrganisationsId(se.getIndex());
					UserRow row = tableModel.getObject(se.getIndex());
					doShowOrganisations(ureq, targetId, row);
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
		List<UserToCertify> selectedIdentities = new ArrayList<>(indexes.size());
		for (Integer index : indexes) {
			UserRow userRow = tableModel.getObject(index.intValue());
			if (userRow != null) {
				selectedIdentities.add(new UserToCertify(userRow.getIdentity(), userRow.getMembershipStatus()));
			}
		}
		membersContext.setSelectedIdentities(selectedIdentities);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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

}
