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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 28.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractCSSIconFlexiCellRenderer implements FlexiCellRenderer {
	
	private final FlexiCellRenderer delegate;
	
	public AbstractCSSIconFlexiCellRenderer() {
		this(null);
	}
	
	public AbstractCSSIconFlexiCellRenderer(FlexiCellRenderer delegate) {
		this.delegate = delegate;
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
		target.append("<span><i class=\"o_icon ")
		     .append(getCssClass(cellValue));
		String hoverText = getHoverText(cellValue, translator);
		if (StringHelper.containsNonWhitespace(hoverText)) {
			target.append("\" title=\"");
			target.append(StringHelper.escapeHtml(hoverText));
		}
		target.append("\"> </i>");
		if(delegate == null) {
			target.append(getCellValue(cellValue));
		} else {
			delegate.render(null, target, cellValue, row, source, ubu, translator);
		}
		target.append("</span>");			
	}
		
	protected abstract String getCssClass(Object val);
	
	protected abstract String getCellValue(Object val);
	
	protected abstract String getHoverText(Object val, Translator translator);
}
