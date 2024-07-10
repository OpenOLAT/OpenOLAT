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
package org.olat.course.nodes.portfolio;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.widget.WidgetRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PortfolioWidgetRenderer extends WidgetRenderer {

	@Override
	public void renderContent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		PortfolioWidget widget = (PortfolioWidget)source;
		
		sb.append("<div class=\"o_widget_main o_widget_main_binder\">");
		
		sb.append("<div class=\"o_widget_binder_title\">");
		sb.append(StringHelper.escapeHtml(widget.getBinderTitle()));
		sb.append("</div>");
		
		sb.append("<div class=\"o_widget_binder_dates\">");
		sb.append("<div class=\"o_widget_binder_date\">");
		sb.append("<div class=\"o_widget_binder_date_label\">");
		sb.append(widget.getTranslator().translate("map.copyDate"));
		sb.append("</div>");
		sb.append("<div class=\"o_widget_binder_date_value\">");
		if (widget.getCopyDate() != null) {
			sb.append(Formatter.getInstance(widget.getTranslator().getLocale()).formatDateAndTime(widget.getCopyDate()));
		}
		sb.append("</div>");
		sb.append("</div>");
		
		/* TODO: OO-7482: temporarily removed, because not being used properly
		sb.append("<div class=\"o_widget_binder_date\">");
		if (widget.getReturnDate() != null) {
			sb.append("<div class=\"o_widget_binder_date_label\">");
			sb.append(widget.getTranslator().translate("map.returnDate"));
			sb.append("</div>");
			sb.append("<div class=\"o_widget_binder_date_value\">");
			sb.append(Formatter.getInstance(widget.getTranslator().getLocale()).formatDateAndTime(widget.getReturnDate()));
			sb.append("</div>");
		} else {
			sb.append("<div class=\"o_widget_binder_date_label\">");
			sb.append("- ").append(widget.getTranslator().translate("portfolio.not.submitted")).append(" -");
			sb.append("</div>");
		} */
		sb.append("</div>");
		
		sb.append("</div>");
		
		sb.append("</div>");
	}

}
