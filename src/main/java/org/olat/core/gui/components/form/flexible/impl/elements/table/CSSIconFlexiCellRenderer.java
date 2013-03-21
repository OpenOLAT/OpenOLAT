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
*/
package org.olat.core.gui.components.form.flexible.impl.elements.table;/**

 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * <h3>Description:</h3>
 * Render a cell with an custom css class applied. The hover text is optional
 * <p>
 * Initial Date: 21.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public abstract class CSSIconFlexiCellRenderer implements FlexiCellRenderer {
	
	private FlexiCellRenderer delegate;
	
	public CSSIconFlexiCellRenderer() {
		//
	}
	
	public CSSIconFlexiCellRenderer(FlexiCellRenderer delegate) {
		this.delegate = delegate;
	}

	  /**
	   * Render Date type with Formatter depending on locale. Render all other types with toString. 
	   * @param target
	   * @param cellValue
	   * @param translator
	   */	
	@Override
	public void render(StringOutput target, Object cellValue, int row, FlexiTableComponent source,
				URLBuilder ubu, Translator translator) {
		target.append("<span class=\"b_small_icon ");
		target.append(getCssClass(cellValue));
		String hoverText = getHoverText(cellValue, translator);
		if (StringHelper.containsNonWhitespace(hoverText)) {
			target.append("\" title=\"");
			target.append(StringEscapeUtils.escapeHtml(hoverText));
		}
		target.append("\">");
		if(delegate == null) {
			target.append(getCellValue(cellValue));
		} else {
			delegate.render(target, cellValue, row, source, ubu, translator);
		}
		target.append("</span>");			
	}
		
	protected abstract String getCssClass(Object val);
	protected abstract String getCellValue(Object val);
	protected abstract String getHoverText(Object val, Translator translator);	
}
