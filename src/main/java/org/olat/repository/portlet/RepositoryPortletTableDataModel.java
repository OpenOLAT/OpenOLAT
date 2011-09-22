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
* Copyright (c) since 2009 by frentix GmbH, www.frentix.com
* <p>
*/
package org.olat.repository.portlet;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * table for the repository data: 
 *  used in Home-portal and manual repository-sorting
 * 
 * <P>
 * Initial Date:  06.03.2009 <br>
 * @author gnaegi, rhaag
 */
public class RepositoryPortletTableDataModel extends PortletDefaultTableDataModel {
	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	public  RepositoryPortletTableDataModel(List<PortletEntry> objects, Locale locale) {
		super(objects, 3);
		super.setLocale(locale);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int col) {
		RepositoryEntry repoEntry = getRepositoryEntry(row);
		switch (col) {
			case 0:
				return repoEntry.getDisplayname();
			case 1:
				return repoEntry.getDescription(); 
			case 2:
				return repoEntry;
			default:
				return "error";
		}
	}	

	public RepositoryEntry getRepositoryEntry(int row) {
		PortletEntry<RepositoryEntry> portletEntry = getObject(row);
		return portletEntry.getValue();
	}
}
