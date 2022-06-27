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

package org.olat.admin.securitygroup.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.olat.admin.securitygroup.gui.multi.UsersToGroupWizardStep00;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.group.ui.main.OnlineIconRenderer;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Generic group management controller. Displays the list of users that are in
 * the given security group and features an add button to add users to the
 * group.
 * <p>
 * Fired events:
 * <ul>
 * <li>IdentityAddedEvent</li>
 * <li>IdentityRemovedEvent</li>
 * <li>SingleIdentityChosenEvent</li>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: Jan 25, 2005
 * 
 * @author Felix Jost, Florian Gnägi
 */

public class GroupController extends BasicController {

	protected boolean keepAtLeastOne;
	protected boolean mayModifyMembers;

	protected static final String COMMAND_REMOVEUSER = "removesubjectofgroup";
	protected static final String COMMAND_IM = "im";
	protected static final String COMMAND_VCARD = "show.vcard";
	protected static final String COMMAND_SELECTUSER = "select.user";

	protected SecurityGroup securityGroup;
	protected VelocityContainer groupmemberview;

	protected IdentitiesOfGroupTableDataModel identitiesTableModel;

	private List<Identity> toAdd, toRemove;

	private UserSearchController usc;
	private MailNotificationEditController addUserMailCtr, removeUserMailCtr;
	private StepsMainRunController userToGroupWizard;
	private DialogBoxController confirmDelete;

	protected TableController tableCtr;
	private Link addUsersButton;
	private Link addUserButton;
	private Translator myTrans;

	private MailTemplate addUserMailDefaultTempl, removeUserMailDefaultTempl, removeUserMailCustomTempl;
	private boolean showSenderInAddMailFooter;
	private boolean showSenderInRemovMailFooter;
	private CloseableModalController cmc;
	protected static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
	protected boolean isAdministrativeUser;
	protected boolean mandatoryEmail;
	protected boolean chatEnabled;

	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	protected UserManager userManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private MailManager mailManager;
	
	private Object userObject;

	/**
	 * @param ureq
	 * @param wControl
	 * @param mayModifyMembers
	 * @param keepAtLeastOne
	 * @param enableTablePreferences
	 * @param aSecurityGroup
	 * @param enableUserSelection
	 */	 
	public GroupController(UserRequest ureq, WindowControl wControl, 
			boolean mayModifyMembers, boolean keepAtLeastOne, boolean enableTablePreferences, boolean enableUserSelection,
			boolean allowDownload, boolean mandatoryEmail, SecurityGroup aSecurityGroup) {
		super(ureq, wControl);
		init(ureq, mayModifyMembers, keepAtLeastOne, enableTablePreferences, enableUserSelection, allowDownload, mandatoryEmail, aSecurityGroup);
	}

