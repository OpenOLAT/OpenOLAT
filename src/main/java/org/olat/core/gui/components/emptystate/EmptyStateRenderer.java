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
	
	public interface ButtonRenderer {
		void render();
	}
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		EmptyState emptyState = (EmptyState)source;
		
		String message = getMessage(emptyState, translator);
		String hint = getHint(emptyState, translator);
		String desc = getDesc(emptyState, translator);
		ButtonRenderer buttonRenderer = getButtonRenderer(renderer, sb, emptyState, translator, args);
		ButtonRenderer secondaryButtonRenderer = getSecondaryButtonRenderer(renderer, sb, emptyState, translator, args);

		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIndicatorIconCss(emptyState.getIndicatorIconCss())
				.withIconCss(emptyState.getIconCss())
				.withMessageTranslated(message)
				.withHintTranslated(hint)
				.withDescTranslated(desc)
				.build();
		renderEmptyState(sb, emptyState.getElementCssClass(), emptyStateConfig, buttonRenderer, secondaryButtonRenderer);
	}

	private String getMessage(EmptyState emptyState, Translator translator) {
		if (StringHelper.containsNonWhitespace(emptyState.getMessageTranslated())) {
			return emptyState.getMessageTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getMessageI18nKey())) {
			return translator.translate(emptyState.getMessageI18nKey(), emptyState.getMessageI18nArgs());
		} else {
			return Util.createPackageTranslator(EmptyState.class, translator.getLocale()).translate("default.message");
		}
	}

	private String getHint(EmptyState emptyState, Translator translator) {
		if (StringHelper.containsNonWhitespace(emptyState.getHintTranslated())) {
			return emptyState.getHintTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getHintI18nKey())) {
			return translator.translate(emptyState.getHintI18nKey(), emptyState.getHintI18nArgs());
		}
		return null;
	}
	
	private String getDesc(EmptyState emptyState, Translator translator) {
		if (StringHelper.containsNonWhitespace(emptyState.getDescTranslated())) {
			return emptyState.getDescTranslated();
		} else if (StringHelper.containsNonWhitespace(emptyState.getDescI18nKey())) {
			return translator.translate(emptyState.getDescI18nKey(), emptyState.getDescI18nArgs());
		}
		return null;
	}

	private ButtonRenderer getButtonRenderer(Renderer renderer, StringOutput sb, EmptyState emptyState, Translator translator, String[] args) {
		String buttonText = getButtonText(emptyState, translator);
		if (StringHelper.containsNonWhitespace(buttonText)) {
			return () -> {
				emptyState.getButton().setCustomDisplayText(buttonText);
				renderer.render(emptyState.getButton(), sb, args);
			};
		}
		return null;
	}

	private String getButtonText(EmptyState emptyState, Translator translator) {
		if (StringHelper.containsNonWhitespace(emptyState.getButtonI18nKey())) {
			return translator.translate(emptyState.getButtonI18nKey());
		} else if (StringHelper.containsNonWhitespace(emptyState.getButtonTranslated())) {
			return emptyState.getButtonTranslated();
		}
		return null;
	}

	private ButtonRenderer getSecondaryButtonRenderer(Renderer renderer, StringOutput sb, EmptyState emptyState, Translator translator, String[] args) {
		String secondaryButtonText = getSecondaryButtonText(emptyState, translator);
		if (StringHelper.containsNonWhitespace(secondaryButtonText)) {
			return () -> {
				emptyState.getSecondaryButton().setCustomDisplayText(secondaryButtonText);
				renderer.render(emptyState.getSecondaryButton(), sb, args);
			};
		}
		return null;
	}

	private String getSecondaryButtonText(EmptyState emptyState, Translator translator) {
		if (StringHelper.containsNonWhitespace(emptyState.getSecondaryButtonI18nKey())) {
			return translator.translate(emptyState.getSecondaryButtonI18nKey());
		}
		return null;
	}

	public static void renderEmptyState(StringOutput sb,
										String elementCssClass, EmptyStateConfig emptyStateConfig,
										ButtonRenderer buttonRenderer,
										ButtonRenderer secondaryButtonRenderer) {
		String indicatorIconCss = StringHelper.containsNonWhitespace(emptyStateConfig.getIndicatorIconCss()) ? 
				emptyStateConfig.getIndicatorIconCss() : "o_icon_empty_indicator";
		String iconCss = StringHelper.containsNonWhitespace(emptyStateConfig.getIconCss()) ? 
				emptyStateConfig.getIconCss() : "o_icon_empty_objects";

		sb.append("<div class='o_empty_state");
		if (StringHelper.containsNonWhitespace(elementCssClass)) {
			sb.append(" ").append(elementCssClass);
		}
		sb.append("'");
		if (emptyStateConfig.getWrapperSelector() != null) {
			sb.append(" id='").append(emptyStateConfig.getWrapperSelector()).append("'");
		}
		sb.append(">");
		
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

		if (buttonRenderer != null || secondaryButtonRenderer != null) {
			sb.append("<div class='o_empty_action'>");
			if (buttonRenderer != null) {
				buttonRenderer.render();
			}

			if (secondaryButtonRenderer != null) {
				sb.append(" ");
				secondaryButtonRenderer.render();
			}
			sb.append("</div>");
		}

		sb.append("</div>");
	}
}
