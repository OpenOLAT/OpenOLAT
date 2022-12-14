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
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		//
		TextAreaElementComponent teC = (TextAreaElementComponent) source;
		TextAreaElementImpl te = teC.getFormItem();

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
			if (teC.isOriginalLineBreaks()) {
				sb.append(" o_original_line_breaks");
			}
			sb.append("' style='");
			if (rows != -1) {
				sb.append(" height:").append(rows * 1.45).append("em;"); // line-height is about 1.5
			}
			sb.append("'>")
			  .append(value)
			  .append("</div></span>");
	
		} else {
			//read write view
			if (teC.isLineNumbersEnabled()) {
				sb.append("<div class=\"o_textarea_line_numbers_container\" style='height:").append(rows * 1.45).append("em'>");
			}
			
			sb.append("<textarea id=\"")
			  .append(id)
			  .append("\" name=\"")
			  .append(id)
			  .append("\" class='form-control textarea");
			if (teC.isFixedFontWidth()) {
				sb.append(" o_fixed_font_with");
			}
			if (teC.isOriginalLineBreaks()) {
				sb.append(" o_original_line_breaks");
			}
			if (teC.isStripedBackgroundEnabled()) {
				sb.append(" o_striped_background");
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
			  .append("</textarea>");
			FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), id);
			FormJSHelper.appendPreventEnterPropagation(sb, id);
			
			if (teC.isLineNumbersEnabled()) {
				sb.append("<script>O_TEXTAREA.append_line_numbers('").append(id).append("')</script>");
				sb.append("</div>");
			}
			if (teC.getErrors() != null) {
				sb.append("<script>O_TEXTAREA.set_errors('").append(id).append("',").append(teC.getErrorsAsString()).append(")</script>");
			}
		}

		// resize element to fit content
		if (teC.isAutoHeightEnabled()) {
			int minSize = Math.max(90, (Math.abs(rows) * 20));
			sb.append("<script>\n")
			  .append("\"use strict\"\n")
			  .append("jQuery(function(){\n")
			  .append(" jQuery('#").append(id).append("').each(function () {\n")
			  .append("  this.setAttribute('style', 'height:' + (jQuery(this).outerHeight() > this.scrollHeight ? jQuery(this).outerHeight() : this.scrollHeight) + 'px;overflow-y:hidden;');\n")
			  .append(" }).on('input', function () {\n")
			  .append("  this.style.height = 'auto';\n")
			  .append("  this.style.height = (this.scrollHeight < ").append(minSize).append(" ? ").append(minSize).append(" : this.scrollHeight) + 'px';\n")
			  .append(" });\n")
			  .append("});\n")
			  .append("</script>\n");
		}
		
		if (teC.isFixedFontWidth()) {
			sb.append(FormJSHelper.getJSStart())
			  .append("jQuery(function() {\n")
			  .append(" jQuery('#").append(id).append("').tabOverride();})")
			  .append(FormJSHelper.getJSEnd());
		}
	}
}
