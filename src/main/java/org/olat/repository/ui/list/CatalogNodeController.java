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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.ui.CatalogEntryComparator;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogNodeController extends BasicController implements Activateable2, StackedControllerAware {

	private StackedController stackPanel;
	private CatalogEntryListController entryListController;
	
	private final VelocityContainer mainVC;
	private Link contactLink;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private CatalogNodeController childNodeController;
	private RepositorySearchController searchResourceCtrl;

	private CatalogEntry catalogEntry;

	private final CatalogManager cm;
	private final RepositoryManager repositoryManager;
	
	public CatalogNodeController(UserRequest ureq, WindowControl wControl, CatalogEntry catalogEntry) {
		// fallback translator to repository package to reduce redundant translations
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));

		cm = CatalogManager.getInstance();
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);

		this.catalogEntry = catalogEntry;
		
		mainVC = createVelocityContainer("node");

		//catalog resources
		entryListController = new CatalogEntryListController(ureq, wControl);
		mainVC.put("entries", entryListController.getInitialComponent());
		listenTo(entryListController);


		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setContent(mainVC);
		putInitialPanel(mainPanel);
		updateContent(ureq);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == contactLink) {
			doContactOwners(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			if("select_node".equals(link.getCommand())) {
				Long categoryNodeKey = (Long)link.getUserObject();
				CatalogEntry entry = cm.getCatalogEntryByKey(categoryNodeKey);
				if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
					removeAsListenerAndDispose(childNodeController);
					childNodeController = new CatalogNodeController(ureq, getWindowControl(), entry);
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
		if(source == searchResourceCtrl) {
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				addResource(searchResourceCtrl.getSelectedEntry());
				updateGUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		//add/remove owners
		} else if(source == contactCtrl) {
			cmc.deactivate();
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
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(searchResourceCtrl);
		
		cmc = null;
		contactCtrl = null;
		searchResourceCtrl = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
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