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
package org.olat.repository.ui;

import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;

/**
 * Description:<br>
 * This cell renderer displays the repository entry type as a CSS icon. The
 * underlying data model must provide an object of type RepositoryEntry
 * 
 * <P>
 * Initial Date: 16.04.2008 <br>
 * 
 * @author Florian Gnägi, http://www.frentix.com
 */
public class RepositoryEntryIconRenderer extends CustomCssCellRenderer {
	private Locale locale;

	/**
	 * Constructor
	 * 
	 * @param locale
	 */
	public RepositoryEntryIconRenderer(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * Constructor
	 */
	public RepositoryEntryIconRenderer() {
		this.locale = I18nModule.getDefaultLocale();
	}

	/**
	 * @see org.olat.core.gui.components.table.CustomCssCellRenderer#getCellValue(java.lang.Object)
	 */
	@Override
	protected String getCellValue(Object val) {
		return "";
	}

	/**
	 * @see org.olat.core.gui.components.table.CustomCssCellRenderer#getCssClass(java.lang.Object)
	 */
	@Override
	protected String getCssClass(Object val) {
		// use small icon and create icon class for resource:
		// o_FileResource-SHAREDFOLDER_icon
		if(val == null) {
			return "";
		}
		
		String cssClass = "";
		boolean managed = false;
		if(val instanceof RepositoryEntryShort) {
			RepositoryEntryShort re = (RepositoryEntryShort)val;
			cssClass = RepositoyUIFactory.getIconCssClass(re);
		} else if (val instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)val;
			cssClass = RepositoyUIFactory.getIconCssClass(re);
			managed = StringHelper.containsNonWhitespace(re.getManagedFlagsString());
		}
		return (managed ? "b_small_icon b_managed_icon " : "b_small_icon ") + cssClass;
	}
	


	/**
	 * @see org.olat.core.gui.components.table.CustomCssCellRenderer#getHoverText(java.lang.Object)
	 */
	@Override
	protected String getHoverText(Object val) {
		if (val == null) {
			return "n/a";
		}
		
		if(val instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry) val;
			String typeName = re.getOlatResource().getResourceableTypeName();
			return NewControllerFactory.translateResourceableTypeName(typeName, locale);
		}
		if(val instanceof RepositoryEntryShort) {
			RepositoryEntryShort re = (RepositoryEntryShort) val;
			String typeName = re.getResourceType();
			return NewControllerFactory.translateResourceableTypeName(typeName, locale);
		}
		return "n/a";
	}
}
