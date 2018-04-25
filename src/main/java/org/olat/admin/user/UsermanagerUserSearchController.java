/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.admin.user.bulkChange.UserBulkChangeStep00;
import org.olat.admin.user.bulkChange.UserBulkChanges;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.id.context.StateMapped;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.ui.admin.UserSearchTableController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Jan 31, 2006
 * 
 * @author gnaegi
 * 
 * Description: This workflow has two constructors. The first one provides the
 * user an advanced user search form with many search criterias that can be
 * defined. The second one has the criterias in the constructor as attributes,
 * so the search form won't appear. The following is a list with the search
 * results. Form the list an identity can be selected which results in a
 * SingleIdentityChosenEvent Alternatively a Canceled Event is fired.
 * 
 */
public class UsermanagerUserSearchController extends BasicController implements Activateable2 {

	private static final String CMD_MAIL = "exeMail";
	private static final String CMD_BULKEDIT = "bulkEditUsers";

	private VelocityContainer userListVC, userSearchVC, mailVC;

	private TooledStackedPanel stackedPanel;

	private UsermanagerUserSearchForm searchFormCtrl;
	private UserSearchTableController tableCtr;
	private List<Identity> identitiesList, selectedIdentities;
	private List<String> notUpdatedIdentities = new ArrayList<>();
	private ExtendedIdentitiesTableDataModel tdm;
	private ContactFormController contactCtr;
	private Link backFromMail;
	private Link backFromList;
	private boolean showEmailButton = true;
	private StepsMainRunController userBulkChangeStepsController;
	
	
	private final boolean isAdministrativeUser;
	private List<Organisation> parentOrganisations;
	private SearchIdentityParams identityQueryParams;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserBulkChangeManager ubcMan;

