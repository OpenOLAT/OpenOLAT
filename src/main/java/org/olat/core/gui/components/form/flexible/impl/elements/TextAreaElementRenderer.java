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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * renders TextAreaElement as HTML
 * <P>
 * Initial Date: 31.01.2008 <br>
 * 
 * @author rhaag
 */
class TextAreaElementRenderer extends DefaultComponentRenderer {


	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		//
		TextAreaElementComponent teC = (TextAreaElementComponent) source;
		TextAreaElementImpl te = teC.getTextAreaElementImpl();

		String id = teC.getFormDispatchId();
		
		String value = te.getValue();
		if(value == null){
			value = "";
		}
		
		// calculate rows height
		int rows = teC.getRows();
		if (teC.isAutoHeightEnabled()){
			// try to reduce screen flickering caused by the auto-height code. Search
			// for all line breaks and add for each a row. Maybe it will need even
			// more rows, but we can't do more at this point
			int buestEffortRowCount = value.split("\n").length;
			if (buestEffortRowCount == 0) buestEffortRowCount = 1;
			if (buestEffortRowCount > rows) rows = buestEffortRowCount;
		}
		// Escape HTMl entities
		value = StringHelper.escapeHtml(value);
		//
		if (!source.isEnabled()) {
			//read only view: rendered as fake textarea element for better styling options (e.g. print)
			sb.append("<span id=\"").append(id).append("\" ")
			  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
			  .append(" ><div id=\"")
			  .append(id)
			  .append("_disabled\" class='form-control textarea_disabled o_disabled o_form_element_disabled");
			if (teC.isFixedFontWidth()) {
				sb.append(" o_fixed_font_with");
			}
			sb.append("' style='");
			/* we do not add the width, not applied to text areas in oo despite configurable 
			if (teC.getCols() != -1) {
				sb.append(" width:").append(teC.getCols()).append("em;");
			}
			*/
			if (rows != -1) {
				sb.append(" height:").append(rows * 1.5).append("em;"); // line-height is about 1.5
			}
			sb.append("'>")
			  .append(value)
			  .append("</div></span>");
	
		} else {
			//read write view
			sb.append("<textarea id=\"")
			  .append(id)
			  .append("\" name=\"")
			  .append(id)
			  .append("\" class='form-control textarea");
			if (teC.isFixedFontWidth()) {
				sb.append(" o_fixed_font_with");
			}
			sb.append("'");
			if (teC.getCols() != -1) {
				sb.append(" cols=\"").append(teC.getCols()).append("\"");
			}
			if (rows != -1) {
				sb.append(" rows=\"").append(rows).append("\"");
			}
			if (te.hasPlaceholder()) {
				sb.append(" placeholder=\"").append(te.getPlaceholder()).append("\"");
			}
			if (te.hasFocus()) {
				sb.append(" autofocus");
			}
			sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
			  .append(" >")
			  .append(value)
			  .append("</textarea>")
			  .append(FormJSHelper.getJSStartWithVarDeclaration(id))
			//plain textAreas should not propagate the keypress "enter" (keynum = 13) as this would submit the form
			  .append(id+".on('keypress', function(event, target){if (13 == event.keyCode) {event.stopPropagation()} })")
			  .append(FormJSHelper.getJSEnd());
		}

		// resize element to fit content
		if (teC.isAutoHeightEnabled()) {
			int minSize = Math.max(90, (Math.abs(rows) * 20));
			sb.append("<script>\n")
			  .append("/* <![CDATA[ */\n")
			  .append("jQuery(function(){\n")
			  .append(" jQuery('#").append(id).append("').each(function () {\n")
			  .append("  this.setAttribute('style', 'height:' + (jQuery(this).outerHeight() > this.scrollHeight ? jQuery(this).outerHeight() : this.scrollHeight) + 'px;overflow-y:hidden;');\n")
			  .append(" }).on('input', function () {\n")
			  .append("  this.style.height = 'auto';\n")
			  .append("  this.style.height = (this.scrollHeight < ").append(minSize).append(" ? ").append(minSize).append(" : this.scrollHeight) + 'px';\n")
			  .append(" });\n")
			  .append("});\n")
			  .append("/* ]]> */\n")
			  .append("</script>\n");
		}
		
		if(source.isEnabled()){
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), teC.getFormDispatchId());
		}
		
		if (teC.isFixedFontWidth()) {
			sb.append(FormJSHelper.getJSStart())
			  .append("jQuery(function() {\n")
			  .append(" jQuery('#").append(id).append("').tabOverride();})")
			  .append(FormJSHelper.getJSEnd());
		}
	}
}