	protected void init(UserRequest ureq,
			boolean mayModifyMembers, boolean keepAtLeastOne, boolean enableTablePreferences, boolean enableUserSelection,
			boolean allowDownload, boolean mandatoryEmail, SecurityGroup aSecurityGroup) {
		this.securityGroup = aSecurityGroup;
		this.mayModifyMembers = mayModifyMembers;
		this.keepAtLeastOne = keepAtLeastOne;
		this.mandatoryEmail = mandatoryEmail;
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();

		// default group controller has no mail functionality
		this.addUserMailDefaultTempl = null;
		this.removeUserMailDefaultTempl = null;

		groupmemberview = createVelocityContainer("index");

		addUsersButton = LinkFactory.createButtonSmall("overview.addusers", groupmemberview, this);
		addUsersButton.setElementCssClass("o_sel_group_import_users");
		addUserButton = LinkFactory.createButtonSmall("overview.adduser", groupmemberview, this);
		addUserButton.setElementCssClass("o_sel_group_add_user");

		if (mayModifyMembers) {
			groupmemberview.contextPut("mayadduser", Boolean.TRUE);
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(allowDownload);
		if (enableTablePreferences) {
			// save table preferences for each group seperatly
			if (mayModifyMembers) {
				tableConfig.setPreferencesOffered(true, "groupcontroller" + securityGroup.getKey());
			} else {
				// different rowcount...
				tableConfig.setPreferencesOffered(true, "groupcontrollerreadonly" + securityGroup.getKey());
			}
		}
		
		myTrans = userManager.getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myTrans);	
		listenTo(tableCtr);
		
		initGroupTable(tableCtr, ureq, enableTablePreferences, enableUserSelection);

		// set data model
		reloadData();
		groupmemberview.put("subjecttable", tableCtr.getInitialComponent());
		putInitialPanel(groupmemberview);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * @param addUserMailDefaultTempl Set a template to send mail when adding
	 *          users to group
	 */
	public void setAddUserMailTempl(MailTemplate addUserMailTempl, boolean showSenderInAddFooter) {
		this.addUserMailDefaultTempl = addUserMailTempl;
		this.showSenderInAddMailFooter = showSenderInAddFooter;
	}

	/**
	 * @param removeUserMailDefaultTempl Set a template to send mail when removing
	 *          a user from the group
	 */
	public void setRemoveUserMailTempl(MailTemplate removeUserMailTempl, boolean showSenderInRemoveFooter) {
		this.removeUserMailDefaultTempl = removeUserMailTempl;
		this.showSenderInRemovMailFooter = showSenderInRemoveFooter;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addUserButton) {
			if (!mayModifyMembers) throw new AssertException("not allowed to add a member!");
			doAddUsers(ureq);
		} else if (source == addUsersButton) {
			if (!mayModifyMembers) throw new AssertException("not allowed to add members!");
			doImportUsers(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller sourceController, Event event) {
		if (sourceController == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				// Single row selects
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				final Identity identity = identitiesTableModel.getObject(te.getRowId()).getIdentity();
				if (actionid.equals(COMMAND_VCARD)) {
					//get identity and open new visiting card controller in new window
					ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new UserInfoMainController(lureq, lwControl, identity, true, false);
						}					
					};
					//wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
					//open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
					//
				} else if (actionid.equals(COMMAND_SELECTUSER)) {
					fireEvent(ureq, new SingleIdentityChosenEvent(identity));
				} else if (COMMAND_IM.equals(actionid)) {
					doIm(ureq, identity);
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(COMMAND_REMOVEUSER)) {
					if(tmse.getSelection().isEmpty()){
						//empty selection 
						showWarning("msg.selectionempty");
						return;
					}
					int size = identitiesTableModel.getObjects().size(); 
					toRemove = identitiesTableModel.getIdentities(tmse.getSelection());
					// list is never null, but can be empty
					if (keepAtLeastOne && (size == 1 || size - toRemove.size() == 0)) {
						//at least one must be kept
						//do not delete the last one => ==1
						//do not allow to delete all => size - selectedCnt == 0
						showError("msg.atleastone");
					} else {
						//valid selection to be deleted.
						if (removeUserMailDefaultTempl == null) {
							doBuildConfirmDeleteDialog(ureq);
						} else {
							removeAsListenerAndDispose(removeUserMailCtr);
							removeUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, removeUserMailDefaultTempl, true, false, true);							
							listenTo(removeUserMailCtr);
							
							removeAsListenerAndDispose(cmc);
							cmc = new CloseableModalController(getWindowControl(), translate("close"), removeUserMailCtr.getInitialComponent());
							listenTo(cmc);
							
							cmc.activate();
						}
					}
				}
			}
		} else if (sourceController == removeUserMailCtr) {
			if (event == Event.DONE_EVENT) {
				removeUserMailCustomTempl = removeUserMailCtr.getMailTemplate();
				cmc.deactivate();
				doBuildConfirmDeleteDialog(ureq);
			} else {
				cmc.deactivate();
			}
		} else if (sourceController == usc) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else {
				if (event instanceof SingleIdentityChosenEvent) {
					SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
					Identity choosenIdentity = singleEvent.getChosenIdentity();
					if (choosenIdentity == null) {
						showError("msg.selectionempty");
						return;
					}
					toAdd = new ArrayList<>();
					toAdd.add(choosenIdentity);
				} else if (event instanceof MultiIdentityChosenEvent) {
					MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
					toAdd = multiEvent.getChosenIdentities();
					if (toAdd.size() == 0) {
						showError("msg.selectionempty");
						return;
					}
				} else {
					throw new RuntimeException("unknown event ::" + event.getCommand());
				}
				
				if (toAdd.size() == 1) {
					//check if already in group [makes only sense for a single choosen identity]
					if (securityGroupDao.isIdentityInSecurityGroup(toAdd.get(0), securityGroup)) {
						String fullName = userManager.getUserDisplayName(toAdd.get(0));
						getWindowControl().setInfo(translate("msg.subjectalreadyingroup", new String[]{ fullName }));
						return;
					}
				} else if (toAdd.size() > 1) {
					//check if already in group
					List<Identity> alreadyInGroup = new ArrayList<>();
					for (int i = 0; i < toAdd.size(); i++) {
						if (securityGroupDao.isIdentityInSecurityGroup(toAdd.get(i), securityGroup)) {
							tableCtr.setMultiSelectSelectedAt(i, false);
							alreadyInGroup.add(toAdd.get(i));
						}
					}
					if (!alreadyInGroup.isEmpty()) {
						StringBuilder names = new StringBuilder();
						for(Identity ident: alreadyInGroup) {
							if(names.length() > 0) names.append(", ");
							names.append(userManager.getUserDisplayName(ident));
							toAdd.remove(ident);
						}
						getWindowControl().setInfo(translate("msg.subjectsalreadyingroup", names.toString()));
					}
					if (toAdd.isEmpty()) {
						return;
					}
				}
				
				// in both cases continue adding the users or asking for the mail
				// template if available (=not null)
				cmc.deactivate();
				if (addUserMailDefaultTempl == null) {
					doAddIdentitiesToGroup(ureq, toAdd, null);
				} else {
					removeAsListenerAndDispose(addUserMailCtr);
					addUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, addUserMailDefaultTempl, true, mandatoryEmail, true);					
					listenTo(addUserMailCtr);
					
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), translate("close"), addUserMailCtr.getInitialComponent());
					listenTo(cmc);
					
					cmc.activate();
				}
			}
			// in any case cleanup this controller, not used anymore
			usc.dispose();
			usc = null;

		} else if (sourceController == addUserMailCtr) {
			if (event == Event.DONE_EVENT) {
				MailTemplate customTemplate = addUserMailCtr.getMailTemplate();
				doAddIdentitiesToGroup(ureq, toAdd, customTemplate);
				cmc.deactivate();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else {
				throw new RuntimeException("unknown event ::" + event.getCommand());
			}

		} else if (sourceController == confirmDelete) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// before deleting, assure it is allowed
				if (!mayModifyMembers) throw new AssertException("not allowed to remove member!");
				// list is never null, but can be empty
				int size = identitiesTableModel.getObjects().size(); 
				if (keepAtLeastOne && (size - toRemove.size() == 0)) {
					showError("msg.atleastone");
				} else {
					doRemoveIdentitiesFromGroup(ureq, toRemove, removeUserMailCustomTempl);
				}
			}

		} else if (sourceController == userToGroupWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(userToGroupWizard);
				userToGroupWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadData();
				}
			}
		} 
	}
	
	private void doAddUsers(UserRequest ureq) {
		removeAsListenerAndDispose(usc);
		usc = new UserSearchController(ureq, getWindowControl(), true, true, false);			
		listenTo(usc);
		
		Component usersearchview = usc.getInitialComponent();
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), usersearchview, true, translate("add.searchuser"));
		listenTo(cmc);
		
		cmc.activate();
	}
	
	private void doImportUsers(UserRequest ureq) {
		removeAsListenerAndDispose(userToGroupWizard);

		Step start = new UsersToGroupWizardStep00(ureq, addUserMailDefaultTempl, mandatoryEmail);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				Set<Identity> choosenIdentities = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
				MailTemplate customTemplate = (MailTemplate)runContext.get("mailTemplate");
				if (choosenIdentities == null || choosenIdentities.size() == 0) {
					showError("msg.selectionempty");
				} else {
					doAddIdentitiesToGroup(uureq, new ArrayList<>(choosenIdentities), customTemplate);
				}
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		userToGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("overview.addusers"), "o_sel_secgroup_import_logins_wizard");
		listenTo(userToGroupWizard);
		getWindowControl().pushAsModalDialog(userToGroupWizard.getInitialComponent());
	}
	
	private void doIm(UserRequest ureq, Identity identity) {
		Buddy buddy = imService.getBuddyById(identity.getKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}

	private void doBuildConfirmDeleteDialog(UserRequest ureq) {
		if (confirmDelete != null) confirmDelete.dispose();
		StringBuilder names = new StringBuilder();
		for (Identity identity : toRemove) {
			if(names.length() > 0) names.append(", ");
			names.append(userManager.getUserDisplayName(identity));
		}
		confirmDelete = activateYesNoDialog(ureq, null, translate("remove.text", names.toString()), confirmDelete);
	}

	private void doRemoveIdentitiesFromGroup(UserRequest ureq, List<Identity> toBeRemoved, MailTemplate mailTemplate) {
		fireEvent(ureq, new IdentitiesRemoveEvent(toBeRemoved));
		identitiesTableModel.remove(toBeRemoved);
		if(tableCtr != null){
			// can be null in the follwoing case.
			// the user which does the removal is also in the list of toBeRemoved
			// hence the fireEvent does trigger a disposal of a GroupController, which
			// in turn nullifies the tableCtr... see also OLAT-3331
			tableCtr.modelChanged();
		}

		// send the notification mail
		if (mailTemplate != null) {
			Identity sender = null; // means no sender in footer
			if (showSenderInRemovMailFooter) {
				sender = ureq.getIdentity();
			}

			String metaId = UUID.randomUUID().toString();
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, toBeRemoved, mailTemplate, sender, metaId, result);
			result.append(mailManager.sendMessage(bundles));
			if(mailTemplate.getCpfrom()) {
				MailBundle ccBundle = mailManager.makeMailBundle(context, ureq.getIdentity(), mailTemplate, sender, metaId, result);
				result.append(mailManager.sendMessage(ccBundle));
			}
			Roles roles = ureq.getUserSession().getRoles();
			boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
			MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, ureq.getLocale());
		}
	}

	/**
	 * Add users from the identites array to the group if they are not guest users
	 * and not already in the group
	 * 
	 * @param ureq
	 * @param choosenIdentities
	 */
	private void doAddIdentitiesToGroup(UserRequest ureq, List<Identity> choosenIdentities, MailTemplate mailTemplate) {
		// additional security check
		if (!mayModifyMembers) throw new AssertException("not allowed to add member!");

		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(choosenIdentities);
		// process workflow to BusinessGroupManager via BusinessGroupEditController
		fireEvent(ureq, identitiesAddedEvent); 
		if (!identitiesAddedEvent.getAddedIdentities().isEmpty()) {
  		// update table model
			reloadData();			
		}
		// build info message for identities which could be added.
		StringBuilder infoMessage = new StringBuilder();
		for (Identity identity : identitiesAddedEvent.getIdentitiesWithoutPermission()) {
	    infoMessage.append(translate("msg.isingroupanonymous", userManager.getUserDisplayName(identity))).append("<br />");
		}
		for (Identity identity : identitiesAddedEvent.getIdentitiesAlreadyInGroup()) {
			infoMessage.append(translate("msg.subjectalreadyingroup", userManager.getUserDisplayName(identity))).append("<br />");
		}
		// send the notification mail fro added users
		StringBuilder errorMessage = new StringBuilder();
		if (mailTemplate != null) {
			Identity sender = null; // means no sender in footer
			if (showSenderInAddMailFooter) {
				sender = ureq.getIdentity();
			}
			
			String metaId = UUID.randomUUID().toString();
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, identitiesAddedEvent.getAddedIdentities(), mailTemplate, sender, metaId, result);
			result.append(mailManager.sendMessage(bundles));
			if(mailTemplate.getCpfrom()) {
				MailBundle ccBundle = mailManager.makeMailBundle(context, ureq.getIdentity(), mailTemplate, sender, metaId, result);
				result.append(mailManager.sendMessage(ccBundle));
			}
			Roles roles = ureq.getUserSession().getRoles();
			boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
			MailHelper.appendErrorsAndWarnings(result, errorMessage, infoMessage, detailedErrorOutput, ureq.getLocale());
		}
		// report any errors on screen
		if (infoMessage.length() > 0) getWindowControl().setWarning(infoMessage.toString());
		if (errorMessage.length() > 0) getWindowControl().setError(errorMessage.toString());
	}

	@Override
	protected void doDispose() {
    // DialogBoxController and TableController get disposed by BasicController
		// usc, userToGroupWizardCtr, addUserMailCtr, and removeUserMailCtr are registerd with listenTo and get disposed in BasicController
		super.doPreDispose();		
	}

	/**
	 * Init GroupList-table-controller for non-waitinglist (participant-list,
	 * owner-list).
	 */
	protected void initGroupTable(TableController tableController, UserRequest ureq, boolean enableTablePreferences, boolean enableUserSelection) {			
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		if(chatEnabled) {
			tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.online", 1, COMMAND_IM, getLocale(),
					ColumnDescriptor.ALIGNMENT_LEFT, new OnlineIconRenderer()));
		}
		
		int visibleColId = 0;
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			ColumnDescriptor cd = userPropertyHandler.getColumnDescriptor(i + 3, COMMAND_VCARD, ureq.getLocale());
			// make all user attributes clickable to open visiting card
			if (cd instanceof DefaultColumnDescriptor) {
				DefaultColumnDescriptor dcd = (DefaultColumnDescriptor) cd;
				dcd.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
				
			}
			tableController.addColumnDescriptor(visible, cd);
			if (visible) {
				visibleColId++;
			}
		}
		
		// in the end
		if (enableTablePreferences) {
			DefaultColumnDescriptor dcd =  new DefaultColumnDescriptor("table.subject.addeddate", 2, COMMAND_VCARD, ureq.getLocale());
			dcd.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
			tableController.addColumnDescriptor(true, dcd);
			tableController.setSortColumn(++visibleColId,true);	
		}
		if (enableUserSelection) {
			tableController.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_SELECTUSER, "table.subject.action", myTrans.translate("action.general")));
		}
		if (mayModifyMembers) {
			tableController.addMultiSelectAction("action.remove", COMMAND_REMOVEUSER);
			tableController.setMultiSelect(true);
		}
	}

	public void reloadData() {
		// refresh view		
		List<Object[]> combo = securityGroupDao.getIdentitiesAndDateOfSecurityGroup(securityGroup); 
		List<GroupMemberView> views = new ArrayList<>(combo.size());
		Map<Long,GroupMemberView> idToViews = new HashMap<>();

		Set<Long> loadStatus = new HashSet<>();
		for(Object[] co:combo) {
			Identity identity = (Identity)co[0];
			Date addedAt = (Date)co[1];
			String onlineStatus = null;
			if(chatEnabled) {
				if(getIdentity().equals(identity)) {
					onlineStatus = "me";
				} else if(sessionManager.isOnline(identity.getKey())) {
					loadStatus.add(identity.getKey());
				} else {
					onlineStatus = Presence.unavailable.name();
				}
			}
			GroupMemberView member = new GroupMemberView(identity, addedAt, onlineStatus);
			views.add(member);
			idToViews.put(identity.getKey(), member);
		}
		
		if(loadStatus.size() > 0) {
			List<Long> statusToLoadList = new ArrayList<>(loadStatus);
			Map<Long,String> statusMap = imService.getBuddyStatus(statusToLoadList);
			for(Long toLoad:statusToLoadList) {
				String status = statusMap.get(toLoad);
				GroupMemberView member = idToViews.get(toLoad);
				if(status == null) {
					member.setOnlineStatus(Presence.available.name());	
				} else {
					member.setOnlineStatus(status);	
				}
			}
		}
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		identitiesTableModel = new IdentitiesOfGroupTableDataModel(views, getLocale(), userPropertyHandlers);
		tableCtr.setTableDataModel(identitiesTableModel);
	}
}
