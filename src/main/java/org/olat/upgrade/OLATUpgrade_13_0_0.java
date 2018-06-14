/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.NamedGroupImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.olat.modules.forms.model.jpa.EvaluationFormSessionImpl;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRollCallRefImpl;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.manager.RepositoryEntryToOrganisationDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.0.0";
	private static final String MIGRATE_ROLE = "MIGRATE ROLE";
	private static final String MIGRATE_REPO_ENTRY_DEFAULT_ORG = "MIGRATE REPO ENTRY TO DEF ORG";
	private static final String MIGRATE_PORTFOLIO_EVAL_FORM = "PORTFOLIO EVALUATION FORM";
	private static final String MIGRATE_SEND_APPEAL_DATES = "LECTURES SEND APPEAL DATES";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private EvaluationFormManager evaManger;
	
	public OLATUpgrade_13_0_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateRole(upgradeManager, uhd);
		allOk &= migrateRepositoryEntriesTodefaultOrganisation(upgradeManager, uhd);
		allOk &= migratePortfolioEvaluationForm(upgradeManager, uhd);
		allOk &= migrateLecturesSendAppealDates(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_13_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migrateLecturesSendAppealDates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_SEND_APPEAL_DATES)) {
			try {
				List<Long> repositoryEntryKeys = getRepositoryEntryWithAuditLog();
				for(int i=0; i<repositoryEntryKeys.size(); i++) {
					migrateLecturesSendAppealDates(repositoryEntryKeys.get(0));
					if(i % 50 == 0) {
						log.info("Migration repository entries with appeal in lectures block roll call: " + i + " / " + repositoryEntryKeys.size());
					}
				}
				log.info("Migration repository entries with appeal in lectures block roll call: " + repositoryEntryKeys.size());
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_SEND_APPEAL_DATES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateLecturesSendAppealDates(Long repositoryEntryKey) {
		int count = 0;
		
		List<LectureBlockAuditLog> auditLogs = getAuditLog(repositoryEntryKey);
		for(LectureBlockAuditLog auditLog:auditLogs) {
			Long rollCallKey = auditLog.getRollCallKey();
			if(rollCallKey != null) {
				LectureBlockRollCall call = lectureService.getRollCall(new LectureBlockRollCallRefImpl(rollCallKey));
				if(call.getAppealDate() == null) {
					call.setAppealDate(auditLog.getCreationDate());
					call.setAppealStatus(LectureBlockAppealStatus.oldWorkflow);
					lectureService.updateRollCall(call);
					if(count++ % 25 == 0) {
						dbInstance.commitAndCloseSession();
					}
				}
			}
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	private List<LectureBlockAuditLog> getAuditLog(Long entryKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select log from lectureblockauditlog log where log.entryKey=:repoEntryKey and log.action=:action");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockAuditLog.class)
				.setParameter("repoEntryKey", entryKey)
				.setParameter("action", LectureBlockAuditLog.Action.sendAppeal.name())
				.getResultList();
	}
	
	private List<Long> getRepositoryEntryWithAuditLog() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct log.entryKey from lectureblockauditlog log where log.action=:action");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("action", LectureBlockAuditLog.Action.sendAppeal.name())
				.getResultList();
	}
	
	private boolean migrateRepositoryEntriesTodefaultOrganisation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_REPO_ENTRY_DEFAULT_ORG)) {
			try {
				List<Long> repositoryEntryKeys = getRepositoryEntryKeys();
				for(int i=0; i<repositoryEntryKeys.size(); i++) {
					migrateRepositoryEntryToDefaultOrganisation(repositoryEntryKeys.get(0));
					if(i % 50 == 0) {
						log.info("Migration repository entries to default organisation: " + i + " / " + repositoryEntryKeys.size());
					}
				}
				log.info("Migration repository entries to default organisation done: " + repositoryEntryKeys.size());
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_REPO_ENTRY_DEFAULT_ORG, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateRepositoryEntryToDefaultOrganisation(Long repositoryEntryKey) {
		RepositoryEntry entry = repositoryService.loadByKey(repositoryEntryKey);
		List<Organisation> currentOrganisations = repositoryEntryRelationDao.getOrganisations(entry);
		if(currentOrganisations.isEmpty()) {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			repositoryEntryToOrganisationDao.createRelation(defOrganisation, entry, false);
			repositoryEntryRelationDao.createRelation(defOrganisation.getGroup(), entry);
			dbInstance.commitAndCloseSession();
		}
	}
	
	private boolean migrateRole(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_ROLE)) {
			try {
				List<Organisation> defOrganisations = organisationDao.loadByIdentifier(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER);
				if(defOrganisations.isEmpty()) {
					allOk &= false;
				} else {
					Organisation defOrganisation = defOrganisations.get(0);
					migrate(defOrganisation, "fxadmins", OrganisationRoles.sysadmin);
					migrate(defOrganisation, "admins", OrganisationRoles.administrator);
					migrate(defOrganisation, "users", OrganisationRoles.user);
					migrate(defOrganisation, "usermanagers", OrganisationRoles.usermanager);
					migrate(defOrganisation, "authors", OrganisationRoles.author);
					migrate(defOrganisation, "instoresmanager", OrganisationRoles.learnresourcemanager);
					migrate(defOrganisation, "groupmanagers", OrganisationRoles.groupmanager);
					migrate(defOrganisation, "poolsmanager", OrganisationRoles.poolmanager);
					migrate(defOrganisation, "curriculmanager", OrganisationRoles.curriculummanager);
					migrate(defOrganisation, "anonymous", OrganisationRoles.guest);
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_ROLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrate(Organisation organisation, String secGroupName, OrganisationRoles role) {
		log.info("Start migration of " + secGroupName);
		List<Long> identitiyKeys = getIdentityInSecurityGroup(secGroupName);
		for(int i=0; i<identitiyKeys.size(); i++) {
			Identity member = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, identitiyKeys.get(i));
			organisationService.addMember(organisation, member, role);
			if(i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(i % 500 == 0) {
				log.info("Migration of " + i + " " + secGroupName);
			}
		}
		dbInstance.commit();
		log.info("End migration of " + identitiyKeys.size() + " " + secGroupName);
	}
	
	public List<Long> getIdentityInSecurityGroup(String securityGroupName) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select msi.identity.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as msi ")
		  .append(" inner join msi.securityGroup secGroup")
		  .append(" inner join ").append(NamedGroupImpl.class.getName()).append(" as ngroup on (ngroup.securityGroup.key=secGroup.key)")
		  .append(" where ngroup.groupName=:groupName");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("groupName", securityGroupName)
				.getResultList();
	}
	
	private List<Long> getRepositoryEntryKeys() {
		String q = "select v.key from repositoryentry as v";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.getResultList();
	}
	
	private boolean migratePortfolioEvaluationForm(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_PORTFOLIO_EVAL_FORM)) {
			try {
				migrateSessions();
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_PORTFOLIO_EVAL_FORM, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	@SuppressWarnings("deprecation")
	private void migrateSessions() {
		log.info("Start migration of sessions of porfolio evaluation forms.");
		List<EvaluationFormSession> sessions = loadPortfolioSessions();
		for(int i=0; i<sessions.size(); i++) {
			EvaluationFormSession session = sessions.get(i);
			if (session.getPageBody() == null || session.getFormEntry() == null || session.getIdentity() == null) {
				log.warn("EvaluationFormSession " + session.getKey() + " was not migrated. [FormEntry: "
						+ session.getFormEntry().toString() + "], [Identity: " + session.getIdentity().toString() + "]");
			}
			EvaluationFormSurvey survey = portfolioService.loadOrCreateSurvey(session.getPageBody(), session.getFormEntry());
			EvaluationFormParticipation participation = loadOrCreateParticipation(session, survey);
			if (session instanceof EvaluationFormSessionImpl) {
				EvaluationFormSessionImpl sessionImpl = (EvaluationFormSessionImpl) session;
				sessionImpl.setParticipation(participation);
				sessionImpl.setSurvey(survey);
				dbInstance.getCurrentEntityManager().merge(sessionImpl);
			}
			if(i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(i % 500 == 0) {
				log.info("" + i + " sessions migrated.");
			}
		}
		dbInstance.commit();
		log.info("End migration of " + sessions.size() + " sessions of porfolio evaluation forms.");
	}

	@SuppressWarnings("deprecation")
	private EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSession session, EvaluationFormSurvey survey) {
		EvaluationFormParticipation participation = evaManger.loadParticipationByExecutor(survey, session.getIdentity());
		if (participation == null) {
			participation = evaManger.createParticipation(survey, session.getIdentity());
			if (EvaluationFormSessionStatus.done.equals(session.getEvaluationFormSessionStatus())) {
				if (participation instanceof EvaluationFormParticipationImpl) {
					EvaluationFormParticipationImpl participationImpl = (EvaluationFormParticipationImpl) participation;
					participationImpl.setStatus(EvaluationFormParticipationStatus.done);
					dbInstance.getCurrentEntityManager().merge(participationImpl);
				}
			}
		}
		return participation;
	}

	private List<EvaluationFormSession> loadPortfolioSessions() {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from evaluationformsession as session");
		sb.append(" where session.pageBody is not null");
		sb.append("   and session.participation is null"); // exclude already migrated session
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSession.class)
				.getResultList();
	}

}
