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
package org.olat.ldap.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Level;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.logging.LogRealTimeViewerController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPEvent;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The LDAPAdminController offers an administrative panel to tweak some
 * parameters and manually run an LDAP sync job
 * 
 * <P>
 * Initial Date: 21.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LDAPAdminController extends BasicController implements GenericEventListener {
	private VelocityContainer ldapAdminVC;
	private DateFormat dateFormatter;
	private Link syncStartLink;
	private Link deleteStartLink;
	private StepsMainRunController deleteStepController;
	private boolean hasIdentitiesToDeleteAfterRun;
	private Integer amountUsersToDelete;
	private LDAPLoginManager ldapLoginManager;

	private UserSearchController userSearchCtrl;
	private CloseableCalloutWindowController calloutCtr;
	private Link syncOneUserLink;
	private Link removeFallBackAuthsLink;
	
	@Autowired
	private LDAPLoginModule ldapLoginModule;

	public LDAPAdminController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		
		ldapLoginManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
		ldapAdminVC = createVelocityContainer("ldapadmin");
		dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
		updateLastSyncDateInVC();
		// Create start LDAP sync link
		syncStartLink = LinkFactory.createButton("sync.button.start", ldapAdminVC, this);
		// remove olat-fallback authentications for ldap-users, see FXOLAT-284
		if (ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()){
			removeFallBackAuthsLink = LinkFactory.createButton("remove.fallback.auth", ldapAdminVC, this);
		}
		// Create start delete User link
		deleteStartLink = LinkFactory.createButton("delete.button.start", ldapAdminVC, this);
		// Create real-time log viewer
		LogRealTimeViewerController logViewController = new LogRealTimeViewerController(ureq, control, "org.olat.ldap", Level.DEBUG, false);
		listenTo(logViewController);
		ldapAdminVC.put("logViewController", logViewController.getInitialComponent());
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, LDAPLoginManager.ldapSyncLockOres);
		
		putInitialPanel(ldapAdminVC);
	}

	@Override
	protected void doDispose() {
		// Controller autodisposed by basic controller
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, LDAPLoginManager.ldapSyncLockOres);
        super.doDispose();
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof LDAPEvent) {
			LDAPEvent ldapEvent = (LDAPEvent)event;
			if(LDAPEvent.SYNCHING_ENDED.equals(ldapEvent.getCommand())) {
				syncTaskFinished(ldapEvent.isSuccess(), ldapEvent.getErrors()); 
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == syncStartLink) {
			// Start sync job
			// Disable start link during sync
			syncStartLink.setEnabled(false);
			LDAPEvent ldapEvent = new LDAPEvent(LDAPEvent.DO_SYNCHING);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ldapEvent, LDAPLoginManager.ldapSyncLockOres);
			showInfo("admin.synchronize.started");
		} else if (source == syncOneUserLink){
			userSearchCtrl = new UserSearchController(ureq, getWindowControl(), false);
			listenTo(userSearchCtrl);
			calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), userSearchCtrl.getInitialComponent(), syncOneUserLink, null, true, null);
			calloutCtr.addDisposableChildController(userSearchCtrl);
			calloutCtr.activate();
			listenTo(calloutCtr);
		} else if (source == deleteStartLink) {
			doStartDeleteProcess(ureq);
		} else if (source == removeFallBackAuthsLink){
			removeFallBackAuthsLink.setEnabled(false);
			ldapLoginManager.removeFallBackAuthentications();		
			showInfo("opsuccess");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteStepController) {
			if (event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(deleteStepController);
				showInfo("delete.step.cancel");
				deleteStartLink.setEnabled(true);
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(deleteStepController);
				if(hasIdentitiesToDeleteAfterRun){
					showInfo("delete.step.finish.users", amountUsersToDelete.toString());
				} else{
					showInfo("delete.step.finish.noUsers");
				}
				deleteStartLink.setEnabled(true);
			}	else if (source == userSearchCtrl) {
				calloutCtr.deactivate();
				Identity choosenIdent = ((SingleIdentityChosenEvent)event).getChosenIdentity();
				ldapLoginManager.doSyncSingleUserWithLoginAttribute(choosenIdent);
			}
		}
	}
	
	private void doStartDeleteProcess(UserRequest ureq) {
		// cancel if some one else is making sync or delete job
		if (!ldapLoginManager.acquireSyncLock()) {
			showError("delete.error.lock");
		} else {
			// check and get LDAP connection
			LdapContext ctx = ldapLoginManager.bindSystem();
			if (ctx == null) {
				showError("delete.error.connection");
				ldapLoginManager.freeSyncLock();
				return;
			}

			deleteStartLink.setEnabled(false);
			// get deleted users
			List<Identity> identitiesToDelete = null;
			try {
				identitiesToDelete = ldapLoginManager.getIdentitiesDeletedInLdap(ctx);
				ctx.close();
			} catch (NamingException e) {
				showError("delete.error.connection.close");
				logError("Could not close LDAP connection on manual delete sync", e);
			} finally {
				ldapLoginManager.freeSyncLock();
			}

			if (identitiesToDelete != null) {
				for(Iterator<Identity> it=identitiesToDelete.iterator(); it.hasNext(); ) {
					if(Identity.STATUS_PERMANENT.equals(it.next().getStatus())) {
						it.remove();
					}
				}
			}
			
			if (identitiesToDelete != null && !identitiesToDelete.isEmpty()) {
				// start step which spawns the whole wizard
				Step start = new DeletStep00(ureq, true, identitiesToDelete);
				// wizard finish callback called after "finish" is called
				StepRunnerCallback finishCallback = (uureq, control, runContext) -> {
					hasIdentitiesToDeleteAfterRun = ((Boolean) runContext.get("hasIdentitiesToDelete")).booleanValue();
					if (hasIdentitiesToDeleteAfterRun) {
						@SuppressWarnings("unchecked")
						List<Identity> idToDelete = (List<Identity>) runContext.get("identitiesToDelete");
						amountUsersToDelete = idToDelete.size();
						// Delete all identities now and tell everybody that
						// we are finished
						ldapLoginManager.deleteIdentities(idToDelete, getIdentity());
						return StepsMainRunController.DONE_MODIFIED;
					} else {
						// otherwise return without deleting anything
						return StepsMainRunController.DONE_UNCHANGED;
					}
				};
				deleteStepController = new StepsMainRunController(ureq, getWindowControl(), start, finishCallback, null,
						translate("admin.deleteUser.title"), "o_sel_ldap_delete_user_wizard");
				listenTo(deleteStepController);
				getWindowControl().pushAsModalDialog(deleteStepController.getInitialComponent());
			} else {
				showInfo("delete.step.noUsers");
				deleteStartLink.setEnabled(true);
			}
		}
	}
	
	
	/**
	 * Callback method for asynchronous sync thread. Called when sync is finished
	 * 
	 * @param success
	 * @param errors
	 */
	private void syncTaskFinished(boolean success, LDAPError errors) {
		if (success) {
			showWarning("admin.synchronize.finished.success");
			logInfo("LDAP user synchronize job finished successfully");
		} else {
			showError("admin.synchronize.finished.failure", errors.get());
			logInfo("LDAP user synchronize job finished with errors::" + errors.get());
		}
		// re-enable start link
		syncStartLink.setEnabled(true);
		// update last sync date
		updateLastSyncDateInVC();
	}
	
	/**
	 * Internal helper to push the last sync date to velocity
	 */
	private void updateLastSyncDateInVC() {
		Date date = ldapLoginManager.getLastSyncDate();
		if (date != null) {
			ldapAdminVC.contextPut("lastSyncDate", dateFormatter.format(date));
		}
	}

}
