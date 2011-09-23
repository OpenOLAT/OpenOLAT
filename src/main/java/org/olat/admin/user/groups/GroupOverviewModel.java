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
* Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/
package org.olat.admin.user.groups;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.group.BusinessGroup;

/**
 * model for details about a group an user is in.
 * Details are: type of group, groupname, 
 * role of user in group (participant, owner, on waiting list), date of joining the group 
 */
class GroupOverviewModel extends DefaultTableDataModel {
	
	private int columnCount = 0;
	/**
	 * @param objects
	 */
	public GroupOverviewModel(List<Object[]> objects, int columnCount) {
		super(objects);
		this.columnCount = columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Object o = getObject(row);
		Object[] dataArray = null;
		dataArray = (Object[]) o;
		
		Object groupColItem = dataArray[col];
		
		switch (col) {
			case 0: 
				return groupColItem;
			case 1: 
				String name = ((BusinessGroup) groupColItem).getName();
				name = StringEscapeUtils.escapeHtml(name).toString();
				return name;
			case 2: 
				return groupColItem;
			case 3:
				return (Date)groupColItem;
			default: 
				return "error";
		}
	}
	
/**
 * method to get the BusinessGroup-Object which is in the model, but 
 * getValueAt() would only return the name of the group.
 * @param row 
 * @return BusinessGroup from a certain row in model
 */
	protected BusinessGroup getBusinessGroupAtRow(int row) {
		Object o = getObject(row);
		Object[] dataArray = null;
		dataArray = (Object[]) o;
		return (BusinessGroup) dataArray[1];
	}

}
