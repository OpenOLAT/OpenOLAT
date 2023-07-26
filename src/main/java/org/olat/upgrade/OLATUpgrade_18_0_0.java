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
package org.olat.upgrade;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.commons.info.InfoMessage;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.manager.RepositoryEntryCertificateConfigurationDAO;
import org.olat.course.config.CourseConfig;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaCenterExtension;
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.BadgesUserToolExtension;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.PortfolioServiceImpl;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.manager.QualityServiceImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.upgrade.model.UpgradeMedia;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 25 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_18_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_0_0.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.0.0";
	private static final String INIT_INFO_MESSAGES_SCHEDULER_UPDATE = "INIT INFO MESSAGES SCHEDULER UPDATE";

	private static final String INIT_PROJECTS_CONFIGS = "INIT PROJECTS CONFIG";

	private static final String CERTIFICATE_AUTO_ENABLED = "CERTIFICATE_AUTO";
	private static final String CERTIFICATE_MANUAL_ENABLED = "CERTIFICATE_MANUAL";
	private static final String CERTIFICATE_TEMPLATE = "CERTIFICATE_TEMPLATE";

	private static final String CERTIFICATE_CUSTOM1 = "CERTIFICATE_CUSTOM1";
	private static final String CERTIFICATE_CUSTOM2 = "CERTIFICATE_CUSTOM2";
	private static final String CERTIFICATE_CUSTOM3 = "CERTIFICATE_CUSTOM3";
	private static final String RECERTIFICATION_ENABLED = "RECERTIFICATION_ENABLED";
	private static final String RECERTIFICATION_TIMELAPSE = "RECERTIFICATION_TIMELAPSE";
	private static final String RECERTIFICATION_TIMELAPSE_UNIT = "RECERTIFICATION_TIMELAPSE_UNIT";

	private static final String MIGRATE_CERTIFICATE_CONFIG = "MIGRATE CERTIFICATE CONFIG";
	
	private static final String MIGRATE_MEDIA_CATEGORIES = "MIGRATE MEDIA CATEGORIES";
	private static final String MIGRATE_MEDIA_CONTENT = "MIGRATE MEDIA CONTENT";
	private static final String MIGRATE_MEDIA_UUID = "MIGRATE MEDIA UUID";
	private static final String MIGRATE_MEDIA_MISSING_CHECKSUM = "MIGRATE MEDIA MISSING CHECKSUM";
	private static final String MIGRATE_MEDIA_VERSION_METADATA = "MIGRATE MEDIA VERSION METADATA";
	private static final String MIGRATE_PUBLISHED_PAGE = "MIGRATE PUBISHED PAGE";
	private static final String ENABLE_MEDIA_CENTER = "ENABLE MEDIA CENTER";
	private static final String INIT_MEDIA_CENTER_LICENSE = "INIT MEDIA CENTER LICENSE";
	
	private static final String INIT_QM_QUALITATIVE_FEEDBACK = "INIT QM QUALITATIVE FEEDBACK";
	private static final String ASSESSMENT_DELETE_STRUCTURE_STATUS = "ASSESSMENT DELETE STRUCTURE STATUS";
	private static final String ASSESSMENT_PROGRESS_OPTIONAL = "ASSESSMENT PROGRESS OPTIONAL";

	private static final String BADGE_TEMPLATES = "BADGE TEMPLATES";

	@Autowired
	private DB dbInstance;
 	@Autowired
	private MediaDAO mediaDao;
 	@Autowired
 	private PageService pageService;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private ProjectModule projectModule;
 	@Autowired
 	private UserToolsModule userToolsModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private RepositoryEntryCertificateConfigurationDAO certificateConfigurationDao;
	@Autowired
	private QualityServiceImpl qualityService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private MediaCenterLicenseHandler mediaCenterLicenseHandler;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;

	public OLATUpgrade_18_0_0() {
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
		allOk &= initProjectsConfigs(upgradeManager, uhd);
		allOk &= initMigrateCertificateConfiguration(upgradeManager, uhd);
		allOk &= initInfoMessageSchedulerUpdate(upgradeManager, uhd);
		allOk &= initQmQualitativeFeedback(upgradeManager, uhd);
		allOk &= deleteAssessmentStatus(upgradeManager, uhd);
		allOk &= updateAssessmentProgressOptional(upgradeManager, uhd);
		allOk &= initMigrateMediaUuid(upgradeManager, uhd);
		allOk &= initMigrateMediaCategoriesToTags(upgradeManager, uhd);
		allOk &= initMigrateMediaContent(upgradeManager, uhd);
		allOk &= initMigrateMediaMissingChecksum(upgradeManager, uhd);
		allOk &= initMigrateMediaMissingMetadata(upgradeManager, uhd);
		allOk &= initMigratePublishedPage(upgradeManager, uhd);
		allOk &= enableMediaCenter(upgradeManager, uhd);
		allOk &= initMediaCenterLicense(upgradeManager, uhd);
		allOk &= createBadgeTemplates(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initInfoMessageSchedulerUpdate(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE)) {
			try {
				log.info("Start updating infoMessages with new scheduler columns");

				int counter = 0;
				List<InfoMessage> infoMessages;
				do {
					infoMessages = getInfoMessages(counter, BATCH_SIZE);
					for (InfoMessage infoMessage : infoMessages) {
						//set initial value to true, because there was no scheduler option before
						infoMessage.setPublished(true);
						//set initial value to creationDate, because there was no scheduler option before
						infoMessage.setPublishDate(infoMessage.getCreationDate());
					}
					counter += infoMessages.size();
					log.info(Tracing.M_AUDIT, "Updated infoMessages: {} total processed ({})", infoMessages.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				log.info("Update for infoMessages with scheduler update finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private boolean initProjectsConfigs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_PROJECTS_CONFIGS)) {
			try {
				String userDataDirectory = WebappHelper.getUserDataRoot();
				Path propsPath = Paths.get(userDataDirectory, "system", "configuration", "org.olat.modules.project.ProjectModule.properties");
				if (!Files.exists(propsPath)) {
					Properties props = new Properties();
					props.setProperty("project.enabled", Boolean.FALSE.toString());
					@SuppressWarnings("resource")
					Writer propWriter = Files.newBufferedWriter(propsPath);
					props.store(propWriter, "");
					propWriter.close();
					projectModule.setEnabled(false);
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_PROJECTS_CONFIGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}


	private boolean initMigrateCertificateConfiguration(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CERTIFICATE_CONFIG)) {
			try {
				log.info("Start courses certificates configuration migration.");

				int counter = 0;
				List<Long> courses = getCourseEntries();
				for(Long courseKey:courses) {
					initMigrateCertificateConfiguration(courseKey);
					if((++counter) % 25 == 0) {
						log.info("Courses certificates configuration migration: {} / {}", counter, courses.size());
						dbInstance.commitAndCloseSession();
					}
				}
				dbInstance.commitAndCloseSession();

				log.info("Courses certificates configuration migration finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_CERTIFICATE_CONFIG, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void initMigrateCertificateConfiguration(Long repositoryEntryKey) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(repositoryEntryKey);
		// Don't create if not exists
		RepositoryEntryCertificateConfiguration configuration = certificateConfigurationDao.getConfiguration(repositoryEntry);
		if(configuration == null) {
			try {
				ICourse course = CourseFactory.loadCourse(repositoryEntry);
				CourseConfig config = course.getCourseConfig();
				configuration = certificatesManager.createConfiguration(repositoryEntry);
				
				Object autoEnabled = config.getRawValue(CERTIFICATE_AUTO_ENABLED);
				if(autoEnabled instanceof Boolean autoEnabledBool) {
					configuration.setAutomaticCertificationEnabled(autoEnabledBool.booleanValue());
				}
				Object manualEnabled = config.getRawValue(CERTIFICATE_MANUAL_ENABLED);
				if(manualEnabled instanceof Boolean manualEnabledBool) {
					configuration.setManualCertificationEnabled(manualEnabledBool.booleanValue());
				}
				
				Object templateId = config.getRawValue(CERTIFICATE_TEMPLATE);
				if (templateId instanceof Long templateIdLong) {
					CertificateTemplate template = certificatesManager.getTemplateById(templateIdLong);
					configuration.setTemplate(template);	
				}
				
				Object custom1 = config.getRawValue(CERTIFICATE_CUSTOM1);
				if(custom1 instanceof String custom1String) {
					configuration.setCertificateCustom1(custom1String);
				}
				Object custom2 = config.getRawValue(CERTIFICATE_CUSTOM2);
				if(custom2 instanceof String custom2String) {
					configuration.setCertificateCustom2(custom2String);
				}
				Object custom3 = config.getRawValue(CERTIFICATE_CUSTOM3);
				if(custom3 instanceof String custom3String) {
					configuration.setCertificateCustom3(custom3String);
				}
				Object recertificationEnabled = config.getRawValue(RECERTIFICATION_ENABLED);
				if(recertificationEnabled instanceof Boolean recertificationEnabledBool) {
					configuration.setValidityEnabled(recertificationEnabledBool.booleanValue());
				}
				Object timelapse = config.getRawValue(RECERTIFICATION_TIMELAPSE);
				if (timelapse instanceof Integer timelapseInt) {
					configuration.setValidityTimelapse(timelapseInt.intValue());
				}
				Object timelapseUnit = config.getRawValue(RECERTIFICATION_TIMELAPSE_UNIT);
				if (timelapseUnit instanceof String unitString  && StringHelper.containsNonWhitespace(unitString)) {
					configuration.setValidityTimelapseUnit(CertificationTimeUnit.valueOf(unitString));
				}
				certificatesManager.updateConfiguration(configuration);
				dbInstance.commit();
			} catch (CorruptedCourseException e) {
				log.warn("Course is corrupted: {}", repositoryEntryKey);
			}
		}
	}

	private List<Long> getCourseEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re.key from repositoryentry re")
		  .append(" inner join re.olatResource as ores")
		  .and().append(" ores.resName = 'CourseModule'")
		  .append(" order by re.key asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
	}

	private List<InfoMessage> getInfoMessages(int firstResult, int maxResults) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select msg from infomessage as msg");

		TypedQuery<InfoMessage> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), InfoMessage.class);

		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		return query.getResultList();
	}
	
	private boolean initQmQualitativeFeedback(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(INIT_QM_QUALITATIVE_FEEDBACK)) {
			try {
				log.info("Start init qualitative feedback");
				initQmQualitativeFeedback();
				dbInstance.commitAndCloseSession();
				log.info("Init qualitative feedback finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_QM_QUALITATIVE_FEEDBACK, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private void initQmQualitativeFeedback() {
		List<QualityDataCollection> dataCollections = qualityService.loadAllDataCollections();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (QualityDataCollection dataCollection : dataCollections) {
			initQmQualitativeFeedback(dataCollection);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init qualitative feedback: num. of data collections: {}", migrationCounter);
			}
		}
		
	}

	private void initQmQualitativeFeedback(QualityDataCollection dataCollection) {
		try {
			RepositoryEntry formEntry = qualityService.loadFormEntry(dataCollection);
			qualityService.updateQualitativeFeedback(dataCollection, formEntry);
		} catch (Exception e) {
			log.error("Error in init qualitative feedback ({}).", dataCollection);
			log.error("", e);
		}
		
	}

	private boolean deleteAssessmentStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_DELETE_STRUCTURE_STATUS)) {
			try {
				log.info("Start delete assessment status.");
				deleteAssessmentStatus();
				dbInstance.commitAndCloseSession();
				log.info("All assessment status deleted.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(ASSESSMENT_DELETE_STRUCTURE_STATUS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void deleteAssessmentStatus() {
		List<Long> assessmentEntryKeys = getAssessmentEntryKeysOfConditionalStrucureNodes();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (Long assessmentEntryKey : assessmentEntryKeys) {
			deleteAssessmentStatus(assessmentEntryKey);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Delete assessment entry status: num. of assessment entries: {}", migrationCounter);
			}
		}
	}
	
	private List<Long> getAssessmentEntryKeysOfConditionalStrucureNodes() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae.key");
		sb.append("  from assessmententry ae");
		sb.append("       inner join ae.repositoryEntry re");
		sb.append("       inner join courseelement ce");
		sb.append("         on ce.repositoryEntry = ae.repositoryEntry");
		sb.append("        and ce.subIdent = ae.subIdent");
		sb.append("        and ce.type = 'st'");
		sb.and().append("re.technicalType = 'condition'");
		sb.and().append("ae.status is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
	}
	
	private void deleteAssessmentStatus(Long assessmentEntrykey) {
		try {
			String query = 
			"""
				update assessmententry ae
				   set ae.status = null
					 , ae.lastModified = :lastModified
				 where ae.key = :aeKey
			""";
			dbInstance.getCurrentEntityManager()
					.createQuery(query)
					.setParameter("lastModified", new Date())
					.setParameter("aeKey", assessmentEntrykey)
					.executeUpdate();
		} catch (Exception e) {
			log.error("Error in update assessment status ({}).", assessmentEntrykey);
			log.error("", e);
		}
	}
	
	private boolean updateAssessmentProgressOptional(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_PROGRESS_OPTIONAL)) {
			try {
				log.info("Start update assessment progress if root node is optional.");
				updateAssessmentProgressOptional();
				dbInstance.commitAndCloseSession();
				log.info("End update assessment progress if root node is optional.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(ASSESSMENT_PROGRESS_OPTIONAL, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void updateAssessmentProgressOptional() {
		List<AssessmentEntry> assessmentEntries = getOptionalAssessmentEntryRootNodes();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			
			evaluate(assessmentEntry);
			
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Update assessment progress if root node is optional: num. of assessment entries: {}", migrationCounter);
			}
		}
	}

	private void evaluate(AssessmentEntry assessmentEntry) {
		try {
			ICourse course = CourseFactory.loadCourse(assessmentEntry.getRepositoryEntry());
			if (course != null) {
				IdentityEnvironment identityEnv = new IdentityEnvironment();
				identityEnv.setIdentity(assessmentEntry.getIdentity());
				UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
				ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
				scoreAccounting.evaluateAll(true);
			}
		} catch (CorruptedCourseException e) {
			//
		} catch (Exception e) {
			log.error("Error updating assessment progress ({}).", assessmentEntry.getKey());
			log.error("", e);
		}
	}
	
	private List<AssessmentEntry> getOptionalAssessmentEntryRootNodes() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae");
		sb.append("  from assessmententry ae");
		sb.append("       inner join fetch ae.repositoryEntry re");
		sb.append("       inner join fetch re.olatResource as ores");
		sb.and().append("re.technicalType = 'learningpath'");
		sb.and().append("ae.entryRoot = true");
		sb.and().append("ae.obligation ").in(AssessmentObligation.optional, AssessmentObligation.excluded);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.getResultList();
	}
	

	private boolean initMigrateMediaUuid(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_UUID)) {
			try {
				log.info("Start generate media UUIDs.");
				
				int counter = 0;
				List<Media> medias;
				do {
					medias = getMediaWithoutUUID(counter, BATCH_SIZE);
					for (Media media : medias) {
						if(!StringHelper.containsNonWhitespace(media.getUuid())) {
							((MediaImpl)media).setUuid(UUID.randomUUID().toString());
							mediaService.updateMedia(media);
						}
					}
					counter += medias.size();
					log.info(Tracing.M_AUDIT, "UUIDs media generated: {} total processed ({})", medias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Media UUIDs generation finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_UUID, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	

	private List<Media> getMediaWithoutUUID(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" where media.uuid is null")
		  .append(" order by media.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	
	private boolean initMigrateMediaCategoriesToTags(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_CATEGORIES)) {
			try {
				log.info("Start migration of media categories to tags.");
				
				int counter = 0;
				List<Media> medias;
				do {
					medias = getMediaWithCategories(counter, BATCH_SIZE);
					for (Media media : medias) {
						List<String> categories = getCategories(media);
						mediaService.updateTags(null, media, categories);
					}
					counter += medias.size();
					log.info(Tracing.M_AUDIT, "Updated media categories: {} total processed ({})", medias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Media categories migration finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_CATEGORIES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	public List<String> getCategories(Media media) {
		StringBuilder sb = new StringBuilder();
		sb.append("select category.name from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" where rel.resId=:resId and rel.resName=:resName");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), String.class)
			.setParameter("resName", "Media")
			.setParameter("resId", media.getKey())
			.getResultList();
	}
	
	private List<Media> getMediaWithCategories(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where exists (select rel.key from pfcategoryrelation as rel")
		  .append("  where rel.resId=media.key and rel.resName='Media'")
		  .append(" ) order by media.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigrateMediaContent(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_CONTENT)) {
			try {
				log.info("Start migration of media content to version.");
				
				int counter = 0;
				List<UpgradeMedia> upgradeMedias;
				do {
					upgradeMedias = getMedia(counter, BATCH_SIZE);
					for (UpgradeMedia upgradeMedia : upgradeMedias) {
						Media media = mediaDao.loadByKey(upgradeMedia.getKey());
						List<MediaVersion> versions = media.getVersions();
						if(versions == null || versions.isEmpty()) {
							mediaDao.createVersion(media, upgradeMedia.getCollectionDate(), null,
									upgradeMedia.getContent(), upgradeMedia.getStoragePath(), upgradeMedia.getRootFilename());
							dbInstance.commit();
						}
					}
					counter += upgradeMedias.size();
					log.info(Tracing.M_AUDIT, "Updated media content: {} total processed ({})", upgradeMedias.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();
				log.info("Media content migration finished.");
				
				// Both are not independent
				log.info("Start migration of page parts to media version.");
				int counterPart = 0;
				List<PagePart> parts;
				do {
					parts = getMediaParts(counterPart, BATCH_SIZE);
					for (PagePart part : parts) {
						if(part instanceof MediaPart mediaPart) {
							Media media = mediaPart.getMedia();
							MediaVersion mediaVersion = mediaPart.getMediaVersion();
							List<MediaVersion> versions = media.getVersions();
							if(versions != null && !versions.isEmpty() && mediaVersion == null) {
								mediaPart.setMediaVersion(versions.get(0));
								dbInstance.getCurrentEntityManager().merge(mediaPart);
								dbInstance.commit();
							}
						}
					}
					counterPart += parts.size();
					log.info(Tracing.M_AUDIT, "Updated page parts: {} total processed ({})", parts.size(), counterPart);
					dbInstance.commitAndCloseSession();
				} while (counterPart == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Page parts migration finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_CONTENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<UpgradeMedia> getMedia(int firstResult, int maxResults) {
		String query = "select media from upgrademedia as media order by media.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UpgradeMedia.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private List<PagePart> getMediaParts(int firstResult, int maxResults) {
		String query = "select part from cepagepart as part order by part.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, PagePart.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigrateMediaMissingChecksum(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_MISSING_CHECKSUM)) {
			try {
				log.info("Start calculating missing versions checksum.");
				
				int counter = 0;
				List<MediaVersionImpl> mediaVersions;
				do {
					mediaVersions = getMediaVersionWithoutChecksum(counter, BATCH_SIZE);
					for (MediaVersionImpl mediaVersion : mediaVersions) {
						mediaDao.checksumAndMetadata(mediaVersion);
						mediaDao.update(mediaVersion);
					}
					counter += mediaVersions.size();
					log.info(Tracing.M_AUDIT, "Updated media version checksum: {} total processed ({})", mediaVersions.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("Calculating missing versions checksum finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_MISSING_CHECKSUM, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean initMigrateMediaMissingMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_VERSION_METADATA)) {
			try {
				log.info("Start linking metadata to version.");
				
				int counter = 0;
				List<MediaVersionImpl> mediaVersions;
				do {
					mediaVersions = getMediaVersionWithoutMetadata(counter, BATCH_SIZE);
					for (MediaVersionImpl mediaVersion : mediaVersions) {
						VFSMetadata metadata = fileStorage.getMediaRootItemMetadata(mediaVersion);
						if(mediaVersion.getMetadata() == null && metadata != null) {
							mediaVersion.setMetadata(metadata);
							mediaService.updateMediaVersion(mediaVersion);
						}	
					}
					counter += mediaVersions.size();
					log.info(Tracing.M_AUDIT, "Updated media version metadata: {} total processed ({})", mediaVersions.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("End linking metadata to version.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_MEDIA_VERSION_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<MediaVersionImpl> getMediaVersionWithoutChecksum(int firstResult, int maxResults) {
		String query = """
			select mversion from mediaversion as mversion
			 where mversion.rootFilename is not null and mversion.storagePath is not null and mversion.versionChecksum is null
			 order by mversion.key asc
			""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersionImpl.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private List<MediaVersionImpl> getMediaVersionWithoutMetadata(int firstResult, int maxResults) {
		String query = """
			select mversion from mediaversion as mversion
			 where mversion.rootFilename is not null and mversion.storagePath is not null and mversion.metadata.key is null
			 order by mversion.key asc
			""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, MediaVersionImpl.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean initMigratePublishedPage(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_PUBLISHED_PAGE)) {
			try {
				log.info("Start versioning of published pages.");
				
				int counter = 0;
				List<Page> publishedPages;
				do {
					publishedPages = getPublishedPage(counter, BATCH_SIZE);
					for (Page publishedPage : publishedPages) {
						versionedPublishedPage(publishedPage);
					}
					counter += publishedPages.size();
					log.info(Tracing.M_AUDIT, "Versioning of pages: {} total processed ({})", publishedPages.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				dbInstance.commitAndCloseSession();

				log.info("End versioning of published pages.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_PUBLISHED_PAGE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void versionedPublishedPage(Page page) {
		try {
			page = pageService.getFullPageByKey(page.getKey());
			((PortfolioServiceImpl)portfolioService).versionedMedias(page);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("Error versioning medias of page: {}", page.getKey(), e);
			dbInstance.rollbackAndCloseSession();
		}
	}
	
	private List<Page> getPublishedPage(int firstResult, int maxResults) {
		QueryBuilder query = new QueryBuilder();
		query.append("select page from cepage as page")
			 .append(" where page.status").in(PageStatus.published, PageStatus.closed)
			 .append(" order by page.key asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Page.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean enableMediaCenter(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ENABLE_MEDIA_CENTER)) {
			try {
				log.info("Enable media center.");
				
				String availableTools = userToolsModule.getAvailableUserTools();
				if(!"none".equals(availableTools) && StringHelper.containsNonWhitespace(availableTools)
						&& !availableTools.contains(MediaCenterExtension.MEDIA_CENTER_USER_TOOL_ID)) {
					availableTools += "," + MediaCenterExtension.MEDIA_CENTER_USER_TOOL_ID;
				}
				userToolsModule.setAvailableUserTools(availableTools);

				log.info("Calculating missing versions checksum finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(ENABLE_MEDIA_CENTER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean initMediaCenterLicense(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_MEDIA_CENTER_LICENSE)) {
			try {
				boolean repositoryLicenseEnabled = licenseModule.isEnabled(repositoryEntryLicenseHandler);
				licenseModule.setEnabled(mediaCenterLicenseHandler.getType(), repositoryLicenseEnabled);
				log.info("Init media center license: {}", repositoryLicenseEnabled);
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			uhd.setBooleanDataValue(INIT_MEDIA_CENTER_LICENSE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean createBadgeTemplates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(BADGE_TEMPLATES)) {
			try {
				log.info("Creating badge templates");
				openBadgesManager.createFactoryBadgeTemplates();
				log.info("Created badge templates");

				log.info("Enable badges user tool");

				String availableTools = userToolsModule.getAvailableUserTools();
				if(!"none".equals(availableTools) && StringHelper.containsNonWhitespace(availableTools)
						&& !availableTools.contains(BadgesUserToolExtension.BADGES_USER_TOOL_ID)) {
					availableTools += "," + BadgesUserToolExtension.BADGES_USER_TOOL_ID;
				}
				userToolsModule.setAvailableUserTools(availableTools);
				log.info("Enabled badges user tool");

			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(BADGE_TEMPLATES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
