/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.ldap.ui;

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
import org.olat.ldap.LDAPLoginManager;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExternalUserController extends BasicController {

	private CloseableModalController cmc;
	private final VelocityContainer mainVC;
	private UserCreateController userCreateCtrl;
	private final ImportExternalUserSearchController searchController;

	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MicrosoftAzureADFSProvider azureADFSProvider;
	
	public ImportExternalUserController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("import_user");
		searchController = new ImportExternalUserSearchController(ureq, getWindowControl());
		listenTo(searchController);
		mainVC.put("content", searchController.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchController == source) {
			if(event instanceof SingleIdentityChosenEvent) {
				doImport(ureq, ((SingleIdentityChosenEvent)event).getChosenIdentity());
			}
		} else if(userCreateCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent) {
				Identity ident = ((SingleIdentityChosenEvent)event).getChosenIdentity();
				/* TODO selectus
				if(userCreateCtrl.getProposedIdentity() != null) {
					doAuthentications(ident, userCreateCtrl.getProposedIdentity());
				}
				*/
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
	
	private Identity doAuthentications(Identity ident, TransientIdentity proposedIdentity) {
		if(proposedIdentity != null) {
			//TODO selectus
			/*
			if(proposedIdentity.isLdap()) {
				ident = ldapManager.makeLdapUser(ident, userCreateCtrl.getUsername());
			} else if(proposedIdentity.isAzure() && azureADFSProvider.isEnabled()) {
				String id = proposedIdentity.getName();
				securityManager.createAndPersistAuthentication(ident, azureADFSProvider.getProviderName(), BaseSecurity.DEFAULT_ISSUER, null, id, null, null);
			}
			*/
		}
		return ident;
	}
	
	private void doImport(UserRequest ureq, Identity identity) {
		Identity importedIdentity = null;
		if(identity instanceof TransientIdentity) {
			TransientIdentity identityToImport = (TransientIdentity)identity;
			//TODO selectus
			/*
			if(identityToImport.isLdap() || identityToImport.isAzure()) {
				doEnhanceProfile(ureq, identityToImport);
			}
			*/
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
		//TODO selectus
		/*
		userCreateCtrl = new UserCreateController(ureq, getWindowControl(), false, true);
		userCreateCtrl.prefillFields(identityToImport);
		userCreateCtrl.getAndRemoveFormTitle();
		listenTo(userCreateCtrl);
		*/
		
		cmc = new CloseableModalController(getWindowControl(), "c", userCreateCtrl.getInitialComponent(), translate("import.ldap.user.title"));
		cmc.activate();
		listenTo(cmc);
	}
}