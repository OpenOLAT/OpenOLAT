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
package org.olat.modules.openbadges.ui.element;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2024-09-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeSelectorRenderer extends DefaultComponentRenderer {
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
								Translator translator, RenderResult renderResult, String[] args) {

		renderButton(renderer, sb, source);
		renderExpander(renderer, sb, source);

		//renderDropdown(renderer, sb, source);
	}

	private void renderButton(Renderer renderer, StringOutput sb, Component source) {
		BadgeSelectorComponent badgeSelectorComponent = (BadgeSelectorComponent) source;
		BadgeSelectorElementImpl badgeSelectorElement = badgeSelectorComponent.getFormItem();
		FormLink button = badgeSelectorElement.getButton();

		if (badgeSelectorElement.isEnabled()) {
			renderer.render(button.getComponent(), sb, null);
		} else {
			sb.append("span class=\"o_badge_selector_button\">");
			sb.append(button.getI18nKey());
			sb.append("</span>");
		}
	}

	private void renderExpander(Renderer renderer, StringOutput sb, Component source) {
		sb.append("<script>\n");
		sb.append("\"use strict\";\n");
		sb.append("</script>\n");
	}

	private void renderDropdown(Renderer renderer, StringOutput sb, Component source) {
		BadgeSelectorComponent badgeSelectorComponent = (BadgeSelectorComponent) source;
		BadgeSelectorElementImpl badgeSelectorEl = badgeSelectorComponent.getFormItem();

		String id = badgeSelectorEl.getFormDispatchId();
		String dropdownId = id + "_dropdown";
		String buttonId = id + "_button";

		sb.append("<div class='o_badge_selector_wrapper'>");
		sb.append("<div id='").append(dropdownId).append("' class='button-group dropdown'>");

		sb.append("<button ");
		sb.append("class='btn btn-default dropdown-toggle o_badge_selector_button o_can_have_focus o_button_printed' ");
		sb.append("type='button' ");
		sb.append("id='").append(buttonId).append("' ");
		sb.append("data-toggle='dropdown' ");
		sb.append("onfocus=\"o_info.lastFormFocusEl='").append(buttonId).append("';\" ");
		sb.append("disabled", !badgeSelectorEl.isEnabled());
		sb.append(">");

		sb.append("<span>").append("placeholder'").append("</span>");

		sb.append("<i class='o_icon o_icon-fw o_icon_caret o_badge_selector_icon'></i>");

		sb.append("</button>");

		sb.append("<div class='dropdown-menu o_badge_selector_dropdown' ");
		sb.append(" aria-labelledby='").append(buttonId).append("'>");

		//renderer.render(badgeSelectorEl.getSearchFieldEl().getComponent(), sb, null);

		if (badgeSelectorEl.getSelectorController() != null) {
			renderer.render(badgeSelectorEl.getSelectorController().getInitialComponent(), sb, null);
		}
		sb.append("</div>"); // dropdown-menu

		sb.append("</div>"); // dropdown
		sb.append("</div>"); // o_badge_selector_wrapper
	}
}
