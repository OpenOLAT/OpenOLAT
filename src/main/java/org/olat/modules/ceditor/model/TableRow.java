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
package org.olat.modules.ceditor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableRow {
	
	public List<TableColumn> columns;

	public List<TableColumn> getColumns() {
		if(columns == null) {
			columns = new ArrayList<>();
		}
		return columns;
	}

	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}
	
	public TableColumn getColumn(int col) {
		if(columns == null || col >= columns.size()) return null;
		return columns.get(col);
	}
	
	protected void ensureColumns(int numOfColumns) {
		if(columns == null) {
			columns = new ArrayList<>();
		}
		if(columns.size() < numOfColumns) {
			for(int i=columns.size(); i<numOfColumns; i++) {
				columns.add(new TableColumn());
			}
		}
	}
}
