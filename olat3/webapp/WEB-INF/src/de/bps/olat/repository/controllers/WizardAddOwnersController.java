/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.repository.controllers;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;


/**
 * 
 * Description:<br>
 * Wizard to add owners to a set of resources. 
 * 
 * <P>
 * Initial Date:  03.11.2008 <br>
 * @author bja
 */
public class WizardAddOwnersController extends WizardController {
	
	private static final int NUM_STEPS = 2; //2 steps
	private static final String CMD_SEARCH_ENTRIES = "cmd.searchEntries";
	private static final String CMD_SEARCH_OWNERS = "cmd.searchOwners";
	private static final String CMD_ALL_ENTRIES = "cmd.allEntries";
	private static final String CMD_MY_ENTRIES = "cmd.myEntries";
	private static final String CMD_SEARCH = "cmd.search";
	
	private VelocityContainer mainVc;
	private Panel panel;
	private VelocityContainer step1Vc, step2Vc;
	private Link myEntriesLink, allEntriesLink, searchEntriesLink;
	private RepositorySearchMultiSelectController searchCtr;
	private UserSearchController userSearchCtr;
	private List<Identity> owners;
	private List<RepositoryEntry> repoEntries;
	private boolean isAdmin, isAuthor;
	private Link backButton;
	
	public WizardAddOwnersController(UserRequest ureq, WindowControl control) {
		super(ureq, control, NUM_STEPS);
		
		setBasePackage(WizardAddOwnersController.class);
		
		isAdmin = ureq.getUserSession().getRoles().isOLATAdmin() | ureq.getUserSession().getRoles().isInstitutionalResourceManager();
		isAuthor = isAdmin | ureq.getUserSession().getRoles().isAuthor();
		
		owners = new ArrayList<Identity>();
		repoEntries = new ArrayList<RepositoryEntry>();
		mainVc = createVelocityContainer("wizard");
		LinkFactory.createLinkBack(mainVc, this);
		panel = new Panel("panel");
		// step 1
		buildStep1(ureq);
		mainVc.put("panel", panel);
		
		this.setWizardTitle(translate("wizard.add.owners.title"));
		this.setNextWizardStep(translate("add.owners.step1"), mainVc);
	}

	/**
	 * Wizard Step 1
	 * @param ureq
	 */
	private void buildStep1(UserRequest ureq) {
		
		step1Vc = createVelocityContainer("step1_wizard_add_owners");
		
		searchCtr = new RepositorySearchMultiSelectController(null, ureq, getWindowControl(), false, false, null, isAuthor & !isAdmin ? ureq.getIdentity().getName() : null);
		listenTo(searchCtr);
		
	  // links
		if(isAuthor) {
			searchEntriesLink = LinkFactory.createCustomLink("searchEntriesLink", CMD_SEARCH_ENTRIES, "wizard.step1." + CMD_SEARCH_ENTRIES,	Link.LINK, step1Vc, this);
		}
		if(isAdmin) {
			allEntriesLink = LinkFactory.createCustomLink("allEntriesLink", CMD_ALL_ENTRIES, "wizard.step1." + CMD_ALL_ENTRIES, Link.LINK, step1Vc, this);
		}
		myEntriesLink = LinkFactory.createCustomLink("myEntriesLink", CMD_MY_ENTRIES, "wizard.step1." + CMD_MY_ENTRIES, Link.LINK, step1Vc, this);
		
		event(ureq, myEntriesLink, null);

		step1Vc.put("searchCtr", searchCtr.getInitialComponent());
		panel.setContent(step1Vc);
	}
	
