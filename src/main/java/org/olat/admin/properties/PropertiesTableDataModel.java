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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.properties.Property;
import org.olat.user.UserManager;

/**
*  Description:<br>
*
*
* @author Alexander Schneider
*/
public class PropertiesTableDataModel extends DefaultTableDataModel<Property> {

	private final UserManager userManager;
	
	/**
	 * Default constructor.
	 */
	public PropertiesTableDataModel() {
		this(new ArrayList<Property>());
	}

	/**
	 * Initialize table model with objects.
	 * @param objects
	 */
	public PropertiesTableDataModel(List<Property> objects) {
		super(objects);
		userManager = UserManager.getInstance();
	}

	@Override
	public int getColumnCount() {
		// resourceTypeName, resourceTypeId, category, name, floatValue, stringValue, textValue
		return 11;
	}
	
	@Override
	public final Object getValueAt(int row, int col) {
		Property p = getObject(row); 
		switch(col) {
			case 0: return userManager.getUserDisplayName(p.getIdentity());
			case 1: return p.getResourceTypeName();
			case 2: return p.getResourceTypeId();
			case 3: return p.getCategory();
			case 4: return p.getName();
			case 5: return p.getFloatValue();
			case 6: return p.getStringValue();
			case 7: return p.getTextValue();
			case 8: return p.getCreationDate().toString();
			case 9: return p.getLastModified().toString();
			case 10: return p.getLongValue();
			default: return "error";
		}
	}
}
