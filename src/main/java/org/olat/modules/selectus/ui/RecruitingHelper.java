/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.mail.MailHelper;

import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.AcceptPolicyEnum;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.PersonName;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.Reference;

public class RecruitingHelper {
	private static final Logger log = Tracing.createLoggerFor(RecruitingHelper.class);
	public static final int ACADEMICAL_SECOND_MARKER = 11;

	private static final DateFormat formatAcademicalTableDate = new SimpleDateFormat("MMMMM yyyy", Locale.ENGLISH);
	private static final DateFormat formatAcademicalTableDateDE = new SimpleDateFormat("MMMMM yyyy", Locale.GERMAN);
	
	private static final DecimalFormat factorFormat = new DecimalFormat("#0", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private RecruitingHelper() {
		//
	}
	
	public static String sessionKeyAcceptedPolicy(PositionRef position, AcceptPolicyEnum policy) {
		return position.getKey() + "-" + policy.name();
	}
	
	public static boolean containsTemplate(String text) {
		return text != null && StringHelper.containsNonWhitespace(text)
				&& !text.equalsIgnoreCase("<p></p>")
				&& !text.equalsIgnoreCase("<p>&nbsp;</p>");
	}
	
	public static String beautifyCountriesList(String list) {
		if(list == null || list.isEmpty()) {
			return list;
		}
		return list.replace(",", ", ");
	}
	
	public static Date endOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}
	
	public static Date startOfDay(Date date)  {
		if(date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getStartOfDay(cal).getTime();
	}
	
	public static Calendar getStartOfDay(Calendar cal)  {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static String mlStringLenient(String textEn, String textDe, String textFr, Locale locale) {
		String val = null;
		if(locale != null) {
			if(locale.getLanguage().equals("de")) {
				val = textDe;
			} else if(locale.getLanguage().equals("fr")) {
				val = textFr;
 			} else if(locale.getLanguage().equals("en")) {
				val =textEn;
			}
		}
		
		if(!StringHelper.containsNonWhitespace(val)) {
			val = textEn;
		}
		if(!StringHelper.containsNonWhitespace(val)) {
			val = textDe;
		}
		if(!StringHelper.containsNonWhitespace(val)) {
			val = textFr;
		}
		return val;
	}
	
	public static String formatFactor(Double number) {
		String formatted = null;
		if(number != null) {
			synchronized(factorFormat) {
				formatted = factorFormat.format(number.doubleValue());
			}
		}
		return formatted;
	}
	
	public static String escWithBR(String text) {
		if(text == null) {
			return "";
		}
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder escaped = Formatter.escWithBR(text);
		return escaped == null ? "" : escaped.toString();
	}
	
	public static boolean isSummerTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int valFeb = cal.get(Calendar.DST_OFFSET);
		return valFeb > 1;
	}
	
	/**
	 * If the date is january -> return only the year (form compatibility reason), else
	 * return the month "." year.
	 * 
	 * @param date
	 * @return
	 */
	public static String formatAcademicalDate(Date date) {
		if(date == null) return "";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String year = Integer.toString(cal.get(Calendar.YEAR));
		if(cal.get(Calendar.SECOND) == ACADEMICAL_SECOND_MARKER) {
			year = (cal.get(Calendar.MONTH) + 1) + "." + year;
		}
		return year;
	}
	
	public static String formatGender(String gender, Locale locale) {
		PersonGender g = PersonGender.genderOf(gender);
		Translator translator = Util.createPackageTranslator(RecruitingMainController.class, locale);
		return translator.translate(g.i18nKey());
	}
	
	public static String formatAcademicalTableDate(Date date, AcademicalDateFormat[] formats, Locale locale) {
		if(date == null) return "";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String year;
		if(cal.get(Calendar.SECOND) == ACADEMICAL_SECOND_MARKER
				&& AcademicalDateFormat.hasFormat(AcademicalDateFormat.monthYear, formats)) {
			if(Locale.GERMAN.getLanguage().equals(locale.getLanguage())) {
				synchronized(formatAcademicalTableDateDE) {
					year = formatAcademicalTableDateDE.format(date);
				}
			} else {
				synchronized(formatAcademicalTableDate) {
					year = formatAcademicalTableDate.format(date);
				}
			}
		} else {
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		return year;
	}
	
	public static Date formatAcademicalDate(String value, AcademicalDateFormat[] formats) {
		Date date = null;
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				if(value.indexOf('.') > 0 && AcademicalDateFormat.hasFormat(AcademicalDateFormat.monthYear, formats)) {
					date = new SimpleDateFormat("MM.yyyy").parse(value);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.set(Calendar.SECOND, ACADEMICAL_SECOND_MARKER);
					date = cal.getTime();
				} else if(formats == null || formats.length == 0 || AcademicalDateFormat.hasFormat(AcademicalDateFormat.year, formats)) {
					date = new SimpleDateFormat("yyyy").parse(value);
				}
			} catch (ParseException e) {
				//can happen
			}
		}
		return date;
	}
	
