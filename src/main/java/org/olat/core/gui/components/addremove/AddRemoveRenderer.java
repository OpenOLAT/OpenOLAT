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
package org.olat.core.gui.components.addremove;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.AddRemoveElement;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 4 Oct 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class AddRemoveRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		
		AddRemoveComponent cmp = (AddRemoveComponent)source;
		renderAddRemove(renderer, sb, cmp, translator);
	}

	private void renderAddRemove(Renderer renderer, StringOutput sb, AddRemoveComponent addRemoveComponent, Translator translator) {
		AddRemoveElement addRemoveElement = addRemoveComponent.getAddRemoveElement();
		String addText = ""; 
		String removeText = ""; 
		
		if (addRemoveElement.isTextShown()) {
			if(StringHelper.containsNonWhitespace(addRemoveElement.getAddText())) {
				addText = addRemoveElement.getAddText();
			} else {
				addText = translator.translate("addremove.add.text");
			}
			
			if(StringHelper.containsNonWhitespace(addRemoveElement.getRemoveText())) {
				removeText = addRemoveElement.getRemoveText();
			} else {
				removeText = translator.translate("addremove.remove.text");
			}
		}
		
		if(!StringHelper.containsNonWhitespace(addRemoveElement.getAddTitle())) {
			addRemoveElement.getAddLink().setTitle(addText);
			addRemoveElement.getAddLink().setAriaLabel(addText);
		}
		
		if(!StringHelper.containsNonWhitespace(addRemoveElement.getRemoveTitle())) {
			addRemoveElement.getRemoveLink().setTitle(removeText);
			addRemoveElement.getRemoveLink().setAriaLabel(removeText);
		}
		
		addRemoveElement.getAddLink().setI18nKey(addText);
		addRemoveElement.getAddLink().setIconLeftCSS(addRemoveElement.getAddIcon());
		
		addRemoveElement.getRemoveLink().setI18nKey(removeText);
		addRemoveElement.getRemoveLink().setIconLeftCSS(addRemoveElement.getRemoveIcon());
		
		sb.append("<div class='btn-group o_addremove");
		if(addRemoveComponent.getElementCssClass() != null) {
			sb.append(" ").append(addRemoveComponent.getElementCssClass());
		}
		sb.append("'>");
		renderer.render(addRemoveElement.getAddLink().getComponent(), sb, null);
		renderer.render(addRemoveElement.getRemoveLink().getComponent(), sb, null);
		sb.append("</div>");
	}
}



