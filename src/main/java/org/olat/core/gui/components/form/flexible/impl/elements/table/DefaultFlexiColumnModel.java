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


/**
 * 
 * @author Christian Guretzki
 */
public class DefaultFlexiColumnModel implements FlexiColumnModel {

	private String headerKey;
	private String headerLabel;
	private String columnKey;
	private int columnIndex;

	private boolean sortable;
	private boolean exportable = true;
	private String sortedKey;

	private boolean defaultVisible;
	private int alignment;
	private FlexiCellRenderer cellRenderer;

	public DefaultFlexiColumnModel(String headerKey, int columnIndex) {
		this(headerKey, columnIndex, false, null);
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, boolean sortable, String sortKey) {
		this(true, headerKey, columnIndex, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT,  new TextFlexiCellRenderer());
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, FlexiCellRenderer renderer) {
		this(true, headerKey, columnIndex, false, null, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, boolean sortable, String sortKey, FlexiCellRenderer renderer) {
		this(true, headerKey, columnIndex, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, String headerKey, int columnIndex, boolean sortable, String sortKey) {
		this(defVisible, headerKey, columnIndex, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT,  new TextFlexiCellRenderer());
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, String headerKey, int columnIndex, boolean sortable, String sortKey, int alignment, FlexiCellRenderer cellRenderer) {
		this.defaultVisible = defVisible;
		this.sortable = sortable;
		this.sortedKey = sortKey;
		this.headerKey = headerKey;
		this.columnIndex = columnIndex;
		this.columnKey = headerKey.replace(".", "").toLowerCase();
		this.alignment = alignment;
		this.cellRenderer = cellRenderer;
	}

	@Override
	public boolean isAlwaysVisible() {
		return false;
	}

	@Override
	public String getAction() {
		return null;
	}

	@Override
	public String getHeaderKey() {
		return headerKey;
	}

	@Override
	public String getHeaderLabel() {
		return headerLabel;
	}

	public void setHeaderLabel(String headerLabel) {
		this.headerLabel = headerLabel;
	}

	@Override
	public String getColumnKey() {
		return columnKey;
	}

	@Override
	public int getColumnIndex() {
		return columnIndex;
	}

	@Override
	public boolean isSortable() {
		return sortable;
	}
	
	@Override
	public void setSortable(boolean enable) {
		sortable = enable;
	}
	
	@Override
	public boolean isExportable() {
		return exportable;
	}
	
	@Override
	public void setExportable(boolean exportable) {
		this.exportable = exportable;
	}
	
	@Override
	public boolean isDefaultVisible() {
		return defaultVisible;
	}
	
	public void setDefaultVisible(boolean defaultVisible) {
		this.defaultVisible = defaultVisible;
	}

	@Override
	public String getSortKey() {
		return sortedKey;
	}

	@Override
	public void setSortKey(String sortedKey) {
		this.sortedKey = sortedKey;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void setCellRenderer(FlexiCellRenderer cellRenderer) {
		this.cellRenderer = cellRenderer;
	}

	public FlexiCellRenderer getCellRenderer() {
		return cellRenderer;
	}

}