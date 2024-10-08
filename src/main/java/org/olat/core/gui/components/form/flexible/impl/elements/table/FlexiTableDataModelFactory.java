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
public class FlexiTableDataModelFactory {

	/**
	 * Factory method to create an empty FlexiTableColumnModel.
	 * You must add FlexiColumnModel for each table column.
	 * @return
	 */
	public static FlexiTableColumnModel createFlexiTableColumnModel() {
		return new FlexiTableColumnModelImpl();
	}

	/**
	 * Get the CSS header for the more / action menu as used in almost all tables. 
	 * Usage: defaultFlexiColumnModel.setIconHeader(FlexiTableDataModelFactory.getColumnModelMoreMenuIconCSS());
	 * @return the CSS classes used as icon header
	 */
	public static String getColumnModelMoreMenuIconCSS() {
		return "o_icon o_icon-lg o_icon_commands";
	}

}