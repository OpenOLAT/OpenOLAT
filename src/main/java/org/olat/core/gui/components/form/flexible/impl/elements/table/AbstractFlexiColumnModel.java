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

/**
 * 
 * Initial date: 15.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractFlexiColumnModel implements FlexiColumnModel {

	private int alignment;
	private String headerKey;
	private String headerLabel;
	private String columnKey;
	private boolean sortable;
	private boolean exportable = true;
	private String sortedKey;
	private int columnIndex;
	private FlexiCellRenderer cellRenderer;

	public AbstractFlexiColumnModel(String headerKey, int columnIndex, int alignment, FlexiCellRenderer cellRenderer) {
		this(headerKey, columnIndex, alignment, false, null, cellRenderer);
	}
	
	public AbstractFlexiColumnModel(String headerKey, int columnIndex, int alignment,
			boolean sortable, String sortedKey, FlexiCellRenderer cellRenderer) {
		this.headerKey = headerKey;
		this.columnIndex = columnIndex;
		this.columnKey = headerKey.replace(".", "").toLowerCase();
		this.sortable = sortable;
		this.sortedKey = sortedKey;
		this.alignment = alignment;
		this.cellRenderer = cellRenderer;
	}
	
	@Override
	public String getAction() {
		return null;
	}

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
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
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
		return true;
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