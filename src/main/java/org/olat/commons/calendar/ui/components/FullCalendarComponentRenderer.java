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

	private final DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
	
	private final String formatDate(Date date) {
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
		
		Locale locale = fcC.getTranslator().getLocale();
		Locale regionalizedLocale = I18nManager.getInstance().getRegionalizedLocale(locale);
		Calendar cal = Calendar.getInstance(regionalizedLocale);
		int firstDay = cal.getFirstDayOfWeek() - 1;
		cal = Calendar.getInstance();
		cal.setTime(fcC.getCurrentDate());
		
		Calendar calRef = Calendar.getInstance(locale);
		calRef.set(Calendar.DATE, 25);
		calRef.set(Calendar.MONTH, 11);
		calRef.set(Calendar.YEAR, 2013);
		
		String amFormatted = DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(calRef.getTime());
		boolean ampm = amFormatted.contains("AM") || amFormatted.contains("PM");

		sb.append("<script>\n")
		  .append("jQuery(function() {\n")
		  .append(" try {\n")
		  .append("  var calendarEl = jQuery('#").append(id).append("').get(0);\n")
		  .append("  var calendar = new FullCalendar.Calendar(calendarEl, {\n")
		  .append("   customButtons: {\n");
		if(fcC.isConfigurationEnabled()) { 
			sb.append("     configuration: {\n")
			  .append("       click: function() {\n")
			  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
			  .append("         o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'config', 'config');\n")
			  .append("       }\n")
			  .append("     },\n");
		}
		if(fcC.isAggregatedFeedEnabled()) {
			sb.append("     aggregatedfeed: {\n")
			  .append("       click: function() {\n")
			  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
			  .append("         o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'aggregate', 'aggregate');\n")
			  .append("       }\n")
			  .append("     },\n");
		}
		sb.append("     print: {\n")
		  .append("       click: function() {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("         o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'print', 'print');\n")
		  .append("       }\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   headerToolbar: {\n")
		  .append("     left: 'prev,next today print").append(" configuration", fcC.isConfigurationEnabled()).append(" aggregatedfeed", fcC.isAggregatedFeedEnabled()).append("',\n")
		  .append("     center: 'title',\n")
		  .append("     right: 'dayGridMonth,timeGridWeek,timeGridDay,listYear'\n")
		  .append("   },\n")
		  .append("   buttonText: {\n")
		  .append("     today: '").append(escapeJavaScript(translator.translate("cal.thisweek"))).append("',\n")
		  .append("     year: '").append(escapeJavaScript(translator.translate("cal.year"))).append("',\n")
		  .append("     month: '").append(escapeJavaScript(translator.translate("cal.month"))).append("',\n")
		  .append("     day: '").append(escapeJavaScript(translator.translate("cal.day"))).append("',\n")
		  .append("     week: '").append(escapeJavaScript(translator.translate("cal.week"))).append("',\n")
		  .append("     print: '").append(escapeJavaScript(translator.translate("print"))).append("',\n")
		  .append("   },\n")
		  .append("   eventTimeFormat: { hour: 'numeric', minute: '2-digit', meridiem: ").append("'short'", "false", ampm).append(" },\n")
		  .append("   locale: '").append(regionalizedLocale.getLanguage()).append("',\n")
		  .append("   views: {\n")
		  .append("     month: {\n")
		  .append("       titleFormat: { year: 'numeric', month: 'long' }\n")
		  .append("     },\n")
		  .append("     week: {\n")
		  .append("       titleFormat: { day: 'numeric', month: 'short', year: 'numeric' },\n")
		  .append("       titleRangeSeparator: ' \u2013 ',\n")
		  .append("       slotLabelFormat: { hour: 'numeric', ").append(" minute:'2-digit',", !ampm).append(" meridiem: ").append("'short'", "false", ampm).append(" }\n")
		  .append("     },\n")
		  .append("     day: {\n")
		  .append("       slotLabelFormat: { hour: 'numeric', ").append(" minute:'2-digit',", !ampm).append(" meridiem: ").append("'short'", "false", ampm).append(" }\n")
		  .append("     },\n")
		  .append("   },\n")
		  //.append("   timezone: false,\n")
		  .append("   firstDay:").append(firstDay).append(",\n")
		  .append("   initialDate: '").append(formatDate(cal.getTime())).append("',\n")
		  .append("   initialView:'").append(fcC.getViewName()).append("',\n")
		  .append("   weekNumbers: true,\n")
		  .append("   editable: true,\n")
		  .append("   selectable: true,\n")
		  .append("   eventSources:[");
		
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
		  .append("   eventDidMount: function(info) {\n")
		  .append("     info.el.setAttribute('id', 'o_cev_' + info.view.type + '_' + info.event.id);\n")
		  .append("     if(info.view.type.lastIndexOf('list', 0) === 0) {\n")
		  .append("       if(info.event.extendedProps.location !== 'undefined' && info.event.extendedProps.location != null && info.event.extendedProps.location.length > 0) {\n")
		  .append("         jQuery(info.el).append('<td class=\"fc-list-event-location\"><span><i class=\"o_icon o_icon_home\"> </i> ' + info.event.extendedProps.location + '</span></td>');\n")
		  .append("       } else {\n")
		  .append("         jQuery(info.el).append('<td class=\"fc-list-event-location\"><span> </span></td>');\n")
		  .append("       }\n")
		  .append("       jQuery('table.fc-list-table th').attr('colspan',4);\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   viewDidMount: function(info) {\n")
		 .append("     jQuery('button.fc-print-button').attr('id','").append(printId).append("').attr('title','").append(translator.translate("print")).append("')\n")
		  .append("       .empty().prepend('<i class=\"o_icon o_icon_print\"> </i>');\n")
		  .append("     jQuery('button.fc-configuration-button').attr('id','").append(configId).append("').attr('title','").append(translator.translate("cal.configuration.tooltip")).append("')\n")
		  .append("       .empty().prepend('<i class=\"o_icon o_icon_customize\"> </i>');\n")
		  .append("     jQuery('button.fc-aggregatedfeed-button').attr('id','").append(aggregatedId).append("').attr('title','").append(translator.translate("cal.icalfeed.aggregated.tooltip")).append("')\n")
		  .append("       .empty().prepend('<i class=\"o_icon o_icon_rss\"> </i>');\n")
		  .append("     jQuery('table.fc-list-table th').attr('colspan',4);\n")
		  .append("   },\n")
		  .append("   datesSet: function(info) {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHRNFEvent(formNam, dispIdField, dispId, eventIdField, eventInt, 'evChangeView', info.view.type, 'evChangeDates', info.view.currentStart.valueOf());\n")
		  .append("   },\n")
		  .append("   eventDrop: function(eventDropInfo) {\n")
		  .append("     var delta = eventDropInfo.delta;\n")
		  .append("     var minuteDelta = delta.milliseconds / 60000;\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evMove', eventDropInfo.event.id, 'dayDelta', delta.days, 'minuteDelta', minuteDelta, 'allDay', eventDropInfo.event.allDay);\n")
		  .append("   },\n")
		  .append("   eventResize: function(eventResizeInfo) {\n")
		  .append("     var allDay = eventResizeInfo.event.allDay;\n")
		  .append("     var delta = eventResizeInfo.endDelta;\n")
		  .append("     var minuteDelta = delta.milliseconds / 60000;\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evResize', eventResizeInfo.event.id, 'dayDelta', delta.days, 'minuteDelta', minuteDelta, 'allDay', allDay);\n")
		  .append("   },\n")
		  .append("   dateClick: function(startDate) {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evAdd','new','start',startDate.dateStr,'end',startDate.dateStr,'allDay',startDate.allDay);\n")
		  .append("   },\n")
		  .append("   eventClick: function(info) {\n")
		  .append(FormJSHelper.generateXHRFnCallVariables(rootForm, formId, 1))
		  .append("     o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, true, false, false, 'evSelect', info.event.id, 'evDomId', 'o_cev_' + info.view.type + '_' + info.event.id);\n")
		  .append("   }\n")
		  .append("  });\n")
		  .append("  o_info.calendar = calendar;\n")
		  .append("  calendar.render();\n")
		  .append(" } catch(e) { if(window.console) console.log(e);\n };\n")
		  .append("});\n")
		  .append("</script>");
	}
}