/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.daynav;

import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Jan 6, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DayNavRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		Translator cmpTranslator = Util.createPackageTranslator(DayNavComponent.class, translator.getLocale());
		Formatter formatter = Formatter.getInstance(translator.getLocale());
		
		DayNavComponent cmp = (DayNavComponent)source;
		String elementId = cmp.getFormDispatchId();
		
		sb.append("<div ");
		if (elementId != null) {
			sb.append("id=\"").append(elementId).append("\" ");
		}
		sb.append("class=\"");
		sb.append("o_day_nav");
		if (StringHelper.containsNonWhitespace(cmp.getElementCssClass())) {
			sb.append(" ").append(cmp.getElementCssClass());
		}
		sb.append("\"");
		sb.append(">");
		
		sb.append("<div class=\"o_day_nav_header\">");
		renderToday(sb, ubu, cmpTranslator, formatter, cmp);
		renderWeekStepper(sb, ubu, cmpTranslator, formatter, cmp);
		sb.append("</div>");
		
		sb.append("<ul class='list-unstyled'>");
		for (int i=0; i < 7; i++) {
			renderDay(sb, ubu, formatter, cmp, i);
		}
		sb.append("</ul>");
		
		sb.append("</div>");
	}

	private void renderToday(StringOutput sb, URLBuilder ubu, Translator cmpTranslator, Formatter formatter, DayNavComponent cmp) {
		Date today = new Date();
		
		sb.append("<div class=\"o_today\">");
		renderTodayButton(sb, ubu, cmp, today);
		sb.append("<div class=\"o_today_text\">");
		sb.append("<div class=\"o_today_month\">");
		sb.append(formatter.formatMonthLongYear(today));
		sb.append("</div>");
		sb.append("<div class=\"o_today_weekday\">");
		sb.append(cmpTranslator.translate("day.nav.today.weekday", formatter.formatWeekdayLong(today)));
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
	}

	private void renderTodayButton(StringOutput sb, URLBuilder ubu, DayNavComponent cmp, Date today) {
		String elementId = getElementId(cmp, "today");
		
		sb.append("<div class=\"o_today_day\">");
		String buttonText = "<div class=\"o_today_day_num\">" + DateUtils.dayFromDate(today) + "</div>";
		NameValuePair nameValue = new NameValuePair(VelocityContainer.COMMAND_ID, DayNavComponent.CMD_TODAY);
		renderButton(sb, ubu, cmp, elementId, "o_today_day_button", buttonText, nameValue);
		sb.append("</div>");
	}

	private void renderWeekStepper(StringOutput sb, URLBuilder ubu, Translator cmpTranslator, Formatter formatter,
			DayNavComponent cmp) {
		sb.append("<div class=\"o_week_steper\">");
		sb.append("<div class=\"o_week\">");
		int startMonth = DateUtils.monthFromDate(cmp.getStartDate());
		Date endDate = DateUtils.addDays(cmp.getStartDate(), 6);
		int endMonth = DateUtils.monthFromDate(endDate);
		String monthText = null;
		if (startMonth == endMonth) {
			monthText = cmpTranslator.translate("day.nav.month",
					formatter.formatMonthLong(cmp.getStartDate()),
					String.valueOf(DateUtils.yearFromDate(cmp.getStartDate())));
		} else {
			int startYear = DateUtils.yearFromDate(cmp.getStartDate());
			int endYear = DateUtils.yearFromDate(endDate);
			if (startYear == endYear) {
				monthText = cmpTranslator.translate("day.nav.months",
						formatter.formatMonthLong(cmp.getStartDate()),
						formatter.formatMonthLong(endDate), 
						String.valueOf(startYear));
			} else {
				monthText = cmpTranslator.translate("day.nav.months.years",
						formatter.formatMonthLong(cmp.getStartDate()),
						String.valueOf(startYear),
						formatter.formatMonthLong(endDate),
						String.valueOf(endYear));
			}
		}
		sb.append(monthText);
		sb.append("</div>");
		
		String elementId = getElementId(cmp, "prev_week");
		String buttonCss = "o_week_button o_link";
		String buttonText = "<i class=\"o_icon o_icon-lg o_icon_course_previous\" aria-hidden=\"true\" title=\""
				+ cmpTranslator.translate("day.nav.week.prev") + "\"> </i> <span class=\"sr-only\">"
				+ cmpTranslator.translate("day.nav.week.prev") + "</span>";
		NameValuePair nameValue = new NameValuePair(VelocityContainer.COMMAND_ID, DayNavComponent.CMD_PREV_WEEK);
		renderButton(sb, ubu, cmp, elementId, buttonCss, buttonText, nameValue);
		
		elementId = getElementId(cmp, "next_week");
		buttonText = "<i class=\"o_icon o_icon-lg o_icon_course_next\" aria-hidden=\"true\" title=\""
				+ cmpTranslator.translate("day.nav.week.next") + "\"> </i> <span class=\"sr-only\">"
				+ cmpTranslator.translate("day.nav.week.next") + "</span>";
		nameValue = new NameValuePair(VelocityContainer.COMMAND_ID, DayNavComponent.CMD_NEXT_WEEK);
		renderButton(sb, ubu, cmp, elementId, buttonCss, buttonText, nameValue);
		
		sb.append("</div>");
	}

	private void renderDay(StringOutput sb, URLBuilder ubu, Formatter formatter, DayNavComponent cmp, int dayIndex) {
		String elementId = getElementId(cmp, String.valueOf(dayIndex));
		
		Date day = DateUtils.addDays(cmp.getStartDate(), dayIndex);
		
		sb.append("<li class=\"o_day_nav_day");
		if (DateUtils.isWeekend(day)) {
			sb.append(" ").append("o_day_weekend");
		}
		sb.append("\">");
		sb.append("<div class=\"o_day_abbr\">").append(formatter.dayOfWeekShort(day)).append("</div>");
		
		NameValuePair nameValue = new NameValuePair(DayNavComponent.CMD_SELECT_DAY_INDEX, dayIndex);
		StringBuilder buttonCssSb = new StringBuilder();
		buttonCssSb.append("o_day_button");
		if (cmp.getSelectedDateIndex() == dayIndex) {
			buttonCssSb.append(" o_day_selected");
		} else if (DateUtils.isSameDay(day, new Date())) {
			buttonCssSb.append(" o_day_today");
		}
		
		String buttonText = "<div class=\"o_day_num\">" + DateUtils.dayFromDate(day) + "</div>";
		renderButton(sb, ubu, cmp, elementId, buttonCssSb.toString(), buttonText, nameValue);
		sb.append("</li>");
	}
	
	private void renderButton(StringOutput sb, URLBuilder ubu, DayNavComponent cmp, String buttonId, String buttonCss, String buttonText, NameValuePair nameValue) {
		sb.append("<button type=\"button\" ");
		sb.append("id = \"").append(buttonId).append("\" ");
		sb.append("class=\"");
		sb.append("o_can_have_focus ");
		if (StringHelper.containsNonWhitespace(buttonCss)) {
			sb.append(" ").append(buttonCss);
		}
		sb.append("\"");
		
		if(cmp.isEnabled()) {
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
			if(cmp.getFormItem() != null) {
				sb.append(FormJSHelper.getXHRFnCallFor(cmp.getFormItem().getRootForm(), cmp.getFormDispatchId(), 1, false, true, false, nameValue));
				sb.append(";\"");
			} else {
				ubu.buildXHREvent(sb, "", false, true, nameValue);
				sb.append("\" ");
			}
			sb.append("onfocus=\"o_info.lastFormFocusEl='").append(buttonId).append("';\" ");
		} else {
			sb.append("disabled=\"true\" ");
		}
		sb.append(">");
		sb.append(buttonText);
		sb.append("</button>");
	}
	
	private String getElementId(DayNavComponent cmp, String suffix) {
		String elementId = cmp.getFormDispatchId();
		if (!StringHelper.containsNonWhitespace(elementId)) {
			elementId = "o_" + CodeHelper.getRAMUniqueID();
		}
		elementId += "_";
		elementId += suffix;
		return elementId;
	}

}
