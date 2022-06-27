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
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderElementRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		SliderElementComponent sec = (SliderElementComponent)source;
		SliderElement sel = sec.getSliderElement();
		String formDispatchFieldId = sel.getRootForm().getDispatchFieldId();
		double value = sec.hasValue()
				? sec.getValue()
				: sec.getMinValue() + (sec.getMaxValue() - sec.getMinValue()) / 2;    // middle of slider
		
		String inputId = sec.getFormDispatchId().concat("_sinput");
		
		sb.append("<div class='o_slider_wrapper'>");
		sb.append("<div id='").append(sec.getFormDispatchId()).append("_slider' class='");
		if (sec.hasValue()) {
			sb.append("o_has_value");
		} else {
			sb.append("o_no_value");
		}
		sb.append("'> </div>");
		sb.append("<input id='").append(inputId).append("' type='hidden' name='").append(sec.getFormDispatchId()).append("' value=''/>");
		
		sb.append("<script>/* <![CDATA[ */\n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('#").append(sec.getFormDispatchId()).append("_slider').slider({\n")
		  .append("  value: ").append(value).append(",\n");
		if(sec.getStep() > 0) {
			sb.append("  step: ").append(sec.getStep()).append(",\n");
		}
		sb.append("  min: ").append(sec.getMinValue()).append(",\n")
		  .append("  max: ").append(sec.getMaxValue()).append(",\n")
		  .append("  slide: function(event, ui) {\n")
		  .append("    jQuery('#").append(inputId).append("').val(ui.value);\n")
		  .append("    setFlexiFormDirty('").append(formDispatchFieldId).append("');\n")
		  .append("    o_info.lastFormFocusEl='").append(formDispatchFieldId).append("';")
		  .append("  },\n")
		  .append("  stop: function(event, ui) {\n")
		  .append("    jQuery('#").append(inputId).append("').val(ui.value);\n");
		if(sel.getAction() >= 0) {
			sb.append("    ").append(FormJSHelper.getJSFnCallFor(sel.getRootForm(), sec.getFormDispatchId(), 2)).append(";\n");
		}
		sb.append("    setFlexiFormDirty('").append(formDispatchFieldId).append("');\n")
		  .append("    o_info.lastFormFocusEl='").append(formDispatchFieldId).append("';")
		  .append("  }\n")
		  .append(" })");
		if(!sec.isEnabled()) {
			sb.append(".slider('disable')");
		}
		if(sec.getStep() > 0) {
			sb.append(".slider('pips', {\n")
			  .append(" first:'pip',\n")
			  .append(" last:'pip',\n")
			  .append("})");
		}
		sb.append("\n");
		
		sb.append("});\n")
		  .append("/* ]]> */</script>\n");
		sb.append("</div>");
	}
}
