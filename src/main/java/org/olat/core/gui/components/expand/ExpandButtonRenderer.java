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
package org.olat.core.gui.components.expand;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Sep 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExpandButtonRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ExpandButton cmp = (ExpandButton) source;
		
		if (cmp.isDisabledAsText() && !cmp.isEnabled()) {
			renderDisabledText(sb, cmp);
			return;
		}
		
		String elementId = cmp.getFormDispatchId();
		
		sb.append("<button type=\"button\"");
		sb.append("id=\"").append(elementId).append("\" ");
		sb.append("class=\"");
		sb.append("o_expand_button o_can_have_focus ");
		if (StringHelper.containsNonWhitespace(cmp.getCssClass())) {
			sb.append(cmp.getCssClass());
		}
		sb.append("\" "); // class
		if (StringHelper.containsNonWhitespace(cmp.getTitle())) {
			sb.append("title=\"").appendHtmlEscaped(cmp.getTitle()).append("\" ");
		}
		if (StringHelper.containsNonWhitespace(cmp.getAriaLabel())) {
			sb.append("aria-label=\"").appendHtmlEscaped(cmp.getAriaLabel()).append("\" ");
		}
		if (StringHelper.containsNonWhitespace(cmp.getAriaHasPopup())) {
			sb.append("aria-haspopup=\"").appendHtmlEscaped(cmp.getAriaHasPopup()).append("\" ");
		}
		if (StringHelper.containsNonWhitespace(cmp.getAriaControls())) {
			sb.append("aria-controls=\"").appendHtmlEscaped(cmp.getAriaControls()).append("\" ");
		}
		sb.append("aria-expanded=\"").append(cmp.isExpanded()).append("\" ");
		sb.append("onfocus=\"o_info.lastFormFocusEl='").append(elementId).append("';\" ");
		if (cmp.isEnabled()) {
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
			if(cmp.getFormItem() != null) {
				sb.append(FormJSHelper.getXHRFnCallFor(cmp.getFormItem().getRootForm(), elementId, 1, false, false, true));
				sb.append("; return false;\" ");
			} else {
				ubu.buildXHREvent(sb, "", false, true, new NameValuePair(VelocityContainer.COMMAND_ID, ExpandButton.CMD_TOGGLE));
				sb.append("\" ");
			}
		} else {
			sb.append("disabled ");
		}
		sb.append(">"); // button
		
		if (cmp.isExpanded() && StringHelper.containsNonWhitespace(cmp.getIconLeftExpandedCss())) {
			sb.append("<i class=\"").append(cmp.getIconLeftExpandedCss()).append("\"></i> ");
		} else if (!cmp.isExpanded() && StringHelper.containsNonWhitespace(cmp.getIconLeftCollapsedCss())) {
			sb.append("<i class=\"").append(cmp.getIconLeftCollapsedCss()).append("\"></i> ");
		}
		sb.append("<span class=\"o_expand_button_text o_nowrap\">");
		sb.append(cmp.getText(), cmp.getEscapeMode());
		sb.append("</span>");
		if (cmp.isExpanded() && StringHelper.containsNonWhitespace(cmp.getIconRightExpandedCss())) {
			sb.append(" <i class=\"").append(cmp.getIconRightExpandedCss()).append("\"></i>");
		} else if (!cmp.isExpanded() && StringHelper.containsNonWhitespace(cmp.getIconRightCollapsedCss())) {
			sb.append(" <i class=\"").append(cmp.getIconRightCollapsedCss()).append("\"></i>");
		}
		
		sb.append("</button>");
	}

	private void renderDisabledText(StringOutput sb, ExpandButton cmp) {
		sb.append("<div ");
		sb.append("id=\"").append(cmp.getFormDispatchId()).append("\" ");
		sb.append("class=\"");
		sb.append("o_expand_button o_expand_button_disabled_text form-control-static");
		sb.append("\"");
		sb.append(">");
		sb.append(cmp.getText(), cmp.getEscapeMode());
		sb.append("</div>");
	}

}
