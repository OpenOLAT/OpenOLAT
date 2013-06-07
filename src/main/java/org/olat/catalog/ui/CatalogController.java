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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.olat.NewControllerFactory;
import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryIconRenderer;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryTableModel;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.RepositoryEditDescriptionController;
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;

/**
 * <pre>
 * Description:
 * <P>
 * The CatalogController is responsible for:
 * <ul>
 * <li>displaying the catalog with its categories and linked resources,
 * starting from the supplied root node</li>
 * <li>handling the access to the actual category:
 * <ul>
 * <li>OLATAdmin is/should be the owner of catalog root</li>
 * <li>LocalTreeAdmin is administrator of a subtree within the catalog</li>
 * <li>Author is responsible for adding his resources to the catalog</li>
 * <li>ordinary user browses the catalog for quickly finding his desired
 * resources</li>
 * </ul>
 * The CatalogController accomplish this access rights by modifying the
 * corresponding toolbox entries in the GUI, which grant access to the mentioned
 * actions.</li>
 * <li>handles the controllers and forms allowing to edit, create the catalog
 * structure
 * <ol>
 * <li>change category data (name, description)</li>
 * <li>modify category's localTreeAdmin group</li>
 * <li>contact category's localTreeAdmin group</li>
 * <li>add a new subcategory</li>
 * <li>add a resource link</li>
 * <li>remove a resource link</li>
 * <li>paste structure</li>
 * </ol>
 * The OLATAdmin as superuser can invoke all of the above listed actions. <br>
 * Whereas the LocalTreeAdmin is restricted to the follwing set: on the
 * LocalTreeAdmin's subtree-root-node only 4,6 are possible. But from the
 * children on he/she can invoke any of 1-6 except 5.<br>
 * The author is solely able to invoke 5.
 * </ul>
 * </li>
 * </ul>
 * The catalog is based upon the idea of having a lot of resources which must
 * somehow be ordered to find them quickly. Frankly speaking the catalog only
 * makes sense if no access restrictions to the linked resources apply. This in
 * mind, it is solely possible to link resources which are accessible for the
 * users of the installation.
 * </pre>
 * Date: 2005/10/14 12:35:40 <br>
 * @author Felix Jost
 */
public class CatalogController extends BasicController implements Activateable2 {

	// catalog actions
	
	private static final String ACTION_ADD_CTLGLINK = "addCtlglink";
	private static final String ACTION_ADD_CTLGCATEGORY = "addCtlgCategory";
	private static final String ACTION_EDIT_CTLGCATEGORY = "editCtlgCategory";
	private static final String ACTION_EDIT_CTLGCATOWNER = "editCtlgCategoryOwnerGroup";
	private static final String ACTION_DELETE_CTLGCATEGORY = "actionDeleteCtlgCategory";
	private static final String ACTION_NEW_CTGREQUEST = "actionCategoryRequest";
	private static final String ACTION_ADD_STRUCTURE = "addStructure";
	
	private static final String ACTION_ADD_BOOKMARK = "addBookmark";
	private static final String ACTION_MOVE_ENTRY = "moveEntry";
	
	// commands in table and history
	
	private static final String CATCMD_HISTORY = "history";
	private static final String CATCMD_REMOVE = "remove.";
	private static final String CATCMD_EDIT = "edit.";
	private static final String CATCMD_DETAIL = "detail.";
	private static final String CATCMD_MOVE = "move.";
	
	// URL command  
	
	private static final String CATENTRY_CHILD = "child";
	private static final String CATENTRY_LEAF = "leaf";
	private static final String CATENTRY_NODE = "node";
	
	// NLS support
	
	private static final String NLS_DIALOG_MODAL_LEAF_DELETE_TEXT = "dialog.modal.leaf.delete.text";
	private static final String NLS_CHOOSE = "choose";
	private static final String NLS_DIALOG_MODAL_SUBTREE_DELETE_TEXT = "dialog.modal.subtree.delete.text";
	private static final String NLS_CONTACT_TO_GROUPNAME_CARETAKER = "contact.to.groupname.caretaker";
	private static final String NLS_TOOLS_EDIT_CATALOG_CATEGORY = "tools.edit.catalog.category";
	private static final String NLS_TOOLS_EDIT_CATALOG_CATEGORY_OWNERGROUP = "tools.edit.catalog.category.ownergroup";
	private static final String NLS_TOOLS_NEW_CATALOG_CATEGORYREQUEST = "tools.new.catalog.categoryrequest";
	private static final String NLS_TOOLS_DELETE_CATALOG_ENTRY = "tools.delete.catalog.entry";
	private static final String NLS_TOOLS_ADD_HEADER = "tools.add.header";
	private static final String NLS_TOOLS_ADD_CATALOG_CATEGORY = "tools.add.catalog.category";
	private static final String NLS_TOOLS_ADD_CATALOG_LINK = "tools.add.catalog.link";
	private static final String NLS_TOOLS_PASTESTRUCTURE = "tools.pastestructure";
	private static final String NLS_TOOLS_MOVE_CATALOG_ENTRY = "tools.move.catalog.entry";
	
	// private stuff

	private VelocityContainer myContent;

