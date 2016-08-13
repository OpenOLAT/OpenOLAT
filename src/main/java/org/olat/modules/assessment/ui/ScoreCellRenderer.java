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
package org.olat.modules.assessment.ui;

import java.math.BigDecimal;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;

/**
 * Format float and big decimal to the standard score format.
 * 
 * Initial date: 03.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScoreCellRenderer implements FlexiCellRenderer, CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		renderValue(sb, val);
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		renderValue(target, cellValue);
	}
	
	private final void renderValue(StringOutput target, Object cellValue) {
		if(cellValue instanceof Float) {
			target.append(AssessmentHelper.getRoundedScore((Float)cellValue));
		} else if(cellValue instanceof Double) {
			target.append(AssessmentHelper.getRoundedScore((Double)cellValue));
		} else if(cellValue instanceof BigDecimal) {
			target.append(AssessmentHelper.getRoundedScore((BigDecimal)cellValue));
		} else if(cellValue instanceof String) {
			target.append((String)cellValue);
		}
	}
}