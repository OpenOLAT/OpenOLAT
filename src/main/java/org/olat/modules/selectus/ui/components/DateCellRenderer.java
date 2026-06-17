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
package org.olat.modules.selectus.ui.components;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DateCellRenderer implements FlexiCellRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(DateCellRenderer.class);

	private static final DateFormat format = new SimpleDateFormat("dd MMMMM yyyy", Locale.ENGLISH);
	private static final DateFormat formatShort = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Date) {
			Date date = (Date)cellValue;
			target.append(format(date));
		} else if(cellValue instanceof String) {
			String xmlDate = (String)cellValue;
			if(StringHelper.containsNonWhitespace(xmlDate)) {
				try {
					Date date = Formatter.parseDatetime(xmlDate);
					target.append(format(date));
				} catch (ParseException e) {
					log.debug("Cannot parse XML date: {}", xmlDate, e);
				}
			}
		}
	}

	public static final String format(Date date) {
		if(date == null) return "";
		synchronized(format) {
			return format.format(date);
		}
	}
	
	public static final String format(Date date, Locale locale) {
		return Formatter.getInstance(locale).formatDateLong(date);
	}
	
	public static final String formatShort(Date date) {
		if(date == null) return "";
		synchronized(formatShort) {
			return formatShort.format(date);
		}
	}
}
