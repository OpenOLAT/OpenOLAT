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
* <p>
*/ 

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseModule;
import org.olat.home.site.HomeSite;


/**
 * Description:<br>
 * Run view controller for the efficiency statement list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class EfficiencyStatementsPortletRunController extends AbstractPortletRunController implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	//private EfficiencyStatementsListModel efficiencyStatementsListModel;
	private EfficiencyStatementsTableDataModel efficiencyStatementsListModel;
	private VelocityContainer efficiencyStatementsVC;
	private boolean needReloadModel;
	private Identity cOwner;
	private Link showAllLink;
	private OLog log = Tracing.createLoggerFor(EfficiencyStatementsPortletRunController.class);

	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public EfficiencyStatementsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) { 
		super(wControl, ureq, trans, portletName);
		this.cOwner = ureq.getIdentity();
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.efficiencyStatementsVC = this.createVelocityContainer("efficiencyStatementsPortlet");
		
		showAllLink = LinkFactory.createLink("efficiencyStatementsPortlet.showAll", efficiencyStatementsVC, this);		
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("efficiencyStatementsPortlet.nostatements"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
    //disable the default sorting for this table
		tableConfig.setSortingEnabled(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.header.course", 0, CMD_LAUNCH, trans.getLocale()); 
		cd0.setIsPopUpWindowAction(true, "height=600, width=800, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		tableCtr.addColumnDescriptor(cd0);
		
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(sortingCriteria);

		this.efficiencyStatementsVC.put("table", tableCtr.getInitialComponent());

		ComponentUtil.registerForValidateEvents(efficiencyStatementsVC, this);
		putInitialPanel(efficiencyStatementsVC);
		
		CourseModule.registerForCourseType(this, ureq.getIdentity());
	}
	
	/**
	 * Gets all EfficiencyStatements for this portlet and wraps them into PortletEntry impl.
	 * @param ureq
	 * @return the PortletEntry list.
	 */
	private List<PortletEntry> getAllPortletEntries() {
		List efficiencyStatementsList = EfficiencyStatementManager.getInstance().findEfficiencyStatements(identity);	
		List<PortletEntry> portletEntryList = this.convertEfficiencyStatementToPortletEntryList(efficiencyStatementsList);
		return portletEntryList;
	}
	
	/**
   * Converts a EfficiencyStatement list into a PortletEntry list.
   * @param items
   * @return
   */
  private List<PortletEntry> convertEfficiencyStatementToPortletEntryList(List<EfficiencyStatement> items) {
		List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
		Iterator<EfficiencyStatement> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new EfficiencyStatementPortletEntry(listIterator.next()));
		}
		return convertedList;
	}
			
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, org.olat.core.gui.control.generic.portal.SortingCriteria)
	 */
  protected void reloadModel(SortingCriteria sortingCriteria) {
  	if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
  		EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
  		List efficiencyStatementsList = esm.findEfficiencyStatements(identity);

  		efficiencyStatementsList = getSortedList(efficiencyStatementsList, sortingCriteria);  		
  		List<PortletEntry> entries = convertEfficiencyStatementToPortletEntryList(efficiencyStatementsList);
  		efficiencyStatementsListModel = new EfficiencyStatementsTableDataModel(entries,2);
  		tableCtr.setTableDataModel(efficiencyStatementsListModel);
  		tableCtr.setTableDataModel(efficiencyStatementsListModel);
		} else {
			reloadModel(this.getPersistentManuallySortedItems());
		}
  }
	
  /**
   * 
   * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, java.util.List)
   */
	protected void reloadModel(List<PortletEntry> sortedItems) {			
		efficiencyStatementsListModel = new EfficiencyStatementsTableDataModel(sortedItems,2);
		tableCtr.setTableDataModel(efficiencyStatementsListModel);
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			// activate homes tab in top navigation and active efficiencyStatements menu item
			//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), "efficiencyStatements");
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			dts.activateStatic(ureq, HomeSite.class.getName(), "efficiencyStatements");
		} else if (event == ComponentUtil.VALIDATE_EVENT && needReloadModel) {			
			reloadModel(sortingCriteria);
		}
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					final EfficiencyStatement efficiencyStatement = efficiencyStatementsListModel.getEfficiencyStatementAt(rowid);
					// will not be disposed on course run dispose, popus up as new browserwindow
					ControllerCreator ctrlCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new EfficiencyStatementController(lwControl, lureq, efficiencyStatement.getCourseRepoEntryKey());
						}					
					};
					//wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
					//open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
					//
				}
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		CourseModule.deregisterForCourseType(this);
		super.doDispose();
	}
	
	/**
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof AssessmentChangedEvent) {
			AssessmentChangedEvent ace = (AssessmentChangedEvent)event;
			
			if (cOwner.getKey().equals(ace.getIdentityKey()) && 
					ace.getCommand().equals(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED)) {
				needReloadModel = true;
			}
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl istance.
	 */
	protected PortletToolSortingControllerImpl createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			
			List<PortletEntry> portletEntryList = getAllPortletEntries();		
			PortletDefaultTableDataModel tableDataModel = new EfficiencyStatementsManualSortingTableDataModel(portletEntryList, 2, ureq.getLocale());
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
	 * Comparator implementation.
	 * Compares EfficiencyStatements according with the input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
  protected Comparator getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator(){			
			public int compare(final Object o1, final Object o2) {
				EfficiencyStatement statement1 = (EfficiencyStatement)o1;
				EfficiencyStatement statement2 = (EfficiencyStatement)o2;		
				int comparisonResult = 0;
			  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(statement1.getCourseTitle(), statement2.getCourseTitle());			  		  	
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = Long.valueOf(statement1.getLastUpdated()).compareTo(Long.valueOf(statement2.getLastUpdated()));
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
  private class EfficiencyStatementsTableDataModel extends PortletDefaultTableDataModel  {
  	
  	public EfficiencyStatementsTableDataModel(List<PortletEntry> objects, int numCols) {
  		super(objects, numCols);
  	}
  	  	
  	public Object getValueAt(int row, int col) {
  		EfficiencyStatement efficiencyStatement = (EfficiencyStatement)this.getObject(row).getValue();
  		List nodeData = efficiencyStatement.getAssessmentNodes();
  		Map rootNode = (Map) nodeData.get(0);
  		switch (col) {
  			case 0:
  				return StringEscapeUtils.escapeHtml(efficiencyStatement.getCourseTitle());
  			case 1:
  				return rootNode.get(AssessmentHelper.KEY_SCORE);
  			case 2:
  				return rootNode.get(AssessmentHelper.KEY_PASSED);
  			default:
  				return "ERROR";
  		}
  	}
  	
  	public EfficiencyStatement getEfficiencyStatementAt(int row) {
  		return (EfficiencyStatement)this.getObject(row).getValue();
  	}
  }
  
  /**
   * 
   * PortletDefaultTableDataModel implementation for the manual sorting component.
   * 
   * <P>
   * Initial Date:  05.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private class EfficiencyStatementsManualSortingTableDataModel extends PortletDefaultTableDataModel  {		
		private Locale locale;
		/**
		 * @param objects
		 * @param locale
		 */
		public EfficiencyStatementsManualSortingTableDataModel(List<PortletEntry> objects, int numCols, Locale locale) {
			super(objects, numCols);
			this.locale = locale;
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {			
			PortletEntry entry = getObject(row);
			EfficiencyStatement statement = (EfficiencyStatement)entry.getValue();
			switch (col) {
				case 0:					
					return statement.getCourseTitle();
				case 1:									
					Date lastUpdate = new Date(statement.getLastUpdated());
					//return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getTranslator().getLocale()).format(lastUpdate);
					return lastUpdate;
				default:
					return "error";
			}
		}		
	}
  
  /**
   * 
   * PortletEntry implementation for EfficiencyStatement objects.
   * 
   * <P>
   * Initial Date:  07.12.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private class EfficiencyStatementPortletEntry implements PortletEntry {
  	private EfficiencyStatement value;
  	private Long key;
  	
  	public EfficiencyStatementPortletEntry(EfficiencyStatement efficiencyStatement) {
  		value = efficiencyStatement;
  		key = efficiencyStatement.getCourseRepoEntryKey();
  	}
  	
  	public Long getKey() {
  		return key;
  	}
  	
  	public EfficiencyStatement getValue() {
  		return value;
  	}
  }
  
}
