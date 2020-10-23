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

package org.olat.repository.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroupModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.repository.ui.author.CreateEntryController;
import org.olat.repository.ui.author.ImportRepositoryEntryController;
import org.olat.repository.ui.author.ImportURLRepositoryEntryController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description: Implements a repository entry search workflow
 *         used by OLAT authors. Entries can be found that are either owned by
 *         the user or visible to authors in conjunction with the canReference
 *         flag.
 * @author gnaegi        
 * Initial Date: Aug 17, 2006
 * 
 */
public class ReferencableEntriesSearchController extends BasicController {
	
	public static final Event EVENT_REPOSITORY_ENTRY_SELECTED = new Event("event.repository.entry.selected");
	public static final Event EVENT_REPOSITORY_ENTRIES_SELECTED = new Event("event.repository.entries.selected");

	private static final String CMD_SEARCH_ENTRIES = "cmd.searchEntries";
	private static final String CMD_ADMIN_SEARCH_ENTRIES = "cmd.adminSearchEntries";
	private static final String CMD_ALL_ENTRIES = "cmd.allEntries";
	private static final String CMD_MY_ENTRIES = "cmd.myEntries";

	private VelocityContainer mainVC;
	private RepositorySearchController searchCtr;
	private String[] limitTypes;

	private SegmentViewComponent segmentView;
	private Link myEntriesLink;
	private Link allEntriesLink;
	private Link searchEntriesLink;
	private Link adminEntriesLink;
	private Link importRessourceButton;
	private Link importRessourceUrlButton;
	private Component createRessourceCmp;
	
	private CloseableModalController cmc;
	private CreateEntryController createController;
	private ImportRepositoryEntryController importController;
	private ImportURLRepositoryEntryController importUrlController;
	
	private RepositoryEntry selectedRepositoryEntry;
	private List<RepositoryEntry> selectedRepositoryEntries;
	
	private final Can canBe;
	private final boolean canImport;
	
	private Object userObject;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	public ReferencableEntriesSearchController(WindowControl wControl, UserRequest ureq, String limitType, String commandLabel) {
		this(wControl, ureq, new String[]{ limitType }, null, null, commandLabel, true, true, false, false, false, Can.referenceable);
	}
	
	public ReferencableEntriesSearchController(WindowControl wControl, UserRequest ureq, String[] limitTypes, String commandLabel) {
		this(wControl, ureq, limitTypes, null, null, commandLabel, true, true, false, false, false, Can.referenceable);
	}
	
	public ReferencableEntriesSearchController(WindowControl wControl, UserRequest ureq, String[] limitTypes, String commandLabel,
			boolean canImport, boolean canCreate, boolean multiSelect, boolean organisationWildCard, boolean adminSearch) {
		this(wControl, ureq, limitTypes, null, null, commandLabel, canImport, canCreate, multiSelect, organisationWildCard, adminSearch, Can.referenceable);
	}

	public ReferencableEntriesSearchController(WindowControl wControl, UserRequest ureq,
			String[] limitTypes, RepositoryEntryFilter filter, IdentityRef asParticipant, String commandLabel,
			boolean canImport, boolean canCreate, boolean multiSelect, boolean organisationWildCard, boolean adminSearch, 
			Can canBe) {

		super(ureq, wControl);
		this.canBe = canBe;
		this.canImport = canImport;
		this.limitTypes = limitTypes;
		setBasePackage(RepositoryService.class);
		mainVC = createVelocityContainer("referencableSearch");
		
		if(limitTypes != null && limitTypes.length == 1 && limitTypes[0] != null) {
			mainVC.contextPut("titleCss", "o_icon o_" + limitTypes[0] + "_icon");
		}

		// add repo search controller
		searchCtr = new RepositorySearchController(commandLabel, ureq, getWindowControl(),
				false, multiSelect, limitTypes, organisationWildCard, filter, asParticipant);
		listenTo(searchCtr);
		
		// do instantiate buttons
		if (canCreate && limitTypes != null) {
			List<String> creatorTypes = new ArrayList<>(limitTypes.length);
			for(String limitType:limitTypes) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(limitType);
				if(handler.supportCreate(getIdentity(), ureq.getUserSession().getRoles())) {
					creatorTypes.add(limitType);
				}
			}
			
			if(creatorTypes.size() == 1) {
				Link createButton = LinkFactory.createButtonSmall("cmd.create.ressource", mainVC, this);
				createButton.setElementCssClass("o_sel_repo_popup_create_resource");
				createButton.setSuppressDirtyFormWarning(true);
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(creatorTypes.get(0));
				createButton.setUserObject(handler);
				createRessourceCmp = createButton;
			} else if(creatorTypes.size() > 1) {
				Dropdown dropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
				dropdown.setElementCssClass("o_sel_repo_popup_create_resources");
				dropdown.setButton(true);
				mainVC.put("cmd.create.ressource", dropdown);
				for(String limitType:creatorTypes) {
					RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(limitType);
					Link createLink = LinkFactory.createLink(handler.getSupportedType(), getTranslator(), this);
					createLink.setUserObject(handler);
					dropdown.addComponent(createLink);
				}
				createRessourceCmp = dropdown;
			}
		}
		
