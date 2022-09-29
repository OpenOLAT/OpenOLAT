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

import javax.persistence.Transient;

/**
 * 
 * Initial date: 10 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerSettings {
	
	public static final String EMPTY = "xxx-empty-xxx";
	
	private String name;
	private int numOfColumns = 2;
	private ContainerLayout type;
	private List<ContainerColumn> columns;
	

	public ContainerLayout getType() {
		return type == null ? ContainerLayout.ofColumn(numOfColumns, columns) : type;
	}

	public void setType(ContainerLayout type) {
		this.type = type;
	}
	
	public void updateType(ContainerLayout type) {
		setType(type);
		
		int numOfBlocks = type.numberOfBlocks();
		if(columns != null && columns.size() >= numOfBlocks) {
			ContainerColumn lastColumn = columns.get(numOfBlocks - 1);
			for(int i=numOfBlocks; i<columns.size(); i++) {
				ContainerColumn outColumn = columns.get(i);
				if(outColumn != null && !outColumn.getElementIds().isEmpty()) {
					lastColumn.getElementIds().addAll(outColumn.getElementIds());
					outColumn.getElementIds().clear();
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumOfColumns() {
		return numOfColumns;
	}
	
	@Transient
	public int getNumOfBlocks() {
		return getType().numberOfBlocks();
	}

	public void setNumOfColumns(int numOfColumns) {
		this.numOfColumns = numOfColumns;
		if(columns != null && columns.size() > numOfColumns) {
			columns = new ArrayList<>(columns.subList(0, numOfColumns));
		}
	}
	
	@Transient
	public List<String> getAllElementIds() {
		List<String> allElementIds = new ArrayList<>();
		if(columns != null) {
			for(ContainerColumn column:columns) {
				allElementIds.addAll(column.getElementIds());
			}
		}
		return allElementIds;
	}
	
	public boolean hasElement(String elementId) {
		if(columns != null) {
			for(ContainerColumn column:columns) {
				if(column.contains(elementId)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<ContainerColumn> getColumns() {
		if(columns == null) {
			columns = new ArrayList<>(4);
			for(int i=0; i<numOfColumns; i++) {
				columns.add(new ContainerColumn());
			}
		}
		return columns;
	}
	
	public void setColumns(List<ContainerColumn> columns) {
		this.columns = columns;
	}
	
	public ContainerColumn getColumn(int index) {
		if(index < getNumOfBlocks()) {
			List<ContainerColumn> columnList = getColumns();
			if(columnList.size() <= index) {
				for(int i=columnList.size(); i<=index; i++) {
					columnList.add(new ContainerColumn());
				}
			}
			return columnList.get(index);
		}
		return null;
	}
	
	public ContainerColumn getColumn(String elementId) {
		List<ContainerColumn> columnList = getColumns();
		for(ContainerColumn column:columnList) {
			if(column != null && column.contains(elementId)) {
				return column;
			}
		}
		return null;
	}
	
	public int getColumnIndex(String elementId) {
		List<ContainerColumn> columnList = getColumns();
		for(int i=0; i<columnList.size(); i++) {
			ContainerColumn column = columnList.get(i);
			if(column != null && column.contains(elementId)) {
				return i;
			}
		}
		return -1;
	}
	
	public void setElementAt(String elementId, int column, String sibling) {
		ContainerColumn col = getColumn(column);
		List<String> elementIds = col.getElementIds();
		if(sibling != null && elementIds.contains(sibling)) {
			int index = elementIds.indexOf(sibling);
			if(index >= 0 && index < elementIds.size()) {
				elementIds.add(index, elementId);
			} else {
				elementIds.add(elementId);
			}
		} else {
			elementIds.add(elementId);
		}
	}
	
	public void removeElement(String elementId) {
		List<ContainerColumn> columnList = getColumns();
		for(ContainerColumn column:columnList) {
			column.getElementIds().remove(elementId);
		}
	}
	
	public void moveUp(String elementId) {
		ContainerColumn column = getColumn(elementId);
		if(column != null) {
			column.moveUp(elementId);
		}
	}
	
	public void moveDown(String elementId) {
		ContainerColumn column = getColumn(elementId);
		if(column != null) {
			column.moveDown(elementId);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ContainerSettings[").append(type == null ? "unkown" : type.name());
		if(columns != null) {
			for(ContainerColumn column:columns) {
				sb.append(";");
				if(column != null) {
					sb.append(column.getElementIds().toString());
				} else {
					sb.append("NULL");
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
