/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.emptystate;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EmptyStateConfigBuilder {
	
	private String iconCss;
	private String indicatorIconCss;
	private String messageI18nKey;
	private String[] messageI18nArgs;
	private String messageTranslated;
	private String hintI18nKey;
	private String[] hintI18nArgs;
	private String hintTranslated;
	private String descI18nKey;
	private String[] descI18nArgs;
	private String descTranslated;
	private EmptyStateVariant variant = EmptyStateVariant.standard;
	private String helpTranslated;
	private String helpPage;
	private EmptyStateButton primaryButton;
	private List<EmptyStateButton> secondaryButtons = new ArrayList<>();
	
	EmptyStateConfigBuilder() {
		//
	}
	
	public EmptyStateConfigBuilder withVariant(EmptyStateVariant variant) {
		this.variant = variant;
		return this;
	}

	public EmptyStateConfigBuilder withIconCss(String iconCss) {
		this.iconCss = iconCss;
		return this;
	}
	
	public EmptyStateConfigBuilder withIndicatorIconCss(String indicatorIconCss) {
		this.indicatorIconCss = indicatorIconCss;
		return this;
	}
	
	public EmptyStateConfigBuilder withMessageI18nKey(String messageI18nKey) {
		this.messageI18nKey = messageI18nKey;
		return this;
	}
	
	public EmptyStateConfigBuilder withMessageI18nArgs(String[] messageI18nArgs) {
		this.messageI18nArgs = messageI18nArgs;
		return this;
	}
	
	public EmptyStateConfigBuilder withMessageTranslated(String messageTranslated) {
		this.messageTranslated = messageTranslated;
		return this;
	}
	
	public EmptyStateConfigBuilder withHintI18nKey(String hintI18nKey) {
		this.hintI18nKey = hintI18nKey;
		return this;
	}
	
	public EmptyStateConfigBuilder withHintI18nArgs(String[] hintI18nArgs) {
		this.hintI18nArgs = hintI18nArgs;
		return this;
	}

	public EmptyStateConfigBuilder withHintTranslated(String hintTranslated) {
		this.hintTranslated = hintTranslated;
		return this;
	}

	public EmptyStateConfigBuilder withDescI18nKey(String descI18nKey) {
		this.descI18nKey = descI18nKey;
		return this;
	}

	public EmptyStateConfigBuilder withDescI18nArgs(String[] descI18nArgs) {
		this.descI18nArgs = descI18nArgs;
		return this;
	}

	public EmptyStateConfigBuilder withDescTranslated(String descTranslated) {
		this.descTranslated = descTranslated;
		return this;
	}
	
	public EmptyStateConfigBuilder withHelp(String helpTranslated, String page) {
		this.helpTranslated = helpTranslated;
		this.helpPage = page;
		return this;
	}
	
	public EmptyStateConfigBuilder withPrimaryButton(String leftIcon, String i18nKey, String translated) {
		if (StringHelper.containsNonWhitespace(i18nKey) ||  StringHelper.containsNonWhitespace(translated)) {
			primaryButton = new EmptyStateButton(leftIcon, i18nKey, translated, "primary");
		}
		return this;
	}
	
	public EmptyStateConfigBuilder withSecondaryButton(String leftIcon, String i18nKey, String translated, String action) {
		if (secondaryButtons.size() >= EmptyState.MAX_SECONDARY_BUTTONS) {
			throw new AssertionError("Too many secondary buttons for empty state");
		}
		if (StringHelper.containsNonWhitespace(i18nKey) || StringHelper.containsNonWhitespace(translated)) {
			secondaryButtons.add(new EmptyStateButton(leftIcon, i18nKey, translated, action));
		}
		return this;
	}
	
	public EmptyStateConfig build() {
		return new EmptyStateConfigImpl(variant, iconCss, indicatorIconCss, messageI18nKey, 
				messageI18nArgs, messageTranslated, hintI18nKey, hintI18nArgs, hintTranslated, descI18nKey, 
				descI18nArgs, descTranslated, helpTranslated, helpPage, primaryButton, secondaryButtons);
	}

	private static class EmptyStateConfigImpl implements EmptyStateConfig {

		private final String iconCss;
		private final String indicatorIconCss;
		private final String messageI18nKey;
		private final String[] messageI18nArgs;
		private final String messageTranslated;
		private final String hintI18nKey;
		private final String[] hintI18nArgs;
		private final String hintTranslated;
		private final String descI18nKey;
		private final String[] descI18nArgs;
		private final String descTranslated;
		private final String helpTranslated;
		private final String helpPage;
		private final EmptyStateVariant variant;
		private final EmptyStateButton primaryButton;
		private final List<EmptyStateButton> secondaryButtons;

		public EmptyStateConfigImpl(EmptyStateVariant variant, String iconCss, String indicatorIconCss, String messageI18nKey,
									String[] messageI18nArgs, String messageTranslated, String hintI18nKey, String[] hintI18nArgs,
									String hintTranslated, String descI18nKey, String[] descI18nArgs,
									String descTranslated, String helpTranslated, String helpPage, 
									EmptyStateButton primaryButton, List<EmptyStateButton> secondaryButtons) {
			this.variant = variant;
			this.iconCss = iconCss;
			this.indicatorIconCss = indicatorIconCss;
			this.messageI18nKey = messageI18nKey;
			this.messageI18nArgs = messageI18nArgs;
			this.messageTranslated = messageTranslated;
			this.hintI18nKey = hintI18nKey;
			this.hintI18nArgs = hintI18nArgs;
			this.hintTranslated = hintTranslated;
			this.descI18nKey = descI18nKey;
			this.descI18nArgs = descI18nArgs;
			this.descTranslated = descTranslated;
			this.helpTranslated = helpTranslated;
			this.helpPage = helpPage;
			this.primaryButton = primaryButton;
			this.secondaryButtons = secondaryButtons;
		}

		@Override
		public String getIconCss() {
			return iconCss;
		}

		@Override
		public String getIndicatorIconCss() {
			return indicatorIconCss;
		}

		@Override
		public String getMessageI18nKey() {
			return messageI18nKey;
		}

		@Override
		public String[] getMessageI18nArgs() {
			return messageI18nArgs;
		}

		@Override
		public String getMessageTranslated() {
			return messageTranslated;
		}

		@Override
		public String getHintI18nKey() {
			return hintI18nKey;
		}

		@Override
		public String[] getHintI18nArgs() {
			return hintI18nArgs;
		}

		@Override
		public String getHintTranslated() {
			return hintTranslated;
		}

		@Override
		public String getDescI18nKey() {
			return descI18nKey;
		}

		@Override
		public String[] getDescI18nArgs() {
			return descI18nArgs;
		}

		@Override
		public String getDescTranslated() {
			return descTranslated;
		}
		
		@Override
		public String getHelpTranslated() {
			return helpTranslated;
		}
		
		@Override
		public String getHelpPage() {
			return helpPage;
		}

		@Override
		public EmptyStateVariant getVariant() {
			return variant;
		}

		@Override
		public EmptyStateButton getPrimaryButton() {
			return primaryButton;
		}

		@Override
		public List<EmptyStateButton> getSecondaryButtons() {
			return secondaryButtons;
		}
	}
}
