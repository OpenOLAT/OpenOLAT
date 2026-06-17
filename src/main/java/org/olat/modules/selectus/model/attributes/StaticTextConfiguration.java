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
package org.olat.modules.selectus.model.attributes;

import java.util.Locale;

/**
 * 
 * Initial date: 22 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StaticTextConfiguration implements AttributeConfiguration {
	
	private String text;
	private String textDe;
	private TextDisplay display;
	
	public static StaticTextConfiguration defaultConfiguration() {
		StaticTextConfiguration config = new StaticTextConfiguration();
		config.setDisplay(TextDisplay.greyBox);
		return config;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getTextDe() {
		return textDe;
	}
	
	public void setTextDe(String textDe) {
		this.textDe = textDe;
	}
	
	public String getText(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getTextDe();
		}
		return getText();
	}
	
	public void setText(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setTextDe(text);
		} else {
			setText(text);
		}
	}
	
	public TextDisplay getDisplay() {
		return display;
	}

	public void setDisplay(TextDisplay display) {
		this.display = display;
	}

	public enum TextDisplay {
		simple(""),
		greyBox("o_info");
		
		private final String cssClass;
		
		private TextDisplay(String cssClass) {
			this.cssClass = cssClass;
		}
		
		public String cssClass() {
			return cssClass;
		}
	}
}
