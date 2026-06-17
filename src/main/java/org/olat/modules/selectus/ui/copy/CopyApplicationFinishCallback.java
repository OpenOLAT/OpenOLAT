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
package org.olat.modules.selectus.ui.copy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.BusinessAddress;
import org.olat.modules.selectus.model.BusinessInformations;
import org.olat.modules.selectus.model.CopyApplicationParameters.Copy;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyApplicationFinishCallback implements StepRunnerCallback {
	
	private final CopyApplicationContext copyContext;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public CopyApplicationFinishCallback(CopyApplicationContext copyContext) {
		CoreSpringFactory.autowireObject(this);
		this.copyContext = copyContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		
		List<Copy> typeOfData = copyContext.getTypeOfData();
		Position sourcePosition = copyContext.getSourcePosition();
		List<ApplicationLight> applications = copyContext.getApplications();
		PositionLight target = copyContext.getSelectedPosition();
		Position targetPosition = recruitingService.getPosition(target.getKey());
		
		for(ApplicationLight app:applications) {
			Application sourceApp = recruitingService.getApplicationByKey(app.getKey());
			if(sourceApp != null) {
				Application targetApp = recruitingService.createTempApplication(targetPosition, true);
				copyApplication(sourcePosition, targetPosition, sourceApp, targetApp, typeOfData, ureq.getIdentity(), ureq.getLocale());
				dbInstance.commitAndCloseSession();
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void copyApplication(Position sourcePosition, Position targetPosition, Application sourceApp, Application targetApp, List<Copy> typeOfData,
			Identity identity, Locale locale) {
		targetApp.setLanguage(sourceApp.getLanguage());
		// Profile are always copied
		copyProfileInformations(sourceApp,  targetApp);
		if(typeOfData.contains(Copy.memo)) {
			targetApp.setMemo(sourceApp.getMemo());
		}
		if(typeOfData.contains(Copy.committeeComment)) {
			targetApp.setCommitteeComment(sourceApp.getCommitteeComment());
		}
		if(typeOfData.contains(Copy.applicationStatus)) {
			targetApp.setApplicationStatus(sourceApp.getApplicationStatus());
		}

		targetApp = recruitingService.saveTempApplication(targetApp, true);
		
		if(typeOfData.contains(Copy.decision) && sourceApp.getDecision() != null) {
			recruitingService.setDecision(targetApp, sourceApp.getDecision());
		}
		if(typeOfData.contains(Copy.tags)) {
			copyCategories(sourceApp, targetApp, identity, locale);
		}
		if(typeOfData.contains(Copy.refereesAndLetters)) {
			copyReferences(sourcePosition, targetPosition, sourceApp, targetApp, ReferenceType.recommendation);
		}
		if(typeOfData.contains(Copy.expertsAndAssessments)) {
			copyReferences(sourcePosition, targetPosition, sourceApp, targetApp, ReferenceType.expert);
		}
		if(typeOfData.contains(Copy.comparativeExperts)) {
			//TODO comp
		}
		if(typeOfData.contains(Copy.applicationDocuments)) {
			copyApplicationDocuments(sourcePosition, targetPosition, sourceApp,  targetApp);
		}
		if(typeOfData.contains(Copy.profileInformations)) {
			copyCustomAttributes(targetPosition, sourceApp, targetApp);
		}
		
		targetApp = recruitingService.saveTempApplication(targetApp, true);
		
		String messageI18n = "audit.log.application.copy";
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(targetApp, locale), targetApp.getId().toString(),
				"", sourcePosition.getMLTitle(locale)
			};
		auditService.auditApplicationLog(Action.add, ActionTarget.application, null, null, messageI18n, messageArgs,
				translator, targetApp.getPosition(), targetApp, identity);
	}
	
	private void copyCategories(Application sourceApp, Application targetApp, Identity identity, Locale locale) {
		List<ApplicationCategoryInfos> categories = taggingService.getApplicationCategories(sourceApp.getPosition(), sourceApp, true);
		List<String> tags = new ArrayList<>();
		for(ApplicationCategoryInfos category:categories) {
			String tag = category.getCategory().getName();
			tags.add(tag);
		}
		
		taggingService.addCategories(targetApp, tags, true, targetApp.getPosition(), identity, locale);
	}
	
	private void copyReferences(Position sourcePosition, Position targetPosition, Application sourceApp, Application targetApp, ReferenceType type) {
		boolean expertEnabled = sourcePosition.isExpertRecommendationEnabled() && targetPosition.isExpertRecommendationEnabled();
		boolean refereeEnabled = sourcePosition.isRefereeRecommendationEnabled() && targetPosition.isRefereeRecommendationEnabled();
		
		List<Reference> sourceReferences = recruitingService.getApplicationReferences(sourceApp, type);
		for(Reference sourceReference:sourceReferences) {
			if((sourceReference.getReferenceType() == ReferenceType.expert && expertEnabled)
					|| (sourceReference.getReferenceType() == ReferenceType.recommendation && refereeEnabled)) {
				Reference targetReference = recruitingService.addReference(sourceReference.getTitle(),
						sourceReference.getFirstName(), sourceReference.getLastName(),
						sourceReference.getInstitution(), sourceReference.getEmail(), sourceReference.getSubmissionDeadline(),
						type, sourceReference.getRequestStatus(), sourceReference.getAdminNote(), targetApp, null);
				targetReference.setLetter(doCopyAttachment(sourceReference.getLetter()));
				targetReference.setReferenceStatus(sourceReference.getReferenceStatus());
				recruitingService.updateReference(targetReference);
				dbInstance.commit();
			}
		}
	}
	
	private void copyApplicationDocuments(Position sourcePosition,  Position targetPosition, Application sourceApp, Application targetApp) {
		List<DocumentOption> docOptions = recruitingModule.getDocumentOptions(targetPosition);
		
		Set<String> targetAvailable = targetPosition.getAvailableDocuments();
		Set<String> targetStaffOnly = targetPosition.getStaffDocuments();
		Set<String> targetMandatory = targetPosition.getMandatoryDocuments();
		
		Set<String> sourceAvailable = sourcePosition.getAvailableDocuments();
		Set<String> sourceStaffOnly = sourcePosition.getStaffDocuments();
		Set<String> sourceMandatory = sourcePosition.getMandatoryDocuments();
		
		for(DocumentOption docOption:docOptions) {
			DocumentEnum doc = docOption.getDoc();
			if(DocumentEnum.combined == doc) {
				continue;
			}
			boolean docTargetEnabled = targetAvailable.contains(doc.name()) || targetMandatory.contains(doc.name()) ||targetStaffOnly.contains(doc.name());
			boolean docSourceEnabled = sourceAvailable.contains(doc.name()) || sourceMandatory.contains(doc.name()) ||sourceStaffOnly.contains(doc.name());
			if(docTargetEnabled && docSourceEnabled) {
				Attachment sourceAttachment = doc.path(sourceApp);
				Attachment targetAttachment = doCopyAttachment(sourceAttachment);
				if(targetAttachment != null) {
					doc.setPath(targetApp, targetAttachment);
				}
			}
		}
	}
	
	private Attachment doCopyAttachment(Attachment attachment) {
		if(attachment == null) return null;
		
		byte[] data = recruitingService.getAttachmentDatas(attachment);
		DocumentType type = DocumentType.secureValueOf(attachment.getType());
		return recruitingService.setAttachmentDatas(null, attachment.getName(), type, data);
	}
	
	private void copyCustomAttributes(Position targetPosition, Application sourceApp, Application targetApp) {
		List<PositionAttributeDefinition> targetDefinitions = targetPosition.getAttributesDefinitions().stream()
				.filter(def -> PositionApplicationAttributeTabEnum.personalData.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.academicalBackground.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.project.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.custom1.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.custom2.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.custom3.equals(def.getTabEnum())
						|| PositionApplicationAttributeTabEnum.custom4.equals(def.getTabEnum()))
				.collect(Collectors.toList());
		
		Set<ApplicationAttribute> sourceAttributes = sourceApp.getAttributes();
		for(ApplicationAttribute sourceAttribute:sourceAttributes) {
			PositionAttributeDefinition targetDefinition = getSimilarAttributeDefinition(sourceAttribute, targetDefinitions);
			if(targetDefinition != null) {
				ApplicationAttribute targetAttribute = recruitingService.createAttribute(targetPosition, targetApp,
						targetDefinition, sourceAttribute.getValue());
				targetApp.getAttributes().add(targetAttribute);
			}
		}
	}
	
	private PositionAttributeDefinition getSimilarAttributeDefinition(ApplicationAttribute attribute,
			List<PositionAttributeDefinition> targetDefinitions) {
		
		PositionAttributeDefinition sourceDefinition = attribute.getDefinition();
		for(PositionAttributeDefinition targetDefinition:targetDefinitions) {
			String sourceLabel = sourceDefinition.getLabel(Locale.ENGLISH, true);
			String targetLabel = targetDefinition.getLabel(Locale.ENGLISH, true);
			if(sourceLabel != null && sourceLabel.equalsIgnoreCase(targetLabel)
					&& sourceDefinition.getTypeEnum() == targetDefinition.getTypeEnum()
					&& sourceDefinition.getTabEnum() == targetDefinition.getTabEnum()) {
				return targetDefinition;
			}
		}
		return null;
	}
	
	private void copyProfileInformations(Application sourceApp, Application targetApp) {
		// Person
		Person sourcePerson = sourceApp.getPerson();
		Person targetPerson = targetApp.getPerson();
		
		targetPerson.setTitle(sourcePerson.getTitle());
		targetPerson.setFirstName(sourcePerson.getFirstName());
		targetPerson.setLastName(sourcePerson.getLastName());
		targetPerson.setGender(sourcePerson.getGender());
		targetPerson.setMaritalStatus(sourcePerson.getMaritalStatus());
		targetPerson.setDisability(sourcePerson.getDisability());
		targetPerson.setBirthday(sourcePerson.getBirthday());
		targetPerson.setNationality(sourcePerson.getNationality());
		targetPerson.setAdditionalNationalities(sourcePerson.getAdditionalNationalities());
		targetPerson.setMail(sourcePerson.getMail());
		targetPerson.setPhone(sourcePerson.getPhone());
		targetPerson.setMobilePhone(sourcePerson.getMobilePhone());
		targetPerson.setAcademicTitle(sourcePerson.getAcademicTitle());
		
		// Addresses
		copyAddress(sourceApp.getAddress(), targetApp.getAddress());
		copyBusinessAddress(sourceApp.getBusinessAddress(), targetApp.getBusinessAddress());
		
		// Business informations
		BusinessInformations sourceInformations = sourceApp.getBusinessInformations();
		BusinessInformations targetInformations = targetApp.getBusinessInformations();
		
		targetInformations.setOrganization(sourceInformations.getOrganization());
		targetInformations.setUnit(sourceInformations.getUnit());
		targetInformations.setCurrentPosition(sourceInformations.getCurrentPosition());
		
		// Academical background
		AcademicalBackground sourceBackground = sourceApp.getAcademicalBackground();
		AcademicalBackground targetBackground = targetApp.getAcademicalBackground();
		
		targetBackground.setHighestDegreeType(sourceBackground.getHighestDegreeType());
		targetBackground.setHighestDegreeDescription(sourceBackground.getHighestDegreeDescription());
		targetBackground.setHighestDegreeDate(sourceBackground.getHighestDegreeDate());
		targetBackground.setHighestDegreeInstitution(sourceBackground.getHighestDegreeInstitution());
		targetBackground.setWorkedInAcademiaSince(sourceBackground.getWorkedInAcademiaSince());
		targetBackground.setWorkedOutAcademiaSince(sourceBackground.getWorkedOutAcademiaSince());
		targetBackground.setWorkedOutAcademiaCareSince(sourceBackground.getWorkedOutAcademiaCareSince());
		targetBackground.setCareerDescription(sourceBackground.getCareerDescription());
		targetBackground.setHabilitationTitle(sourceBackground.getHabilitationTitle());
		targetBackground.setHabilitationDate(sourceBackground.getHabilitationDate());
		targetBackground.setHabilitationInstitution(sourceBackground.getHabilitationInstitution());
		targetBackground.setOrcid(sourceBackground.getOrcid());
		
		targetBackground.setDissertationTitle(sourceBackground.getDissertationTitle());
		targetBackground.setDissertationDate(sourceBackground.getDissertationDate());
		targetBackground.setDissertationInstitution(sourceBackground.getDissertationInstitution());
		targetBackground.setDissertationKeyword1(sourceBackground.getDissertationKeyword1());
		targetBackground.setDissertationKeyword2(sourceBackground.getDissertationKeyword2());
		targetBackground.setDissertationKeyword3(sourceBackground.getDissertationKeyword3());
		
		targetBackground.setNumberOfOriginalPublications(sourceBackground.getNumberOfOriginalPublications());
		targetBackground.setNumberOfFirstAuthorships(sourceBackground.getNumberOfFirstAuthorships());
		targetBackground.setNumberOfLastAuthorships(sourceBackground.getNumberOfLastAuthorships());
		targetBackground.setCitations(sourceBackground.getCitations());
		targetBackground.setImpactFactor(sourceBackground.getImpactFactor());
		targetBackground.setHFactor(sourceBackground.getHFactor());
		
		// Project
		Project sourceProject = sourceApp.getProject();
		Project targetProject = targetApp.getProject();
		
		targetProject.setTitle(sourceProject.getTitle());
		targetProject.setFinancialImpact1(sourceProject.getFinancialImpact1());
		targetProject.setFinancialImpact2(sourceProject.getFinancialImpact2());
		targetProject.setFinancialImpact3(sourceProject.getFinancialImpact3());
		targetProject.setFinancialImpact4(sourceProject.getFinancialImpact4());
		targetProject.setFinancialImpact5(sourceProject.getFinancialImpact5());
		targetProject.setStartDate(sourceProject.getStartDate());
		targetProject.setDuration(sourceProject.getDuration());
		targetProject.setDescription(sourceProject.getDescription());
		targetProject.setAcronym(sourceProject.getAcronym());
		targetProject.setKeywords(sourceProject.getKeywords());
		targetProject.setDisciplines(sourceProject.getDisciplines());
	}
	
	private void copyBusinessAddress(BusinessAddress sourceAddress, BusinessAddress targetAddress) {
		copyAddress(sourceAddress, targetAddress);
		targetAddress.setPhone(sourceAddress.getPhone());
		targetAddress.setEmail(sourceAddress.getEmail());
	}
	
	private void copyAddress(Address sourceAddress, Address targetAddress) {
		targetAddress.setType(sourceAddress.getType());
		targetAddress.setAddressLine1(sourceAddress.getAddressLine1());
		targetAddress.setAddressLine2(sourceAddress.getAddressLine2());
		targetAddress.setAddressLine3(sourceAddress.getAddressLine3());
		targetAddress.setZipCode(sourceAddress.getZipCode());
		targetAddress.setCity(sourceAddress.getCity());
		targetAddress.setCountry(sourceAddress.getCountry());
	}
}
