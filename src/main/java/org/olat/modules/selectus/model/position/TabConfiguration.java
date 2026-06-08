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
package org.olat.modules.selectus.model.position;

import java.util.Locale;

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabConfiguration {

	private Tab tab;
	private String help;
	private String helpDe;
	private String helpFr;
	private String additionalHelp;
	private String additionalHelpDe;
	private String additionalHelpFr;
	private String title;
	private String titleDe;
	private String titleFr;
	private String heading;
	private String headingDe;
	private String headingFr;
	private boolean disabled;
	
	public TabConfiguration() {
		//
	}
	
	public TabConfiguration(Tab tab) {
		this.tab = tab;
	}
	
	public Tab getTab() {
		return tab;
	}
	
	public void setTab(Tab tab) {
		this.tab = tab;
	}
	
	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleDe() {
		return titleDe;
	}

	public void setTitleDe(String titleDe) {
		this.titleDe = titleDe;
	}
	
	public String getTitleFr() {
		return titleFr;
	}

	public void setTitleFr(String titleFr) {
		this.titleFr = titleFr;
	}

	public String getTitle(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getTitleDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getTitleFr();
		}
		return getTitle();
	}
	
	public void setTitle(String title, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setTitleDe(title);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setTitleFr(title);
		} else {
			setTitle(title);
		}
	}
	
	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getHeadingDe() {
		return headingDe;
	}

	public void setHeadingDe(String headingDe) {
		this.headingDe = headingDe;
	}

	public String getHeadingFr() {
		return headingFr;
	}

	public void setHeadingFr(String headingFr) {
		this.headingFr = headingFr;
	}
	
	public String getHeading(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getHeadingDe();
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			return getHeadingFr();
		}
		return getHeading();
	}
	
	public void setHeading(String title, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setHeadingDe(title);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setHeadingFr(title);
		} else {
			setHeading(title);
		}
	}

	public String getHelp() {
		return help;
	}
	
	public void setHelp(String help) {
		this.help = help;
	}
	
	public String getHelpDe() {
		return helpDe;
	}
	
	public void setHelpDe(String helpDe) {
		this.helpDe = helpDe;
	}
	
	public String getHelpFr() {
		return helpFr;
	}

	public void setHelpFr(String helpFr) {
		this.helpFr = helpFr;
	}

	public void setHelp(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setHelpDe(text);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setHelpFr(text);
		} else {
			setHelp(text);
		}
	}
	
	public String getHelp(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getHelpDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getHelpFr();
		}
		return getHelp();
	}
	
	
	public String getAdditionalHelp() {
		return additionalHelp;
	}
	
	public void setAdditionalHelp(String additionalHelp) {
		this.additionalHelp = additionalHelp;
	}
	
	public String getAdditionalHelpDe() {
		return additionalHelpDe;
	}
	
	public void setAdditionalHelpDe(String additionalHelpDe) {
		this.additionalHelpDe = additionalHelpDe;
	}
	
	public String getAdditionalHelpFr() {
		return additionalHelpFr;
	}

	public void setAdditionalHelpFr(String additionalHelpFr) {
		this.additionalHelpFr = additionalHelpFr;
	}

	public void setAdditionalHelp(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setAdditionalHelpDe(text);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setAdditionalHelpFr(text);
		} else {
			setAdditionalHelp(text);
		}
	}
	
	public String getAdditionalHelp(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getAdditionalHelpDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getAdditionalHelpFr();
		}
		return getAdditionalHelp();
	}
}
