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
import org.olat.core.util.Formatter;

/**
 * Description:<br>
 * TODO: patrickb Class Description for JSDateChooserRenderer
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserRenderer implements ComponentRenderer {

	private int maxlength = -1;

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
		// render textelement TODO:pb discuss with fj concatenation/composition of renderers.
		//ComponentRenderer txtelmRenderer = jsdcc.getTextElementComponent().getHTMLRendererSingleton();
		//txtelmRenderer.render(renderer,sb, jsdcc.getTextElementComponent(), ubu, translator, renderResult, args);


		String exDate = jsdcc.getExampleDateString();
		maxlength = exDate.length() + 4;
		
		StringOutput content = new StringOutput();
		renderTextElementPart(content, jsdcc.getTextElementComponent());
		
		//
		String triggerId = "trigger_" + jsdcc.getFormDispatchId();
		Translator sourceTranslator = jsdcc.getElementTranslator();

		
		/*
		 * add pop js for date chooser, if componente is enabled
		 */
		if (source.isEnabled()) {
			
			//date chooser button
			content.append("<span class=\"b_form_datechooser\" id=\"").append(triggerId).append("\" title=\"").append(StringEscapeUtils.escapeHtml(sourceTranslator.translate("calendar.choose"))).append("\">&nbsp;</span>");
			// date chooser javascript
			content.append("<script type=\"text/javascript\">\n /* <![CDATA[ */ \n").append("Calendar.setup({").append("inputField:\"").append(receiverId).append("\",")
					.append("ifFormat:\"");
			
			boolean timeFormat24 = true;
			if (jsdcc.getDateChooserDateFormat() == null) {
				// use default format from default locale file

				Formatter formatter = Formatter.getInstance(translator.getLocale());
				if (jsdcc.isDateChooserTimeEnabled()) {
					String dateTimePattern = formatter.getSimpleDatePatternForDateAndTime();
					timeFormat24 = dateTimePattern.indexOf("%I") < 0;
					content.append(dateTimePattern);
				}
				else content.append(formatter.getSimpleDatePatternForDate());

			} else {
				// use custom date format
				content.append(jsdcc.getDateChooserDateFormat());
			}
			// close calendar after choosing a date.
			content.append("\",").append("button:\"").append(triggerId).append("\",").append("align:\"Tl\",").append("singleClick:true,");
			if (jsdcc.isDateChooserTimeEnabled()) {
				content.append("showsTime:true,");
				content.append("timeFormat:\"").append(timeFormat24 ? "24" : "12").append("\",");
			}
			content.append("cache:true,").append("firstDay:1,").append("showOthers:true,");
			// Call on change method on input field to trigger dirty button
			TextElementComponent teC = jsdcc.getTextElementComponent();
			TextElementImpl te = teC.getTextElementImpl();
			content.append("onUpdate:function(){setFlexiFormDirty('").append(te.getRootForm().getDispatchFieldId()).append("')}");
			// Finish js code
			content.append("});").append("\n/* ]]> */ \n</script>");
			sb.append(content);
		} else{
			//readonly view
			FormJSHelper.appendReadOnly(content.toString(), sb);
		}

	}

	private void renderTextElementPart(StringOutput sb, Component source) {
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
	// TODO Auto-generated method stub

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
	// TODO Auto-generated method stub

	}

}
