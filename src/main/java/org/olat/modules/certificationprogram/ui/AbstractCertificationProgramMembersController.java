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
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.certificationprogram.ui.CertificationProgramMembersTableModel.CertificationProgramMembersCols;
import org.olat.modules.certificationprogram.ui.component.CertificationProgramMemberWithInfosCreationComparator;
import org.olat.modules.certificationprogram.ui.component.CertificationStatusCellRenderer;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
abstract class AbstractCertificationProgramMembersController extends FormBasicController implements FlexiTableComponentDelegate {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();

	protected static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	protected static final String FILTER_STATUS = "Status";
	protected static final String FILTER_EXPIRATION = "Expiration";
	protected static final String FILTER_EXPIRE_SOON = "ExpireSoon";
	protected static final String FILTER_RECERTIFIED = "Recertified";
	protected static final String FILTER_NOT_ENOUGH_CREDIT_POINTS = "NotEnoughCreditPoints";

	protected FlexiFiltersTab allTab;
	
	protected FlexiTableElement tableEl;
	protected CertificationProgramMembersTableModel tableModel;
	protected final VelocityContainer detailsVC;
	protected final TooledStackedPanel toolbarPanel;
	
	protected final CertificationProgram certificationProgram;
	protected final List<UserPropertyHandler> userPropertyHandlers;
	protected final CertificationProgramSecurityCallback secCallback;
	
	private ContactFormController contactCtrl;
	
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	protected CertificationProgramService certificationProgramService;
	
	public AbstractCertificationProgramMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, "members_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.certificationProgram = certificationProgram;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		detailsVC = createVelocityContainer("member_details");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTableForm(formLayout);
	}
	
	protected void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String name = userPropertyHandler.getName();
			String action = UserConstants.NICKNAME.equals(name) || UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name)
					? TOGGLE_DETAILS_CMD : null;
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, action, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificationProgramMembersCols.identityStatus,
				new CertificationStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CertificationProgramMembersCols.certificateStatus,
				new CertificationStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificationProgramMembersCols.recertificationCount));
		if(certificationProgram.isValidityEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificationProgramMembersCols.validUntil,
				new DateFlexiCellRenderer(getLocale())));
		}
		initColumns(columnsModel);
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(CertificationProgramMembersCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		BigDecimal creditPoints = certificationProgram.getCreditPoints();
		tableModel = new CertificationProgramMembersTableModel(columnsModel, creditPoints, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		initFilters();
		initFiltersPresets();
	}
	
	protected abstract void initColumns(FlexiTableColumnModel columnsModel);
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		initFilters(filters);
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected abstract void initFilters(List<FlexiTableExtendedFilter> filters);
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		initFiltersPresets(tabs);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected abstract void initFiltersPresets(List<FlexiFiltersTab> tabs);
	

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		if(rowObject instanceof CertificationProgramMemberRow memberRow
				&& memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}
	
	protected final void loadModel(UserRequest ureq) {
		final Date referenceDate = ureq.getRequestTimestamp();
		CertificationProgramMemberSearchParameters searchParams = getSearchParams();
		searchParams.setCertificationProgram(certificationProgram);
		List<CertificationProgramMemberWithInfos> membersWithInfos = new ArrayList<>(certificationProgramService.getMembers(searchParams, referenceDate, -1));

		// Sort last certificates first
		Collections.sort(membersWithInfos, new CertificationProgramMemberWithInfosCreationComparator());
		// Only one per user
		final Set<Identity> ids = new HashSet<>();
		List<CertificationProgramMemberRow> rows = membersWithInfos.stream()
				.filter(infos -> infos.identity() != null)
				.filter(infos -> {
					if(ids.contains(infos.identity())) {
						return false;
					}
					ids.add(infos.identity());
					return true;
				})
				.map(infos -> forgeRow(infos, referenceDate))
				.toList();

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	protected final CertificationProgramMemberRow forgeRow(CertificationProgramMemberWithInfos infos, Date referenceDate) {
		Certificate certificate = infos.certificate();
		CertificationStatus certificateStatus = CertificationStatus.evaluate(certificate, referenceDate);
		CertificationIdentityStatus identityStatus = CertificationIdentityStatus.evaluate(certificate, referenceDate);
		NextRecertificationInDays nextRecertification = NextRecertificationInDays.valueOf(certificate, referenceDate);
		return new CertificationProgramMemberRow(infos.identity(), certificate, nextRecertification, certificateStatus,
				identityStatus, infos.wallet(), userPropertyHandlers, getLocale());
	}
	
	protected final void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	protected abstract CertificationProgramMemberSearchParameters getSearchParams();
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(contactCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				toolbarPanel.popController(contactCtrl);
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}

	protected void cleanUp() {
		removeAsListenerAndDispose(contactCtrl);
		contactCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					CertificationProgramMemberRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseMemberDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenMemberDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CertificationProgramMemberRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				CertificationProgramMemberRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenMemberDetails(ureq, row);
				} else {
					doCloseMemberDetails(row);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected abstract void doOpenTools(UserRequest ureq, CertificationProgramMemberRow selectedRow, String targetId);
	
	protected final void doOpenMemberDetails(UserRequest ureq, CertificationProgramMemberRow row) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		CertificationProgramMemberDetailsController detailsCtrl = new CertificationProgramMemberDetailsController(ureq, getWindowControl(), mainForm,
				certificationProgram, row.getNextRecertification(), assessedIdentity);
		detailsCtrl.setUserObject(row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	protected final void doCloseMemberDetails(CertificationProgramMemberRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	protected void doOpenContact(UserRequest ureq, CertificationProgramMemberRow row) {
		removeAsListenerAndDispose(contactCtrl);
		
		Identity programMeber = securityManager.loadIdentityByKey(row.getIdentityKey());
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList emailList = new ContactList(certificationProgram.getDisplayName());
		emailList.add(programMeber);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("Contact");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactCtrl = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactCtrl);
		
		toolbarPanel.pushController(translate("contact.title"), contactCtrl);
	}

}
