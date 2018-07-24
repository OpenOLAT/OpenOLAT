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
package org.olat.repository.portlet;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.util.filter.FilterFactory;
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
public class RepositoryPortletTableDataModel extends PortletDefaultTableDataModel<RepositoryEntry> {
	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	public  RepositoryPortletTableDataModel(List<PortletEntry<RepositoryEntry>> objects, Locale locale) {
		super(objects, 3);
		super.setLocale(locale);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int col) {
		RepositoryPortletEntry repoEntry = (RepositoryPortletEntry)getObject(row);
		switch (col) {
			case 0:
				return repoEntry.getValue().getDisplayname();
			case 1:
				String desc = repoEntry.getDescription();
				return FilterFactory.getHtmlTagsFilter().filter(desc);
			case 2:
				return repoEntry.getValue();
			default:
				return "error";
		}
	}	

	public RepositoryEntry getRepositoryEntry(int row) {
		PortletEntry<RepositoryEntry> portletEntry = getObject(row);
		return portletEntry.getValue();
	}
}
