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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.SingleSelectionComponent.RadioElementComponent;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SingleSelectionRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		SingleSelectionComponent teC = (SingleSelectionComponent) source;
		Layout layout = teC.getSingleSelectionImpl().getLayout();
		if(layout == Layout.vertical) {
			renderVertical(sb, teC);
		} else {
			renderHorizontal(sb, teC);
		}
	}
	
	private void renderVertical(StringOutput sb, SingleSelectionComponent source) {
		RadioElementComponent[] radios = source.getRadioComponents();
		String css = source.getElementCssClass();
		boolean hasCss = css != null || source.getSingleSelectionImpl().isRenderAsCard();

		if (hasCss) {
			sb.append("<div class=\"")
				.append(css, css != null)
				.append(" o_radio_cards", source.getSingleSelectionImpl().isRenderAsCard())
				.append("\">");
			
		}
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, false);
		}
		sb.append("</div>", hasCss);
	}
	
	private void renderHorizontal(StringOutput sb, SingleSelectionComponent source) {
		String css = source.getElementCssClass();
		sb.append("<div class=\"form-inline ")
			.append("o_radio_cards ", source.getSingleSelectionImpl().isRenderAsCard())
			.append(css, css != null)
			.append("\">");
		RadioElementComponent[] radios = source.getRadioComponents();
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, true);
		}
		sb.append("</div>");
	}
	
	private void renderRadio(StringOutput sb, SingleSelectionComponent source, RadioElementComponent ssec, boolean inline) {
		String subStrName = "name='" + ssec.getGroupingName() + "'";

		String key = ssec.getKey();
		String value = ssec.getValue();
		boolean selected = ssec.isSelected();
		String formDispatchId = ssec.getFormDispatchId();
		
		// read write view
		sb.append("<div class='radio' ", !inline); // normal radios need a wrapper (bootstrap) ...
		if(!inline && source.getWidthInPercent() > 0) {
			sb.append("style='width:").append(source.getWidthInPercent()).append("%;'");
		}
		sb.append(">", !inline)
		  .append("<label ").append("class='radio-inline' ", inline); // ... and inline a class on the label (bootstrap)
		if(inline && source.getWidthInPercent() > 0) {
			sb.append("style='width:").append(source.getWidthInPercent()).append("%;'");
		}
		sb.append(" for=\"").append(formDispatchId).append("\">")
		  .append("<input id='").append(formDispatchId).append("' ")
		  .append("type='radio' ").append(subStrName)
		  .append(" value='").append(key).append("' ")
		  .append(" checked='checked' ", selected);

		if(source.isEnabled()){
			// use the selection elements formDispId instead of the one of this element.
			sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction(), false, null, formDispatchId));
		} else {
			//mark as disabled and do not add javascript
			sb.append(" disabled='disabled' ");
		}
		sb.append(">");
		
		if (source.getSingleSelectionImpl().isRenderAsCard()) {
			// Card style rendering
			sb.append("<span class='o_radio_card'><span class='o_radio_text_wrapper'>");
			if (StringHelper.containsNonWhitespace(value)) {
				sb.append(" <span class='o_radio_label'>");
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(value));
				} else {
					sb.append(value);
				}
				sb.append(" </span>");
			}
			String desc = ssec.getDescription();
			if (StringHelper.containsNonWhitespace(desc)) {
				sb.append(" <span class='o_radio_desc'>");
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(desc));
				} else {
					sb.append(desc);
				}
				sb.append(" </span>");
			}
			sb.append("</span>"); // END o_radio_text_wrapper
			String iconCssClass = ssec.getIconCssClass();
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				sb.append(" <span class='o_radio_icon ").append(iconCssClass).append("'> </span>");
			}
			sb.append("</span>"); // END o_radio_card
			
		} else {
			// Standard radio button rendering
			if (StringHelper.containsNonWhitespace(value)) {
				if(source.isEscapeHtml()) {
					sb.append(StringHelper.escapeHtml(value));
				} else {
					sb.append(value);
				}
			} else if(inline) {
				// at least something in label required for properly aligned rendering, nbsp is important for bootstrap
				sb.append("&nbsp;"); 
			}
		}
		
		if(source.isEnabled()){
			// add set dirty form only if enabled
			// must be placed within label to make multiple radio-inline rules of bootstrap match 
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, ssec.getRootForm(), formDispatchId);
		}
		
		sb.append("</label>")
		  .append("</div>", !inline) // normal radios need a wrapper (bootstrap)
		  .append(" ", source.isTrailingSpace());
	}
}