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

package org.olat.group.ui.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortletRunController extends AbstractPortletRunController<BusinessGroupEntry> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private final TableController tableCtr;
	private final GroupTableDataModel groupListModel;
	private VelocityContainer groupsVC;
	private Link showAllLink;
	
	private final BusinessGroupService businessGroupService;
	

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param component
	 */
	public GroupsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) {
		super(wControl, ureq, trans, portletName, defaultMaxEntries);

		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		groupsVC = createVelocityContainer("groupsPortlet");
		showAllLink = LinkFactory.createLink("groupsPortlet.showAll", groupsVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("groupsPortlet.nogroups"), null, "o_icon_group");
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
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("groupsPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale()));
		
		sortingCriteria = getPersistentSortingConfiguration(ureq);
		groupListModel = new GroupTableDataModel(Collections.<PortletEntry<BusinessGroupEntry>>emptyList());
		tableCtr.setTableDataModel(groupListModel);
		reloadModel(sortingCriteria);
     
		groupsVC.put("table", tableCtr.getInitialComponent());		
		putInitialPanel(groupsVC);

		// register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
	}
	
	private List<PortletEntry<BusinessGroupEntry>> convertBusinessGroupToPortletEntryList(List<BusinessGroup> groups, boolean withDescription) {
		List<PortletEntry<BusinessGroupEntry>> convertedList = new ArrayList<>();
		for(BusinessGroup group:groups) {
			GroupPortletEntry entry = new GroupPortletEntry(group);
			if(withDescription) {
				entry.getValue().setDescription(group.getDescription());
			}
			convertedList.add(entry);
		}
		return convertedList;
	}
	
	private List<PortletEntry<BusinessGroupEntry>> convertShortBusinessGroupToPortletEntryList(List<BusinessGroup> groups, boolean withDescription) {
		List<PortletEntry<BusinessGroupEntry>> convertedList = new ArrayList<>();
		for(BusinessGroup group:groups) {
			GroupPortletEntry entry = new GroupPortletEntry(group);
			if(withDescription) {
				entry.getValue().setDescription(group.getDescription());
			}
			convertedList.add(entry);
		}
		return convertedList;
	}
	
	@Override
	protected void reloadModel(SortingCriteria sortCriteria) {
		if (sortCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			BusinessGroupOrder order = null;
			if(sortCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {
				order = sortCriteria.isAscending() ? BusinessGroupOrder.nameAsc : BusinessGroupOrder.nameDesc;
			} else if(sortCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
				order = sortCriteria.isAscending() ? BusinessGroupOrder.creationDateAsc : BusinessGroupOrder.creationDateDesc;
			}
			
			int maxEntries = sortCriteria.getMaxEntries();
			List<BusinessGroup> groupList = businessGroupService.findBusinessGroups(getIdentity(), maxEntries * 2, order);
			Set<BusinessGroup> removeDuplicates = new HashSet<>(maxEntries);
			for(Iterator<BusinessGroup> it=groupList.iterator(); it.hasNext(); ) {
				BusinessGroup group = it.next();
				if(removeDuplicates.contains(group)) {
					it.remove();
				} else {
					removeDuplicates.add(group);
				}
			}
			
			List<BusinessGroup> uniqueList = groupList.subList(0, Math.min(maxEntries, groupList.size())); 
			List<PortletEntry<BusinessGroupEntry>> entries = convertShortBusinessGroupToPortletEntryList(uniqueList, false);
			groupListModel.setObjects(entries);
			tableCtr.modelChanged();
		} else {
			reloadModel(getPersistentManuallySortedItems());
		}
	}
	
	protected void reloadModel(List<PortletEntry<BusinessGroupEntry>> sortedItems) {						
		groupListModel.setObjects(sortedItems);
		tableCtr.modelChanged();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink) {
			String businessPath = "[GroupsSite:0][AllGroups:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} 
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					PortletEntry<BusinessGroupEntry> entry = groupListModel.getObject(te.getRowId());
					String businessPath = "[BusinessGroup:" + entry.getKey() + "]";
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				}
			}
		}	
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		// de-register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
		// POST: all firing event for the source just deregistered are finished
		// (listeners lock in EventAgency)
	}

	@Override
	public void event(Event event) {
		if (event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent)event;
			if(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT.equals(mev.getCommand())
					&& getIdentity().getKey().equals(mev.getAffectedIdentityKey())
					&& mev.getAffectedRepositoryEntryKey() == null) {
				
				Long modifiedKey = mev.getModifiedGroupKey();
				for(PortletEntry<BusinessGroupEntry> portlet:groupListModel.getObjects()) {
					if(modifiedKey.equals(portlet.getKey())) {
						groupListModel.getObjects().remove(portlet);
						tableCtr.modelChanged();
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<BusinessGroupEntry> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {
			List<BusinessGroup> groupList = businessGroupService.findBusinessGroups(getIdentity(), -1);
			List<PortletEntry<BusinessGroupEntry>> portletEntryList = convertShortBusinessGroupToPortletEntryList(groupList, true);
			GroupsManualSortingTableDataModel tableDataModel = new GroupsManualSortingTableDataModel(portletEntryList);
			List<PortletEntry<BusinessGroupEntry>> sortedItems = getPersistentManuallySortedItems();
			
			portletToolsController = new PortletToolSortingControllerImpl<>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
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
	private List<PortletEntry<BusinessGroupEntry>> getPersistentManuallySortedItems() { 
		@SuppressWarnings("unchecked")
		Map<Long, Integer> storedPrefs = (Map<Long, Integer>)guiPreferences.get(Map.class, getPreferenceKey(SORTED_ITEMS_PREF));
  	
		List<PortletEntry<BusinessGroupEntry>> portletEntryList;
		if(storedPrefs != null) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
			params.setGroupKeys(storedPrefs.keySet());
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
			portletEntryList = convertBusinessGroupToPortletEntryList(groups, false);
		} else {
			List<BusinessGroup> groups = new ArrayList<>();
			portletEntryList = convertShortBusinessGroupToPortletEntryList(groups, false);
		}
		return getPersistentManuallySortedItems(portletEntryList);
	}
  
  /**
	 * Comparator implementation used for sorting BusinessGroup entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param sortCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
	@Override
  protected Comparator<BusinessGroupEntry> getComparator(final SortingCriteria sortCriteria) {
		return new Comparator<>(){
			@Override
			public int compare(final BusinessGroupEntry group1, final BusinessGroupEntry group2) {
				int comparisonResult = 0;
			  if(sortCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(group1.getName(), group2.getName());			  		  	
			  } else if(sortCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = group1.getCreationDate().compareTo(group2.getCreationDate());
			  }
			  if(!sortCriteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}
		};
	}
  
  /**
   * 
   * PortletDefaultTableDataModel implementation for the current portlet.
   * 
   * <P>
   * Initial Date:  10.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private static class GroupTableDataModel extends PortletDefaultTableDataModel<BusinessGroupEntry> {  	
  	public GroupTableDataModel(List<PortletEntry<BusinessGroupEntry>> objects) {
  		super(objects, 1);
  	}
  	
  	public Object getValueAt(int row, int col) {
  		PortletEntry<BusinessGroupEntry> entry = getObject(row);
  		BusinessGroupEntry businessGroup = entry.getValue();
  		switch (col) {
  			case 0:
  				return businessGroup.getName();
  			default:
  				return "ERROR";
  		}
  	}

		@Override
		public Object createCopyWithEmptyList() {
			return new GroupTableDataModel(new ArrayList<PortletEntry<BusinessGroupEntry>>());
		}
  }

  /**
   * 
   * PortletDefaultTableDataModel implementation for the manual sorting component.
   * 
   * <P>
   * Initial Date:  10.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
	private static class GroupsManualSortingTableDataModel extends PortletDefaultTableDataModel<BusinessGroupEntry>  {		
		/**
		 * @param objects
		 * @param locale
		 */
		public GroupsManualSortingTableDataModel(List<PortletEntry<BusinessGroupEntry>> objects) {
			super(objects, 3);
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			PortletEntry<BusinessGroupEntry> portletEntry = getObject(row);
			BusinessGroupEntry group = portletEntry.getValue();
			switch (col) {
				case 0:
					return group.getName();
				case 1:
					String description = group.getDescription();
					description = FilterFactory.getHtmlTagsFilter().filter(description);
					return (description == null ? "n/a" : description);
				case 2:
					Date date = group.getCreationDate();
					return date;
				default:
					return "error";
			}
		}

		@Override
		public GroupsManualSortingTableDataModel createCopyWithEmptyList() {
			return new GroupsManualSortingTableDataModel(new ArrayList<PortletEntry<BusinessGroupEntry>>());
		}
	}
	
	private static class GroupPortletEntry implements PortletEntry<BusinessGroupEntry> {
	  	private BusinessGroupEntry value;
	  	private Long key;
	  	
	  	public GroupPortletEntry(BusinessGroup group) {
	  		value = new BusinessGroupEntry(group);
	  		key = group.getKey();
	  	}
	  	
	  	public Long getKey() {
	  		return key;
	  	}
	  	
	  	public BusinessGroupEntry getValue() {
	  		return value;
	  	}
	}
}
