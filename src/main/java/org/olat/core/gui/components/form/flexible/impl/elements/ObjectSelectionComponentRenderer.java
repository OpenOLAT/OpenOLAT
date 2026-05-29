/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 *
 * Initial date: 2026-05-27<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ObjectSelectionComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ObjectSelectionComponent cmp = (ObjectSelectionComponent) source;
		Component expandButton = cmp.getComponent(ObjectSelectionElementImpl.EXPAND_COMP_NAME);

		boolean showBrowse = cmp.isBrowserButtonVisible() && cmp.isEnabled();

		if (!showBrowse) {
			renderer.render(expandButton, sb, null);
			return;
		}

		sb.append("<div class=\"o_object_selection_display\">");

		renderer.render(expandButton, sb, null);

		String dispatchId = cmp.getFormDispatchId();
		String browseButtonId = dispatchId + "_browse";

		sb.append("<button type=\"button\"");
		sb.append(" id=\"").append(browseButtonId).append("\"");
		sb.append(" class=\"btn btn-default o_selection_browse_button\"");
		if (StringHelper.containsNonWhitespace(cmp.getBrowserButtonTitle())) {
			sb.append(" title=\"").appendHtmlEscaped(cmp.getBrowserButtonTitle()).append("\"");
		}
		if (StringHelper.containsNonWhitespace(cmp.getBrowserButtonAriaLabel())) {
			sb.append(" aria-label=\"").appendHtmlEscaped(cmp.getBrowserButtonAriaLabel()).append("\"");
		}
		sb.append(" onfocus=\"o_info.lastFormFocusEl='").append(browseButtonId).append("';\"");
		sb.append(" onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\"");
		sb.append(" onclick=\"");
		sb.append(FormJSHelper.getXHRFnCallFor(cmp.getFormItem().getRootForm(), dispatchId, 1, false, false, true,
				new NameValuePair("os_cmd", ObjectSelectionComponent.CMD_BROWSE)));
		sb.append("; return false;\">");
		if (StringHelper.containsNonWhitespace(cmp.getBrowserButtonIconCss())) {
			sb.append("<i class=\"").append(cmp.getBrowserButtonIconCss()).append("\"></i> ");
		}
		if (StringHelper.containsNonWhitespace(cmp.getBrowserButtonTitle())) {
			sb.append("<span>").appendHtmlEscaped(cmp.getBrowserButtonTitle()).append("</span>");
		}
		sb.append("</button>");

		sb.append("</div>");
	}
}
