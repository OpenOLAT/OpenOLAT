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
package org.olat.repository.ui.author.copy.wizard;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 31.01.2022<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class DateWithLabelRenderer extends DateFlexiCellRenderer {
	
	private final LocalDate today;

	public DateWithLabelRenderer(Locale locale) {
		super(locale);
		today = LocalDate.now();
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof DateWithLabel) {
			Date date = ((DateWithLabel) cellValue).getDate();
			String label = ((DateWithLabel) cellValue).getLabel();
			
			if (date != null) {
				if (today.isAfter(DateUtils.toLocalDate(date))) {
					target.append("<i class=\"o_icon o_icon_lg o_icon_warn\"></i> ");
				}
				super.render(renderer, target, date, row, source, ubu, translator);
			}
			
			if (StringHelper.containsNonWhitespace(label)) {
				target.append(" - ").append(label);
			}
		}
		
	}

}
