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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;

/**
 * 
 */
public interface FlexiTableElement extends FormItem {

	public static final String ROM_SELECT_EVENT = "rSelect";
	
	/**
	 * @return the type of renderer used by  this table
	 */
	public FlexiTableRendererType getRendererType();
	
	/**
	 * Set the renderer for this table
	 * @param rendererType
	 */
	public void setRendererType(FlexiTableRendererType rendererType);

	/**
	 * @return True if muli selection is enabled
	 */
	public boolean isMultiSelect();
	
	/**
	 * Enable multi-selection
	 * @return
	 */
	public void setMultiSelect(boolean enable);
	
	/**
	 * @return true if the links select all / unselect all are enabled
	 */
	public boolean isSelectAllEnable();
	
	/**
	 * Enable the select all /unselect all links
	 * @param enable
	 */
	public void setSelectAllEnable(boolean enable);
	
	/**
	 * All rows are selected
	 * @return
	 */
	public boolean isAllSelectedIndex();
	
	/**
	 * Set all selecteds rows
	 * @return
	 */
	public Set<Integer> getMultiSelectedIndex();
	
	/**
	 * 
	 * @param index
	 * @return true if the row is selected
	 */
	public boolean isMultiSelectedIndex(int index);
	
	/**
	 * Is a search field enabled
	 * @return
	 */
	public boolean isSearchEnabled();
	
	
	/**
	 * Return the page size
	 * @return
	 */
	public int getPageSize();
	
	public void setPageSize(int pageSize);
	
	public void setPage(int page);
	
}