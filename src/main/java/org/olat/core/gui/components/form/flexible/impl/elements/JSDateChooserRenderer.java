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
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * TODO: patrickb Class Description for JSDateChooserRenderer
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserRenderer implements ComponentRenderer {



	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		JSDateChooserComponent jsdcc = (JSDateChooserComponent) source;

		String receiverId = jsdcc.getTextElementComponent().getFormDispatchId();

		String exDate = jsdcc.getExampleDateString();
		int maxlength = exDate.length() + 4;
		
		StringOutput dc = new StringOutput();
		renderTextElementPart(dc, jsdcc.getTextElementComponent(), maxlength);

		String triggerId = "trigger_" + jsdcc.getFormDispatchId();
		Translator sourceTranslator = jsdcc.getElementTranslator();
		Translator dateTranslator = Util.createPackageTranslator(JSDateChooserRenderer.class, translator.getLocale());

		//add pop js for date chooser, if componente is enabled
		if (source.isEnabled()) {
			String format = jsdcc.getDateChooserDateFormat();
			TextElementComponent teC = jsdcc.getTextElementComponent();
			TextElementImpl te = teC.getTextElementImpl();

			//date chooser button
			dc.append("<span class=\"b_form_datechooser\" id=\"").append(triggerId).append("\" title=\"").append(StringEscapeUtils.escapeHtml(sourceTranslator.translate("calendar.choose"))).append("\"")
			  .append(" onclick=\"jQuery('#").append(receiverId).append("').datepicker('show');\"")
			  .append(">&nbsp;</span>");
			// date chooser javascript
			dc.append("<script type=\"text/javascript\">\n /* <![CDATA[ */ \n")
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
				.append("  showOtherMonths:true,\n")
				.append("  onSelect:function(){\n")
				.append("    setFlexiFormDirty('").append(te.getRootForm().getDispatchFieldId()).append("')")
				.append("  }\n")
			  .append("})});")
			  .append("\n/* ]]> */ \n</script>");
			
			//input fields for hour and minute
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

				renderMS(dc, "o_dch_" + receiverId, hour);
				dc.append(" : ");
				renderMS(dc, "o_dcm_" + receiverId, minute);
			}
			sb.append(dc);
		} else{
			//readonly view
			FormJSHelper.appendReadOnly(dc.toString(), sb);
		}
	}
	
	private StringOutput renderMS(StringOutput dc, String id, int time) {
		dc.append("<input type=\"text\" id=\"").append(id).append("\"")
	    .append(" name=\"").append(id).append("\" size=\"2\"")
		  .append(" maxlength=\"2\"").append("\" value=\"").append(time).append("\"")
		  .append(" />");
		return dc;
	}

	private void renderTextElementPart(StringOutput sb, Component source, int maxlength) {
		TextElementComponent teC = (TextElementComponent) source;
		TextElementImpl te = teC.getTextElementImpl();

		//display size cannot be bigger the maxlenght given by dateformat
		te.displaySize = te.displaySize <= maxlength ? te.displaySize : maxlength;
		
		String id = teC.getFormDispatchId();
		//
		if (!source.isEnabled()) {
			//read only view
			String value = te.getValue();
			if(value == null){
				value = "";
			}
			value = StringEscapeUtils.escapeHtml(value);
			sb.append("<span\" id=\"");
			sb.append(id);
			sb.append("\" ");
			sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()));
			sb.append("title=\"");
			sb.append(value); //the uncutted value in tooltip
			sb.append("\">");
			String shorter;
			//beautify
			if(value.length() != te.displaySize && value.length() > te.displaySize - 3){
				shorter = value.substring(0,te.displaySize-4);
				shorter += "...";
			}else{
				int fill = te.displaySize - value.length();
				shorter = value;
				for(int i=0; i <= fill; i++){
					shorter += "&nbsp;";
				}
			}				
			sb.append("<input disabled=\"disabled\" class=\"b_form_element_disabled\" size=\"");
			sb.append(te.displaySize);
			sb.append("\" value=\"");		
			sb.append(shorter);
			sb.append("\" />");	
			sb.append("</span>");
			
		} else {
			//read write view
			sb.append("<input type=\"text\" id=\"");
			sb.append(id);
			sb.append("\" name=\"");
			sb.append(id);
			sb.append("\" size=\"");
			sb.append(te.displaySize);
			sb.append("\" maxlength=\"");
			sb.append(maxlength);
			sb.append("\" value=\"");
			sb.append(StringEscapeUtils.escapeHtml(te.getValue()));
			sb.append("\" ");
			sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()));
			sb.append("/>");
		}

		if(source.isEnabled()){
			//add set dirty form only if enabled
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(teC.getFormDispatchId()));
			/* deactivated due OLAT-3094 and OLAT-3040
			if(te.hasFocus()){
				sb.append(FormJSHelper.getFocusFor(teC.getFormDispatchId()));
			}
			*/
			sb.append(FormJSHelper.getSetFlexiFormDirty(te.getRootForm(), teC.getFormDispatchId()));
			sb.append(FormJSHelper.getJSEnd());
		}
		
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		//
	}
}
