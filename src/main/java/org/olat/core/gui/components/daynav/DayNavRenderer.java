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

import java.time.format.DateTimeFormatter;
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
		
		if (source instanceof DayNavWeekComponent weekCmp) {
			renderWeek(sb, ubu, cmpTranslator, weekCmp);
		} else if (source instanceof DayNavComponent cmp) {
			renderDays(sb, ubu, cmpTranslator, cmp);
		}
	}
	
	private void renderWeek(StringOutput sb, URLBuilder ubu, Translator cmpTranslator, DayNavWeekComponent weekCmp) {
		DayNavComponent cmp = weekCmp.getDayNav();
		
		sb.append("<div id=\"o_c").append(weekCmp.getDispatchID()).append("\" class=\"o_day_nav_week\">");
		sb.append("<div class=\"o_day_nav_period\">");
		renderWeekButton(sb, ubu, cmpTranslator, cmp, "prev_week", "day.nav.week.prev", "o_icon_course_previous", DayNavComponent.CMD_PREV_WEEK);
		sb.append("<span class=\"o_day_nav_period_dates\">").append(StringHelper.escapeHtml(getPeriod(cmpTranslator, cmp))).append("</span>");
		sb.append("<span class=\"o_day_nav_period_week\">")
				.append(StringHelper.escapeHtml(cmpTranslator.translate("day.nav.week", String.valueOf(cmp.getWeekOfYear()))))
				.append("</span>");
		renderWeekButton(sb, ubu, cmpTranslator, cmp, "next_week", "day.nav.week.next", "o_icon_course_next", DayNavComponent.CMD_NEXT_WEEK);
		sb.append("</div>");
		
		Date today = new Date();
		String todayTitle = StringHelper.escapeForHtmlAttribute(cmpTranslator.translate("day.nav.today.weekday", format(cmpTranslator, "day.nav.pattern.today", today)));
		String attributes = "title=\"" + todayTitle + "\" aria-label=\"" + todayTitle + "\"";
		String todayText = "<span class=\"o_day_nav_today_text\">" + StringHelper.escapeHtml(cmpTranslator.translate("day.nav.today")) + "</span>"
				+ "<span class=\"o_day_nav_today_num\">" + DateUtils.dayFromDate(today) + "</span>";
		NameValuePair nameValue = new NameValuePair(VelocityContainer.COMMAND_ID, DayNavComponent.CMD_TODAY);
		renderButton(sb, ubu, cmp, getElementId(cmp, "today"), "btn btn-default btn-sm o_button_primary_light o_day_nav_today", attributes,
				todayText, nameValue);
		sb.append("</div>");
	}

	private String getPeriod(Translator cmpTranslator, DayNavComponent cmp) {
		Date startDate = cmp.getStartDate();
		Date endDate = cmp.getEndDate();
		String startPatternKey = DateUtils.yearFromDate(startDate) == DateUtils.yearFromDate(endDate)
				? "day.nav.pattern.day.month"
				: "day.nav.pattern.day.month.year";
		return cmpTranslator.translate("day.nav.period",
				format(cmpTranslator, startPatternKey, startDate),
				format(cmpTranslator, "day.nav.pattern.day.month.year", endDate));
	}

	private String format(Translator cmpTranslator, String patternKey, Date date) {
		try {
			return DateTimeFormatter.ofPattern(cmpTranslator.translate(patternKey), cmpTranslator.getLocale())
					.format(DateUtils.toLocalDate(date));
		} catch (IllegalArgumentException e) {
			return Formatter.getInstance(cmpTranslator.getLocale()).formatDate(date);
		}
	}

	private void renderWeekButton(StringOutput sb, URLBuilder ubu, Translator cmpTranslator, DayNavComponent cmp,
			String suffix, String i18nKey, String iconCss, String command) {
		String text = cmpTranslator.translate(i18nKey);
		String attributes = "title=\"" + StringHelper.escapeForHtmlAttribute(text) + "\"";
		String buttonText = "<i class=\"o_icon o_icon-fw " + iconCss + "\" aria-hidden=\"true\"></i><span class=\"sr-only\">"
				+ StringHelper.escapeHtml(text) + "</span>";
		NameValuePair nameValue = new NameValuePair(VelocityContainer.COMMAND_ID, command);
		renderButton(sb, ubu, cmp, getElementId(cmp, suffix), "btn btn-default btn-sm o_button_ghost o_day_nav_week_button", attributes, buttonText, nameValue);
	}
	
	private void renderDays(StringOutput sb, URLBuilder ubu, Translator cmpTranslator, DayNavComponent cmp) {
		Formatter formatter = Formatter.getInstance(cmpTranslator.getLocale());
		
		String elementId = cmp.getFormDispatchId();
		if (!StringHelper.containsNonWhitespace(elementId)) {
			elementId = "o_c" + cmp.getDispatchID();
		}
		sb.append("<div id=\"").append(elementId).append("\" class=\"o_day_nav");
		if (StringHelper.containsNonWhitespace(cmp.getElementCssClass())) {
			sb.append(" ").append(cmp.getElementCssClass());
		}
		sb.append("\">");
		sb.append("<ul class=\"list-unstyled o_day_nav_days\">");
		for (int i=0; i < 7; i++) {
			renderDay(sb, ubu, formatter, cmp, i);
		}
		sb.append("</ul>");
		sb.append("</div>");
	}

	private void renderDay(StringOutput sb, URLBuilder ubu, Formatter formatter, DayNavComponent cmp, int dayIndex) {
		Date day = DateUtils.addDays(cmp.getStartDate(), dayIndex);
		
		sb.append("<li class=\"o_day_nav_day");
		if (DateUtils.isWeekend(day)) {
			sb.append(" o_day_weekend");
		}
		sb.append("\">");
		
		boolean selected = cmp.getSelectedDateIndex() == dayIndex;
		String buttonCss = "o_day_button";
		if (selected) {
			buttonCss += " o_day_selected";
		} else if (DateUtils.isSameDay(day, new Date())) {
			buttonCss += " o_day_today";
		}
		String attributes = "aria-pressed=\"" + selected + "\"";
		
		StringBuilder buttonText = new StringBuilder();
		buttonText.append("<span class=\"o_day_abbr\">").append(StringHelper.escapeHtml(formatter.dayOfWeekShort(day))).append("</span>");
		buttonText.append("<span class=\"o_day_num\">").append(DateUtils.dayFromDate(day)).append("</span>");
		if (cmp.isMarked(dayIndex)) {
			buttonText.append("<span class=\"o_day_dot\" aria-hidden=\"true\"></span>");
		}
		NameValuePair nameValue = new NameValuePair(DayNavComponent.CMD_SELECT_DAY_INDEX, dayIndex);
		renderButton(sb, ubu, cmp, getElementId(cmp, String.valueOf(dayIndex)), buttonCss, attributes, buttonText.toString(), nameValue);
		sb.append("</li>");
	}
	
	private void renderButton(StringOutput sb, URLBuilder ubu, DayNavComponent cmp, String buttonId, String buttonCss,
			String attributes, String buttonText, NameValuePair nameValue) {
		sb.append("<button type=\"button\" id=\"").append(buttonId).append("\" class=\"").append(buttonCss).append("\" ");
		if (StringHelper.containsNonWhitespace(attributes)) {
			sb.append(attributes).append(" ");
		}
		
		if(cmp.isEnabled()) {
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
			if(cmp.getFormItem() != null) {
				sb.append(FormJSHelper.getXHRFnCallFor(cmp.getFormItem().getRootForm(), cmp.getFormDispatchId(), 1, false, true, false, nameValue));
				sb.append(";\"");
			} else {
				ubu.buildXHREvent(sb, "", false, true, nameValue);
				sb.append("\" ");
			}
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
			elementId = "o_dn" + cmp.getDispatchID();
		}
		return elementId + "_" + suffix;
	}

}
