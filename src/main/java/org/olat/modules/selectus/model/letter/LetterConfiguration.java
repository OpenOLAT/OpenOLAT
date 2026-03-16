/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.letter;

import java.util.Locale;

/**
 * 
 * Initial date: 12 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterConfiguration {
	
	private String title;
	private LetterLanguageConfiguration configurationEn;
	private LetterLanguageConfiguration configurationDe;
	private LetterLanguageConfiguration configurationFr;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LetterLanguageConfiguration getConfiguration(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getConfigurationDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getConfigurationFr();
		}
		return getConfigurationEn();
	}
	
	public LetterLanguageConfiguration getConfigurationEn() {
		if(configurationEn == null) {
			configurationEn = new LetterLanguageConfiguration();
		}
		return configurationEn;
	}
	
	public void setConfigurationEn(LetterLanguageConfiguration configurationEn) {
		this.configurationEn = configurationEn;
	}
	
	public LetterLanguageConfiguration getConfigurationDe() {
		if(configurationDe == null) {
			configurationDe = new LetterLanguageConfiguration();
		}
		return configurationDe;
	}
	
	public void setConfigurationDe(LetterLanguageConfiguration configurationDe) {
		this.configurationDe = configurationDe;
	}
	
	public LetterLanguageConfiguration getConfigurationFr() {
		if(configurationFr == null) {
			configurationFr = new LetterLanguageConfiguration();
		}
		return configurationFr;
	}
	
	public void setConfigurationFr(LetterLanguageConfiguration configuration) {
		this.configurationFr = configuration;
	}
}
