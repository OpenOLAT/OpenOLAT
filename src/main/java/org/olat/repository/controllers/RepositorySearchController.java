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
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.repository.ui.author.RepositoryEntrySmallDetailsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
*  Description:
*  This workflow is used to search for repository entries. The workflow has two steps: 
*  1) a search form and 2) the results list. After calling the constructor nothing 
*  happens. The views must be initialized manually. Using some public methods the 
*  desired view can be triggered.
*  The workflow can be limited to a specific repository type. 
*  Onother option is to set the set the enableSearchforAllReferencalbeInSearchForm
*  In this case, the search workflow can be used to find repository entires that can be
*  referenced by the user. 
*  Some doSearch... methods allow the presentation of lists without using the search
*  form at all.
*
* @author Felix Jost
*/
public class RepositorySearchController extends BasicController implements Activateable2 {

	
	private VelocityContainer vc;
	private RepositoryTableModel repoTableModel;
	private SearchForm searchForm;
	private TableController tableCtr;
	private CloseableCalloutWindowController calloutCtrl;
	private RepositoryEntrySmallDetailsController infosCtrl;
	
	private Link loginLink;
	private Link backLink;
	private Link cancelButton;
	
	private RepositoryEntry selectedEntry;
	private List<RepositoryEntry> selectedEntries;
	private Can enableSearchforAllInSearchForm;
	private SearchType searchType;
	private final Roles identityRoles;
	private final IdentityRef asParticipant;
	private final RepositoryEntryFilter filter;
	private final boolean organisationWildCard;
	
	@Autowired
	private RepositoryManager repositoryManager;

	public RepositorySearchController(String selectButtonLabel, UserRequest ureq, WindowControl myWControl,
			boolean withCancel, boolean multiSelect, String[] limitTypes, boolean organisationWildCard,
			RepositoryEntryFilter filter, IdentityRef asParticipant) {
		super(ureq, myWControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		
		this.filter = filter;
		this.asParticipant = asParticipant;
		this.organisationWildCard = organisationWildCard;
		identityRoles = ureq.getUserSession().getRoles();
		
		vc = createVelocityContainer("reposearch", "search");

		removeAsListenerAndDispose(searchForm);
		searchForm = new SearchForm(ureq, getWindowControl(), withCancel, limitTypes);
		listenTo(searchForm);
		
		searchForm.setVisible(false);
		vc.put("searchform",searchForm.getInitialComponent());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setSuppressDirtyFormWarning(true);
		if (selectButtonLabel != null) {
			tableConfig.setPreferencesOffered(true, "repositorySearchResult_v2");
		}
		
		String filterTitle = translate("search.filter.type");
		String noFilterOption = translate("search.filter.showAll");
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, filterTitle, noFilterOption, true, getTranslator());
		if(multiSelect) {
			tableCtr.setMultiSelect(multiSelect);
			tableCtr.addLabeledMultiSelectAction(selectButtonLabel, "mselect");
		}
		listenTo(tableCtr);
		
		repoTableModel = new RepositoryTableModel(getLocale());
		ColumnDescriptor sortCol = repoTableModel.addColumnDescriptors(tableCtr, selectButtonLabel != null, selectButtonLabel != null, false, true);
		tableCtr.setTableDataModel(repoTableModel);
		tableCtr.setSortColumn(sortCol, true);
		vc.put("repotable", tableCtr.getInitialComponent());
		vc.contextPut("withCancel", Boolean.valueOf(withCancel));
		enableBackToSearchFormLink(false); // default, must be enabled explicitly
		enableSearchforAllXXAbleInSearchForm(null); // default
		putInitialPanel(vc);
	}

	/**
	 * @param enableBack true: back link is shown, back goes to search form; false; no back link
	 */
	public void enableBackToSearchFormLink(boolean enableBack) {
		vc.contextPut("withBack", Boolean.valueOf(enableBack));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String subType = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(RepositoryEntry.class.getSimpleName().equals(subType)) {
			//activate details
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			selectedEntry = repositoryManager.lookupRepositoryEntry(resId);
			fireEvent(ureq, new Event(RepositoryTableModel.TABLE_ACTION_SELECT_LINK));
		}
	}

