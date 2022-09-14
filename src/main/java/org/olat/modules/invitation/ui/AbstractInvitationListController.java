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
package org.olat.modules.invitation.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.MemberListController;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupMailing;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.invitation.ui.InvitationListTableModel.InvitationCols;
import org.olat.modules.invitation.ui.component.InvitationRolesCellRenderer;
import org.olat.modules.invitation.ui.component.InvitationStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryMailing;
import org.olat.repository.RepositoryMailing.RepositoryEntryMailTemplate;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractInvitationListController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	public static final String ALL_TAB_ID = "All";
	public static final String ACTIVE_TAB_ID = "Active";
	public static final String INACTIVE_TAB_ID = "Inactive";
	public static final String FILTER_STATUS = "status";
	
	protected FormLink activateBatchButton;
	protected FlexiTableElement tableEl;
	protected InvitationListTableModel tableModel;
	
	protected int counter = 0;
	protected FlexiFiltersTab allTab;
	protected FlexiFiltersTab activeTab;
	protected FlexiFiltersTab inactiveTab;
	
	protected final boolean readOnly; 
	
	private ToolsController toolsCtrl;
	private InvitationURLController invitationUrlCtrl;
	private CloseableCalloutWindowController calloutCtrl; 

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	protected UserManager userManager;
	@Autowired
	protected InvitationService invitationService;
	
	public AbstractInvitationListController(UserRequest ureq, WindowControl wControl, String pageName, boolean readOnly) {
		super(ureq, wControl, pageName);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.readOnly = readOnly;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		SortKey defaultSortKey = initColumns(columnsModel);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.role,
				new InvitationRolesCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.status,
				new InvitationStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.invitationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.invitationLink));
		
		if(!readOnly) {
			StickyActionColumnModel toolsColumn = new StickyActionColumnModel(InvitationCols.tools);
			toolsColumn.setExportable(false);
			toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		tableModel = initTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("noinvitations", null, "o_icon_message_open", null, null, false);
		tableEl.setExportEnabled(true);
		tableEl.setElementCssClass("o_sel_invitations_list");
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(!readOnly);
		tableEl.setSearchEnabled(true);
		
		if(!readOnly) {
			activateBatchButton = uifactory.addFormLink("activate", "activate", "activate", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(activateBatchButton);
		}
		
		if(defaultSortKey != null) {
			FlexiTableSortOptions options = new FlexiTableSortOptions();
			options.setDefaultOrderBy(defaultSortKey);
			tableEl.setSortSettings(options);
		}
		
		initFilters();
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "invitations-list");
		
	}
	
	protected abstract String getTableId();
	
	protected abstract InvitationListTableModel initTableModel(FlexiTableColumnModel columnsModel);
	
	protected abstract SortKey initColumns(FlexiTableColumnModel columnsModel);
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_invitations_all");
		allTab.setFiltersExpanded(false);
		tabs.add(allTab);
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB_ID, translate("filter.active"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, InvitationStatusEnum.active.name())));
		activeTab.setElementCssClass("o_sel_invitations_active");
		activeTab.setFiltersExpanded(false);
		tabs.add(activeTab);
		
		inactiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INACTIVE_TAB_ID, translate("filter.inactive"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, InvitationStatusEnum.inactive.name())));
		inactiveTab.setElementCssClass("o_sel_invitations_inactive");
		inactiveTab.setFiltersExpanded(false);
		tabs.add(inactiveTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// life-cycle
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry("active", translate("filter.active")));
		statusValues.add(SelectionValues.entry("inactive", translate("filter.inactive")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true));

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected abstract void loadModel();
	
	protected InvitationRow forgeRow(Invitation invitation, RepositoryEntry entry, BusinessGroup businessGroup) {
		FormLink urlLink = uifactory.addFormLink("url_" + (++counter), "url", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		urlLink.setIconLeftCSS("o_icon o_icon_link o_icon-fw");
		
		FormLink toolsLink = null;
		if(invitation.getStatus() == InvitationStatusEnum.inactive) {
			toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		}
		
		InvitationRow row = new InvitationRow(invitation, entry, businessGroup, urlLink, toolsLink);
		urlLink.setUserObject(row);
		if(toolsLink != null) {
			toolsLink.setUserObject(row);
		}
		return row;
	}
	
	protected SearchInvitationParameters getSearchParameters() {
		SearchInvitationParameters params = new SearchInvitationParameters();
		params.setSearchString(tableEl.getQuickSearchString());
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_STATUS);
		if (statusFilter != null) {
			String filterValue = statusFilter.getValue();
			if (StringHelper.containsNonWhitespace(filterValue)) {
				params.setStatus(InvitationStatusEnum.valueOf(filterValue));
			} else {
				params.setStatus(null);
			}
		}
		
		return params;
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(invitationUrlCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		invitationUrlCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(invitationUrlCtrl == source) {
			if(calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if(toolsCtrl == source) {
			loadModel();
			if(calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(activateBatchButton == source) {
			doBatchInvitationStatus(InvitationStatusEnum.active);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("url".equals(link.getCmd()) && link.getUserObject() instanceof InvitationRow) {
				doOpenUrl(ureq, link.getFormDispatchId(), (InvitationRow)link.getUserObject());
			} else if("tools".equals(link.getCmd()) && link.getUserObject() instanceof InvitationRow) {
				doOpenTools(ureq, link.getFormDispatchId(), (InvitationRow)link.getUserObject());
			}
		} else if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenUrl(UserRequest ureq, String elementId, InvitationRow row) {
		String url = invitationService.toUrl(row.getInvitation());
		invitationUrlCtrl = new InvitationURLController(ureq, getWindowControl(), url);
		listenTo(invitationUrlCtrl);

		String title = translate("invitation.url.title");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				invitationUrlCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, String elementId, InvitationRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doBatchInvitationStatus(InvitationStatusEnum status) {
		List<InvitationRow> invitationRows = getSelectedRows();
		for(InvitationRow invitationRow:invitationRows) {
			Invitation invitation = invitationService.getInvitation(invitationRow.getInvitation());
			invitation.setStatus(status);
			invitation = invitationService.update(invitation);
			invitationRow.setInvitation(invitation);
		}
		dbInstance.commit();
		
		if(status == InvitationStatusEnum.active) {
			MailerResult results = new MailerResult();
			for(InvitationRow invitationRow:invitationRows) {
				MailerResult result = sendEmail(invitationRow.getInvitation(), invitationRow.getRepositoryEntry(), invitationRow.getBusinessGroup());
				results.append(result);
			}
			if(results.getReturnCode() != MailerResult.OK) {
				MailHelper.printErrorsAndWarnings(results, getWindowControl(), false, getLocale());
			}
		}

		loadModel();
	}
	
	private List<InvitationRow> getSelectedRows() {
		Set<Integer> selectedKeys = tableEl.getMultiSelectedIndex();
		List<InvitationRow> invitations = new ArrayList<>(selectedKeys.size());
		for(Integer index:selectedKeys) {
			InvitationRow row = tableModel.getObject(index.intValue());
			invitations.add(row);
		}
		return invitations;
	}
	
	private void doInvitationStatus(InvitationRow row, InvitationStatusEnum status) {
		Invitation invitation = invitationService.getInvitation(row.getInvitation());
		invitation.setStatus(status);
		invitation = invitationService.update(invitation);
		dbInstance.commit();
		
		if(status == InvitationStatusEnum.active) {
			MailerResult result = sendEmail(invitation, row.getRepositoryEntry(), row.getBusinessGroup());	
			if(result.getReturnCode() != MailerResult.OK) {
				MailHelper.printErrorsAndWarnings(result, getWindowControl(), false, getLocale());
			}
		}

		loadModel();
	}
	
	protected MailerResult sendEmail(Invitation invitation, RepositoryEntry entry, BusinessGroup businessGroup) {
		ContactList contactList = new ContactList("Invitation");
		contactList.add(invitation.getMail());
		
		MailTemplate mailTemplate = null;
		OLATResourceable ores = null;
		if(entry != null) {
			ores = OresHelper.clone(entry);
			mailTemplate = RepositoryMailing.getInvitationTemplate(entry, getIdentity());
			if(mailTemplate instanceof RepositoryEntryMailTemplate) {
				String courseUrl = invitationService.toUrl(invitation, entry);
				((RepositoryEntryMailTemplate)mailTemplate).setCourseUrl(courseUrl);
			}
		} else if(businessGroup != null) {
			ores = OresHelper.clone(businessGroup);
			mailTemplate = BusinessGroupMailing.getDefaultTemplate(MailType.invitation, businessGroup, getIdentity());
			String businessGroupUrl = invitationService.toUrl(invitation, businessGroup);
			mailTemplate.addToContext("groupurl", businessGroupUrl);
		}
		
		MailerResult result = new MailerResult();
		MailContext ctxt = new MailContextImpl(ores, null, getWindowControl().getBusinessControl().getAsString());
		MailBundle bundle = mailManager.makeMailBundle(ctxt, mailTemplate, getIdentity(), null, result);
		bundle.setContactList(contactList);

		return mailManager.sendExternMessage(bundle, result, true);
	}
	
	private class ToolsController extends BasicController {

		private Link activateLink;
		private final VelocityContainer mainVC;
		
		private final InvitationRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, InvitationRow row) {
			super(ureq, wControl);
			this.row = row;
			mainVC = createVelocityContainer("tools");
			
			if(row.getInvitationStatus() == InvitationStatusEnum.inactive) {
				activateLink = LinkFactory.createLink("activate", "activate", getTranslator(), mainVC, this, Link.LINK);
				mainVC.put("activate", activateLink);
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(activateLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doInvitationStatus(row, InvitationStatusEnum.active);
			}
		}
	}
}
