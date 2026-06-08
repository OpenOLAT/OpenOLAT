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
package org.olat.modules.selectus.ui.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.olat.modules.selectus.AddressOption;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.BusinessAddress;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Initial date: 30 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationXMLV2 {

	private final Position position;
	private final Application application;
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

	private final Translator translator;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public ApplicationXMLV2(Position position, Application application, RecruitingPositionSecurityCallback secCallback, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		
		this.position = position;
		this.application = application;
		this.translator = translator;
		this.secCallback = secCallback;
		excludedAttributesList = position.getExcludedAttributesList();
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> customTabs = position.getCustomEnabledTabsList();
			for(Tab customTab:customTabs) {
				customAttributesDelegateList.add(new ApplicationAttributesDelegate(customTab.attributesTab()));
			}
		}
	}
	
	protected Document appendCoverDocument(Document doc, Element rootEl) {
		rootEl.setAttribute("output-date", getOutputDate());
		appendPositionDataDOM(doc, rootEl);
		appendApplicationDataDOM(doc, rootEl);
		return doc;
	}
	
	private void appendPositionDataDOM(Document doc, Element rootEl) {
		Element positionEl = (Element)rootEl.appendChild(doc.createElement("position"));
		String positionTitle = position.getPositionTitle(translator.getLocale());
		if(!StringHelper.containsNonWhitespace(positionTitle)) {
			positionTitle = position.getPositionTitle();
		}
		if(!StringHelper.containsNonWhitespace(positionTitle)) {
			positionTitle = position.getPositionTitleDe();
		}
		addStaticTextElement(doc, positionEl, "positionTitle", "edit.position_title", positionTitle);
		addStaticTextElement(doc, positionEl, "planingsNumber", "edit.position_id", position.getPlaningsNumber());
	}
	
	private void appendApplicationDataDOM(Document doc, Element rootEl) {
		Element applicationEl = (Element)rootEl.appendChild(doc.createElement("application"));
		
		// Personal informations
		Element personalInfosEl = createSection(doc, applicationEl, "person", "personal_information");
		addStaticTextElement(doc, personalInfosEl, "id", "edit.position_id", application.getId().toString());
		appendPersonalInformations(doc, personalInfosEl, application.getPerson());
		
		// Contact informations
		Element contactInfosEl = createSection(doc, applicationEl, "contact", "contact.data");
		appendEmailAndPhone(doc, contactInfosEl, application.getPerson());
		
		// Business informations
		Element businessInfosEl = createSection(doc, applicationEl, "businessInformations", "business.infos");
		appendBusinessInformations(doc, businessInfosEl, application.getBusinessInformations());
		
		// Addresses
		Address address = application.getAddress();
		AddressOption privateOption = recruitingModule.getApplicationAddressPrivateOption();
		AddressOption businessOption = recruitingModule.getApplicationAddressBusinessOption();
		if(AddressOption.xor.equals(privateOption) && AddressOption.xor.equals(businessOption)) {
			Element addressEl = (Element)applicationEl.appendChild(doc.createElement("address"));
			appendAddress(doc, addressEl, address);
		} else {
			if(AddressOption.enabled.equals(businessOption) || AddressOption.optional.equals(businessOption)) {
				Element businessAddressEl = (Element)applicationEl.appendChild(doc.createElement("businessAddress"));
				appendBusinessAddress(doc, businessAddressEl, application.getBusinessAddress());
			}
			if(AddressOption.enabled.equals(privateOption) || AddressOption.optional.equals(privateOption)) {
				Element addressEl = (Element)applicationEl.appendChild(doc.createElement("address"));
				appendPrivateAddress(doc, addressEl, address);
			}
		}

		Element personalCustomAttributesEl = (Element)applicationEl.appendChild(doc.createElement("personalCustomAttributes"));
		personalDataAttributesDelegate.appendAdditionalAttributesDetails(doc, personalCustomAttributesEl,
				application, RecruitingModule.APP_SECTION_PERSON, secCallback, translator);
		
		// Academical background
		if(recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) {
			Element academicalBackgroundEl = createSection(doc, applicationEl, "academicalBackground", "academical_background");
			appendAcademicalBackground(doc, academicalBackgroundEl, application.getAcademicalBackground());
		}
		
		// Project
		if(position.isApplicationProject() && recruitingModule.isApplicationProjectEnabled()) {
			Element projectEl = createSection(doc, applicationEl, "project", "project");
			appendProject(doc, projectEl, application.getProject());
		}
		
		// Custom steps
		Element customStepsEl = (Element)applicationEl.appendChild(doc.createElement("customSteps"));
		appendCustomStepAttributes(doc, customStepsEl);
		
		// Documents
		Element documentsEl = (Element)applicationEl.appendChild(doc.createElement("documents-v2"));
		appendAttachments(doc, documentsEl);
		appendRefereesLetters( doc, documentsEl);
	}
	
	private void appendCustomStepAttributes(Document doc, Element parentEl) {
		for(ApplicationAttributesDelegate customAttributesDelegate:customAttributesDelegateList) {
			Tab tab = customAttributesDelegate.tab().tab();
			TabConfiguration tabConfiguration = position.getTabConfiguration(tab);

			if(!tabConfiguration.isDisabled() && customAttributesDelegate.hasSomeValue(application)) {
				Element customStepEl = (Element)parentEl.appendChild(doc.createElement("customStep"));
				
				String id = "custom_" + tab.name();
				customStepEl.setAttribute("id", id);
				String title = tabConfiguration.getTitle(translator.getLocale());
				customStepEl.setAttribute("title", title);
				
				customAttributesDelegate.appendAdditionalAttributesDetails(doc, customStepEl,
						application, tab.name(), secCallback, translator);
			}
		}
	}
	
	private void appendAttachments(Document document, Element rootEl) {
		Element attachmentsEl = createSection(document, rootEl, "attachments", "edit.application.document.title.cover");

		Set<String> available = position.getAvailableDocuments();
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(!secCallback.canViewDocument(doc)
					|| DocumentEnum.combined == doc
					|| !available.contains(doc.name())) {
				continue;
			}
			
			Attachment attachment = doc.path(application);
			if(attachment != null) {
				Element documentEl = (Element)attachmentsEl.appendChild(document.createElement("document"));
				String docName = position.getDocumentName(docOption.getDoc(), translator.getLocale());
				if(!StringHelper.containsNonWhitespace(docName)) {
					docName = translator.translate(docOption.getDoc().i18nKey());
				}
				documentEl.setAttribute("name", docName);
				attachmentsEl.setAttribute("visible", "true");
			}
		}
	}
	
	private void appendRefereesLetters(Document document, Element rootEl) {
		Element lettersEl = createSection(document, rootEl, "refereesLetters", "application.recommendations.document.explain.cover");
		
		if(position.isRefereeRecommendationEnabled() && secCallback.canViewReferencesOfReferees()) {
			List<Reference> allReferences = recruitingService.getApplicationReferences(application, null);
			for(Reference reference:allReferences) {
				if(reference.getReferenceType() == ReferenceType.recommendation
						&& reference.getReferenceStatus() == ReferenceStatus.submitted
						&& reference.getRequestStatus() != ReferenceRequestStatus.declined) {
					Element documentEl = (Element)lettersEl.appendChild(document.createElement("document"));
					String docName = salutationGenerator.getTitleFullname(reference, translator.getLocale());
					documentEl.setAttribute("name", docName);
					if(StringHelper.containsNonWhitespace(reference.getInstitution())) {
						documentEl.setAttribute("institution", reference.getInstitution());
					}
					lettersEl.setAttribute("visible", "true");
				}
			}
		}
	}	
	
	
	private Element createSection(Document doc, Element parentEl, String element, String titleI18nKey) {
		Element sectionEl = (Element)parentEl.appendChild(doc.createElement(element));
		sectionEl.setAttribute("title", translator.translate(titleI18nKey));
		return sectionEl;
	}
	
	private void appendPersonalInformations(Document doc, Element parentEl, Person person) {
		if(person == null) return;
			
		String id = application.getId() == null ? "" : application.getId().toString();
		addStaticTextElement(doc, parentEl, "appId", "edit.application.id.long", id);
		
		String fullname = StringHelper.escapeHtml(RecruitingHelper.formatFullName(application, translator));
		addStaticTextElement(doc, parentEl, "fullname", "edit.application.name", fullname);

		if(recruitingModule.isApplicationPersonGenderEnabled()
				&& recruitingModule.isApplicationDetailsFieldVisible("gender")
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_GENDER)) {
			String gender = person.getGender();
			String genderLabel = RecruitingHelper.formatGender(gender, translator.getLocale());
			addStaticTextElement(doc, parentEl, "gender", "edit.application.gender", genderLabel);
		}
		
		if(StringHelper.containsNonWhitespace(person.getMaritalStatus())
				&& !"-".equals(person.getMaritalStatus())
				&& recruitingModule.isApplicationPersonMaritalStatusEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_MARITAL_STATUS)) {
			String status = translator.translate("edit.application.marital.status." + person.getMaritalStatus());
			addStaticTextElement(doc, parentEl, "maritalStatus", "edit.application.marital.status", status);
		}
		
		if(StringHelper.containsNonWhitespace(person.getAcademicTitle())
				&& recruitingModule.isApplicationPersonAcademicTitleEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_ACADEMIC_TITLE)) {
			addStaticTextElement(doc, parentEl, "academicTitle", "edit.application.academicTitle", person.getAcademicTitle());
		}

		if(person.getBirthday() != null
				&& recruitingModule.isApplicationPersonBirthdayEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_BIRTHDAY)) {
			String birthday = DateCellRenderer.format(person.getBirthday());
			addStaticTextElement(doc, parentEl, "birthday", "edit.application.birthday", birthday);
		}
		if(StringHelper.containsNonWhitespace(person.getNationality())
				&& recruitingModule.isApplicationPersonNationalityEnabled()
				&& recruitingModule.isApplicationDetailsFieldVisible("nationality")
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_NATIONALITY)) {
			addStaticTextElement(doc, parentEl, "nationality", "edit.application.nationality", person.getNationality());
		}
		if(StringHelper.containsNonWhitespace(person.getAdditionalNationalities())
				&& recruitingModule.isApplicationPersonAdditionalNationalitiesEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_ADD_NATIONALITIES)) {
			String addNationalities = RecruitingHelper.beautifyCountriesList(person.getAdditionalNationalities());
			addStaticTextElement(doc, parentEl, "addNationalities", "edit.application.additional.nationalities", addNationalities);
		}
		
		if(person.getDisability() != null && person.getDisability().booleanValue()
				&& recruitingModule.isApplicationPersonDisabilityEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_DISABILITY)) {
			addStaticTextElement(doc, parentEl, "disability", "edit.application.disability",
					translator.translate("yes"));
		}
	}
	
	private void appendEmailAndPhone(Document doc, Element parentEl, Person person) {
		if(person == null) return;

		if(StringHelper.containsNonWhitespace(person.getPhone())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_PHONE)) {
			addStaticTextElement(doc, parentEl, "phone", "edit.application.phone", person.getPhone());
		}
		if(StringHelper.containsNonWhitespace(person.getMobilePhone())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_MOBILE_PHONE)) {
			addStaticTextElement(doc, parentEl, "mobilePhone", "edit.application.mobile.phone", person.getMobilePhone());
		}
		if(StringHelper.containsNonWhitespace(person.getMail())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_PERSON_EMAIL)) {
			addStaticTextElement(doc, parentEl, "mail", "edit.application.mail", person.getMail());
		}
	}
	
	private void appendBusinessInformations(Document doc, Element parentEl, BusinessInformations businessInfos) {
		if(businessInfos == null) return;
	
		String merged = RecruitingHelper.mergeOrganizationAndAffiliation(businessInfos);
		if(StringHelper.containsNonWhitespace(merged)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)
				&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_ORGANISATION)) {
			addStaticTextElement(doc, parentEl, "addressOrg", "edit.application.organization", merged);
		}
		if(StringHelper.containsNonWhitespace(businessInfos.getUnit())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_UNIT)
				&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_UNIT)) {
			addStaticTextElement(doc, parentEl, "addressUnit", "edit.application.unit", businessInfos.getUnit());
		}
		if(StringHelper.containsNonWhitespace(businessInfos.getCurrentPosition())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_BUSINESS_INFOS_POSITION)
				&& !excludedAttributesList.contains(RecruitingModule.APP_BUSINESS_INFOS_POSITION)) {
			addStaticTextElement(doc, parentEl, "addressCurrentPosition", "edit.application.currentPosition", businessInfos.getCurrentPosition());
		}
	}
	
	private void appendAddress(Document doc, Element parentEl, Address address) {
		if(address == null) return;
	
		boolean privateAddress = false;
		if(Address.Type.PRIVATE == address.getType()) {
			privateAddress = true;
			parentEl.setAttribute("title", translator.translate("address.private"));
		} else if (Address.Type.BUSINESS == address.getType()) {
			parentEl.setAttribute("title", translator.translate("address.business"));
		}
			
		if(StringHelper.containsNonWhitespace(address.getAddressLine1())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE1 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE1)) {
			addStaticTextElement(doc, parentEl, "addressLine1", "edit.application.addressLine1", address.getAddressLine1());
		}
		if(StringHelper.containsNonWhitespace(address.getAddressLine2())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE2 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE2)) {
			addStaticTextElement(doc, parentEl, "addressLine2", "edit.application.addressLine2", address.getAddressLine2());
		}
		if(StringHelper.containsNonWhitespace(address.getAddressLine3())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_LINE3 : RecruitingModule.APP_ADDRESS_BUSINESS_LINE3)) {
			addStaticTextElement(doc, parentEl, "addressLine3", "edit.application.addressLine3", address.getAddressLine3());
		}
		
		if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CITY : RecruitingModule.APP_ADDRESS_BUSINESS_CITY)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CODE : RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
			String fullCity = RecruitingHelper.formatZipcodeCity(address);
			if(StringHelper.containsNonWhitespace(fullCity)) {
				addStaticTextElement(doc, parentEl, "city", "edit.application.city", fullCity);
			}
		} else if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CITY : RecruitingModule.APP_ADDRESS_BUSINESS_CITY)) {
			if(StringHelper.containsNonWhitespace(address.getCity())) {
				addStaticTextElement(doc, parentEl, "city", "edit.application.city", address.getCity());
			}
		} else if(secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_CODE : RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
			if(StringHelper.containsNonWhitespace(address.getZipCode())) {
				addStaticTextElement(doc, parentEl, "city", "edit.application.zipcode", address.getZipCode());
			}
		}
		
		if(recruitingModule.isApplicationAddressCountryEnabled()
				&& StringHelper.containsNonWhitespace(address.getCountry())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, privateAddress ? RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY : RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY)) {
			addStaticTextElement(doc, parentEl, "country", "edit.application.country", address.getCountry());
		}
	}
	
	private void appendBusinessAddress(Document doc, Element parentEl, BusinessAddress businessAddress) {
		if(businessAddress == null) return;

		parentEl.setAttribute("title", translator.translate("address.business"));

		if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine1())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE1)) {
			addStaticTextElement(doc, parentEl, "biz_addressLine1", "edit.application.addressLine1", businessAddress.getAddressLine1());
		}
		if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine2())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE2)) {
			addStaticTextElement(doc, parentEl, "biz_addressLine2", "edit.application.addressLine2", businessAddress.getAddressLine2());
		}
		if(StringHelper.containsNonWhitespace(businessAddress.getAddressLine3())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_LINE3)) {
			addStaticTextElement(doc, parentEl, "biz_addressLine3", "edit.application.addressLine3", businessAddress.getAddressLine3());
		}
		
		String fullCity = RecruitingHelper.formatZipcodeCity(businessAddress);
		if(StringHelper.containsNonWhitespace(fullCity)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CITY)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
			addStaticTextElement(doc, parentEl, "biz_city", "edit.application.city", fullCity);
		} else if(StringHelper.containsNonWhitespace(businessAddress.getCity())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CITY)) {
			addStaticTextElement(doc, parentEl, "biz_city", "edit.application.city", businessAddress.getCity());
		} else if(StringHelper.containsNonWhitespace(businessAddress.getZipCode())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_CODE)) {
			addStaticTextElement(doc, parentEl, "biz_city", "edit.application.zipcode", businessAddress.getZipCode());
		}
		
		if(recruitingModule.isApplicationAddressCountryEnabled() && StringHelper.containsNonWhitespace(businessAddress.getCountry())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_COUNTRY)) {
			addStaticTextElement(doc, parentEl, "biz_country", "edit.application.country", businessAddress.getCountry());
		}
		
		if(StringHelper.containsNonWhitespace(businessAddress.getPhone())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_PHONE)) {
			addStaticTextElement(doc, parentEl, "biz_phone", "edit.application.business.phone", businessAddress.getPhone());
		}
		if(StringHelper.containsNonWhitespace(businessAddress.getEmail())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_BUSINESS_MAIL)) {
			addStaticTextElement(doc, parentEl, "biz_mail", "edit.application.business.mail", businessAddress.getEmail());
		}
	}

	private void appendPrivateAddress(Document doc, Element parentEl, Address address) {
		if(address == null) return;
		
		parentEl.setAttribute("title", translator.translate("address.private"));

		if(StringHelper.containsNonWhitespace(address.getAddressLine1())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE1)) {
			addStaticTextElement(doc, parentEl, "addressLine1", "edit.application.addressLine1", address.getAddressLine1());
		}
		if(StringHelper.containsNonWhitespace(address.getAddressLine2())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE2)) {
			addStaticTextElement(doc, parentEl, "addressLine2", "edit.application.addressLine2", address.getAddressLine2());
		}
		if(StringHelper.containsNonWhitespace(address.getAddressLine3())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_LINE3)) {
			addStaticTextElement(doc, parentEl, "addressLine3", "edit.application.addressLine3", address.getAddressLine3());
		}
		
		String fullCity = RecruitingHelper.formatZipcodeCity(address);
		if(StringHelper.containsNonWhitespace(fullCity)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CITY)
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CODE)) {
			addStaticTextElement(doc, parentEl, "city", "edit.application.city", fullCity);
		} else if(StringHelper.containsNonWhitespace(address.getCity())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CITY)) {
			addStaticTextElement(doc, parentEl, "city", "edit.application.city", address.getCity());
		} else if(StringHelper.containsNonWhitespace(address.getZipCode())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_CODE)) {
			addStaticTextElement(doc, parentEl, "city", "edit.application.zipcode", address.getZipCode());
		}
		
		if(recruitingModule.isApplicationAddressCountryEnabled()
				&& StringHelper.containsNonWhitespace(address.getCountry())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PERSON, RecruitingModule.APP_ADDRESS_PRIVATE_COUNTRY)) {
			addStaticTextElement(doc, parentEl, "country", "edit.application.country", address.getCountry());
		}
	}
	
	private void appendAcademicalBackground(Document doc, Element parentEl, AcademicalBackground background) {
		if (background == null || !recruitingModule.isApplicationAcademicalBackgroundEnabled(position)) return;


		Element highestDegreeEl = (Element)parentEl.appendChild(doc.createElement("highestDegree"));
		if(background.getNumberOfOriginalPublications() != null && background.getNumberOfOriginalPublications().intValue() > 0
				&& recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_ORIGINAL_PUBLICATIONS)) {
			addStaticTextElement(doc, highestDegreeEl, "numOfPublications", "edit.application.numberOfOriginalPublications", background.getNumberOfOriginalPublications());
		}
		
		if(background.getNumberOfFirstAuthorships() != null && background.getNumberOfFirstAuthorships().intValue() > 0
				&& recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_FIRST_AUTHORSHIPS)) {
			addStaticTextElement(doc, highestDegreeEl, "numOfFirstAuthorships", "edit.application.numberOfFirstAuthorships", background.getNumberOfFirstAuthorships());
		}
		
		if(background.getNumberOfLastAuthorships() != null && background.getNumberOfLastAuthorships().intValue() > 0
				&& recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_NUM_OF_LAST_AUTHORSHIPS)) {
			addStaticTextElement(doc, highestDegreeEl, "numOfLastAuthorships", "edit.application.numberOfLastAuthorships", background.getNumberOfLastAuthorships());
		}
		
		if(background.getCitations() != null && background.getCitations().intValue() > 0
				&& recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_CITATIONS)) {
			addStaticTextElement(doc, highestDegreeEl, "citations", "edit.application.citations", background.getCitations());
		}
		
		if(background.getImpactFactor() != null
				&& recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_IMPACT_FACTOR)) {
			addStaticTextElement(doc, highestDegreeEl, "impactFactor", "edit.application.impactFactor", background.getImpactFactor());
		}
		
		if(background.getHFactor() != null
				&& recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HFACTORY)) {
			addStaticTextElement(doc, highestDegreeEl, "hFactor", "edit.application.hFactor", background.getHFactor());
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)) {
			String highestDegree = RecruitingHelper.formatHighestDegree(application, excludedAttributesList, translator);
			if(StringHelper.containsNonWhitespace(highestDegree)) {
				addStaticTextElement(doc, highestDegreeEl, "highestdegree", "edit.application.highestdegree", highestDegree);
			}
		}
		
		Element workedSinceEl = createSection(doc, parentEl, "highestDegreeWorkedSince", "edit.application.highestdegreeWorkedSince");

		if(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_IN_ACADEMIA_SINCE)) {
			String workedInAcademia = application.getAcademicalBackground().getWorkedInAcademiaSince();
			if(StringHelper.containsNonWhitespace(workedInAcademia)) {
				addStaticTextElement(doc, workedSinceEl, "workedInAcademiaWS", "edit.application.workedInAcademiaSince.label", workedInAcademia);
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_SINCE)) {
			String workedOutAcademia = application.getAcademicalBackground().getWorkedOutAcademiaSince();
			if(StringHelper.containsNonWhitespace(workedOutAcademia)) {
				addStaticTextElement(doc, workedSinceEl, "workedOutAcademiaWS", "edit.application.workedOutAcademiaSince.label", workedOutAcademia);
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_WORKED_OUT_ACADEMIA_CARE_SINCE)) {
			String workedOutAcademiaCare = application.getAcademicalBackground().getWorkedOutAcademiaCareSince();
			if(StringHelper.containsNonWhitespace(workedOutAcademiaCare)) {
				addStaticTextElement(doc, workedSinceEl, "workedOutAcademiaCareWS", "edit.application.workedOutAcademiaCareSince.label", workedOutAcademiaCare);
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_CAREER_DESCRIPTION)) {
			String careerDescription = application.getAcademicalBackground().getCareerDescription();
			if(StringHelper.containsNonWhitespace(careerDescription)) {
				addStaticTextElement(doc, workedSinceEl, "careerDescriptionWS", "edit.application.careerDescription.details.label", careerDescription);
			}
		}
		
		// dissertation

		Element dissertationEl = (Element)parentEl.appendChild(doc.createElement("dissertation"));	
		if((recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION)) {
			String dissertation = RecruitingHelper.formatDissertation(application, translator.getLocale());
			if(StringHelper.containsNonWhitespace(dissertation)) {
				addStaticTextElement(doc, dissertationEl, "dissertation", "edit.application.dissertation", dissertation);
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD1)) {
			String keyword1 = application.getAcademicalBackground().getDissertationKeyword1();
			if(StringHelper.containsNonWhitespace(keyword1)) {
				addStaticTextElement(doc, dissertationEl, "dissertationKeyword1", "edit.application.dissertationkeyword1", keyword1);
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD2)) {
			String keyword2 = application.getAcademicalBackground().getDissertationKeyword2();
			if(StringHelper.containsNonWhitespace(keyword2)) {
				addStaticTextElement(doc, dissertationEl, "dissertationKeyword2", "edit.application.dissertationkeyword2", keyword2);
			}
		}
		if(recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_DISSERTATION_KEYWORD3)) {
			String keyword3 = application.getAcademicalBackground().getDissertationKeyword3();
			if(StringHelper.containsNonWhitespace(keyword3)) {
				addStaticTextElement(doc, dissertationEl, "dissertationKeyword3", "edit.application.dissertationkeyword3", keyword3);
			}
		}
		
		// habilitation
		if(recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_HABILITATION)) {
			String habilitation = RecruitingHelper.formatHabilitation(application);
			if(StringHelper.containsNonWhitespace(habilitation)) {
				addStaticTextElement(doc, dissertationEl, "habilitation", "edit.application.habilitation", habilitation);
			}
		}
		
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, RecruitingModule.APP_ACADEMIC_ORCID)) {
			String orcid = background.getOrcid();
			if(StringHelper.containsNonWhitespace(orcid)) {
				addStaticTextElement(doc, dissertationEl, "orcid", "edit.application.orcid", orcid);
			}
		}
		
		academicalBackgroundAttributesDelegate.appendAdditionalAttributesDetails(doc, parentEl,
				application, RecruitingModule.APP_SECTION_ACADEMICAL_BACKGROUND, secCallback, translator);
	}
	
	private void appendProject(Document doc, Element parentEl, Project project) {
		if(project == null) return ;

		if(recruitingModule.isApplicationProjectTitleEnabled() && StringHelper.containsNonWhitespace(project.getTitle())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_TITLE)) {
			addStaticTextElement(doc, parentEl, "projectTitle", "edit.application.project.title", project.getTitle());
		}
		if(recruitingModule.isApplicationProjectAcronymEnabled() && StringHelper.containsNonWhitespace(project.getAcronym())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_ACRONYM)) {
			addStaticTextElement(doc, parentEl, "projectAcronym", "edit.application.project.acronym", project.getAcronym());
		}
		if(recruitingModule.isApplicationProjectKeywordsEnabled() && StringHelper.containsNonWhitespace(project.getKeywords())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_KEYWORDS)) {
			addStaticTextElement(doc, parentEl, "projectKeywords", "edit.application.project.keywords", project.getKeywords());
		}
		if(recruitingModule.isApplicationProjectDisciplinesEnabled() && StringHelper.containsNonWhitespace(project.getDisciplines())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DISCIPLINES)) {
			addStaticTextElement(doc, parentEl, "projectDisciplines", "edit.application.project.disciplines", project.getDisciplines());
		}
		if(recruitingModule.isApplicationProjectStartDateEnabled() && project.getStartDate() != null
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_START_DATE)) {
			String startDate = DateCellRenderer.format(project.getStartDate());
			addStaticTextElement(doc, parentEl, "projectStartDate", "edit.application.project.start.date", startDate);
		}
		if(recruitingModule.isApplicationProjectDurationEnabled() && StringHelper.containsNonWhitespace(project.getDuration())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DURATION)) {
			addStaticTextElement(doc, parentEl, "projectDuration", "edit.application.project.duration", project.getDuration());
		}
		if(recruitingModule.isApplicationProjectFinancialImpact1Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact1())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_1)) {
			appendFinancialImpact(doc, parentEl, 1, project.getFinancialImpact1());
		}
		if(recruitingModule.isApplicationProjectFinancialImpact2Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact2())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_2)) {
			appendFinancialImpact(doc, parentEl, 2, project.getFinancialImpact2());
		}
		if(recruitingModule.isApplicationProjectFinancialImpact3Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact3())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_3)) {
			appendFinancialImpact(doc, parentEl, 3, project.getFinancialImpact3());
		}
		if(recruitingModule.isApplicationProjectFinancialImpact4Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact4())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_4)) {
			appendFinancialImpact(doc, parentEl, 4, project.getFinancialImpact4());
		}
		if(recruitingModule.isApplicationProjectFinancialImpact5Enabled() && StringHelper.containsNonWhitespace(project.getFinancialImpact5())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_FINANCIAL_IMPACT_5)) {
			appendFinancialImpact(doc, parentEl, 5, project.getFinancialImpact5());
		}
		if(recruitingModule.isApplicationProjectDescriptionEnabled() && StringHelper.containsNonWhitespace(project.getDescription())
				&& secCallback.canViewField(RecruitingModule.APP_SECTION_PROJECT, RecruitingModule.APP_PROJECT_DESCRIPTION)) {
			addStaticTextElement(doc, parentEl, "projectDescription", "edit.application.project.description", project.getDescription());
		}
		
		projectAttributesDelegate.appendAdditionalAttributesDetails(doc, parentEl,
				application, RecruitingModule.APP_SECTION_PROJECT, secCallback, translator);
	}
	
	private void appendFinancialImpact(Document doc, Element parentEl, int num, String text) {
		String unit = translator.translate("edit.application.project.financialimpact.unit." + num);
		if(StringHelper.containsNonWhitespace(unit)) {
			text += " " + unit;
		}
		addStaticTextElement(doc, parentEl, "projectFinancialImpact" + num, "edit.application.project.financialimpact." + num, text);
	}
	
	private void addStaticTextElement(Document doc, Element parentEl, String element, String labelI18nKey, Integer value) {
		if(value != null) {
			addStaticTextElement(doc, parentEl, element, labelI18nKey, value.toString());
		}
	}

	private void addStaticTextElement(Document doc, Element parentEl, String element, String labelI18nKey, Double value) {
		if(value != null) {
			addStaticTextElement(doc, parentEl, element, labelI18nKey, Double.toString(value.doubleValue()));
		}
	}
	
	private void addStaticTextElement(Document doc, Element parentEl, String element, String labelI18nKey, String value) {
		if(StringHelper.containsNonWhitespace(element)) {
			if(value == null) {
				value = "&#160;";
			}
			Element attributeEl = (Element)parentEl.appendChild(doc.createElement(element));
			attributeEl.setAttribute("id", element);
			attributeEl.setAttribute("label", translator.translate(labelI18nKey));
			attributeEl.appendChild(doc.createTextNode(replaceLineBreaks(value)));
			if(StringHelper.containsNonWhitespace(value)) {
				parentEl.setAttribute("visible", "true");
			}
		}
	}
	
	public static String replaceLineBreaks(String value) {
		return value
				.replace('\n', '\u2028')
				.replace("<br>", "\u2028");
	}
	
	private String getOutputDate() {
		return DateCellRenderer.format(new Date());
	}
	
}