	private final CatalogManager cm;
	private final ACService acService;
	private final RepositoryManager repositoryManager;
	private CatalogEntry currentCatalogEntry;
	private CatalogEntry newLinkNotPersistedYet;
	private int currentCatalogEntryLevel = -1;
	private List<CatalogEntry> historyStack = new ArrayList<CatalogEntry>(5);
	private List<CatalogEntry> childCe;
	private boolean isOLATAdmin;
	private boolean isAuthor;
	private boolean isLocalTreeAdmin = false;
	private int isLocalTreeAdminLevel = -1;
	private boolean canAddLinks;
	private boolean canAdministrateCategory;
	private boolean canAddSubCategories;
	private boolean canRemoveAllLinks;
	private ToolController catalogToolC;
	private RepositorySearchController rsc;
	private EntryForm addEntryForm;
	private EntryForm editEntryForm;
	private GroupController groupController;
	private DialogBoxController dialogDeleteSubtree;
	private CatalogEntry linkMarkedToBeDeleted;
	private CatalogEntry linkMarkedToBeEdited;
	private DialogBoxController dialogDeleteLink;
	private ContactFormController cfc;
	private EntryForm addStructureForm;
	private boolean isGuest;
	private Link loginLink;
	private CloseableModalController cmc;
	private Controller catEntryMoveController;
	private RepositoryEditDescriptionController repositoryEditDescriptionController;

	// locking stuff for cataloge edit operations
	private LockResult catModificationLock;
	public static final String LOCK_TOKEN = "catalogeditlock";
	
	private final MarkManager markManager;
	private final UserManager userManager;
	