	/**
	 * @param enable true: searches done by the search form will find all resources
	 * that are referencable/copyable by the current user; false: searches done by the search 
	 * form will find all resources that have at least BAR setting in the BARG configuration
	 * list
	 */
	public void enableSearchforAllXXAbleInSearchForm(Can enable) {
		enableSearchforAllInSearchForm = enable;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == backLink){
			displaySearchForm();
		} else if (source == loginLink){
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		}
	}
	
	private void filterRepositoryEntries(List<RepositoryEntry> entries) {
		if(filter != null && entries != null && !entries.isEmpty()) {
			for(Iterator<RepositoryEntry> entryIt=entries.iterator(); entryIt.hasNext(); ) {
				if(!filter.accept(entryIt.next())) {
					entryIt.remove();
				}
			}
		}
	}

	/**
	 * Implementation normal search: find repo entries that are public
	 * using the values from the form
	 * @param ureq
	 */
	private void doSearch(UserRequest ureq, String limitType, boolean updateFilters) {
		searchType = SearchType.searchForm;
		Collection<String> s = searchForm.getRestrictedTypes();
		List<String> restrictedTypes;
		if(limitType != null) {
			restrictedTypes = Collections.singletonList(limitType);
		} else {
			restrictedTypes = (s == null) ? null : new ArrayList<>(s);
		}

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(searchForm.getDisplayName(),
				searchForm.getAuthor(), searchForm.getDescription(), restrictedTypes, getIdentity(), identityRoles);
		if (searchForm.hasId()) {
			params.setIdRefsAndTitle(searchForm.getId(), true);
		}
		List<RepositoryEntry> entries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		if(updateFilters) {
			updateFilters(entries, null);
		}
		tableCtr.modelChanged();
		displaySearchResults(ureq);
	}

	/**
	 * Implementation of referencable search: find repo entries that are 
	 * owned by the uer or set to referencable and have at lease BA settings
	 * @param ureq
	 */
	private void doSearchAllReferencables(UserRequest ureq, String limitType, boolean updateFilters) {
		searchType = SearchType.searchForm;
		List<String> restrictedTypes;
		if(limitType != null) {
			restrictedTypes = Collections.singletonList(limitType);
		} else {
			Collection<String> s = searchForm.getRestrictedTypes();
			restrictedTypes = (s == null) ? null : new ArrayList<>(s);
		}
		
		String name = searchForm.getDisplayName();
		String author = searchForm.getAuthor();
		String desc = searchForm.getDescription();
		String idAndRefs = searchForm.getId();
		List<RepositoryEntry> entries = repositoryManager.queryResourcesLimitType(getIdentity(),
				identityRoles,organisationWildCard, restrictedTypes, name, author, desc, idAndRefs,
				asParticipant, enableSearchforAllInSearchForm == Can.referenceable, enableSearchforAllInSearchForm == Can.copyable);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		if(updateFilters) {
			updateFilters(entries, null);
		}
		tableCtr.modelChanged();
		displaySearchResults(ureq);
	}

	/**
	 * Do search for all resources that the user can reference either because he 
	 * is the owner of the resource or because he has author rights and the resource
	 * is set to at least BA in the BARG settings and the resource has the flag
	 * 'canReference' set to true.
	 * @param owner The current identity
	 * @param limitTypes List of Types to limit the search
	 * @param roles The users roles
	 */
	public void doSearchForReferencableResourcesLimitType(Identity owner, String[] limitTypes, Roles roles) {
		List<String> restrictedTypes = new ArrayList<>();
		if(limitTypes == null) {
			restrictedTypes = null;
		} else {
			restrictedTypes.addAll(Arrays.asList(limitTypes));
		}
		
		List<RepositoryEntry> entries = repositoryManager.queryResourcesLimitType(owner, roles, organisationWildCard,
				restrictedTypes, null, null, null, null, asParticipant, true, false);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		tableCtr.setFilters(null, null);
		tableCtr.modelChanged();
		displaySearchResults(null);
	}
	
	/**
	 * Do search for all resources that the user can copy either because he 
	 * is the owner of the resource or because he has author rights and the resource
	 * is set to at least BA in the BARG settings and the resource has the flag
	 * 'canCopy' set to true.
	 * @param owner The current identity
	 * @param limitTypes List of Types to limit the search
	 * @param roles The users roles
	 */
	public void doSearchForCopyableResourcesLimitType(Identity owner, String[] limitTypes, Roles roles) {
		List<String> restrictedTypes = new ArrayList<>();
		if(limitTypes == null) {
			restrictedTypes = null;
		} else {
			restrictedTypes.addAll(Arrays.asList(limitTypes));
		}
		List<RepositoryEntry> entries = repositoryManager.queryResourcesLimitType(owner, roles, organisationWildCard,
				restrictedTypes, null, null, null, null, asParticipant, false, true);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		tableCtr.setFilters(null, null);
		tableCtr.modelChanged();
		displaySearchResults(null);
	}
	
	/**
	 * Search for all resources where identity is owner.
	 * 
	 * @param owner
	 */
	private void doSearchByOwner(Identity owner) {
		doSearchByOwnerLimitTypeInternal(owner, new String[] {}, true);
	}

	/**
	 * Do search for all resources of a given type where identity is owner.
	 * @param owner
	 * @param limitType
	 */
	protected void doSearchByOwnerLimitType(Identity owner, String[] limitTypes) {
		doSearchByOwnerLimitTypeInternal(owner, limitTypes, true);
	}
	
	private void doSearchByOwnerLimitTypeInternal(Identity owner, String[] limitTypes, boolean updateFilters) {
		searchType = SearchType.byOwner;
		List<RepositoryEntry> entries = repositoryManager.queryByOwner(owner, true, asParticipant, limitTypes);
		filterRepositoryEntries(entries);
		if(updateFilters) {
			updateFilters(entries, owner);
		}
		repoTableModel.setObjects(entries);
		tableCtr.modelChanged(updateFilters);
		displaySearchResults(null);
	}
	
	/**
	 * Do search for all resources of a given type where identity is owner.
	 * @param owner
	 * @param access
	 */
	public void doSearchByOwnerLimitAccess(Identity owner) {
		List<RepositoryEntry> entries = repositoryManager.queryByOwnerLimitAccess(owner);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		tableCtr.setFilters(null, null);
		tableCtr.modelChanged();
		displaySearchResults(null);
	}
	
	/**
	 * Used by repository main controller to execute predefined searches.
	 * 
	 * @param restrictedTypes
	 * @param ureq
	 */
	public void doSearchByTypeLimitAccess(String[] restrictedTypes, UserRequest ureq) {
		searchType = null;

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(getIdentity(), identityRoles, restrictedTypes);
		params.setAsParticipant(asParticipant);
		List<RepositoryEntry> entries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
		filterRepositoryEntries(entries);
		repoTableModel.setObjects(entries);
		tableCtr.setFilters(null, null);
		tableCtr.modelChanged();
		displaySearchResults(ureq);
	}
	
	protected void updateFilters(List<RepositoryEntry> entries, Identity owner) {
		List<ShortName> restrictedTypes = new ArrayList<>();
		Set<String> uniqueTypes = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			if(entry.getOlatResource() == null) continue;//no red screen for that
			String type = entry.getOlatResource().getResourceableTypeName();
			if(type != null && !uniqueTypes.contains(type)) {
				String label = translate(type);
				restrictedTypes.add(new TypeFilter(type, label, owner));
				uniqueTypes.add(type);
			}
		}
		if(restrictedTypes.size() > 1) {
			tableCtr.setFilters(restrictedTypes, null);
		} else {
			tableCtr.setFilters(null, null);
		}
	}
	
	/**
	 * @return Returns the selectedEntry.
	 */
	public RepositoryEntry getSelectedEntry() {
		return selectedEntry;
	}
	
	public List<RepositoryEntry> getSelectedEntries() {
		if(selectedEntries == null && selectedEntry != null) {
			return Collections.singletonList(selectedEntry);
		}
		return selectedEntries;
	}

	/**
	 * Will reset the controller to display the search form again.
	 */
	public void displaySearchForm() {
		searchForm.setVisible(true);
		searchForm.setAdminSearch(false);
		vc.setPage(velocity_root + "/search.html");
	}
	
	/**
	 * Will reset the controller to display the search form again.
	 */
	public void displayAdminSearchForm() {
		searchForm.setVisible(true);
		searchForm.setAdminSearch(true);
		vc.setPage(velocity_root + "/search.html");
	}
	
	/**
	 * Present the search results page.
	 */
	public void displaySearchResults(UserRequest ureq) {
		searchForm.setVisible(false);
		if (repoTableModel.getRowCount() == 0) vc.contextPut("hasResults", Boolean.FALSE);
		else vc.contextPut("hasResults", Boolean.TRUE);
		backLink = LinkFactory.createLinkBack(vc, this);
		vc.setPage(velocity_root + "/results.html");
		//REVIEW:pb why can ureq be null here?
		vc.contextPut("isGuest", (ureq != null) ? Boolean.valueOf(identityRoles.isGuestOnly()) : Boolean.FALSE);
		loginLink = LinkFactory.createLink("repo.login", vc, this);
		cancelButton = LinkFactory.createButton("cancel", vc, this);
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == tableCtr) { // process table actions
			if(event instanceof TableEvent) {
				TableEvent te = (TableEvent)event;
				int rowId = te.getRowId();
				if (te.getActionId().equals(RepositoryTableModel.TABLE_ACTION_INFOS)) {
					RepositoryEntry entry =  (RepositoryEntry)tableCtr.getTableDataModel().getObject(rowId);
					int row = tableCtr.getIndexOfSortedObject(entry);
					doOpenInfos(urequest, entry, row);
				} else if (te.getActionId().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
					selectedEntry =  (RepositoryEntry)tableCtr.getTableDataModel().getObject(rowId);
					fireEvent(urequest, new Event(RepositoryTableModel.TABLE_ACTION_SELECT_LINK));
					selectedEntries = null;
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent mse = (TableMultiSelectEvent)event;	
				if(!mse.getSelection().isEmpty()) {
					selectedEntry = null;
					
					BitSet objectMarkers = mse.getSelection();
					selectedEntries = new ArrayList<>(objectMarkers.size());
					for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
						RepositoryEntry entry =  (RepositoryEntry)tableCtr.getTableDataModel().getObject(i);
						selectedEntries.add(entry);
					}
					fireEvent(urequest, new Event(RepositoryTableModel.TABLE_ACTION_SELECT_ENTRIES));
				}
			} else if (TableController.EVENT_FILTER_SELECTED.equals(event)) {
				TypeFilter typeFilter = (TypeFilter) tableCtr.getActiveFilter();
				if(searchType == SearchType.byOwner) {
					doSearchByOwnerLimitTypeInternal(typeFilter.getOwner(), new String[]{typeFilter.getType()}, false);
				} else if(searchType == SearchType.searchForm) {
					if(enableSearchforAllInSearchForm != null) {
						doSearchAllReferencables(urequest, typeFilter.getType(), false);
					} else {
						doSearch(urequest, typeFilter.getType(), false);
					}
				}
			} else if (TableController.EVENT_NOFILTER_SELECTED.equals(event)) {
				if(searchType == SearchType.byOwner) {
					doSearchByOwnerLimitTypeInternal(getIdentity(), new String[]{}, false);
				} else if(searchType == SearchType.searchForm) {
					if(enableSearchforAllInSearchForm != null) {
						doSearchAllReferencables(urequest, null, false);
					} else {
						doSearch(urequest, null, false);
					}
				}
			}
		} 
		else if (event instanceof EntryChangedEvent) { // remove deleted entry
			EntryChangedEvent ecv = (EntryChangedEvent)event;
			if (ecv.getChange() == Change.deleted) {
				List<RepositoryEntry> newEntries = new ArrayList<>();
				for (int i = 0; i < repoTableModel.getRowCount(); i++) {
					RepositoryEntry foo = repoTableModel.getObject(i);
					if (!foo.getKey().equals(ecv.getRepositoryEntryKey())) {
						newEntries.add(foo);
					}
				}
				repoTableModel.setObjects(newEntries);
				tableCtr.modelChanged();
			} else if (ecv.getChange() == Change.added) {
				doSearchByOwner(urequest.getIdentity());
			}
		}	else if (source == searchForm) { // process search form events
			if (event == Event.DONE_EVENT) {
				if (enableSearchforAllInSearchForm != null) {
					doSearchAllReferencables(urequest, null, true);
				} else {
					doSearch(urequest, null, true);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(urequest, Event.CANCELLED_EVENT);
			}
		} else if(calloutCtrl == source) {
			removeAsListenerAndDispose(calloutCtrl);
			removeAsListenerAndDispose(infosCtrl);
			calloutCtrl = null;
			infosCtrl = null;
		}
	}
	
	private void doOpenInfos(UserRequest ureq, RepositoryEntry repositoryEntry, int rowId) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(infosCtrl);
		
		infosCtrl = new RepositoryEntrySmallDetailsController(ureq, getWindowControl(), repositoryEntry);
		listenTo(infosCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), infosCtrl.getInitialComponent(),
				"ore" + rowId + "ref", null, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}
	
	private static class TypeFilter implements ShortName {
		
		private final String type;
		private final String typeName;
		private final Identity owner;

		public TypeFilter(String type, String typeName, Identity owner) {
			this.type = type;
			this.typeName = typeName;
			this.owner = owner;
		}

		public String getType() {
			return type;
		}
		
		public Identity getOwner() {
			return owner;
		}

		@Override
		public String getShortName() {
			return typeName;
		}
		
		@Override
		public int hashCode() {
			return type.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof TypeFilter) {
				TypeFilter typeobj = (TypeFilter)obj;
				return type != null && type.equals(typeobj.type);
			}
			return false;
		}
	}
	
	public enum SearchType {
		byOwner,
		searchForm,
	}
	
	public enum Can {
		referenceable,
		copyable,
		all
	}
}