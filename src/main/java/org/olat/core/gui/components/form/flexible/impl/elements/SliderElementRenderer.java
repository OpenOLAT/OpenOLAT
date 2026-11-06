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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderElementRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		SliderElementComponent sec = (SliderElementComponent)source;
		SliderElement sel = sec.getFormItem();
		String formDispatchFieldId = sel.getRootForm().getDispatchFieldId();
		double value = sec.hasValue()
				? sec.getValue()
				: sec.getMinValue() + (sec.getMaxValue() - sec.getMinValue()) / 2;    // middle of slider
		
		String inputId = sec.getFormDispatchId().concat("_sinput");
		String sliderId = sec.getFormDispatchId().concat("_slider");

		sb.append("<div class='o_slider_wrapper'>");
		sb.append("<div id='").append(sliderId).append("' class='");
		if (sec.hasValue()) {
			sb.append("o_has_value");
		} else {
			sb.append("o_no_value");
		}
		sb.append("'><span id='").append(sliderId).append("_handle' class='ui-slider-handle' role='slider'")
		  .append(" tabindex='0'", sec.isEnabled());
		if (sel.hasLabel()) {
			sb.append(" aria-labelledby='o_cl").append(sec.getDispatchID()).append("'");
		} else if (StringHelper.containsNonWhitespace(sel.getAriaLabel())) {
			sb.append(" aria-label=\"").append(StringHelper.escapeHtml(sel.getAriaLabel())).append("\"");
		}
		sb.append(" aria-valuemin='").append(sec.getMinValue()).append("' aria-valuemax='").append(sec.getMaxValue()).append("'");
		if (sec.hasValue()) {
			sb.append(" aria-valuenow='").append(sec.getValue()).append("'");
			String valueText = getValueText(sec, sec.getValue());
			if (StringHelper.containsNonWhitespace(valueText)) {
				sb.append(" aria-valuetext=\"").append(StringHelper.escapeHtml(valueText)).append("\"");
			}
		}
		sb.append(" aria-orientation='vertical'", sec.isVertical())
		  .append(" aria-disabled='true'", !sec.isEnabled())
		  .append("></span></div>");
		sb.append("<input id='").append(inputId).append("' type='hidden' name='").append(sec.getFormDispatchId()).append("' value=''/>");
		
		List<String> valueTexts = sel.getValueTexts();
		boolean withValueTexts = valueTexts != null && !valueTexts.isEmpty();
		sb.append("<script>/* <![CDATA[ */\n")
		  .append("jQuery(function() {\n");
		if (withValueTexts) {
			sb.append(" var valueTexts = [");
			for (int i = 0; i < valueTexts.size(); i++) {
				sb.append(",", i > 0).append("\"").append(StringHelper.escapeJavaScript(valueTexts.get(i))).append("\"");
			}
			sb.append("];\n");
		}
		sb.append(" jQuery('#").append(sec.getFormDispatchId()).append("_slider').slider({\n")
		  .append("  value: ").append(value).append(",\n");
		if(sec.getStep() > 0) {
			sb.append("  step: ").append(sec.getStep()).append(",\n");
		}
		sb.append("  min: ").append(sec.getMinValue()).append(",\n")
		  .append("  max: ").append(sec.getMaxValue()).append(",\n");
		if (sec.isVertical()) {
			sb.append("  orientation: \"vertical\",\n");
		}
		sb.append("  slide: function(event, ui) {\n")
		  .append("    jQuery('#").append(inputId).append("').val(ui.value);\n")
		  .append("    jQuery('#").append(sliderId).append("_handle').attr('aria-valuenow', ui.value);\n");
		if (withValueTexts) {
			sb.append("    var valueText = valueTexts[Math.round((ui.value - ").append(sec.getMinValue()).append(") / ").append(getStep(sec)).append(")];\n")
			  .append("    if (valueText) {\n")
			  .append("      jQuery('#").append(sliderId).append("_handle').attr('aria-valuetext', valueText);\n")
			  .append("    } else {\n")
			  .append("      jQuery('#").append(sliderId).append("_handle').removeAttr('aria-valuetext');\n")
			  .append("    }\n");
		}
		sb.append("    setFlexiFormDirty('").append(formDispatchFieldId).append("');\n")
		  .append("  },\n")
		  .append("  stop: function(event, ui) {\n")
		  .append("    jQuery('#").append(inputId).append("').val(ui.value);\n");
		if(sel.getAction() >= 0) {
			sb.append("    ").append(FormJSHelper.getJSFnCallFor(sel.getRootForm(), sec.getFormDispatchId(), 2)).append(";\n");
		}
		sb.append("    setFlexiFormDirty('").append(formDispatchFieldId).append("');\n")
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
		sb.append(";\n");
		if (sel.hasFocus()) {
			sb.append("setTimeout(function() { jQuery('#").append(sliderId).append("_handle').focus(); }, 0);\n");
		}

		sb.append("});\n")
		  .append("/* ]]> */</script>\n");
		sb.append("</div>");
	}
	
	private String getValueText(SliderElementComponent sec, double value) {
		List<String> valueTexts = sec.getFormItem().getValueTexts();
		if (valueTexts == null) {
			return null;
		}
		int index = (int)Math.round((value - sec.getMinValue()) / getStep(sec));
		return index >= 0 && index < valueTexts.size() ? valueTexts.get(index) : null;
	}
	
	private int getStep(SliderElementComponent sec) {
		return sec.getStep() > 0 ? sec.getStep() : 1;
	}
}