		if (canImport) {
			if(isImportButtonVisible()) {
				importRessourceButton = LinkFactory.createButtonSmall("cmd.import.ressource", mainVC, this);
				importRessourceButton.setElementCssClass("o_sel_repo_popup_import_resource");
			}
			
			if(isImportUrlButtonVisible()) {
				importRessourceUrlButton = LinkFactory.createButtonSmall("cmd.import.url.ressource", mainVC, this);
				importRessourceUrlButton.setElementCssClass("o_sel_repo_popup_import_url_resource");
			}
		}

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		allEntriesLink = LinkFactory.createCustomLink(CMD_ALL_ENTRIES, CMD_ALL_ENTRIES, "referencableSearch." + CMD_ALL_ENTRIES, Link.LINK, mainVC, this);
		allEntriesLink.setElementCssClass("o_sel_repo_popup_all_resources");
		segmentView.addSegment(allEntriesLink, false);
		myEntriesLink = LinkFactory.createCustomLink(CMD_MY_ENTRIES, CMD_MY_ENTRIES, "referencableSearch." + CMD_MY_ENTRIES, Link.LINK, mainVC, this);
		myEntriesLink.setElementCssClass("o_sel_repo_popup_my_resources");
		segmentView.addSegment(myEntriesLink, true);
		searchEntriesLink = LinkFactory.createCustomLink(CMD_SEARCH_ENTRIES, CMD_SEARCH_ENTRIES, "referencableSearch." + CMD_SEARCH_ENTRIES, Link.LINK, mainVC, this);
		searchEntriesLink.setElementCssClass("o_sel_repo_popup_search_resources");
		segmentView.addSegment(searchEntriesLink, false);
		
		if(adminSearch && isAdminSearchVisible(limitTypes, ureq)) {
			adminEntriesLink = LinkFactory.createCustomLink(CMD_ADMIN_SEARCH_ENTRIES, CMD_ADMIN_SEARCH_ENTRIES, "referencableSearch." + CMD_ADMIN_SEARCH_ENTRIES, Link.LINK, mainVC, this);
			adminEntriesLink.setElementCssClass("o_sel_repo_popup_admin_search_resources");	
			segmentView.addSegment(adminEntriesLink, false);
		}

