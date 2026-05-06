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
package org.olat.user.ui.importexternal;

import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportExternalUserController extends BasicController {

	private CloseableModalController cmc;
	private final VelocityContainer mainVC;
	private UserCreateController userCreateCtrl;
	private Organisation preselectedOrganisation;
	private final ImportExternalUserSearchController searchController;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MicrosoftAzureADFSProvider azureADFSProvider;
	
	public ImportExternalUserController(UserRequest ureq, WindowControl wControl, Organisation preselectedOrganisation) {
		super(ureq, wControl);
		this.preselectedOrganisation = preselectedOrganisation;
		
		mainVC = createVelocityContainer("import_user");
		searchController = new ImportExternalUserSearchController(ureq, getWindowControl());
		listenTo(searchController);
		mainVC.put("content", searchController.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchController == source) {
			if(event instanceof SingleIdentityChosenEvent sice) {
				doImport(ureq, sice.getChosenIdentity());
			}
		} else if(userCreateCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent sice) {
				Identity ident = sice.getChosenIdentity();
				if(userCreateCtrl.getUserObject() instanceof TransientIdentity proposedIdentity) {
					doAuthentications(ident, proposedIdentity, userCreateCtrl.getUsername());
				}
				doImport(ureq, ident);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userCreateCtrl);
		removeAsListenerAndDispose(cmc);
		userCreateCtrl = null;
		cmc = null;
	}
	
	private Identity doAuthentications(Identity ident, TransientIdentity proposedIdentity, String username) {
		if(proposedIdentity != null) {
			if(proposedIdentity.isLdap()) {
				securityManager.createAndPersistAuthentication(ident, LDAPAuthenticationController.PROVIDER_LDAP,
						BaseSecurity.DEFAULT_ISSUER, null, username, null, null);	
			} else if(proposedIdentity.isAzure() && azureADFSProvider.isEnabled()) {
				securityManager.createAndPersistAuthentication(ident, azureADFSProvider.getProviderName(),
						BaseSecurity.DEFAULT_ISSUER, null, proposedIdentity.getName(), null, null);
			}
		}
		return ident;
	}
	
	private void doImport(UserRequest ureq, Identity identity) {
		Identity importedIdentity = null;
		if(identity instanceof TransientIdentity identityToImport
				&& (identityToImport.isLdap() || identityToImport.isAzure())) {
			doEnhanceProfile(ureq, identityToImport);
		} else {
			importedIdentity = identity;
		}

		if(importedIdentity == null) {
			//do something
		} else {
			fireEvent(ureq, new SingleIdentityChosenEvent(importedIdentity));
		}
	}
	
	private void doEnhanceProfile(UserRequest ureq, TransientIdentity identityToImport) {
		userCreateCtrl = new UserCreateController(ureq, getWindowControl(), preselectedOrganisation, false);
		userCreateCtrl.prefill(identityToImport);
		userCreateCtrl.getAndRemoveFormTitle();
		userCreateCtrl.setUserObject(identityToImport);
		listenTo(userCreateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", userCreateCtrl.getInitialComponent(), translate("import.ldap.user.title"));
		cmc.activate();
		listenTo(cmc);
	}
}