/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.title_generator;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.PersonName;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("salutationGeneratorETH")
public class ETHSalutationGenerator extends AbstractSalutationGenerator {
	

	@Override
	public String getSalutation(Application app, Locale locale) {
		return "Dear " + getTitleFullname(app.getPerson(), locale);
	}

	@Override
	public String getSalutation(ApplicationShort app, Locale locale) {
		return "Dear " + getTitleFullname(app.getPerson(), locale);
	}

	@Override
	public String getSalutation(PersonName person, Locale locale) {
		return "Dear " + getTitleFullname(person, locale);
	}
	
	@Override
	public String getTitleLastName(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			String title = person.getTitle();
			Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
			
			//WENN Titel ( - kein Titel) DANN Dear FIRST NAME LAST NAME (Dear Peter Müller) 
			//WENN Titel Dr. DANN Dear Dr. LAST NAME (Dear Dr. Müller) 
			//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
			//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
			if(StringHelper.containsNonWhitespace(title) ) {
				if(title.startsWith("prof") || title.startsWith("Prof")) {
					sb.append(translator.translate("apply_application.email.prof"));
				} else if (title.startsWith("Dr") || title.startsWith("dr")) {
					sb.append(translator.translate("apply_application.email.dr"));
				} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
					// fallback for unknown title
					sb.append(person.getFirstName());
				}
			} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
				// fallback for no title
				sb.append(person.getFirstName());
			}
			
			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
	
	@Override
	public String getTitleFirstLastName(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			String title = person.getTitle();
			Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
			
			//WENN Titel ( - kein Titel) DANN Dear FIRST NAME LAST NAME (Dear Peter Müller) 
			//WENN Titel Dr. DANN Dear Dr. LAST NAME (Dear Dr. Müller) 
			//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
			//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
			boolean firstnameFallback = false;
			if(StringHelper.containsNonWhitespace(title) ) {
				if(title.startsWith("prof") || title.startsWith("Prof")) {
					sb.append(translator.translate("apply_application.email.prof"));
				} else if (title.startsWith("Dr") || title.startsWith("dr")) {
					sb.append(translator.translate("apply_application.email.dr"));
				} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
					// fallback for unknown title
					sb.append(person.getFirstName());
					firstnameFallback = true;
				}
			}
			
			if(!firstnameFallback && StringHelper.containsNonWhitespace(person.getFirstName())) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(person.getFirstName());
			}

			if(StringHelper.containsNonWhitespace(person.getLastName())) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(person.getLastName());
			}
		}
		return sb.toString();
	}
	

	@Override
	public String getTitleFullname(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			String title = person.getTitle();
			Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
			
			//WENN Titel ( - kein Titel) DANN Dear FIRST NAME LAST NAME (Dear Peter Müller) 
			//WENN Titel Dr. DANN Dear Dr. LAST NAME (Dear Dr. Müller) 
			//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
			//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
			if(StringHelper.containsNonWhitespace(title) ) {
				if(title.startsWith("prof") || title.startsWith("Prof")) {
					sb.append(translator.translate("apply_application.email.prof"));
				} else if (title.startsWith("Dr") || title.startsWith("dr")) {
					sb.append(translator.translate("apply_application.email.dr"));
				} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
					sb.append(person.getFirstName());
				}
			} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
				sb.append(person.getFirstName());
			}
			
			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
}
