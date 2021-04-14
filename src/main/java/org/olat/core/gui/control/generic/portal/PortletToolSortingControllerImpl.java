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
* <p>
*/

package org.olat.core.gui.control.generic.portal;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Controller for providing the necessary tools for a Portlet (e.g. ManualSortingConfig, AutoSortingConfig).
 * This has to be provided by every Portlet to the PortletContainer in order to be added to the PortletContainerToolbox.
 * 
 * <P>
 * Initial Date:  07.11.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class PortletToolSortingControllerImpl<T> extends BasicController 
						 implements PortletToolController<T>, PortletToolSorting {

	public static final String COMMAND_AUTO_SORTING = "a_sort";
	public static final String COMMAND_MANUAL_SORTING = "m_sort";

	private VelocityContainer mainVC;
			
	private boolean isAutoSortable;
	private boolean isManualSortable;
	private SortingCriteria sortingCriteria;
	private List<PortletEntry<T>> sortedItems;
	
	
	//GUI elements
	private Link manualSorting;
	private Link autoSorting;
	private PortletAutoSortingConfigurator portletAutoSortingConfigurator;
	private CloseableModalController closeableModalController;
	private PortletManualSortingConfigurator<T> portletManualSortingConfigurator;
	
	/**
	 * The sorting terms list is configurable.
	 * @param ureq
	 * @param wControl
	 * @param sortingTerms (a list with e.g. SortingCriteria.TYPE_SORTING, SortingCriteria.ALPHABETICAL_SORTING,SortingCriteria.DATE_SORTING)
	 */
	public PortletToolSortingControllerImpl(UserRequest ureq, WindowControl wControl,Translator callerTranslator, SortingCriteria sortingCriteria, 
			PortletDefaultTableDataModel<T> tableDataModel, List<PortletEntry<T>> sortedItems) {
		super(ureq, wControl);
		
		this.sortedItems = sortedItems;
		//this.callerTranslator = callerTranslator;
		mainVC = createVelocityContainer("portletTools");
		mainVC.setDomReplacementWrapperRequired(false);
						
		manualSorting = LinkFactory.createCustomLink("manual.sorting.config", "manual.sorting.config", null, Link.NONTRANSLATED, mainVC, this);
		manualSorting.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_list");
		manualSorting.setElementCssClass("o_portlet_edit_sort_manual");
		if(tableDataModel.getObjects().isEmpty()) {
			manualSorting.setEnabled(false);
		}
				
		autoSorting = LinkFactory.createCustomLink("auto.sorting.config", "auto.sorting.config", null, Link.NONTRANSLATED, mainVC, this);
		autoSorting.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_sort");
		autoSorting.setElementCssClass("o_portlet_edit_sort_auto");
		
		putInitialPanel(mainVC);
		
		portletAutoSortingConfigurator = new PortletAutoSortingConfigurator(ureq, wControl, sortingCriteria);
		portletAutoSortingConfigurator.addControllerListener(this);
						
		portletManualSortingConfigurator = new PortletManualSortingConfigurator<>(ureq, wControl, callerTranslator, tableDataModel, sortedItems);
		portletManualSortingConfigurator.addControllerListener(this);
		
	}
	
	@Override
	protected void doDispose() {		
		if(closeableModalController!=null) {			
			closeableModalController.dispose();
			closeableModalController = null;
		}		
		if(portletAutoSortingConfigurator!=null) {
		  portletAutoSortingConfigurator.dispose();
		  portletAutoSortingConfigurator = null;
		}
		if(portletManualSortingConfigurator!=null) {
			portletManualSortingConfigurator.dispose();
		  portletManualSortingConfigurator = null;
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {	   
		if(source == autoSorting) {			
		  closeableModalController = new CloseableModalController(getWindowControl(), "close", portletAutoSortingConfigurator.getInitialComponent(),
				true,getTranslator().translate("auto.sorting.title"));		  
		  closeableModalController.addControllerListener(this);
		  closeableModalController.activate();
		} else if(source == manualSorting) {
		  closeableModalController = new CloseableModalController(getWindowControl(), "close", portletManualSortingConfigurator.getInitialComponent(),
						true,getTranslator().translate("manual.sorting.title"));		  
			closeableModalController.addControllerListener(this);
			closeableModalController.activate();
		}		
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event)	 {
		if (source == portletAutoSortingConfigurator) {
			if (event==Event.DONE_EVENT) {
				SortingCriteria newSortingCriteria = portletAutoSortingConfigurator.getSortingCriteria();				
				this.sortingCriteria = newSortingCriteria;				
				closeableModalController.deactivate();				
				closeableModalController.dispose();
				fireEvent(ureq, new Event(COMMAND_AUTO_SORTING));
			}
		} else if (source == portletManualSortingConfigurator) {
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {			 
			  this.sortedItems = portletManualSortingConfigurator.getSortedItems();
			  closeableModalController.deactivate();
				closeableModalController.dispose();
			  fireEvent(ureq, new Event(COMMAND_MANUAL_SORTING));
			}
		}
		if (event==Event.CANCELLED_EVENT) {
			closeableModalController.deactivate();
			closeableModalController.dispose();
		} else if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
			closeableModalController.dispose();
		}		
	}

	
	public void setConfigManualSorting(boolean configManualSorting) {		
		mainVC.contextPut("hasManualSorting", new Boolean(configManualSorting));		
	}
	
	public void setConfigAutoSorting(boolean configAutoSorting) {		
		mainVC.contextPut("hasAutoSorting", new Boolean(configAutoSorting));		
	}

	@Override
	public boolean isAutoSortable() {
		return isAutoSortable;
	}

	@Override
	public void setAutoSortable(boolean isAutoSortable) {
		this.isAutoSortable = isAutoSortable;
	}

	@Override
	public boolean isManualSortable() {
		return isManualSortable;
	}

	@Override
	public void setManualSortable(boolean isManualSortable) {
		this.isManualSortable = isManualSortable;
	}

	@Override
	public SortingCriteria getSortingCriteria() {
		return sortingCriteria;
	}
	
	public void setSortingCriteria(SortingCriteria sortingCriteria) {
		this.sortingCriteria = sortingCriteria;
		portletAutoSortingConfigurator.setSortingCriteria(sortingCriteria);
	}	

	@Override
	public List<PortletEntry<T>> getSortedItems() {
		return sortedItems;
	}

}
