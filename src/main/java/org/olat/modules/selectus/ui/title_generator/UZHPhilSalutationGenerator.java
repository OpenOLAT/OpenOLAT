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
package org.olat.modules.selectus.ui.title_generator;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.PersonName;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * The salutation generator is in French, German and English and
 * it is gendered.
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service("salutationGeneratorUZHPhil")
public class UZHPhilSalutationGenerator extends AbstractSalutationGenerator {

	@Override
	public String getTitleLastName(PersonName person, Locale locale) {
		return titleLastName(false, person, locale);
	}

	@Override
	public String getTitleLastName(Application app, Locale locale) {
		return titleLastName(false, app.getPerson(), locale);
	}

	@Override
	public String getTitleLastName(ApplicationShort app, Locale locale) {
		return titleLastName(false, app.getPerson(), locale);
	}
	
	@Override
	public String getTitleFullname(PersonName person, Locale locale) {
		return titleFullname(false, person, locale);
	}

	@Override
	public String getTitleFullname(Application app, Locale locale) {
		return titleFullname(false, app.getPerson(), locale);
	}

	@Override
	public String getTitleFullname(ApplicationShort app, Locale locale) {
		return titleFullname(false, app.getPerson(), locale);
	}

	@Override
	public String getSalutation(Application app, Locale locale) {
		return titleFullname(true, app.getPerson(), locale);
	}

	@Override
	public String getSalutation(ApplicationShort app, Locale locale) {
		return titleFullname(true, app.getPerson(), locale);
	}
	
	@Override
	public String getSalutation(PersonName person, Locale locale) {
		return titleFullname(true, person, locale);
	}

	@Override
	public String getFullname(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder(64);
		if(StringHelper.containsNonWhitespace(person.getFirstName())) {
			sb.append(person.getFirstName());
		}
		
		String lastName = person.getLastName();
		if(StringHelper.containsNonWhitespace(lastName)) {
			if(sb.length() > 0) sb.append(' ');
			sb.append(lastName);
		}
		return sb.toString();
	}

	@Override
	public String getFullname(Application app, Locale locale) {
		return getFullname(app.getPerson(), locale);
	}

	@Override
	public String getFullname(ApplicationShort app, Locale locale) {
		return getFullname(app.getPerson(), locale);
	}

	@Override
	public String getTitleFirstLastName(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			PersonGender gender = getGender(person);
			String language = locale.getLanguage();
			
			if("en".equals(language)) {
				enLastName(sb, person, locale);
			} else {
				deFrLastName(sb, gender, person, locale);
			}
			
			if(StringHelper.containsNonWhitespace(person.getFirstName())) {
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

	private String titleLastName(boolean dear, PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			PersonGender gender = getGender(person);
			String language = locale.getLanguage();
			
			if("en".equals(language)) {
				if(dear) {
					sb.append("Dear ");
				}
				enLastName(sb, person, locale);
			} else if("fr".equals(language)) {
				if(dear) {
					if(gender == PersonGender.female) {
						sb.append("Ch\u00E8re Madame ");
					} else if(gender == PersonGender.male) {
						sb.append("Cher Monsieur ");
					} else {
						sb.append("Bonjour ");
					} 
				}
				deFrLastName(sb, gender, person, locale);
			} else {
				if(dear) {
					if(gender == PersonGender.female) {
						sb.append("Sehr geehrte Frau ");
					} else if(gender == PersonGender.male) {
						sb.append("Sehr geehrter Herr ");
					} else {
						sb.append("Guten Tag ");
					} 
				}
				deFrLastName(sb, gender, person, locale);
			}

			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
	
	//WENN Titel ( - kein Titel) DANN Dear LAST NAME (Dear Müller) 
	//WENN Titel Dr. DANN Dear Dr LAST NAME (Dear Dr Müller) 
	//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
	//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
	private void enLastName(StringBuilder sb, PersonName person, Locale locale) {
		String title = person.getTitle();
		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		if(StringHelper.containsNonWhitespace(title) ) {
			if(title.startsWith("prof") || title.startsWith("Prof")) {
				sb.append(translator.translate("apply_application.email.prof"));
			} else if (title.startsWith("Dr") || title.startsWith("dr")) {
				sb.append(translator.translate("apply_application.email.dr"));
			}
		}
	}
	
	private void deFrLastName(StringBuilder sb, PersonGender gender, PersonName person, Locale locale) {
		String title = person.getTitle();

		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		if(StringHelper.containsNonWhitespace(title) ) {
			if(title.startsWith("prof") || title.startsWith("Prof")) {
				sb.append(translator.translate("apply_application.email.prof." + gender.gender()));
			} else if (title.contains("Dr") || title.contains("dr")) {
				sb.append(translator.translate("apply_application.email.dr"));
			}
		}
	}

	private String titleFullname(boolean dear, PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			PersonGender gender = getGender(person);
			String language = locale.getLanguage();
			
			if("en".equals(language)) {
				if(dear) {
					sb.append("Dear ");
				}
				en(sb, person, locale);
			} else if("fr".equals(language)) {
				if(dear) {
					if(gender == PersonGender.female) {
						sb.append("Chère Madame ");
					} else if(gender == PersonGender.male) {
						sb.append("Cher Monsieur ");
					} else {
						sb.append("Bonjour ");
					}
				}
				defr(sb, gender, person, dear, locale);
			} else {
				if(dear) {
					if(gender == PersonGender.female) {
						sb.append("Sehr geehrte Frau ");
					} else if(gender == PersonGender.male) {
						sb.append("Sehr geehrter Herr ");
					} else {
						sb.append("Guten Tag ");
					}
				}
				defr(sb, gender, person, dear, locale);
			}
			
			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
	
	private PersonGender getGender(Object person) {
		PersonGender gender = PersonGender.male;
		if(person instanceof Person) {
			gender = PersonGender.genderOf(((Person)person).getGender());
		}
		return gender;
	}
	
	//WENN Titel ( - kein Titel) DANN Dear FIRST NAME LAST NAME (Dear Peter Müller) 
	//WENN Titel Dr. DANN Dear Dr LAST NAME (Dear Dr Müller) 
	//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
	//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
	private void en(StringBuilder sb, PersonName person, Locale locale) {
		String title = person.getTitle();
		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
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
	}
	
	private void defr(StringBuilder sb, PersonGender gender, PersonName person, boolean dear, Locale locale) {
		String title = person.getTitle();

		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		if(StringHelper.containsNonWhitespace(title) ) {
			if(title.startsWith("prof") || title.startsWith("Prof")) {
				sb.append(translator.translate("apply_application.email.prof." + gender.gender()));
			} else if (title.contains("Dr") || title.contains("dr")) {
				sb.append(translator.translate("apply_application.email.dr"));
			} else if((!dear || (gender != PersonGender.male && gender != PersonGender.female)) && StringHelper.containsNonWhitespace(person.getFirstName())) {
				sb.append(person.getFirstName());
			}
		} else if((!dear || (gender != PersonGender.male && gender != PersonGender.female)) && StringHelper.containsNonWhitespace(person.getFirstName())) {
			sb.append(person.getFirstName());
		}
	}
}
