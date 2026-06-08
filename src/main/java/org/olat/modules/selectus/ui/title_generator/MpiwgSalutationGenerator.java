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
import org.olat.modules.selectus.model.PersonName;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("salutationGeneratorMpiwg")
public class MpiwgSalutationGenerator extends AbstractSalutationGenerator {

	@Override
	public String getTitleLastName(PersonName person, Locale locale) {
		return titleFullname(false, person, locale);
	}

	@Override
	public String getTitleLastName(Application app, Locale locale) {
		return titleFullname(false, app.getPerson(), locale);
	}

	@Override
	public String getTitleLastName(ApplicationShort app, Locale locale) {
		return titleFullname(false, app.getPerson(), locale);
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
		return titleFullname(false, person, locale);
	}

	private String titleFullname(boolean dear, PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			String gender = null;
			if(person instanceof Person) {
				gender = ((Person)person).getGender();
			}
			if(gender != null) {
				if(gender.startsWith("f")) {
					gender = "f";
				} else if(gender.startsWith("m")) {
					gender = "m";
				}
			}

			String language = locale.getLanguage();
			boolean firstNameWritten = false;
			
			if("en".equals(language)) {
				if(dear) {
					sb.append("Dear ");
				}
				firstNameWritten = en(sb, person, locale);
			} else if("fr".equals(language)) {
				if(dear) {
					if(gender == null) {
						sb.append("Cher Monsieur, Ch\u00E8re Madame ");
					} else if(gender.equals("f")) {
						sb.append("Ch\\u00E8re Madame ");
					} else if(gender.equals("m")) {
						sb.append("Cher Monsieur ");
					} else if(gender.equals("o")) {
						sb.append("Bonjour ");
					} else {
						sb.append("Cher Monsieur, Ch\u00E8re Madame ");
					}
				}
				firstNameWritten = defr(sb, gender, person, locale);
			} else {
				if(dear) {
					if(gender == null) {
						sb.append("Sehr geehrte/r Frau/ Herr ");
					} else if(gender.equals("f")) {
						sb.append("Sehr geehrte Frau ");
					} else if(gender.equals("m")) {
						sb.append("Sehr geehrter Herr ");
					} else if(gender.equals("o")) {
						sb.append("Guten Tag ");
					} else {
						sb.append("Sehr geehrte/r Frau/ Herr ");
					}
				}
				firstNameWritten = defr(sb, gender, person, locale);
			}
			
			if(dear && !firstNameWritten && "de".equals(language)) {
				String firstName = person.getFirstName();
				if(StringHelper.containsNonWhitespace(firstName)) {
					if(sb.length() > 0) sb.append(' ');
					sb.append(firstName);
				}
			}
			
			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
	
	//WENN Titel ( - kein Titel) DANN Dear FIRST NAME LAST NAME (Dear Peter Müller) 
	//WENN Titel Dr. DANN Dear Dr LAST NAME (Dear Dr Müller) 
	//WENN Titel Prof. DANN Dear Professor LAST NAME (Dear Professor Müller) 
	//WENN Titel Prof. Dr. DANN Dear Professor LAST NAME (dito, Dear Professor Müller) 
	private boolean en(StringBuilder sb, PersonName person, Locale locale) {
		boolean writeFirstName = false;
		
		String title = person.getTitle();
		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		if(StringHelper.containsNonWhitespace(title) ) {
			if(title.startsWith("prof") || title.startsWith("Prof")) {
				sb.append(translator.translate("apply_application.email.prof"));
			} else if (title.startsWith("Dr") || title.startsWith("dr")) {
				sb.append(translator.translate("apply_application.email.dr"));
			} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
				sb.append(person.getFirstName());
				writeFirstName = true;
			}
		} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
			sb.append(person.getFirstName());
			writeFirstName = true;
		}
		
		return writeFirstName;
	}
	
	private boolean defr(StringBuilder sb, String gender, PersonName person, Locale locale) {
		boolean writeFirstName = false;
		
		String title = person.getTitle();
		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		if(StringHelper.containsNonWhitespace(title) ) {
			if(title.startsWith("prof") || title.startsWith("Prof")) {
				if(gender == null || "-".equals(gender)) {
					gender = "unspecified";
				}
				sb.append(translator.translate("apply_application.email.prof." + gender));
			} else if (title.contains("Dr") || title.contains("dr")) {
				sb.append(translator.translate("apply_application.email.dr"));
			}
		} else if(StringHelper.containsNonWhitespace(person.getFirstName())) {
			sb.append(person.getFirstName());
			writeFirstName = true;
		}
		return writeFirstName;
	}
}
