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
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.commons.info.InfoMessage;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
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
import org.olat.modules.project.ProjectModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
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
	
	private static final String ASSESSMENT_DELETE_STRUCTURE_STATUS_ = "ASSESSMENT DELETE STRUCTURE STATUS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private RepositoryEntryCertificateConfigurationDAO certificateConfigurationDao;

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
		allOk &= deleteAssessmentStatus(upgradeManager, uhd);

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
				log.error("Course is corrupted: {}", repositoryEntryKey);
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
	
	private boolean deleteAssessmentStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_DELETE_STRUCTURE_STATUS_)) {
			try {
				log.info("Start delete assessment status.");
				deleteAssessmentStatus();
				dbInstance.commitAndCloseSession();
				log.info("All assessment status deleted.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(ASSESSMENT_DELETE_STRUCTURE_STATUS_, allOk);
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

}
