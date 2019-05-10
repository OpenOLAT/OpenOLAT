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
	private Integer headerAlignment;
	private String columnKey;
	private int columnIndex;

	private boolean sortable;
	private boolean exportable = true;
	private String sortedKey;

	private boolean defaultVisible;
	private boolean alwaysVisible;
	private int alignment;
	
	private final String action;
	
	private FlexiCellRenderer cellRenderer;
	private FlexiCellRenderer footerCellRenderer;
	
	public DefaultFlexiColumnModel(FlexiColumnDef def) {
		this(def.i18nHeaderKey(), def.ordinal(), false, null);
	}
	
	public DefaultFlexiColumnModel(FlexiSortableColumnDef def) {
		this(def.i18nHeaderKey(), def.ordinal(), def.sortable(), def.sortKey());
	}
	
	public DefaultFlexiColumnModel(FlexiColumnDef def, String action) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), action, false, null, FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, FlexiColumnDef def, String action) {
		this(defVisible, false, def.i18nHeaderKey(), def.ordinal(), action, false, null, FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, FlexiSortableColumnDef def) {
		this(defVisible, false, def.i18nHeaderKey(), def.ordinal(), null, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT,
				new TextFlexiCellRenderer());
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, FlexiSortableColumnDef def, FlexiCellRenderer renderer) {
		this(defVisible, false, def.i18nHeaderKey(), def.ordinal(), null, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT,
				renderer);
	}
	
	public DefaultFlexiColumnModel(FlexiSortableColumnDef def, String action) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), action, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, FlexiSortableColumnDef def, String action) {
		this(defVisible, false, def.i18nHeaderKey(), def.ordinal(), action, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
	}
	
	public DefaultFlexiColumnModel(FlexiColumnDef def, FlexiCellRenderer renderer) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), null, false, null, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(FlexiSortableColumnDef def, FlexiCellRenderer renderer) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), null, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(FlexiColumnDef def, String action, FlexiCellRenderer renderer) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), action, false, null, FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, renderer));
	}
	
	public DefaultFlexiColumnModel(FlexiSortableColumnDef def, String action, FlexiCellRenderer renderer) {
		this(true, false, def.i18nHeaderKey(), def.ordinal(), action, def.sortable(), def.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT, 
				new StaticFlexiCellRenderer(action, renderer));
	}

	/**
	 * 
	 * @param headerKey
	 * @param columnIndex
	 */
	public DefaultFlexiColumnModel(String headerKey, int columnIndex) {
		this(headerKey, columnIndex, false, null);
	}
	
	/**
	 * Always visible
	 * @param headerKey
	 * @param label
	 * @param action
	 */
	public DefaultFlexiColumnModel(String headerKey, String label, String action) {
		this(headerKey, label, action, false);
	}
	
	/**
	 * Always visible
	 * @param headerKey
	 * @param label
	 * @param action
	 */
	public DefaultFlexiColumnModel(String headerKey, String label, String action, boolean newWindow) {
		this(true, true, headerKey, -1, action, false, null, FlexiColumnModel.ALIGNMENT_LEFT, new StaticFlexiCellRenderer(label, action, newWindow));
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, boolean sortable, String sortKey) {
		this(true, false, headerKey, columnIndex, null, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer());
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, FlexiCellRenderer renderer) {
		this(true, false, headerKey, columnIndex, null, false, null, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	/**
	 * Always visible
	 * @param headerKey
	 * @param columnIndex
	 * @param action
	 * @param renderer
	 */
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, String action, FlexiCellRenderer renderer) {
		this(true, true, headerKey, columnIndex, action, false, null, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, boolean sortable, String sortKey, FlexiCellRenderer renderer) {
		this(true, false, headerKey, columnIndex, null, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, String headerKey, int columnIndex, boolean sortable, String sortKey) {
		this(defVisible, false, headerKey, columnIndex, null, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT,
				new TextFlexiCellRenderer());
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, String headerKey, int columnIndex, String action, boolean sortable, String sortKey) {
		this(defVisible, false, headerKey, columnIndex, action, sortable, sortKey, FlexiColumnModel.ALIGNMENT_LEFT,
				new StaticFlexiCellRenderer(action, new TextFlexiCellRenderer()));
	}
	
	/**
	 * Always visible
	 * @param headerKey
	 * @param columnIndex
	 * @param action
	 * @param sortable
	 * @param sortedKey
	 * @param renderer
	 */
	public DefaultFlexiColumnModel(String headerKey, int columnIndex, String action, boolean sortable, String sortedKey, FlexiCellRenderer renderer) {
		this(true, true, headerKey, columnIndex, action, sortable, sortedKey, FlexiColumnModel.ALIGNMENT_LEFT, renderer);
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, String headerKey, int columnIndex, boolean sortable, String sortKey, int alignment, FlexiCellRenderer cellRenderer) {
		this(defVisible, false, headerKey, columnIndex, null, sortable, sortKey, alignment, cellRenderer);
	}
	
	public DefaultFlexiColumnModel(boolean defVisible, boolean alwaysVisible, String headerKey, int columnIndex, String action,
			boolean sortable, String sortKey, int alignment, FlexiCellRenderer cellRenderer) {
		this.defaultVisible = defVisible;
		this.alwaysVisible = alwaysVisible;
		this.sortable = sortable;
		this.sortedKey = sortKey;
		this.headerKey = headerKey;
		this.columnIndex = columnIndex;
		this.columnKey = headerKey.replace(".", "").toLowerCase();
		this.alignment = alignment;
		this.cellRenderer = cellRenderer;
		footerCellRenderer = new TextFlexiCellRenderer();
		this.action = action;
	}

	@Override
	public boolean isAlwaysVisible() {
		return alwaysVisible;
	}
	
	public void setAlwaysVisible(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
	}

	@Override
	public String getAction() {
		return action;
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
	public Integer getHeaderAlignment() {
		return headerAlignment;
	}

	public void setHeaderAlignment(int headerAlignment) {
		this.headerAlignment = headerAlignment;
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

	@Override
	public int getAlignment() {
		return alignment;
	}

	@Override
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	@Override
	public FlexiCellRenderer getCellRenderer() {
		return cellRenderer;
	}

	@Override
	public void setCellRenderer(FlexiCellRenderer cellRenderer) {
		this.cellRenderer = cellRenderer;
	}

	@Override
	public FlexiCellRenderer getFooterCellRenderer() {
		return footerCellRenderer;
	}

	@Override
	public void setFooterCellRenderer(FlexiCellRenderer footerCellRenderer) {
		this.footerCellRenderer = footerCellRenderer;
	}

	

}