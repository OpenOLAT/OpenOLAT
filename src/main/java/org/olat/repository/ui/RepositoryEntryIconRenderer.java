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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.NewControllerFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
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
public class RepositoryEntryIconRenderer implements CustomCellRenderer, FlexiCellRenderer {
	
	private final Locale locale;
	
	public RepositoryEntryIconRenderer(Locale locale) {
		this.locale = locale;
	}

	public String getIconCssClass(Object val) {
		String cssClass = null;
		if (val instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)val;
			cssClass = RepositoyUIFactory.getIconCssClass(re);
		} else if(val instanceof RepositoryEntryShort) {
			RepositoryEntryShort re = (RepositoryEntryShort)val;
			cssClass = RepositoyUIFactory.getIconCssClass(re);
		}
		return cssClass == null ? "" : cssClass;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		render(target, renderer, cellValue);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale loc, int alignment, String action) {
		render(sb, renderer, val);
	}
	
	private void render(StringOutput sb, Renderer renderer, Object val) {
		if (renderer == null) {
			// render for export
		} else {
			String type = "";
			String cssClass = "";
			boolean managed = false;
			if(val instanceof RepositoryEntryShort) {
				RepositoryEntryShort re = (RepositoryEntryShort)val;
				cssClass = RepositoyUIFactory.getIconCssClass(re);
				String typeName = re.getResourceType();
				type = NewControllerFactory.translateResourceableTypeName(typeName, locale);
			} else if (val instanceof RepositoryEntry) {
				RepositoryEntry re = (RepositoryEntry)val;
				cssClass = RepositoyUIFactory.getIconCssClass(re);
				managed = StringHelper.containsNonWhitespace(re.getManagedFlagsString());
				String typeName = re.getOlatResource().getResourceableTypeName();
				type = NewControllerFactory.translateResourceableTypeName(typeName, locale);
			}
			
			sb.append("<i class='o_icon ").append(cssClass).append("'");
			if (StringHelper.containsNonWhitespace(type)) {
				sb.append(" title=\"");
				sb.append(StringEscapeUtils.escapeHtml(type));
			}
			sb.append("\"> </i>");	
			if(managed) {
				sb.append(" <i class='o_icon o_icon_managed'> </i>");
			}
		}
	}
}
