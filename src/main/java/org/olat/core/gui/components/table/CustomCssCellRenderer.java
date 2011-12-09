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

import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Render a cell with an custom css class applied. The hover text is optional
 * 
 * <P>
 * Initial Date:  Nov 13, 2006 <br>
 * @author guido
 */
public abstract class CustomCssCellRenderer implements CustomCellRenderer {

	public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
		if (renderer == null) {
			// render for export
			String value = getCellValue(val);
			if ( ! StringHelper.containsNonWhitespace(value) ) {
				// try css class
				value = getCssClass(val);
				if (! StringHelper.containsNonWhitespace(value)) {
					// try title
					value = getHoverText(val);
				} else {
					// remove helper css classes
					if (value != null){
						value = value.replace("b_small_icon", "").trim();
					}
				}
			}
			sb.append(value);
		} else {
			sb.append("<span class=\"");
			sb.append(getCssClass(val));
			String hoverText = getHoverText(val);
			if (StringHelper.containsNonWhitespace(hoverText)) {
				sb.append("\" title=\"");
				sb.append(StringEscapeUtils.escapeHtml(hoverText));
			}
			sb.append("\">");
			sb.append(getCellValue(val));
			sb.append("</span>");			
		}
		
	}
	protected abstract String getCssClass(Object val);
	protected abstract String getCellValue(Object val);
	protected abstract String getHoverText(Object val);
	
}
