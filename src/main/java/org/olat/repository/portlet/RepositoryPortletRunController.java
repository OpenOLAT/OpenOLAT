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

import org.olat.core.commons.persistence.DBFactory;
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
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryTypeColumnDescriptor;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.site.RepositorySite;


/**
 * 
 * Description:<br>
 * Runtime view that shows a list of courses, either as student or teacher
 * 
 * <P>
 * Initial Date:  06.03.2009 <br>
 * @author gnaegi
 */
public class RepositoryPortletRunController extends AbstractPortletRunController<RepositoryEntry> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	private RepositoryPortletTableDataModel repoEntryListModel;
	private VelocityContainer repoEntriesVC;	
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
	public RepositoryPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName, boolean studentView) {
		super(wControl, ureq, trans, portletName);
		this.studentView = studentView;	
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);				
		repoEntriesVC = this.createVelocityContainer("repositoryPortlet");
		showAllLink = LinkFactory.createLink("repositoryPortlet.showAll", repoEntriesVC, this);
			
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("repositoryPortlet.noentry"));
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
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("repositoryPortlet.img", 2, CMD_LAUNCH, trans.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("repositoryPortlet.name", 0, CMD_LAUNCH, trans.getLocale(),	ColumnDescriptor.ALIGNMENT_LEFT));
		
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(this.sortingCriteria);
		repoEntriesVC.put("table", tableCtr.getInitialComponent());

		putInitialPanel(repoEntriesVC);

		// register for businessgroup type events
//FIXME:RH:repo listen to changes
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(BusinessGroup.class));
	}
	
	private List<PortletEntry<RepositoryEntry>> getAllPortletEntries() {
		if (studentView) {
			List<RepositoryEntry> allRepoEntries = RepositoryManager.getInstance().getLearningResourcesAsStudent(getIdentity());
			return convertRepositoryEntriesToPortletEntryList(allRepoEntries);
		} else {
			List<RepositoryEntry> allRepoEntries = RepositoryManager.getInstance().getLearningResourcesAsTeacher(getIdentity());
			return convertRepositoryEntriesToPortletEntryList(allRepoEntries);
		}
	}
	
	private List<RepositoryEntry> getAllEntries() {
		if (studentView) {
			return RepositoryManager.getInstance().getLearningResourcesAsStudent(getIdentity());
		} else {
			return RepositoryManager.getInstance().getLearningResourcesAsTeacher(getIdentity());
		}
	}

	private List<PortletEntry<RepositoryEntry>> convertRepositoryEntriesToPortletEntryList(List<RepositoryEntry> items) {
		List<PortletEntry<RepositoryEntry>> convertedList = new ArrayList<PortletEntry<RepositoryEntry>>();
		for(RepositoryEntry item:items) {
			boolean closed = RepositoryManager.getInstance().createRepositoryEntryStatus(item.getStatusCode()).isClosed();
			if(!closed) {
				convertedList.add(new RepositoryPortletEntry(item));
			}
		}
		return convertedList;
	}
	
	protected void reloadModel(SortingCriteria sortingCriteria) {
		if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			List<RepositoryEntry> items = getAllEntries();
			items = getSortedList(items, sortingCriteria );
			List<PortletEntry<RepositoryEntry>> entries = convertRepositoryEntriesToPortletEntryList(items);
			repoEntryListModel = new RepositoryPortletTableDataModel(entries, getLocale());
			tableCtr.setTableDataModel(repoEntryListModel);
		} else {
			reloadModel(getPersistentManuallySortedItems());
		}
	}
	
	protected void reloadModel(List<PortletEntry<RepositoryEntry>> sortedItems) {						
		repoEntryListModel = new RepositoryPortletTableDataModel(sortedItems, getLocale());
		tableCtr.setTableDataModel(repoEntryListModel);
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			// activate learning resource tab in top navigation and active my courses menu item
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			//attach controller / action extension dynamically to lr-site
			if (studentView) {
				dts.activateStatic(ureq, RepositorySite.class.getName(), "search.mycourses.student");
			} else {
				dts.activateStatic(ureq, RepositorySite.class.getName(), "search.mycourses.teacher");
			}			
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
					int rowId = te.getRowId();
					RepositoryEntry repoEntry = repoEntryListModel.getRepositoryEntry(rowId);
					// refresh repo entry, attach to hibernate session
					repoEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repoEntry);
					// get run controller fro this repo entry and launch it in new tab
					DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
					//was brasato:: DTabs dts = wControl.getDTabs();
					DTab dt = dts.getDTab(repoEntry.getOlatResource());
					if (dt == null) {
						// does not yet exist -> create and add
						//fxdiff BAKS-7 Resume function
						dt = dts.createDTab(repoEntry.getOlatResource(), repoEntry, repoEntry.getDisplayname());
						// tabs full
						if (dt != null) {
							Controller runCtr = RepositoyUIFactory.createLaunchController(repoEntry, null, ureq, dt.getWindowControl());					
							dt.setController(runCtr);
							dts.addDTab(dt);
							dts.activate(ureq, dt, null); // null: do not activate to a certain view
						}
					} else {
						dts.activate(ureq, dt, null);
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
//FIXME:RH:repo listen to changes
		// de-register for businessgroup type events
//		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(BusinessGroup.class));
		// POST: all firing event for the source just deregistered are finished
		// (listeners lock in EventAgency)
	}

	public void event(Event event) {
//FIXME:RH:repo listen to changes
//		if (event instanceof BusinessGroupModifiedEvent) {
//			BusinessGroupModifiedEvent mev = (BusinessGroupModifiedEvent) event;
//			// TODO:fj:b this operation should not be too expensive since many other
//			// users have to be served also
//			// store the event and apply it only when the component validate event is
//			// fired.
//			// FIXME:fj:a check all such event that they do not say, execute more than
//			// 1-2 db queries : 100 listening users -> 100-200 db queries!
//			// TODO:fj:b concept of defering that event if this controller here is in
//			// the dispatchEvent - code (e.g. DefaultController implements
//			// GenericEventListener)
//			// -> to avoid rare race conditions like e.g. dispose->deregister and null
//			// controllers, but queue is still firing events
//			boolean modified = mev.updateBusinessGroupList(groupList, ident);
//			if (modified) tableCtr.modelChanged();
//		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<RepositoryEntry> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			List<PortletEntry<RepositoryEntry>> portletEntryList = getAllPortletEntries();			
			PortletDefaultTableDataModel<RepositoryEntry> tableDataModel = new RepositoryPortletTableDataModel(portletEntryList, ureq.getLocale());
			List<PortletEntry<RepositoryEntry>> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl<RepositoryEntry>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
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
  private List<PortletEntry<RepositoryEntry>> getPersistentManuallySortedItems() {  	
  	List<PortletEntry<RepositoryEntry>> portletEntryList = getAllPortletEntries();
		return this.getPersistentManuallySortedItems(portletEntryList);
	}
  
  /**
	 * Comparator implementation used for sorting BusinessGroup entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
  protected Comparator<RepositoryEntry> getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator<RepositoryEntry>(){			
			public int compare(final RepositoryEntry repoEntry1, final RepositoryEntry repoEntry2) {
				int comparisonResult = 0;
			  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(repoEntry1.getDisplayname(), repoEntry2.getDisplayname());			  		  	
			  }
			  if(!sortingCriteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}};
	}
  
}
