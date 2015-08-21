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
package org.olat.commons.calendar.ui.components;

import static org.apache.commons.lang.StringEscapeUtils.escapeJavaScript;

import java.text.DateFormat;
import java.util.Calendar;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FullCalendarComponent fcC = (FullCalendarComponent)source;
		FullCalendarElement fcE = fcC.getCalendarElement();
		Form rootForm = fcE.getRootForm();
		String id = "o_c" + fcC.getDispatchID();
		String formId = fcE.getFormDispatchId();
		String printId = "fc_p" + fcC.getDispatchID();
		
		Calendar cal = Calendar.getInstance(fcC.getTranslator().getLocale());
		int firstDay = cal.getFirstDayOfWeek() - 1;
		cal = Calendar.getInstance();
		cal.setTime(fcC.getCurrentDate());
		
		Calendar calRef = Calendar.getInstance(fcC.getTranslator().getLocale());
		calRef.set(Calendar.DATE, 25);
		calRef.set(Calendar.MONTH, 11);
		calRef.set(Calendar.YEAR, 2013);

		String formatted = DateFormat.getDateInstance(DateFormat.SHORT, fcC.getTranslator().getLocale()).format(calRef.getTime());
		boolean firstMonth = (formatted.indexOf("12") < formatted.indexOf("25"));
		
		String amFormatted = DateFormat.getTimeInstance(DateFormat.SHORT, fcC.getTranslator().getLocale()).format(calRef.getTime());
		boolean ampm = amFormatted.contains("AM") || amFormatted.contains("PM");
		
		sb.append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */ \n")
	    .append("jQuery(function() {\n")
      .append("	jQuery('#").append(id).append("').fullCalendar( {\n")
      .append("   header: {\n")
      .append("     left: 'prev,print,next today',\n")
      .append("     center: 'title',\n")
      .append("     right: 'month,agendaWeek,agendaDay'\n")
      .append("   },\n")
      .append("   buttonText: {\n")
      .append("     today: '").append(escapeJavaScript(translator.translate("cal.thisweek"))).append("',\n")
      .append("     month: '").append(escapeJavaScript(translator.translate("cal.month"))).append("',\n")
      .append("     day: '").append(escapeJavaScript(translator.translate("cal.day"))).append("',\n")
      .append("     week: '").append(escapeJavaScript(translator.translate("cal.week"))).append("',\n")
      .append("     print: '").append(escapeJavaScript(translator.translate("print"))).append("'\n")
      .append("   },\n")
      .append("   monthNames: ").append(getMonthLong(translator)).append(",\n")
      .append("   monthNamesShort: ").append(getMonthShort(translator)).append(",\n")
      .append("   dayNames: ").append(getDayLong(translator)).append(",\n")
      .append("   dayNamesShort: ").append(getDayShort(translator)).append(",\n")
      .append("   allDayText:'").append(translator.translate("cal.form.allday")).append("',\n")
      .append("   axisFormat:'").append(ampm ? "h(:mm)tt" : "H.mm").append("',\n")
      .append("   timeFormat:'").append(ampm ? "h(:mm)tt" : "H.mm").append("',\n")
      .append("   titleFormat: {\n")
      .append("     month: 'MMMM yyyy',\n")
      .append("     week: ").append(firstMonth ? "\"MMM d[ yyyy]{ '&#8212;'[ MMM] d yyyy}\"" : "\"d. [ MMM] [ yyyy]{ '&#8212;' d. MMM yyyy}\"").append(",\n")
      .append("     day: ").append(firstMonth ? "'dddd, MMM d, yyyy'" : "'dddd, d. MMM yyyy'").append("\n")
      .append("   },\n")
      .append("   columnFormat: {\n")
      .append("     month: 'ddd',\n")
      .append("     week: ").append(firstMonth ? "'ddd M/d'" : "'ddd d.M.'").append(",\n")
      .append("     day: ").append(firstMonth ? "'dddd M/d'" : "'dddd d.M.'").append("\n")
      .append("   },\n")
      .append("   year:").append(cal.get(Calendar.YEAR)).append(",\n")
      .append("   month:").append(cal.get(Calendar.MONTH)).append(",\n")
      .append("   date:").append(cal.get(Calendar.DAY_OF_MONTH)).append(",\n")
      .append("   firstDay:").append(firstDay).append(",\n")
      .append("   defaultView:'").append(fcC.getViewName()).append("',\n")
      .append("   weekNumbers: true,\n")
      .append("   editable: true,\n")
      .append("   selectable: true,\n")
      .append("   selectHelper: true,\n")
      .append("	  eventSources:[");
		int count = 0;
		for(KalendarRenderWrapper calWrapper: fcC.getKalendarRenderWrappers()) {
			if(calWrapper.getKalendarConfig().isVis()) {
				String calId = calWrapper.getKalendar().getCalendarID();
				String color = calWrapper.getKalendarConfig().getCss();
				if(StringHelper.containsNonWhitespace(color) && color.startsWith("o_cal_")) {
					color = color.substring(6, color.length());
				}
				if(count++ != 0) sb.append(",");
				sb.append("{\n")
				  .append("   url:'").append(fcC.getMapperUrl()).append("/").append(calId).append(".json',\n")
				  .append("   color:'").append(color).append("'\n")
				  .append("}");
			}
		}
    sb.append("   ],\n")
      .append("   eventAfterRender: function(event, element, view) {\n")
      .append("     element.each(function(index, el) {\n")
      .append("       jQuery(el).attr('id', 'o_cev_' + view.name + '_' + event.id);\n")
      .append("     });\n")
      .append("   },\n")
      .append("   viewDisplay: function(view) {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
      .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'evChangeView',view.name,'start',view.start.getTime());\n")
      .append("   },\n")
      .append("	  eventDrop: function(calEvent, dayDelta, minuteDelta, allDay, revertFunc, jsEvent, ui, view) {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
      .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'evMove',calEvent.id,'dayDelta',dayDelta,'minuteDelta',minuteDelta,'allDay',allDay);\n")
      .append("	  },\n")
      .append("   select: function(startDate, endDate, allDay, jsEvent, view) {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
      .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'evAdd','new','start',startDate.getTime(),'end',endDate.getTime(),'allDay',allDay);\n")
      .append("   },\n")
      .append("   eventClick: function(calEvent, jsEvent, view) {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
      .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'evSelect',calEvent.id,'evDomId','o_cev_' + view.name + '_' + calEvent.id);\n")
      .append("   }\n")
      .append(" });\n")
      .append(" jQuery('.fc-header-left').append('<span class=\"fc-header-space\"></span><span id=\"").append(printId).append("\" class=\"fc-button fc-button-print fc-state-default fc-corner-left fc-corner-right\"><span>")
                 .append(translator.translate("print")).append("</span></span>');\n")
      .append(" jQuery('.fc-button-print').click(function () {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
      .append("   o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'print','print');\n")
      .append(" });\n")
      .append("});\n")
      .append("/* ]]> */\n")
      .append("</script>");
	}
	
	private String getDayShort(Translator translator) {
		StringBuilder sb = new StringBuilder();
		sb.append("['")
		  .append(escapeJavaScript(translator.translate("day.short.so"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.mo"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.di"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.mi"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.do"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.fr"))).append("','")
		  .append(escapeJavaScript(translator.translate("day.short.sa"))).append("']");
		return sb.toString();
	}
	
	private String getDayLong(Translator translator) {
		StringBuilder sb = new StringBuilder();
		sb.append("['")
		  .append(escapeJavaScript(translator.translate("cal.sun"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.mon"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.tue"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.wed"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.thu"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.fri"))).append("','")
		  .append(escapeJavaScript(translator.translate("cal.sat"))).append("']");
		return sb.toString();
	}
	
	private String getMonthLong(Translator translator) {
		StringBuilder sb = new StringBuilder();
		sb.append("['")
		  .append(escapeJavaScript(translator.translate("month.long.jan"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.feb"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.mar"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.apr"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.mai"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.jun"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.jul"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.aug"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.sep"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.oct"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.nov"))).append("','")
	    .append(escapeJavaScript(translator.translate("month.long.dec"))).append("']");
		return sb.toString();
	}
	
	private String getMonthShort(Translator translator) {
		StringBuilder sb = new StringBuilder();
		sb.append("['")
		  .append(escapeJavaScript(translator.translate("month.short.jan"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.feb"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.mar"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.apr"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.mai"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.jun"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.jul"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.aug"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.sep"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.oct"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.nov"))).append("','")
      .append(escapeJavaScript(translator.translate("month.short.dec"))).append("']");
		return sb.toString();
	}
}