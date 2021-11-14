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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;


/**
 * 
 * Description:<br>
 * its important to provide this controller a tableModel which follows this configuration:
 * column-#	| content			| Condition
 *  1					Title					-
 *  2					Date 					only 2 columns
 *  2/3				Description		3 Columns, but only 2 showed
 *  4					Type					4 columns in model
 * 
 */
public class PortletManualSortingConfigurator<T> extends BasicController {

	private static final String ACTION_MULTISELECT_CHOOSE = "msc";
	private static final String ACTION_MULTISELECT_CANCEL = "cancel";
	
	private Panel tablePanel;
	private VelocityContainer mainVC;		
	private TableController tableController;
	private PortletDefaultTableDataModel<T> tableDataModel;
	
	private List<PortletEntry<T>> sortedItems = new ArrayList<>();
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param fallBackTranslator
	 * @param tableDataModel		=>	See class-description!
	 * @param sortedItems
	 */
	public PortletManualSortingConfigurator(UserRequest ureq, WindowControl wControl, Translator fallBackTranslator, 
			PortletDefaultTableDataModel<T> tableDataModel, List<PortletEntry<T>> sortedItems) {
		super(ureq, wControl, fallBackTranslator);
    this.tableDataModel = tableDataModel;
		this.sortedItems = sortedItems; //select the items in table!!!
		
		mainVC = createVelocityContainer("manualSorting");
		tablePanel = new Panel("table");
		mainVC.put("table", tablePanel);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setMultiSelect(true);
		tableConfig.setSortingEnabled(true); 
		tableConfig.setTableEmptyMessage("manual.sorting.no.entries.found", null, null);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableController);
		int maxNumColumns = tableDataModel.getColumnCount();
		int columnCounter=0; 
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.title", columnCounter++, null, getLocale()));
		if(maxNumColumns==2){ 
			tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.date", columnCounter++, null, getLocale()));
		} else {
			DefaultColumnDescriptor descCol = new DefaultColumnDescriptor("table.manual.sorting.description", columnCounter++, null, getLocale());
			descCol.setEscapeHtml(EscapeMode.antisamy);
			tableController.addColumnDescriptor(descCol);
		}
		if(maxNumColumns==4) {
		  tableController.addColumnDescriptor(new DefaultColumnDescriptor("table.manual.sorting.type", columnCounter++, null, getLocale()));
		}
		tableController.addMultiSelectAction("action.choose", ACTION_MULTISELECT_CHOOSE);
		tableController.addMultiSelectAction("action.cancel", ACTION_MULTISELECT_CANCEL);
		tableController.setTableDataModel(tableDataModel);
		tablePanel.setContent(tableController.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {		
		fireEvent(ureq, event);		
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {		
		if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
			TableMultiSelectEvent multiselectionEvent = (TableMultiSelectEvent) event;
			if (multiselectionEvent.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {				
				//sortedItems = tableDataModel.getObjects(multiselectionEvent.getSelection());
				sortedItems = tableController.getSelectedSortedObjects(multiselectionEvent.getSelection(), tableDataModel);		
				if(sortedItems.size()==0) {
					showWarning("portlet.sorting.manual.empty_sel");
				} else {
				  fireEvent(ureq, event);
				}
			} else if (multiselectionEvent.getAction().equals(ACTION_MULTISELECT_CANCEL)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}		
	}

	public List<PortletEntry<T>> getSortedItems() {
		return sortedItems;
	}
	
}
