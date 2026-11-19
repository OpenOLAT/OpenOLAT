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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
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
		
		String message = getMessage(emptyState, translator);
		String hint = getHint(emptyState, translator);

		EmptyStateRenderConfigBuilder builder = EmptyStateRenderConfig.builder()
				.withWrapperSelector(Renderer.getComponentPrefix(emptyState))
				.withVariant(emptyState.getVariant())
				.withIndicatorIconCss(emptyState.getIndicatorIconCss())
				.withIconCss(emptyState.getIconCss())
				.withMessageTranslated(message)
				.withHintTranslated(hint)
				.withHelp(emptyState.getHelpTranslated(), emptyState.getHelpPage());

		addPrimaryButtonRenderer(builder, renderer, sb, emptyState, translator, args);
		addSecondaryButtonRenderers(builder, renderer, sb, emptyState, translator, args);

		renderEmptyState(sb, translator, emptyState.getElementCssClass(), builder.build());
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

	private void addPrimaryButtonRenderer(EmptyStateRenderConfigBuilder builder, Renderer renderer, StringOutput sb, 
										  EmptyState emptyState, Translator translator, String[] args) {
		String buttonText = getPrimaryButtonText(emptyState, translator);
		if (!StringHelper.containsNonWhitespace(buttonText)) {
			return;
		}
		builder.withPrimaryButtonRenderer(() -> {
			emptyState.getPrimaryButtonLink().setCustomDisplayText(buttonText);
			emptyState.getPrimaryButtonLink().setIconLeftCSS(getButtonLeftIcon(emptyState.getPrimaryButton()));
			renderer.render(emptyState.getPrimaryButtonLink(), sb, args);
		});
	}

	private String getPrimaryButtonText(EmptyState emptyState, Translator translator) {
		EmptyStateButton primaryButton = emptyState.getPrimaryButton();
		if (primaryButton == null) {
			return null;
		}
		return getButtonText(primaryButton, translator);
	}

	private String getButtonText(EmptyStateButton button, Translator translator) {
		if (StringHelper.containsNonWhitespace(button.i18nKey())) {
			return translator.translate(button.i18nKey());
		} else if (StringHelper.containsNonWhitespace(button.translated())) {
			return button.translated();
		}
		return null;
	}
	
	private String getButtonLeftIcon(EmptyStateButton button) {
		String leftIcon = button.leftIcon();
		if (StringHelper.containsNonWhitespace(leftIcon)) {
			if (!leftIcon.startsWith("o_icon ")) {
				leftIcon = "o_icon o_icon-fw " + leftIcon;
			}
			return leftIcon;
		}
		return null;
	}

	private void addSecondaryButtonRenderers(EmptyStateRenderConfigBuilder builder, Renderer renderer, StringOutput sb, 
											 EmptyState emptyState, Translator translator, String[] args) {
		if (emptyState.getSecondaryButtons() == null || emptyState.getSecondaryButtons().isEmpty()) {
			return;
		}
		for (int i = 0; i < emptyState.getSecondaryButtons().size(); i++) {
			EmptyStateButton secondaryButton = emptyState.getSecondaryButtons().get(i);
			String text = getButtonText(secondaryButton, translator);
			if (!StringHelper.containsNonWhitespace(text)) {
				continue;
			}
			String leftIcon = getButtonLeftIcon(secondaryButton);
			Link link = emptyState.getSecondaryButtonLinks().get(i);
			link.setCustomDisplayText(text);
			link.setIconLeftCSS(leftIcon);
			builder.withSecondaryButtonRenderer(() -> {
				renderer.render(link, sb, args);
			});
		}
	}

	public static void renderEmptyState(StringOutput sb, Translator translator,
										String elementCssClass, EmptyStateRenderConfig config) {
		EmptyStateVariant variant = config.getVariant() != null ? config.getVariant() : EmptyStateVariant.auto;
		String domId = config.getWrapperSelector();
		if (domId == null && variant == EmptyStateVariant.auto) {
			domId = "o_empty_" + CodeHelper.getRAMUniqueID();
		}
		
		sb.append("<div class='o_empty_state o_empty_").append(variant.name());
		sb.append(" o_empty_low", variant == EmptyStateVariant.auto);
		if (StringHelper.containsNonWhitespace(elementCssClass)) {
			sb.append(" ").append(elementCssClass);
		}
		sb.append("'");
		if (domId != null) {
			sb.append(" id='").append(domId).append("'");
		}
		sb.append(">");
		sb.append("<div class='o_empty_inner'>");

		String iconCss = StringHelper.containsNonWhitespace(config.getIconCss()) ? config.getIconCss() : "o_icon_question";
		sb.append("<div class='o_empty_circle'>");
		sb.append("<i class='o_icon ").append(iconCss).append("' aria-hidden='true'></i>");
		sb.append("</div>");

		sb.append("<div class='o_empty_body'>");
		sb.append("<div class='o_empty_texts'>");
		sb.append("<div class='o_empty_msg'>").append(config.getMessageTranslated()).append("</div>");
		if (StringHelper.containsNonWhitespace(config.getHintTranslated())) {
			sb.append("<div class='o_empty_hint'>").append(config.getHintTranslated()).append("</div>");
		}

		if (StringHelper.containsNonWhitespace(config.getHelpTranslated()) &&
				StringHelper.containsNonWhitespace(config.getHelpPage())) {
			sb.append("<div class='o_empty_help'>");
			HelpModule helpModule = CoreSpringFactory.getImpl(HelpModule.class);
			String url = helpModule.getManualProvider().getURL(translator.getLocale(), config.getHelpPage());
			String linkText = config.getHelpTranslated();
			String title = translator.translate("help.button");
			sb.append("<a href='").append(url).append("' target='_blank' title='").append(title).append("'>");
			sb.append(linkText).append(" ");
			sb.append("<i class='o_icon o_icon_help'></i>");
			sb.append("</a>");
			sb.append("</div>");
		}
		sb.append("</div>");

		if (config.getPrimaryButtonRenderer() != null ||
				(config.getSecondaryButtonRenderers() != null && !config.getSecondaryButtonRenderers().isEmpty())) {
			sb.append("<div class='o_empty_action'>");
			if (config.getPrimaryButtonRenderer() != null) {
				config.getPrimaryButtonRenderer().render();
			}
			if (config.getSecondaryButtonRenderers() != null && !config.getSecondaryButtonRenderers().isEmpty()) {
				for (EmptyStateButtonRenderer secondaryButton : config.getSecondaryButtonRenderers()) {
					secondaryButton.render();
				}
			}
			sb.append("</div>");
		}

		sb.append("</div>");
		sb.append("</div>");
		if (variant == EmptyStateVariant.auto) {
			sb.append("<script>'use strict';o_initEmptyStateAuto('").append(domId).append("');</script>");
		}
		sb.append("</div>");
	}
}
