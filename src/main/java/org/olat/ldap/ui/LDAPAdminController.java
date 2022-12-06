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
	private Link inactivateStartLink;
	private StepsMainRunController removeStepController;

	private UserSearchController userSearchCtrl;
	private CloseableCalloutWindowController calloutCtr;
	private Link syncOneUserLink;
	private Link removeFallBackAuthsLink;
	
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;

	public LDAPAdminController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		
		ldapAdminVC = createVelocityContainer("ldapadmin");
		dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
		updateLastSyncDateInVC();
		// Create start LDAP sync link
		syncStartLink = LinkFactory.createButton("sync.button.start", ldapAdminVC, this);
		// remove olat-fallback authentications for ldap-users, see FXOLAT-284
		if (ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()){
			removeFallBackAuthsLink = LinkFactory.createButton("remove.fallback.auth", ldapAdminVC, this);
		}
		// Create start removal links
		deleteStartLink = LinkFactory.createButton("delete.button.start", ldapAdminVC, this);
		inactivateStartLink = LinkFactory.createButton("inactivate.button.start", ldapAdminVC, this);
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
			doStartRemovalProcess(ureq, true);
		} else if (source == inactivateStartLink) {
			doStartRemovalProcess(ureq, false);
		} else if (source == removeFallBackAuthsLink){
			removeFallBackAuthsLink.setEnabled(false);
			ldapLoginManager.removeFallBackAuthentications();		
			showInfo("opsuccess");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == removeStepController) {
			if (event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(removeStepController);
				showInfo("delete.step.cancel");
				deleteStartLink.setEnabled(true);
				inactivateStartLink.setEnabled(true);
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(removeStepController);
				
				Integer identitiesDeleted = (Integer)removeStepController.getRunContext().get("identitiesDeleted");
				Integer identitiesInactivated = (Integer)removeStepController.getRunContext().get("identitiesInactivated");
				if (identitiesDeleted != null ) {
					showInfo("delete.step.finish.users", identitiesDeleted.toString());
				} else if (identitiesInactivated != null ) {
					showInfo("inactivate.step.finish.users", identitiesInactivated.toString());
				} else {
					showInfo("delete.step.finish.noUsers");
				}
				deleteStartLink.setEnabled(true);
				inactivateStartLink.setEnabled(true);
			}
		} else if (source == userSearchCtrl) {
			calloutCtr.deactivate();
			Identity choosenIdent = ((SingleIdentityChosenEvent)event).getChosenIdentity();
			ldapLoginManager.doSyncSingleUserWithLoginAttribute(choosenIdent);
		}
	}
	
	private void doStartRemovalProcess(UserRequest ureq, final boolean delete) {
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
			inactivateStartLink.setEnabled(false);
			
			final List<Identity> identitiesForRemoval = getIdentitiesForRemoval(ctx, delete);
			if (identitiesForRemoval == null || identitiesForRemoval.isEmpty()) {
				showInfo("delete.step.noUsers");
				deleteStartLink.setEnabled(true);
			} else {
				// start step which spawns the whole wizard
				Step start = new RemovalStep00(ureq, delete, identitiesForRemoval);
				// wizard finish callback called after "finish" is called
				StepRunnerCallback finishCallback = (uureq, control, runContext) -> {
					@SuppressWarnings("unchecked")
					final List<Identity> identities = (List<Identity>) runContext.get("identitiesToDelete");
					if (identities != null && !identities.isEmpty()) {
						if(delete) {
							runContext.put("identitiesDeleted", Integer.valueOf(identities.size()));
							ldapLoginManager.deleteIdentities(identities, getIdentity());
						} else {
							runContext.put("identitiesInactivated", Integer.valueOf(identities.size()));
							ldapLoginManager.inactivateIdentities(identities, getIdentity());
						}
						return StepsMainRunController.DONE_MODIFIED;
					}
					// otherwise return without deleting anything
					return StepsMainRunController.DONE_UNCHANGED;
				};
				
				String i18nTitle = delete ? "wizard.title.delete" : "wizard.title.inactivate";
				removeStepController = new StepsMainRunController(ureq, getWindowControl(), start, finishCallback, null,
						translate(i18nTitle), "o_sel_ldap_delete_user_wizard");
				listenTo(removeStepController);
				getWindowControl().pushAsModalDialog(removeStepController.getInitialComponent());
			}
		}
	}
	
	private List<Identity> getIdentitiesForRemoval(LdapContext ctx, boolean delete) {
		// get deleted users
		List<Identity> identitiesForRemoval = null;
		try {
			identitiesForRemoval = ldapLoginManager.getIdentitiesDeletedInLdap(ctx);
			ctx.close();
		} catch (NamingException e) {
			showError("delete.error.connection.close");
			logError("Could not close LDAP connection on manual delete sync", e);
		} finally {
			ldapLoginManager.freeSyncLock();
		}

		if (identitiesForRemoval != null) {
			for(Iterator<Identity> it=identitiesForRemoval.iterator(); it.hasNext(); ) {
				Integer identityStatus = it.next().getStatus();
				if(Identity.STATUS_PERMANENT.equals(identityStatus)
						|| (!delete && Identity.STATUS_INACTIVE.equals(identityStatus))) {
					it.remove();
				}
			}
		}
		
		return identitiesForRemoval;
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
