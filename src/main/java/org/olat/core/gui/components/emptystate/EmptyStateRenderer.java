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
package org.olat.core.gui.components.emptystate;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EmptyStateRenderer extends DefaultComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		EmptyState emptyState = (EmptyState)source;
		
		sb.append("<div  class='o_empty_state'>");
		String indicatorIconCss = StringHelper.containsNonWhitespace(emptyState.getIndicatorIconCss())
				? emptyState.getIndicatorIconCss()
				: "o_icon_empty_indicator";
		sb.append("<div class='o_empty_visual'><i class='o_icon ").append(indicatorIconCss).append("'></i>");
		String iconCss = StringHelper.containsNonWhitespace(emptyState.getIconCss())
				? emptyState.getIconCss()
				: "o_icon_empty_objects";
		sb.append("<i class='o_icon ").append(iconCss).append("'> </i>");
		sb.append("</div>");
	
		String message = null;
		if (StringHelper.containsNonWhitespace(emptyState.getMessageTranslated())) {
			message = emptyState.getMessageTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getMessageI18nKey())) {
			message = translator.translate(emptyState.getMessageI18nKey(), emptyState.getMessageI18nArgs());
		} else {
			Util.createPackageTranslator(EmptyState.class, translator.getLocale()).translate("default.message");
		}
		sb.append("<h3 class='o_empty_msg'>").append(message).append("</h3>");
		if (StringHelper.containsNonWhitespace(emptyState.getHintI18nKey())) {
			sb.append("<div class='o_empty_hint'>").append(translator.translate(emptyState.getHintI18nKey(), emptyState.getHintI18nArgs())).append("</div>");
		}
		if (StringHelper.containsNonWhitespace(emptyState.getButtonI18nKey())) {
			String customDisplayText = translator.translate(emptyState.getButtonI18nKey());
			emptyState.getButton().setCustomDisplayText(customDisplayText);
			sb.append("<div class='o_empty_action'>");
			renderer.render(emptyState.getButton(), sb, args);
			sb.append("</div>");
		}
		sb.append("</div>");
	}
	
}
