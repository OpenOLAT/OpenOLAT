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
package org.olat.core.gui.components.table;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class LabelCellRenderer implements FlexiCellRenderer {

	protected abstract String getCellValue(Object val, Translator translator);

	protected abstract String getIconCssClass(Object val);

	protected abstract String getElementCssClass(Object val);
	
	@SuppressWarnings("unused")
	protected String getTitle(Object val, Translator translator) {
		return null;
	}
	
	protected String getExportValue(Object val, Translator translator) {
		return getCellValue(val, translator);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (renderer == null) {
			String value = getExportValue(cellValue, translator);
			target.append(value);
		} else {
			target.append("<div>");
			target.append("<span class='o_labeled_light");
			String elementCssClass = getElementCssClass(cellValue);
			if (StringHelper.containsNonWhitespace(elementCssClass)) {
				target.append(" ").append(elementCssClass);
			}
			target.append("'");
			String title = getTitle(cellValue, translator);
			if (StringHelper.containsNonWhitespace(title)) {
				target.append(" title='").append(title).append("'");
			}
			target.append(">");
			
			String iconCssClass = getIconCssClass(cellValue);
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				target.append("<i class='o_icon ").append(iconCssClass).append("'> </i> ");
			}
			
			String value = getCellValue(cellValue, translator);
			if (StringHelper.containsNonWhitespace(value)) {
				target.append("<span>").append(value).append("</span>");
			}
			
			target.append("</span>");
			target.append("</div>");			
		}
	}

}