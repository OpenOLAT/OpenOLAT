/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 4 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditHelper {
	
	public static void calculatePositionLanguages(Position position, List<Locale> positionLanguages, Map<String,Locale> positionLanguageToLocale) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		
		Locale[] locales;
		if(position == null) {
			locales = new Locale[] { recruitingModule.getReportingLocale() };
		} else {
			locales = recruitingModule.getPositionLocales();
			if(position != null && StringHelper.containsNonWhitespace(position.getAvailableLanguages())) {
				String[] langArr = position.getAvailableLanguagesArray();
				for(Locale locale:locales) {
					for(String lang:langArr) {
						if(locale.getLanguage().equals(lang) && !positionLanguages.contains(locale)) {
							positionLanguages.add(locale);
						}
					}
				}
			} 
		}
		if(positionLanguages.isEmpty()) {
			for(Locale locale:locales) {
				positionLanguages.add(locale);
			}
		}
		for(int i=locales.length; i-->0; ) {
			positionLanguageToLocale.put(locales[i].getLanguage(), locales[i]);
		}
	}

}
