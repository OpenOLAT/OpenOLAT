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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.NamedGroupImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.olat.modules.forms.model.jpa.EvaluationFormSessionImpl;
import org.olat.modules.invitation.manager.InvitationDAO;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRollCallRefImpl;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.manager.RepositoryEntryToOrganisationDAO;
import org.olat.search.SearchModule;
import org.olat.upgrade.model.RepositoryEntryAccessUpgrade;
import org.olat.upgrade.model.RepositoryEntryAccessUpgradeStatus;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_13_0_0.class);
	
	private static final String VERSION = "OLAT_13.0.0";
	private static final String MIGRATE_ROLE = "MIGRATE ROLE";
	private static final String MIGRATE_INVITEE = "MIGRATE INVITEE";
	private static final String MIGRATE_REPO_ENTRY_DEFAULT_ORG = "MIGRATE REPO ENTRY TO DEF ORG";
	private static final String MIGRATE_PORTFOLIO_EVAL_FORM = "PORTFOLIO EVALUATION FORM";
	private static final String MIGRATE_SEND_APPEAL_DATES = "LECTURES SEND APPEAL DATES";
	private static final String MIGRATE_ADMIN_SITE_SEC = "MIGRATE ADMIN SITE SECURITY CALLBACK";
	private static final String MIGRATE_REPO_ENTRY_ACCESS = "MIGRATE REPO ENTRY ACCESS";
	private static final String MIGRATE_LUCENE = "MIGRATE LUCENE 7.5";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationDAO invitationDao;
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
	@Autowired
	private SiteDefinitions sitesModule;
	@Autowired
	private SearchModule searchModule;
	
	public OLATUpgrade_13_0_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
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
		allOk &= migrateInvitee(upgradeManager, uhd);
		allOk &= migrateRepositoryEntriesToDefaultOrganisation(upgradeManager, uhd);
		allOk &= migratePortfolioEvaluationForm(upgradeManager, uhd);
		allOk &= migrateLecturesSendAppealDates(upgradeManager, uhd);
		allOk &= migrateAdminSiteSecurityCallback(upgradeManager, uhd);
		allOk &= migrateRepositoryEntriesAccess(upgradeManager, uhd);
		allOk &= migrateLucene(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_13_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_13_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migrateLucene(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_LUCENE)) {
			try {
				String indexPath = searchModule.getFullIndexPath();
				deleteIndex(indexPath);
				String permIndexPath = searchModule.getFullPermanentIndexPath();
				deleteIndex(permIndexPath);
				String tempIndexPath = searchModule.getFullTempIndexPath();
				deleteIndex(tempIndexPath);
				String spellIndexPath = searchModule.getSpellCheckDictionaryPath();
				deleteIndex(spellIndexPath);
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_LUCENE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void deleteIndex(String path) {
		File index = new File(path);
		if(index.exists() && index.isDirectory()) {
			FileUtils.deleteDirsAndFiles(index, true, true);
			log.info("Delete Lucene index at: " + index);
		}
	}
	
	private boolean migrateAdminSiteSecurityCallback(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_ADMIN_SITE_SEC)) {
			try {
				List<SiteConfiguration> siteConfigurations = sitesModule.getSitesConfiguration();
				for(SiteConfiguration siteConfiguration:siteConfigurations) {
					if("olatsites_admin".equals(siteConfiguration.getId())
							&& "adminSiteSecurityCallback".equals(siteConfiguration.getSecurityCallbackBeanId())) {
						siteConfiguration.setSecurityCallbackBeanId("restrictToSysAdminSiteSecurityCallback");
						sitesModule.setSitesConfiguration(siteConfigurations);
						log.info("Migrate admin site security callback");
						break;
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_ADMIN_SITE_SEC, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean migrateLecturesSendAppealDates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_SEND_APPEAL_DATES)) {
			try {
				List<Long> repositoryEntryKeys = getRepositoryEntryWithAuditLog();
				for(int i=0; i<repositoryEntryKeys.size(); i++) {
					migrateLecturesSendAppealDates(repositoryEntryKeys.get(i));
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
				if(call != null && call.getAppealDate() == null) {
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
	
	private boolean migrateRepositoryEntriesAccess(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_REPO_ENTRY_ACCESS)) {
			try {
				List<Long> repositoryEntryKeys = getRepositoryEntryKeys();
				for(int i=0; i<repositoryEntryKeys.size(); i++) {
					migrateRepositoryEntryAccess(repositoryEntryKeys.get(i));
					if(i % 50 == 0) {
						log.info("Migration repository entries access flags: " + i + " / " + repositoryEntryKeys.size());
						dbInstance.commitAndCloseSession();
					}
				}
				log.info("Migration repository entries access flags done: " + repositoryEntryKeys.size());
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_REPO_ENTRY_ACCESS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateRepositoryEntryAccess(Long repositoryEntryKey) {
		RepositoryEntryAccessUpgrade oldEntry = dbInstance.getCurrentEntityManager()
				.find(RepositoryEntryAccessUpgrade.class, repositoryEntryKey);
		RepositoryEntry entry = repositoryService.loadByKey(repositoryEntryKey);
		if(oldEntry != null && entry != null) {
			int access = oldEntry.getAccess();
			boolean isMembersOnly = oldEntry.isMembersOnly();
			if(access == RepositoryEntryAccessUpgrade.ACC_USERS) {
				entry.setAllUsers(true);
				entry.setGuests(false);
			} else if(access == RepositoryEntryAccessUpgrade.ACC_USERS_GUESTS) {
				entry.setAllUsers(true);
				entry.setGuests(true);
			} else {
				entry.setAllUsers(false);
				entry.setGuests(false);
			}
			
			RepositoryEntryStatusEnum status = RepositoryEntryStatusEnum.preparation;
			RepositoryEntryAccessUpgradeStatus statusCode = new RepositoryEntryAccessUpgradeStatus(oldEntry.getStatusCode());
			if(access == RepositoryEntryAccessUpgrade.DELETED) {
				status = RepositoryEntryStatusEnum.trash;
			} else if(statusCode.isClosed()) {
				status = RepositoryEntryStatusEnum.closed;		
			} else if(statusCode.isUnpublished()) {
				if(access == RepositoryEntryAccessUpgrade.ACC_OWNERS) {
					status = RepositoryEntryStatusEnum.preparation;
				} else if(access == RepositoryEntryAccessUpgrade.ACC_OWNERS_AUTHORS) {
					status = RepositoryEntryStatusEnum.review;
				}	
			} else if(access == RepositoryEntryAccessUpgrade.ACC_OWNERS) {
				if(isMembersOnly) {
					status = RepositoryEntryStatusEnum.published;
				} else {
					status = RepositoryEntryStatusEnum.preparation;
				}
			} else if(access == RepositoryEntryAccessUpgrade.ACC_OWNERS_AUTHORS) {
				status = RepositoryEntryStatusEnum.review;
			} else if(access == RepositoryEntryAccessUpgrade.ACC_USERS || access == RepositoryEntryAccessUpgrade.ACC_USERS_GUESTS) {
				status = RepositoryEntryStatusEnum.published;
			}
			
			entry.setEntryStatus(status);
			dbInstance.getCurrentEntityManager().merge(entry);
			dbInstance.commit();
		}
	}
	
	private boolean migrateRepositoryEntriesToDefaultOrganisation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_REPO_ENTRY_DEFAULT_ORG)) {
			try {
				List<Long> repositoryEntryKeys = getRepositoryEntryKeys();
				for(int i=0; i<repositoryEntryKeys.size(); i++) {
					migrateRepositoryEntryToDefaultOrganisation(repositoryEntryKeys.get(i));
					if(i % 50 == 0) {
						log.info("Migration repository entries to default organisation: " + i + " / " + repositoryEntryKeys.size());
						dbInstance.commitAndCloseSession();
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
		List<RepositoryEntry> entries = loadRepositoryEntry(repositoryEntryKey);
		for(RepositoryEntry entry:entries) {
			List<Organisation> currentOrganisations = repositoryEntryRelationDao.getOrganisations(Collections.singletonList(entry));
			if(currentOrganisations.isEmpty()) {
				Organisation defOrganisation = organisationService.getDefaultOrganisation();
				repositoryEntryToOrganisationDao.createRelation(defOrganisation, entry, false);
				repositoryEntryRelationDao.createRelation(defOrganisation.getGroup(), entry);
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private List<RepositoryEntry> loadRepositoryEntry(Long repositoryEntryKey) {
		String query = "select v from repositoryentry as v where v.key=:repoKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setParameter("repoKey", repositoryEntryKey)
				.getResultList();
	}
	
	private boolean migrateInvitee(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_INVITEE)) {
			try {

				List<Organisation> defOrganisations = organisationDao.loadDefaultOrganisation();
				Organisation defOrganisation = null;
				if(!defOrganisations.isEmpty()) {
					defOrganisation = defOrganisations.get(0);
				}
				List<Long> invitationKeys = getInvitations();
				for(Long invitationKey:invitationKeys) {
					InvitationImpl invitation = (InvitationImpl)invitationDao.loadByKey(invitationKey);
					migrateInvitee(invitation, defOrganisation);
					dbInstance.commitAndCloseSession();
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_INVITEE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrateInvitee(InvitationImpl invitation, Organisation defOrganisation) {
		if(invitation.getIdentity() != null) return;
		
		String mail = invitation.getMail();
		List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
		
		Identity identity = null;
		if(identities.size() == 1) {
			identity = identities.get(0);
		} else if(identities.size() > 1) {
			for(Identity possibleIdentity:identities) {
				String username = possibleIdentity.getName();
				if(username.length() >= 32 && username.length() <= 36) {
					//UUID
					identity = possibleIdentity;
				}
			}
		}
		
		if(identity != null) {
			invitation.setIdentity(identity);
			dbInstance.getCurrentEntityManager().merge(invitation);
			if(defOrganisation != null) {
				organisationService.addMember(defOrganisation, identity, OrganisationRoles.invitee);
			}
		}
	}
	
	private List<Long> getInvitations() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select invitation.key from binvitation as invitation");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
	}
	
	private boolean migrateRole(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_ROLE)) {
			try {
				List<Organisation> defOrganisations = organisationDao.loadDefaultOrganisation();
				if(defOrganisations.isEmpty()) {
					allOk &= false;
				} else {
					Organisation defOrganisation = defOrganisations.get(0);
					migrate(defOrganisation, "fxadmins", OrganisationRoles.sysadmin);
					migrate(defOrganisation, "fxadmins", OrganisationRoles.administrator);
					migrate(defOrganisation, "fxadmins", OrganisationRoles.rolesmanager);
					migrate(defOrganisation, "admins", OrganisationRoles.administrator);
					migrate(defOrganisation, "admins", OrganisationRoles.sysadmin);
					migrate(defOrganisation, "admins", OrganisationRoles.rolesmanager);
					migrate(defOrganisation, "users", OrganisationRoles.user);
					migrate(defOrganisation, "usermanagers", OrganisationRoles.usermanager);
					migrate(defOrganisation, "usermanagers", OrganisationRoles.rolesmanager);
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
		List<Long> identitiesKeys = getIdentityInSecurityGroup(secGroupName);
		for(int i=0; i<identitiesKeys.size(); i++) {
			Identity member = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, identitiesKeys.get(i));
			organisationService.addMember(organisation, member, role);
			if(i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(i % 500 == 0) {
				log.info("Migration of " + i + " " + secGroupName);
			}
		}
		dbInstance.commit();
		log.info("End migration of " + identitiesKeys.size() + " " + secGroupName);
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
