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

	int ALIGNMENT_LEFT = 1;
	int ALIGNMENT_RIGHT = 2;
	int ALIGNMENT_CENTER = 3;

	boolean isSortable();
	
	void setSortable(boolean enable);
	
	boolean isExportable();
	
	void setExportable(boolean export);
	
	boolean isDefaultVisible();
	
	boolean isAlwaysVisible();

	boolean isForExportOnly();
	
	String getSortKey();

	void setSortKey(String sortedKey);

	String getHeaderKey();
	
	String getHeaderLabel();
	
	String getColumnKey();

	String getIconHeader();
	
	Integer getHeaderAlignment();

	int getColumnIndex();
	
	String getAction();

	int getAlignment();

	void setAlignment(int alignment);

	FlexiCellRenderer getCellRenderer();

	void setCellRenderer(FlexiCellRenderer cellRenderer);

	public FlexiCellRenderer getFooterCellRenderer();
	
	public void setFooterCellRenderer(FlexiCellRenderer cellRenderer);
	
}
