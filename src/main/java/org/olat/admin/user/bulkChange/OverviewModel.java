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
package org.olat.admin.user.bulkChange;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 * Description:<br>
 * simple model for an overview at last step
 * maybe some changes are needed, if other icons, were inserted.
 * <P>
 * Initial Date: 29.02.2008 <br>
 * 
 * @author rhaag
 */
public class OverviewModel extends DefaultTableDataModel<List<String>> {

	private int columnCount = 0;

	public OverviewModel(List<List<String>> objects, int columnCount) {
		super(objects);
		this.columnCount = columnCount;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		List<String> dataArray = getObject(row);
		String value = dataArray.get(col);
		return (value == null ? "n/a" : value);
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new OverviewModel(new ArrayList<List<String>>(), columnCount);
	}
}
