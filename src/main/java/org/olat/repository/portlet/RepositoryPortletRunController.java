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
package org.olat.repository.portlet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLight;
import org.olat.repository.RepositoryEntryOrder;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryEntryTypeColumnDescriptor;


/**
 * 
 * Description:<br>
 * Runtime view that shows a list of courses, either as student or teacher
 * 
 * <P>
 * Initial Date:  06.03.2009 <br>
 * @author gnaegi
 */
public class RepositoryPortletRunController extends AbstractPortletRunController<RepositoryEntryLight> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	private RepositoryPortletTableDataModel repoEntryListModel;
	private boolean studentView;

	private Link showAllLink;
	
	/**
	 * Constructor
	 * @param wControl
	 * @param ureq
	 * @param trans
	 * @param portletName
	 * @param studentView true: show courses where I'm student; false: show courses where I'm teacher
	 */
	public RepositoryPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName,
			int defaultMaxEntries, boolean studentView) {
		super(wControl, ureq, trans, portletName, defaultMaxEntries);
		this.studentView = studentView;	
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);				
		VelocityContainer repoEntriesVC = this.createVelocityContainer("repositoryPortlet");
		showAllLink = LinkFactory.createLink("repositoryPortlet.showAll", repoEntriesVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
			
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("repositoryPortlet.noentry"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
    //disable the default sorting for this table
		tableConfig.setSortingEnabled(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to
		// false
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("repositoryPortlet.img", 2, CMD_LAUNCH, trans.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("repositoryPortlet.name", 0, CMD_LAUNCH, trans.getLocale(),	ColumnDescriptor.ALIGNMENT_LEFT));
		
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(this.sortingCriteria);
		repoEntriesVC.put("table", tableCtr.getInitialComponent());

		putInitialPanel(repoEntriesVC);
	}
	
	private List<RepositoryEntryLight> getAllEntries(SortingCriteria criteria) {
		int maxResults = criteria == null ? -1 : criteria.getMaxEntries();
		RepositoryEntryOrder orderBy = RepositoryEntryOrder.nameAsc;
		if(criteria != null && !criteria.isAscending()) {
			orderBy = RepositoryEntryOrder.nameDesc;
		}
		
		List<RepositoryEntryLight> entries;
		if (studentView) {
			entries = RepositoryManager.getInstance().getParticipantRepositoryEntry(getIdentity(), maxResults, orderBy);
		} else {
			List<RepositoryEntry> fullEntries = RepositoryManager.getInstance()
					.getLearningResourcesAsTeacher(getIdentity(), 0, maxResults, orderBy);
			entries = new ArrayList<>();
			for(RepositoryEntry fullEntry:fullEntries) {
				entries.add(new FullReWrapper(fullEntry));
			}
		}
		return entries;
	}

	private List<PortletEntry<RepositoryEntryLight>> convertShortRepositoryEntriesToPortletEntryList(List<RepositoryEntryLight> items) {
		List<PortletEntry<RepositoryEntryLight>> convertedList = new ArrayList<>();
		for(RepositoryEntryLight item:items) {
			RepositoryEntryStatusEnum status = item.getEntryStatus();
			if(!status.decommissioned()) {
				RepositoryPortletEntry entry = new RepositoryPortletEntry(item);
				convertedList.add(entry);
			}
		}
		return convertedList;
	}
	
	private List<PortletEntry<RepositoryEntryLight>> convertRepositoryEntriesToPortletEntryList(List<RepositoryEntry> items) {
		List<PortletEntry<RepositoryEntryLight>> convertedList = new ArrayList<>();
		for(RepositoryEntry item:items) {
			RepositoryEntryStatusEnum status = item.getEntryStatus();
			if(!status.decommissioned()) {
				RepositoryPortletEntry entry = new RepositoryPortletEntry(item);
				convertedList.add(entry);
			}
		}
		return convertedList;
	}

	@Override
	protected void reloadModel(SortingCriteria criteria) {
		if (criteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			List<RepositoryEntryLight> items = getAllEntries(criteria);
			List<PortletEntry<RepositoryEntryLight>> entries = convertShortRepositoryEntriesToPortletEntryList(items);
			repoEntryListModel = new RepositoryPortletTableDataModel(entries, getLocale());
			tableCtr.setTableDataModel(repoEntryListModel);
		} else {
			reloadModel(getPersistentManuallySortedItems());
		}
	}

	@Override
	protected void reloadModel(List<PortletEntry<RepositoryEntryLight>> sortedItems) {						
		repoEntryListModel = new RepositoryPortletTableDataModel(sortedItems, getLocale());
		tableCtr.setTableDataModel(repoEntryListModel);
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			String target;
			if(studentView) {
				target = "[MyCoursesSite:0][My:0]";
			} else {
				target = "[RepositorySite:0][My:0]";
			}
			NewControllerFactory.getInstance().launch(target, ureq, getWindowControl());
		} 
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowId = te.getRowId();
					PortletEntry<RepositoryEntryLight> entry = repoEntryListModel.getObject(rowId);
					NewControllerFactory.getInstance().launch("[RepositoryEntry:" + entry.getKey() + "]", ureq, getWindowControl());
				}
			}
		}	
	}

	@Override
	public void event(Event event) {
		//
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<RepositoryEntryLight> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			List<RepositoryEntryLight> items = getAllEntries(null);
			List<PortletEntry<RepositoryEntryLight>> entries = convertShortRepositoryEntriesToPortletEntryList(items);
			PortletDefaultTableDataModel<RepositoryEntryLight> tableDataModel = new RepositoryPortletTableDataModel(entries, ureq.getLocale());
			List<PortletEntry<RepositoryEntryLight>> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl<RepositoryEntryLight>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}
	
	 /**
   * Retrieves the persistent manually sorted items for the current portlet.
   * @param ureq
   * @return
   */
  private List<PortletEntry<RepositoryEntryLight>> getPersistentManuallySortedItems() {
		@SuppressWarnings("unchecked")
		Map<Long, Integer> storedPrefs = (Map<Long, Integer>) guiPreferences.get(Map.class, getPreferenceKey(SORTED_ITEMS_PREF));
		if(storedPrefs == null) {
			return new ArrayList<PortletEntry<RepositoryEntryLight>>();
		}
		List<RepositoryEntry> items = RepositoryManager.getInstance().lookupRepositoryEntries(storedPrefs.keySet());
		List<PortletEntry<RepositoryEntryLight>> entries = convertRepositoryEntriesToPortletEntryList(items);
		return getPersistentManuallySortedItems(entries);
	}
  
  /**
	 * Comparator implementation used for sorting BusinessGroup entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param criteria
	 * @return a Comparator for the input sortingCriteria
	 */
	@Override
	protected Comparator<RepositoryEntryLight> getComparator(final SortingCriteria criteria) {
		return new Comparator<RepositoryEntryLight>(){			
			public int compare(final RepositoryEntryLight repoEntry1, final RepositoryEntryLight repoEntry2) {
				int comparisonResult = 0;
			  if(criteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(repoEntry1.getDisplayname(), repoEntry2.getDisplayname());			  		  	
			  }
			  if(!criteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}};
	}
  
	private static class FullReWrapper implements  RepositoryEntryLight {
		
		private final RepositoryEntry re;
		
		public FullReWrapper(RepositoryEntry re) {
			this.re = re;
		}

		@Override
		public Long getKey() {
			return re.getKey();
		}

		@Override
		public String getDisplayname() {
			return re.getDisplayname();
		}

		@Override
		public String getResourceType() {
			return re.getOlatResource().getResourceableTypeName();
		}

		@Override
		public String getDescription() {
			return re.getDescription();
		}

		@Override
		public RepositoryEntryStatusEnum getEntryStatus() {
			return re.getEntryStatus();
		}

		@Override
		public boolean isAllUsers() {
			return re.isAllUsers();
		}

		@Override
		public boolean isGuests() {
			return re.isGuests();
		}
	}
}