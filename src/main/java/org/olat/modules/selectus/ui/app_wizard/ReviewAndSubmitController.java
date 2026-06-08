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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.BusinessAddress;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.Country;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.ApplicationDetailsController.CustomSet;
import org.olat.modules.selectus.ui.ApplicationDocument;
import org.olat.modules.selectus.ui.ApplicationDocumentMapper;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.RecruitingPositionSecurityCallbackForApplicant;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate.Details;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.position.TabsConfigurationDelegate;
import org.olat.user.propertyhandlers.CountryCodePropertyHandler;
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
public class ReviewAndSubmitController extends FormBasicController {
	
	private Position position;
	private RefereeList referees;
	private Application application;
	private final String mapperBaseURL;
	private SingleSelection jobAdsElement;
	private TextElement otherAdElement;
	private MultipleSelectionElement acceptTermsEl;
	
	private TabConfiguration tabConfiguration;
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	private final List<ApplicationAttributesDelegate> customAttributesDelegateList
		= new ArrayList<>();
	
	private String[] jobAdsKeys;
	private String[] jobAdsValues;
	private Translator countryTranslator;

	private final AddressOption privateOption;
	private final AddressOption businessOption;
	private final List<String> excludedAttributesList;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public ReviewAndSubmitController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Position position, Application application, TabConfiguration tabConfiguration, RefereeList referees) {
		super(ureq, wControl, "app_details", Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.referees = referees;
		this.position = position;
		this.application = application;
		this.tabConfiguration = tabConfiguration == null
				? application.getPosition().getTabConfiguration(Tab.reviewAndSubmit) : tabConfiguration;
		excludedAttributesList = position.getExcludedAttributesList();
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomEnabledTabsList();
			for(Tab customTab:customTabs) {
				customAttributesDelegateList.add(new ApplicationAttributesDelegate(customTab.attributesTab()));
			}
		}
		
		countryTranslator = Util.createPackageTranslator(CountryCodePropertyHandler.class, getLocale());

		mapperBaseURL = registerCacheableMapper(ureq, ReviewAndSubmitController.class.getSimpleName(),
				new ApplicationDocumentMapper(new RecruitingPositionSecurityCallbackForApplicant()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.review_submit.legend", new String[] {StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		setFormDescription("wizard.review_submit.explanation");
		
		Person person = application.getPerson();
		if(formLayout instanceof FormLayoutContainer ) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(recruitingModule.isApplayTermsInLastStep()) {
				String[] keys = new String[]{"yes"};
				String[] values = new String[]{translate("apply_application.acceptTerms.lastStep")};
				acceptTermsEl = uifactory.addCheckboxesHorizontal("acceptTerms", null, formLayout, keys, values);	
				acceptTermsEl.setElementCssClass("o_sel_accept_terms");
				layoutCont.add("acceptTerms", acceptTermsEl);
			}
			
			String msg = tabConfiguration.getHelp(getLocale());
			if(StringHelper.containsNonWhitespace(msg)) {
				msg = RecruitingHelper.escWithBR(msg);
			} else {
				msg = new TabsConfigurationDelegate(Tab.reviewAndSubmit)
						.getWarningReviewAndSubmit(position, getLocale());
			}
			layoutCont.contextPut("warningMsg", StringHelper.xssScan(msg));
		}
		initPersonalInformations(person, formLayout);
		initEmailAndPhone(person, formLayout);
		initBusinessInformations(application.getBusinessInformations(), formLayout);
		initPersonalDataAdditionalAttributes(formLayout);

		Address address = application.getAddress();
		if(AddressOption.xor.equals(privateOption) && AddressOption.xor.equals(businessOption)) {
			initAddress(address, formLayout);
		} else {
			if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
				initBusinessAddress(application.getBusinessAddress(), formLayout);
			}
			if(AddressOption.enabled.equals(privateOption) || AddressOption.optional.equals(privateOption)) {
				initPrivateAddress(address, formLayout);
			}
		}

		initAcademicalBackground(formLayout);
		initProject(formLayout);
		initCustomStepAttributes(formLayout);
		initDocuments(formLayout);
		initJobsAdd(formLayout);
	}
	
	
	private void initPersonalInformations(Person person, FormItemContainer formLayout) {
		if(person != null) {
			FormLayoutContainer personInfosLayout = FormLayoutContainer.createTableCondensedLayout("personInfos", getTranslator());
			personInfosLayout.setRootForm(mainForm);
			formLayout.add(personInfosLayout);
			
			TabConfiguration tabConfiguration = position.getTabConfiguration(Tab.personalData);
			if(StringHelper.containsNonWhitespace(tabConfiguration.getHeading(getLocale()))) {
				personInfosLayout.setFormTitle(StringHelper.escapeHtml(tabConfiguration.getHeading(getLocale())));
			}
			
			String fullname = StringHelper.escapeHtml(RecruitingHelper.formatFullName(application, getTranslator()));
			uifactory.addStaticTextElement("fullname", "edit.application.name", fullname, personInfosLayout);
			
			if(recruitingModule.isApplicationPersonGenderEnabled()) {
				String gender = person.getGender();
				String genderLabel = RecruitingHelper.formatGender(gender, getLocale());
				uifactory.addStaticTextElement("gender", "edit.application.gender", genderLabel, personInfosLayout);
			}
			
			if(StringHelper.containsNonWhitespace(person.getMaritalStatus())
					&& recruitingModule.isApplicationPersonMaritalStatusEnabled()
					&& !"-".equals(person.getMaritalStatus())) {
				String status = translate("edit.application.marital.status." + person.getMaritalStatus());
				uifactory.addStaticTextElement("maritalStatus", "edit.application.marital.status", status, personInfosLayout);
			}
			
			if(StringHelper.containsNonWhitespace(person.getAcademicTitle())
					&& recruitingModule.isApplicationPersonAcademicTitleEnabled()) {
				uifactory.addStaticTextElement("academicTitle", "edit.application.academicTitle",
						StringHelper.escapeHtml(person.getAcademicTitle()), personInfosLayout);
			}
			
			if(person.getBirthday() != null
					&& recruitingModule.isApplicationPersonBirthdayEnabled()) {
				String birthday = DateCellRenderer.format(person.getBirthday(), getLocale());
				uifactory.addStaticTextElement("birthday", "edit.application.birthday", birthday, personInfosLayout);
			}
			
			
			if(StringHelper.containsNonWhitespace(person.getNationality())
					&& recruitingModule.isApplicationPersonNationalityEnabled()) {
				String translatedCountry = translateCountry(person.getNationality());
				uifactory.addStaticTextElement("nationality", "edit.application.nationality",
						StringHelper.escapeHtml(translatedCountry), personInfosLayout);
			}
			if(StringHelper.containsNonWhitespace(person.getAdditionalNationalities())
					&& recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled()) {
				String translatedCountry = translateCountries(person.getAdditionalNationalities());
				uifactory.addStaticTextElement("add.nationalities", "edit.application.additional.nationalities",
						StringHelper.escapeHtml(translatedCountry), personInfosLayout);
			}
			
		}
	}
	
	private void initPersonalDataAdditionalAttributes(FormItemContainer formLayout) {
		if(personalDataAttributesDelegate.hasSomeValue(application)) {
			FormLayoutContainer additionalInfosLayout = FormLayoutContainer.createTableCondensedLayout("additionalPersonalData", getTranslator());
			additionalInfosLayout.setRootForm(mainForm);
			formLayout.add(additionalInfosLayout);
	
			personalDataAttributesDelegate.initAdditionalAttributesDetails(formLayout, additionalInfosLayout, mainForm,
					application, RecruitingModule.APP_SECTION_PERSON, null, getLocale());
		}
	}
	
	private void initEmailAndPhone(Person person, FormItemContainer formLayout) {
		if(person != null) {
			FormLayoutContainer mailPhoneLayout = FormLayoutContainer.createTableCondensedLayout("emailAndPhone", getTranslator());
			mailPhoneLayout.setRootForm(mainForm);
			mailPhoneLayout.setVisible(true);
			mailPhoneLayout.setFormTitle(translate("contact.data"));
			formLayout.add(mailPhoneLayout);
		
			if(StringHelper.containsNonWhitespace(person.getPhone())
					&& recruitingModule.isApplicationPersonPhoneEnabled()) {
				uifactory.addStaticTextElement("phone", "edit.application.phone",
						StringHelper.escapeHtml(person.getPhone()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(person.getMobilePhone())
					&& recruitingModule.isApplicationPersonMobilePhoneEnabled()) {
				uifactory.addStaticTextElement("mobilePhone", "edit.application.mobile.phone",
						StringHelper.escapeHtml(person.getMobilePhone()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(person.getMail())) {
				uifactory.addStaticTextElement("mail", "edit.application.mail",
						StringHelper.escapeHtml(person.getMail()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
			
			if(person.getDisability() != null && person.getDisability().booleanValue()
					&& recruitingModule.isApplicationPersonDisabilityEnabled()) {
				MultipleSelectionElement disabilityEl = uifactory.addCheckboxesHorizontal("edit.application.disability", mailPhoneLayout, new String[]{ "on" }, new String[]{ "" });
				disabilityEl.setEnabled(false);
				disabilityEl.select("on", true);
			}
		}
	}
	
	private void initBusinessInformations(BusinessInformations businessInfos, FormItemContainer formLayout) {
		if(businessInfos != null) {
			FormLayoutContainer businessInfosLayout = FormLayoutContainer.createTableCondensedLayout("businessInfos", getTranslator());
			businessInfosLayout.setRootForm(mainForm);
			businessInfosLayout.setVisible(false);
			businessInfosLayout.setFormTitle(translate("business.infos"));
			businessInfosLayout.setElementCssClass("o_sel_business_infos");
			formLayout.add(businessInfosLayout);
			
			String merged = RecruitingHelper.mergeOrganizationAndAffiliation(businessInfos);
			if(StringHelper.containsNonWhitespace(merged)
					&& recruitingModule.isApplicationBusinessInformationsOrganizationEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)) {
				uifactory.addStaticTextElement("addressOrg", "edit.application.organization",
						StringHelper.escapeHtml(merged), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessInfos.getUnit())
					&& recruitingModule.isApplicationBusinessInformationsUnitEnable()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT)) {
				uifactory.addStaticTextElement("addressUnit", "edit.application.unit",
						StringHelper.escapeHtml(businessInfos.getUnit()), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessInfos.getCurrentPosition())
					&& recruitingModule.isApplicationBusinessInformationsCurrentPositionEnabled()
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION)) {
				uifactory.addStaticTextElement("addressCurrentPosition", "edit.application.currentPosition",
						StringHelper.escapeHtml(businessInfos.getCurrentPosition()), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
		}
	}
	
	private void initAddress(Address address, FormItemContainer formLayout) {
		if(address != null) {
			FormLayoutContainer adressLayout = FormLayoutContainer.createTableCondensedLayout("singleAddress", getTranslator());
			adressLayout.setRootForm(mainForm);
			adressLayout.setVisible(false);
			formLayout.add(adressLayout);
			
			if(StringHelper.containsNonWhitespace(address.getAddressLine1())) {
				uifactory.addStaticTextElement("addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(address.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine2())) {
				uifactory.addStaticTextElement("addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(address.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine3())) {
				uifactory.addStaticTextElement("addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(address.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			String fullCity = RecruitingHelper.formatZipcodeCity(address);
			if(StringHelper.containsNonWhitespace(fullCity)) {
				uifactory.addStaticTextElement("city", "edit.application.city",
						StringHelper.escapeHtml(fullCity), adressLayout);
				adressLayout.setVisible(true);
			}
			if(recruitingModule.isApplicationAddressCountryEnabled() && StringHelper.containsNonWhitespace(address.getCountry())) {
				String translatedCountry = translateCountry(address.getCountry());
				uifactory.addStaticTextElement("country", "edit.application.country",
						StringHelper.escapeHtml(translatedCountry), adressLayout);
				adressLayout.setVisible(true);
			}
		}
	}
	
	private void initBusinessAddress(BusinessAddress businessAddress, FormItemContainer formLayout) {
		if(businessAddress != null) {
			FormLayoutContainer adressLayout = FormLayoutContainer.createTableCondensedLayout("businessAddress", getTranslator());
			adressLayout.setRootForm(mainForm);
			adressLayout.setFormTitle(translate("address.business"));
			adressLayout.setVisible(false);
			formLayout.add(adressLayout);

			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine1())) {
				uifactory.addStaticTextElement("biz_addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(businessAddress.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine2())) {
				uifactory.addStaticTextElement("biz_addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(businessAddress.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine3())) {
				uifactory.addStaticTextElement("biz_addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(businessAddress.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			String fullCity = RecruitingHelper.formatZipcodeCity(businessAddress);
			if(StringHelper.containsNonWhitespace(fullCity)) {
				uifactory.addStaticTextElement("biz_city", "edit.application.city",
						StringHelper.escapeHtml(fullCity), adressLayout);
				adressLayout.setVisible(true);
			}
			if(recruitingModule.isApplicationAddressCountryEnabled() && StringHelper.containsNonWhitespace(businessAddress.getCountry())) {
				String translatedCountry = translateCountry(businessAddress.getCountry());
				uifactory.addStaticTextElement("biz_country", "edit.application.country",
						StringHelper.escapeHtml(translatedCountry), adressLayout);
				adressLayout.setVisible(true);
			}
			
			if(StringHelper.containsNonWhitespace(businessAddress.getPhone())) {
				uifactory.addStaticTextElement("biz_phone", "edit.application.business.phone",
						StringHelper.escapeHtml(businessAddress.getPhone()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getEmail())) {
				uifactory.addStaticTextElement("biz_mail", "edit.application.business.mail",
						StringHelper.escapeHtml(businessAddress.getEmail()), adressLayout);
				adressLayout.setVisible(true);
			}
		}
	}

	private void initPrivateAddress(Address address, FormItemContainer formLayout) {
		if(address != null) {
			FormLayoutContainer adressLayout = FormLayoutContainer.createTableCondensedLayout("privateAddress", getTranslator());
			adressLayout.setRootForm(mainForm);
			adressLayout.setVisible(false);
			adressLayout.setFormTitle(translate("address.private"));
			formLayout.add(adressLayout);

			if(StringHelper.containsNonWhitespace(address.getAddressLine1())) {
				uifactory.addStaticTextElement("addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(address.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine2())) {
				uifactory.addStaticTextElement("addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(address.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine3())) {
				uifactory.addStaticTextElement("addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(address.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			String fullCity = RecruitingHelper.formatZipcodeCity(address);
			if(StringHelper.containsNonWhitespace(fullCity)) {
				uifactory.addStaticTextElement("city", "edit.application.city",
						StringHelper.escapeHtml(fullCity), adressLayout);
				adressLayout.setVisible(true);
			}
			if(recruitingModule.isApplicationAddressCountryEnabled() && StringHelper.containsNonWhitespace(address.getCountry())) {
				String translatedCountry = translateCountry(address.getCountry());
				uifactory.addStaticTextElement("country", "edit.application.country",
						StringHelper.escapeHtml(translatedCountry), adressLayout);
				adressLayout.setVisible(true);
			}
		}
	}
	
	private String translateCountries(String countryKeys) {
		StringBuilder sb = new StringBuilder();
		if(countryKeys != null) {
			String[] countryKeysArr = countryKeys.split("[,]");
			for(String countryKey:countryKeysArr) {
				String translatedCountry = translateCountry(countryKey);
				if(StringHelper.containsNonWhitespace(translatedCountry)) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(translatedCountry);
				}
			}
		}
		return sb.toString();
	}
	
	private String translateCountry(String countryKey) {
		Country country = Country.country(countryKey);
		String translation;
		if(country != null) {
			translation = countryTranslator.translate(country.i18nKey());
		} else {
			translation = countryKey;
		}
		return translation;
	}
	
	private void initProject(FormItemContainer formLayout) {
		Project project = application.getProject();
		if(project != null && recruitingModule.isApplicationProjectEnabled()
				&& position.isApplicationProject()
				&& (project.hasData() || projectAttributesDelegate.hasSomeValue(application))) {
			FormLayoutContainer projectLayout = FormLayoutContainer.createTableCondensedLayout("project", getTranslator());
			projectLayout.setRootForm(mainForm);
			formLayout.add(projectLayout);
			
			TabConfiguration tabConfiguration = position.getTabConfiguration(Tab.project);
			if(StringHelper.containsNonWhitespace(tabConfiguration.getHeading(getLocale()))) {
				projectLayout.setFormTitle(StringHelper.escapeHtml(tabConfiguration.getHeading(getLocale())));
			}

			if(recruitingModule.isApplicationProjectTitleEnabled()
					&& StringHelper.containsNonWhitespace(project.getTitle())) {
				uifactory.addStaticTextElement("projectTitle", "edit.application.project.title", project.getTitle(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectAcronymEnabled()
					&& StringHelper.containsNonWhitespace(project.getAcronym())) {
				uifactory.addStaticTextElement("projectAcronym", "edit.application.project.acronym", project.getAcronym(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectKeywordsEnabled()
					&& StringHelper.containsNonWhitespace(project.getKeywords())) {
				uifactory.addStaticTextElement("projectKeywords", "edit.application.project.keywords", project.getKeywords(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectDisciplinesEnabled()
					&& StringHelper.containsNonWhitespace(project.getDisciplines())) {
				uifactory.addStaticTextElement("projectDisciplines", "edit.application.project.disciplines", project.getDisciplines(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectStartDateEnabled()
					&& project.getStartDate() != null) {
				String startDate = DateCellRenderer.format(project.getStartDate());
				uifactory.addStaticTextElement("projectStartDate", "edit.application.project.start.date", startDate, projectLayout);
			}
			if(recruitingModule.isApplicationProjectDurationEnabled()
					&& StringHelper.containsNonWhitespace(project.getDuration())) {
				uifactory.addStaticTextElement("projectDuration", "edit.application.project.duration", project.getDuration(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectFinancialImpact1Enabled()
					&& StringHelper.containsNonWhitespace(project.getFinancialImpact1())) {
				initFinancialImpact(1, project.getFinancialImpact1(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectFinancialImpact2Enabled()
					&& StringHelper.containsNonWhitespace(project.getFinancialImpact2())) {
				initFinancialImpact(2, project.getFinancialImpact2(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectFinancialImpact3Enabled()
					&& StringHelper.containsNonWhitespace(project.getFinancialImpact3())) {
				initFinancialImpact(3, project.getFinancialImpact3(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectFinancialImpact4Enabled()
					&& StringHelper.containsNonWhitespace(project.getFinancialImpact4())) {
				initFinancialImpact(4, project.getFinancialImpact4(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectFinancialImpact5Enabled()
					&& StringHelper.containsNonWhitespace(project.getFinancialImpact5())) {
				initFinancialImpact(5, project.getFinancialImpact5(), projectLayout);
			}
			if(recruitingModule.isApplicationProjectDescriptionEnabled()
					&& StringHelper.containsNonWhitespace(project.getDescription())) {
				StringBuilder desc = Formatter.escWithBR(project.getDescription());
				uifactory.addStaticTextElement("projectDescription", "edit.application.project.description", desc.toString(), projectLayout);
			}
			
			projectAttributesDelegate.initAdditionalAttributesDetails(formLayout, projectLayout, mainForm,
					application, RecruitingModule.APP_SECTION_PROJECT, null, getLocale());	
		}
	}
	
	private void initFinancialImpact(int num, String text, FormItemContainer projectLayout) {
		String unit = translate("edit.application.project.financialimpact.unit." + num);
		if(StringHelper.containsNonWhitespace(unit)) {
			text += " " + unit;
		}
		uifactory.addStaticTextElement("projectFinancialImpact" + num, "edit.application.project.financialimpact." + num,
				StringHelper.escapeHtml(text), projectLayout);
	}
	
	private void initAcademicalBackground(FormItemContainer formLayout) {
		if (recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			FormLayoutContainer backgroundLayout = FormLayoutContainer.createTableCondensedLayout("background", getTranslator());
			backgroundLayout.setRootForm(mainForm);
			formLayout.add(backgroundLayout);
			
			AcademicalBackground background = application.getAcademicalBackground();
			if(background != null) {
				
				TabConfiguration tabConfiguration = position.getTabConfiguration(Tab.academicalBackground);
				if(StringHelper.containsNonWhitespace(tabConfiguration.getHeading(getLocale()))) {
					backgroundLayout.setFormTitle(StringHelper.escapeHtml(tabConfiguration.getHeading(getLocale())));
				}
			
				if(background.getNumberOfOriginalPublications() != null && background.getNumberOfOriginalPublications().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()) {
					uifactory.addStaticTextElement("numOfPublications", "edit.application.numberOfOriginalPublications",
							Integer.toString(background.getNumberOfOriginalPublications()), backgroundLayout);
				}
			
				if(background.getNumberOfFirstAuthorships() != null && background.getNumberOfFirstAuthorships().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()) {
					uifactory.addStaticTextElement("numOfFirstAuthorships", "edit.application.numberOfFirstAuthorships",
							Integer.toString(background.getNumberOfFirstAuthorships()), backgroundLayout);
				}
				
				if(background.getNumberOfLastAuthorships() != null && background.getNumberOfLastAuthorships().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()) {
					uifactory.addStaticTextElement("numOfLastAuthorships", "edit.application.numberOfLastAuthorships",
							Integer.toString(background.getNumberOfLastAuthorships()), backgroundLayout);
				}
				
				if(background.getCitations() != null && background.getCitations().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()) {
					uifactory.addStaticTextElement("citations", "edit.application.citations",
							Integer.toString(background.getCitations()), backgroundLayout);
				}
			
				if(background.getImpactFactor() != null
						&& recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()) {
					uifactory.addStaticTextElement("impactFactor", "edit.application.impactFactor",
							RecruitingHelper.formatFactor(background.getImpactFactor()), backgroundLayout);
				}
				
				if(background.getHFactor() != null
						&& recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {
					uifactory.addStaticTextElement("hFactor", "edit.application.hFactor",
							RecruitingHelper.formatFactor(background.getHFactor()), backgroundLayout);
				}
			
				if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
						&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
					String highestDegree = RecruitingHelper.formatHighestDegree(application, excludedAttributesList, getTranslator());
					if(StringHelper.containsNonWhitespace(highestDegree)) {
						uifactory.addStaticTextElement("highestdegree", "edit.application.highestdegree",
								StringHelper.escapeHtml(highestDegree), backgroundLayout);
					}
				}

				if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()) {
					String workedInAcademia = application.getAcademicalBackground().getWorkedInAcademiaSince();
					if(StringHelper.containsNonWhitespace(workedInAcademia)) {
						uifactory.addStaticTextElement("workedInAcademiaWS", "edit.application.workedInAcademiaSince.label",
								StringHelper.escapeHtml(workedInAcademia), backgroundLayout);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()) {
					String workedOutAcademia = application.getAcademicalBackground().getWorkedOutAcademiaSince();
					if(StringHelper.containsNonWhitespace(workedOutAcademia)) {
						uifactory.addStaticTextElement("workedOutAcademiaWS", "edit.application.workedOutAcademiaSince.label",
								StringHelper.escapeHtml(workedOutAcademia), backgroundLayout);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()) {
					String workedOutAcademiaCare = application.getAcademicalBackground().getWorkedOutAcademiaCareSince();
					if(StringHelper.containsNonWhitespace(workedOutAcademiaCare)) {
						uifactory.addStaticTextElement("workedOutAcademiaCareWS", "edit.application.workedOutAcademiaCareSince.label",
								StringHelper.escapeHtml(workedOutAcademiaCare), backgroundLayout);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()) {
					String careerDescription = application.getAcademicalBackground().getCareerDescription();
					if(StringHelper.containsNonWhitespace(careerDescription)) {
						uifactory.addStaticTextElement("careerDescriptionWS", "edit.application.careerDescription.details.label",
								StringHelper.escapeHtml(careerDescription), backgroundLayout);
					}
				}
				
				if((recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
						|| recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
						|| recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled())
						&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
					String dissertation = RecruitingHelper.formatDissertation(application, getLocale());
					if(StringHelper.containsNonWhitespace(dissertation)) {
						uifactory.addStaticTextElement("dissertation", "edit.application.dissertation",
								StringHelper.escapeHtml(dissertation), backgroundLayout);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()) {
					String keyword1 = application.getAcademicalBackground().getDissertationKeyword1();
					if(StringHelper.containsNonWhitespace(keyword1)) {
						uifactory.addStaticTextElement("dissertation.keyword1", "edit.application.dissertationkeyword1",
								StringHelper.escapeHtml(keyword1), backgroundLayout);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()) {
					String keyword2 = application.getAcademicalBackground().getDissertationKeyword2();
					if(StringHelper.containsNonWhitespace(keyword2)) {
						uifactory.addStaticTextElement("dissertation.keyword2", "edit.application.dissertationkeyword2",
								StringHelper.escapeHtml(keyword2), backgroundLayout);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()) {
					String keyword3 = application.getAcademicalBackground().getDissertationKeyword3();
					if(StringHelper.containsNonWhitespace(keyword3)) {
						uifactory.addStaticTextElement("dissertation.keyword3", "edit.application.dissertationkeyword3",
								StringHelper.escapeHtml(keyword3), backgroundLayout);
					}
				}
			
				if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()) {
					String habilitation = RecruitingHelper.formatHabilitation(application);
					if(StringHelper.containsNonWhitespace(habilitation)) {
						uifactory.addStaticTextElement("habilitation", "edit.application.habilitation",
								StringHelper.escapeHtml(habilitation), backgroundLayout);
					}
				}
				
				academicalBackgroundAttributesDelegate.initAdditionalAttributesDetails(formLayout, backgroundLayout, mainForm,
						application, RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, null, getLocale());
			}
		}
	}
	
	private void initDocuments(FormItemContainer formLayout) {
		String docPage = Util.getPackageVelocityRoot(ReviewAndSubmitController.class) + "/documents.html";
		FormLayoutContainer documentsLayout = FormLayoutContainer.createCustomFormLayout("documents", getTranslator(), docPage);
		documentsLayout.setRootForm(mainForm);
		formLayout.add(documentsLayout);

		List<ApplicationDocument> documents = new ArrayList<>();
		Set<String> available = position.getAvailableDocuments();
		Set<String> mandatory = position.getMandatoryDocuments();
		
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(!available.contains(doc.name()) && !mandatory.contains(doc.name())) {
				continue;
			}
			
			Attachment attachment = doc.path(application);
			if(attachment != null) {
				String label = attachment.getName();
				if(!StringHelper.containsNonWhitespace(label)) {
					label = position.getDocumentName(doc, getLocale());
				}
				if(!StringHelper.containsNonWhitespace(label)) {
					label = translate(doc.i18nKey());
				}
				
				String relativePath = "/" + application.getKey() + "/" + doc.name() + "/" + RecruitingHelper.normalizeFilename(label);
				
				String lLabel = label.toLowerCase();
				if(!lLabel.endsWith(".pdf")
						&& !lLabel.endsWith(".doc") && !lLabel.endsWith(".docx")
						&& !lLabel.endsWith(".xls") && !lLabel.endsWith(".xlsx")
						&& !lLabel.endsWith(".jpg") && !lLabel.endsWith(".jpeg")) {
					label += ".pdf";
				}
				
				ApplicationDocument document = new ApplicationDocument(label, relativePath);
				if(label.endsWith(".jpg") || label.endsWith(".jpeg")) {
					document.setImage(true);
				}
				documents.add(document);
			}
		}
		documentsLayout.contextPut("mapperBaseURL", mapperBaseURL);
		documentsLayout.contextPut("documents", documents);
		
		if(referees != null && referees.getReferees() != null && !referees.getReferees().isEmpty()) {
			List<Referee> filledReferees = new ArrayList<>();
			for(Referee ref:referees.getReferees()) {
				if(ref.isComplete()) {
					filledReferees.add(ref);
				}
			}
			documentsLayout.contextPut("referees", filledReferees);
		}
	}
	
	private void initCustomStepAttributes(FormItemContainer formLayout) {
		List<CustomSet> customTabsIds = new ArrayList<>();
		for(ApplicationAttributesDelegate customAttributesDelegate:customAttributesDelegateList) {
			Tab tab = customAttributesDelegate.tab().tab();
			TabConfiguration configuration = position.getTabConfiguration(tab);
			if(!configuration.isDisabled() && customAttributesDelegate.hasSomeValue(application)) {
				String id = "custom_" + tab.name();
				String title = configuration.getTitle(getLocale());
				
				FormLayoutContainer customLayout = FormLayoutContainer.createTableCondensedLayout(id, getTranslator());
				customLayout.setRootForm(mainForm);
				formLayout.add(customLayout);
				
				Details details = customAttributesDelegate.initAdditionalAttributesDetails(formLayout, customLayout, mainForm,
						application, tab.name(), null, getLocale());
				if(details.hasValue()) {
					customTabsIds.add(new CustomSet(customLayout.getComponent().getComponentName(), title, tab.name(), details.containers()));
				}
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("customTabsIds", customTabsIds);
		}
	}

	private void initJobsAdd(FormItemContainer formLayout) {
		String[] values = getJobAdsValues();
		boolean textFieldOnly = recruitingModule.isPositionJobAdsFreeTextOnlyEnabled();
		if(textFieldOnly) {
			String jobAdsPage = Util.getPackageVelocityRoot(ReviewAndSubmitController.class) + "/jobads.html";
			FormLayoutContainer jobAdslayout = FormLayoutContainer.createCustomFormLayout("jobAds", getTranslator(), jobAdsPage);
			jobAdslayout.setRootForm(mainForm);
			jobAdslayout.contextPut("textFieldOnly", Boolean.valueOf(textFieldOnly));
			formLayout.add(jobAdslayout);
			
			otherAdElement = uifactory.addTextElement("jobads.other", "jobads.other", 200, "", jobAdslayout);
			otherAdElement.setDomReplacementWrapperRequired(false);
			otherAdElement.setDisplaySize(96);
			otherAdElement.setVisible(textFieldOnly);
		} else if(values != null && values.length > 0) {
			String jobAdsPage = Util.getPackageVelocityRoot(ReviewAndSubmitController.class) + "/jobads.html";
			FormLayoutContainer jobAdslayout = FormLayoutContainer.createCustomFormLayout("jobAds", getTranslator(), jobAdsPage);
			jobAdslayout.setRootForm(mainForm);
			jobAdslayout.contextPut("textFieldOnly", Boolean.valueOf(textFieldOnly));
			formLayout.add(jobAdslayout);
			
			boolean otherEnabled = recruitingModule.isPositionJobAdsOtherEnabled();
			
			jobAdsValues = new String[values.length + (otherEnabled ? 2 : 1)];
			jobAdsValues[0] = translate("jobads.choose");
			for(int i=0; i<values.length;i++) {
				jobAdsValues[i+1] = values[i];
			}
			if(otherEnabled) {
				jobAdsValues[values.length + 1] = translate("jobads.other");
			}
			
			jobAdsKeys = new String[jobAdsValues.length];
			for(int i=0; i<jobAdsValues.length; i++) {
				jobAdsKeys[i] = "ad_" + Integer.toString(i);
			}
			
			jobAdsElement = uifactory.addDropdownSingleselect("jobads.select", null, jobAdslayout, jobAdsKeys, jobAdsValues, null);
			jobAdsElement.setDomReplacementWrapperRequired(false);
			jobAdsElement.addActionListener(FormEvent.ONCHANGE);
			jobAdsElement.select(jobAdsKeys[0], true);
			otherAdElement = uifactory.addTextElement("jobads.other", "jobads.other", 200, "", jobAdslayout);
			otherAdElement.setDomReplacementWrapperRequired(false);
			otherAdElement.setDisplaySize(32);
			otherAdElement.setVisible(false);
			
			String selectedJobAd = application.getJobAd();
			if(StringHelper.containsNonWhitespace(selectedJobAd)) {
				boolean found = false;
				for(int i=jobAdsValues.length;i-->0; ) {
					if(selectedJobAd.equals(jobAdsValues[i])) {
						jobAdsElement.select(jobAdsKeys[i], true);
						found = true;
					}
				}
				
				if(!found) {
					jobAdsElement.select(jobAdsKeys[jobAdsKeys.length - 1], true);
					otherAdElement.setValue(selectedJobAd);
					otherAdElement.setVisible(true);
				}
			}
		}
	}
	
	private String[] getJobAdsValues() {
		String[] values;
		String adsStr = application.getPosition().getJobAds();
		if(StringHelper.containsNonWhitespace(adsStr)) {
			String[] valueArray = adsStr.split("\\r?\\n");
			List<String> valueList = new ArrayList<>();
			for(String value:valueArray) {
				if(StringHelper.containsNonWhitespace(value)) {
					valueList.add(value);
				}
			}
			values = valueList.toArray(new String[valueList.size()]);
		} else {
			values = new String[0];
		}
		return values;
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}
	
	public void commitChanges(Application app) {
		String ad = null;
		if(jobAdsKeys != null && jobAdsElement != null && jobAdsElement.isOneSelected()) {
			String key = jobAdsElement.getSelectedKey();
			if(jobAdsKeys[0].equals(key)) {
				//do nothing
			} else if(jobAdsKeys[jobAdsKeys.length - 1].equals(key)) {
				ad = otherAdElement.getValue();
			} else {
				for(int i=1; i<jobAdsKeys.length - 1; i++) {
					if(jobAdsKeys[i].equals(key)) {
						ad = jobAdsValues[i];
					}
				}	
			}
		}
		app.setJobAd(ad);
		application = app;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(acceptTermsEl != null) {
			acceptTermsEl.clearError();
			if(!acceptTermsEl.isMultiselect() || !acceptTermsEl.isSelected(0)) {
				acceptTermsEl.setErrorKey("apply_application.acceptTerms.error");
				allOk = false;
			}
		}
		
		if(otherAdElement != null && otherAdElement.isVisible()) {
			allOk &= RecruitingHelper.validateTextElement(otherAdElement, 200, otherAdElement.isMandatory(), new OWASPAntiSamyXSSFilter());
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == jobAdsElement) {
			if(jobAdsElement.isOneSelected() && jobAdsKeys != null) {
				String key = jobAdsElement.getSelectedKey();
				boolean other = key.equals(jobAdsKeys[jobAdsKeys.length - 1]);
				otherAdElement.setVisible(other);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}