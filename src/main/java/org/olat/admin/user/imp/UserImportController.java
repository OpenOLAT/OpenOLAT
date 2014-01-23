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

package org.olat.admin.user.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.login.auth.OLATAuthManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * Bulk import and update of users.
 * 
 * <P>
 * Initial Date: 17.08.2005 <br>
 * 
 * @author Felix, Roman Haag
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserImportController extends BasicController {

	private List<UserPropertyHandler> userPropertyHandlers;
	private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
	private boolean canCreateOLATPassword;
	private VelocityContainer mainVC;
	private Link startLink;
	
	private StepsMainRunController importStepsController;
	
	private final BaseSecurity securityManager;
	private final OLATAuthManager olatAuthManager;
	private final BusinessGroupService businessGroupService;
	private final UserManager um ;

	/**
	 * @param ureq
	 * @param wControl
	 * @param canCreateOLATPassword true: workflow offers column to create
	 *          passwords; false: workflow does not offer pwd column
	 */
	public UserImportController(UserRequest ureq, WindowControl wControl, boolean canCreateOLATPassword) {
		super(ureq, wControl);
		um = UserManager.getInstance();
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		olatAuthManager = CoreSpringFactory.getImpl(OLATAuthManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.canCreateOLATPassword = canCreateOLATPassword;
		mainVC = createVelocityContainer("importindex");
		startLink = LinkFactory.createButton("import.start", mainVC, this);
		putInitialPanel(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source==importStepsController){
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importStepsController);
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importStepsController);
				showInfo("import.success");
			}
		}
	}

	private Identity doCreateAndPersistIdentity(TransientIdentity singleUser) {
		// Create new user and identity and put user to users group
		String login = singleUser.getName(); //pos 0 is used for existing/non-existing user flag
		String pwd = singleUser.getPassword();
		String lang = singleUser.getLanguage();

		// use password only when configured to do so
		if (canCreateOLATPassword) {
			if (!StringHelper.containsNonWhitespace(pwd)) {
				// treat white-space passwords as no-password. This is fine, a password
				// can be set later on
				pwd = null;
			}
		}

		// Create transient user without firstName,lastName, email
		
		User newUser = um.createUser(null, null, null);

		List<UserPropertyHandler> userProperties = userPropertyHandlers;
		for (UserPropertyHandler userPropertyHandler : userProperties) {
			String thisValue = singleUser.getProperty(userPropertyHandler.getName(), null);
			String stringValue = userPropertyHandler.getStringValue(thisValue, getLocale());
			userPropertyHandler.setUserProperty(newUser, stringValue);
		}
		// Init preferences
		newUser.getPreferences().setLanguage(lang);
		newUser.getPreferences().setInformSessionTimeout(true);
		// Save everything in database
		Identity ident = AuthHelper.createAndPersistIdentityAndUserWithUserGroup(login, pwd, newUser);
		return ident;
	}
	
	private Identity doUpdateIdentity(UpdateIdentity userToUpdate, Boolean updateUsers, Boolean updatePassword) {
		Identity identity;
		if(updateUsers != null && updateUsers.booleanValue()) {
			identity = userToUpdate.getIdentity(true);
			um.updateUserFromIdentity(identity);
		} else {
			identity = userToUpdate.getIdentity();
		}
		
		String password = userToUpdate.getPassword();
		if(StringHelper.containsNonWhitespace(password) && updatePassword != null && updatePassword.booleanValue()) {
			Authentication auth = securityManager.findAuthentication(identity, "OLAT");
			if(auth != null) {
				olatAuthManager.changePassword(getIdentity(), identity, password);
			}
		}
		return userToUpdate.getIdentity();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	// child controllers disposed by basic controller
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startLink){
		// use fallback translator for user property translation
		setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
		userPropertyHandlers = um.getUserPropertyHandlersFor(usageIdentifyer, true);
		
		Step start = new ImportStep00(ureq, canCreateOLATPassword);
		// callback executed in case wizard is finished.
		StepRunnerCallback finish = new StepRunnerCallback() {
			public Step execute(UserRequest ureq1, WindowControl wControl1, StepsRunContext runContext) {
				// all information to do now is within the runContext saved
				boolean hasChanges = false;
				try {
					if (runContext.containsKey("validImport") && ((Boolean) runContext.get("validImport")).booleanValue()) {
						// create new users and persist
						@SuppressWarnings("unchecked")
						List<TransientIdentity> newIdents = (List<TransientIdentity>) runContext.get("newIdents");
						for (TransientIdentity newIdent:newIdents) {
							doCreateAndPersistIdentity(newIdent);
						}

						Boolean updateUsers = (Boolean)runContext.get("updateUsers");
						Boolean updatePasswords = (Boolean)runContext.get("updatePasswords");
						@SuppressWarnings("unchecked")
						List<UpdateIdentity> updateIdents = (List<UpdateIdentity>) runContext.get("updateIdents");
						for (UpdateIdentity updateIdent:updateIdents) {
							doUpdateIdentity(updateIdent, updateUsers, updatePasswords);
						}

						@SuppressWarnings("unchecked")
						List<Long> ownGroups = (List<Long>) runContext.get("ownerGroups");
						@SuppressWarnings("unchecked")
						List<Long> partGroups = (List<Long>) runContext.get("partGroups");

						if (ownGroups.size() > 0 || partGroups.size() > 0){
							@SuppressWarnings("unchecked")
							List<Identity> allIdents = (List<Identity>) runContext.get("idents");
							Boolean sendMailObj = (Boolean)runContext.get("sendMail");
							boolean sendmail = sendMailObj == null ? true : sendMailObj.booleanValue();
							processGroupAdditionForAllIdents(allIdents, ownGroups, partGroups, sendmail);
						}
						hasChanges = true;
					}
				} catch (Exception any) {
					// return new ErrorStep
				}
				// signal correct completion and tell if changes were made or not.
				return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
			}

		};

		importStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("title"), "o_sel_user_import_wizard");
		listenTo(importStepsController);
			getWindowControl().pushAsModalDialog(importStepsController.getInitialComponent());
		}
	}
	
	private Collection<Identity> getIdentities(List<Identity> allIdents) {
		Set<Identity> identities = new HashSet<Identity>(allIdents.size());
		List<String> usernames = new ArrayList<String>();
		for (Object o : allIdents) {
			if(o instanceof TransientIdentity) {
				TransientIdentity transIdent = (TransientIdentity)o;
				usernames.add(transIdent.getName());
			} else if (o instanceof Identity) {
				identities.add((Identity)o);	
			}
		}

		List<Identity> nextIds = securityManager.findIdentitiesByName(usernames);
		identities.addAll(nextIds);
		return identities;
	}

	private void processGroupAdditionForAllIdents(List<Identity> allIdents, List<Long> tutorGroups, List<Long> partGroups, boolean sendmail) {
		Collection<Identity> identities = getIdentities(allIdents);
		List<BusinessGroupMembershipChange> changes = new ArrayList<BusinessGroupMembershipChange>();
		for(Identity identity:identities) {
			if(tutorGroups != null && !tutorGroups.isEmpty()) {
				for(Long tutorGroupKey:tutorGroups) {
					BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(identity, tutorGroupKey);
					change.setTutor(Boolean.TRUE);
					changes.add(change);
				}
			}
			if(partGroups != null && !partGroups.isEmpty()) {
				for(Long partGroupKey:partGroups) {
					BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(identity, partGroupKey);
					change.setParticipant(Boolean.TRUE);
					changes.add(change);
				}
			}
		}
		
		MailPackage mailing = new MailPackage(sendmail);
		businessGroupService.updateMemberships(getIdentity(), changes, mailing);
		DBFactory.getInstance().commit();
	}
}