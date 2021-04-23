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

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EmptyStateConfigBuilder {
	
	private String iconCss;
	private String indicatorIconCss;
	private String messageI18nKey;
	private String hintI18nKey;
	private String buttonI18nKey;
	
	EmptyStateConfigBuilder() {
		//
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
	
	public EmptyStateConfigBuilder withHintI18nKey(String messageI18nKey) {
		this.messageI18nKey = messageI18nKey;
		return this;
	}
	
	public EmptyStateConfigBuilder withButtonI18nKey(String buttonI18nKey) {
		this.buttonI18nKey = buttonI18nKey;
		return this;
	}
	
	public EmptyStateConfig build() {
		return new EmptyStateConfigImpl(iconCss, indicatorIconCss, messageI18nKey, hintI18nKey, buttonI18nKey);
	}
	
	private static class EmptyStateConfigImpl implements EmptyStateConfig {
		
		private final String iconCss;
		private final String indicatorIconCss;
		private final String messageI18nKey;
		private final String hintI18nKey;
		private final String buttonI18nKey;
		
		public EmptyStateConfigImpl(String iconCss, String indicatorIconCss, String messageI18nKey, String hintI18nKey,
				String buttonI18nKey) {
			this.iconCss = iconCss;
			this.indicatorIconCss = indicatorIconCss;
			this.messageI18nKey = messageI18nKey;
			this.hintI18nKey = hintI18nKey;
			this.buttonI18nKey = buttonI18nKey;
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
		public String getHintI18nKey() {
			return hintI18nKey;
		}
		
		@Override
		public String getButtonI18nKey() {
			return buttonI18nKey;
		}
		
	}
	
}
