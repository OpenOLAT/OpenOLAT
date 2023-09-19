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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 31 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SelectionDisplayRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		SelectionDisplayComponent dispalyCmp = (SelectionDisplayComponent) source;
		FormItem displayItem = dispalyCmp.getFormItem();
		
		
		sb.append("<button");
		sb.append(" id=\"").append(displayItem.getFormDispatchId()).append("\"");
		sb.append(" class=\"btn btn-default o_selection_display o_can_have_focus o_button_printed ");
		sb.append("\""); // class
		sb.append(" aria-expanded=\"").append(dispalyCmp.isAriaExpanded()).append("\"");
		sb.append(" disabled", !displayItem.isEnabled());
		sb.append(" onfocus=\"o_info.lastFormFocusEl='").append(displayItem.getFormDispatchId()).append("';\" ");
		if (displayItem.isEnabled()) {
			sb.append(" onclick=\"");
			sb.append(FormJSHelper.getXHRFnCallFor(displayItem, false, false, true,
					new NameValuePair("cid", SelectionDisplayElement.CMD_CLICK)));
			sb.append("\"");
		}
		sb.append(">"); // button
		sb.append(dispalyCmp.getValue());
		sb.append("<i class=\"o_icon o_icon_caret\"></i>");
		sb.append("</button>");
	}

}