	public static boolean isVisible(ApplicationFieldType... fieldTypes) {
		if(fieldTypes == null || fieldTypes.length == 0 || (fieldTypes.length == 1 && fieldTypes[0] == null)) {
			return false;
		}
		
		boolean visible = false;
		for(ApplicationFieldType fieldType:fieldTypes) {
			visible |= fieldType.isEnabled();
		}
		return visible;
	}
	
	public static String getLabel(Category category) {
		return getLabel(category.getName(), category.getColor(), false);
	}
	
	public static String getLabel(String name, String color, boolean administrative) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("<span class='tag label label-info");
		if(administrative) {
			sb.append(" o_tag_admin");
		}
		sb.append("'");
		if(!StringHelper.containsNonWhitespace(color)) {
			color = "#5bc0de";
		}
		sb.append(" style='background-color: ").append(color).append("'>")
		      .append(name).append("</span> ");
		return sb.toString();
	}
	
	public static String getPositionDerivedFilename(Position position, Locale preferedLocale) {
		String title = PositionMLHelper.getShortMLTitle(position, preferedLocale);
		if(!StringHelper.containsNonWhitespace(title)) {
			title = PositionMLHelper.getPositionMLTitle(position, preferedLocale);
		}
		return title;
	}
	
	public static String mergeOrganizationAndAffiliation(BusinessInformations infos) {
		String organization = infos.getOrganization();
		String affiliation = infos.getAffiliation();
		
		String merged;
		if(StringHelper.containsNonWhitespace(organization) && StringHelper.containsNonWhitespace(affiliation)) {
			if(organization.equalsIgnoreCase(affiliation)) {
				merged = organization;
			} else {
				merged = organization + ", " + affiliation;
			}
		} else if(StringHelper.containsNonWhitespace(organization)) {
			merged = organization;
		} else if(StringHelper.containsNonWhitespace(affiliation)) {
			merged = affiliation;
		} else {
			merged = null;
		}
		return merged;
	}
	
	/**
	 * !!! Without ending
	 * @param filename
	 * @return
	 */
	public static String normalizeFilename(String name) {
		String nameFirstPass = name.replace(" ", "_");
		nameFirstPass = nameFirstPass.replace("\u00C4", "Ae");
		nameFirstPass = nameFirstPass.replace("\u00D6", "Oe");
		nameFirstPass = nameFirstPass.replace("\u00DC", "Ue");
		nameFirstPass = nameFirstPass.replace("\u00E4", "ae");
		nameFirstPass = nameFirstPass.replace("\u00F6", "oe");
		nameFirstPass = nameFirstPass.replace("\u00FC", "ue");
		String nameNormalized = Normalizer.normalize(nameFirstPass, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		return nameNormalized.replaceAll("\\W+", "");
	}
	
	public static String getLinkToPosition(Position position) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Settings.getServerContextPathURI())
		  .append("/position/")
		  .append(position.getKey());
		return sb.toString();
	}
	
	public static String getLinkToPositionDetails(Position position) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Settings.getServerContextPathURI())
		  .append("/positiondetails/")
		  .append(position.getKey());
		return sb.toString();
	}
	
	public static String getLinkToPositionList() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Settings.getServerContextPathURI())
		  .append("/positions/0");
		return sb.toString();
	}
	
	public static String getLinkToReference(Reference reference) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Settings.getServerContextPathURI())
		  .append("/reference/")
		  .append(reference.getSubmissionUrl());
		return sb.toString();
	}
	
	public static String getLinkToRefereeDashboard(ApplicationShort app) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(Settings.getServerContextPathURI())
		  .append("/refereedashboard/")
		  .append(app.getApplicantUrl());
		return sb.toString();
	}
	
	public static String formatFullName(Identity identity) {
		StringBuilder sb = new StringBuilder(32);
		sb.append(identity.getUser().getProperty(UserConstants.FIRSTNAME, null))
		  .append(" ")
		  .append(identity.getUser().getProperty(UserConstants.LASTNAME, null));
		return sb.toString();
	}
	
	public static final String formatLastnameFirstName(Identity identity) {
		String firstName = identity.getUser().getProperty(UserConstants.FIRSTNAME, Locale.ENGLISH);
		String lastName = identity.getUser().getProperty(UserConstants.LASTNAME, Locale.ENGLISH);
		return formatToLastnameFirstname(firstName, lastName);
	}
	
	public static final String formatPersonLastnameFirstname(List<? extends ApplicationShort> applicationsList) {
		StringBuilder sb = new StringBuilder();
		if(applicationsList != null && !applicationsList.isEmpty()) {
			for(ApplicationShort application:applicationsList) {
				if(sb.length() > 0) sb.append(",");
				String val = formatPersonLastnameFirstname(application.getPerson());
				sb.append(val);
			}
		}
		return sb.toString();
	}
	
	public static final String formatPersonLastnameFirstname(PersonName person) {
		String firstName = person.getFirstName();
		String lastName = person.getLastName();
		return formatToLastnameFirstname(firstName, lastName);
	}
	
	private static final String formatToLastnameFirstname(String firstName, String lastName) {
		StringBuilder sb = new StringBuilder(32);
		if(StringHelper.containsNonWhitespace(lastName)) {
			sb.append(lastName);
		}
		if(StringHelper.containsNonWhitespace(firstName)) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(firstName);
		}
		return sb.toString();
	}
	
	public static String formatFullNameWithTitle(Identity identity, Locale locale) {
		StringBuilder sb = new StringBuilder(64);
		String title = identity.getUser().getProperty("title", locale);
		if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
			sb.append(title).append(" ");
		}
		sb.append(identity.getUser().getProperty(UserConstants.FIRSTNAME, locale))
		  .append(" ")
		  .append(identity.getUser().getProperty(UserConstants.LASTNAME, locale));
		return sb.toString();
	}
	
	public static String formatFullName(PublicFeedback feedback, boolean emailFallback) {
		StringBuilder sb = new StringBuilder();
		String firstName = feedback.getFirstName();
		String lastName = feedback.getLastName();
		String email = feedback.getEmail();
		if(StringHelper.containsNonWhitespace(firstName)) {
			sb.append(firstName);
		}
		if(StringHelper.containsNonWhitespace(lastName)) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(lastName);
		}
		
		if(sb.length() == 0 && emailFallback && StringHelper.containsNonWhitespace(email)) {
			sb.append(email);
		}
		return sb.toString();
	}

	public static String formatFullName(Application app, Translator translator) {
		Person person = app.getPerson();
		return formatFullName(person, translator);
	}
	
	public static String formatFullName(ApplicationLight app, Translator translator) {
		Person person = app.getPerson();
		return formatFullName(person, translator);
	}

	public static String formatFullName(Person person, Translator translator) {
		StringBuilder sb = new StringBuilder();
		if(person != null) {
			String title = person.getTitle();
			if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
				title = translator.translate(person.getTitle());
				if(title != null && title.length() < 15) {
					sb.append(title);
				} else {
					sb.append(person.getTitle());
				}
			}
			
			String firstName = person.getFirstName();
			if(StringHelper.containsNonWhitespace(firstName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(firstName);
			}
			
			String lastName = person.getLastName();
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(sb.length() > 0) sb.append(' ');
				sb.append(lastName);
			}
		}
		return sb.toString();
	}
	
	public static String formatZipcodeCity(Address address) {
		if(address == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		String zipcode = address.getZipCode();
		if(StringHelper.containsNonWhitespace(zipcode) ) {
			sb.append(zipcode);
		}
		
		String city = address.getCity();
		if(StringHelper.containsNonWhitespace(city)) {
			if(sb.length() > 0) sb.append(' ');
			sb.append(city);
		}

		return sb.toString();
	}

	
	public static String formatDissertation(Application app, Locale locale) {
		StringBuilder sb = new StringBuilder();
		
		AcademicalBackground background = app.getAcademicalBackground();
		if(background != null) {
			if(StringHelper.containsNonWhitespace(background.getDissertationTitle())) {
				sb.append(background.getDissertationTitle());
			}
			
			if(background.getDissertationDate() != null) {
				if(sb.length() > 0) sb.append(", ");
				
				AcademicalDateFormat[] formats = CoreSpringFactory.getImpl(RecruitingModule.class)
						.getApplicationAcademicalBackgroundDissertationDateFormat();
				sb.append(formatAcademicalTableDate(background.getDissertationDate(), formats, locale));
			}
			
			if(StringHelper.containsNonWhitespace(background.getDissertationInstitution())) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(background.getDissertationInstitution());
			}
		}
		return sb.toString();
	}
	
	public static String formatHabilitation(Application app) {
		StringBuilder sb = new StringBuilder();
		
		AcademicalBackground background = app.getAcademicalBackground();
		if(background != null) {
			if(StringHelper.containsNonWhitespace(background.getHabilitationTitle())) {
				sb.append(background.getHabilitationTitle());
			}
			
			if(background.getHabilitationDate() != null) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(toYear(background.getHabilitationDate()));
			}
			
			if(StringHelper.containsNonWhitespace(background.getHabilitationInstitution())) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(background.getHabilitationInstitution());
			}
		}
		return sb.toString();
	}
	
	public static String formatHighestDegree(Application app, List<String> excludedAttributesList, Translator translator) {
		StringBuilder sb = new StringBuilder();
		
		AcademicalBackground background = app.getAcademicalBackground();
		if(background != null) {
			String type = background.getHighestDegreeType();
			if(StringHelper.containsNonWhitespace(type)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
				if(HighestDegreeType.other.name().equals(type)
						&& StringHelper.containsNonWhitespace(app.getAcademicalBackground().getHighestDegreeDescription())) {
					sb.append(app.getAcademicalBackground().getHighestDegreeDescription());
				} else {
					String translatedText = translator.translate("edit.application.degreetype." + type);
					if(translatedText.length() > 50) {
						sb.append(type);
					} else {
						sb.append(translatedText);
					}
				}
			}
			
			Date date = background.getHighestDegreeDate();
			if(date != null && type != null
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR)) {
				if(CoreSpringFactory.getImpl(RecruitingModule.class).isTableApplicationsHighestDegreeYearOnlyPhDOption()) {
					if(HighestDegreeType.phd.name().equals(type)) {
						if(sb.length() > 0) sb.append(", ");
						sb.append(toYear(date));
					}
				} else {
					if(sb.length() > 0) sb.append(", ");
					sb.append(toYear(date));
				}
			}
			
			String institution = background.getHighestDegreeInstitution();
			if(StringHelper.containsNonWhitespace(institution)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION)) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(institution);
			}
		}

		return sb.toString();
	}
	
	public static String formatIDs(ApplicationShort application, List<? extends ApplicationShort> applicationsList) {
		StringBuilder sb = new StringBuilder();
		if(application != null) {
			sb.append(application.getId().toString());
		}
		if(applicationsList != null && !applicationsList.isEmpty()) {
			for(ApplicationShort app:applicationsList) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(app.getId().toString());	
			}
		}
		return sb.toString();
	}
	
	public static int toYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}
	
	public static boolean validateFieldElement(FormItem textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok;
		if(textEl instanceof IntegerElement) {
			ok = validateIntegerElement((IntegerElement)textEl, mandatory);
		} else if(textEl instanceof TextElement) {
			ok = validateTextElement((TextElement)textEl, length, mandatory, filter);
		} else {
			ok = true;
		}
		return ok;
	}
	

	public static boolean validateSingleSelection(SingleSelection el) {
		boolean ok = true;
		
		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory");
			ok &= false;
		}
		
		return ok;
	}
	
	public static boolean validateDateChooser(DateChooser el) {
		boolean ok = true;
		
		el.clearError();
		if(el.isVisible() && el.isEnabled() && el.isMandatory() && el.getDate() == null) {
			el.setErrorKey("form.legende.mandatory");
			ok &= false;
		}
		
		return ok;
	}
	
	public static boolean validateIntegerElement(IntegerElement textEl, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		if(textEl.isVisible()) {
			try {
				String value = textEl.getValue();
				if(mandatory && !StringHelper.containsNonWhitespace(value)) {
					textEl.setErrorKey("form.legende.mandatory");
					ok = false;
				} else if(StringHelper.containsNonWhitespace(value) && !StringHelper.isLong(value)) {
					String lenient = StringHelper.lenientInteger(value);
					if(!StringHelper.isLong(lenient)) {
						textEl.setErrorKey("form.error.nointeger");
					} else {
						textEl.setValue(lenient);
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return ok;
	}
	
	public static boolean validateRichTextElement(RichTextElement textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = true;
		textEl.clearError();
		if(textEl.isVisible()) {
			String value = textEl.getValue(filter);
			String textValue = FilterFactory.getHtmlTagsFilter().filter(value);
			if(mandatory && !StringHelper.containsNonWhitespace(textValue)) {
				textEl.setErrorKey("form.legende.mandatory");
				ok = false;
			} else if (value.length() > length || value.getBytes().length > length
					|| value.getBytes(StandardCharsets.UTF_8).length > length
					|| value.getBytes(StandardCharsets.ISO_8859_1).length > length) {
				textEl.setErrorKey("input.toolong", new String[]{ Integer.toString(length) });
				ok = false;
			} else if (filter.errors(value)) {
				textEl.setErrorKey("form.general.error", new String[]{ Integer.toString(length) });
				ok = false;
			}
		}
		return ok;
	}
	
	public static boolean validateTextElement(TextElement textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = true;
		textEl.clearError();
		if(textEl.isVisible()) {
			String value = textEl.getValue(filter);
			if(mandatory && !StringHelper.containsNonWhitespace(value)) {
				textEl.setErrorKey("form.legende.mandatory");
				ok = false;
			} else if (value.length() > length || value.getBytes().length > length
					|| value.getBytes(StandardCharsets.UTF_8).length > length
					|| value.getBytes(StandardCharsets.ISO_8859_1).length > length) {
				textEl.setErrorKey("input.toolong", Integer.toString(length));
				ok = false;
			} else if (filter.errors(value)) {
				textEl.setErrorKey("form.general.error", Integer.toString(length));
				ok = false;
			}
		}
		return ok;
	}
	
	public static boolean validateEmailElement(TextElement emailEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = true;
		emailEl.clearError();
		if(emailEl.isVisible()) {
			String value = emailEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				if(mandatory) {
					emailEl.setErrorKey("form.legende.mandatory");
					ok &= false;
				}
			} else if (value.length() > length) {
				emailEl.setErrorKey("input.toolong", Integer.toString(length));
				ok &= false;
			} else if(filter.errors(value)) {
				emailEl.setErrorKey("form.general.error");
				ok &= false;
			} else if(!MailHelper.isValidEmailAddress(value)) {
				emailEl.setErrorKey("email.error.valid");
				ok &= false;
			}
		}
		return ok;
	}
	
	public static boolean validateIntegerElement(TextElement textEl, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		
		String value = textEl.getValue();
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			textEl.setErrorKey("form.legende.mandatory");
			ok = false;
		} else if(StringHelper.containsNonWhitespace(value)) {
			try {
				Integer.parseInt(value);
			} catch(NumberFormatException e) {
				textEl.setErrorKey("integer.element.int.error");
				ok = false;
			}
		}

		return ok;
	}
	
	public static boolean validateIntegerElement(TextElement textEl, int min, int max, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		
		String value = textEl.getValue();
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			textEl.setErrorKey("form.legende.mandatory");
			ok = false;
		} else if(StringHelper.containsNonWhitespace(value)) {
			try {
				int val = Integer.parseInt(value);
				if(val < min || val > max) {
					textEl.setErrorKey("error.integer.between", Integer.toString(min), Integer.toString(max));
					ok = false;
				}
			} catch(NumberFormatException e) {
				textEl.setErrorKey("integer.element.int.error");
				ok = false;
			}
		}

		return ok;
	}
	
	public static List<String> splitEmails(String text) {
		String[] emails = text.split("[;,]");
		List<String> emailList = new ArrayList<>();
		for(String email:emails) {
			if(StringHelper.containsNonWhitespace(email)) {
				emailList.add(email);
			}
		}
		return emailList;
	}
	
	public static String letterName(String name, ApplicationShort app) {
		StringBuilder sb = new StringBuilder(64);
		if(app != null && app.getPerson() != null) {
			if(StringHelper.containsNonWhitespace(app.getPerson().getLastName())) {
				sb.append(app.getPerson().getLastName());
			}
			
			if(StringHelper.containsNonWhitespace(app.getPerson().getFirstName())) {
				if(sb.length() > 0) {
					sb.append("_");
				}
				sb.append(app.getPerson().getFirstName());
			}
		}
		if(sb.length() > 0) {
			sb.append("_");
		}
		sb.append(name);
		String letterName = sb.toString();
		if(!letterName.endsWith(".pdf")) {
			letterName += ".pdf";
		}
		return letterName;
	}
	
	public static boolean isSendRefereeNotificationToApplicant(UserRequest ureq, Application application, Position position) {
		return application.getIdentity() != null && position.isApplicantRefereeManagementEnabled()
				&& (position.getApplicantRefereeManagementDeadline() == null
					|| RecruitingHelper.endOfDay(position.getApplicantRefereeManagementDeadline()).after(ureq.getRequestTimestamp()));
	}
}
