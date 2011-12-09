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

package org.olat.ims.qti;


import java.util.List;

import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentHelper;

/**
 * Initial Date:  12.01.2005
 *
 * @author Mike Stock
 */
public class QTIResultTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {

	private static final int COLUMN_COUNT = 3;
	private List resultSets;
	
	/**
	 * @param resultSets
	 */
	public QTIResultTableModel(List resultSets) {
		this.resultSets = resultSets;
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	public int getRowCount() {
		return resultSets.size();
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		QTIResultSet resultSet = (QTIResultSet)resultSets.get(row);
		switch (col) {
			case 0: return resultSet.getLastModified();
			case 1: {
				if (resultSet.getDuration() == null) {
					// fix for old testsets generated previously to duration introduction
					return "n/a";
				} else {
					return Formatter.formatDuration(resultSet.getDuration().longValue());
				}
			}
			case 2: return "" + AssessmentHelper.getRoundedScore(resultSet.getScore());
			default: return "error";
		}	
	}

	/**
	 * @param rowId
	 * @return result set
	 */
	public QTIResultSet getResultSet(int rowId) {
		return (QTIResultSet)resultSets.get(rowId);
	}

}
