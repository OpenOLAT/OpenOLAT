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
package org.olat.core.gui.components.widget;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class WidgetRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		Widget widget = (Widget)source;
		
		sb.append("<div class=\"o_widget ");
		if (StringHelper.containsNonWhitespace(widget.getElementCssClass())) {
			sb.append(widget.getElementCssClass());
		}
		sb.append("\">");
		
		sb.append("<div class=\"o_widget_header\">");
		sb.append("<div class=\"o_widget_title o_flex_item_max\">");
		sb.append(widget.getTitle());
		if (StringHelper.containsNonWhitespace(widget.getSubTitle())) {
			sb.append(" <span class=\"o_widget_subtitle\">");
			sb.append(widget.getSubTitle());
			sb.append("</span>");
		}
		sb.append("</div>");
		sb.append("<div class=\"o_widget_icon\">");
		sb.append("<i class=\"o_icon ");
		sb.append(widget.getIconCss());
		sb.append("\"> </i>");
		sb.append("</div>");
		sb.append("</div>");
		
		sb.append("<div class=\"o_widget_content\">");
		renderContent(renderer, sb, source, ubu, translator, renderResult, args);
		sb.append("</div>");
		
		sb.append("</div>");
	}
	
	public abstract void renderContent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args);

	protected void renderAdditional(Renderer renderer, StringOutput sb, String[] args, Widget widget) {
		sb.append("<div class=\"o_widget_additional\">");
		
		if (widget instanceof AdditionalWidget additionalWidget) {
			if (StringHelper.containsNonWhitespace(additionalWidget.getAdditionalText())) {
				sb.append("<div class=\"");
				sb.append("o_widget_additional_text o_flex_item_max ");
				if (StringHelper.containsNonWhitespace(additionalWidget.getAdditionalCssClass())) {
					sb.append(additionalWidget.getAdditionalCssClass());
				}
				sb.append("\">");
				sb.append(additionalWidget.getAdditionalText());
				sb.append("</div>");
			} else if (additionalWidget.getAdditionalComp() != null) {
				sb.append("<div class=\"");
				if (StringHelper.containsNonWhitespace(additionalWidget.getAdditionalCssClass())) {
					sb.append(additionalWidget.getAdditionalCssClass());
				}
				sb.append("\">");
				renderer.render(additionalWidget.getAdditionalComp(), sb, args);
				additionalWidget.getAdditionalComp().setDirty(false);
				sb.append("</div>");
			}
		}
		
		sb.append("</div>");
	}

}
