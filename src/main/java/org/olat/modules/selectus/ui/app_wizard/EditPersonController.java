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
package org.olat.modules.selectus.ui.app_wizard;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.BusinessAddress;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.PersonGender;
import org.olat.modules.selectus.model.PersonImpl;
import org.olat.modules.selectus.model.PersonMaritalStatus;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.components.ReflectionStaticElement;
import org.olat.modules.selectus.ui.components.SelectusUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditPersonController extends FormBasicController {

	private SingleSelection titleEl;
	private SingleSelection genderEl;
	private SingleSelection maritalStatusEl;
	private TextElement firstNameEl;
	private TextElement lastNameEl;
	private TextElement birthDayEl;
	private TextElement academicTitleEl;
	private SingleSelection birthMonthEl;
	private TextElement birthYearEl;
	private TextElement nationalityEl;
	private SingleSelection nationalitySelectionEl;
	private TextElement additionalNationalitiesEl;
	private MultipleSelectionElement additionalNationalitieSelectionEl;
	private TextElement mailEl;
	private TextElement businessMailEl;
	private TextElement phoneEl;
	private TextElement businessPhoneEl;
	private TextElement mobilePhoneEl;
	private MultipleSelectionElement disabilityEl;
	private SingleSelection addressTypeEl;
	private TextElement organizationEl;
	private SingleSelection organizationDropdownEl;
	private TextElement unitEl;
	private TextElement currentPositionEl;
	private ReflectionStaticElement organizationCopyEl;
	private ReflectionStaticElement unitCopyEl;
	private TextElement addressLine1El;
	private TextElement businessAddressLine1El;
	private TextElement addressLine2El;
	private TextElement businessAddressLine2El;
	private TextElement addressLine3El;
	private TextElement businessAddressLine3El;
	private TextElement zipcodeEl;
	private TextElement businessZipcodeEl;
	private TextElement cityEl;
	private TextElement businessCityEl;
	private SingleSelection countryEl;
	private SingleSelection businessCountryEl;
	
	private FormLayoutContainer birthdayContainer;
	private List<FormItem> additionalAttributesEl = new ArrayList<>();
	private final ApplicationAttributesDelegate attributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	
	private final boolean admin;
	private Position position;
	private Application application;
	
	private final String[] titleKeys;
	private final String[] titleValues;
	private final String[] genderKeys;
	private final String[] genderValues;
	private static final String genderChooseKey = "choose";
	private final SelectionValues maritalStatusKV;
	private final String[] monthKeys;
	private final String[] monthValues;
	private final String[] addressTypeKeys = {Address.Type.BUSINESS.getType(), Address.Type.PRIVATE.getType()};
	private final String[] addressTypeValues = new String[2];
	private static final String[] disabilityKeys = new String[] { "xx" };
	private static final String[] disabilityValues = new String[] { "" };
	
	private final boolean editable;
	private final boolean segmented;
	private final AddressOption privateOption;
	private final AddressOption businessOption;
	private final TabConfiguration tabConfiguration;
	private final List<String> excludedAttributes;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public EditPersonController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application,
			TabConfiguration tabConfiguration, List<String> excludedAttributes,
			boolean admin, boolean segmented, boolean editable) {
		super(ureq, wControl, null, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		this.segmented = segmented;
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		
		this.admin = admin;
		this.editable = editable;
		this.application = application;
		this.position = application.getPosition();
		this.tabConfiguration = tabConfiguration == null
				? application.getPosition().getTabConfiguration(Tab.personalData) : tabConfiguration;
		this.excludedAttributes = excludedAttributes == null ? List.of() : excludedAttributes;
		
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		
		PersonTitle[] personTitles = recruitingModule.getApplicantPersonTitles();
		titleKeys = new String[personTitles.length + 2];
		titleValues = new String[personTitles.length + 2];
		titleKeys[0] = "not1";
		titleValues[0] = translate("edit.application.title.choose");
		titleKeys[1] = "not2";
		titleValues[1] = translate("edit.application.title.none");
		for(int i=personTitles.length; i-->0; ) {
			titleKeys[i+2] = personTitles[i].title();
			titleValues[i+2] = translate(personTitles[i].i18nKey());
		}
		
		if(admin) {
			monthKeys = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "-"};
			monthValues = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "-"};
			for(int i=0; i<12; i++) {
				monthValues[i] = translate("month.long." + i);
			}
		} else {
			monthKeys = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
			monthValues = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
			for(int i=monthKeys.length; i-->0; ) {
				monthValues[i] = translate("month.long." + i);
			}
		}

		PersonGender[] personGenders = recruitingModule.getPersonGenders();
		if(admin || recruitingModule.isApplicationPersonGenderOptional()) {
			genderKeys = new String[personGenders.length + 1];
			genderValues = new String[personGenders.length + 1];
			genderKeys[0] = "-";
			genderValues[0] = admin && !recruitingModule.isApplicationPersonGenderOptional()
					? translate("edit.application.gender.choose") : "-";
			for(int i=personGenders.length; i-->0; ) {
				genderKeys[i+1] = personGenders[i].gender();
				genderValues[i+1] = translate(personGenders[i].i18nKey());
			}	
		} else {
			genderKeys = new String[personGenders.length + 1];
			genderValues = new String[personGenders.length + 1];
			genderKeys[0] = genderChooseKey;
			genderValues[0] = translate("edit.application.gender.choose");
			for(int i=personGenders.length; i-->0; ) {
				genderKeys[i+1] = personGenders[i].gender();
				genderValues[i+1] = translate(personGenders[i].i18nKey());
			}
		}
		
		maritalStatusKV = new SelectionValues();
		maritalStatusKV.add(SelectionValues.entry("-", translate("please.choose")));
		PersonMaritalStatus[] maritalStatusList = recruitingModule.getMaritalStatusList();
		for(PersonMaritalStatus maritalStatus:maritalStatusList) {
			maritalStatusKV.add(SelectionValues.entry(maritalStatus.name(), translate("edit.application.marital.status." + maritalStatus.name())));
		}

		addressTypeValues[0] = translate("edit.application.addressType.business");
		addressTypeValues[1] = translate("edit.application.addressType.private");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!segmented) {
			setFormTitle("wizard.edit_person.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) } );
		}
		String explanation = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(explanation)) {
			setFormTranslatedDescription(StringHelper.xssScan(RecruitingHelper.escWithBR(explanation)));
		} else {
			setFormDescription("wizard.edit_person.explanation");
		}

		formLayout.setElementCssClass("o_sel_edit_person");
		initPersonForm(formLayout);
		initBusinessInformationsForm(formLayout);

		if(AddressOption.xor.equals(privateOption) && AddressOption.xor.equals(businessOption)) {
			initAddressForm(formLayout);
		} else {
			if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
				initBusinessAddressForm(formLayout);
			}
			if(AddressOption.enabled.equals(privateOption) || AddressOption.optional.equals(privateOption)) {
				initPrivateAddressForm(formLayout);
			}
		}
		
		initCustomAttributes(formLayout);
	}
		
	private void initPersonForm(FormItemContainer formLayout) {
		Person person = application.getPerson();
		
		String heading = StringHelper.escapeHtml(tabConfiguration.getHeading(getLocale()));
		if(StringHelper.containsNonWhitespace(heading)) {
			StaticTextElement headingEl = uifactory.addStaticTextElement("personal-data", "wizard.edit_person.title", "", formLayout);
			headingEl.setElementCssClass("o_static_heading");
			headingEl.setLabel(heading, null, false);
		}

		titleEl = uifactory.addDropdownSingleselect("title", "edit.application.title", formLayout, titleKeys, titleValues, null);
		titleEl.setMandatory(true);
		titleEl.setElementCssClass("o_sel_edit_person_title");
		titleEl.setEnabled(editable);
		titleEl.setVisible(recruitingModule.isApplicationPersonTitleEnabled());
		String title = person == null ? null : person.getTitle();
		if(StringHelper.containsNonWhitespace(title)) {
			for(String key: titleKeys) {
				if(key.equals(title)) {
					titleEl.select(key , true);
				}
			}
			for(int i=0; i<titleValues.length; i++) {
				String value = titleValues[i];
				if(value.equals(title)) {
					titleEl.select(titleKeys[i], true);
				}
			}
		} else if(admin) {
			for(String titleKey:titleKeys) {
				if("not2".equals(titleKey)) {
					titleEl.select(titleKey, true);
				}
			}
		}
		
		genderEl = uifactory.addDropdownSingleselect("gender", "edit.application.gender", formLayout, genderKeys, genderValues, null);
		genderEl.setElementCssClass("o_sel_edit_person_gender");
		genderEl.setMandatory(!admin && !recruitingModule.isApplicationPersonGenderOptional());
		genderEl.setAllowNoSelection(admin || recruitingModule.isApplicationPersonGenderOptional());
		genderEl.setVisible(recruitingModule.isApplicationPersonGenderEnabled());
		genderEl.setEnabled(editable);
		String gender = person == null ? null : person.getGender();
		if(gender != null && StringHelper.containsNonWhitespace(gender)) {
			for(int i=genderKeys.length; i-->0;) {
				if(gender.equals(genderKeys[i])) {
					genderEl.select(genderKeys[i], true);
					break;
				}
			}
		} else {
			PersonGender defaultGender = recruitingModule.getPersonDefaultGender();
			boolean selected = false;
			if(defaultGender != null) {
				for(int i=genderKeys.length; i-->0;) {
					String genderKey = genderKeys[i];
					if(defaultGender.gender().equals(genderKey)) {
						genderEl.select(genderKeys[i], true);
						selected = true;
						break;
					}
				}
			}
			
			if(!selected) {
				if(application != null
						&& (application.isValid() && application.getKey() != null)
						&& (genderKeys.length > 1 && "-".equals(genderKeys[1]))) {
					genderEl.select(genderKeys[1], true);
				} else {
					genderEl.select(genderKeys[0], true);
				}
			}
		}

		String firstName = person == null ? null : person.getFirstName();
		firstNameEl = uifactory.addTextElement("firstName", "edit.application.firstName", 255, firstName, formLayout);
		firstNameEl.setMandatory(true);
		firstNameEl.setElementCssClass("o_sel_edit_person_firstname");
		firstNameEl.setEnabled(editable);
		
		String lastName = person == null ? null : person.getLastName();
		lastNameEl = uifactory.addTextElement("lastName", "edit.application.lastName", 255, lastName, formLayout);
		lastNameEl.setMandatory(true);
		lastNameEl.setElementCssClass("o_sel_edit_person_lastname");
		lastNameEl.setEnabled(editable);
		
		maritalStatusEl = uifactory.addDropdownSingleselect("maritalstatus", "edit.application.marital.status", formLayout, maritalStatusKV.keys(), maritalStatusKV.values(), null);
		maritalStatusEl.setMandatory(true);
		maritalStatusEl.setElementCssClass("o_sel_edit_person_marital_status");
		maritalStatusEl.setVisible(recruitingModule.isApplicationPersonMaritalStatusEnabled());
		maritalStatusEl.setEnabled(editable);
		String maritalStatus = person == null ? null : person.getMaritalStatus();
		if(maritalStatus != null && maritalStatusKV.containsKey(maritalStatus)) {
			maritalStatusEl.select(maritalStatus, true);
		} else {
			maritalStatusEl.select(maritalStatusKV.keys()[0], true);
		}
		
		//birthday container
		String page = velocity_root + "/edit_birthday.html";
		birthdayContainer = FormLayoutContainer.createCustomFormLayout("birthday_cont", getTranslator(), page);
		birthdayContainer.setRootForm(mainForm);
		birthdayContainer.setLabel("edit.application.birthday", null);
		birthdayContainer.setElementCssClass("o_sel_edit_person_birthday");
		birthdayContainer.setMandatory(!admin && !recruitingModule.isApplicationPersonBirthdayOptional());
		birthdayContainer.setVisible(recruitingModule.isApplicationPersonBirthdayEnabled());
		formLayout.add(birthdayContainer);

		String day = "";
		String month= "0";
		String year = "";
		Date birthday = person == null ? null : person.getBirthday();
		if(birthday != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(birthday);
			day = Integer.toString(cal.get(Calendar.DATE));
			month = Integer.toString(cal.get(Calendar.MONTH));
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		birthDayEl = uifactory.addTextElement("birthday.day", "", 2, day, birthdayContainer);
		birthDayEl.setDomReplacementWrapperRequired(false);
		birthDayEl.setDisplaySize(2);
		birthDayEl.setMandatory(!admin);
		birthDayEl.setEnabled(editable);
		
		birthMonthEl = uifactory.addDropdownSingleselect("birthday.month", "", birthdayContainer, monthKeys, monthValues, null);
		birthMonthEl.setDomReplacementWrapperRequired(false);
		birthMonthEl.setMandatory(true);
		birthMonthEl.setEnabled(editable);
		if(admin && birthday == null) {
			birthMonthEl.select("-", true);
		} else {
			for(String monthKey:monthKeys) {
				if(monthKey.equals(month)) {
					birthMonthEl.select(monthKey, true);
				}
			}
		}
		
		birthYearEl = uifactory.addTextElement("birthday.year", "", 4, year, birthdayContainer);
		birthYearEl.setDomReplacementWrapperRequired(false);
		birthYearEl.setDisplaySize(4);
		birthYearEl.setMandatory(!admin);
		birthYearEl.setEnabled(editable);
		
		initNationalitiesForm(person, formLayout);
		
		String academicTitle = person == null ? null : person.getAcademicTitle();
		academicTitleEl = uifactory.addTextElement("academictitle", "edit.application.academicTitle", 255, academicTitle, formLayout);
		academicTitleEl.setMandatory(!admin && !recruitingModule.isApplicationPersonAcademicTitleOptional());
		academicTitleEl.setVisible(recruitingModule.isApplicationPersonAcademicTitleEnabled());
		academicTitleEl.setEnabled(editable);
	
		uifactory.addSpacerElement("first_spacer", formLayout, false);
		
		Boolean disability = person == null ? null : person.getDisability();
		disabilityEl = uifactory.addCheckboxesHorizontal("disability", "edit.application.disability", formLayout, disabilityKeys, disabilityValues);
		disabilityEl.setElementCssClass("o_sel_edit_person_disability");
		disabilityEl.setVisible(recruitingModule.isApplicationPersonDisabilityEnabled());
		disabilityEl.setEnabled(editable);
		if(disability != null && disability.booleanValue()) {
			disabilityEl.select(disabilityKeys[0], true);
		}
		if(disabilityEl.isVisible()) {
			uifactory.addSpacerElement("first_extra_spacer", formLayout, false);
		}
		
		StaticTextElement headingEl = uifactory.addStaticTextElement("contact-data", "contact.data", "", formLayout);
		headingEl.setElementCssClass("o_static_heading o_sel_edit_contact_data");
		
		String phone = person == null ? null : person.getPhone();
		phoneEl = uifactory.addTextElement("phone", "edit.application.phone", 255, phone, formLayout);
		phoneEl.setElementCssClass("o_sel_edit_person_phone");
		phoneEl.setPlaceholderText("+41 12 345 67 89");
		phoneEl.setMandatory(!recruitingModule.isApplicationPersonPhoneOptional() && !admin);
		phoneEl.setVisible(recruitingModule.isApplicationPersonPhoneEnabled());
		phoneEl.setEnabled(editable);
		
		String mobilePhone = person == null ? null : person.getMobilePhone();
		mobilePhoneEl = uifactory.addTextElement("mobilephone", "edit.application.mobile.phone", 255, mobilePhone, formLayout);
		mobilePhoneEl.setElementCssClass("o_sel_edit_person_mobile_phone");
		mobilePhoneEl.setPlaceholderText("+41 79 211 23 89");
		mobilePhoneEl.setMandatory(!recruitingModule.isApplicationPersonMobilePhoneOptional() && !admin);
		mobilePhoneEl.setVisible(recruitingModule.isApplicationPersonMobilePhoneEnabled());
		mobilePhoneEl.setEnabled(editable);

		String mail = person == null ? null : person.getMail();
		mailEl = uifactory.addTextElement("mail", "edit.application.mail", 255, mail, formLayout);
		mailEl.setElementCssClass("o_sel_edit_person_email");
		mailEl.setMandatory(true);
		mailEl.setEnabled(editable);
		
		SpacerElement spacer = uifactory.addSpacerElement("secund_spacer", formLayout, false);
		spacer.setElementCssClass("o_sel_spacer_person_secund");
	}
	
	private void initNationalitiesForm(Person person, FormItemContainer formLayout) {
		String nationality = person == null ? null : person.getNationality();
		if(recruitingModule.isApplicationPersonNationalityEnabled()) {
			if(recruitingModule.isApplicationPersonNationalityUseCountry()) {
				CountryKeysValues nationKeysValues = generateCountryKeysAndValues(nationality);
				nationalitySelectionEl = uifactory.addDropdownSingleselect("nationality", "edit.application.nationality", formLayout,
						nationKeysValues.getKeys(), nationKeysValues.getValues(), null);
				nationalitySelectionEl.setMandatory(!admin && !recruitingModule.isApplicationPersonNationalityOptional());
				nationalitySelectionEl.setElementCssClass("o_sel_edit_person_nationality");
				nationalitySelectionEl.setEnabled(editable);
				securelySelectCountry(nationalitySelectionEl, nationality, nationKeysValues);
			} else {
				nationalityEl = uifactory.addTextElement("nationality", "edit.application.nationality", 255, nationality, formLayout);
				nationalityEl.setMandatory(!admin && !recruitingModule.isApplicationPersonNationalityOptional());
				nationalityEl.setElementCssClass("o_sel_edit_person_nationality");
				nationalityEl.setEnabled(editable);
			}
		}

		String addNationalities = person == null ? null : person.getAdditionalNationalities();
		if(recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled()) {
			if(recruitingModule.isApplicationPersonAdditionalNationalitiesUseCountry()) {
				CountryKeysValues nationKeysValues = generateAdditionalCountryKeysAndValues(addNationalities);
				additionalNationalitieSelectionEl = uifactory.addCheckboxesDropdown("add.nationalities", "edit.application.additional.nationalities", formLayout,
						nationKeysValues.getKeys(), nationKeysValues.getValues());
				additionalNationalitieSelectionEl.setMandatory(!admin && !recruitingModule.isApplicationPersonAdditionalNationalitiesOptional());
				additionalNationalitieSelectionEl.setElementCssClass("o_sel_edit_person_add_nationalities");
				additionalNationalitieSelectionEl.setEnabled(editable);
				securelySelectCountry(additionalNationalitieSelectionEl, addNationalities, nationKeysValues);
			} else {
				additionalNationalitiesEl = uifactory.addTextElement("add.nationalities", "edit.application.additional.nationalities", 255, addNationalities, formLayout);
				additionalNationalitiesEl.setMandatory(!admin && !recruitingModule.isApplicationPersonAdditionalNationalitiesOptional());
				additionalNationalitiesEl.setPlaceholderKey("edit.application.additional.nationalities.placeholder", null);
				additionalNationalitiesEl.setElementCssClass("o_sel_edit_person_add_nationalities");
				additionalNationalitiesEl.setEnabled(editable);
			}
		}
	}
	
	private void initBusinessInformationsForm(FormItemContainer formLayout) {
		StaticTextElement infosTitleEl = uifactory.addStaticTextElement("business-infos", "business.infos", "", formLayout);
		infosTitleEl.setElementCssClass("o_static_heading o_sel_edit_business_infos_title");
		infosTitleEl.setVisible(false);
		
		BusinessInformations businessInfos = application.getBusinessInformations();
		
		String organization = businessInfos == null ? null  : RecruitingHelper.mergeOrganizationAndAffiliation(businessInfos);
		List<String> listOfValues = recruitingModule.getApplicationBusinessInformationsOrganizationListOfValues();
		
		organizationEl = uifactory.addTextElement("address.org", "edit.application.organization", 255, organization, formLayout);
		organizationEl.setElementCssClass("o_sel_edit_person_organization");
		organizationEl.setMandatory(!recruitingModule.isApplicationBusinessInformationsOrganizationOptional() && !admin);
		organizationEl.setVisible(recruitingModule.isApplicationBusinessInformationsOrganizationEnabled()
				&& !excludedAttributes.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)
				&& listOfValues.isEmpty());
		organizationEl.setEnabled(editable);
		
		Translator organisationEnTranslator = Util.createPackageTranslator(RecruitingMainController.class, Locale.ENGLISH);
		Translator organisationDeTranslator = Util.createPackageTranslator(RecruitingMainController.class, Locale.GERMAN);
		
		String selectedKey = null;
		SelectionValues organizationPK = new SelectionValues();
		organizationPK.add(SelectionValues.entry("", translate("please.choose")));
		
		for(String key:listOfValues ) {
			String i18nKey = "edit.application.organization." + key;
			organizationPK.add(SelectionValues.entry(key, translate(i18nKey)));
			
			String translatedDe = organisationDeTranslator.translate(i18nKey);
			String translatedEn = organisationEnTranslator.translate(i18nKey);
			if(organization != null && (organization.equals(key)
					|| organization.equals(translatedDe) || organization.equals(translatedEn))) {
				selectedKey = key;
			}
		}
		if(StringHelper.containsNonWhitespace(organization) && selectedKey == null) {
			organizationPK.add(SelectionValues.entry(organization, organization));
			selectedKey = organization;
		}
		
		organizationDropdownEl = uifactory.addDropdownSingleselect("address.org.list", "edit.application.organization", formLayout,
				organizationPK.keys(), organizationPK.values());
		organizationDropdownEl.setElementCssClass("o_sel_edit_person_organization");
		organizationDropdownEl.setMandatory(!recruitingModule.isApplicationBusinessInformationsOrganizationOptional() && !admin);
		organizationDropdownEl.setVisible(recruitingModule.isApplicationBusinessInformationsOrganizationEnabled()
				&& !excludedAttributes.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)
				&& !listOfValues.isEmpty());
		organizationDropdownEl.setEnabled(editable);
		if(selectedKey != null && organizationPK.containsKey(selectedKey)) {
			organizationDropdownEl.select(selectedKey, true);
		} else {
			organizationDropdownEl.select("", true);
		}
	
		String unit = businessInfos == null ? null  : businessInfos.getUnit();
		unitEl = uifactory.addTextElement("address.unit", "edit.application.unit", 255, unit, formLayout);
		unitEl.setElementCssClass("o_sel_edit_person_unit");
		unitEl.setMandatory(!recruitingModule.isApplicationBusinessInformationsUnitOptional() && !admin);
		unitEl.setVisible(recruitingModule.isApplicationBusinessInformationsUnitEnable()
				&& !excludedAttributes.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT));
		unitEl.setEnabled(editable);
		
		String currentPosition = businessInfos == null ? null : businessInfos.getCurrentPosition();
		currentPositionEl = uifactory.addTextElement("cp", "edit.application.currentPosition", 255, currentPosition, formLayout);
		currentPositionEl.setElementCssClass("o_sel_edit_person_current_position");
		currentPositionEl.setExampleKey("edit.application.currentPosition.example", null);
		currentPositionEl.setMandatory(!recruitingModule.isApplicationBusinessInformationsCurrentPositionOptional() && !admin);
		currentPositionEl.setVisible(recruitingModule.isApplicationBusinessInformationsCurrentPositionEnabled()
				&& !excludedAttributes.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION));
		currentPositionEl.setEnabled(editable);
		
		if(organizationEl.isVisible() || unitEl.isVisible() || currentPositionEl.isVisible()) {
			SpacerElement spacer = uifactory.addSpacerElement("secund_spacer", formLayout, false);
			spacer.setElementCssClass("o_sel_spacer_person_secund");
			infosTitleEl.setVisible(true);
		}
	}
	
	private void initAddressForm(FormItemContainer formLayout) {
		StaticTextElement headingEl = uifactory.addStaticTextElement("correspendence-add", "correspondence.address", "", formLayout);
		headingEl.setElementCssClass("o_static_heading");
		
		Address address = application.getAddress();
		
		Address.Type addrType = address == null ? null : address.getType();
		addrType = addrType == null ? Address.Type.BUSINESS : addrType;
		addressTypeEl = uifactory.addRadiosHorizontal("address.type", "edit.application.addressType", formLayout, addressTypeKeys, addressTypeValues);
		addressTypeEl.setElementCssClass("o_sel_edit_person_address_type");
		addressTypeEl.select(addrType.getType(), true);
		addressTypeEl.setEnabled(editable);
		addressTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		if(organizationEl.isVisible() || unitEl.isVisible()) {
			boolean copyVisible = addrType == Address.Type.BUSINESS;
			organizationCopyEl = SelectusUIFactory.addReflectionStaticText("address.org.copy", "edit.application.organization", organizationEl, formLayout);
			organizationCopyEl.setVisible(copyVisible && organizationEl.isVisible());
			unitCopyEl = SelectusUIFactory.addReflectionStaticText("address.unit.copy", "edit.application.unit", unitEl, formLayout);
			unitCopyEl.setVisible(copyVisible && unitEl.isVisible());
		}
		
		String addressLine1 = address == null ? null  : address.getAddressLine1();
		addressLine1El = uifactory.addTextElement("addressLine1", "edit.application.addressLine1", 255, addressLine1, formLayout);
		addressLine1El.setElementCssClass("o_sel_edit_address_line_1");
		addressLine1El.setMandatory(!admin);
		addressLine1El.setEnabled(editable);
		
		String addressLine2 = address == null ? null  : address.getAddressLine2();
		addressLine2El = uifactory.addTextElement("addressLine2", "edit.application.addressLine2", 255, addressLine2, formLayout);
		addressLine2El.setElementCssClass("o_sel_edit_address_line_2");
		addressLine2El.setEnabled(editable);
		
		String addressLine3 = address == null ? null  : address.getAddressLine3();
		addressLine3El = uifactory.addTextElement("addressLine3", "edit.application.addressLine3", 255, addressLine3, formLayout);
		addressLine3El.setElementCssClass("o_sel_edit_address_line_3");
		addressLine3El.setEnabled(editable);
		addressLine3El.setVisible(recruitingModule.isApplicationAddressLine3Enabled());
		
		String zipcode = address == null ? null  : address.getZipCode();
		zipcodeEl = uifactory.addTextElement("zipcode", "edit.application.zipcode", 255, zipcode, formLayout);
		zipcodeEl.setElementCssClass("o_sel_edit_address_zipcode");
		zipcodeEl.setMandatory(!admin);
		zipcodeEl.setEnabled(editable);
		
		String city = address == null ? null  : address.getCity();
		cityEl = uifactory.addTextElement("city", "edit.application.city", 255, city, formLayout);
		cityEl.setElementCssClass("o_sel_edit_address_city");
		cityEl.setMandatory(!admin);
		cityEl.setEnabled(editable);
		
		String country = address == null ? null  : address.getCountry();
		CountryKeysValues countryKeysValues = generateCountryKeysAndValues(country);
		countryEl = uifactory.addDropdownSingleselect("country", "edit.application.country", formLayout,
				countryKeysValues.getKeys(), countryKeysValues.getValues(), null);
		countryEl.setElementCssClass("o_sel_edit_address_country");
		countryEl.setMandatory(true);//always
		countryEl.setEnabled(editable);
		countryEl.setVisible(recruitingModule.isApplicationAddressCountryEnabled());
		securelySelectCountry(countryEl, country, countryKeysValues);
	}
	
	private void initPrivateAddressForm(FormItemContainer formLayout) {
		StaticTextElement headingEl = uifactory.addStaticTextElement("address-private", "address.private", "", formLayout);
		headingEl.setElementCssClass("o_static_heading");
		
		boolean optional = AddressOption.optional.equals(privateOption);
		
		Address address = application.getAddress();

		String addressLine1 = address == null ? null  : address.getAddressLine1();
		addressLine1El = uifactory.addTextElement("addressLine1", "edit.application.addressLine1", 255, addressLine1, formLayout);
		addressLine1El.setElementCssClass("o_sel_edit_private_address_line1");
		addressLine1El.setMandatory(!admin && !optional);
		addressLine1El.setEnabled(editable);
		
		String addressLine2 = address == null ? null  : address.getAddressLine2();
		addressLine2El = uifactory.addTextElement("addressLine2", "edit.application.addressLine2", 255, addressLine2, formLayout);
		addressLine2El.setElementCssClass("o_sel_edit_private_address_line2");
		addressLine2El.setEnabled(editable);
		
		String addressLine3 = address == null ? null  : address.getAddressLine3();
		addressLine3El = uifactory.addTextElement("addressLine3", "edit.application.addressLine3", 255, addressLine3, formLayout);
		addressLine3El.setElementCssClass("o_sel_edit_private_address_line3");
		addressLine3El.setEnabled(editable);
		addressLine3El.setVisible(recruitingModule.isApplicationAddressLine3Enabled());
		
		String zipcode = address == null ? null  : address.getZipCode();
		zipcodeEl = uifactory.addTextElement("zipcode", "edit.application.zipcode", 255, zipcode, formLayout);
		zipcodeEl.setElementCssClass("o_sel_edit_private_address_zipcode");
		zipcodeEl.setMandatory(!admin && !optional);
		zipcodeEl.setEnabled(editable);
		
		String city = address == null ? null  : address.getCity();
		cityEl = uifactory.addTextElement("city", "edit.application.city", 255, city, formLayout);
		cityEl.setElementCssClass("o_sel_edit_private_address_city");
		cityEl.setMandatory(!admin && !optional);
		cityEl.setEnabled(editable);
		
		String country = address == null ? null  : address.getCountry();
		CountryKeysValues countryKeysValues = generateCountryKeysAndValues(country);
		countryEl = uifactory.addDropdownSingleselect("country", "edit.application.country", formLayout,
				countryKeysValues.getKeys(), countryKeysValues.getValues(), null);
		countryEl.setElementCssClass("o_sel_edit_private_address_country");
		countryEl.setMandatory(!admin && !optional && !recruitingModule.isApplicationAddressCountryOptional());
		countryEl.setEnabled(editable);
		countryEl.setVisible(recruitingModule.isApplicationAddressCountryEnabled());
		securelySelectCountry(countryEl, country, countryKeysValues);
	}
	
	private void initBusinessAddressForm(FormItemContainer formLayout) {
		StaticTextElement headingEl = uifactory.addStaticTextElement("address-business", "address.business", "", formLayout);
		headingEl.setElementCssClass("o_static_heading");
		
		boolean optional = AddressOption.optional.equals(businessOption);
		
		BusinessAddress address = application.getBusinessAddress();
		
		if(organizationEl.isVisible() || unitEl.isVisible()) {
			organizationCopyEl = SelectusUIFactory.addReflectionStaticText("address.org.copy", "edit.application.organization", organizationEl, formLayout);
			organizationCopyEl.setVisible(organizationEl.isVisible());
			unitCopyEl = SelectusUIFactory.addReflectionStaticText("address.unit.copy", "edit.application.unit", unitEl, formLayout);
			unitCopyEl.setVisible(unitEl.isVisible());
		}

		String addressLine1 = address == null ? null  : address.getAddressLine1();
		businessAddressLine1El = uifactory.addTextElement("biz.addressLine1", "edit.application.addressLine1", 255, addressLine1, formLayout);
		businessAddressLine1El.setElementCssClass("o_sel_edit_business_address_line1");
		businessAddressLine1El.setMandatory(!admin && !optional);
		businessAddressLine1El.setEnabled(editable);
		
		String addressLine2 = address == null ? null  : address.getAddressLine2();
		businessAddressLine2El = uifactory.addTextElement("biz.addressLine2", "edit.application.addressLine2", 255, addressLine2, formLayout);
		businessAddressLine2El.setElementCssClass("o_sel_edit_business_address_line2");
		businessAddressLine2El.setEnabled(editable);
		
		String addressLine3 = address == null ? null  : address.getAddressLine3();
		businessAddressLine3El = uifactory.addTextElement("biz.addressLine3", "edit.application.addressLine3", 255, addressLine3, formLayout);
		businessAddressLine3El.setElementCssClass("o_sel_edit_business_address_line3");
		businessAddressLine3El.setEnabled(editable);
		businessAddressLine3El.setVisible(recruitingModule.isApplicationAddressLine3Enabled());
		
		String zipcode = address == null ? null  : address.getZipCode();
		businessZipcodeEl = uifactory.addTextElement("biz.zipcode", "edit.application.zipcode", 255, zipcode, formLayout);
		businessZipcodeEl.setElementCssClass("o_sel_edit_business_address_zipcode");
		businessZipcodeEl.setMandatory(!admin && !optional);
		businessZipcodeEl.setEnabled(editable);
		
		String city = address == null ? null  : address.getCity();
		businessCityEl = uifactory.addTextElement("biz.city", "edit.application.city", 255, city, formLayout);
		businessCityEl.setElementCssClass("o_sel_edit_business_address_city");
		businessCityEl.setMandatory(!admin && !optional);
		businessCityEl.setEnabled(editable);
		
		String country = address == null ? null  : address.getCountry();
		CountryKeysValues countryKeysValues = generateCountryKeysAndValues(country);
		businessCountryEl = uifactory.addDropdownSingleselect("biz.country", "edit.application.country", formLayout,
				countryKeysValues.getKeys(), countryKeysValues.getValues(), null);
		businessCountryEl.setElementCssClass("o_sel_edit_business_address_country");
		businessCountryEl.setMandatory(!admin && !optional && !recruitingModule.isApplicationAddressCountryOptional());
		businessCountryEl.setEnabled(editable);
		businessCountryEl.setVisible(recruitingModule.isApplicationAddressCountryEnabled());
		securelySelectCountry(businessCountryEl, country, countryKeysValues);
		
		String phone = address == null ? null  : address.getPhone();
		businessPhoneEl = uifactory.addTextElement("biz.phone", "edit.application.business.phone", 255, phone, formLayout);
		businessPhoneEl.setElementCssClass("o_sel_edit_business_phone");
		businessPhoneEl.setMandatory(!recruitingModule.isApplicationBusinessPhoneOptional() && !admin);
		businessPhoneEl.setVisible(recruitingModule.isApplicationBusinessPhoneEnabled());
		businessPhoneEl.setEnabled(editable);
		
		String mail = address == null ? null  : address.getEmail();
		businessMailEl = uifactory.addTextElement("biz.mail", "edit.application.business.mail", 255, mail, formLayout);
		businessMailEl.setElementCssClass("o_sel_edit_business_mail");
		businessMailEl.setMandatory(!recruitingModule.isApplicationBusinessMailOptional() && !admin);
		businessMailEl.setVisible(recruitingModule.isApplicationBusinessMailEnabled());
		businessMailEl.setEnabled(editable);
	}
	
	private void initCustomAttributes(FormItemContainer formLayout) {
		attributesDelegate.initAdditionalAttributes(formLayout, additionalAttributesEl, application, admin, editable, getLocale());
	}
	
	private void securelySelectCountry(SingleSelection selectEl, String country, CountryKeysValues countryKeysValues) {
		boolean found = false;
		if(StringHelper.containsNonWhitespace(country)) {
			found = securelySelect(selectEl, country, countryKeysValues);
		} else if(StringHelper.containsNonWhitespace(recruitingModule.getApplicationDefaultCountry())) {
			String defCountry = recruitingModule.getApplicationDefaultCountry();
			found = securelySelect(selectEl, defCountry, countryKeysValues);
		} else {
			found = securelySelect(selectEl, "-", countryKeysValues);
		}

		if(!found) {
			selectEl.select(countryKeysValues.getKeys()[0], true);
		}
	}
	
	private boolean securelySelect(SingleSelection selectEl, String val, CountryKeysValues countryKeysValues) {
		for(String keyValue:countryKeysValues.getKeys()) {
			if(val.equals(keyValue)) {
				selectEl.select(keyValue, true);
				return true;
			}
		}
		return false;
	}
	
	private void securelySelectCountry(MultipleSelectionElement selectEl, String countries, CountryKeysValues countryKeysValues) {
		if(!StringHelper.containsNonWhitespace(countries)) return;
		
		String[] countriesArr = countries.split("[,]");
		for(String val:countriesArr) {
			securelySelect(selectEl, val, countryKeysValues);
		}
	}
	
	private void securelySelect(MultipleSelectionElement selectEl, String val, CountryKeysValues countryKeysValues) {
		for(String keyValue:countryKeysValues.getKeys()) {
			if(val.equals(keyValue)) {
				selectEl.select(keyValue, true);
			}
		}
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTitle();
		allOk &= validateTextElement(firstNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(lastNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		
		allOk &= validateMaritalStatus(!admin && !recruitingModule.isApplicationPersonMaritalStatusOptional());
		
		if(nationalityEl != null) {
			allOk &= validateTextElement(nationalityEl, 64,
					!admin && !recruitingModule.isApplicationPersonNationalityOptional(), new OWASPAntiSamyXSSFilter());
		} else if(nationalitySelectionEl != null) {
			allOk &= validateCountry(nationalitySelectionEl, !admin && !recruitingModule.isApplicationPersonNationalityOptional());
		}
		
		allOk &= validateTextElement(academicTitleEl, 255,
				!admin && !recruitingModule.isApplicationPersonAcademicTitleOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateEmailElement(mailEl, 64, true, new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(phoneEl, 64, !admin, new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(mobilePhoneEl, 64,
				!admin && !recruitingModule.isApplicationPersonMobilePhoneOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateBirthDay();
		allOk &= validateGender();
		
		//business informations
		allOk &= validateTextElement(organizationEl, 255, !admin && !recruitingModule.isApplicationBusinessInformationsOrganizationOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateSingleSelection(organizationDropdownEl, !admin && !recruitingModule.isApplicationBusinessInformationsOrganizationOptional())		;				
		allOk &= validateTextElement(unitEl, 255, !admin && !recruitingModule.isApplicationBusinessInformationsUnitOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(currentPositionEl, 255,
				!admin && !recruitingModule.isApplicationBusinessInformationsCurrentPositionOptional(), new OWASPAntiSamyXSSFilter());

		//private address
		if(!AddressOption.disabled.equals(privateOption)) {
			boolean mandatory = !AddressOption.optional.equals(privateOption);

			allOk &= validateTextElement(addressLine1El, 255, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(addressLine2El, 255, false, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(addressLine3El, 255, false, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(zipcodeEl, 64, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(cityEl, 64, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateCountry(countryEl, !admin && mandatory && !recruitingModule.isApplicationAddressCountryOptional());
		}
		
		//business address
		if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
			boolean mandatory = !AddressOption.optional.equals(businessOption);
			allOk &= validateTextElement(businessAddressLine1El, 255, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(businessAddressLine2El, 255, false, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(businessAddressLine3El, 255, false, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(businessZipcodeEl, 64, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateTextElement(businessCityEl, 64, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateCountry(businessCountryEl, !admin && mandatory && !recruitingModule.isApplicationAddressCountryOptional());
			allOk &= validateTextElement(businessPhoneEl, 64, mandatory && !admin, new OWASPAntiSamyXSSFilter());
			allOk &= validateEmailElement(businessMailEl, 255, mandatory && !admin, new OWASPAntiSamyXSSFilter());
		}
		
		allOk &= attributesDelegate.validateFormLogic(additionalAttributesEl, admin);

		return allOk;
	}
	
	protected boolean validateTitle() {
		boolean allOk = true;
		
		if(titleEl.isMandatory() && titleEl.isVisible() && titleEl.isEnabled()) {
			titleEl.clearError();
			if(!titleEl.isOneSelected()
					|| (titleEl.isOneSelected() && "not1".equals(titleEl.getSelectedKey()) && !admin)) {
				titleEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	protected boolean validateGender() {
		boolean allOk = true;
		
		if(genderEl.isMandatory() && genderEl.isVisible() && genderEl.isEnabled()) {
			genderEl.clearError();
			if(!genderEl.isOneSelected()
					|| (genderEl.isOneSelected() && genderChooseKey.equals(genderEl.getSelectedKey()))) {
				genderEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	protected boolean validateCountry(SingleSelection countrySelection, boolean mandatory) {
		boolean allOk = true;
		
		countrySelection.clearError();
		if(!countrySelection.isOneSelected()) {
			countrySelection.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(mandatory) {
			String selectKey = countrySelection.getSelectedKey();
			if("-".equals(selectKey)) {
				countrySelection.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	protected boolean validateMaritalStatus(boolean mandatory) {
		boolean allOk = true;
		
		maritalStatusEl.clearError();
		if(!maritalStatusEl.isOneSelected()) {
			maritalStatusEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(mandatory) {
			String selectKey = maritalStatusEl.getSelectedKey();
			if("-".equals(selectKey)) {
				maritalStatusEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	protected boolean validateBirthDay() {
		boolean allOk = true;
		
		birthdayContainer.clearError();
		int month = -1;
		try {
			birthMonthEl.clearError();
			String monthStr = birthMonthEl.getSelectedKey();
			if(admin && "-".equals(monthStr)) {
				month = -1;
			} else {
				month = Integer.parseInt(monthStr);
			}
		} catch (NumberFormatException e) {
			allOk = false;
			birthdayContainer.setErrorKey("birthday.date.error");
		}
		
		int year = -1;
		try {
			birthYearEl.clearError();
			String yearStr = birthYearEl.getValue();
			if(!admin && !StringHelper.containsNonWhitespace(yearStr) && !recruitingModule.isApplicationPersonBirthdayOptional()) {
				birthdayContainer.setErrorKey("birthday.date.error");
				allOk = false;
			} else if (StringHelper.containsNonWhitespace(yearStr)) {
				year = Integer.parseInt(yearStr);
				if(year < 1900) {
					allOk = false;
					birthdayContainer.setErrorKey("birthday.date.error");
				}
			}
		} catch (NumberFormatException e) {
			allOk =false;
			birthdayContainer.setErrorKey("birthday.date.error");
		}
		
		try {
			birthDayEl.clearError();
			String dayStr = birthDayEl.getValue();
			if(!admin && !StringHelper.containsNonWhitespace(dayStr) && !recruitingModule.isApplicationPersonBirthdayOptional()) {
				birthdayContainer.setErrorKey("birthday.date.error");
				allOk =false;
			} else if (StringHelper.containsNonWhitespace(dayStr)) {
				int day = Integer.parseInt(dayStr);
				int maxDay = 31;
				if(month >= 0 && year > 1900) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(getBirthday(1, month, year));
					maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}
				if(day < 1 || day > maxDay) {
					allOk =false;
					birthdayContainer.setErrorKey("birthday.date.error");
				}
			}
		} catch (NumberFormatException e) {
			allOk =false;
			birthdayContainer.setErrorKey("birthday.date.error");
		}
		
		Date birthday = getBirthday();
		if(!admin && birthday == null && !recruitingModule.isApplicationPersonBirthdayOptional()) {
			allOk &= false;
			birthdayContainer.setErrorKey("birthday.date.error");
		}
		
		return allOk;
	}
	
	private boolean validateEmailElement(TextElement textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = validateTextElement(textEl, length, mandatory, filter);
		if(ok) {
			//check email format
			String value = textEl.getValue();
			if(StringHelper.containsNonWhitespace(value) && !MailHelper.isValidEmailAddress(value)) {
				textEl.setErrorKey("email.error.valid");
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean validateTextElement(TextElement textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = true;
		textEl.clearError();
		if(textEl.isVisible()) {
			String value = textEl.getValue(filter);
			if(mandatory && !StringHelper.containsNonWhitespace(value)) {
				textEl.setErrorKey("form.legende.mandatory");
				ok = false;
			} else if (value.length() > length) {
				textEl.setErrorKey("input.toolong", Integer.toString(length));
				ok = false;
			} else if (filter.errors(value)) {
				textEl.setErrorKey("form.general.error", Integer.toString(length));
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean validateSingleSelection(SingleSelection selectEl, boolean mandatory) {
		boolean ok = true;
		selectEl.clearError();
		if(selectEl.isVisible()) {
			if(!selectEl.isOneSelected()) {
				selectEl.setErrorKey("form.legende.mandatory");
				ok = false;
			} else if(mandatory && (!StringHelper.containsNonWhitespace(selectEl.getSelectedKey()) || "-".equals(selectEl.getSelectedKey()))) {
				selectEl.setErrorKey("form.legende.mandatory");
				ok = false;
			}
		}
		return ok;
	}
	
	public void commitChanges(Application app) {
		Person person = app.getPerson();
		if(person == null) {
			person = new PersonImpl();
		}
		
		String title = null;
		if(titleEl.isVisible() && titleEl.isOneSelected()) {
			title = titleEl.getSelectedKey();
		}
		if("not1".equals(title) || "not2".equals(title)) {
			title = null;
		}
		person.setTitle(title);
		
		if(genderEl.isVisible() && genderEl.isOneSelected()) {
			person.setGender(genderEl.getSelectedKey());
		} else {
			person.setGender(null);
		}

		person.setMaritalStatus(maritalStatusEl.getSelectedKey());
		person.setFirstName(firstNameEl.getValue());
		person.setLastName(lastNameEl.getValue());
		
		attributesDelegate.commitChanges(additionalAttributesEl, app);
		
		if(birthdayContainer.isVisible()) {
			person.setBirthday(getBirthday());
		}
		
		if(nationalityEl != null) {
			person.setNationality(nationalityEl.getValue());
		} else if(nationalitySelectionEl != null) {
			if(nationalitySelectionEl.isOneSelected()) {
				person.setNationality(nationalitySelectionEl.getSelectedKey());
			}
		}
		
		if(additionalNationalitiesEl != null) {
			person.setAdditionalNationalities(additionalNationalitiesEl.getValue());
		} else if(additionalNationalitieSelectionEl != null) {
			Collection<String> values = additionalNationalitieSelectionEl.getSelectedKeys();
			person.setAdditionalNationalities(String.join(",", values));
		}
		
		person.setMail(mailEl.getValue());
		person.setPhone(phoneEl.getValue());
		person.setMobilePhone(mobilePhoneEl.getValue());
		person.setAcademicTitle(academicTitleEl.getValue());
		person.setDisability(Boolean.valueOf(disabilityEl.isAtLeastSelected(1)));
		
		app.setPerson(person);
		
		BusinessInformations businessInfos = app.getBusinessInformations();
		if(organizationEl.isVisible()) {
			businessInfos.setOrganization(organizationEl.getValue());
		} else if(organizationDropdownEl.isVisible() && organizationDropdownEl.isOneSelected()) {
			businessInfos.setOrganization(organizationDropdownEl.getSelectedValue());
		}
		businessInfos.setUnit(unitEl.getValue());
		businessInfos.setCurrentPosition(currentPositionEl.getValue());
		app.setBusinessInformations(businessInfos);
		
		Address address = app.getAddress();
		if((AddressOption.xor.equals(privateOption) && AddressOption.xor.equals(businessOption))
				|| AddressOption.enabled.equals(privateOption) || AddressOption.optional.equals(privateOption)) {

			Address.Type addrType = Address.Type.BUSINESS;
			if(addressTypeEl == null) {
				addrType = Address.Type.PRIVATE;
			} else if(addressTypeEl.isOneSelected()) {
				String selected = addressTypeEl.getSelectedKey();
				addrType = Address.Type.toType(selected);
			}
			address.setType(addrType);
			address.setAddressLine1(addressLine1El.getValue());
			address.setAddressLine2(addressLine2El.getValue());
			address.setAddressLine3(addressLine3El.getValue());
			address.setZipCode(zipcodeEl.getValue());
			address.setCity(cityEl.getValue());
			if(countryEl.isOneSelected() && !"-".equals(countryEl.getSelectedKey())) {
				address.setCountry(countryEl.getSelectedKey());
			} else {
				address.setCountry(null);
			}
		}
		
		BusinessAddress businessAddress = app.getBusinessAddress();
		if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
			businessAddress.setAddressLine1(businessAddressLine1El.getValue());
			businessAddress.setAddressLine2(businessAddressLine2El.getValue());
			businessAddress.setAddressLine3(businessAddressLine3El.getValue());
			businessAddress.setZipCode(businessZipcodeEl.getValue());
			businessAddress.setCity(businessCityEl.getValue());
			if(businessCountryEl.isOneSelected() && !"-".equals(businessCountryEl.getSelectedKey())) {
				businessAddress.setCountry(businessCountryEl.getSelectedKey());
			} else {
				businessAddress.setCountry(null);
			}

			businessAddress.setPhone(businessPhoneEl.getValue());
			businessAddress.setEmail(businessMailEl.getValue());
		}
		
		application = app;
	}
	
	private Date getBirthday() {
		String dayStr = birthDayEl.getValue();
		String monthStr = birthMonthEl.getSelectedKey();
		String yearStr = birthYearEl.getValue();
		
		if("-".equals(monthStr)) {
			return null;
		}
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getBirthday(day, month, year);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getBirthday(int day, int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges(application);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addressTypeEl == source) {
			if(addressTypeEl.isOneSelected()) {
				boolean businessOption = Address.Type.BUSINESS.getType().equals(addressTypeEl.getSelectedKey());
				if(organizationCopyEl != null) {
					organizationCopyEl.setVisible(businessOption);
				}
				if(unitCopyEl != null) {
					unitCopyEl.setVisible(businessOption && (unitEl != null && unitEl.isVisible()));
				}
			}
		}
		attributesDelegate.formInnerEvent(source);
		super.formInnerEvent(ureq, source, event);
	}
	
	private CountryKeysValues generateAdditionalCountryKeysAndValues(String value) {
		Country[] countries = Country.values();

		List<String> keys = new ArrayList<>(countries.length);
		List<String> values = new ArrayList<>(countries.length);

		Collator collator = Collator.getInstance(getLocale());

		List<CountryItem> sortedCountries = new ArrayList<>();
		for(Country country:countries) {
			String translation = translate(country.i18nKey());
			sortedCountries.add(new CountryItem(country.key(), translation));
		}
		
		Collections.sort(sortedCountries, new CountryComparator(collator));
		
		for(CountryItem country:sortedCountries) {
			keys.add(country.key());
			values.add(country.translation());
		}
		
		if(StringHelper.containsNonWhitespace(value)) {
			String[] valueArr = value.split("[,]");
			for(String val:valueArr) {
				if(!Country.isInTheList(val)) {
					keys.add(val);
					values.add(val);
				}
			}
		}
		
		String[] keyArr = keys.toArray(new String[keys.size()]);
		String[] valueArr = values.toArray(new String[values.size()]);
		return new CountryKeysValues(keyArr, valueArr);
	}

	private CountryKeysValues generateCountryKeysAndValues(String value) {
		Country[] countries = Country.values();
		Country[] preferedCountries = recruitingModule.getPreferedCountries();
		Set<Country> preferedCountrySet = new HashSet<>();
		for(Country preferedCountry:preferedCountries) {
			preferedCountrySet.add(preferedCountry);
		}
		
		List<String> keys = new ArrayList<>(countries.length + preferedCountrySet.size() + 3);
		List<String> values = new ArrayList<>(countries.length + preferedCountrySet.size() + 3);
		
		if(value != null && !"-".equals(value) && !Country.isInTheList(value) ) {
			keys.add(value);
			values.add(value);
		}

		keys.add("-");
		values.add(translate("please.choose"));
		
		Translator countryTranslator = userManager.getPropertyHandlerTranslator(getTranslator());
		
		Collator collator = Collator.getInstance(getLocale());
		if(preferedCountries.length > 0) {
			List<CountryItem> sortedPreferedCountries = new ArrayList<>();
			for(Country country:preferedCountries) {
				String translation = countryTranslator.translate(country.i18nKey());
				sortedPreferedCountries.add(new CountryItem(country.key(), translation));
			}
			
			Collections.sort(sortedPreferedCountries, new CountryComparator(collator));
		
			for(CountryItem country:sortedPreferedCountries) {
				keys.add(country.key());
				values.add(country.translation());
			}
		
			keys.add(SingleSelection.SEPARATOR);
			values.add(SingleSelection.SEPARATOR);
		}
		
		List<CountryItem> sortedCountries = new ArrayList<>();
		for(Country country:countries) {
			String translation = countryTranslator.translate(country.i18nKey());
			sortedCountries.add(new CountryItem(country.key(), translation));
		}
		
		Collections.sort(sortedCountries, new CountryComparator(collator));
		
		for(CountryItem country:sortedCountries) {
			keys.add(country.key());
			values.add(country.translation());
		}
		
		String[] keyArr = keys.toArray(new String[keys.size()]);
		String[] valueArr = values.toArray(new String[values.size()]);
		return new CountryKeysValues(keyArr, valueArr);
	}
	
	private static class CountryItem {
		
		private String key;
		private String translation;
		
		private CountryItem(String key, String translation) {
			this.key = key;
			this.translation = translation;
		}
		
		public String key() {
			return key;
		}
		
		public String translation() {
			return translation;
		}
	}
	
	private static class CountryComparator implements Comparator<CountryItem> {
		
		private final Collator collator;

		public CountryComparator(Collator collator) {
			this.collator = collator;
		}

		@Override
		public int compare(CountryItem c1, CountryItem c2) {
			String v1 = c1.translation();
			String v2 = c2.translation();
			
			int c = 0;
			if(collator != null) {
				c = collator.compare(v1, v2);
			} else {
				c = v1.compareTo(v2);
			}
			return c;
		}
	}
	
	private static class CountryKeysValues {
		
		private final String[] keys;
		private final String[] values;
		
		public CountryKeysValues(String[] keys, String[] values) {
			this.keys = keys;
			this.values = values;
		}

		public String[] getKeys() {
			return keys;
		}

		public String[] getValues() {
			return values;
		}
	}
}