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
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/

package org.olat.user.propertyhandlers.ui;

import java.util.List;
import java.util.Map.Entry;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;

/**
 * 
 * Description:<br>
 * simple TableModel that wraps UserPropertyContext-entries
 * 
 * <P>
 * Initial Date:  29.08.2011 <br>
 * @author strentini
 */
public class UsrPropContextCfgTableModel extends DefaultTableDataModel<Entry<String, UserPropertyUsageContext>> {

	public UsrPropContextCfgTableModel(List<Entry<String, UserPropertyUsageContext>> objects) {
		super(objects);
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Entry<String, UserPropertyUsageContext> contextRow = objects.get(row);
		UserPropertyUsageContext ucontext = contextRow.getValue();
		switch (col) {
			case 0:return contextRow.getKey();
			case 1: return ucontext.getDescription();
			case 2:return ucontext.getPropertyHandlers().size();
			default: return "-";
		}
	}
}
