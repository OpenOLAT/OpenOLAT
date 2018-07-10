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


import java.util.Date;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * Render value with toString. Render Date value with Formatter depending on locale.
 * @author Christian Guretzki
 */
public class TextFlexiCellRenderer implements FlexiCellRenderer {
	
	private final EscapeMode escapeHtml;
	
	public TextFlexiCellRenderer() {
		escapeHtml = EscapeMode.html;
	}
	
	public TextFlexiCellRenderer(EscapeMode escapeHtml) {
		this.escapeHtml = escapeHtml;
	}
	

  /**
   * Render Date type with Formatter depending on locale. Render all other types with toString. 
 * @param target
 * @param cellValue
 * @param translator
   */	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Date) {
			Formatter formatter = Formatter.getInstance(translator.getLocale());
			target.append( formatter.formatDateAndTime((Date)cellValue) );
		} else if(cellValue instanceof String) {
			String str = (String)cellValue;
			if(escapeHtml != null) {
				switch(escapeHtml) {
					case antisamy:
						target.append(new OWASPAntiSamyXSSFilter().filter(str));
						break;
					case html:
						StringHelper.escapeHtml(target, str);
						break;
					case none:
						target.append(str);
						break;
				}
			} else {
				StringHelper.escapeHtml(target, str);
			}
		} else if(cellValue instanceof Boolean) {
			Boolean bool = (Boolean)cellValue;
			if(bool.booleanValue()) {
				target.append("<input type='checkbox' value='' checked='checked' disabled='disabled' />");
			} else {
				target.append("<input type='checkbox' value='' disabled='disabled' />");
			}
		} else if (cellValue != null) {
			target.append( cellValue.toString() );
		}
	}
}
