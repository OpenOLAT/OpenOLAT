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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 4 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MarkdownElementRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		MarkdownComponent component = (MarkdownComponent) source;
		MarkdownElement element = (MarkdownElement)component.getFormItem();
		
		String inputElementId = element.getFormDispatchId();
		String editElementId = element.getFormDispatchId() + "_edit";
		
		// Hidden dom element with the raw markdown. This is submitted.
		sb.append("<input type=\"hidden\"");
		sb.append(" id=\"").append(inputElementId).append("\"");
		sb.append(" name=\"").append(inputElementId).append("\"");
		sb.append(" value=\"").append(element.getValue()).append("\"");
		sb.append(FormJSHelper.getRawJSFor(element.getRootForm(), inputElementId, element.getAction(), false, null, editElementId));
		sb.append(">");
		
		// Visible dom element with the editor.
		sb.append("<div ");
		sb.append(" id=\"").append(editElementId).append("\"");
		sb.append(" class=\"o_markdown_element ");
		if (StringHelper.containsNonWhitespace(element.getElementCssClass())) {
			sb.append(element.getElementCssClass());
		}
		sb.append("\" ");
		sb.append(">");
		sb.append("</div>");
		
		if (element.isEnabled()) {
			sb.append(FormJSHelper.getJSStart());
			sb.append("var updateListener = function(markdown) {");
			sb.append("  document.getElementById(\"").append(inputElementId).append("\").value = markdown;");
			sb.append("  setFlexiFormDirty(\"").append(element.getRootForm().getDispatchFieldId()).append("\", false);");
			sb.append("};");
			if (element.getAction() == FormEvent.ONCHANGE) {
				sb.append("var onBlur = function() { ");
				sb.append("  document.getElementById(\"").append(inputElementId).append("\").dispatchEvent(new Event('change'));");
				sb.append("};");
			} else {
				sb.append("var onBlur = function() {};");
			}
			sb.append("oomilkdown.ooMdEditFormElement(")
					.append(editElementId)
					.append(",").append("'").append(StringHelper.escapeJavaScriptParam(element.getValue())).append("'")
					.append(",").append("updateListener")
					.append(",").append("onBlur")
					.append(");");
			sb.append(FormJSHelper.getJSEnd());
		} else {
			sb.append(FormJSHelper.getJSStart());
			sb.append("oomilkdown.ooMdView(")
					.append(editElementId).append(",")
					.append("'").append(StringHelper.escapeJavaScriptParam(element.getValue())).append("');");
			sb.append(FormJSHelper.getJSEnd());
		}
		
		if (element.isAutosave()) {
			Form form = element.getRootForm();
			sb.append(FormJSHelper.getJSStart());
			sb.append("jQuery(function() {\n");
			sb.append("var periodic = jQuery.periodic({period: 60000, decay:1.0, max_period: Number.MAX_VALUE }, function() {");
			sb.append("try {");
			sb.append("  if(jQuery('#").append(inputElementId).append("').length > 0) {");
			sb.append("    var text = jQuery('#").append(inputElementId).append("').val();");
			sb.append(FormJSHelper.generateXHRFnCallVariables(form, element.getFormDispatchId(), 4));
			sb.append("    o_ffXHRNFEvent(formNam, dispIdField, dispId, eventIdField, eventInt, false, false, false, 'evAutosave', 'autosave', text);\n");
			sb.append("  } else {");
			sb.append("    periodic.cancel();");
			sb.append("  }");
			sb.append("} catch(e) {");
			sb.append("  periodic.cancel();");
			sb.append("  if(window.console) console.log(e);");
			sb.append("}");
			sb.append("})");
			sb.append("});\n");
			sb.append(FormJSHelper.getJSEnd());
		}
	}

}
