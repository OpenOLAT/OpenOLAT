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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.AnonymiseService;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.Address;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationCategory;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.committee.ReportCommittee;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;

/**
 * 
 * Initial date: 2 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AnonymiseServiceImpl implements AnonymiseService {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private ReportingCommitteeDAO reportingCommitteeDao;
	@Autowired
	private ApplicationCategoryDAO applicationCategoryDao;
	
	@Override
	public Position anonymise(Position position) {
		
		List<Identity> committee = recruitingService.getCommitteeMembers(position);
		
		List<ApplicationLight> apps = recruitingService.getApplications(position);
		for(ApplicationLight app:apps) {
			Application application = recruitingService.getApplicationByKey(app.getKey());
			anonymiseApplication(position, application, committee);
		}
		
		anonymiseCommittee(position, committee);
		anonymisePosition(position);
		return position;
	}

	private void anonymisePosition(Position position) {
		if(!recruitingModule.isReportingKeepPositionPlanningId()) {
			position.setPlaningsNumber(null);
		}
		if(!recruitingModule.isReportingKeepPositionTitle()) {
			position.setPositionTitle(null);
			position.setPositionTitleDe(null);
		}
		if(!recruitingModule.isReportingKeepPositionShortTitle()) {
			position.setShortTitle(null);
			position.setShortTitleDe(null);
		}
		position.setDescription(null);
		position.setDescriptionDe(null);
		position.setMessageToCommitte(null);
		if(!recruitingModule.isReportingKeepPositionDepartment()) {
			position.setDepartment(null);
			position.setDepartmentDe(null);
		}
		position.setHomepage(null);
		if(!recruitingModule.isReportingKeepPositionApplicationDeadline()) {
			position.setApplicationDeadline(null);
		}
		if(!recruitingModule.isReportingKeepPositionRatingDeadline()) {
			position.setRatingDeadline(null);
		}

		if(!recruitingModule.isReportingKeepPositionOrgUnit()) {
			position.setOrganisation(null);
		}
	}
	
	private void anonymiseCommittee(Position position, List<Identity> committee) {
		Set<Long> members = recruitingService.getCommitteeRefs(position, PositionRole.member).stream()
				.map(IdentityRef::getKey).collect(Collectors.toSet());
		Set<Long> secretaries = recruitingService.getCommitteeRefs(position, PositionRole.secretary).stream()
				.map(IdentityRef::getKey).collect(Collectors.toSet());
		Set<Long> heads = recruitingService.getCommitteeRefs(position, PositionRole.head).stream()
				.map(IdentityRef::getKey).collect(Collectors.toSet());
		Set<Long> exOfficios = recruitingService.getCommitteeRefs(position, PositionRole.exofficio).stream()
				.map(IdentityRef::getKey).collect(Collectors.toSet());

		for(Identity member:committee) {
			ReportCommittee report = reportingCommitteeDao.createReportCommittee(position);
			if(recruitingModule.isReportingKeepCommitteeRole()) {			
				report.setRole(getRole(member, members, secretaries, heads, exOfficios));
			}
			if(recruitingModule.isReportingKeepCommitteeRatingRights()) {			
				report.setRatingsRights(null);
			}
			if(recruitingModule.isReportingKeepCommitteeGender()) {			
				report.setGender(member.getUser().getProperty(UserConstants.GENDER));
			}
			if(recruitingModule.isReportingKeepCommitteeUserClassification()) {			
				report.setGender(member.getUser().getProperty("typeOfUser"));
			}
			
			statisticsRatingsCommittee(report, position, member);
			reportingCommitteeDao.persistReportCommittee(report);
		}
	}
	
	private void statisticsRatingsCommittee(ReportCommittee report, Position position, Identity member) {
		List<UserRating> ratings = recruitingService.getRatings(position, Collections.singletonList(member));
		
		int numOfRatingsA = 0;
		int numOfRatingsB = 0;
		int numOfRatingsC = 0;
		int numOfRatingsAbsentions = 0;

		for(UserRating rating:ratings) {
			Integer val = rating.getRating();
			if(val != null) {
				int value = val.intValue();
				if(value == 3) {
					numOfRatingsA++;
				} else if(value == 2) {
					numOfRatingsB++;
				} else if(value == 1) {
					numOfRatingsC++;
				} else if(value == RecruitingService.ABSTENTION) {
					numOfRatingsAbsentions++;
				}
			}
		}
		
		if(recruitingModule.isReportingKeepCommitteeNumberRatingsA()) {
			report.setNumOfRatingsA(numOfRatingsA);
		}
		if(recruitingModule.isReportingKeepCommitteeNumberRatingsB()) {
			report.setNumOfRatingsB(numOfRatingsB);
		}
		if(recruitingModule.isReportingKeepCommitteeNumberRatingsC()) {
			report.setNumOfRatingsC(numOfRatingsC);
		}
		if(recruitingModule.isReportingKeepCommitteeNumberRatingsAbstentions()) {			
			report.setNumOfAbstentions(numOfRatingsAbsentions);
		}
	}
	
	private String getRole(Identity member, Set<Long> members, Set<Long> secretaries, Set<Long> heads, Set<Long> exOfficios) {
		List<String> roles = new ArrayList<>(4);
		if(members.contains(member.getKey())) {
			roles.add(PositionRole.member.name());
		}
		if(secretaries.contains(member.getKey())) {
			roles.add(PositionRole.secretary.name());
		}
		if(heads.contains(member.getKey())) {
			roles.add(PositionRole.head.name());
		}
		if(exOfficios.contains(member.getKey())) {
			roles.add(PositionRole.exofficio.name());
		}
		return String.join(",", roles);
	}

	private void anonymiseApplication(Position position, Application app, List<Identity> committee) {
		anonymiseApplicationPerson(app.getPerson());
		anonymiseAddress(app.getAddress(), recruitingModule.isReportingKeepPrivateCountry());
		anonymiseAddress(app.getBusinessAddress(), recruitingModule.isReportingKeepBusinessCountry());
		anonymiseApplicationAcademicalBackground(app.getAcademicalBackground());
		anonymiseApplicationProject(app.getProject());
		
		if(!recruitingModule.isReportingKeepApplicationStatus()) {
			app.setApplicationStatus(null);
		}
		app.setStatusComment(null);
		app.setOnholdDate(null);
		app.setWithdrawnDate(null);
		app.setRejectedDate(null);
		app.setNotEligibleDate(null);
		app.setGrantedDate(null);
		app.setHiredDate(null);
		app.setAcceptTerms(null);
		app.setJobAd(null);
		app.setExpertConsent(null);
		app.setExpertBlackList(null);
		app.setPublicFeedbackDeadline(null);
		app.setPublicFeedbackKey(null);
		app.setMemo(null);
		app.setCommitteeComment(null);
		app.setLanguage(null);
		app.getBusinessInformations().setCurrentPosition(null);
		app.getBusinessInformations().setOrganization(null);
		app.getBusinessInformations().setUnit(null);
		app.setCommitteeComment(null);
		
		if(!recruitingModule.isReportingKeepApplicationDecision()) {
			recruitingService.setDecision(app, 0);
		}
		
		if(recruitingModule.isReportingKeepApplicationRatingsA()
				|| recruitingModule.isReportingKeepApplicationRatingsB()
				|| recruitingModule.isReportingKeepApplicationRatingsC()
				|| recruitingModule.isReportingKeepApplicationRatingsAbstentions()) {
			statisticsRatingsApplication(position, app, committee);
		}
		
		if(recruitingModule.isReportingKeepApplicationNumReferees()
				|| recruitingModule.isReportingKeepApplicationNumRefereesDocuments()
				|| recruitingModule.isReportingKeepApplicationNumExperts()
				|| recruitingModule.isReportingKeepApplicationNumExpertsDocuments()) {
			statisticsReferencesApplication(position, app);
		}
		
		app = recruitingService.saveApplication(app);
		
		for(DocumentEnum doc:DocumentEnum.values()) {
			Attachment attachment = doc.path(app);
			if(attachment != null) {
				recruitingService.removeAttachmentDatas(app, attachment);
				doc.setPath(app, null);
			}
		}

		boolean keepSystemTags = recruitingModule.isReportingKeepApplicationSystemTags();
		List<ApplicationCategory> tags = applicationCategoryDao.getApplicationCategories(app, !keepSystemTags, true);
		for(ApplicationCategory tag:tags) {
			applicationCategoryDao.delete(tag);
		}

		dbInstance.commit();
	}
	
	private void statisticsReferencesApplication(Position position, Application app) {
		ReferenceSearchParameters params = new ReferenceSearchParameters();
		params.setPosition(position);
		params.setApplications(Collections.singletonList(app));
		
		int numOfReferees = 0;
		int numOfRefereesDocs = 0;
		int numOfExperts = 0;
		int numOfExpertsDocs = 0;
		int numOfComparativeExperts = 0;
		int numOfComparativeExpertsDocs = 0;
		
		List<Reference> references = recruitingService.getReferences(params);
		for(Reference reference: references) {
			ReferenceType type = reference.getReferenceType();
			Attachment letter = reference.getLetter();
	
			if(type == ReferenceType.expert) {
				numOfExperts++;
				if(letter != null) {
					numOfExpertsDocs++;
				}
			} else if(type == ReferenceType.recommendation) {
				numOfReferees++;
				if(letter != null) {
					numOfRefereesDocs++;
				}
			} else if(type == ReferenceType.comparativeAssessmentExpert) {
				numOfComparativeExperts++;
				if(letter != null) {
					numOfComparativeExpertsDocs++;
				}
			}
		}

		if(recruitingModule.isReportingKeepApplicationNumReferees()) {
			app.setReportingNumOfReferees(numOfReferees);
		}
		if(recruitingModule.isReportingKeepApplicationNumRefereesDocuments()) {
			app.setReportingNumOfRefereesLetters(numOfRefereesDocs);
		}
		if(recruitingModule.isReportingKeepApplicationNumExperts()) {
			app.setReportingNumOfExperts(numOfExperts);
		}
		if(recruitingModule.isReportingKeepApplicationNumExpertsDocuments()) {
			app.setReportingNumOfExpertsLetters(numOfExpertsDocs);
		}
		if(recruitingModule.isReportingKeepApplicationNumComparativeExperts()) {
			app.setReportingNumOfComparativeExperts(numOfComparativeExperts);
		}
		if(recruitingModule.isReportingKeepApplicationNumComparativeExpertsDocuments()) {
			app.setReportingNumOfComparativeExpertsLetters(numOfComparativeExpertsDocs);
		}
		
		
	}
	
	private void statisticsRatingsApplication(Position position, Application app, List<Identity> committee) {
		List<UserRating> ratings = recruitingService.getRatings(position, app, committee);
		
		int numOfRatingsA = 0;
		int numOfRatingsB = 0;
		int numOfRatingsC = 0;
		int numOfRatingsAbsentions = 0;

		for(UserRating rating:ratings) {
			Integer val = rating.getRating();
			if(val != null) {
				int value = val.intValue();
				if(value == 3) {
					numOfRatingsA++;
				} else if(value == 2) {
					numOfRatingsB++;
				} else if(value == 1) {
					numOfRatingsC++;
				} else if(value == RecruitingService.ABSTENTION) {
					numOfRatingsAbsentions++;
				}
			}
		}
		
		if(recruitingModule.isReportingKeepApplicationRatingsA()) {
			app.setReportingNumOfRatingsA(numOfRatingsA);
		}
		if(recruitingModule.isReportingKeepApplicationRatingsB()) {
			app.setReportingNumOfRatingsA(numOfRatingsB);
		}
		if(recruitingModule.isReportingKeepApplicationRatingsC()) {
			app.setReportingNumOfRatingsA(numOfRatingsC);
		}
		if(recruitingModule.isReportingKeepApplicationRatingsAbstentions()) {
			app.setReportingNumOfRatingsA(numOfRatingsAbsentions);
		}
		
		//TODO anony delete ratings
	}
	
	private void anonymiseApplicationProject(Project project) {
		project.setTitle(null);
		if(!recruitingModule.isReportingKeepProjectFinancialImpact1()) {
			project.setFinancialImpact1(null);
		}
		if(!recruitingModule.isReportingKeepProjectFinancialImpact2()) {
			project.setFinancialImpact2(null);
		}
		if(!recruitingModule.isReportingKeepProjectFinancialImpact3()) {
			project.setFinancialImpact3(null);
		}
		if(!recruitingModule.isReportingKeepProjectFinancialImpact4()) {
			project.setFinancialImpact4(null);
		}
		if(!recruitingModule.isReportingKeepProjectFinancialImpact5()) {
			project.setFinancialImpact5(null);
		}
		if(!recruitingModule.isReportingKeepProjectStartDate()) {
			project.setStartDate(null);
		}
		project.setDuration(null);
		project.setDescription(null);
		project.setAcronym(null);
		project.setKeywords(null);
		project.setDisciplines(null);
	}

	private void anonymiseApplicationAcademicalBackground(AcademicalBackground background) {
		if(!recruitingModule.isReportingKeepAcademicalBackgroundHighestDegreeType()) {
			background.setHighestDegreeType(null);
		}
		background.setHighestDegreeDescription(null);
		if(!recruitingModule.isReportingKeepAcademicalBackgroundHighestDegreeYear()) {
			background.setHighestDegreeDate(null);
		}
		background.setHighestDegreeInstitution(null);
		background.setWorkedInAcademiaSince(null);
		background.setWorkedOutAcademiaSince(null);
		background.setWorkedOutAcademiaCareSince(null);
		background.setCareerDescription(null);
		
		background.setHabilitationTitle(null);
		if(!recruitingModule.isReportingKeepAcademicalBackgroundHabilitationDate()) {
			background.setHabilitationDate(null);
		}
		background.setHabilitationInstitution(null);
		background.setOrcid(null);
		
		background.setDissertationTitle(null);
		if(!recruitingModule.isReportingKeepAcademicalBackgroundDissertationDate()) {
			background.setDissertationDate(null);
		}
		background.setDissertationInstitution(null);
		background.setDissertationKeyword1(null);
		background.setDissertationKeyword2(null);
		background.setDissertationKeyword3(null);
		
		if(!recruitingModule.isReportingKeepAcademicalBackgroundNumberOfOriginalPublications()) {
			background.setNumberOfOriginalPublications(null);
		}
		if(!recruitingModule.isReportingKeepAcademicalBackgroundNumberOfFirstAuthorships()) {
			background.setNumberOfFirstAuthorships(null);
		}
		if(!recruitingModule.isReportingKeepAcademicalBackgroundNumberOfLastAuthorships()) {
			background.setNumberOfLastAuthorships(null);
		}
		if(!recruitingModule.isReportingKeepAcademicalBackgroundCitations()) {
			background.setCitations(null);
		}
		if(!recruitingModule.isReportingKeepAcademicalBackgroundImpactFactor()) {
			background.setImpactFactor(null);
		}
		if(!recruitingModule.isReportingKeepAcademicalBackgroundHFactor()) {
			background.setHFactor(null);
		}
	}
	
	private void anonymiseApplicationPerson(Person person) {
		person.setTitle(null);
		person.setFirstName(null);
		person.setLastName(null);
		if(!recruitingModule.isReportingKeepGender()) {
			person.setGender(null);
		}
		if(!recruitingModule.isReportingKeepMaritalStatus()) {
			person.setMaritalStatus(null);
		}
		person.setDisability(null);
		if(!recruitingModule.isReportingKeepBirthday()) {
			person.setBirthday(null);
		}
		if(!recruitingModule.isReportingKeepNationality()) {
			person.setNationality(null);
			person.setAdditionalNationalities(null);
		}
		person.setMail(null);
		person.setPhone(null);
		person.setMobilePhone(null);
		person.setAcademicTitle(null);
	}
	
	private void anonymiseAddress(Address address, boolean keepCountry) {
		address.setAddressLine1(null);
		address.setAddressLine2(null);
		address.setAddressLine3(null);
		address.setZipCode(null);
		address.setCity(null);
		if(!keepCountry) {
			address.setCountry(null);
		}
	}
}
