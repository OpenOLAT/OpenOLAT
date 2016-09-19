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

package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.components.table.TableDataModel;

/**
 * Table data model including column models.
 * @author Christian Guretzki
 */
public class FlexiTableDataModelImpl<T> implements FlexiTableDataModel<T> {
	
	private TableDataModel<T> tableModel;
	private FlexiTableColumnModel tableColumnModel;

	/**
	 * Constructor to create a FlexiTableDataModel with a non-flexi table data model as input.
	 * @param tableModel        non-flexi table data model
	 * @param tableColumnModel  table-column-model (all columns)
	 */
	public FlexiTableDataModelImpl(TableDataModel<T> tableModel, FlexiTableColumnModel tableColumnModel ) {
		this.tableModel = tableModel;
		this.tableColumnModel = tableColumnModel;
	}
	
	@Override
	public boolean isSelectable(int row) {
		return true;
	}

	/**
	 * @return Number of table row.
	 */
	@Override
	public int getRowCount() {
		return tableModel.getRowCount();
	}
	
	@Override
	public boolean isRowLoaded(int row) {
		return row < tableModel.getRowCount();
	}

	@Override
	public T getObject(int row) {
		return tableModel.getObject(row);
	}

	/**
	 * Return Object for certain table cell.
	 * @param row Row number [0...row]
	 * @param col column number [0...column]
	 * @return Object for certain table cell
	 */
	@Override
	public Object getValueAt(int row, int col) {
		return tableModel.getValueAt(row, col);
	}

	/**
	 * Return table-column-model (all columns) for this table-data-model.
	 * @return table-column-model
	 */
	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return tableColumnModel;
	}

	/**
	 * Set table-column-model (all columns) for this table-data-model.
	 */
	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.tableColumnModel = tableColumnModel;
	}

}