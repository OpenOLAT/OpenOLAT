/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMLHelper {
	
	public static String getShortMLTitle(Position position, Locale preferedLocale) {
		String title = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				title = position.getShortTitle(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						title = position.getShortTitle(new Locale(availableLanguageArr[i]));
					}
				}

				//default
				if(!StringHelper.containsNonWhitespace(title)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							title = position.getShortTitle(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(title)) {
			title = position.getShortTitle();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = position.getShortTitleDe();
		}
		return title;
	}
	
	public static String getPositionMLTitle(PositionCommonFields position, Locale preferedLocale) {
		String title = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				title = position.getPositionTitle(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						title = position.getPositionTitle(new Locale(availableLanguageArr[i]));
					}
				}
				
				//default
				if(!StringHelper.containsNonWhitespace(title)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							title = position.getPositionTitle(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(title)) {
			title = position.getPositionTitle();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = position.getPositionTitleDe();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = position.getPositionTitleFr();
		}
		return title;
	}
	
	public static String getPositionMLDescription(Position position, Locale preferedLocale) {
		String description = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				description = position.getDescription(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						description = position.getDescription(preferedLocale);
					}
				}

				//default
				if(!StringHelper.containsNonWhitespace(description)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							description = position.getDescription(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(description)) {
			description = position.getDescription();
		}
		if(!StringHelper.containsNonWhitespace(description)) {
			description = position.getDescription();
		}
		return description;
	}
	
	public static String getPositionMLDepartment(PositionCommonFields position, Locale preferedLocale) {
		String departement = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				departement = position.getDepartment(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						departement = position.getDepartment(preferedLocale);
					}
				}

				//default
				if(!StringHelper.containsNonWhitespace(departement)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							departement = position.getDepartment(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(departement)) {
			departement = position.getDepartment();
		}
		if(!StringHelper.containsNonWhitespace(departement)) {
			departement = position.getDepartmentDe();
		}
		if(!StringHelper.containsNonWhitespace(departement)) {
			departement = position.getDepartmentFr();
		}
		return departement;
	}
	
	public static String getMailTemplateMLSubject(PositionCommonFields position, PositionMailTemplate template, Locale preferedLocale) {
		String title = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				title = template.getSubject(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						title = template.getSubject(new Locale(availableLanguageArr[i]));
					}
				}
				
				//default
				if(!StringHelper.containsNonWhitespace(title)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							title = template.getSubject(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getSubject();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getSubjectDe();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getSubjectFr();
		}
		return title;
	}
	
	public static String getMailTemplateMLBody(PositionCommonFields position, PositionMailTemplate template, Locale preferedLocale) {
		String title = null;
		String availableLanguages = position.getAvailableLanguages();
		Locale defaultPositionLocale = CoreSpringFactory.getImpl(RecruitingModule.class).getPositionDefaultLocale();
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = availableLanguages.split(",");
			if(availableLanguageArr.length == 1) {
				title = template.getBody(new Locale(availableLanguageArr[0]));
			} else if(availableLanguageArr.length > 1) {
				//preferred
				for(int i=availableLanguageArr.length; i-->0; ) {
					if(preferedLocale.getLanguage().equals(availableLanguageArr[i])) {
						title = template.getBody(new Locale(availableLanguageArr[i]));
					}
				}
				
				//default
				if(!StringHelper.containsNonWhitespace(title)) {
					for(int i=availableLanguageArr.length; i-->0; ) {
						if(defaultPositionLocale.getLanguage().equals(availableLanguageArr[i])) {
							title = template.getBody(defaultPositionLocale);
						}
					}
				}
			}
		}

		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getBody();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getBodyDe();
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = template.getBodyFr();
		}
		return title;
	}
	
	public static String getApplicationConfirmationMailTemplate(Position position, Locale locale) {
		String textEn = position.getApplicationConfirmationMailTemplate();
		String textDe = position.getApplicationConfirmationMailTemplateDe();
		String textFr = position.getApplicationConfirmationMailTemplateFr();
		return RecruitingHelper.mlStringLenient(textEn, textDe, textFr, locale);
	}
	
	public static String getApplicationConfirmationWithRefereeManagementMailTemplate(Position position, Locale locale) {
		String textEn = position.getApplicationConfirmationWithRefereeManagementMailTemplate();
		String textDe = position.getApplicationConfirmationWithRefereeManagementMailTemplateDe();
		String textFr = position.getApplicationConfirmationWithRefereeManagementMailTemplateFr();
		return RecruitingHelper.mlStringLenient(textEn, textDe, textFr, locale);
	}
	
	public static String getApplicationDuplicateConfirmationMailTemplate(Position position, Locale locale) {
		String textEn = position.getApplicationConfirmationDuplicateMailTemplate();
		String textDe = position.getApplicationConfirmationDuplicateMailTemplateDe();
		String textFr = position.getApplicationConfirmationDuplicateMailTemplateFr();
		return RecruitingHelper.mlStringLenient(textEn, textDe, textFr, locale);
	}
	

}