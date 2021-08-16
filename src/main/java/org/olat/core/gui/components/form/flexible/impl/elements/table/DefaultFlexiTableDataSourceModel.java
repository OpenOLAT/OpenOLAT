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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.table.TableDataModel;

/**
 * 
 * Delegate the source to someone else.
 * 
 * Don't forget to implement the getObject(int row, int col) method :-)
 * 
 * Initial date: 30.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 * @param <U>
 */
public abstract class DefaultFlexiTableDataSourceModel<U> implements FlexiTableDataSource<U>, TableDataModel<U> {
	private List<U> rows;
	private FlexiTableColumnModel columnModel;
	private FlexiTableDataSourceDelegate<U> sourceDelegate;
	
	private int rowCount;
	
	public DefaultFlexiTableDataSourceModel(FlexiTableDataSourceDelegate<U> sourceDelegate, FlexiTableColumnModel columnModel) {
		this.rows = new ArrayList<>(100);
		this.sourceDelegate = sourceDelegate;
		this.columnModel = columnModel;
	}
	
	@Override
	public boolean isSelectable(int row) {
		return true;
	}

	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}
	
	@Override
	public U getObject(int row) {
		if(isRowLoaded(row)) {
			return rows.get(row);
		}
		return null;
	}
	
	public U getPreviousObject(U refObject, FlexiTableElement tableEl) {
		int  index = getIndexOfObject(refObject);
		U previousObject = null;
		if(index > 0 && !rows.isEmpty()) {
			if(!isRowLoaded(index - 1)) {
				tableEl.preloadPageOfObjectIndex(index - 1);
			}
			if(index - 1 < 0) {
				previousObject = rows.get(rows.size() - 1);
			} else {
				previousObject = rows.get(index - 1);
			}
		}
		return previousObject;
	}
	
	public U getNextObject(U refObject, FlexiTableElement tableEl) {
		int  index = getIndexOfObject(refObject);
		U nextObject = null;
		if(index >= 0 && !rows.isEmpty()) {
			if(!isRowLoaded(index + 1)) {
				tableEl.preloadPageOfObjectIndex(index + 1);
			}
			if((index + 1) < rows.size()) {
				nextObject = rows.get(index + 1);
			} else {
				nextObject = rows.get(0);
			}
		}
		return nextObject;
	}
	
	public int getIndexOfObject(U object) {
		if(object != null && rows != null) {
			return rows.indexOf(object);
		}
		return -1;
	}
	
	public FlexiTableDataSourceDelegate<U> getSourceDelegate() {
		return sourceDelegate;
	}
	
	public void setSource(FlexiTableDataSourceDelegate<U> sourceDelegate) {
		this.sourceDelegate = sourceDelegate;
	}
	
	public List<U> getObjects() {
		return rows;
	}
	
	@Override
	public void setObjects(List<U> objects) {
		this.rows = new ArrayList<>(objects);
	}
	
	@Override
	public abstract DefaultFlexiTableDataSourceModel<U> createCopyWithEmptyList();
	
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	@Override
	public boolean isRowLoaded(int row) {
		return rows != null && row >= 0 && row < rows.size() && rows.get(row) != null;
	}

	@Override
	public void clear() {
		rowCount = 0;
		if(rows != null) {
			rows.clear();
		}
	}

	@Override
	public void reload(List<Integer> rowIndex) {
		if(rowIndex == null || rowIndex.isEmpty()) return;
		
		List<U> rowToUpdate = new ArrayList<>(rowIndex.size());
		for(Integer index:rowIndex) {
			int row = index.intValue();
			if(isRowLoaded(row)) {
				U object = getObject(row);
				if(object != null) {
					rowToUpdate.add(object);
				}
			}
		}
	
		List<U> updatedRows = sourceDelegate.reload(rowToUpdate);
		Map<U,U> updatedRowsMap = new HashMap<>();
		for(U updatedRow:updatedRows) {
			updatedRowsMap.put(updatedRow, updatedRow);
		}
		for(int i=0; i<rows.size(); i++) {
			U row = rows.get(i);
			if(updatedRowsMap.containsKey(row)) {
				rows.set(i, updatedRowsMap.get(row));
			}
		}
	}

	@Override
	public ResultInfos<U> load(String query, List<FlexiTableFilter> filters, int firstResult, int maxResults, SortKey... orderBy) {
		return loadDatas(query, filters, false, firstResult, maxResults, orderBy);
	}
	
	private ResultInfos<U> loadDatas(String query, List<FlexiTableFilter> filters, final boolean force, final int firstResult, final int maxResults, SortKey... orderBy) {
		if(rows == null) {
			rows = new ArrayList<>();
		}
		for(int i=rows.size(); i<firstResult; i++) {
			rows.add(null);
		}

		int correctedFirstResult = firstResult;
		int correctMaxResults = maxResults;

		if(!force && !rows.isEmpty()) {
			correctMaxResults = maxResults <= 0 ? rowCount : maxResults;
			int maxRowsResults = maxResults <= 0 ? (rowCount - firstResult) : maxResults;
			for(int i=firstResult; i<maxRowsResults && i<rows.size(); i++) {
				if(rows.get(i) == null) {
					break;
				} else {
					correctedFirstResult++;
					correctMaxResults--;
				}
			}
			//check if all datas are loaded
			if(correctMaxResults == 0) {
				return new DefaultResultInfos<>(rowCount, rowCount, rows); 
			}
		}
		
		ResultInfos<U> newRows = sourceDelegate.getRows(query, filters, correctedFirstResult, correctMaxResults, orderBy);
		if(firstResult == 0) {
			if(newRows.getObjects().size() < correctMaxResults) {
				rowCount = newRows.getObjects().size();
			} else if(newRows.getCorrectedRowCount() >= 0) {
				rowCount = newRows.getCorrectedRowCount();
			} else {
				rowCount = sourceDelegate.getRowCount();
			}
		} else if(newRows.getCorrectedRowCount() >= 0) {
			rowCount = newRows.getCorrectedRowCount();
		} else if(rowCount == 0 && !newRows.getObjects().isEmpty()) {
			rowCount = sourceDelegate.getRowCount();
		}
		
		int numOfNewRows = newRows.getObjects().size();
		for(int i=0; i<numOfNewRows; i++) {
			int rowIndex = i + correctedFirstResult;
			if(rowIndex < rows.size()) {
				rows.set(rowIndex, newRows.getObjects().get(i));
			} else {
				rows.add(newRows.getObjects().get(i));
			}
		}
		return new DefaultResultInfos<>(newRows.getNextFirstResult(), newRows.getCorrectedRowCount(), rows);
	}
	
	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return columnModel;
	}
	
	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.columnModel = tableColumnModel;
	}
}
