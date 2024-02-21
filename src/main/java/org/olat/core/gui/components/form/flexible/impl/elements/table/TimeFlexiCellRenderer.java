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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * Render the time only.
 * 
 * Initial date: 30 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TimeFlexiCellRenderer implements FlexiCellRenderer {
	
	private final Formatter format;
	private final boolean fullDateTooltip;
	
	public TimeFlexiCellRenderer(Locale locale) {
		this(locale, false);
	}
	
	public TimeFlexiCellRenderer(Locale locale, boolean fullDateTooltip) {
		format = Formatter.getInstance(locale);
		this.fullDateTooltip = fullDateTooltip;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Date date) {
			if(fullDateTooltip) {
				target.append("<span title='").append(format.formatDateAndTime(date)).append("'>")
					.append(format.formatTimeShort((Date)cellValue))
					.append("</span>");
			} else {
				target.append(format.formatTimeShort((Date)cellValue));
			}
		}
	}
}