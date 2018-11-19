/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserRenderer extends DefaultComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		JSDateChooserComponent jsdcc = (JSDateChooserComponent) source;
		TextElementComponent teC = jsdcc.getTextElementComponent();
		String receiverId = teC.getFormDispatchId();

		String exDate = jsdcc.getExampleDateString();
		int maxlength = exDate.length() + 4;
		
		String triggerId = "trigger_" + jsdcc.getFormDispatchId();
		Translator sourceTranslator = jsdcc.getElementTranslator();
		Translator dateTranslator = Util.createPackageTranslator(JSDateChooserRenderer.class, translator.getLocale());

		//add pop js for date chooser, if componente is enabled
		sb.append("<div class='o_date form-inline'>");
		if (source.isEnabled()) {
			String format = jsdcc.getDateChooserDateFormat();
			TextElementImpl te = teC.getTextElementImpl();

			sb.append("<div class='form-group'><div class='input-group o_date_picker'>");
			renderTextElement(sb, teC, maxlength);

			//date chooser button
			sb.append("<span class='input-group-addon'>")
			  .append("<i class='o_icon o_icon_calendar' id=\"").append(triggerId).append("\" title=\"").append(StringEscapeUtils.escapeHtml(sourceTranslator.translate("calendar.choose"))).append("\"")
			  .append(" onclick=\"jQuery('#").append(receiverId).append("').datepicker('show');\"")
			  .append("></i></span>")
			  .append("</div></div>");//input-group
			// date chooser javascript
			sb.append("<script>\n /* <![CDATA[ */ \n")
				.append("jQuery(function(){ jQuery('#").append(receiverId).append("').datepicker({\n")
				.append("  dateFormat:'").append(format).append("',\n")
				.append("  firstDay:1,\n")
				.append("  monthNames:[")
				  .append("'").append(dateTranslator.translate("month.long.jan")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.feb")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.mar")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.apr")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.mai")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.jun")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.jul")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.aug")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.sep")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.oct")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.nov")).append("',")
				  .append("'").append(dateTranslator.translate("month.long.dec")).append("'")
				.append("],\n")
				.append("  dayNamesMin:[")
				  .append("'").append(dateTranslator.translate("day.short.so")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.mo")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.di")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.mi")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.do")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.fr")).append("',")
				  .append("'").append(dateTranslator.translate("day.short.sa")).append("'")
				.append("],\n")
				.append("  showOtherMonths:true,\n");
			if(jsdcc.getFormItem().getDefaultValue() != null) {
				String id = ((JSDateChooser)jsdcc.getFormItem().getDefaultValue()).getTextElementComponent().getFormDispatchId();
				sb.append("  beforeShow:function(el, inst) {\n")
				  .append("    var defDate = jQuery('#").append(id).append("').datepicker('getDate');\n")
				  .append("    jQuery('#").append(receiverId).append("').datepicker('option', 'defaultDate', defDate);")
				  .append("  },\n");
			}
			sb.append("  onSelect:function(){\n")
			  .append("    setFlexiFormDirty('").append(te.getRootForm().getDispatchFieldId()).append("');\n")
			  .append("    jQuery(this).change();\n")
			  .append("  }\n")
			  .append("})});")
			  .append("\n/* ]]> */ \n</script>");
			
			//input fields for hour and minute
			if (jsdcc.isDateChooserTimeEnabled()) {
				int hour, minute;
				Date currentDate = jsdcc.getDate();
				if(currentDate == null) {
					if(jsdcc.isDefaultTimeAtEndOfDay()) {
						hour = 23;
						minute = 59;
					} else {
						hour = minute = 0;
					}
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(currentDate);
					hour = cal.get(Calendar.HOUR_OF_DAY);
					minute = cal.get(Calendar.MINUTE);
				}
				sb.append("<div class='form-group o_date_ms'>");
				renderMS(sb, "o_dch_" + receiverId, hour);
				sb.append(" : ");
				renderMS(sb, "o_dcm_" + receiverId, minute);
				sb.append("</div>");
			}
		} else{
			renderTextElementReadonly(sb, jsdcc, maxlength);
		}
		sb.append("</div>");
	}

	private void renderTextElementReadonly(StringOutput sb, JSDateChooserComponent jsdcc, int maxlength) {
		TextElementComponent teC = jsdcc.getTextElementComponent();
		TextElementImpl te = teC.getTextElementImpl();
		//display size cannot be bigger the maxlenght given by dateformat
		te.displaySize = te.displaySize <= maxlength ? te.displaySize : maxlength;
		
		String id = teC.getFormDispatchId();
		//read only view
		String value = te.getValue();
		if(value == null){
			value = "";
		}
		value = StringEscapeUtils.escapeHtml(value);
		sb.append("<span id='").append(id).append("_wp' ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
		  .append("title=\"").append(value).append("\">");
		String shorter;
		//beautify
		if(value.length() != te.displaySize && value.length() > te.displaySize - 3){
			shorter = value.substring(0,te.displaySize-4);
			shorter += "...";
		} else {
			int fill = te.displaySize - value.length();
			shorter = value;
			for(int i=0; i <= fill; i++){
				shorter += "&nbsp;";
			}
		}				
		sb.append("<input id='").append(id).append("' disabled='disabled' class='form-control o_disabled' size=\"")
		  .append(te.displaySize)
		  .append("\" value=\"").append(shorter).append("\" /></span>");
		
		if (jsdcc.isDateChooserTimeEnabled()) {
			int hour, minute;
			Date currentDate = jsdcc.getDate();
			if(currentDate == null) {
				hour = minute = 0;
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				hour = cal.get(Calendar.HOUR_OF_DAY);
				minute = cal.get(Calendar.MINUTE);
			}
			sb.append("<div class='form-group o_date_ms'>");
			renderMSDisabled(sb, "o_dch_" + id, hour);
			sb.append(" : ");
			renderMSDisabled(sb, "o_dcm_" + id, minute);
			sb.append("</div>");
		}
	}
	
	private StringOutput renderMS(StringOutput dc, String id, int time) {
		dc.append("<input class='form-control o_date_ms' type='text' id='").append(id).append("'")
	      .append(" name=\"").append(id).append("\" size='2'")
		  .append(" maxlength='2' value='").append(time > 9 ? "" + time : "0" + time).append("'")
		  .append(" />");
		return dc;
	}
	
	private StringOutput renderMSDisabled(StringOutput dc, String id, int time) {
		dc.append("<input class='form-control o_disabled o_date_ms' disabled='disabled' type='text' id='").append(id).append("'")
	      .append(" name=\"").append(id).append("\" size='2'")
		  .append(" maxlength='2' value='").append(time > 9 ? "" + time : "0" + time).append("'")
		  .append(" />");
		return dc;
	}
	
	private void renderTextElement(StringOutput sb, TextElementComponent teC, int maxlength) {
		TextElementImpl te = teC.getTextElementImpl();
		String id = teC.getFormDispatchId();
		//display size cannot be bigger the maxlenght given by dateformat
		te.displaySize = te.displaySize <= maxlength ? te.displaySize : maxlength;
	
		//read write view
		sb.append("<input type=\"text\" class='form-control o_date_day' id=\"")
		  .append(id).append("\" name=\"").append(id)
		  .append("\" size=\"").append(te.displaySize)
		  .append("\" maxlength=\"").append(maxlength)
		  .append("\" value=\"").append(StringEscapeUtils.escapeHtml(te.getValue())).append("\" ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
		  .append("/>");
		//add set dirty form only if enabled
		FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), teC.getFormDispatchId());
	}
}
