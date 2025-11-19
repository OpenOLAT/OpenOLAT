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
package org.olat.modules.certificationprogram;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.certificationprogram.model.CertificationCurriculumElementWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramWithStatistics;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 26 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CertificationProgramService {
	
	CertificationProgram createCertificationProgram(String identifier, String displayName, Identity owner);
	
	CertificationProgram updateCertificationProgram(CertificationProgram program);
	
	CertificationProgram updateCertificationProgram(CertificationProgram program, List<Organisation> organisations);
	
	CertificationProgram getCertificationProgram(CertificationProgramRef program);
	
	List<CertificationProgram> getCertificationPrograms();
	
	List<CertificationProgram> getCertificationPrograms(List<Organisation> organisations);
	
	List<CertificationProgramWithStatistics> getCertificationProgramsWithStatistics(IdentityRef identity, Date referenceDate);
	
	
	List<Organisation> getOrganisations(CertificationProgramRef program);
	
	List<Identity> getCertificationProgramOwners(CertificationProgram program);
	
	void addCertificationProgramOwner(CertificationProgram program, Identity identity);
	
	void removeCertificationProgramOwner(CertificationProgram program, Identity identity);
	
	
	List<CertificationCurriculumElementWithInfos> getCurriculumElementsFor(CertificationProgramRef program, Date referenceDate);
	
	CertificationProgram getCertificationProgram(CurriculumElementRef element);
	
	void addCurriculumElementToCertificationProgram(CertificationProgram program, CurriculumElement element);
	
	void removeCurriculumElementToCertificationProgram(CurriculumElement element);
	
	boolean isInCertificationProgram(RepositoryEntryRef entry);
	
	List<CertificationProgram> getCertificationPrograms(RepositoryEntryRef entry);
	
	
	boolean isCertificationAccepted(Identity identity, RepositoryEntry entry, CertificationProgram program);
	
	long countMembers(CertificationProgramMemberSearchParameters searchParams, Date referenceDate);
	
	List<CertificationProgramMemberWithInfos> getMembers(CertificationProgramMemberSearchParameters searchParams, Date referenceDate);
	
	List<Certificate> getCertificates(IdentityRef identity, CertificationProgram certificationProgram);
	
	List<AssessmentEntry> getAssessmentEntries(CertificationProgram program, IdentityRef identity, List<RepositoryEntryStatusEnum> status);
	
	
	List<Identity> getEligiblesIdentitiesToRecertification(CertificationProgram program, Date referenceDate);
	
	
	CertificationProgramMailConfiguration createMailConfigurations(CertificationProgram program, CertificationProgramMailType type);
	
	List<CertificationProgramMailConfiguration> getMailConfigurations(CertificationProgram program);
	
	CertificationProgramMailConfiguration getMailConfiguration(Long key);
	
	CertificationProgramMailConfiguration updateMailConfiguration(CertificationProgramMailConfiguration configuration);	
 
}