		searchCtr.doSearchByOwnerLimitType(ureq.getIdentity(), limitTypes);
		searchCtr.enableSearchforAllXXAbleInSearchForm(canBe);
		mainVC.put("searchCtr", searchCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * Admin. search allow group managers to search all courses
	 * @param limitingTypes
	 * @param ureq
	 * @return
	 */
	private boolean isAdminSearchVisible(String[] limitingTypes, UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		return limitingTypes != null && limitingTypes.length == 1 && "CourseModule".equals(limitingTypes[0])
				&& (roles.isAdministrator() ||
						(roles.isLearnResourceManager() && roles.isGroupManager()) ||
						(roles.isGroupManager() && businessGroupModule.isGroupManagersAllowedToLinkCourses()));
	}
	
	/**
	 * if building block can be imported, return true
	 * 
	 * @return
	 */
	private boolean isImportButtonVisible() {
		if(!canImport) return false;

		boolean importAllowed = false;
		List<String> limitTypeList = Arrays.asList(limitTypes);
		Set<String> supportedTypes = repositoryHandlerFactory.getSupportedTypes();
		for(String supportedType:supportedTypes) {
			if(repositoryHandlerFactory.getRepositoryHandler(supportedType).supportImport() && limitTypeList.contains(supportedType)) {
				importAllowed = true;
				break;
			}
		}
		return importAllowed;
	}
	
	private boolean isImportUrlButtonVisible() {
		if(!canImport) return false;

		boolean importAllowed = false;
		List<String> limitTypeList = Arrays.asList(limitTypes);
		Set<String> supportedTypes = repositoryHandlerFactory.getSupportedTypes();
		for(String supportedType:supportedTypes) {
			if(repositoryHandlerFactory.getRepositoryHandler(supportedType).supportImportUrl() && limitTypeList.contains(supportedType)) {
				importAllowed = true;
				break;
			}
		}
		return importAllowed;
	}

	/**
	 * @return Returns the selectedEntry.
	 */
	public RepositoryEntry getSelectedEntry() {
		return selectedRepositoryEntry;
	}
	
	/**
	 * @return Returns the selected entries
	 */
	public List<RepositoryEntry> getSelectedEntries() {
		if(selectedRepositoryEntries == null && selectedRepositoryEntry != null) {
			return Collections.singletonList(selectedRepositoryEntry);
		}
		return selectedRepositoryEntries;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myEntriesLink) {
					searchCtr.doSearchByOwnerLimitType(ureq.getIdentity(), limitTypes);
				} else if (clickedLink == allEntriesLink){
					switch(canBe) {
						case referenceable:
							searchCtr.doSearchForReferencableResourcesLimitType(getIdentity(), limitTypes, ureq.getUserSession().getRoles());
							break;
						case copyable:
							searchCtr.doSearchForCopyableResourcesLimitType(getIdentity(), limitTypes, ureq.getUserSession().getRoles());
							break;
						case all:
							searchCtr.doSearchByTypeLimitAccess(limitTypes, ureq);							
							break;
					}
				} else if (clickedLink == searchEntriesLink){
					searchCtr.displaySearchForm();
				} else if (clickedLink == adminEntriesLink) {
					searchCtr.displayAdminSearchForm();
				}
				mainVC.setDirty(true);
			}
		} else if(source == createRessourceCmp
				|| (source instanceof Link && ((Link)source).getUserObject() instanceof RepositoryHandler)) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(createController);
			RepositoryHandler handler = (RepositoryHandler)((Link)source).getUserObject();
			createController = handler.createCreateRepositoryEntryController(ureq, getWindowControl());
			listenTo(createController);
			
			String title = translate(handler.getCreateLabelI18nKey());
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					createController.getInitialComponent(), true, title);
			cmc.setCustomWindowCSS("o_sel_author_create_popup");
			listenTo(cmc);
			
			cmc.activate();
		} else if (source == importRessourceButton) {
			doImportResource(ureq);
		} else if (source == importRessourceUrlButton) {
			doImportResourceUrl(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		String cmd = event.getCommand();
		if (source == searchCtr) {
			if (cmd.equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				// done, user selected a repo entry
				selectedRepositoryEntry = searchCtr.getSelectedEntry();
				selectedRepositoryEntries = null;
				fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
			} else if (cmd.equals(RepositoryTableModel.TABLE_ACTION_SELECT_ENTRIES)) {
				selectedRepositoryEntry = null;
				selectedRepositoryEntries = searchCtr.getSelectedEntries();
				fireEvent(ureq, EVENT_REPOSITORY_ENTRIES_SELECTED);
			}
		} else if (source == createController) { 
			if (event.equals(Event.DONE_EVENT)) {
				cmc.deactivate();
				
				selectedRepositoryEntry = createController.getAddedEntry();
				fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
				// info message
				String message = translate("message.entry.selected", new String[] { selectedRepositoryEntry.getDisplayname(), selectedRepositoryEntry.getResourcename()});
				getWindowControl().setInfo(message);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
			} else if (event.equals(Event.FAILED_EVENT)) {
				showError("add.failed");
			}
		} else if (source == importController) { 
			if (event.equals(Event.DONE_EVENT)) {
				cmc.deactivate();
				selectedRepositoryEntry = importController.getImportedEntry();
				fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
				// info message
				String message = translate("message.entry.selected", new String[] { selectedRepositoryEntry.getDisplayname(), selectedRepositoryEntry.getResourcename()});
				getWindowControl().setInfo(message);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
			} else if (event.equals(Event.FAILED_EVENT)) {
				showError("add.failed");
			}
		} else if (source == importUrlController) { 
			if (event.equals(Event.DONE_EVENT)) {
				cmc.deactivate();
				selectedRepositoryEntry = importUrlController.getImportedEntry();
				fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
				// info message
				String message = translate("message.entry.selected", new String[] { selectedRepositoryEntry.getDisplayname(), selectedRepositoryEntry.getResourcename()});
				getWindowControl().setInfo(message);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
			} else if (event.equals(Event.FAILED_EVENT)) {
				showError("add.failed");
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void doImportResource(UserRequest ureq) {
		removeAsListenerAndDispose(importController);
		importController = new ImportRepositoryEntryController(ureq, getWindowControl(), limitTypes);
		listenTo(importController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importController.getInitialComponent(), true, "");
		listenTo(cmc);
		cmc.activate();
	}
	
	
	private void doImportResourceUrl(UserRequest ureq) {
		removeAsListenerAndDispose(importController);
		importUrlController = new ImportURLRepositoryEntryController(ureq, getWindowControl(), limitTypes);
		listenTo(importUrlController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importUrlController.getInitialComponent(), true, "");
		listenTo(cmc);
		cmc.activate();
	}
}
