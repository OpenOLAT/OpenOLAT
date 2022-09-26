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
package org.olat.core.gui.components.dropdown;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.Dropdown.ButtonSize;
import org.olat.core.gui.components.dropdown.Dropdown.CaretPosition;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DropdownRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		Dropdown dropdown = (Dropdown)source;
		sb.append("<div class='btn-group'>", dropdown.isEmbbeded());
		
		boolean hasComponents = dropdown.size() > 0;
		Iterable<Component> components = dropdown.getComponents();
		if(dropdown.isButton()) {
			sb.append("<button type='button' class='btn btn-default dropdown-toggle");
			sb.append(" btn-xs", dropdown.getButtonSize() == ButtonSize.extraSmall);
			sb.append(" btn-sm", dropdown.getButtonSize() == ButtonSize.small);
			sb.append(" btn-lg", dropdown.getButtonSize() == ButtonSize.large);
			sb.append(" btn-primary", dropdown.isPrimary());
		} else {
			sb.append("<a href='#' class='dropdown-toggle");
		}
		if(StringHelper.containsNonWhitespace(dropdown.getElementCssClass())) {
			sb.append(" ").append(dropdown.getElementCssClass());
		}
		if(dropdown.isLabeledToggle()) {
			sb.append(" o_with_labeled");
		}
		if (!hasComponents) {
			sb.append(" o_empty");
		}
		String btnDomID = "dd_btn_" + dropdown.getDispatchID();
		sb.append("' id='").append(btnDomID);
		sb.append("' data-toggle='dropdown'>");		

		String dropdownInnerCss = dropdown.getInnerCSS();
		sb.append("<span class='o_inner_wrapper ").append(dropdownInnerCss, (dropdownInnerCss != null)).append("'>");
		// With or without Icon
		if(StringHelper.containsNonWhitespace(dropdown.getIconCSS())) {
			sb.append("<i class='").append(dropdown.getIconCSS()).append("'>&nbsp;</i>");
		}
		String dropdownInnerText = dropdown.getInnerText();
		sb.append("<span class='o_inner_text'>", (dropdownInnerText != null));
		sb.append(dropdownInnerText, (dropdownInnerText != null));
		sb.append("</span>", (dropdownInnerText != null));
		sb.append("</span>");
		
		if (hasComponents) {
			if (dropdown.getCaretPosition().equals(CaretPosition.left)) {
				sb.append(" <i class='");
				if(StringHelper.containsNonWhitespace(dropdown.getCarretIconCSS())) {
					sb.append(dropdown.getCarretIconCSS());
				} else {
					// Caret to indicate the drop-down nature of the button
					sb.append("o_icon o_icon_caret");
				}
				sb.append("'> </i> ");
			}
		}
		
		// Button label, normally rendered below the button, but within the clickable link
		String i18nKey = dropdown.getI18nKey();
		if(StringHelper.containsNonWhitespace(i18nKey)) {
			String label;
			if(dropdown.isTranslated()) {
				label = i18nKey;
			} else {
				label = dropdown.getTranslator().translate(dropdown.getI18nKey());
			}
			sb.append("<span class='o_label'>").append(label).append("</span>");
		}
		
		if (hasComponents) {
			if (dropdown.getCaretPosition().equals(CaretPosition.right)) {
				sb.append(" <i class='");
				if(StringHelper.containsNonWhitespace(dropdown.getCarretIconCSS())) {
					sb.append(dropdown.getCarretIconCSS());
				} else {
					// Caret to indicate the drop-down nature of the button
					sb.append("o_icon o_icon_caret");
				}
				sb.append("'> </i> ");
			}
		}
		
		if(dropdown.isButton()) {
			sb.append("</button>");
		} else {
			sb.append("</a>");
		}
		String itemsDomID = "dd_items_" + dropdown.getDispatchID();
		if (hasComponents) {
			sb.append("<ul class='dropdown-menu");
			if(StringHelper.containsNonWhitespace(dropdown.getElementCssClass())) {
				sb.append(" ").append(dropdown.getElementCssClass());
			}
			if(dropdown.isLabeledMenu()) {
				sb.append(" o_with_labeled");
			}
			if(dropdown.getOrientation() == DropdownOrientation.right) {
				sb.append(" dropdown-menu-right");
			}		
			sb.append("' id='").append(itemsDomID).append("'");
			sb.append(" role='menu'>");

			boolean wantSpacer = false;
			for(Component component:components) {
				if(component instanceof Spacer) {
					wantSpacer = true;
				} else if(component.isVisible()) {
					if(wantSpacer) {
						sb.append("<li class='divider'></li>");
						wantSpacer = false;
					}
					
					if(component.isEnabled()) {
						sb.append("<li>");
					} else {
						sb.append("<li class='disabled'>");
					}
					renderer.render(component, sb, args);
					sb.append("</li>");
				} else {
					component.setDirty(false);
				}
			}
			sb.append("</ul>");
		}
		sb.append("</div>", dropdown.isEmbbeded());
		
		if (hasComponents) {
			// Check if dropdown has enough space in center main container, enlarge if necessary
			if (dropdown.isExpandContentHeight()) {
				sb.append("<script>setTimeout(function(){");
				sb.append("OPOL.adjustContentHeightForAbsoluteElement('#").append(itemsDomID).append("');");
				sb.append("});</script>");
			}
		}
		
		
	}
}
