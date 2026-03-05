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

import java.util.ArrayList;
import java.util.List;

/**
 * Initial date: 2026-03-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EmptyStateRenderConfigBuilder {

	private String wrapperSelector;
	private String iconCss;
	private String indicatorIconCss;
	private String messageTranslated;
	private String hintTranslated;
	private String descTranslated;
	private EmptyStateVariant variant = EmptyStateVariant.standard;
	private String helpTranslated;
	private String helpPage;
	private EmptyStateButtonRenderer primaryButtonRenderer;
	private final List<EmptyStateButtonRenderer> secondaryButtonRenderers = new ArrayList<>();

	EmptyStateRenderConfigBuilder() {
		//
	}

	public EmptyStateRenderConfigBuilder withVariant(EmptyStateVariant variant) {
		this.variant = variant;
		return this;
	}

	public EmptyStateRenderConfigBuilder withWrapperSelector(String wrapperSelector) {
		this.wrapperSelector = wrapperSelector;
		return this;
	}

	public EmptyStateRenderConfigBuilder withIconCss(String iconCss) {
		this.iconCss = iconCss;
		return this;
	}

	public EmptyStateRenderConfigBuilder withIndicatorIconCss(String indicatorIconCss) {
		this.indicatorIconCss = indicatorIconCss;
		return this;
	}

	public EmptyStateRenderConfigBuilder withMessageTranslated(String messageTranslated) {
		this.messageTranslated = messageTranslated;
		return this;
	}

	public EmptyStateRenderConfigBuilder withHintTranslated(String hintTranslated) {
		this.hintTranslated = hintTranslated;
		return this;
	}

	public EmptyStateRenderConfigBuilder withDescTranslated(String descTranslated) {
		this.descTranslated = descTranslated;
		return this;
	}

	public EmptyStateRenderConfigBuilder withHelp(String helpTranslated, String page) {
		this.helpTranslated = helpTranslated;
		this.helpPage = page;
		return this;
	}

	public EmptyStateRenderConfigBuilder withPrimaryButtonRenderer(EmptyStateButtonRenderer renderer) {
		primaryButtonRenderer = renderer;
		return this;
	}

	public EmptyStateRenderConfigBuilder withSecondaryButtonRenderer(EmptyStateButtonRenderer renderer) {
		if (renderer != null) {
			secondaryButtonRenderers.add(renderer);
		}
		return this;
	}

	public EmptyStateRenderConfig build() {
		return new EmptyStateRenderConfigImpl(variant, wrapperSelector, iconCss, indicatorIconCss, messageTranslated, 
				hintTranslated, descTranslated, helpTranslated, helpPage, primaryButtonRenderer, 
				secondaryButtonRenderers);
	}

	private static class EmptyStateRenderConfigImpl implements EmptyStateRenderConfig {

		private final String wrapperSelector;
		private final String iconCss;
		private final String indicatorIconCss;
		private final String messageTranslated;
		private final String hintTranslated;
		private final String descTranslated;
		private final String helpTranslated;
		private final String helpPage;
		private final EmptyStateVariant variant;
		private final EmptyStateButtonRenderer primaryButtonRenderer;
		private final List<EmptyStateButtonRenderer> secondaryButtonRenderers;

		public EmptyStateRenderConfigImpl(EmptyStateVariant variant, String wrapperSelector, String iconCss,
										  String indicatorIconCss, String messageTranslated, String hintTranslated, 
										  String descTranslated, String helpTranslated, String helpPage, 
										  EmptyStateButtonRenderer primaryButtonRenderer, 
										  List<EmptyStateButtonRenderer> secondaryButtonRenderers) {
			this.variant = variant;
			this.wrapperSelector = wrapperSelector;
			this.iconCss = iconCss;
			this.indicatorIconCss = indicatorIconCss;
			this.messageTranslated = messageTranslated;
			this.hintTranslated = hintTranslated;
			this.descTranslated = descTranslated;
			this.helpTranslated = helpTranslated;
			this.helpPage = helpPage;
			this.primaryButtonRenderer = primaryButtonRenderer;
			this.secondaryButtonRenderers = secondaryButtonRenderers;
		}

		@Override
		public String getWrapperSelector() {
			return wrapperSelector;
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
		public String getMessageTranslated() {
			return messageTranslated;
		}

		@Override
		public String getHintTranslated() {
			return hintTranslated;
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
		public EmptyStateButtonRenderer getPrimaryButtonRenderer() {
			return primaryButtonRenderer;
		}

		@Override
		public List<EmptyStateButtonRenderer> getSecondaryButtonRenderers() {
			return secondaryButtonRenderers;
		}
	}

}
