/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.BusinessAddress;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate.Details;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDetailsController extends FormBasicController {

	private final Position position;
	private final Application application;
	private final AddressOption privateOption;
	private final AddressOption businessOption;
	private final List<String> excludedAttributesList;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private final ApplicationAttributesDelegate projectAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	private final ApplicationAttributesDelegate personalDataAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.personalData);
	private final ApplicationAttributesDelegate academicalBackgroundAttributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	private final List<ApplicationAttributesDelegate> customAttributesDelegateList
		= new ArrayList<>();
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public ApplicationDetailsController(UserRequest ureq, WindowControl wControl, Position position, Application application,
			RecruitingPositionSecurityCallback secCallback, Form rootForm) {
		super(ureq, wControl,"app_details");
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		excludedAttributesList = position.getExcludedAttributesList();
		privateOption = recruitingModule.getApplicationAddressPrivateOption();
		businessOption = recruitingModule.getApplicationAddressBusinessOption();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomEnabledTabsList();
			for(Tab customTab:customTabs) {
				customAttributesDelegateList.add(new ApplicationAttributesDelegate(customTab.attributesTab()));
			}
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Person person = application.getPerson();
		initPersonalInformations(person, formLayout);
		initEmailAndPhone(person, formLayout);
		initBusinessInformations(application.getBusinessInformations(), formLayout);
		
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
		
		initPersonalDataAdditionalAttributes(formLayout);

		if(recruitingModule.isPositionAcademicalBackgroundEnabled() && position.isApplicationAcademicalBackground()) {
			initAcademicalBackground(formLayout);
		}
		if(position.isApplicationProject()  && recruitingModule.isApplicationProjectEnabled() && application.getProject() != null
				&& (application.getProject().hasData() || projectAttributesDelegate.hasSomeValue(application))) {
			initProject(formLayout);
		}
		
		initCustomStepAttributes(formLayout);
	}
	
	private void initProject(FormItemContainer formLayout) {
		Project project = application.getProject();
		
		FormLayoutContainer projectLayout = FormLayoutContainer.createTableCondensedLayout("project", getTranslator());
		projectLayout.setRootForm(mainForm);
		formLayout.add(projectLayout);
		projectLayout.setVisible(false);
	
		if(recruitingModule.isApplicationProjectTitleEnabled() && StringHelper.containsNonWhitespace(project.getTitle())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_TITLE)) {
			uifactory.addStaticTextElement("projectTitle", "edit.application.project.title",
					StringHelper.escapeHtml(project.getTitle()), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectAcronymEnabled() && StringHelper.containsNonWhitespace(project.getAcronym())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_ACRONYM)) {
			uifactory.addStaticTextElement("projectAcronym", "edit.application.project.acronym",
					StringHelper.escapeHtml(project.getAcronym()), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectKeywordsEnabled() && StringHelper.containsNonWhitespace(project.getKeywords())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_KEYWORDS)) {
			uifactory.addStaticTextElement("projectKeywords", "edit.application.project.keywords",
					StringHelper.escapeHtml(project.getKeywords()), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectDisciplinesEnabled() && StringHelper.containsNonWhitespace(project.getDisciplines())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DISCIPLINES)) {
			uifactory.addStaticTextElement("projectDisciplines", "edit.application.project.disciplines",
					StringHelper.escapeHtml(project.getDisciplines()), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectStartDateEnabled() && project.getStartDate() != null
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_START_DATE)) {
			String startDate = DateCellRenderer.format(project.getStartDate());
			uifactory.addStaticTextElement("projectStartDate", "edit.application.project.start.date", startDate, projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectDurationEnabled() && StringHelper.containsNonWhitespace(project.getDuration())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DURATION)) {
			uifactory.addStaticTextElement("projectDuration", "edit.application.project.duration",
					StringHelper.escapeHtml(project.getDuration()), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectFinancialImpact1Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact1())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_1)) {
			initFinancialImpact(1, project.getFinancialImpact1(), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectFinancialImpact2Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact2())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_2)) {
			initFinancialImpact(2, project.getFinancialImpact2(), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectFinancialImpact3Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact3())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_3)) {
			initFinancialImpact(3, project.getFinancialImpact3(), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectFinancialImpact4Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact4())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_4)) {
			initFinancialImpact(4, project.getFinancialImpact4(), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectFinancialImpact5Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact5())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_5)) {
			initFinancialImpact(5, project.getFinancialImpact5(), projectLayout);
			projectLayout.setVisible(true);
		}
		if(recruitingModule.isApplicationProjectDescriptionEnabled() && StringHelper.containsNonWhitespace(project.getDescription())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DESCRIPTION)) {
			StringBuilder desc = Formatter.escWithBR(project.getDescription());
			uifactory.addStaticTextElement("projectDescription", "edit.application.project.description", desc.toString(), projectLayout);
			projectLayout.setVisible(true);
		}
		
		boolean hasCustomValues = projectAttributesDelegate.initAdditionalAttributesDetails(formLayout, projectLayout, mainForm,
				application, RecruitingModule.APP_SECTION_PROJECT, secCallback, getLocale()).hasValue();
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("hasProjectValues", Boolean.valueOf(projectLayout.isVisible() || hasCustomValues));
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

	private void initPersonalInformations(Person person, FormItemContainer formLayout) {
		if(person != null) {
			FormLayoutContainer personInfosLayout = FormLayoutContainer.createTableCondensedLayout("personInfos", getTranslator());
			personInfosLayout.setRootForm(mainForm);
			formLayout.add(personInfosLayout);
			
			String id = application.getId() == null ? "" : application.getId().toString();
			if(StringHelper.containsNonWhitespace(id)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ID)) {
				uifactory.addStaticTextElement("appId", "edit.application.id.long", id, personInfosLayout);
			}
			
			String fullname = StringHelper.escapeHtml(RecruitingHelper.formatFullName(application, getTranslator()));
			uifactory.addStaticTextElement("fullname", "edit.application.name", fullname, personInfosLayout);

			if(recruitingModule.isApplicationPersonGenderEnabled()
					&& recruitingModule.isApplicationDetailsFieldVisible("gender")
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_GENDER)) {
				String gender = person.getGender();
				String genderLabel = RecruitingHelper.formatGender(gender, getLocale());
				uifactory.addStaticTextElement("gender", "edit.application.gender", genderLabel, personInfosLayout);
			}
			
			if(StringHelper.containsNonWhitespace(person.getMaritalStatus())
					&& !"-".equals(person.getMaritalStatus())
					&& recruitingModule.isApplicationPersonMaritalStatusEnabled()
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_MARITAL_STATUS)) {
				String status = translate("edit.application.marital.status." + person.getMaritalStatus());
				uifactory.addStaticTextElement("maritalStatus", "edit.application.marital.status", status, personInfosLayout);
			}
			
			if(StringHelper.containsNonWhitespace(person.getAcademicTitle())
					&& recruitingModule.isApplicationPersonAcademicTitleEnabled()
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_ACADEMIC_TITLE)) {
				uifactory.addStaticTextElement("academicTitle", "edit.application.academicTitle",
						StringHelper.escapeHtml(person.getAcademicTitle()), personInfosLayout);
			}

			if(person.getBirthday() != null
					&& recruitingModule.isApplicationPersonBirthdayEnabled()
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_BIRTHDAY)) {
				String birthday = DateCellRenderer.format(person.getBirthday());
				uifactory.addStaticTextElement("birthday", "edit.application.birthday", birthday, personInfosLayout);
			}
			if(StringHelper.containsNonWhitespace(person.getNationality())
					&& recruitingModule.isApplicationPersonNationalityEnabled()
					&& recruitingModule.isApplicationDetailsFieldVisible("nationality")
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_NATIONALITY)) {
				uifactory.addStaticTextElement("nationality", "edit.application.nationality",
						StringHelper.escapeHtml(person.getNationality()), personInfosLayout);
			}
			if(StringHelper.containsNonWhitespace(person.getAdditionalNationalities())
					&& recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled()
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_ADD_NATIONALITIES)) {
				String addNationalities = RecruitingHelper.beautifyCountriesList(person.getAdditionalNationalities());
				uifactory.addStaticTextElement("add.nationalities", "edit.application.additional.nationalities",
						StringHelper.escapeHtml(addNationalities), personInfosLayout);
			}
			
			if(person.getDisability() != null && person.getDisability().booleanValue()
					&& recruitingModule.isApplicationPersonDisabilityEnabled()
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_DISABILITY)) {
				MultipleSelectionElement disabilityEl = uifactory.addCheckboxesHorizontal("edit.application.disability", personInfosLayout,
						new String[]{ "on" }, new String[]{ "" });
				disabilityEl.setEnabled(false);
				disabilityEl.select("on", true);
			}
		}
	}
	
	private void initEmailAndPhone(Person person, FormItemContainer formLayout) {
		if(person != null) {
			FormLayoutContainer mailPhoneLayout = FormLayoutContainer.createTableCondensedLayout("emailAndPhone", getTranslator());
			mailPhoneLayout.setRootForm(mainForm);
			mailPhoneLayout.setVisible(false);
			mailPhoneLayout.setFormTitle(translate("contact.data"));
			formLayout.add(mailPhoneLayout);
		
			if(StringHelper.containsNonWhitespace(person.getPhone())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_PHONE)) {
				uifactory.addStaticTextElement("phone", "edit.application.phone",
						StringHelper.escapeHtml(person.getPhone()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(person.getMobilePhone())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_MOBILE_PHONE)) {
				uifactory.addStaticTextElement("mobilePhone", "edit.application.mobile.phone",
						StringHelper.escapeHtml(person.getMobilePhone()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(person.getMail())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_EMAIL)) {
				uifactory.addStaticTextElement("mail", "edit.application.mail",
						StringHelper.escapeHtml(person.getMail()), mailPhoneLayout);
				mailPhoneLayout.setVisible(true);
			}
		}
	}
	
	private void initBusinessInformations(BusinessInformations businessInfos, FormItemContainer formLayout) {
		if(businessInfos != null) {
			FormLayoutContainer businessInfosLayout = FormLayoutContainer.createTableCondensedLayout("businessInfos", getTranslator());
			businessInfosLayout.setRootForm(mainForm);
			businessInfosLayout.setVisible(false);
			businessInfosLayout.setFormTitle(translate("business.infos"));
			businessInfosLayout.setElementCssClass("o_sel_details_business_infos_title");
			formLayout.add(businessInfosLayout);
			
			String merged = RecruitingHelper.mergeOrganizationAndAffiliation(businessInfos);
			if(StringHelper.containsNonWhitespace(merged)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)) {
				uifactory.addStaticTextElement("addressOrg", "edit.application.organization",
						StringHelper.escapeHtml(merged), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessInfos.getUnit())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_UNIT)
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT)) {
				uifactory.addStaticTextElement("addressUnit", "edit.application.unit",
						StringHelper.escapeHtml(businessInfos.getUnit()), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessInfos.getCurrentPosition())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_POSITION)
					&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION)) {
				uifactory.addStaticTextElement("addressCurrentPosition", "edit.application.currentPosition",
						StringHelper.escapeHtml(businessInfos.getCurrentPosition()), businessInfosLayout);
				businessInfosLayout.setVisible(true);
			}
		}
	}
	
	private void initPersonalDataAdditionalAttributes(FormItemContainer formLayout) {
		if(personalDataAttributesDelegate.hasSomeValue(application)) {
			FormLayoutContainer additionalInfosLayout = FormLayoutContainer.createTableCondensedLayout("additionalPersonalData", getTranslator());
			additionalInfosLayout.setRootForm(mainForm);
			formLayout.add(additionalInfosLayout);
	
			personalDataAttributesDelegate.initAdditionalAttributesDetails(formLayout, additionalInfosLayout, mainForm,
					application, RecruitingModule.APP_SECTION_PERSON, secCallback, getLocale());
		}
	}
	
	private void initAddress(Address address, FormItemContainer formLayout) {
		if(address != null) {
			FormLayoutContainer adressLayout = FormLayoutContainer.createTableCondensedLayout("singleAddress", getTranslator());
			adressLayout.setRootForm(mainForm);
			adressLayout.setVisible(false);
			
			boolean privateAddress = false;
			if(Address.Type.PRIVATE == address.getType()) {
				privateAddress = true;
				adressLayout.setFormTitle(translate("address.private"));
			} else if (Address.Type.BUSINESS == address.getType()) {
				adressLayout.setFormTitle(translate("address.business"));
			}
			formLayout.add(adressLayout);
			
			if(StringHelper.containsNonWhitespace(address.getAddressLine1())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE1 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE1)) {
				uifactory.addStaticTextElement("addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(address.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine2())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE2 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE2)) {
				uifactory.addStaticTextElement("addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(address.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine3())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE3 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE3)) {
				uifactory.addStaticTextElement("addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(address.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CITY : RecruitingModule.APP_ADDRESS_BUSINESS_CITY)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CODE : RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
				String fullCity = RecruitingHelper.formatZipcodeCity(address);
				if(StringHelper.containsNonWhitespace(fullCity)) {
					uifactory.addStaticTextElement("city", "edit.application.city",
							StringHelper.escapeHtml(fullCity), adressLayout);
					adressLayout.setVisible(true);
				}
			} else if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CITY : RecruitingModule.APP_ADDRESS_BUSINESS_CITY)) {
				if(StringHelper.containsNonWhitespace(address.getCity())) {
					uifactory.addStaticTextElement("city", "edit.application.city",
							StringHelper.escapeHtml(address.getCity()), adressLayout);
					adressLayout.setVisible(true);
				}
			} else if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CODE : RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
				if(StringHelper.containsNonWhitespace(address.getZipCode())) {
					uifactory.addStaticTextElement("city", "edit.application.zipcode",
							StringHelper.escapeHtml(address.getZipCode()), adressLayout);
					adressLayout.setVisible(true);
				}
			}
			
			if(recruitingModule.isApplicationAddressCountryEnabled()
					&& StringHelper.containsNonWhitespace(address.getCountry())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY : RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY)) {
				uifactory.addStaticTextElement("country", "edit.application.country",
						StringHelper.escapeHtml(address.getCountry()), adressLayout);
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

			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine1())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE1)) {
				uifactory.addStaticTextElement("biz_addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(businessAddress.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine2())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE2)) {
				uifactory.addStaticTextElement("biz_addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(businessAddress.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine3())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE3)) {
				uifactory.addStaticTextElement("biz_addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(businessAddress.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			String fullCity = RecruitingHelper.formatZipcodeCity(businessAddress);
			if(StringHelper.containsNonWhitespace(fullCity)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CITY)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
				uifactory.addStaticTextElement("biz_city", "edit.application.city",
						StringHelper.escapeHtml(fullCity), adressLayout);
				adressLayout.setVisible(true);
			} else if(StringHelper.containsNonWhitespace(businessAddress.getCity())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CITY)) {
				uifactory.addStaticTextElement("biz_city", "edit.application.city",
						StringHelper.escapeHtml(businessAddress.getCity()), adressLayout);
				adressLayout.setVisible(true);
			} else if(StringHelper.containsNonWhitespace(businessAddress.getZipCode())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
				uifactory.addStaticTextElement("biz_city", "edit.application.zipcode",
						StringHelper.escapeHtml(businessAddress.getZipCode()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			if(recruitingModule.isApplicationAddressCountryEnabled() && StringHelper.containsNonWhitespace(businessAddress.getCountry())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY)) {
				uifactory.addStaticTextElement("biz_country", "edit.application.country",
						StringHelper.escapeHtml(businessAddress.getCountry()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			if(StringHelper.containsNonWhitespace(businessAddress.getPhone())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_PHONE)) {
				uifactory.addStaticTextElement("biz_phone", "edit.application.business.phone",
						StringHelper.escapeHtml(businessAddress.getPhone()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(businessAddress.getEmail())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_MAIL)) {
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

			if(StringHelper.containsNonWhitespace(address.getAddressLine1())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE1)) {
				uifactory.addStaticTextElement("addressLine1", "edit.application.addressLine1",
						StringHelper.escapeHtml(address.getAddressLine1()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine2())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE2)) {
				uifactory.addStaticTextElement("addressLine2", "edit.application.addressLine2",
						StringHelper.escapeHtml(address.getAddressLine2()), adressLayout);
				adressLayout.setVisible(true);
			}
			if(StringHelper.containsNonWhitespace(address.getAddressLine3())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE3)) {
				uifactory.addStaticTextElement("addressLine3", "edit.application.addressLine3",
						StringHelper.escapeHtml(address.getAddressLine3()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			String fullCity = RecruitingHelper.formatZipcodeCity(address);
			if(StringHelper.containsNonWhitespace(fullCity)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CITY)
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CODE)) {
				uifactory.addStaticTextElement("city", "edit.application.city",
						StringHelper.escapeHtml(fullCity), adressLayout);
				adressLayout.setVisible(true);
			} else if(StringHelper.containsNonWhitespace(address.getCity())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CITY)) {
				uifactory.addStaticTextElement("city", "edit.application.city",
						StringHelper.escapeHtml(address.getCity()), adressLayout);
				adressLayout.setVisible(true);
			} else if(StringHelper.containsNonWhitespace(address.getZipCode())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CODE)) {
				uifactory.addStaticTextElement("city", "edit.application.zipcode",
						StringHelper.escapeHtml(address.getZipCode()), adressLayout);
				adressLayout.setVisible(true);
			}
			
			if(recruitingModule.isApplicationAddressCountryEnabled()
					&& StringHelper.containsNonWhitespace(address.getCountry())
					&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY)) {
				uifactory.addStaticTextElement("country", "edit.application.country",
						StringHelper.escapeHtml(address.getCountry()), adressLayout);
				adressLayout.setVisible(true);
			}
		}
	}
	
	private void initAcademicalBackground(FormItemContainer formLayout) {
		boolean visible = false;
		
		if (recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			FormLayoutContainer backgroundLayout = FormLayoutContainer.createTableCondensedLayout("background", getTranslator());
			backgroundLayout.setRootForm(mainForm);
			formLayout.add(backgroundLayout);
			backgroundLayout.setVisible(false);
			
			AcademicalBackground background = application.getAcademicalBackground();
			if(background != null) {
				
				if(background.getNumberOfOriginalPublications() != null && background.getNumberOfOriginalPublications().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS)) {
					uifactory.addStaticTextElement("numOfPublications", "edit.application.numberOfOriginalPublications",
							Integer.toString(background.getNumberOfOriginalPublications()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(background.getNumberOfFirstAuthorships() != null && background.getNumberOfFirstAuthorships().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS)) {
					uifactory.addStaticTextElement("numOfFirstAuthorships", "edit.application.numberOfFirstAuthorships",
							Integer.toString(background.getNumberOfFirstAuthorships()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(background.getNumberOfLastAuthorships() != null && background.getNumberOfLastAuthorships().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS)) {
					uifactory.addStaticTextElement("numOfLastAuthorships", "edit.application.numberOfLastAuthorships",
							Integer.toString(background.getNumberOfLastAuthorships()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(background.getCitations() != null && background.getCitations().intValue() > 0
						&& recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_CITATIONS)) {
					uifactory.addStaticTextElement("citations", "edit.application.citations",
							Integer.toString(background.getCitations()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(background.getImpactFactor() != null
						&& recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_IMPACT_FACTOR)) {
					uifactory.addStaticTextElement("impactFactor", "edit.application.impactFactor",
							RecruitingHelper.formatFactor(background.getImpactFactor()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(background.getHFactor() != null
						&& recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HFACTORY)) {
					uifactory.addStaticTextElement("hFactor", "edit.application.hFactor",
							RecruitingHelper.formatFactor(background.getHFactor()), backgroundLayout);
					backgroundLayout.setVisible(true);
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
						&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
					String highestDegree = RecruitingHelper.formatHighestDegree(application, excludedAttributesList, getTranslator());
					if(StringHelper.containsNonWhitespace(highestDegree)) {
						uifactory.addStaticTextElement("highestdegree", "edit.application.highestdegree",
								StringHelper.escapeHtml(highestDegree), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				visible |= backgroundLayout.isVisible();
				
				// new layout
				backgroundLayout = FormLayoutContainer.createTableCondensedLayout("academic.age", getTranslator());
				backgroundLayout.setRootForm(mainForm);
				formLayout.add(backgroundLayout);
				backgroundLayout.setFormTitle(translate("edit.application.highestdegreeWorkedSince"));
				backgroundLayout.setVisible(false);
				
				if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE)) {
					String workedInAcademia = application.getAcademicalBackground().getWorkedInAcademiaSince();
					if(StringHelper.containsNonWhitespace(workedInAcademia)) {
						uifactory.addStaticTextElement("workedInAcademiaWS", "edit.application.workedInAcademiaSince.label",
								StringHelper.escapeHtml(workedInAcademia), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE)) {
					String workedOutAcademia = application.getAcademicalBackground().getWorkedOutAcademiaSince();
					if(StringHelper.containsNonWhitespace(workedOutAcademia)) {
						uifactory.addStaticTextElement("workedOutAcademiaWS", "edit.application.workedOutAcademiaSince.label",
								StringHelper.escapeHtml(workedOutAcademia), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE)) {
					String workedOutAcademiaCare = application.getAcademicalBackground().getWorkedOutAcademiaCareSince();
					if(StringHelper.containsNonWhitespace(workedOutAcademiaCare)) {
						uifactory.addStaticTextElement("workedOutAcademiaCareWS", "edit.application.workedOutAcademiaCareSince.label",
								StringHelper.escapeHtml(workedOutAcademiaCare), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_CAREER_DESCRIPTION)) {
					String careerDescription = application.getAcademicalBackground().getCareerDescription();
					if(StringHelper.containsNonWhitespace(careerDescription)) {
						careerDescription = Formatter.escWithBR(careerDescription).toString();
						careerDescription = StringHelper.xssScan(careerDescription);
						uifactory.addStaticTextElement("careerDescriptionWS", "edit.application.careerDescription.details.label",
								careerDescription, backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				visible |= backgroundLayout.isVisible();
				
				// dissertation
				backgroundLayout = FormLayoutContainer.createTableCondensedLayout("dissertation.and.co", getTranslator());
				backgroundLayout.setRootForm(mainForm);
				formLayout.add(backgroundLayout);
				backgroundLayout.setVisible(false);

				if((recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
						|| recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
						|| recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled())
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
					String dissertation = RecruitingHelper.formatDissertation(application, getLocale());
					if(StringHelper.containsNonWhitespace(dissertation)) {
						uifactory.addStaticTextElement("dissertation", "edit.application.dissertation",
								StringHelper.escapeHtml(dissertation), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD1)) {
					String keyword1 = application.getAcademicalBackground().getDissertationKeyword1();
					if(StringHelper.containsNonWhitespace(keyword1)) {
						uifactory.addStaticTextElement("dissertation.keyword1", "edit.application.dissertationkeyword1",
								StringHelper.escapeHtml(keyword1), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD2)) {
					String keyword2 = application.getAcademicalBackground().getDissertationKeyword2();
					if(StringHelper.containsNonWhitespace(keyword2)) {
						uifactory.addStaticTextElement("dissertation.keyword2", "edit.application.dissertationkeyword2",
								StringHelper.escapeHtml(keyword2), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD3)) {
					String keyword3 = application.getAcademicalBackground().getDissertationKeyword3();
					if(StringHelper.containsNonWhitespace(keyword3)) {
						uifactory.addStaticTextElement("dissertation.keyword3", "edit.application.dissertationkeyword3",
								StringHelper.escapeHtml(keyword3), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				
				// habilitation
				if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HABILITATION)) {
					String habilitation = RecruitingHelper.formatHabilitation(application);
					if(StringHelper.containsNonWhitespace(habilitation)) {
						uifactory.addStaticTextElement("habilitation", "edit.application.habilitation",
								StringHelper.escapeHtml(habilitation), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				
				if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()
						&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_ORCID)) {
					String orcid = background.getOrcid();
					if(StringHelper.containsNonWhitespace(orcid)) {
						uifactory.addStaticTextElement("orcid", "edit.application.orcid",
								StringHelper.escapeHtml(orcid), backgroundLayout);
						backgroundLayout.setVisible(true);
					}
				}
				visible |= backgroundLayout.isVisible();
				
				visible |= academicalBackgroundAttributesDelegate.initAdditionalAttributesDetails(formLayout, backgroundLayout, mainForm,
						application, RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, secCallback, getLocale()).hasValue();
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("hasBackgroundValues", Boolean.valueOf(visible));
		}
	}
	
	private void initCustomStepAttributes(FormItemContainer formLayout) {
		List<CustomSet> customTabsIds = new ArrayList<>();
		for(ApplicationAttributesDelegate customAttributesDelegate:customAttributesDelegateList) {
			Tab tab = customAttributesDelegate.tab().tab();
			TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
			if(!tabConfiguration.isDisabled() && customAttributesDelegate.hasSomeValue(application)) {
				String id = "custom_" + tab.name();
				String title = tabConfiguration.getTitle(getLocale());
				
				FormLayoutContainer customLayout = FormLayoutContainer.createTableCondensedLayout(id, getTranslator());
				customLayout.setRootForm(mainForm);
				formLayout.add(customLayout);
				
				Details details = customAttributesDelegate.initAdditionalAttributesDetails(formLayout, customLayout, mainForm,
						application, tab.name(), secCallback, getLocale());
				if(details.hasValue()) {
					if(!customLayout.getFormItems().iterator().hasNext()) {
						customLayout.setVisible(false);
					}
					customTabsIds.add(new CustomSet(customLayout.getComponent().getComponentName(), title, tab.name(), details.containers()));
				}
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("customTabsIds", customTabsIds);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class CustomSet {
		
		private final String componentName;
		private final String title;
		private final String tabName;
		private final List<FormLayoutContainer> containers;
		
		public CustomSet(String componentName, String title, String tabName, List<FormLayoutContainer> containers) {
			this.componentName = componentName;
			this.title = title;
			this.tabName = tabName;
			this.containers = containers;
		}
		
		public String componentName() {
			return componentName;
		}
		
		public String title() {
			return title;
		}
		
		public String tabName() {
			return tabName;
		}
		
		public List<FormLayoutContainer> containers() {
			return containers;
		}
	}
}
