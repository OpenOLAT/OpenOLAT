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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.ExtendedIdentitiesTableDataModel;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.admin.user.bulkChange.UserBulkChangeStep00;
import org.olat.admin.user.bulkChange.UserBulkChanges;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.UserSearchTableModel.UserCols;
import org.olat.user.ui.identity.UserInfoSegmentedController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableController extends FormBasicController {
	
	private static final String USER_PROPS_ID = ExtendedIdentitiesTableDataModel.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	
	private Link nextLink;
	private Link previousLink;
	private FormLink mailButton;
	private FormLink bulkChangesButton;
	
	private FlexiTableElement tableEl;
	private UserSearchTableModel tableModel;
	private TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private UserAdminController userAdminCtr;
	private ContactFormController contactCtr;
	private UserInfoSegmentedController userInfoCtr;
	private StepsMainRunController userBulkChangesController; 
	
	private final Roles roles;
	private final boolean vCard;
	private final boolean bulkMail;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserBulkChangeManager userBulkChangesManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public UserSearchTableController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			boolean bulkMail, boolean vCard) {
		super(ureq, wControl, "search_table");
		this.vCard = vCard;
		this.bulkMail = bulkMail;
		this.stackPanel = stackPanel;
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		
		if(bulkMail) {
			mailButton = uifactory.addFormLink("command.mail", formLayout, Link.BUTTON);
		}
		if(roles.isAdministrator() || roles.isUserManager() || roles.isRolesManager()) {
			bulkChangesButton = uifactory.addFormLink("bulkChange.title", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UserCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.status, new IdentityStatusCellRenderer(getTranslator())));
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.username, "select"));
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UserCols.lastLogin));
		
		if(vCard) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.vcard", translate("table.identity.vcard"), "vcard"));
		}
		
		tableModel = new UserSearchTableModel(new EmptyDataSource(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("error.no.user.found");
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "user_search_table");
	}
	
	public void loadModel(SearchIdentityParams params) {
		UserSearchDataSource dataSource = new UserSearchDataSource(params, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
	}
	
	public void loadModel(List<Identity> identityList) {
		IdentityListDataSource dataSource = new IdentityListDataSource(identityList, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
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
		removeAsListenerAndDispose(userBulkChangesController);
		removeAsListenerAndDispose(userInfoCtr);
		removeAsListenerAndDispose(contactCtr);
		removeAsListenerAndDispose(cmc);
		userBulkChangesController = null;
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

	private void doSelectIdentity(UserRequest ureq, IdentityPropertiesRow userRow) {
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
		if(contactCtr != null) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning();
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
				true, translate("command.mail"), false);
		listenTo(cmc);
		cmc.activate(); 
	}

	private void doBulkEdit(UserRequest ureq) {
		if(userBulkChangesController != null) return;
		
		List<Identity> identities = getSelectedIdentitiesWithWarning();
		if(identities.isEmpty()) {
			return;
		}

		// valid selection: load in wizard
		final UserBulkChanges userBulkChanges = new UserBulkChanges();
		Step start = new UserBulkChangeStep00(ureq, identities, userBulkChanges);
		// callback executed in case wizard is finished.
		StepRunnerCallback finish = (ureq1, wControl1, runContext) -> {
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
						Identity addingIdentity = ureq1.getIdentity();
						userBulkChangesManager.changeSelectedIdentities(identities, userBulkChanges, notUpdatedIdentities,
							isAdministrativeUser, getTranslator(), addingIdentity);
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
					new String[] { sucChanges.toString(), selIdentCount.toString(), changeErrors.toString() }));
		} else {
			showInfo("bulkChange.success");
		}
		// reload the data
		tableEl.reset(true, true, true);
	}
	
	private List<Identity> getSelectedIdentitiesWithWarning() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		if(selections.isEmpty()) {
			showWarning("msg.selectionempty");
			return Collections.emptyList();
		}
		
		List<Long> identityKeys = new ArrayList<>(selections.size());
		for(Integer selection:selections) {
			IdentityPropertiesRow row = tableModel.getObject(selection.intValue());
			identityKeys.add(row.getIdentityKey());
		}
		return securityManager.loadIdentityByKeys(identityKeys);
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
				List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
			return new DefaultResultInfos<>();
		}
	}
}
