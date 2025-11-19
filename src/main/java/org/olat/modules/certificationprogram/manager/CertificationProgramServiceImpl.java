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
package org.olat.modules.certificationprogram.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.CertificationProgramToCurriculumElement;
import org.olat.modules.certificationprogram.CertificationRoles;
import org.olat.modules.certificationprogram.model.CertificationCurriculumElementWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramWithStatistics;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.manager.CreditPointWalletDAO;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 26 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramServiceImpl implements CertificationProgramService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao; 
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramToOrganisationDAO certificationProgramToOrganisationDao;
	@Autowired
	private CertificationProgramMailConfigurationDAO certificationProgramMailConfigurationDao;
	@Autowired
	private CertificationProgramToCurriculumElementDAO certificationProgramToCurriculumElementDao;
	
	@Override
	public CertificationProgram createCertificationProgram(String identifier, String displayName, Identity owner) {
		CertificationProgram program = certificationProgramDao.createCertificationProgram(identifier, displayName);
		if(owner != null) {
			groupDao.addMembershipOneWay(program.getGroup(), owner, CertificationRoles.programowner.name());
		}
		
		for(CertificationProgramMailType notificationType:CertificationProgramMailType.notifications()) {
			createMailConfigurations(program, notificationType);
		}
		dbInstance.commit();
		return program;
	}
	
	@Override
	public CertificationProgram updateCertificationProgram(CertificationProgram program) {
		program = certificationProgramDao.updateCertificationProgram(program);
		if(program.getTemplate() != null) {
			// Fetch template
			program.getTemplate().getName();
		}
		return program;
	}
	
	@Override
	public CertificationProgram updateCertificationProgram(CertificationProgram program, List<Organisation> organisations) {
		program = updateCertificationProgram(program);
		List<Organisation> currentOrganisations = certificationProgramToOrganisationDao.getOrganisations(program);
		List<Organisation> organisationsToRemove = new ArrayList<>();
		for(Organisation organisation:organisations) {
			if(!currentOrganisations.contains(organisation)) {
				certificationProgramToOrganisationDao.createRelation(program, organisation);
			}
			organisationsToRemove.remove(organisation);
		}
		for(Organisation organisationToRemove:organisationsToRemove) {
			certificationProgramToOrganisationDao.removeOrganisation(program, organisationToRemove);
		}
		return program;
	}
	
	@Override
	public CertificationProgram getCertificationProgram(CertificationProgramRef program) {
		if(program == null || program.getKey() == null) return null;
		return certificationProgramDao.loadCertificationProgram(program.getKey());
	}

	@Override
	public List<CertificationProgram> getCertificationPrograms() {
		return certificationProgramDao.loadCertificationPrograms();
	}

	@Override
	public List<CertificationProgram> getCertificationPrograms(List<Organisation> organisations) {
		return certificationProgramDao.loadCertificationPrograms(organisations);
	}

	@Override
	public List<Organisation> getOrganisations(CertificationProgramRef program) {
		return certificationProgramToOrganisationDao.getOrganisations(program);
	}
	
	@Override
	public CertificationProgramMailConfiguration createMailConfigurations(CertificationProgram program, CertificationProgramMailType type) {
		return certificationProgramMailConfigurationDao.createConfiguration(program, type);
	}

	@Override
	public List<CertificationProgramMailConfiguration> getMailConfigurations(CertificationProgram program) {
		return certificationProgramMailConfigurationDao.getConfigurations(program);
	}

	@Override
	public CertificationProgramMailConfiguration getMailConfiguration(Long key) {
		return certificationProgramMailConfigurationDao.getConfiguration(key);
	}

	@Override
	public CertificationProgramMailConfiguration updateMailConfiguration(CertificationProgramMailConfiguration configuration) {
		return certificationProgramMailConfigurationDao.updateConfiguration(configuration);
	}

	@Override
	public List<Identity> getCertificationProgramOwners(CertificationProgram program) {
		return groupDao.getMembers(program.getGroup(), CertificationRoles.programowner.name());
	}

	@Override
	public void addCertificationProgramOwner(CertificationProgram program, Identity identity) {
		if(!groupDao.hasRole(program.getGroup(), identity, CertificationRoles.programowner.name())) {
			groupDao.addMembershipOneWay(program.getGroup(), identity, CertificationRoles.programowner.name());
		}
	}

	@Override
	public void removeCertificationProgramOwner(CertificationProgram program, Identity identity) {
		groupDao.removeMembership(program.getGroup(), identity, CertificationRoles.programowner.name());
	}

	@Override
	public List<CertificationProgramWithStatistics> getCertificationProgramsWithStatistics(IdentityRef identity, Date referenceDate) {
		return certificationProgramDao.loadCertificationProgramsWithStatistics(identity, referenceDate);
	}

	@Override
	public List<CertificationCurriculumElementWithInfos> getCurriculumElementsFor(CertificationProgramRef program, Date referenceDate) {
		return certificationProgramToCurriculumElementDao.getCurriculumElementsFor(program, referenceDate);
	}

	@Override
	public CertificationProgram getCertificationProgram(CurriculumElementRef element) {
		return certificationProgramToCurriculumElementDao.getCertificationProgram(element);
	}

	@Override
	public void addCurriculumElementToCertificationProgram(CertificationProgram program, CurriculumElement element) {
		if(!certificationProgramToCurriculumElementDao.hasCurriculumElement(program, element)) {
			certificationProgramToCurriculumElementDao.createRelation(program, element);
			dbInstance.commit();
		}
	}
	
	@Override
	public void removeCurriculumElementToCertificationProgram(CurriculumElement element) {
		List<CertificationProgramToCurriculumElement> rels = certificationProgramToCurriculumElementDao.getRelations(element);
		for(CertificationProgramToCurriculumElement rel:rels) {
			certificationProgramToCurriculumElementDao.deleteRelation(rel);
		}
		dbInstance.commit();
	}

	@Override
	public List<CertificationProgram> getCertificationPrograms(RepositoryEntryRef entry) {
		return certificationProgramDao.getCertificationPrograms(entry);
	}

	@Override
	public boolean isInCertificationProgram(RepositoryEntryRef entry) {
		return certificationProgramDao.isCertificationProgram(entry);
	}

	@Override
	public boolean isCertificationAccepted(Identity identity, RepositoryEntry entry, CertificationProgram certificationProgram) {
		BigDecimal points = certificationProgram.getCreditPoints();
		CreditPointSystem system = certificationProgram.getCreditPointSystem();
		CreditPointWallet wallet = creditPointWalletDao.getWallet(identity, system);
		return wallet != null && points != null
				&& points.compareTo(wallet.getBalance()) <= 0;
	}
	
	@Override
	public long countMembers(CertificationProgramMemberSearchParameters searchParams, Date referenceDate) {
		return certificationProgramToCurriculumElementDao.countCertificates(searchParams, referenceDate);
	}

	@Override
	public List<CertificationProgramMemberWithInfos> getMembers(CertificationProgramMemberSearchParameters searchParams, Date referenceDate) {
		List<Certificate> certificates = certificationProgramToCurriculumElementDao.getCertificates(searchParams, referenceDate);
		List<CreditPointWallet> wallets = creditPointWalletDao.loadWalletOfCertificationProgram(searchParams.getCertificationProgram());
		Map<Long,CreditPointWallet> identityKeyToWallet = wallets.stream()
				.collect(Collectors.toMap(w -> w.getIdentity().getKey(), w -> w, (u, v) -> u));
		
		List<CertificationProgramMemberWithInfos> infos = certificates.stream().map(certificate -> {
			Identity identity = certificate.getIdentity();
			CreditPointWallet wallet = identityKeyToWallet.get(identity.getKey());
			return new CertificationProgramMemberWithInfos(certificate, identity, wallet);
		}).toList();
				
		return infos;
	}

	@Override
	public List<Certificate> getCertificates(IdentityRef identity, CertificationProgram certificationProgram) {
		return certificatesDao.getCertificates(identity, certificationProgram);
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(CertificationProgram program, IdentityRef identity, List<RepositoryEntryStatusEnum> status) {
		return certificationProgramDao.getAssessmentEntries(program, identity, status);
	}

	@Override
	public Certificate pauseRecertification(CertificationProgram program, IdentityRef identity, Identity doer) {
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		if(lastCertificate != null) {
			lastCertificate = certificatesDao.pauseCertificate(lastCertificate);
		}
		return lastCertificate;
	}

	@Override
	public Certificate continueRecertification(CertificationProgram program, IdentityRef identity, Identity doer) {
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		if(lastCertificate != null) {
			lastCertificate = certificatesDao.continueCertificate(lastCertificate);
		}
		return lastCertificate;
	}

	@Override
	public List<Identity> getEligiblesIdentitiesToRecertification(CertificationProgram program, Date referenceDate) {
		if(program == null || !program.isRecertificationEnabled()) return List.of();
		
		if(program.getCreditPoints() != null && program.getCreditPoints().compareTo(BigDecimal.ZERO) > 0) {
			return certificationProgramDao.getEligibleForRecertificationsWithCreditPoints(program, referenceDate);
		}
		return certificationProgramDao.getEligibleForRecertifications(program, referenceDate);
	}
}
