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
package org.olat.course.style.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.style.ColorCategory;

/**
 * 
 * Initial date: 22 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SystemImagePreviewRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof SystemImageRow) {
			SystemImageRow systemImageRow = (SystemImageRow)cellValue;
			
			target.append("<div class=\"o_cn_styled o_system_image\">");
			target.append("<div class=\"o_top\">");
			target.append("<div class=\"o_image\">");
			target.append("<img src=\"").append(systemImageRow.getMapperUrl()).append("\"");
			if (systemImageRow.isTransparent()) {
				target.append(" class=\"").append(ColorCategory.CSS_NO_COLOR).append("\"");
			}
			target.append(" alt=\"\">");
			target.append("</div>");
			target.append("</div>");
			target.append("</div>");
		}
	}

}
