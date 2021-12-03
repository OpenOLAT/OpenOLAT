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
package org.olat.core.gui.components.table;

import static org.olat.core.util.StringHelper.blankIfNull;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class IconCssCellRenderer implements CustomCellRenderer, FlexiCellRenderer {
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (renderer == null) {
			// render for export
			String value = getCellValue(cellValue);
			if (!StringHelper.containsNonWhitespace(value)) {
				value = getHoverText(cellValue);
			}
			target.append(value);
		} else {
			target.append("<div");
			String cssClass = getCssClass(cellValue);
			if (StringHelper.containsNonWhitespace(cssClass)) {
				target.append(" class=\"");
				target.appendHtmlEscaped(cssClass);
				target.append("\"");
			}
			target.append(" style='white-space: nowrap;'");
			String hoverText = getHoverText(cellValue);
			if (StringHelper.containsNonWhitespace(hoverText)) {
				target.append(" title=\"");
				target.appendHtmlEscaped(hoverText);
			}
			target.append("\">");
			target.append("<i class='").append(blankIfNull(getIconCssClass(cellValue))).append("'> </i> <span>");
	
			target.append(blankIfNull(getCellValue(cellValue)));
			target.append("</span></div>");
		}
		
	}

	@Override
	public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
		if (renderer == null) {
			// render for export
			String value = getCellValue(val);
			if (!StringHelper.containsNonWhitespace(value)) {
				value = getHoverText(val);
			}
			sb.append(value);
		} else {
			sb.append("<div");
			String cssClass = getCssClass(val);
			if (StringHelper.containsNonWhitespace(cssClass)) {
				sb.append(" class=\"");
				sb.appendHtmlEscaped(cssClass);
				sb.append("\"");
			}
			sb.append(" style='white-space: nowrap;'><i class='").append(blankIfNull(getIconCssClass(val))).append("'> </i> <span");
			String hoverText = getHoverText(val);
			if (StringHelper.containsNonWhitespace(hoverText)) {
				sb.append(" title=\"");
				sb.appendHtmlEscaped(hoverText);
			}
			sb.append("\">");
			sb.append(blankIfNull(getCellValue(val)));
			sb.append("</span></div>");
		}
	}
	
	
	protected abstract String getIconCssClass(Object val);
	protected abstract String getCellValue(Object val);
	
	@SuppressWarnings("unused")
	protected String getCssClass(Object val) {
		return null;
	}
	
	@SuppressWarnings("unused")
	protected String getHoverText(Object val) {
		return null;
	}
}
