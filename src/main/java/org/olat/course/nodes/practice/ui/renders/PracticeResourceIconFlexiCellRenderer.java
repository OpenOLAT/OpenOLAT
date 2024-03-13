/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.practice.ui.renders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractCSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 5 mai 2022<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PracticeResourceIconFlexiCellRenderer extends AbstractCSSIconFlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
					   FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		List<Object> cellValues = new ArrayList<>();
		if (cellValue.toString().contains(",")) {
			Object[] valuesArray = cellValue.toString().split(",");
			cellValues = Arrays.asList(valuesArray);
		}
		if (cellValues.isEmpty()) {
			target.append("<span><i class=\"o_icon ")
					.append(getCssClass(cellValue));
			target.append("\"> </i>");
			target.append("</span>");
		} else {
			for (Object cValue : cellValues) {
				target.append("<span><i class=\"o_icon ")
						.append(getCssClass(cValue));
				target.append("\"> </i>");
				target.append("</span>");
			}
		}
	}

	@Override
	protected String getCssClass(Object val) {
		return val == null ? null : val.toString();
	}

	@Override
	protected String getCellValue(Object val) {
		return "";
	}

	@Override
	protected String getHoverText(Object val, Translator translator) {
		return null;
	}
}
