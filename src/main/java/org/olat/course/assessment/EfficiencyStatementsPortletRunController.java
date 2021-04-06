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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseModule;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:<br>
 * Run view controller for the efficiency statement list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class EfficiencyStatementsPortletRunController extends AbstractPortletRunController<UserEfficiencyStatementLight> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	private EfficiencyStatementsTableDataModel efficiencyStatementsListModel;
	private VelocityContainer efficiencyStatementsVC;
	private boolean needReloadModel;
	private Identity cOwner;
	private Link showAllLink;
	
	@Autowired
	private EfficiencyStatementManager esm;

	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public EfficiencyStatementsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) { 
		super(wControl, ureq, trans, portletName, defaultMaxEntries);
		this.cOwner = ureq.getIdentity();
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.efficiencyStatementsVC = this.createVelocityContainer("efficiencyStatementsPortlet");
		
		showAllLink = LinkFactory.createLink("efficiencyStatementsPortlet.showAll", efficiencyStatementsVC, this);		
		showAllLink.setIconRightCSS("o_icon o_icon_start");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("efficiencyStatementsPortlet.nostatements"), null, "o_icon_certificate");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
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
	private List<PortletEntry<UserEfficiencyStatementLight>> getAllPortletEntries() {
		List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(getIdentity());	
		List<PortletEntry<UserEfficiencyStatementLight>> portletEntryList = convertEfficiencyStatementToPortletEntryList(efficiencyStatementsList);
		return portletEntryList;
	}
	
	/**
   * Converts a EfficiencyStatement list into a PortletEntry list.
   * @param items
   * @return
   */
  private List<PortletEntry<UserEfficiencyStatementLight>> convertEfficiencyStatementToPortletEntryList(List<UserEfficiencyStatementLight> items) {
		List<PortletEntry<UserEfficiencyStatementLight>> convertedList = new ArrayList<>();
		for(UserEfficiencyStatementLight item:items) {
			if(StringHelper.containsNonWhitespace(item.getShortTitle())) {
				convertedList.add(new EfficiencyStatementPortletEntry(item));
			}
		}
		return convertedList;
	}
			
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, org.olat.core.gui.control.generic.portal.SortingCriteria)
	 */
	protected void reloadModel(SortingCriteria sortingCriteria) {
		if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(getIdentity());

			efficiencyStatementsList = getSortedList(efficiencyStatementsList, sortingCriteria);  		
			List<PortletEntry<UserEfficiencyStatementLight>> entries = convertEfficiencyStatementToPortletEntryList(efficiencyStatementsList);
			efficiencyStatementsListModel = new EfficiencyStatementsTableDataModel(entries,2);
			tableCtr.setTableDataModel(efficiencyStatementsListModel);
		} else {
			reloadModel(getPersistentManuallySortedItems());
		}
	}
	
  /**
   * 
   * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, java.util.List)
   */
	protected void reloadModel(List<PortletEntry<UserEfficiencyStatementLight>> sortedItems) {			
		efficiencyStatementsListModel = new EfficiencyStatementsTableDataModel(sortedItems,2);
		tableCtr.setTableDataModel(efficiencyStatementsListModel);
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			// activate homes tab in top navigation and active calendar menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][effstatements:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
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
					final UserEfficiencyStatementLight statement = efficiencyStatementsListModel.getEfficiencyStatementAt(rowid);
					// will not be disposed on course run dispose, popus up as new browserwindow
					ControllerCreator ctrlCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							CertificateAndEfficiencyStatementController efficiencyCtrl = new CertificateAndEfficiencyStatementController(lwControl, lureq, statement.getArchivedResourceKey());
							return new LayoutMain3ColsController(lureq, getWindowControl(), efficiencyCtrl);
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
	protected PortletToolSortingControllerImpl<UserEfficiencyStatementLight> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			
			List<PortletEntry<UserEfficiencyStatementLight>> portletEntryList = getAllPortletEntries();		
			PortletDefaultTableDataModel<UserEfficiencyStatementLight> tableDataModel = new EfficiencyStatementsManualSortingTableDataModel(portletEntryList, 2);
			List<PortletEntry<UserEfficiencyStatementLight>> sortedItems = getPersistentManuallySortedItems(); 
			
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
	private List<PortletEntry<UserEfficiencyStatementLight>> getPersistentManuallySortedItems() {
		List<PortletEntry<UserEfficiencyStatementLight>> portletEntryList = getAllPortletEntries();		
		return this.getPersistentManuallySortedItems(portletEntryList);		
	}
	
	/**
	 * Comparator implementation.
	 * Compares EfficiencyStatements according with the input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
  protected Comparator<UserEfficiencyStatementLight> getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator<UserEfficiencyStatementLight>(){			
			public int compare(final UserEfficiencyStatementLight s1, final UserEfficiencyStatementLight s2) {	
				int comparisonResult = 0;
				if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
					String st1 = s1.getShortTitle();
					String st2 = s2.getShortTitle();
					if(st2 == null) return -1;
					if(st1 == null) return 1;
					comparisonResult = collator.compare(st1, st2);			  		  	
				} else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
					comparisonResult = s1.getLastModified().compareTo(s2.getLastModified());
				} 
				if(!sortingCriteria.isAscending()) {
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
  private class EfficiencyStatementsTableDataModel extends PortletDefaultTableDataModel<UserEfficiencyStatementLight>  {
  	
  	public EfficiencyStatementsTableDataModel(List<PortletEntry<UserEfficiencyStatementLight>> objects, int numCols) {
  		super(objects, numCols);
  	}
  	  	
  	public Object getValueAt(int row, int col) {
  		UserEfficiencyStatementLight efficiencyStatement = getEfficiencyStatementAt(row);
  		switch (col) {
  			case 0:
  				return efficiencyStatement.getShortTitle();
  			case 1:
  				Float score = efficiencyStatement.getScore();
  				return AssessmentHelper.getRoundedScore(score);
  			case 2:
  				return efficiencyStatement.getPassed();
  			default:
  				return "ERROR";
  		}
  	}
  	
  	public UserEfficiencyStatementLight getEfficiencyStatementAt(int row) {
  		return getObject(row).getValue();
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
  private class EfficiencyStatementsManualSortingTableDataModel extends PortletDefaultTableDataModel<UserEfficiencyStatementLight>  {		
		/**
		 * @param objects
		 * @param locale
		 */
		public EfficiencyStatementsManualSortingTableDataModel(List<PortletEntry<UserEfficiencyStatementLight>> objects, int numCols) {
			super(objects, numCols);
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {			
			PortletEntry<UserEfficiencyStatementLight> entry = getObject(row);
			UserEfficiencyStatementLight statement = entry.getValue();
			switch (col) {
				case 0:					
					return statement.getShortTitle();
				case 1:									
					return statement.getLastModified();
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
  private class EfficiencyStatementPortletEntry implements PortletEntry<UserEfficiencyStatementLight> {
  	private UserEfficiencyStatementLight value;
  	private Long key;
  	
  	public EfficiencyStatementPortletEntry(UserEfficiencyStatementLight efficiencyStatement) {
  		value = efficiencyStatement;
  		key = efficiencyStatement.getCourseRepoKey();
  	}
  	
  	public Long getKey() {
  		return key;
  	}
  	
  	public UserEfficiencyStatementLight getValue() {
  		return value;
  	}
  }
  
}
