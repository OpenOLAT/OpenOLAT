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

package org.olat.catalog.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryTableModel;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.repository.controllers.RepositorySearchController;

/**
 * 
 * Initial date: 21.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogNodeController extends BasicController implements Activateable2, StackedControllerAware {

	// locking stuff for catalog edit operations
	private LockResult catModificationLock;
	public static final String LOCK_TOKEN = "catalogeditlock";


	private StackedController stackPanel;
	private CatalogEntryListController entryListController;
	
	private final VelocityContainer mainVC;
	private Link nominateLink, contactLink, editLink, deleteLink;
	private Link addCategoryLink, addResourceLink;
	
	private CloseableModalController cmc;
	private GroupController groupController;
	private ContactFormController contactCtrl;
	private EntryForm addEntryForm, editEntryForm;
	private DialogBoxController dialogDeleteSubtree;
	private CatalogNodeController childNodeController;
	private RepositorySearchController searchResourceCtrl;

	private CatalogEntry catalogEntry;

	private final CatalogManager cm;
	private final BaseSecurity securityManager;
	private final RepositoryManager repositoryManager;
	
	private boolean localTreeAdmin; 
	
	public CatalogNodeController(UserRequest ureq, WindowControl wControl, CatalogEntry catalogEntry,
			boolean admin) {
		// fallback translator to repository package to reduce redundant translations
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));
		
		
		cm = CatalogManager.getInstance();
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);

		this.catalogEntry = catalogEntry;
		
		localTreeAdmin = admin
				|| securityManager.isIdentityInSecurityGroup(getIdentity(), catalogEntry.getOwnerGroup());
		
		boolean root = catalogEntry.getParent() == null;

		mainVC = createVelocityContainer("node");
		//toolbars
		if(localTreeAdmin) {
			editLink = LinkFactory.createButton("tools.edit.catalog.category", mainVC, this);
			nominateLink = LinkFactory.createButton("tools.edit.catalog.category.ownergroup", mainVC, this);
			if(!root) {
				deleteLink = LinkFactory.createButton("tools.delete.catalog.entry", mainVC, this);
				//move
			}
			addCategoryLink = LinkFactory.createButton("tools.add.catalog.category", mainVC, this);
		}
		
		if(localTreeAdmin || ureq.getUserSession().getRoles().isAuthor()) {
			contactLink = LinkFactory.createButton("contact.to.groupname.caretaker", mainVC, this);
			contactLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_mail");
			addResourceLink = LinkFactory.createButton("tools.add.catalog.link", mainVC, this);
		}

		//catalog resources
		entryListController = new CatalogEntryListController(ureq, wControl);
		mainVC.put("entries", entryListController.getInitialComponent());
		listenTo(entryListController);

		putInitialPanel(mainVC);
		updateContent(ureq);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == addCategoryLink) {
			doAddCategory(ureq);
		} else if(source == editLink) {
			doEditCateogry(ureq);
		} else if(source == nominateLink) {
			doEditOwners(ureq);
		} else if(source == addResourceLink) {
			doAddResource(ureq);
		} else if(source == contactLink) {
			doContactOwners(ureq);
		} else if(source == deleteLink) {
			doDelete(ureq);
		}
		
		
		
		else if(source instanceof Link) {
			Link link = (Link)source;
			if("select_node".equals(link.getCommand())) {
				Long categoryNodeKey = (Long)link.getUserObject();
				CatalogEntry entry = cm.getCatalogEntryByKey(categoryNodeKey);
				if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
					removeAsListenerAndDispose(childNodeController);
					childNodeController = new CatalogNodeController(ureq, getWindowControl(), entry, localTreeAdmin);
					listenTo(childNodeController);
					childNodeController.setStackedController(stackPanel);
					stackPanel.pushController(entry.getName(), childNodeController);	
				}
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		//add sub catalog entry
		if(source == addEntryForm) {
			if(event == Event.DONE_EVENT) {
				createSubCategory();
				updateGUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		//edit catalog entry
		} else if(source == editEntryForm) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateCurrentCategory();
				updateGUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		//add a new resource
		} else if(source == searchResourceCtrl) {
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				addResource(searchResourceCtrl.getSelectedEntry());
				updateGUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		//add/remove owners
		} else if (source == groupController) {
			if(event instanceof IdentitiesAddEvent ) {
				addOwners((IdentitiesAddEvent)event);
			} else if (event instanceof IdentitiesRemoveEvent) {
				removeOwners((IdentitiesRemoveEvent)event);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == contactCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == dialogDeleteSubtree) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				deleteCatalogEntry();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cleanUp();
		} else if(source == childNodeController) {
			if(event == Event.DONE_EVENT) {
				stackPanel.popController(childNodeController);
				removeAsListenerAndDispose(childNodeController);
				childNodeController = null;
				updateGUI(ureq);
			} else if(event == Event.CHANGED_EVENT) {
				updateGUI(ureq);
			}
		} else if(source == entryListController) {
			if(event == Event.CHANGED_EVENT) {
				updateGUI(ureq);
			}
		} else if(source == cmc) {
			cleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void cleanUp() {
		if (catModificationLock != null && catModificationLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
			catModificationLock = null;
		}
		
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(addEntryForm);
		removeAsListenerAndDispose(editEntryForm);
		removeAsListenerAndDispose(groupController);
		removeAsListenerAndDispose(searchResourceCtrl);
		removeAsListenerAndDispose(dialogDeleteSubtree);
		
		cmc = null;
		contactCtrl = null;
		addEntryForm = null;
		editEntryForm = null;
		groupController = null;
		searchResourceCtrl = null;
		dialogDeleteSubtree = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// remove any locks
		if (catModificationLock != null && catModificationLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
			catModificationLock = null;
		}
	}
	
	private boolean acquireLock() {
		catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), getIdentity(), LOCK_TOKEN);
		if (!catModificationLock.isSuccess()) {
			showError("catalog.locked.by", catModificationLock.getOwner().getName());
			return false;
		}
		return true;
	}
	
	private void doAddCategory(UserRequest ureq) {
		if (!acquireLock()) return;
		
		addEntryForm = new EntryForm(ureq, getWindowControl(), false);
		addEntryForm.setElementCssClass("o_sel_catalog_add_category_popup");
		listenTo(addEntryForm);

		cmc = new CloseableModalController(getWindowControl(), "close", addEntryForm.getInitialComponent(), true, translate("tools.add.catalog.category"));
		listenTo(cmc);
		cmc.activate();		
	}
	
	private void doEditCateogry(UserRequest ureq) {
		if (!acquireLock()) return;
		
		editEntryForm = new EntryForm(ureq, getWindowControl(), false);
		editEntryForm.setElementCssClass("o_sel_catalog_edit_category_popup");
		editEntryForm.setFormFields(catalogEntry);// fill the
		listenTo(editEntryForm);

		cmc = new CloseableModalController(getWindowControl(), "close", editEntryForm.getInitialComponent(), true, translate("tools.edit.catalog.category"));
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doDelete(UserRequest ureq) {
		if (!acquireLock()) return;
		
		String[] trnslP = { catalogEntry.getName() };
		dialogDeleteSubtree = activateYesNoDialog(ureq, null, translate("dialog.modal.subtree.delete.text", trnslP), dialogDeleteSubtree);
	}
	
	private void doAddResource(UserRequest ureq) {
		searchResourceCtrl = new RepositorySearchController(translate("choose"), ureq, getWindowControl(), true, false, false);
		listenTo(searchResourceCtrl);
		if (ureq.getUserSession().getRoles().isOLATAdmin()
				|| ureq.getUserSession().getRoles().isInstitutionalResourceManager()) {
			searchResourceCtrl.displaySearchForm();
		} else {
			searchResourceCtrl.doSearchByOwnerLimitAccess(ureq.getIdentity());
		}
		// open form in dialog
		cmc = new CloseableModalController(getWindowControl(), "close", searchResourceCtrl.getInitialComponent(), true, translate("tools.add.catalog.link"));
		listenTo(cmc);
		cmc.activate();		
	}
	
	private void doEditOwners(UserRequest ureq) {
		SecurityGroup secGroup = catalogEntry.getOwnerGroup();
		if (secGroup == null) {
			catalogEntry = cm.loadCatalogEntry(catalogEntry);
			catalogEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
			catalogEntry = cm.updateCatalogEntry(catalogEntry);
		}
		
		groupController = new GroupController(ureq, getWindowControl(), true, false, false, false, false, false, secGroup);
		listenTo(groupController);
		
		cmc = new CloseableModalController(getWindowControl(), "close", groupController.getInitialComponent(), true, translate("tools.edit.catalog.category.ownergroup"));
		listenTo(cmc);
		cmc.activate();	
	}
	
	
	private void doContactOwners(UserRequest ureq) {
		ContactList caretaker = new ContactList(translate("contact.to.groupname.caretaker"));
		List<Identity> owners = cm.getOwnersOfParentLine(catalogEntry);
		caretaker.addAllIdentites(owners);
		ContactMessage cmsg = new ContactMessage(getIdentity());
		cmsg.addEmailTo(caretaker);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
		listenTo(contactCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", contactCtrl.getInitialComponent(), true, translate("contact.caretaker"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void createSubCategory() {
		CatalogEntry ce = cm.createCatalogEntry();
		addEntryForm.fillEntry(ce);
		ce.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
		ce.setRepositoryEntry(null);
		ce.setParent(catalogEntry);
		cm.saveCatalogEntry(ce);
	}
	
	private void updateCurrentCategory() {
		catalogEntry = cm.getCatalogEntryByKey(catalogEntry.getKey());
		editEntryForm.fillEntry(catalogEntry);
		catalogEntry = cm.updateCatalogEntry(catalogEntry);
	}
	
	private void deleteCatalogEntry() {
		cm.deleteCatalogEntry(catalogEntry);
	}
	
	private void addResource(RepositoryEntry re) {
		CatalogEntry newCatalogEntry = cm.createCatalogEntry();
		newCatalogEntry.setName(re.getDisplayname());
		newCatalogEntry.setDescription(re.getDescription());
		newCatalogEntry.setRepositoryEntry(re);
		newCatalogEntry.setType(CatalogEntry.TYPE_LEAF);
		newCatalogEntry.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
		cm.addCatalogEntry(catalogEntry, newCatalogEntry);
		cm.saveCatalogEntry(newCatalogEntry);
	}
	
	private void addOwners(IdentitiesAddEvent identitiesAddedEvent) {
		List<Identity> list = identitiesAddedEvent.getAddIdentities();
    for (Identity identity : list) {
    	if (!securityManager.isIdentityInSecurityGroup(identity, catalogEntry.getOwnerGroup())) {
    		securityManager.addIdentityToSecurityGroup(identity, catalogEntry.getOwnerGroup());
    		identitiesAddedEvent.getAddedIdentities().add(identity);
    	}
    }
	}
	
	private void removeOwners(IdentitiesRemoveEvent identitiesRemoveEvent) {
		List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
    for (Identity identity : list) {
    	securityManager.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
    }	
	}
	

	/**
	 * refresh content of current category for displaying
	 * 
	 * @param ce
	 * @param ceLevel
	 */
	private void updateGUI(UserRequest ureq) {
		updateContent(ureq);
		updateHistory(ureq);
	}

	private void updateContent(UserRequest ureq) {
		mainVC.contextPut("catalogEntryName", catalogEntry.getName());
		if(StringHelper.containsNonWhitespace(catalogEntry.getDescription())) {
			mainVC.contextPut("catalogEntryDesc", catalogEntry.getDescription());
		}
		
		List<CatalogEntry> childCe = cm.getChildrenOf(catalogEntry);
		// Sort to fix ordering by repo entry display name. For leafs the displayed
		// name is not the catalog entry name but the repo entry display name. The
		// SQL query orders by catalog entry name, thus the visual ordering is
		// wrong.
		Collections.sort(childCe, new CatalogEntryComparator(getLocale()));
	
		List<Long> repositoryEntryKeys = new ArrayList<Long>();
		List<Link> subCategories = new ArrayList<Link>();
		for (CatalogEntry entry : childCe) {
			if(entry.getType() == CatalogEntry.TYPE_NODE) {
				String cmpId = "cat_" + entry.getKey();
				Link link = LinkFactory.createCustomLink(cmpId, "select_node", cmpId, Link.LINK + Link.NONTRANSLATED, mainVC, this);
				link.setCustomDisplayText(entry.getName());
				link.setCustomEnabledLinkCSS("b_with_small_icon_left o_catalog_sub_icon");
				link.setUserObject(entry.getKey());
				subCategories.add(link);
			} else if(entry.getRepositoryEntry() != null && entry.getRepositoryEntry().getOlatResource() != null) {
				repositoryEntryKeys.add(entry.getRepositoryEntry().getKey());
			}
		}
		boolean hasEntries = subCategories.size() != childCe.size();
		mainVC.getComponent("entries").setVisible(hasEntries);
		mainVC.contextPut("subCategories", subCategories);
		
		if(!repositoryEntryKeys.isEmpty()) {
			SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setIdentity(getIdentity());
			params.setRoles(ureq.getUserSession().getRoles());
			params.setRepositoryEntryKeys(repositoryEntryKeys);
			List<RepositoryEntry> allowedEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);

			List<CatalogEntry> catalogEntriesOfRepo = new ArrayList<CatalogEntry>(childCe);
			for(Iterator<CatalogEntry> it=catalogEntriesOfRepo.iterator(); it.hasNext(); ) {
				RepositoryEntry catRepoEntry = it.next().getRepositoryEntry();
				if(catRepoEntry == null || !allowedEntries.contains(catRepoEntry)) {
					it.remove();
				}
			}
	
			entryListController.setCatalogEntries(catalogEntriesOfRepo);
			entryListController.loadModel();
		}

		String url = Settings.getServerContextPathURI() + "/url/CatalogEntry/" + catalogEntry.getKey();
		mainVC.contextPut("guestExtLink", url + "?guest=true&amp;lang=" + getLocale().getLanguage());
		if (!ureq.getUserSession().getRoles().isGuestOnly()) {
			mainVC.contextPut("extLink", url);
		}
	}

	public void updateHistory(UserRequest ureq) {
		if(catalogEntry != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CatalogEntry.class, catalogEntry.getKey());
			addToHistory(ureq, ores, null);
		} else {
			addToHistory(ureq);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
	}
	
	public class PriceMethod {
		private String price;
		private String type;
		
		public PriceMethod(String price, String type) {
			this.price = price;
			this.type = type;
		}
		
		public String getPrice() {
			return price;
		}
		
		public String getType() {
			return type;
		}
	}
}