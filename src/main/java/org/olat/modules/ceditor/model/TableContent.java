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

import jakarta.persistence.Transient;

/**
 * 
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableContent {
	
	private int numOfRows;
	private int numOfColumns;
	private List<TableRow> rows;
	
	private String title;
	private String caption;
	
	/**
	 * Create a table with default size 3 x 4
	 * 
	 */
	public TableContent() {
		numOfRows = 3;
		numOfColumns = 4;
	}
	
	public TableContent(int numOfRows, int numOfColumns) {
		this.numOfRows = numOfRows;
		this.numOfColumns = numOfColumns;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return A copy of the list
	 */
	public List<TableRow> getRows() {
		ensureRows();
		
		List<TableRow> rowList;
		if(rows.size() > numOfRows) {
			rowList = new ArrayList<>(rows.subList(0, numOfRows));// don't delete possible data
		} else {
			rowList = new ArrayList<>(rows);
		}
		return rowList;
	}
	
	private void ensureRows() {
		if(rows == null) {
			rows = new ArrayList<>();
		}

		if(rows.size() < numOfRows) {
			for(int i=rows.size(); i<numOfRows; i++) {
				rows.add(new TableRow());
			}
		}
	}

	public void setRows(List<TableRow> rows) {
		this.rows = rows;
	}
	
	public TableColumn getColumn(int row, int col) {
		if(row >= numOfRows) return null;
		
		ensureRows();
		TableRow tableRow = rows.get(row);
		tableRow.ensureColumns(numOfColumns);
		return tableRow.getColumn(col);
	}
	
	public int getNumOfRows() {
		return numOfRows;
	}
	
	public void setNumOfRows(int numOfRows) {
		if(numOfRows < 1) return;
		this.numOfRows = numOfRows;	
	}

	public int getNumOfColumns() {
		return numOfColumns;
	}

	public void setNumOfColumns(int numOfColumns) {
		if(numOfColumns < 1) return;
		this.numOfColumns = numOfColumns;
	}
	
	public String getContent(int row, int col) {
		TableColumn tableColumn = getColumn(row, col);
		return tableColumn == null ? null : tableColumn.getContent();
	}

	@Transient
	public void addContent(int row, int col, String text) {
		TableColumn tableColumn = getColumn(row, col);
		if(tableColumn != null) {
			 tableColumn.setContent(text);
		}
	}
}