	/**
	 * Init with catalog root
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootce
	 */
	public CatalogController(UserRequest ureq, WindowControl wControl) {
		// fallback translator to repository package to reduce redundant translations
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));
		
		cm = CatalogManager.getInstance();
		acService = CoreSpringFactory.getImpl(ACService.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);

		List<CatalogEntry> rootNodes = cm.getRootCatalogEntries();
		CatalogEntry rootce;
		if (rootNodes.isEmpty()) throw new AssertException("No RootNodes found for Catalog! failed module init? corrupt DB?");
		rootce = (CatalogEntry) cm.getRootCatalogEntries().get(0);

		// Check AccessRights
		isAuthor = ureq.getUserSession().getRoles().isAuthor();
		isOLATAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		revokeRightsAndSetDefaults();
		// and also if user is localTreeAdmin
		updateToolAccessRights(ureq, rootce, 0);

		myContent = createVelocityContainer("catalog");
		myContent.contextPut(CATENTRY_LEAF, new Integer(CatalogEntry.TYPE_LEAF));
		myContent.contextPut(CATENTRY_NODE, new Integer(CatalogEntry.TYPE_NODE));
		// access rights for use in the Velocity Container
		myContent.contextPut("canAddLinks", new Boolean(canAddLinks));
		myContent.contextPut("canRemoveAllLinks", new Boolean(canRemoveAllLinks));
		myContent.contextPut("isGuest", new Boolean(isGuest));
		// add icon renderer
		myContent.contextPut("iconRenderer", new RepositoryEntryIconRenderer(getLocale()));
		// add this root node as history start
		historyStack.add(rootce);
		updateContent(ureq, rootce, 0);
		
		loginLink = LinkFactory.createLink("cat.login", myContent, this);
	
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) { // links from vc -> here a link used in a
			// form action
			String command = event.getCommand();
			// FIXME:fj:c in .hbm.xml file: optimize loading (secgroup
			// outer-join=false)
			//			
			// events:
			// - subcategory selection fires a 'child' event
			// - 'navigation path' history
			// - link selectionfires a leaf event
			//
			if (command.startsWith(CATENTRY_CHILD)) { // child clicked
				int pos = Integer.parseInt(command.substring(CATENTRY_CHILD.length()));
				CatalogEntry cur = (CatalogEntry) childCe.get(pos);
				// put new as trail on stack
				historyStack.add(cur);
				updateToolAccessRights(ureq, cur, historyStack.indexOf(cur));
				updateContent(ureq, cur, historyStack.indexOf(cur));
				fireEvent(ureq, Event.CHANGED_EVENT);
				
			} else if (command.startsWith(CATCMD_HISTORY)) { // history clicked
				int pos = Integer.parseInt(command.substring(CATCMD_HISTORY.length()));
				CatalogEntry cur = historyStack.get(pos);
				historyStack = historyStack.subList(0, pos + 1);
				updateToolAccessRights(ureq, cur, historyStack.indexOf(cur));
				updateContent(ureq, cur, historyStack.indexOf(cur));
				fireEvent(ureq, Event.CHANGED_EVENT);
				
			} else if (command.startsWith(CATENTRY_LEAF)) { // link clicked
				int pos = Integer.parseInt(command.substring(CATENTRY_LEAF.length()));
				CatalogEntry cur = (CatalogEntry) childCe.get(pos);
				RepositoryEntry repoEntry = cur.getRepositoryEntry();
				if (repoEntry == null) throw new AssertException("a leaf did not have a repositoryentry! catalogEntry = key:" + cur.getKey()
						+ ", title " + cur.getName());
				// launch entry if launchable, otherwise offer it as download / launch
				// it as non-html in browser
				if (repoEntry.getCanLaunch()) {
					String businessPath = "[RepositoryEntry:" + repoEntry.getKey() + "]";
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				} else if (repoEntry.getCanDownload()) {
					OLATResource ores = repoEntry.getOlatResource();
					if (ores == null) {
						throw new AssertException("repoEntry had no olatresource, repoKey = " + repoEntry.getKey());
					}
					// else not launchable in olat, but downloadable -> send the document
					// directly to browser but "downloadable" (pdf, word, excel)
					RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repoEntry);
					MediaResource mr = handler.getAsMediaResource(ores, false);
					RepositoryManager.getInstance().incrementDownloadCounter(repoEntry);
					ureq.getDispatchResult().setResultingMediaResource(mr);
					return;
				} else { // neither launchable nor downloadable -> show details					
					//REVIEW:pb:replace EntryChangedEvent with a more specific event
					fireEvent(ureq, new EntryChangedEvent(repoEntry, EntryChangedEvent.MODIFIED));
					return;
				}

			} else if (command.startsWith(CATCMD_MOVE)) {
				String s = command.substring(CATCMD_MOVE.length());
				if (s.startsWith(CATENTRY_LEAF)) {
					// move a resource in the catalog - moving of catalog leves is triggered by a toolbox action
					int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
					linkMarkedToBeEdited = (CatalogEntry) childCe.get(pos);
					
					removeAsListenerAndDispose(cmc);
					removeAsListenerAndDispose(catEntryMoveController);
					catEntryMoveController= new CatalogEntryMoveController(getWindowControl(), ureq, linkMarkedToBeEdited, getTranslator());
					listenTo(catEntryMoveController);
					cmc = new CloseableModalController(getWindowControl(), "close", catEntryMoveController.getInitialComponent());
					listenTo(cmc);
					cmc.activate();
				}
			} else if (command.startsWith(CATCMD_REMOVE)) {
				String s = command.substring(CATCMD_REMOVE.length());
				if (s.startsWith(CATENTRY_LEAF)) {
					int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
					linkMarkedToBeDeleted = (CatalogEntry) childCe.get(pos);
					// create modal dialog
					String[] trnslP = { linkMarkedToBeDeleted.getName() };
					dialogDeleteLink = activateYesNoDialog(ureq, null, getTranslator().translate(NLS_DIALOG_MODAL_LEAF_DELETE_TEXT, trnslP), dialogDeleteLink);
					return;
				}
			} else if (command.startsWith(CATCMD_EDIT)) {
				String s = command.substring(CATCMD_EDIT.length());
				if (s.startsWith(CATENTRY_LEAF)) {
					int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
					linkMarkedToBeEdited = (CatalogEntry) childCe.get(pos);
					repositoryEditDescriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), linkMarkedToBeEdited.getRepositoryEntry(), false);
					repositoryEditDescriptionController.addControllerListener(this);
					// open form in dialog
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), "close", repositoryEditDescriptionController.getInitialComponent(), true, translate("tools.edit.catalog.category"));
					listenTo(cmc);
					cmc.activate();					
				}
			} else if (command.startsWith(CATCMD_DETAIL)) {
				String s = command.substring(CATCMD_DETAIL.length());
				if (s.startsWith(CATENTRY_LEAF)) {
					int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
					CatalogEntry showDetailForLink = (CatalogEntry) childCe.get(pos);
					RepositoryEntry repoEnt = showDetailForLink.getRepositoryEntry();
					if(repoEnt == null) {//concurrent edition, reload the current listing
						updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
					} else {
						fireEvent(ureq, new EntryChangedEvent(repoEnt, EntryChangedEvent.MODIFIED));
						//fxdiff BAKS-7 Resume function
						OLATResourceable ceRes = OresHelper.createOLATResourceableInstance(CatalogEntry.class.getSimpleName(), showDetailForLink.getKey());
						WindowControl bwControl = addToHistory(ureq, ceRes, null);
						OLATResourceable ores = OresHelper.createOLATResourceableInstance("details", 0l);
						addToHistory(ureq, ores, null, bwControl, true);
					}
				}
			}
		}
		/*
		 * login link clicked
		 */		
		else if (source == loginLink){
			DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
		}
		
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {

		/*
		 * events from the catalogToolC
		 */
		if (source == catalogToolC) {
			/*
			 * add new subcategory to the currentCategory
			 */
			if (event.getCommand().equals(ACTION_ADD_CTLGCATEGORY)) {
				catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
				if ( ! catModificationLock.isSuccess()) {
					String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
					showError("catalog.locked.by", ownerName);
					return;
				}
				removeAsListenerAndDispose(addEntryForm);
				addEntryForm = new EntryForm(ureq, getWindowControl(), false);
				addEntryForm.setElementCssClass("o_sel_catalog_add_category_popup");
				listenTo(addEntryForm);
				
				// open form in dialog
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", addEntryForm.getInitialComponent(), true, translate("tools.add.catalog.category"));
				listenTo(cmc);
				cmc.activate();					
			}
			/*
			 * add a link to the currentCategory
			 */
			else if (event.getCommand().equals(ACTION_ADD_CTLGLINK)) {
				removeAsListenerAndDispose(rsc);
				rsc = new RepositorySearchController(translate(NLS_CHOOSE), ureq, getWindowControl(), true, false, false);
				listenTo(rsc);
				// OLAT-Admin has search form
				if (isOLATAdmin) {
					rsc.displaySearchForm();
				}
				// an Author gets the list of his repository
				else {
					// admin is responsible for not inserting wrong visibility entries!!
					rsc.doSearchByOwnerLimitAccess(ureq.getIdentity());
				}
				// open form in dialog
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", rsc.getInitialComponent(), true, translate("tools.add.catalog.link"));
				listenTo(cmc);
				cmc.activate();					
			}
			/*
			 * edit the currentCategory
			 */
			else if (event.getCommand().equals(ACTION_EDIT_CTLGCATEGORY)) {
				catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
				if ( ! catModificationLock.isSuccess()) {
					String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
					showError("catalog.locked.by", ownerName);
					return;
				}
				removeAsListenerAndDispose(editEntryForm);
				editEntryForm = new EntryForm(ureq, getWindowControl(), false);
				editEntryForm.setElementCssClass("o_sel_catalog_edit_category_popup");
				listenTo(editEntryForm);
				
				editEntryForm.setFormFields(currentCatalogEntry);// fill the
				
				// open form in dialog
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", editEntryForm.getInitialComponent(), true, translate("tools.edit.catalog.category"));
				listenTo(cmc);
				
				cmc.activate();				
			}
			/*
			 * edit current category's ownergroup
			 */
			else if (event.getCommand().equals(ACTION_EDIT_CTLGCATOWNER)) {
				// add ownership management
				SecurityGroup secGroup = currentCatalogEntry.getOwnerGroup();
				if (secGroup == null) {
					CatalogEntry reloaded = cm.loadCatalogEntry(currentCatalogEntry);
					currentCatalogEntry = reloaded;// FIXME:pb:?
					secGroup = BaseSecurityManager.getInstance().createAndPersistSecurityGroup();
					currentCatalogEntry.setOwnerGroup(secGroup);
					cm.saveCatalogEntry(currentCatalogEntry);
				}
				boolean keepAtLeastOne = currentCatalogEntryLevel == 0;
				
				removeAsListenerAndDispose(groupController);
				groupController = new GroupController(ureq, getWindowControl(), true, keepAtLeastOne, false, false, false, false, secGroup);
				listenTo(groupController);
				
				// open form in dialog
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", groupController.getInitialComponent(), true, translate("tools.edit.catalog.category.ownergroup"));
				listenTo(cmc);
				
				cmc.activate();					
			}
			/*
			 * delete category (subtree)
			 */
			else if (event.getCommand().equals(ACTION_DELETE_CTLGCATEGORY)) {
				catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
				if ( ! catModificationLock.isSuccess()) {
					String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
					showError("catalog.locked.by", ownerName);
					return;
				}
				String[] trnslP = { currentCatalogEntry.getName() };
				dialogDeleteSubtree = activateYesNoDialog(ureq, null, getTranslator().translate(NLS_DIALOG_MODAL_SUBTREE_DELETE_TEXT, trnslP), dialogDeleteSubtree);
				return;
			}
			/*
			 * contact caretaker, request subcategory, request deletion of an entry,
			 * etc.
			 */
			else if (event.getCommand().equals(ACTION_NEW_CTGREQUEST)) {
				/*
				 * find the first caretaker, looking from the leaf towards the root,
				 * following the selected branch.
				 */
				BaseSecurity mngr = BaseSecurityManager.getInstance();
				ContactList caretaker = new ContactList(translate(NLS_CONTACT_TO_GROUPNAME_CARETAKER));
				final List<Identity> emptyList = new ArrayList<Identity>();
				List<Identity> tmpIdent = new ArrayList<Identity>();
				for (int i = historyStack.size() - 1; i >= 0 && tmpIdent.isEmpty(); i--) {
					// start at the selected category, the root category is asserted to
					// have the OLATAdministrator
					// so we end up having always at least one identity as receiver for a
					// request ;-)
					CatalogEntry tmp = historyStack.get(i);
					SecurityGroup tmpOwn = tmp.getOwnerGroup();
					if (tmpOwn != null) tmpIdent = mngr.getIdentitiesOfSecurityGroup(tmpOwn);
					else tmpIdent = emptyList;
				}
				for (int i = tmpIdent.size() - 1; i >= 0; i--) {
					caretaker.add((Identity) tmpIdent.get(i));
				}
				
				//create e-mail Message
				ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
				cmsg.addEmailTo(caretaker);
				
				removeAsListenerAndDispose(cfc);
				cfc = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
				listenTo(cfc);
				
				// open form in dialog
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", cfc.getInitialComponent(), true, translate("contact.caretaker"));
				listenTo(cmc);
				
				cmc.activate();					
			}
			/*
			 * add a structure
			 */
			else if (event.getCommand().equals(ACTION_ADD_STRUCTURE)) {
				removeAsListenerAndDispose(addStructureForm);
				addStructureForm = new EntryForm(ureq, getWindowControl(), false);
				addStructureForm.setElementCssClass("o_sel_catalog_add_root_category_popup");
				listenTo(addStructureForm);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), "close", addStructureForm.getInitialComponent(), true, translate("contact.caretaker"));
				listenTo(cmc);
				
				cmc.activate();					
			} else if (ACTION_ADD_BOOKMARK.equals(event.getCommand())) {
				String businessPath = "[CatalogEntry:" + currentCatalogEntry.getKey() + "]";
				OLATResourceable ores = cm.createOLATResouceableFor(currentCatalogEntry);
				markManager.setMark(ores, getIdentity(), null, businessPath);
			}
			/*
			 * move catalogentry
			 */
			else if(event.getCommand().equals(ACTION_MOVE_ENTRY)){		
				// Move catalog level - moving of resources in the catalog (leafs) is triggered by a velocity command
				// so, reset stale link to the current resource first (OLAT-4253), the linkMarkedToBeEdited will be reset
				// when an edit or move operation on the resource is done 
				linkMarkedToBeEdited = null; 
				//
				catModificationLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
				if (!catModificationLock.isSuccess()) {
					String ownerName = userManager.getUserDisplayName(catModificationLock.getOwner());
					showError("catalog.locked.by", ownerName);
					return;
				}
				
				removeAsListenerAndDispose(cmc);
				removeAsListenerAndDispose(catEntryMoveController);
				catEntryMoveController= new CatalogEntryMoveController(getWindowControl(), ureq, currentCatalogEntry, getTranslator());					
				listenTo(catEntryMoveController);
				cmc = new CloseableModalController(getWindowControl(), "close", catEntryMoveController.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			} else if (source == addStructureForm) {
				// remove modal dialog first
				cmc.deactivate();
				if (event == Event.DONE_EVENT) {
					importStructure();
				}
				CatalogEntry newRoot = (CatalogEntry) cm.getRootCatalogEntries().get(0);
				historyStack = new ArrayList<CatalogEntry>();
				historyStack.add(newRoot);
				updateContent(ureq, newRoot, 0);
				updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		/*
		 * from the repository search, a entry was selected to add
		 */
		else if (source == rsc) {
			// remove modal dialog
			cmc.deactivate();
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				/*
				 * succesfully selected a repository entry which will be a link within
				 * the current Category
				 */
				RepositoryEntry re = rsc.getSelectedEntry();
				/*
				 * create, but do not persist a new catalog entry
				 */
				newLinkNotPersistedYet = cm.createCatalogEntry();
				newLinkNotPersistedYet.setName(re.getDisplayname());
				newLinkNotPersistedYet.setDescription(re.getDescription());
				newLinkNotPersistedYet.setRepositoryEntry(re);
				newLinkNotPersistedYet.setType(CatalogEntry.TYPE_LEAF);
				/*
				 * open the confirm form, which allows to change the link-title,
				 * link-description.
				 */
				newLinkNotPersistedYet.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
				cm.addCatalogEntry(currentCatalogEntry, newLinkNotPersistedYet);
				newLinkNotPersistedYet = null;
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				fireEvent(ureq, Event.CHANGED_EVENT);

			}
		}
		/*
		 * from remove subtree dialog -> yes or no
		 */
		else if (source == dialogDeleteSubtree) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// remember the parent of the subtree being deleted
				CatalogEntry parent = currentCatalogEntry.getParent();
				// delete the subtree!!!
				cm.deleteCatalogEntry(currentCatalogEntry);
				// display the parent
				historyStack.remove(historyStack.size() - 1);
				updateContent(ureq, parent, historyStack.indexOf(parent));
				updateToolAccessRights(ureq, parent, historyStack.indexOf(parent));
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}

		}
		/*
		 * from remove link dialog -> yes or no
		 */
		else if (source == dialogDeleteLink) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				cm.deleteCatalogEntry(linkMarkedToBeDeleted);
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			}
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
		}
		/*
		 * from contactform controller, aka sending e-mail to caretaker
		 */
		else if (source == cfc) {
			// remove modal dialog
			cmc.deactivate();
			if (event.equals(Event.DONE_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			}
		} else if (source == groupController) {
			// remove modal dialog
			cmc.deactivate();
			if(event instanceof IdentitiesAddEvent ) { //FIXME:chg: Move into seperate RepositoryOwnerGroupController like BusinessGroupEditController ?
				IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
				List<Identity> list = identitiesAddedEvent.getAddIdentities();
				BaseSecurity securityManager = BaseSecurityManager.getInstance();
        for (Identity identity : list) {
        	if (!securityManager.isIdentityInSecurityGroup(identity, currentCatalogEntry.getOwnerGroup())) {
        		securityManager.addIdentityToSecurityGroup(identity, currentCatalogEntry.getOwnerGroup());
        		identitiesAddedEvent.getAddedIdentities().add(identity);
        	}
        }
			} else if (event instanceof IdentitiesRemoveEvent) {
				IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
				List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
        for (Identity identity : list) {
        	BaseSecurityManager.getInstance().removeIdentityFromSecurityGroup(identity, currentCatalogEntry.getOwnerGroup());
        }		
			}
		} else if(source == catEntryMoveController){
			cmc.deactivate();
			if(event.equals(Event.DONE_EVENT)){
				//linkMarkedToBeEdited is the catalog entry - "leaf" - which is moved
				showInfo("tools.move.catalog.entry.success", (linkMarkedToBeEdited == null ? currentCatalogEntry.getName() : linkMarkedToBeEdited.getName()));
				//currentCatalogEntry is the current active "Folder" - reload model to reflect change.
				reloadHistoryStack(ureq, currentCatalogEntry.getKey());
			} else if(event.equals(Event.FAILED_EVENT)){
				showError("tools.move.catalog.entry.failed");
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			}
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
		} 
		
		else if (source == cmc) {
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
		}
		else if (source == repositoryEditDescriptionController) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				linkMarkedToBeEdited.setRepositoryEntry(repositoryEditDescriptionController.getRepositoryEntry());
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
				cm.updateReferencedRepositoryEntry(repositoryEditDescriptionController.getRepositoryEntry());
			}
			cmc.deactivate();
		} else if (source == addEntryForm) {
			// remove modal dialog
			cmc.deactivate();
			
			if (event == Event.DONE_EVENT) {
				CatalogEntry ce = cm.createCatalogEntry();
				addEntryForm.fillEntry(ce);
				ce.setOwnerGroup(BaseSecurityManager.getInstance().createAndPersistSecurityGroup());
				ce.setRepositoryEntry(null);
				ce.setParent(currentCatalogEntry);
				// optimistic save: might fail in case the parent has been deleted in the meantime
				cm.saveCatalogEntry(ce);
			} else if (event == Event.CANCELLED_EVENT) {
				// nothing to do
			}
			CatalogEntry reloaded = cm.loadCatalogEntry(currentCatalogEntry);
			currentCatalogEntry = reloaded;// FIXME:pb:
			updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			// in any case, remove the lock
			if (catModificationLock != null && catModificationLock.isSuccess()) {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
				catModificationLock = null;
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
			
		} else if (source == editEntryForm) {
				// remove modal dialog
				cmc.deactivate();
				// optimistic save: might fail in case the current entry has been deleted
				// in the meantime by someone else
				CatalogEntry reloaded = (CatalogEntry) DBFactory.getInstance().loadObject(currentCatalogEntry);
				currentCatalogEntry = reloaded;// FIXME:pb
				if (event == Event.DONE_EVENT) {
					editEntryForm.fillEntry(currentCatalogEntry);
					cm.updateCatalogEntry(currentCatalogEntry);
					// update the changed name in the history path
					historyStack.remove(historyStack.size() - 1);
					historyStack.add(currentCatalogEntry);
				} else if (event == Event.CANCELLED_EVENT) {
					// nothing to do
				}
				// in any case, remove the lock
				if (catModificationLock != null && catModificationLock.isSuccess()) {
					CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
					catModificationLock = null;
				}
				updateContent(ureq, currentCatalogEntry, currentCatalogEntryLevel);
			}
	}

	/**
	 * before calling this method make sure the person has the right to add
	 * categories. The method checks further if the person also can add links as
	 * author.
	 * 
	 * @return configured tool controller
	 */
	public ToolController createCatalogToolController() {
		removeAsListenerAndDispose(catalogToolC);
		catalogToolC = ToolFactory.createToolController(getWindowControl());
		listenTo(catalogToolC);
		//if (isOLATAdmin || isLocalTreeAdmin || isAuthor ) {
		// at least a person being able to do something...
		if (!isGuest) {
			// included normal user now for bookmark functionality
			/*
			 * edit tools
			 */
			catalogToolC.addHeader(getTranslator().translate("tools.edit.header"));			
			if (canAdministrateCategory || canAddLinks) {
				if (canAdministrateCategory) {
					catalogToolC.addLink(ACTION_EDIT_CTLGCATEGORY, translate(NLS_TOOLS_EDIT_CATALOG_CATEGORY), null, null, "o_sel_catalog_edit_category", false);
				}
				if (canAdministrateCategory) {
					catalogToolC.addLink(ACTION_EDIT_CTLGCATOWNER, translate(NLS_TOOLS_EDIT_CATALOG_CATEGORY_OWNERGROUP), null, null, "o_sel_catalog_category_owner", false);
				}
				if (canAddLinks) {
					catalogToolC.addLink(ACTION_NEW_CTGREQUEST, translate(NLS_TOOLS_NEW_CATALOG_CATEGORYREQUEST), null, null, "o_sel_catalog_contact_owner", false);
				}
				if (canAdministrateCategory && currentCatalogEntryLevel > 0) {
					// delete root? very dangerous, disabled!
					catalogToolC.addLink(ACTION_DELETE_CTLGCATEGORY, translate(NLS_TOOLS_DELETE_CATALOG_ENTRY), null, null, "o_sel_catalog_delete_category", false);
				}
				if (canAdministrateCategory && currentCatalogEntryLevel > 0) {
					catalogToolC.addLink(ACTION_MOVE_ENTRY, translate(NLS_TOOLS_MOVE_CATALOG_ENTRY), null, null, "o_sel_catalog_move_category", false);
				}
			}

			/*
			 * add tools
			 */
			if(isOLATAdmin || isLocalTreeAdmin || isAuthor){
					if (canAddSubCategories || canAddLinks) catalogToolC.addHeader(translate(NLS_TOOLS_ADD_HEADER));
					if (canAddSubCategories) {
						catalogToolC.addLink(ACTION_ADD_CTLGCATEGORY, translate(NLS_TOOLS_ADD_CATALOG_CATEGORY), null, null, "o_sel_catalog_add_category", false);
					}
					if (canAddLinks) {
						catalogToolC.addLink(ACTION_ADD_CTLGLINK, translate(NLS_TOOLS_ADD_CATALOG_LINK), null, null, "o_sel_catalog_add_link_to_resource", false);
					}
					if (currentCatalogEntryLevel == 0 && isOLATAdmin && cm.getChildrenOf(currentCatalogEntry).isEmpty()) {
						catalogToolC.addLink(ACTION_ADD_STRUCTURE, translate(NLS_TOOLS_PASTESTRUCTURE), null, null, "o_sel_catalog_add_root_category", false);
					}
			}	
		}
		return catalogToolC;
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
		// controllers autodisposed by basic controller
	}

	/**
	 * refresh content of current category for displaying
	 * 
	 * @param ce
	 * @param ceLevel
	 */
	//fxdiff BAKS-7 Resume function
	private void updateContent(UserRequest ureq, CatalogEntry ce, int ceLevel) {
		Identity identity = ureq.getIdentity();
		/*
		 * FIXME:pb:c include lookahead feature, displaying the 1st 3 children if
		 * any, to give a google directory feeling
		 */
		currentCatalogEntry = ce;
		currentCatalogEntryLevel = ceLevel;
		myContent.contextPut("canAddLinks", new Boolean(canAddLinks));
		myContent.contextPut("canRemoveAllLinks", new Boolean(canRemoveAllLinks));
		myContent.contextPut("currentCatalogEntry", currentCatalogEntry);
		childCe = cm.getChildrenOf(ce);
		// Sort to fix ordering by repo entry display name. For leafs the displayed
		// name is not the catalog entry name but the repo entry display name. The
		// SQL query orders by catalog entry name, thus the visual ordering is
		// wrong.
		// fxdiff: FXOLAT-100
		Collections.sort(childCe, new Comparator<CatalogEntry>() {
			@Override
			public int compare(final CatalogEntry c1, final CatalogEntry c2) {
				String c1Title, c2Title;
				if (c1.getType() == CatalogEntry.TYPE_LEAF) {
					final RepositoryEntry repoEntry = c1.getRepositoryEntry();
					if (repoEntry != null) {
						c1Title = repoEntry.getDisplayname();
					} else {
						c1Title = c1.getName();
					}
				} else {
					c1Title = c1.getName();
				}
				if (c2.getType() == CatalogEntry.TYPE_LEAF) {
					final RepositoryEntry repoEntry = c2.getRepositoryEntry();
					if (repoEntry != null) {
						c2Title = repoEntry.getDisplayname();
					} else {
						c2Title = c2.getName();
					}
				} else {
					c2Title = c2.getName();
				}
				// Sort now based on users locale
				final Collator myCollator = Collator.getInstance(getLocale());
				return myCollator.compare(c1Title, c2Title);
			}
		});
	
		myContent.contextPut("children", childCe);
		//fxdiff VCRP-1,2: access control of resources
		List<Long> resourceKeys = new ArrayList<Long>();
		List<Long> repositoryEntryKeys = new ArrayList<Long>();
		for ( CatalogEntry entry : childCe ) {
			if(entry.getRepositoryEntry() != null && entry.getRepositoryEntry().getOlatResource() != null) {
				resourceKeys.add(entry.getRepositoryEntry().getOlatResource().getKey());
				repositoryEntryKeys.add(entry.getRepositoryEntry().getKey());
			}
		}
		
		if(!repositoryEntryKeys.isEmpty()) {
			SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setIdentity(getIdentity());
			params.setRoles(ureq.getUserSession().getRoles());
			params.setRepositoryEntryKeys(repositoryEntryKeys);
			List<RepositoryEntry> allowedEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
			for (Iterator<CatalogEntry> itEntry = childCe.iterator(); itEntry.hasNext(); ) {
				CatalogEntry entry = itEntry.next();
				if(entry.getRepositoryEntry() != null && !allowedEntries.contains(entry.getRepositoryEntry())) {
					itEntry.remove();
				}
			}
		}
		
		List<OLATResourceAccess> resourcesWithOffer = acService.getAccessMethodForResources(resourceKeys, null, true, new Date());
		for ( CatalogEntry entry : childCe ) {
			if(entry.getType() == CatalogEntry.TYPE_NODE) continue;
			//fxdiff VCRP-1,2: access control of resources
			if(entry.getRepositoryEntry() != null && entry.getRepositoryEntry().getOlatResource() != null) {
				List<PriceMethod> types = new ArrayList<PriceMethod>();
				if (entry.getRepositoryEntry().isMembersOnly()) {
					// members only always show lock icon
					types.add(new PriceMethod("", "b_access_membersonly_icon"));
				} else {
					// collect access control method icons
					OLATResource resource = entry.getRepositoryEntry().getOlatResource();
					for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
						if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
							for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
								String type = bundle.getMethod().getMethodCssClass() + "_icon";
								String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
								types.add(new PriceMethod(price, type));
							}
						}
					}
				}
				
				//fxdiff VCRP-1,2: access control of resources
				String acName = "ac_" + childCe.indexOf(entry);
				if(!types.isEmpty()) {
					myContent.contextPut(acName, types);
				} else {
					myContent.contextRemove(acName);
				}
			}
			
			VFSLeaf image = repositoryManager.getImage(entry.getRepositoryEntry());
			String name = "image" + childCe.indexOf(entry);
			if(image == null) {
				myContent.remove(myContent.getComponent(name));
			} else {
				ImageComponent ic = new ImageComponent(name);
				ic.setMediaResource(new VFSMediaResource(image));
				ic.setMaxWithAndHeightToFitWithin(200, 100);
				myContent.put(name, ic);
			}
		}
		myContent.contextPut(CATCMD_HISTORY, historyStack);

		String url = Settings.getServerContextPathURI() + "/url/CatalogEntry/" + ce.getKey();
		myContent.contextPut("guestExtLink", url + "?guest=true&amp;lang=" + getLocale().getLanguage());
		if ( ! isGuest) {
			myContent.contextPut("extLink", url);
		}
		// check which of the entries are owned entries. users who can add links
		// can also remove links. users who can remove all links do not need to be
		// checked
		if (canAddLinks && !canRemoveAllLinks) {
			List<CatalogEntry> ownedLinks = cm.filterOwnedLeafs(identity, childCe);
			if (ownedLinks.size() > 0) {
				myContent.contextPut("hasOwnedLinks", Boolean.TRUE);
				myContent.contextPut("ownedLinks", ownedLinks);
			} else myContent.contextPut("hasOwnedLinks", Boolean.FALSE);

		} else myContent.contextPut("hasOwnedLinks", Boolean.FALSE);
		//fxdiff BAKS-7 Resume function
		updateHistory(ureq);
	}

	//fxdiff BAKS-7 Resume function
	public void updateHistory(UserRequest ureq) {
		if(currentCatalogEntry != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CatalogEntry.class, currentCatalogEntry.getKey());
			addToHistory(ureq, ores, null);
		} else {
			addToHistory(ureq);
		}
	}

	/**
	 * Helper to imports simple tree structure, for simplicity
	 */
	private void importStructure() {
		CatalogEntry oldRoot = (CatalogEntry) cm.getRootCatalogEntries().get(0);
		SecurityGroup rootOwners = oldRoot.getOwnerGroup();
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		List<Identity> olatAdminIdents = secMgr.getIdentitiesOfSecurityGroup(rootOwners);
		SecurityGroup catalogAdmins = secMgr.createAndPersistSecurityGroup();
		for (int i = 0; i < olatAdminIdents.size(); i++) {
			secMgr.addIdentityToSecurityGroup(olatAdminIdents.get(i), catalogAdmins);
		} 
		cm.deleteCatalogEntry(oldRoot);

		CatalogEntry dummy = cm.createCatalogEntry();
		addStructureForm.fillEntry(dummy);
		String structure = dummy.getDescription();
		String[] lines = structure.split("\n");
		Stack<CatalogEntry> treeStack = new Stack<CatalogEntry>();
		//
		CatalogEntry newRoot = cm.createCatalogEntry();
		newRoot.setParent(null);
		newRoot.setType(CatalogEntry.TYPE_NODE);
		newRoot.setDescription("fill it");
		newRoot.setName(lines[0]);
		newRoot.setOwnerGroup(catalogAdmins);
		cm.saveCatalogEntry(newRoot);
		treeStack.push(newRoot);
		for (int i = 1; i < lines.length; i++) {
			int level = 0;
			int pos = 0;
			while ("".equals(lines[i].substring(pos, pos + 2).trim())) {
				level++;
				pos += 3;
			}
			CatalogEntry tmp = cm.createCatalogEntry();
			tmp.setType(CatalogEntry.TYPE_NODE);
			tmp.setDescription("fill it");
			tmp.setName(lines[i].trim());
			if (treeStack.size() == level) {
				tmp.setParent(treeStack.lastElement());
				treeStack.push(tmp);
			} else if (treeStack.size() > level) {
				// moving towards root
				for (int ii = treeStack.size() - 1; ii >= level; ii--) {
					treeStack.pop();
				}
				tmp.setParent(treeStack.lastElement());
				treeStack.push(tmp);
			}
			cm.saveCatalogEntry(tmp);
		}
	}

	/**
	 * Internal helper to calculate the users rights within the controller. The
	 * method will fire change events if necessary to signal the parent controller
	 * that he need to rebuild the tool controller
	 * 
	 * @param ureq
	 * @param ce The current catalog category element from the given level
	 * @param pos The current level in the catalog
	 */
	private void updateToolAccessRights(UserRequest ureq, CatalogEntry ce, int pos) {
		// 1) check if user has already a bookmark for this level

		// 2) check if insert structure must be removed or showed 
		if (isOLATAdmin && currentCatalogEntryLevel == 0) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		if (isOLATAdmin) return;

		// 3) check other cases that might change default values from constructor
		if (isLocalTreeAdminLevel > pos) {
			// 3a) below branch that user has admin rights - revoke all rights
			isLocalTreeAdminLevel = -1;
			isLocalTreeAdmin = false;
			revokeRightsAndSetDefaults();
			fireEvent(ureq, Event.CHANGED_EVENT);
			
		} else if (isLocalTreeAdminLevel == -1) {
			// 3b) check if user is admin for this level
			SecurityGroup owners = ce.getOwnerGroup();
			boolean isInGroup = false;
			if (owners != null) {
				isInGroup = BaseSecurityManager.getInstance().isIdentityInSecurityGroup(ureq.getIdentity(), owners);
			}
			if (isInGroup) {
				isLocalTreeAdminLevel = pos;
				isLocalTreeAdmin = true;
				canAddLinks = true;
				canAdministrateCategory = true;
				canAddSubCategories = true;
				canRemoveAllLinks = true;
				fireEvent(ureq, Event.CHANGED_EVENT);				
			} else {
				isLocalTreeAdmin = false;
				revokeRightsAndSetDefaults();
			}
		}
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		ContextEntry catCe = entries.remove(0);
		Long catId = catCe.getOLATResourceable().getResourceableId();
		if(catId == null || catId.longValue() == 0l) return;//nothing to do
		CatalogEntry ce = CatalogManager.getInstance().loadCatalogEntry(catId);
		if(ce == null) return;//catalog entry not found, do nothing
		switch(ce.getType()) {
			case CatalogEntry.TYPE_NODE: {
				reloadHistoryStack(ureq, catId);
				break;
			}
			case CatalogEntry.TYPE_LEAF: {
				Long folderId = ce.getParent().getKey();
				reloadHistoryStack(ureq, folderId);
				if(!entries.isEmpty()) {
					ContextEntry subEntry = entries.remove(0);
					String subType = subEntry.getOLATResourceable().getResourceableTypeName();
					if("details".equals(subType)) {
						event(ureq, myContent, new Event(CATCMD_DETAIL + CATENTRY_LEAF + "0"));
					}
				}
				break;
			}
		}
	}
	
	// fxdiff: FXOLAT-71 do the right checks correctly
	private void revokeRightsAndSetDefaults(){
		canAddLinks = isOLATAdmin || isAuthor; // author is allowed to add!
		canAdministrateCategory = isOLATAdmin;
		canAddSubCategories = isOLATAdmin || isLocalTreeAdmin;
		canRemoveAllLinks = isOLATAdmin || isLocalTreeAdmin;
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest, java.lang.String)
	 *//*
	public void activate(UserRequest ureq, String viewIdentifier){
		 // transforms the parameter jumpToNode into a long value and calls jumpToNode(UserRequest, long)
		try{
			long parsed=Long.parseLong(viewIdentifier);
			reloadHistoryStack(ureq, parsed);
		} catch(Exception e){
			logWarn("Could not activate catalog entry with ID::" + viewIdentifier, null);
		}
	}*/
	
	/**
	 * Internal helper: Get's the requested catalog node and set it as active
	 * content, builds also the history stack from the root-node to this node.
	 * 
	 * @return true if successful otherwise false (e.c. jumpToNode referenced a
	 *         catalog leaf or no catalog entry at all)
	 */
	private boolean jumpToNode(UserRequest ureq, long jumpToNode){
		CatalogEntry cE=CatalogManager.getInstance().loadCatalogEntry(Long.valueOf(jumpToNode));
			if(cE!=null){
					Stack<CatalogEntry> stack=new Stack<CatalogEntry>();
					// get elements, and add to filo stack
					while(cE !=null){
						stack.push(cE);
						cE=cE.getParent();
					}
					// read filo stack
					while ( !stack.isEmpty())
					{
						cE = stack.pop();
						historyStack.add(cE);
						//fxdiff BAKS-7 Resume function
						updateContent(ureq, cE, historyStack.size()-1);					
						updateToolAccessRights(ureq, cE, historyStack.size()-1);
					}
					return true;
			}
			return false;
	}

	/**
	 * Internal helper: clear history and jumpt to the given node
	 * @param ureq
	 * @param jumpToNode
	 */
	private void reloadHistoryStack(UserRequest ureq, long jumpToNode){
		historyStack.clear();
		jumpToNode(ureq, jumpToNode);
	}
	
	public static class PriceMethod {
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