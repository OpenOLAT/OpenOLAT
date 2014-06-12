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

/**
 * 
 * Initial date: 12.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SingleSelectionRenderer extends DefaultComponentRenderer {

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
		SingleSelectionComponent teC = (SingleSelectionComponent) source;
		Layout layout = teC.getSingleSelectionImpl().getLayout();
		if(layout == Layout.vertical) {
			renderVertical(sb, teC);
		} else {
			renderHorizontal(sb, teC);
		}
	}
	
	private void renderVertical(StringOutput sb, SingleSelectionComponent source) {
		sb.append("<div class='o_form_selection_vertical'>");
		RadioElementComponent[] radios = source.getRadioComponents();
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, "radio");
		}
		sb.append("</div>");
	}
	
	private void renderHorizontal(StringOutput sb, SingleSelectionComponent source) {
		sb.append("<div class='form-inline'>");
		RadioElementComponent[] radios = source.getRadioComponents();
		for(RadioElementComponent radio:radios) {
			renderRadio(sb, source, radio, "radio-inline");
		}
		sb.append("</div>");
	}
	
	private void renderRadio(StringOutput sb, SingleSelectionComponent source, RadioElementComponent ssec, String css) {
		String subStrName = "name='" + ssec.getGroupingName() + "'";

		String key = ssec.getKey();
		String value = ssec.getValue();
		boolean selected = ssec.isSelected();
		
		// read write view
		sb.append("<label class='").append(css).append("' for='").append(ssec.getFormDispatchId()).append("'>")
		  .append("<input id='").append(ssec.getFormDispatchId()).append("' ")
		  .append("type='radio' ").append(subStrName)
		  .append(" value='").append(key).append("' ")
		  .append(" checked='checked' ", selected);

		if(source.isEnabled()){
			// use the selection elements formDispId instead of the one of this element.
			sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
		} else {
			//mark as disabled and do not add javascript
			sb.append(" disabled='disabled' ");
		}
		sb.append(" />").append(value).append("</label> ");
		
		if(source.isEnabled()){
			//add set dirty form only if enabled
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(ssec.getFormDispatchId()));
			sb.append(FormJSHelper.getSetFlexiFormDirtyForCheckbox(ssec.getRootForm(), ssec.getFormDispatchId()));
			sb.append(FormJSHelper.getJSEnd());
		}
	}
}