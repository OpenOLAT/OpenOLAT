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
import org.olat.core.gui.components.link.Link;
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
		
		String message = null;
		if (StringHelper.containsNonWhitespace(emptyState.getMessageTranslated())) {
			message = emptyState.getMessageTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getMessageI18nKey())) {
			message = translator.translate(emptyState.getMessageI18nKey(), emptyState.getMessageI18nArgs());
		} else {
			Util.createPackageTranslator(EmptyState.class, translator.getLocale()).translate("default.message");
		}

		String hint = null;
		if (StringHelper.containsNonWhitespace(emptyState.getHintTranslated())) {
			hint = emptyState.getHintTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getHintI18nKey())) {
			hint = translator.translate(emptyState.getHintI18nKey(), emptyState.getHintI18nArgs());
		}

		String desc = null;
		if (StringHelper.containsNonWhitespace(emptyState.getDescTranslated())) {
			desc = emptyState.getDescTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getDescI18nKey())) {
			desc = translator.translate(emptyState.getDescI18nKey(), emptyState.getDescI18nArgs());
		}

		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIndicatorIconCss(emptyState.getIndicatorIconCss())
				.withIconCss(emptyState.getIconCss())
				.withMessageTranslated(message)
				.withHintTranslated(hint)
				.withDescTranslated(desc)
				.withButtonI18nKey(emptyState.getButtonI18nKey())
				.withButtonTranslated(emptyState.getButtonTranslated())
				.withSecondaryButtonI18nKey(emptyState.getSecondaryButtonI18nKey())
				.build();
		renderEmptyState(renderer, sb, translator, emptyState.getElementCssClass(), emptyStateConfig, 
				emptyState.getButton(), emptyState.getSecondaryButton(), args);
	}
	
	/**
	 * Renders an empty state UI element based on the provided configuration.
	 *
	 * @param renderer The renderer used to render the UI buttons and actions.
	 * @param sb The StringOutput to append the rendered HTML output.
	 * @param translator Used to translate i18n keys into localized text.
	 * @param elementCssClass Optional CSS class to be added to the root element of the empty state.
	 * @param emptyStateConfig The configuration object defining the content and layout of the empty state.
	 * @param button The primary action button to be rendered in the empty state, if applicable.
	 * @param secondaryButton The secondary action button to be rendered in the empty state, if applicable.
	 * @param args Optional arguments passed to the renderer for processing.
	 */
	public static void renderEmptyState(Renderer renderer, StringOutput sb, Translator translator,
										String elementCssClass, EmptyStateConfig emptyStateConfig,
										Link button, Link secondaryButton, String[] args) {
		String indicatorIconCss = StringHelper.containsNonWhitespace(emptyStateConfig.getIndicatorIconCss()) ? 
				emptyStateConfig.getIndicatorIconCss() : "o_icon_empty_indicator";
		String iconCss = StringHelper.containsNonWhitespace(emptyStateConfig.getIconCss()) ? 
				emptyStateConfig.getIconCss() : "o_icon_empty_objects";

		// empty state element
		sb.append("<div class='o_empty_state");
		if (StringHelper.containsNonWhitespace(elementCssClass)) {
			sb.append(" ").append(elementCssClass);
		}
		sb.append("'>");
		
		// icon
		sb.append("<div class='o_empty_visual'><i class='o_icon ").append(indicatorIconCss).append("'></i>");
		sb.append("<i class='o_icon ").append(iconCss).append("'> </i>");
		sb.append("</div>");

		// message
		sb.append("<div class='o_empty_msg'>").append(emptyStateConfig.getMessageTranslated()).append("</div>");

		// hint
		if (StringHelper.containsNonWhitespace(emptyStateConfig.getHintTranslated())) {
			sb.append("<div class='o_empty_hint'>").append(emptyStateConfig.getHintTranslated()).append("</div>");
		}

		// description
		if (StringHelper.containsNonWhitespace(emptyStateConfig.getDescTranslated())) {
			sb.append("<small class='text-mutedx'>").append(emptyStateConfig.getDescTranslated()).append("</small>");
		}

		String buttonCustomDisplayText = null;
		if (StringHelper.containsNonWhitespace(emptyStateConfig.getButtonI18nKey())) {
			buttonCustomDisplayText = translator.translate(emptyStateConfig.getButtonI18nKey());
		} else if (StringHelper.containsNonWhitespace(emptyStateConfig.getButtonTranslated())) {
			buttonCustomDisplayText = emptyStateConfig.getButtonTranslated();
		}
		
		String secondaryButtonCustomDisplayText = null;
		if (StringHelper.containsNonWhitespace(emptyStateConfig.getSecondaryButtonI18nKey())) {
			secondaryButtonCustomDisplayText = translator.translate(emptyStateConfig.getSecondaryButtonI18nKey());
		}

		if (StringHelper.containsNonWhitespace(buttonCustomDisplayText) ||
				StringHelper.containsNonWhitespace(secondaryButtonCustomDisplayText)) {
			sb.append("<div class='o_empty_action'>");
			if (button != null && StringHelper.containsNonWhitespace(buttonCustomDisplayText)) {
				button.setCustomDisplayText(buttonCustomDisplayText);
				renderer.render(button, sb, args);
			}

			if (secondaryButton != null && StringHelper.containsNonWhitespace(secondaryButtonCustomDisplayText)) {
				sb.append(" ");
				secondaryButton.setCustomDisplayText(secondaryButtonCustomDisplayText);
				renderer.render(secondaryButton, sb, args);
			}
			sb.append("</div>");
		}

		// close empty state element
		sb.append("</div>");
	}
	
}
