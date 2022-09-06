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
package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.admin.user.bulkChange.UserBulkChangeStep00;
import org.olat.admin.user.bulkChange.UserBulkChanges;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.model.IdentityPropertiesRow;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.UserSearchTableModel.UserCols;
import org.olat.user.ui.admin.bulk.move.UserBulkMove;
import org.olat.user.ui.admin.bulk.move.UserBulkMove_1_ChooseRoleStep;
import org.olat.user.ui.admin.lifecycle.ConfirmDeleteUserController;
import org.olat.user.ui.admin.lifecycle.IdentityDeletedEvent;
import org.olat.user.ui.identity.UserInfoSegmentedController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableController extends FormBasicController implements Activateable2 {
	
	public static final String USER_PROPS_ID = "org.olat.admin.user.ExtendedIdentitiesTableDataModel";
	public static final int USER_PROPS_OFFSET = 500;
	public static final String ALL_TAB_ID = "All";
	public static final String ACTIVE_TAB_ID = "Active";
	public static final String PERMANENT_ID = "Permanent";
	public static final String INACTIVE_TAB_ID = "Inactive";
	public static final String PENDING_TAB_ID = "Pending";
	public static final String LOGIN_DENIED_TAB_ID = "LoginDenied";
	public static final String FILTER_STATUS = "status";
	public static final String FILTER_ORGANISATIONS = "organisations";
	
	private Link nextLink;
	private Link previousLink;
	private FormLink mailButton;
	private FormLink bulkMovebutton;
	private FormLink bulkDeleteButton;
	private FormLink bulkStatusButton;
	private FormLink bulkChangesButton;
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab inactiveTab;
	private FlexiFiltersTab permanentTab;
	private FlexiFiltersTab pendingTab;
	private FlexiFiltersTab loginDeniedTab;
	
	private FlexiTableElement tableEl;
	private UserSearchTableModel tableModel;
	private TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private UserAdminController userAdminCtr;
	private ContactFormController contactCtr;
	private UserInfoSegmentedController userInfoCtr;
	private ChangeStatusController changeStatusController;
	private StepsMainRunController userBulkMoveController;
	private StepsMainRunController userBulkChangesController;
	private ConfirmDeleteUserController confirmDeleteUserController;
	
	private final Roles roles;
	private final UserSearchTableSettings settings;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final List<Organisation> manageableOrganisations;
	
	private boolean tableDirty = false;
	private SearchIdentityParams currentSearchParams;

	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private UserBulkChangeManager userBulkChangesManager;
	
	public UserSearchTableController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserSearchTableSettings settings) {
		super(ureq, wControl, "search_table");
		this.settings = settings;
		this.stackPanel = stackPanel;
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.principal,
				OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		
		initForm(ureq);
		if(stackPanel != null) {
			stackPanel.addListener(this);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.status, new IdentityStatusCellRenderer(getTranslator())));
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UserCols.id));
		}

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(settings.isLifecycleColumnsDefault(), UserCols.lastLogin));

		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(settings.isLifecycleColumnsDefault(), UserCols.inactivationDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UserCols.expirationDate, dateRenderer));
		if(userModule.isUserAutomaticDeactivation()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(settings.isLifecycleColumnsDefault(), UserCols.daysToInactivation));
		}
		if(userModule.isUserAutomaticDeletion()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(settings.isLifecycleColumnsDefault(), UserCols.daysToDeletion));
		}
		if(settings.isVCard()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.vcard", translate("table.identity.vcard"), "vcard"));
		}
		
		tableModel = new UserSearchTableModel(new EmptyDataSource(), columnsModel, userModule, userLifecycleManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("error.no.user.found", null, "o_icon_user");
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(settings.isTableSearch());
		tableEl.setAndLoadPersistedPreferences(ureq, "user_search_table-v4");
		
		initBulkActions(formLayout);
		initFilters(settings.isStatusFilter(), settings.isOrganisationsFilter());
		if(settings.isStatusFilter()) {
			initFiltersPresets(ureq);
		}
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of());
		allTab.setElementCssClass("o_sel_users_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB_ID, translate("rightsForm.status.activ"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, Identity.STATUS_ACTIV.toString())));
		activeTab.setElementCssClass("o_sel_users_active");
		activeTab.setFiltersExpanded(true);
		tabs.add(activeTab);
		
		permanentTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PERMANENT_ID, translate("rightsForm.status.permanent"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, Identity.STATUS_PERMANENT.toString())));
		permanentTab.setElementCssClass("o_sel_users_permanent");
		permanentTab.setFiltersExpanded(true);
		tabs.add(permanentTab);
		
		pendingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PENDING_TAB_ID, translate("rightsForm.status.pending"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, Identity.STATUS_PENDING.toString())));
		pendingTab.setElementCssClass("o_sel_users_pending");
		pendingTab.setFiltersExpanded(true);
		tabs.add(pendingTab);
		
		inactiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INACTIVE_TAB_ID, translate("rightsForm.status.inactive"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, Identity.STATUS_INACTIVE.toString())));
		inactiveTab.setElementCssClass("o_sel_users_inactive");
		inactiveTab.setFiltersExpanded(true);
		tabs.add(inactiveTab);
		
		loginDeniedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LOGIN_DENIED_TAB_ID, translate("rightsForm.status.inactive"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, Identity.STATUS_LOGIN_DENIED.toString())));
		loginDeniedTab.setElementCssClass("o_sel_users_login_denied");
		loginDeniedTab.setFiltersExpanded(true);
		tabs.add(loginDeniedTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters(boolean withStatusFilters, boolean withOrganisationsFilters) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(withStatusFilters) {
			SelectionValues statusValues = new SelectionValues();
			statusValues.add(SelectionValues.entry(Identity.STATUS_ACTIV.toString(), translate("rightsForm.status.activ")));
			statusValues.add(SelectionValues.entry(Identity.STATUS_PERMANENT.toString(), translate("rightsForm.status.permanent")));
			statusValues.add(SelectionValues.entry(Identity.STATUS_PENDING.toString(), translate("rightsForm.status.pending")));
			statusValues.add(SelectionValues.entry(Identity.STATUS_INACTIVE.toString(), translate("rightsForm.status.inactive")));
			statusValues.add(SelectionValues.entry(Identity.STATUS_LOGIN_DENIED.toString(), translate("rightsForm.status.login_denied")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
					FILTER_STATUS, statusValues, true));
		}
		
		if(withOrganisationsFilters) {
			Collections.sort(manageableOrganisations, new OrganisationNameComparator(getLocale()));
			SelectionValues organisationsValues = new SelectionValues();
			for(Organisation organisation:manageableOrganisations) {
				organisationsValues.add(SelectionValues.entry(organisation.getKey().toString(), organisation.getDisplayName()));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
					FILTER_ORGANISATIONS, organisationsValues, true));
		}
		
		if(!filters.isEmpty()) {
			tableEl.setFilters(true, filters, false, false);
		}
	}
	
	private void initBulkActions(FormItemContainer formLayout) {
		if(settings.isBulkMail()) {
			mailButton = uifactory.addFormLink("command.mail", formLayout, Link.BUTTON);
			tableEl.addBatchButton(mailButton);
		}
		if(roles.isAdministrator() || roles.isUserManager() || roles.isRolesManager()) {
			bulkChangesButton = uifactory.addFormLink("bulkChange.title", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkChangesButton);
		}
		if(roles.isAdministrator() || roles.isRolesManager()) {
			bulkStatusButton = uifactory.addFormLink("bulkStatus.title", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkStatusButton);
		}
		if(roles.isAdministrator() ) {
			bulkDeleteButton = uifactory.addFormLink("bulkDelete.title", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkDeleteButton);
		}

		bulkMovebutton = uifactory.addFormLink("command.move.to.organisation", formLayout, Link.BUTTON);
		bulkMovebutton.setVisible(settings.isBulkOrganisationMove());
		tableEl.addBatchButton(bulkMovebutton);
	}

	public void loadModel(SearchIdentityParams params) {
		if (settings.isStatusFilter() && params.getExactStatusList() != null && !params.getExactStatusList().isEmpty()) {
			Collection<String> keys = params.getExactStatusList().stream()
					.map(i -> Integer.toString(i))
					.collect(Collectors.toSet());
			tableEl.setSelectedFilterKeys(keys);
			params.setExactStatusList(null);
		}
		currentSearchParams = params;
		UserSearchDataSource dataSource = new UserSearchDataSource(params, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
		bulkMovebutton.setVisible(settings.isBulkOrganisationMove()
				&& currentSearchParams != null
				&& currentSearchParams.getOrganisations() != null
				&& currentSearchParams.getOrganisations().size() == 1);
	}
	
	public void loadModel(List<Identity> identityList) {
		// Activate all filters
		List<String> statusList = new ArrayList<>();
		statusList.add(Identity.STATUS_ACTIV.toString());
		statusList.add(Identity.STATUS_PERMANENT.toString());
		statusList.add(Identity.STATUS_LOGIN_DENIED.toString());
		statusList.add(Identity.STATUS_PENDING.toString());
		statusList.add(Identity.STATUS_INACTIVE.toString());
		tableEl.setSelectedFilterKeys(statusList);
		
		currentSearchParams = null;
		IdentityListDataSource dataSource = new IdentityListDataSource(identityList, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
		bulkMovebutton.setVisible(false);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Identity".equalsIgnoreCase(type)) {
			Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
			List<IdentityPropertiesRow> rows = tableModel.getObjects();
			for(IdentityPropertiesRow row:rows) {
				if(row != null && row.getIdentityKey().equals(identityKey)) {
					doSelectIdentity(ureq, row).activate(ureq, entries.subList(1, entries.size()), null);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userBulkChangesController == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
			}
			if (event == Event.CHANGED_EVENT) {
				doFinishBulkEdit();
			} else if (event == Event.DONE_EVENT) {
				showError("bulkChange.failed");
			}
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cleanUp();
			}	
		} else if(userBulkMoveController == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
			}
			if (event == Event.CHANGED_EVENT) {
				reloadTable();
			} else if (event == Event.DONE_EVENT) {
				showError("bulkChange.failed");
			}
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cleanUp();
			}
		} else if(changeStatusController == source || confirmDeleteUserController == source) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				reloadTable();
			}
			cmc.deactivate();
			cleanUp();
		} else if(userAdminCtr == source) {
			if(event instanceof IdentityDeletedEvent) {
				reloadTable();
				stackPanel.popController(userAdminCtr);
			} else if(event == Event.CHANGED_EVENT) {
				tableDirty = true;
			} else if(event instanceof ReloadIdentityEvent) {
				doSelectIdentity(ureq, userAdminCtr.getEditedIdentity());
			}
		} else if(contactCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(userInfoCtr == source) {
			stackPanel.popController(userInfoCtr);
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteUserController);
		removeAsListenerAndDispose(userBulkChangesController);
		removeAsListenerAndDispose(userBulkMoveController);
		removeAsListenerAndDispose(changeStatusController);
		removeAsListenerAndDispose(userInfoCtr);
		removeAsListenerAndDispose(contactCtr);
		removeAsListenerAndDispose(cmc);
		confirmDeleteUserController = null;
		userBulkChangesController = null;
		userBulkMoveController = null;
		changeStatusController = null;
		userInfoCtr = null;
		contactCtr = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				IdentityPropertiesRow userRow = tableModel.getObject(te.getIndex());
				if("select".equals(cmd)) {
					doSelectIdentity(ureq, userRow);
				} else if("vcard".equals(cmd)) {
					doSelectVcard(ureq, userRow);
				}
			}
		} else if(mailButton == source) {
			doMail(ureq);
		} else if(bulkChangesButton == source) {
			doBulkEdit(ureq);
		}  else if(bulkDeleteButton == source) {
			doBulkDelete(ureq);
		} else if(bulkMovebutton == source) {
			doBulkMove(ureq);
		} else if(bulkStatusButton == source) {
			doBulkStatus(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(nextLink == source) {
			Index index = (Index)nextLink.getUserObject();
			doNext(ureq, index.getRow(), index.isVcard()); 
		} else if(previousLink == source) {
			Index index = (Index)previousLink.getUserObject();
			doPrevious(ureq, index.getRow(), index.isVcard()); 
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(tableDirty && pe.getController() == userAdminCtr) {
					tableEl.reset(false, false, true);
					tableDirty = false;
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doNext(UserRequest ureq, IdentityPropertiesRow current, boolean vcard) {
		stackPanel.popUpToController(this);
		
		IdentityPropertiesRow nextRow = tableModel.getNextObject(current, tableEl);
		if(vcard) {
			doSelectVcard(ureq, nextRow);
		} else {
			doSelectIdentity(ureq, nextRow);
		}
	}
	
	private void doPrevious(UserRequest ureq, IdentityPropertiesRow current, boolean vcard) {
		stackPanel.popUpToController(this);
		
		IdentityPropertiesRow previousRow = tableModel.getPreviousObject(current, tableEl);
		if(vcard) {
			doSelectVcard(ureq, previousRow);
		} else {
			doSelectIdentity(ureq, previousRow);
		}
	}
	

	private UserAdminController doSelectIdentity(UserRequest ureq, Identity user) {
		IdentityPropertiesRow userRow = tableModel.getObject(user);
		if(userRow != null) {
			return doSelectIdentity(ureq, userRow); 
		}
		return null;
	}

	private UserAdminController doSelectIdentity(UserRequest ureq, IdentityPropertiesRow userRow) {
		removeAsListenerAndDispose(userAdminCtr);
		
		Identity identity = securityManager.loadIdentityByKey(userRow.getIdentityKey());

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);

		userAdminCtr = new UserAdminController(ureq, bwControl, stackPanel, identity);
		userAdminCtr.setBackButtonEnabled(false);
		listenTo(userAdminCtr);

		String fullName = userManager.getUserDisplayName(identity);
		stackPanel.pushController(fullName, userAdminCtr);
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
		updateNextPrevious(userRow, false);
		return userAdminCtr;
	}
	
	private void doSelectVcard(UserRequest ureq, IdentityPropertiesRow userRow) {
		Identity identity = securityManager.loadIdentityByKey(userRow.getIdentityKey());
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("VCard", identity.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		userInfoCtr = new UserInfoSegmentedController(ureq, bwControl, identity, true, true);
		listenTo(userInfoCtr);
		
		String fullName = userManager.getUserDisplayName(identity);
		stackPanel.pushController(fullName, userInfoCtr);
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
		updateNextPrevious(userRow, true);	
	}
	
	private void updateNextPrevious(IdentityPropertiesRow row, boolean vcard) {
		int index = tableModel.getIndexOfObject(row);
		previousLink.setUserObject(new Index(row, vcard));
		nextLink.setUserObject(new Index(row, vcard));
		previousLink.setEnabled(index > 0);
		nextLink.setEnabled(index + 1 < tableModel.getRowCount());
	}
	
	private void doMail(UserRequest ureq) {
		if(guardModalController(contactCtr)) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning(new All());
		if(identities.isEmpty()) {
			return;
		}
		
		// create e-mail message
		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList contacts = new ContactList(translate("mailto.userlist"));
		contacts.addAllIdentites(identities);
		cmsg.addEmailTo(contacts);

		// create contact form controller with ContactMessage
		contactCtr = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", contactCtr.getInitialComponent(),
				true, translate("command.mail"), true);
		listenTo(cmc);
		cmc.activate(); 
	}

	private void doBulkEdit(UserRequest ureq) {
		if(userBulkChangesController != null) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning(new All());
		if(identities.isEmpty()) {
			return;
		}
		
		OrganisationRef selectedOrganisation = null;
		if(currentSearchParams != null && currentSearchParams.getOrganisations() != null
				&& currentSearchParams.getOrganisations().size() == 1) {
			selectedOrganisation = currentSearchParams.getOrganisations().get(0);
		}

		// valid selection: load in wizard
		final UserBulkChanges userBulkChanges = new UserBulkChanges(selectedOrganisation);
		Step start = new UserBulkChangeStep00(ureq, identities, userBulkChanges);
		// callback executed in case wizard is finished.
		StepRunnerCallback finish = (uureq, wwControl, runContext) -> {
			// all information to do now is within the runContext saved
			boolean hasChanges = false;
			try {
				if (userBulkChanges.isValidChange()) {
					Map<String, String> attributeChangeMap = userBulkChanges.getAttributeChangeMap();
					Map<OrganisationRoles, String> roleChangeMap = userBulkChanges.getRoleChangeMap();
					userBulkChanges.getStatus();
					List<Long> ownGroups = userBulkChanges.getOwnerGroups();
					List<Long> partGroups = userBulkChanges.getParticipantGroups();
					List<String> notUpdatedIdentities = new ArrayList<>();
					if (!attributeChangeMap.isEmpty() || !roleChangeMap.isEmpty()
							|| !ownGroups.isEmpty() || !partGroups.isEmpty()
							|| userBulkChanges.getStatus() != null){
						Roles actingRoles = uureq.getUserSession().getRoles();
						Identity actingIdentity = uureq.getIdentity();
						userBulkChangesManager.changeSelectedIdentities(identities, userBulkChanges, notUpdatedIdentities,
							isAdministrativeUser, getTranslator(), actingIdentity, actingRoles);
						hasChanges = true;
					}
					runContext.put("notUpdatedIdentities", notUpdatedIdentities);
					runContext.put("selectedIdentities", identities);
				}
			} catch (Exception e) {
				logError("", e);
			}
			// signal correct completion and tell if changes were made or not.
			return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
		};

		userBulkChangesController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("bulkChange.title"), "o_sel_user_bulk_change_wizard");
		listenTo(userBulkChangesController);
		getWindowControl().pushAsModalDialog(userBulkChangesController.getInitialComponent());
	}
	
	private void doFinishBulkEdit() {
		@SuppressWarnings("unchecked")
		List<Identity> selectedIdentities = (List<Identity>)userBulkChangesController.getRunContext().get("selectedIdentities");
		@SuppressWarnings("unchecked")
		List<String> notUpdatedIdentities = (List<String>)userBulkChangesController.getRunContext().get("notUpdatedIdentities");
		
		Integer selIdentCount = selectedIdentities.size();
		if (!notUpdatedIdentities.isEmpty()) {
			Integer notUpdatedIdentCount = notUpdatedIdentities.size();
			Integer sucChanges = selIdentCount - notUpdatedIdentCount;
			StringBuilder changeErrors = new StringBuilder(1024);
			for (String err : notUpdatedIdentities) {
				if(changeErrors.length() > 0) changeErrors.append("<br>");
				changeErrors.append(err);
			}
			getWindowControl().setError(translate("bulkChange.partialsuccess",
					sucChanges.toString(), selIdentCount.toString(), changeErrors.toString()));
		} else {
			showInfo("bulkChange.success");
		}
		// reload the data
		tableEl.reset(true, true, true);
	}
	
	private void doBulkDelete(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentitiesWithWarning(new ExcludePermanent());
		if(identities.isEmpty()) {
			return;
		}
		
		confirmDeleteUserController = new ConfirmDeleteUserController(ureq, getWindowControl(), identities);
		listenTo(confirmDeleteUserController);

		String title;
		if(identities.size() == 1) {
			String fullname = userManager.getUserDisplayName(identities.get(0));
			title = translate("delete.user.data.title", fullname);
		} else {
			title = translate("delete.users.data.title", Integer.toString(identities.size()));
		}
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteUserController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate(); 
	}
	
	private List<Identity> getSelectedIdentitiesWithWarning(Predicate<Identity> filter) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		if(selections.isEmpty()) {
			showWarning("msg.selectionempty");
			return Collections.emptyList();
		}
		
		List<Long> identityKeys = new ArrayList<>(selections.size());
		for(Integer selection:selections) {
			IdentityPropertiesRow row = tableModel.getObject(selection.intValue());
			if(row != null) {
				identityKeys.add(row.getIdentityKey());
			}
		}
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		identities = identities.stream()
				.filter(filter)
				.collect(Collectors.toList());
		if(identities.isEmpty()) {
			showWarning("msg.selectionempty");
			return Collections.emptyList();
		}
		return identities;
	}
	
	private void doBulkStatus(UserRequest ureq) {
		if(guardModalController(changeStatusController)) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning(new ExcludePermanent());
		if(identities.isEmpty()) {
			return;
		}
		
		changeStatusController = new ChangeStatusController(ureq, getWindowControl(), identities);
		listenTo(changeStatusController);
		
		String title;
		if(identities.size() == 1) {
			String fullName = userManager.getUserDisplayName(identities.get(0));
			title = translate("bulkStatus.title.single", StringHelper.escapeHtml(fullName));
		} else {
			title = translate("bulkStatus.title.plural", Integer.toString(identities.size()));
		}
		cmc = new CloseableModalController(getWindowControl(), "close", changeStatusController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkMove(UserRequest ureq) {
		if(guardModalController(userBulkMoveController)) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning(new All());
		if(identities.isEmpty()) {
			return;
		}
		Organisation organisation = null;
		if(currentSearchParams != null && currentSearchParams.getOrganisations() != null && currentSearchParams.getOrganisations().size() == 1) {
			organisation = organisationService.getOrganisation(currentSearchParams.getOrganisations().get(0));
		}
		if(organisation == null) {
			return;
		}
		
		// valid selection: load in wizard
		final UserBulkMove userBulkMove = new UserBulkMove(organisation, identities);
		Step start = new UserBulkMove_1_ChooseRoleStep(ureq, userBulkMove);
		// callback executed in case wizard is finished.
		StepRunnerCallback finish = (uureq, wwControl, runContext) -> {
			// all information to do now is within the runContext saved
			boolean hasChanges = userBulkMove.getRoles() != null && !userBulkMove.getRoles().isEmpty()
					&& userBulkMove.getIdentitiesToMove() != null && !userBulkMove.getIdentitiesToMove().isEmpty();
			if(hasChanges) {
				organisationService.moveMembers(userBulkMove.getOrganisation(), userBulkMove.getTargetOrganisation(),
						userBulkMove.getIdentitiesToMove(), userBulkMove.getRoles());
			}
			// signal correct completion and tell if changes were made or not.
			return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
		};

		userBulkMoveController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("bulkChange.title"), "o_sel_user_bulk_move_wizard");
		listenTo(userBulkMoveController);
		getWindowControl().pushAsModalDialog(userBulkMoveController.getInitialComponent());
	}
	
	public void reloadTable() {
		tableEl.reset(true, true, true);
	}
	
	private final class Index {
		private final boolean vcard;
		private final IdentityPropertiesRow row;
		
		public Index(IdentityPropertiesRow row, boolean vcard) {
			this.row = row;
			this.vcard = vcard;
		}

		public boolean isVcard() {
			return vcard;
		}
		
		public IdentityPropertiesRow getRow() {
			return row;
		}
	}
	
	private static class ExcludePermanent implements Predicate<Identity> {
		@Override
		public boolean test(Identity t) {
			return t != null && !Identity.STATUS_PERMANENT.equals(t.getStatus());
		}
	}
	
	private static class All implements Predicate<Identity> {
		@Override
		public boolean test(Identity t) {
			return t != null;
		}
	}
	
	private final class EmptyDataSource implements FlexiTableDataSourceDelegate<IdentityPropertiesRow> {

		@Override
		public int getRowCount() {
			return 0;
		}

		@Override
		public List<IdentityPropertiesRow> reload(List<IdentityPropertiesRow> rows) {
			return Collections.emptyList();
		}

		@Override
		public ResultInfos<IdentityPropertiesRow> getRows(String query, List<FlexiTableFilter> filters,
				int firstResult, int maxResults, SortKey... orderBy) {
			return new DefaultResultInfos<>();
		}
	}
}