	/**
	 * 
	 * @param ureq
	 */
	private void buildStep2(UserRequest ureq) {
		
		step2Vc = createVelocityContainer("step2_wizard_add_owners");
		step2Vc.contextPut("subtitle", translate("wizard.step2." + CMD_SEARCH_OWNERS));
		
		removeAsListenerAndDispose(userSearchCtr);
		userSearchCtr = new UserSearchController(ureq, getWindowControl(), false, true, false, UserSearchController.ACTION_KEY_CHOOSE_FINISH);
		listenTo(userSearchCtr);
		
		// back button
		backButton = LinkFactory.createButton("btn.back", step2Vc, this);
		
		step2Vc.put("userSearchCtr", userSearchCtr.getInitialComponent());
		panel.setContent(step2Vc);
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		// events for finish and cancel button
		super.event(ureq, source, event);
		
		if (source == myEntriesLink) {
			searchCtr.doSearchByOwner(ureq.getIdentity());
			step1Vc.contextPut("subtitle", translate("wizard.step1." + CMD_MY_ENTRIES));
		  myEntriesLink.setCustomEnabledLinkCSS("b_selected");
		  if(isAdmin) allEntriesLink.removeCSS();
		  if(isAdmin | isAuthor) searchEntriesLink.removeCSS();
			panel.setContent(step1Vc);
		}
		if (source == allEntriesLink) {
			searchCtr.doSearchAll(ureq);
			step1Vc.contextPut("subtitle", translate("wizard.step1." + CMD_ALL_ENTRIES));
			myEntriesLink.removeCSS();
			allEntriesLink.setCustomEnabledLinkCSS("b_selected");
			if(isAdmin | isAuthor) searchEntriesLink.removeCSS();
			panel.setContent(step1Vc);
		}
		if (source == searchEntriesLink) {
			step1Vc.contextPut("subtitle", translate("wizard.step1." + CMD_SEARCH_ENTRIES));
			searchCtr.displaySearchForm();
			step1Vc.contextPut("subtitle", translate("wizard.step1." + CMD_SEARCH));
			myEntriesLink.removeCSS();
			searchEntriesLink.setCustomEnabledLinkCSS("b_selected");
			if(isAdmin) allEntriesLink.removeCSS();
			panel.setContent(step1Vc);
		} 
		if (source == backButton) {
			buildStep1(ureq);
			this.setWizardTitle(translate("wizard.add.owners.title"));
			this.setBackWizardStep(translate("add.owners.step1"), mainVc);
			mainVc.setDirty(true);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if ( source == searchCtr ) {
			if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event; 
				if (tmse.getAction().equals(RepositorySearchMultiSelectController.ACTION_MULTI_SELECT)) {
					boolean hasSelected = false;
					for (int i = tmse.getSelection().nextSetBit(0); i >= 0; i = tmse.getSelection().nextSetBit(i+1)) {
						if (i != -1) {
							hasSelected = true;
							repoEntries.add(searchCtr.getValueAt(i));
						}
					} 
					if(hasSelected) {
						buildStep2(ureq);
						this.setNextWizardStep(translate("add.owners.step2"), mainVc);
					} else {
						showError("wizard.add.owners.step1.notselected");
					}
				}
			}
		}
		if ( source == userSearchCtr ) {
			if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent mice = (MultiIdentityChosenEvent) event; 
				owners = mice.getChosenIdentities();
			}
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent  sice = (SingleIdentityChosenEvent) event;
				final Identity choosenIdentity = sice.getChosenIdentity();
				owners.add(sice.getChosenIdentity());
			}
			//add choosen identities as owners
			if ((owners.size() > 0) && (repoEntries.size() > 0)) {
				addOwnersToRepositoryEntry();
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				// info message
				showError("info.message.add.owners.empty");
			}
		}
	}
	
	/**
	 * add owners to a set of repository entries
	 */
	private void addOwnersToRepositoryEntry() {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		for (RepositoryEntry entry : repoEntries) {
			SecurityGroup secGroup = entry.getOwnerGroup();
			for (Identity identity : owners) {
				if (!securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
					securityManager.addIdentityToSecurityGroup(identity, secGroup);
				}
			}
		}
		// info message
		showInfo("info.message.add.owners");
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
