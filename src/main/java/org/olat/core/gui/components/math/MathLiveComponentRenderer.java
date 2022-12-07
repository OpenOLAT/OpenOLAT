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
package org.olat.core.gui.components.math;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		MathLiveComponent mlCmp = (MathLiveComponent)source;
		if(mlCmp.isEnabled() && mlCmp.getFormItem() != null) {
			renderEditor(sb, mlCmp.getFormItem());
		} else {
			render(sb, mlCmp);
		}
	}
	
	private void renderEditor(StringOutput sb, MathLiveElement element) {
		Form form = element.getRootForm();
		String dispatchId = element.getComponent().getDispatchID();
		String formDispatchId = element.getFormDispatchId();
		MathLiveVirtualKeyboardMode mode = element.getVirtualKeyboardMode();
		if(mode == null) {
			mode = MathLiveVirtualKeyboardMode.onfocus;
		}
		sb.append("<div id='o_c").append(dispatchId).append("' class='o_mathlive_editor'>")
		  .append("<textarea id='o_mf_input_").append(dispatchId).append("' ").append(" name='").append(formDispatchId).append("' hidden='hidden'>");
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			sb.append(element.getValue());
		}
		sb.append("</textarea>")
		  .append("<math-field id='o_mlive_").append(dispatchId).append("' virtual-keyboard-mode='").append(mode.name()).append("'>");
		if(StringHelper.containsNonWhitespace(element.getValue())) {
			sb.append(element.getValue());
		}
		sb.append("</math-field>\n")
		  .append("<script>\n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('#o_mlive_").append(dispatchId).append("').on('input', function(ev) {\n")
		  .append("  jQuery('#o_mf_input_").append(dispatchId).append("').val(ev.target.value);")
		  .append(" });\n");
		if(element.isSendOnBlur()) {
			sb.append(" jQuery('#o_mlive_").append(dispatchId).append("').on('blur', function(e) {\n")
			  .append("  o_ffXHREvent('").append(form.getFormName()).append("','").append(form.getDispatchFieldId()).append("','").append(formDispatchId).append("','").append(form.getEventFieldId()).append("', 2, false, false, true, false,'cmd','saveinlinedmathlive');\n")
			  .append(" });\n");
		}
		sb.append("});\n")
		  .append("</script></div>");
	}
	
	private void render(StringOutput sb, MathLiveComponent cmp) {
		String dispatchId = cmp.getDispatchID();
		DomWrapperElement element = cmp.getDomWrapperElement();
		sb.append("<").append(element.name()).append(" id='o_c").append(dispatchId).append("' class='math");
		if(StringHelper.containsNonWhitespace(cmp.getElementCssClass())) {
			sb.append(" ").append(cmp.getElementCssClass());
		}
		sb.append("'>");
		
		if(StringHelper.containsNonWhitespace(cmp.getValue())) {
			sb.append(cmp.getValue());
		}
		sb.append("</").append(element.name()).append(">");
		sb.append(Formatter.elementLatexFormattingScript(dispatchId));
	}
}
