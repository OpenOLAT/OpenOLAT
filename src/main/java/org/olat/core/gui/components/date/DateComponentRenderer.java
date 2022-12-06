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
package org.olat.core.gui.components.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Renderer for the date component. An optional render argument can be used.
 * This is interpreted as a CSS class name. 
 * 
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DateComponentRenderer extends DefaultComponentRenderer {
	
	/**
	 * Package scope constuctro
	 */
	DateComponentRenderer() {
		// Nothing to do
	}

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		DateComponent dateC = (DateComponent) source;
		Date date = dateC.getDate();
		Locale locale = translator.getLocale();

		sb.append("<div id=\"o_c").append(dateC.getDispatchID());
		sb.append("\" class=\"o_datecomp");
		// Optional css class as render arg
		if (args != null && args.length == 1) {
			sb.append(args[0]);
		}
		sb.append("\">");
		// Add year if configured
		if (dateC.isShowYear()) {
			SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", locale);
			String year = yearFormat.format(date);
			sb.append("<div class=\"o_year o_year_").append(year);
			sb.append("\"><span>");
			sb.append(year);
			sb.append("</span>");
			sb.append("</div>");			
		}
		// Add month.
		SimpleDateFormat monthNumberFormat = new SimpleDateFormat("MM", locale);
		sb.append("<div class=\"o_month o_month_").append(monthNumberFormat.format(date));
		sb.append("\"><span>");
		SimpleDateFormat monthDisplayFormat = new SimpleDateFormat("MMM", locale);
		sb.append(monthDisplayFormat.format(date).toUpperCase());
		sb.append("</span>");
		sb.append("</div>");
		// Add day
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd", locale);
		String day = dayFormat.format(date); 
		sb.append("<div class=\"o_day o_day_").append(day);
		sb.append("\"><span>");
		sb.append(day);
		sb.append("</span>");
		sb.append("</div>");
		//
		sb.append("</div>");
	}
}
