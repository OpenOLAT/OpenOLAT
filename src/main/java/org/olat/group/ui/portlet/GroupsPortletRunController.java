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
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
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
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.site.GroupsSite;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortletRunController extends AbstractPortletRunController implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	//private GroupListMiniModel groupListModel;
	private GroupTableDataModel groupListModel;
	private VelocityContainer groupsVC;
	private List<BusinessGroup> groupList;
	private Identity ident;
	private Link showAllLink;
	
	private final BusinessGroupService businessGroupService;
	

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param component
	 */
	public GroupsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) {
		super(wControl, ureq, trans, portletName);

		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		sortingTermsList.add(SortingCriteria.TYPE_SORTING);
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.ident = ureq.getIdentity();
		
		this.groupsVC = this.createVelocityContainer("groupsPortlet");
		showAllLink = LinkFactory.createLink("groupsPortlet.showAll", groupsVC, this);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("groupsPortlet.nogroups"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
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
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("groupsPortlet.type", 1, null, trans.getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT));
		
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(this.sortingCriteria);
     
		this.groupsVC.put("table", tableCtr.getInitialComponent());		
		putInitialPanel(groupsVC);

		// register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
	}
	
	/**
	 * Gets all groups for this portlet and wraps them into PortletEntry impl.
	 * @param ureq
	 * @return the PortletEntry list.
	 */
	private List<PortletEntry> getAllPortletEntries() {
		groupList = businessGroupService.findBusinessGroups(null, identity, true, true, null, 0, -1);
		List<PortletEntry> entries = convertBusinessGroupToPortletEntryList(groupList);
		return entries;
	}
	
	private List<PortletEntry> convertBusinessGroupToPortletEntryList(List<BusinessGroup> items) {
		List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
		Iterator<BusinessGroup> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new GroupPortletEntry(listIterator.next()));
		}
		return convertedList;
	}
	
	protected void reloadModel(SortingCriteria sortingCriteria) {
		if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			groupList = businessGroupService.findBusinessGroups(null, identity, true, true, null, 0, -1);
			groupList = getSortedList(groupList, sortingCriteria);

			List<PortletEntry> entries = convertBusinessGroupToPortletEntryList(groupList);
			
			groupListModel = new GroupTableDataModel(entries);
			tableCtr.setTableDataModel(groupListModel);
		} else {
			reloadModel(this.getPersistentManuallySortedItems());
		}
	}
	
	protected void reloadModel(List<PortletEntry> sortedItems) {						
		groupListModel = new GroupTableDataModel(sortedItems);
		tableCtr.setTableDataModel(groupListModel);
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink) {
			// activate group tab in top navigation
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, GroupsSite.class.getName(), null);
			dts.activateStatic(ureq, GroupsSite.class.getName(), null);
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
					int rowid = te.getRowId();
					BusinessGroup currBusinessGroup = groupListModel.getBusinessGroupAt(rowid);
					boolean isInBusinessGroup = businessGroupService.isIdentityInBusinessGroup(ureq.getIdentity(), currBusinessGroup);
					if(isInBusinessGroup) {
					  BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false);
					} else {
						showInfo("groupsPortlet.no_member");
					}
				}
			}
		}	
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		super.doDispose();
		// de-register for businessgroup type events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
		// POST: all firing event for the source just deregistered are finished
		// (listeners lock in EventAgency)
	}

	public void event(Event event) {
		if (event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent) event;
			// TODO:fj:b this operation should not be too expensive since many other
			// users have to be served also
			// store the event and apply it only when the component validate event is
			// fired.
			// FIXME:fj:a check all such event that they do not say, execute more than
			// 1-2 db queries : 100 listening users -> 100-200 db queries!
			// TODO:fj:b concept of defering that event if this controller here is in
			// the dispatchEvent - code (e.g. DefaultController implements
			// GenericEventListener)
			// -> to avoid rare race conditions like e.g. dispose->deregister and null
			// controllers, but queue is still firing events
			boolean modified = mev.updateBusinessGroupList(groupList, ident);
			if (modified) tableCtr.modelChanged();
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			
			List<PortletEntry> portletEntryList = getAllPortletEntries();			
			PortletDefaultTableDataModel tableDataModel = new GroupsManualSortingTableDataModel(portletEntryList);
			List<PortletEntry> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
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
  private List<PortletEntry> getPersistentManuallySortedItems() {  	
  	List<PortletEntry> portletEntryList = getAllPortletEntries();
		return this.getPersistentManuallySortedItems(portletEntryList);
	}
  
  /**
	 * Comparator implementation used for sorting BusinessGroup entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
  protected Comparator<BusinessGroup> getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator<BusinessGroup>(){			
			public int compare(final BusinessGroup group1, final BusinessGroup group2) {
				int comparisonResult = 0;
			  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(group1.getName(), group2.getName());			  		  	
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = group1.getCreationDate().compareTo(group2.getCreationDate());
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.TYPE_SORTING) {
			  	comparisonResult = group1.getType().compareTo(group2.getType());
			  }
			  if(!sortingCriteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}};
	}
  
  /**
   * 
   * PortletDefaultTableDataModel implementation for the current portlet.
   * 
   * <P>
   * Initial Date:  10.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private class GroupTableDataModel extends PortletDefaultTableDataModel {  	
  	public GroupTableDataModel(List<PortletEntry> objects) {
  		super(objects,2);
  	}
  	
  	public Object getValueAt(int row, int col) {
  		PortletEntry entry = getObject(row);
  		BusinessGroup businessGroup = (BusinessGroup) entry.getValue();
  		switch (col) {
  			case 0:
  				String name = businessGroup.getName();
  				name = StringEscapeUtils.escapeHtml(name).toString();
  				return name;
  			case 1:
  				return getTranslator().translate(businessGroup.getType());
  			default:
  				return "ERROR";
  		}
  	}
  	
  	public BusinessGroup getBusinessGroupAt(int row) {
  		return (BusinessGroup) getObject(row).getValue();
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
	private class GroupsManualSortingTableDataModel extends PortletDefaultTableDataModel  {		
		/**
		 * @param objects
		 * @param locale
		 */
		public GroupsManualSortingTableDataModel(List<PortletEntry> objects) {
			super(objects, 4);
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			PortletEntry portletEntry = getObject(row);
			BusinessGroup group = (BusinessGroup) portletEntry.getValue();
			switch (col) {
				case 0:
					return group.getName();
				case 1:
					String description = group.getDescription();
					description = FilterFactory.getHtmlTagsFilter().filter(description);
					return (description == null ? "n/a" : description);
				case 2:
					String resType = group.getType();					
					return (resType == null ? "n/a" : translate(resType));
				case 3:
					Date date = group.getCreationDate();
					//return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getTranslator().getLocale()).format(date);
					return date;
				default:
					return "error";
			}
		}	
	}
	
	 private class GroupPortletEntry implements PortletEntry {
	  	private BusinessGroup value;
	  	private Long key;
	  	
	  	public GroupPortletEntry(BusinessGroup group) {
	  		value = group;
	  		key = group.getKey();
	  	}
	  	
	  	public Long getKey() {
	  		return key;
	  	}
	  	
	  	public BusinessGroup getValue() {
	  		return value;
	  	}
	  }

}
