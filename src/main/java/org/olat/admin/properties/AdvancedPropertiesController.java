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

package org.olat.admin.properties;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
*  Description:<br>
*  is the controller for
*
* @author Felix Jost
*/
public class AdvancedPropertiesController extends BasicController {

	private Panel myPanel;
	private AdvancedPropertySearchForm searchForm;
	private VelocityContainer vcSearchForm;
	
	private TableController tableCtr;
	
	/**
	 * caller of this constructor must make sure only olat admins come here
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public AdvancedPropertiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);
		
		myPanel = new Panel("myPanel");
		myPanel.addListener(this);
		
		searchForm = new AdvancedPropertySearchForm(ureq, wControl);
		listenTo(searchForm);
		
		vcSearchForm = createVelocityContainer("searchForm");
		vcSearchForm.put("searchForm",searchForm.getInitialComponent());
		myPanel.setContent(vcSearchForm);
		putInitialPanel(myPanel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchForm && event == Event.DONE_EVENT) {
				
				String resourceTypeName = searchForm.getResourceTypeName();
				String resourceTypeId = searchForm.getResourceTypeId();
				Long resTypeId = null;
				if (resourceTypeId != null && !resourceTypeId.equals("")) resTypeId = Long.valueOf(resourceTypeId);
				String category = searchForm.getCategory();
				if (category != null && category.equals("")) category = null;
				String propertyName = searchForm.getPropertyName();
				if (propertyName != null && propertyName.equals("")) propertyName = null;

				List<Property> entries = PropertyManager.getInstance().listProperties(searchForm.getIdentity(), null, resourceTypeName, resTypeId, category, propertyName);
				
				
				PropertiesTableDataModel ptdm = new PropertiesTableDataModel(entries);

				TableGuiConfiguration tableConfig = new TableGuiConfiguration();
				
				removeAsListenerAndDispose(tableCtr);
				tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
				//use null as listener argument because we are using listenTo(..) from basiccontroller
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.userName", 0, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.resourceTypeName", 1, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.resourceTypeId", 2, null, getLocale(),ColumnDescriptor.ALIGNMENT_RIGHT));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.category", 3, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.name", 4, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.floatValue", 5, null, getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.stringValue", 6, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.longValue", 10, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.textValue", 7, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.creationdate", 8, null, getLocale()));
				tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastmodified", 9, null, getLocale()));
				tableCtr.setTableDataModel(ptdm);
				listenTo(tableCtr);

				myPanel.setContent(tableCtr.getInitialComponent());		
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
