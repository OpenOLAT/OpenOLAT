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

package org.olat.user;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Initial Date:  Jul 29, 2003
 * 
 *  @author Florian Gnaegi
 *  
 *  
 */
public class UserPropertiesController extends BasicController {

	private PropTableDataModel tdm;
	private TableController tableCtr;
	private DialogBoxController confirmDeleteCtrl;
	
	@Autowired
	private PropertyManager pm;
	@Autowired
	private UserManager userManager;

	/*
	 * the identity that is displayed (not the user/admin, that views the properties)
	 */
	private Identity displayedIdentity;
	private boolean identityAdministator;

	/**
	 * Administer properties of a user.
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public UserPropertiesController(UserRequest ureq, WindowControl wControl, Identity displayedIdentity, Roles displayedRoles) {
		super(ureq, wControl);
		this.displayedIdentity = displayedIdentity;
		identityAdministator = ureq.getUserSession().getRoles().isManagerOf(OrganisationRoles.administrator, displayedRoles);

		List<Property> l = pm.listProperties(displayedIdentity, null, null, null, null);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(getTranslator().translate("error.no.props.found"), null, TableController.TABLE_EMPTY_ICON);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.category", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.grp", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.resource", 2, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.name", 3, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.value", 4, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.creatdat", 5, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.prop.moddat", 6, null, getLocale()));
		// property selection / id only for admins
		if (identityAdministator) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.action", translate("delete")));
		}
		tdm = new PropTableDataModel(l);
		tableCtr.setTableDataModel(tdm);
		putInitialPanel(tableCtr.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals("delete") && identityAdministator) {
					Property foundProp = tdm.getObject(te.getRowId());
					String fullName = userManager.getUserDisplayName(displayedIdentity);
					confirmDeleteCtrl = activateYesNoDialog(ureq, translate("propdelete.yesno.title"), translate("propdelete.yesno.text", new String[]{foundProp.getName(), fullName}), confirmDeleteCtrl);
					confirmDeleteCtrl.setUserObject(foundProp);
				}
			}
		} else if (confirmDeleteCtrl == source&& DialogBoxUIFactory.isYesEvent(event)) {
			Property foundProp = (Property)confirmDeleteCtrl.getUserObject();
			pm.deleteProperty(foundProp);
			tdm.getObjects().remove(foundProp);
			tableCtr.modelChanged();
		}
	}
}
/**
 *  Comment:  
 *  The prop table data model. 
 *  
 */
class PropTableDataModel extends DefaultTableDataModel<Property> {

	/**
	 * Table model holding list of properties.
	 * @param objects
	 */
	public PropTableDataModel(List<Property> objects) {
		super(objects);
	}

	@Override
	public final Object getValueAt(int row, int col) {
		Property p = getObject(row);
		switch (col) {
			case 0 :
				String cat = p.getCategory();
				return (cat == null ? "n/a" : cat);
			case 1 :
				BusinessGroup grp = p.getGrp();
				return (grp == null ? "n/a" : grp.getKey().toString());
			case 2 :
				String resType = p.getResourceTypeName();
				return (resType == null ? "n/a" : resType);
			case 3 :
				String name = p.getName();
				return (name == null ? "n/a" : name);
			case 4 :
				Float floatvalue = p.getFloatValue();
				Long longvalue = p.getLongValue();
				String stringvalue = p.getStringValue();
				String textvalue = p.getTextValue();
				String val;
				if (floatvalue != null)
					val = floatvalue.toString();
				else if (longvalue != null)
					val = longvalue.toString();
				else if (stringvalue != null)
					val = stringvalue;
				else if (textvalue != null)
					val = textvalue;
				else val = "n/a";
				return val;
			case 5 :
				Date dateCD = p.getCreationDate();
				return (dateCD == null ? new Date() : dateCD);
			case 6 :
				Date dateLM = p.getLastModified();
				return (dateLM == null ? new Date() : dateLM);
			default :
				return "error";
		}
	}

	@Override
	public int getColumnCount() {
		return 7;
	}
}