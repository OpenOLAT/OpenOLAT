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

package org.olat.course.area;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.area.BGArea;

/**
 * Description:<BR>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public class BGAreaTableModel extends DefaultTableDataModel<BGArea> {
	private static final int COLUMN_COUNT = 3;

	/**
	 * @param owned list of group areas
	 * @param translator
	 */
	public BGAreaTableModel(List<BGArea> owned) {
		super(owned);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		BGArea area = getObject(row);
		switch (col) {
			case 0:
				return area.getName();
			case 1:
				String description = area.getDescription();
				description = FilterFactory.getHtmlTagAndDescapingFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			default:
				return "ERROR";
		}
	}

	@Override
	public BGAreaTableModel createCopyWithEmptyList() {
		return new BGAreaTableModel(new ArrayList<BGArea>());
	}
	
	
}