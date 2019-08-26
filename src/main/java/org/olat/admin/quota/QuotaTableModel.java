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

package org.olat.admin.quota;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.util.vfs.Quota;

/**
 * Initial Date: Mar 30, 2004
 * @author Mike Stock 
 */
public class QuotaTableModel extends BaseTableDataModelWithoutFilter<Quota> {

	private List<Quota> quotaList;

	@Override
	public void setObjects(List<Quota> objects) {
		quotaList = new ArrayList<>(objects);
	}

	/**
	 * @param row
	 * @return Quota.
	 */
	public Quota getRowData(int row) {
		return quotaList.get(row);
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return quotaList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Quota q = quotaList.get(row);
		switch (col) {
			case 0:
				return q.getPath();
			case 1:
				return q.getQuotaKB();
			case 2:
				return q.getUlLimitKB();
			case 3:
				return "Choose";
			default:
				return "error";
		}
	}
}