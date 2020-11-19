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

import static org.olat.core.util.StringHelper.escapeJavaScript;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import org.olat.core.util.i18n.I18nManager;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarComponentRenderer extends DefaultComponentRenderer {

	private static final DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String formatDate(Date date) {
		synchronized(formatDate) {
			return formatDate.format(date);
		}
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FullCalendarComponent fcC = (FullCalendarComponent)source;
		FullCalendarElement fcE = fcC.getCalendarElement();
		Form rootForm = fcE.getRootForm();
		String id = "o_c".concat(fcC.getDispatchID());
		String formId = fcE.getFormDispatchId();
		String printId = "fc_p".concat(fcC.getDispatchID());
		String configId = "fc_x".concat(fcC.getDispatchID());
		String aggregatedId = "fc_g".concat(fcC.getDispatchID());
		
		Locale regionalizedLocale = fcC.getTranslator().getLocale();
		regionalizedLocale = I18nManager.getInstance().getRegionalizedLocale(regionalizedLocale);
		Calendar cal = Calendar.getInstance(regionalizedLocale);
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
		String timeFormat = ampm ? "h(:mm) a" : "H.mm";
		
		sb.append("<script>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n")
		  .append(" var jCalendar = jQuery('#").append(id).append("');\n")
		  .append("	jCalendar.fullCalendar('destroy');\n")
		  .append("	jCalendar.fullCalendar( {\n")
		  .append("   header: {\n")
		  .append("     left: 'prev,print,next today',\n")
		  .append("     center: 'title',\n")
		  .append("     right: 'month,agendaWeek,agendaDay,listYear'\n")
		  .append("   },\n")
		  .append("   buttonText: {\n")
		  .append("     today: '").append(escapeJavaScript(translator.translate("cal.thisweek"))).append("',\n")
		  .append("     year: '").append(escapeJavaScript(translator.translate("cal.year"))).append("',\n")
		  .append("     month: '").append(escapeJavaScript(translator.translate("cal.month"))).append("',\n")
		  .append("     day: '").append(escapeJavaScript(translator.translate("cal.day"))).append("',\n")
		  .append("     week: '").append(escapeJavaScript(translator.translate("cal.week"))).append("',\n")
		  .append("     print: '").append(escapeJavaScript(translator.translate("print"))).append("',\n")
		  .append("   },\n")
		  .append("   monthNames: ").append(getMonthLong(translator)).append(",\n")
		  .append("   monthNamesShort: ").append(getMonthShort(translator)).append(",\n")
		  .append("   dayNames: ").append(getDayLong(translator)).append(",\n")
		  .append("   dayNamesShort: ").append(getDayShort(translator)).append(",\n")
		  .append("   allDayText:'").append(translator.translate("cal.form.allday")).append("',\n")
		  .append("   axisFormat:'").append(timeFormat).append("',\n")
		  .append("   timeFormat:'").append(timeFormat).append("',\n")
		  .append("   views: {\n")
		  .append("     month: {\n")
		  .append("       titleFormat: 'MMMM YYYY',\n")
		  .append("       columnHeaderFormat: 'ddd',\n")
		  .append("     },\n")
		  .append("     week: {\n")
		  .append("       titleFormat: ").append("'D MMM. YYYY'").append(",\n")
		  .append("       columnHeaderFormat: ").append(firstMonth ? "'ddd M/D'" : "'ddd D.M.'").append(",\n")
		  .append("       slotLabelFormat: '").append(timeFormat).append("',\n")
		  .append("     },\n")
		  .append("     day: {\n")
		  .append("       titleFormat: ").append(firstMonth ? "'dddd, MMM D, YYYY'" : "'dddd, D. MMM YYYY'").append(",\n")
		  .append("       columnHeaderFormat: ").append(firstMonth ? "'dddd M/D'" : "'dddd D.M.'").append(",\n")
		  .append("       slotLabelFormat: '").append(timeFormat).append("',\n")
		  .append("     },\n")
		  .append("   },\n")
		  .append("   timezone: false,\n")
		  .append("   firstDay:").append(firstDay).append(",\n")
		  .append("   weekNumberCalculation: 'ISO',\n")
		  .append("   defaultDate: moment('").append(formatDate(cal.getTime())).append("'),\n")
		  .append("   defaultView:'").append(fcC.getViewName()).append("',\n")
		  .append("   weekNumbers: true,\n")
		  .append("   editable: true,\n")
		  .append("   selectable: true,\n")
		  .append("   selectHelper: true,\n")
		  .append("	  eventSources:[");
		int count = 0;
		for(KalendarRenderWrapper calWrapper: fcC.getCalendars()) {
			if(fcC.isCalendarVisible(calWrapper)) {
				String calId = calWrapper.getKalendar().getCalendarID();
				String color = calWrapper.getCssClass();
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
		  .append("   eventRender: function(event, element, view) {\n")
		  .append("     if(view.type.lastIndexOf('list', 0) === 0) {\n")
		  .append("       if(event.location !== 'undefined' && event.location != null && event.location.length > 0) {\n")
		  .append("         element.append('<td class=\"fc-list-item-location fc-widget-content\"><span><i class=\"o_icon o_icon_home\"> </i> ' + event.location + '</span></td>');\n")
		  .append("       } else {\n")
		  .append("         element.append('<td class=\"fc-list-item-location fc-widget-content\"></td>');\n")
		  .append("       }\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   eventAfterRender: function(event, element, view) {\n")
		  .append("     element.each(function(index, el) {\n")
		  .append("       jQuery(el).attr('id', 'o_cev_' + view.name + '_' + event.id);\n")
		  .append("     });\n")
		  .append("   },\n")
		  .append("   eventAfterAllRender: function(event, element, view) {\n")
		  .append("     jQuery('table.fc-list-table td.fc-widget-header').each(function(index, el) {\n")
		  .append("       jQuery(el).attr('colspan',4);\n")
		  .append("     });\n")
		  .append("   },\n")
		  .append("   viewRender: function(view) {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evChangeView',view.name,'start',view.intervalStart.valueOf());\n")
		  .append("   },\n")
		  .append("	  eventDrop: function(calEvent, delta, revertFunc, jsEvent, ui, view) {\n")
		  .append("  	var allDay = calEvent.allDay;\n")
		  .append("  	var minuteDelta = delta.minutes() + (60 * delta.hours());\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evMove',calEvent.id,'dayDelta',delta.days(),'minuteDelta',minuteDelta,'allDay',allDay);\n")
		  .append("	  },\n")
		  .append("	  eventResize: function(calEvent, delta, revertFunc, jsEvent, ui, view) {\n")
		  .append("  	var allDay = calEvent.allDay;\n")
		  .append("  	var minuteDelta = delta.minutes() + (60 * delta.hours());\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evResize',calEvent.id,'dayDelta',delta.days(),'minuteDelta',minuteDelta,'allDay',allDay);\n")
		  .append("	  },\n")
		  .append("   select: function(startDate, endDate, jsEvent, view) {\n")
		  .append("  	var allDay = !startDate.hasTime();\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evAdd','new','start',startDate.toISOString(),'end',endDate.toISOString(),'allDay',allDay);\n")
		  .append("   },\n")
		  .append("   eventClick: function(calEvent, jsEvent, view) {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evSelect',calEvent.id,'evDomId','o_cev_' + view.name + '_' + calEvent.id);\n")
		  .append("   }\n")
		  .append(" });\n")

		//print button
		  .append(" jQuery('.fc-left').append('<span class=\"fc-header-space\"></span><button id=\"").append(printId).append("\" class=\"fc-button fc-button-print fc-state-default fc-corner-left fc-corner-right\">")
		  .append(" <span title=\"").append(translator.translate("print")).append("\"><i class=\"o_icon o_icon_print\"> </i></span></button>');\n")      
		  .append(" jQuery('.fc-button-print').click(function () {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("   o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'print','print');\n")
		  .append(" });\n");
		if(fcC.isConfigurationEnabled()) {
			//config button
			sb.append(" jQuery('.fc-left').append('<span class=\"fc-header-space\"></span><button id=\"").append(configId).append("\" class=\"fc-button fc-button-config fc-state-default fc-corner-left fc-corner-right\">")
			  .append(" <span title=\"").append(translator.translate("cal.configuration.tooltip")).append("\"><i class=\"o_icon o_icon_customize\"> </i></span></button>');\n")      
			  .append(" jQuery('.fc-button-config').click(function () {\n")
			  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
			  .append("   o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'config', 'config');\n")
			  .append(" });\n");
		}
		if(fcC.isAggregatedFeedEnabled()) {
			//aggregated button
			sb.append(" jQuery('.fc-left').append('<span class=\"fc-header-space\"></span><button id=\"").append(aggregatedId).append("\" class=\"fc-button fc-button-aggregate fc-state-default fc-corner-left fc-corner-right\">")
			  .append("<span title=\"").append(translator.translate("cal.icalfeed.aggregated.tooltip")).append("\"><i class=\"o_icon o_icon_rss\"> </i></span></button>');\n")      
			  .append(" jQuery('.fc-button-aggregate').click(function () {\n")
			  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
			  .append("   o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'aggregate','aggregate');\n")
			  .append(" });\n");
		}
      
		sb.append("});\n")
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