	/**
	 * Constructor to trigger the user search workflow using a generic search form
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public UsermanagerUserSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			List<Organisation> parentOrganisations) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.stackedPanel = stackedPanel;
		this.parentOrganisations = parentOrganisations;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		
		userSearchVC = createVelocityContainer("usermanagerUsersearch");

		mailVC = createVelocityContainer("usermanagerMail");
		backFromMail = LinkFactory.createLinkBack(mailVC, this);

		userListVC = createVelocityContainer("usermanagerUserlist");

		backFromList = LinkFactory.createLinkBack(userListVC, this);

		userListVC.contextPut("showBackButton", Boolean.TRUE);
		userListVC.contextPut("emptyList", Boolean.FALSE);
		userListVC.contextPut("showTitle", Boolean.TRUE);

		searchFormCtrl = new UsermanagerUserSearchForm(ureq, wControl, isAdministrativeUser);
		listenTo(searchFormCtrl);
		
		userSearchVC.put("usersearch", searchFormCtrl.getInitialComponent());

		putInitialPanel(userSearchVC);
	}
	
	/**
	 * Constructor to trigger the user search workflow using the given attributes.
	 * The user has no possibility to manually search, the search will be
	 * performed using the constructor attributes.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param searchGroups
	 * @param searchPermissionOnResources
	 * @param searchAuthProviders
	 * @param searchCreatedAfter
	 * @param searchCreatedBefore
	 */
	public UsermanagerUserSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			SearchIdentityParams predefinedQuery, boolean showEmailButton) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackedPanel = stackedPanel;

		identityQueryParams = predefinedQuery;
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		mailVC = createVelocityContainer("usermanagerMail");
		backFromMail = LinkFactory.createLinkBack(mailVC, this);

		userListVC = createVelocityContainer("usermanagerUserlist");
		this.showEmailButton = showEmailButton;
		
		tableCtr = new UserSearchTableController(ureq, getWindowControl(), stackedPanel, true);
		listenTo(tableCtr);
		tableCtr.loadModel(identityQueryParams);
		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	/**
	 * Constructor to trigger the user search workflow using the predefined list of
	 * identities. The user has no possibility to manually search.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param identitiesList
	 * @param status
	 * @param showEmailButton
	 */
	public UsermanagerUserSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			List<Identity> identitiesList, Integer status, boolean showEmailButton, boolean showTitle) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackedPanel = stackedPanel;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		mailVC = createVelocityContainer("usermanagerMail");

		backFromMail = LinkFactory.createLinkBack(mailVC, this);

		userListVC = createVelocityContainer("usermanagerUserlist");
		this.showEmailButton = showEmailButton;

		userListVC.contextPut("showBackButton", Boolean.FALSE);
		userListVC.contextPut("showTitle", Boolean.valueOf(showTitle));

		tableCtr = new UserSearchTableController(ureq, getWindowControl(), stackedPanel, true);
		listenTo(tableCtr);
		tableCtr.loadModel(identitiesList);
		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	public WindowControl getTableControl() {
		return tableCtr == null ? null : tableCtr.getWindowControlForDebug();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof StateMapped) {
			StateMapped searchState = (StateMapped)state;
			searchFormCtrl.setStateEntry(searchState);
			
			if(entries != null && entries.size() > 0) {
				String table = entries.get(0).getOLATResourceable().getResourceableTypeName();
				if("table".equals(table)) {
					entries.remove(0);
					event(ureq, searchFormCtrl, Event.DONE_EVENT);
				}
			}
		}
		
		if(entries == null || entries.isEmpty()) return;
		
		for(int i=0; i<entries.size(); i++) {
			String resourceType = entries.get(i).getOLATResourceable().getResourceableTypeName();
			if("Identity".equalsIgnoreCase(resourceType)) {
				Long identityKey = entries.get(i).getOLATResourceable().getResourceableId();
				Identity found = null;
				if(tdm != null) {
					for(Identity identity:tdm.getObjects()) {
						if(identityKey.equals(identity.getKey())) {
							found = identity;
						}
					}
				}
				
				if(found == null) {
					found = securityManager.loadIdentityByKey(identityKey);
					if(found == null) return;
					
					List<Identity> foundIdentites = new ArrayList<>();
					foundIdentites.add(found);
					initUserListCtr(ureq, foundIdentites, 0);
				}
				fireEvent(ureq, new SingleIdentityChosenEvent(found));
			}
		}
	}

	/**
	 * Remove the given identites from the list of identites in the table model
	 * and reinitialize the table controller
	 * 
	 * @param ureq
	 * @param tobeRemovedIdentities
	 */
	public void removeIdentitiesFromSearchResult(UserRequest ureq, List<Identity> tobeRemovedIdentities) {
		PersistenceHelper.removeObjectsFromList(identitiesList, tobeRemovedIdentities);
		initUserListCtr(ureq, identitiesList, null);
		userListVC.put("userlist", tableCtr.getInitialComponent());
	}

	/**
	 * Add the given identities to the list of identities in the table model 
	 * and reinitialize the table controller
	 * 
	 * @param ureq
	 * @param tobeAddedIdentities
	 */
	public void addIdentitiesToSearchResult(UserRequest ureq, List<Identity> tobeAddedIdentities) {
		Set<Identity> identitiesSet = new HashSet<>();
		if(identitiesList != null) {
			identitiesSet.addAll(identitiesList);
		}
		for (Identity toBeAdded : tobeAddedIdentities) {
			if (!identitiesSet.contains(toBeAdded)) {
				identitiesList.add(toBeAdded);
			}
		}
		initUserListCtr(ureq, identitiesList, null);
		userListVC.put("userlist", tableCtr.getInitialComponent());		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * Initialize the table controller using the list of identities
	 * 
	 * @param ureq
	 * @param identitiesList
	 */
	private void initUserListCtr(UserRequest ureq, List<Identity> myIdentities, Integer searchStatusField) {
		removeAsListenerAndDispose(tableCtr);
		
		boolean actionEnabled = true;
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
		if ((searchStatusField != null) && (searchStatusField.equals(Identity.STATUS_DELETED))) {
			actionEnabled = false;
		}
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "ExtendedIdentitiesTable");		
		tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
		
		tdm = new ExtendedIdentitiesTableDataModel(ureq, myIdentities, actionEnabled);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("table", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		tableCtr = new UserSearchTableController(ureq, bwControl, stackedPanel, true);
		listenTo(tableCtr);
		
		/*
		
		if (showEmailButton) {
			tableCtr.addMultiSelectAction("command.mail", CMD_MAIL);
		}
		if (actionEnabled){
			tableCtr.addMultiSelectAction("action.bulkedit", CMD_BULKEDIT);
		}
		if (showEmailButton || actionEnabled){
			tableCtr.setMultiSelect(true);
		}
		*/
	}
	
	private void doPushSearch(UserRequest ureq) {
		identityQueryParams = searchFormCtrl.getSearchIdentityParams();
		identityQueryParams.setOrganisationParents(parentOrganisations);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("table", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		tableCtr = new UserSearchTableController(ureq, bwControl, stackedPanel, true);
		listenTo(tableCtr);
		tableCtr.loadModel(identityQueryParams);
		stackedPanel.pushController("Results", tableCtr);

		if(searchFormCtrl != null) {
			ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
			if(currentEntry != null) {
				currentEntry.setTransientState(searchFormCtrl.getStateEntry());
			}
		}
		addToHistory(ureq, tableCtr);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchFormCtrl) {
			if (event == Event.DONE_EVENT) {
				doPushSearch(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(ExtendedIdentitiesTableDataModel.COMMAND_SELECTUSER)) {
					int rowid = te.getRowId();
					Identity foundIdentity = tdm.getObject(rowid);
					// Tell parentController that a subject has been found
					fireEvent(ureq, new SingleIdentityChosenEvent(foundIdentity));
				} else if (actionid.equals(ExtendedIdentitiesTableDataModel.COMMAND_VCARD)) {
					// get identity and open new visiting card controller in new window
					int rowid = te.getRowId();
					final Identity identity = tdm.getObject(rowid);
					ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new UserInfoMainController(lureq, lwControl, identity, true, false);
						}
					};
					// wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
					// open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager()
							.createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
				}
			}
			if (event instanceof TableMultiSelectEvent) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(CMD_BULKEDIT)) {
					if (tmse.getSelection().isEmpty()) {
						// empty selection
						showWarning("msg.selectionempty");
						return;
					}
					selectedIdentities = tdm.getIdentities(tmse.getSelection());
					// valid selection: load in wizard
					final UserBulkChanges userBulkChanges = new UserBulkChanges();
					Step start = new UserBulkChangeStep00(ureq, selectedIdentities, userBulkChanges);
					// callback executed in case wizard is finished.
					StepRunnerCallback finish = (ureq1, wControl1, runContext) -> {
						// all information to do now is within the runContext saved
						boolean hasChanges = false;
						try {
							if (userBulkChanges.isValidChange()){
								Map<String, String> attributeChangeMap = userBulkChanges.getAttributeChangeMap();
								Map<OrganisationRoles, String> roleChangeMap = userBulkChanges.getRoleChangeMap();
								List<Long> ownGroups = userBulkChanges.getOwnerGroups();
								List<Long> partGroups = userBulkChanges.getParticipantGroups();
								if (!attributeChangeMap.isEmpty() || !roleChangeMap.isEmpty() || !ownGroups.isEmpty() || !partGroups.isEmpty()){
									Identity addingIdentity = ureq1.getIdentity();
									ubcMan.changeSelectedIdentities(selectedIdentities, userBulkChanges, notUpdatedIdentities,
										isAdministrativeUser, getTranslator(), addingIdentity);
									hasChanges = true;
								}
							}
						} catch (Exception e) {
							logError("", e);
						}
						// signal correct completion and tell if changes were made or not.
						return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
					};

					removeAsListenerAndDispose(userBulkChangeStepsController);
					userBulkChangeStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
							translate("bulkChange.title"), "o_sel_user_bulk_change_wizard");
					listenTo(userBulkChangeStepsController);
					
					getWindowControl().pushAsModalDialog(userBulkChangeStepsController.getInitialComponent());

				} else if (tmse.getAction().equals(CMD_MAIL)) {
					if (tmse.getSelection().isEmpty()) {
						// empty selection
						showWarning("msg.selectionempty");
						return;
					}
					// create e-mail message
					ContactMessage cmsg = new ContactMessage(ureq.getIdentity());

					selectedIdentities = tdm.getIdentities(tmse.getSelection());
					ContactList contacts = new ContactList(translate("mailto.userlist"));
					contacts.addAllIdentites(selectedIdentities);
					cmsg.addEmailTo(contacts);

					// create contact form controller with ContactMessage
					removeAsListenerAndDispose(contactCtr);
					contactCtr = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
					listenTo(contactCtr);

					mailVC.put("mailform", contactCtr.getInitialComponent());
					//panel.setContent(mailVC);
				}
			}
		} else if (source == contactCtr) {

		} else if (source == userBulkChangeStepsController) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				Integer selIdentCount = selectedIdentities.size();
				if (notUpdatedIdentities.size() > 0) {
					Integer notUpdatedIdentCount = notUpdatedIdentities.size();
					Integer sucChanges = selIdentCount - notUpdatedIdentCount;
					String changeErrors = "";
					for (String err : notUpdatedIdentities) {
						changeErrors += err + "<br />";
					}
					getWindowControl().setError(translate("bulkChange.partialsuccess",
							new String[] { sucChanges.toString(), selIdentCount.toString(), changeErrors }));
				} else {
					showInfo("bulkChange.success");
				}
				// update table model - has changed
				reloadDataModel(ureq);

			} else if (event == Event.DONE_EVENT){
				showError("bulkChange.failed");
			}
			
		}
	}

	/**
	 * Reload the currently used identitiesList and rebuild the table controller
	 * 
	 * @param ureq
	 */
	private void reloadDataModel(UserRequest ureq) {
		if (identitiesList == null) return;
		for (int i = 0; i < identitiesList.size(); i++) {
			Identity ident = identitiesList.get(i);
			Identity refrshed = securityManager.loadIdentityByKey(ident.getKey());
			identitiesList.set(i, refrshed);
		}
		initUserListCtr(ureq, identitiesList, null);
		userListVC.put("userlist", tableCtr.getInitialComponent());
	}

	/**
	 * Reload the identity used currently in the workflow and in the currently
	 * activated user table list model. The identity will be reloaded from the
	 * database to have accurate values.
	 */
	public void reloadFoundIdentity(Identity editedIdentity) {
		if(editedIdentity == null) return;//nothing to replace
		
		List<Identity> identities = tdm.getObjects();
		int index = identities.indexOf(editedIdentity);
		if(index >= 0) {
			// reload the found identity
			Identity reloadedIdentity = securityManager.loadIdentityByKey(editedIdentity.getKey());
			// replace the found identity in the table list model to display changed
			identities.set(index, reloadedIdentity);
		}
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}

}