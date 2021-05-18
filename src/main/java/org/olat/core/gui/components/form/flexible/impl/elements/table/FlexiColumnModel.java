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
public interface FlexiColumnModel {

	public static int ALIGNMENT_LEFT = 1;
	public static int ALIGNMENT_RIGHT = 2;
	public static int ALIGNMENT_CENTER = 3;
	public static int ALIGNMENT_ICON = 4;
	

	
	public boolean isSortable();
	
	public void setSortable(boolean enable);
	
	public boolean isExportable();
	
	public void setExportable(boolean export);
	
	public boolean isDefaultVisible();
	
	public boolean isAlwaysVisible();
	
	public String getSortKey();

	public void setSortKey(String sortedKey);

	public String getHeaderKey();
	
	public String getHeaderLabel();
	
	public String getHeaderTooltip();
	
	public String getIconHeader();
	
	public Integer getHeaderAlignment();
	
	public String getColumnKey();
	
	public int getColumnIndex();
	
	/**
	 * @return The CSS classes for this column headers and cells or NULL
	 */
	public String getColumnCssClass();

	/**
	 * Set optional CSS classes for this column headers and cells or NULL
	 */
	public void setColumnCssClass(String columnCssClass);

	public String getAction();

	public int getAlignment();

	public void setAlignment(int alignment);
	
	public boolean isSelectAll();
	
	/**
	 * Enable/disable select all / deselect all links under a column.
	 * If checkbox are used, they need to be ajax only to not loose
	 * their state in the process.
	 * 
	 * @param selectAll true to enable select all / deselect all links
	 */
	public void setSelectAll(boolean selectAll);

	public FlexiCellRenderer getCellRenderer();

	public void setCellRenderer(FlexiCellRenderer cellRenderer);

	public FlexiCellRenderer getFooterCellRenderer();
	
	public void setFooterCellRenderer(FlexiCellRenderer cellRenderer);
	
}