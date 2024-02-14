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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 14 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class StaticListRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		StaticListComponent comp = (StaticListComponent)source;
		String id = comp.getFormDispatchId();
		List<String> values = comp.getValues();
		
		boolean showAllVisible = comp.isShowAllVisible();
		if (showAllVisible) {
			if (values.size() <= comp.getInitialNumValues()) {
				showAllVisible = false;
			}
		}
		
		int showNumValues = values.size();
		int showNumAdditionalValues = 0;
		if (showAllVisible && values.size() > comp.getInitialNumValues()) {
			showNumValues = comp.getInitialNumValues();
			showNumAdditionalValues = values.size() - comp.getInitialNumValues();
		}
		
		// Open the list
		sb.append("<ul id=\"").append(id).append("\"");
		sb.append(" class='o_static_list");
		sb.append(" form-control-static ", comp.getFormItem() != null);
		sb.append(comp.getElementCssClass(), StringHelper.containsNonWhitespace(comp.getElementCssClass()));
		sb.append("'>");
		
		// Show values
		for (int i = 0; i < showNumValues; i++) {
			sb.append("<li>");
			sb.append(values.get(i));
			sb.append("</li>");
		}
		
		// Link to show all values
		if (showAllVisible) {
			sb.append("<li>");
			sb.append("<a role=\"button\" ");
			ubu.buildHrefAndOnclick(sb, null, true, false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, StaticListComponent.CMD_SHOW_ALL));
			sb.append(">");
			String showAllText;
			if (StringHelper.containsNonWhitespace(comp.getShowAllI18nKey())) {
				showAllText = translator.translate(comp.getShowAllI18nKey(), new String[] {String.valueOf(showNumAdditionalValues)});
			} else {
				showAllText = Util.createPackageTranslator(StaticListRenderer.class, translator.getLocale())
						.translate("static.list.further", new String[] {String.valueOf(showNumAdditionalValues)});
			}
			sb.append(showAllText);
			sb.append("</a>");
			sb.append("</li>");
		}
		
		// Close the list
		sb.append("</ul>");
	}

}
