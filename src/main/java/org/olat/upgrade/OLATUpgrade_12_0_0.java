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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_12_0_0.class);
	
	private static final String VERSION = "OLAT_12.0.0";
	private static final String FEED_XML_TO_DB = "FEED XML TO DB";
	private static final String USER_PROPERTY_CONTEXT_RENAME = "USER PROPERTY CONTEXT RENAME";
	private static final String LAST_USER_MODIFICATION = "LAST USER MODIFICATION";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedManager feedManager;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	public OLATUpgrade_12_0_0() {
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
		// migrate all blogs and podcasts from xml to database data structure
		allOk &= upgradeBlogXmlToDb(upgradeManager, uhd);
		// rename a user property context name to a more suitable name
		allOk &= changeUserPropertyContextName(upgradeManager, uhd);
		allOk &= upgradeLastModified(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_12_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_12_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBlogXmlToDb(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(FEED_XML_TO_DB)) {
			
			List<String> feedTypes = Arrays.asList(BlogFileResource.TYPE_NAME, PodcastFileResource.TYPE_NAME);
			List<OLATResource> feeds = OLATResourceManager.getInstance().findResourceByTypes(feedTypes);
			log.info("Number of feeds to upgrade: " + feeds.size());
			for (OLATResource ores : feeds) {
				log.info("Upgrade feed " + "(" + ores.getResourceableTypeName() + "): " + ores.getResourceableId());
				try {
					feedManager.importFeedFromXML(ores, false);
				} catch (Exception e) {
					allOk &= false;
					log.error("", e);
				}
				dbInstance.commitAndCloseSession();
			}
			
			uhd.setBooleanDataValue(FEED_XML_TO_DB, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	/**
	 * Copy existing user property configuration to the new user property
	 * context handler name. The handler was renamed because it is now not only
	 * used in the course but also in the group.
	 * 
	 * @param upgradeManager
	 * @param uhd
	 * @return
	 */
	private boolean changeUserPropertyContextName(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(USER_PROPERTY_CONTEXT_RENAME)) {		
			// Load configured properties from properties file		
			String userDataDirectory = WebappHelper.getUserDataRoot();
			File configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", "com.frentix.olat.admin.userproperties.UsrPropCfgManager.properties").toFile();
			if (configurationPropertiesFile.exists()) {
				InputStream is = null;
				OutputStream fileStream = null;
				try {
					is = new FileInputStream(configurationPropertiesFile);
					Properties configuredProperties = new Properties();
					configuredProperties.load(is);
					is.close();
					boolean dirty = false;
					// list of possible user property appendices
					List<String> appendices = Arrays.asList("", "_hndl_", "_hndl_mandatory", "_hndl_usrreadonly", "_hndl_adminonly");
					for (String appendix : appendices) {
						String oldKey = "org.olat.course.nodes.members.MembersCourseNodeRunController" + appendix;
						String existingConfig = configuredProperties.getProperty(oldKey);
						String newKey = "org.olat.commons.memberlist.ui.MembersPrintController" + appendix;
						String existingNewConfig = configuredProperties.getProperty(newKey);
						if (existingConfig != null && existingNewConfig == null) {
							configuredProperties.setProperty("org.olat.commons.memberlist.ui.MembersPrintController" + appendix, existingConfig);
							dirty = true;
							log.info("Migrated user property context handler config from::" + oldKey + " to::" + newKey);
						}						
					}
					if (dirty) {
						fileStream = new FileOutputStream(configurationPropertiesFile);
						configuredProperties.store(fileStream, null);
						// Flush and close before sending events to other nodes to make changes appear on other node
						fileStream.flush();
					}
				} catch (Exception e) {
					log.error("Error when reading / writing user properties config file from path::" + configurationPropertiesFile.getAbsolutePath(), e);
					allOk &= false;
				} finally {
					try {
						if (is != null ) is.close();
						if (fileStream != null ) fileStream.close();
					} catch (Exception e) {
						log.error("Could not close stream after storing config to file::" + configurationPropertiesFile.getAbsolutePath(), e);
						allOk &= false;
					}
				}
			}
			
			uhd.setBooleanDataValue(USER_PROPERTY_CONTEXT_RENAME, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean upgradeLastModified(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(LAST_USER_MODIFICATION)) {
			int counter = 0;
			List<Long> entryKeys = getRepositoryEntryKeys();
			for(Long entryKey:entryKeys) {
				RepositoryEntry course = repositoryService.loadByKey(entryKey);
				if(course.getOlatResource().getResourceableTypeName().equals("CourseModule")) {
					try {
						allOk &= processCourseAssessmentLastModified(course);
					} catch (CorruptedCourseException e) {
						log.error("Corrupted course: " + course.getKey(), e);
					}
				}
				
				if(counter++ % 25 == 0) {
					log.info(Tracing.M_AUDIT, "Last modifications migration processed: " + counter + ", total courses processed (" + counter + ")");
					dbInstance.commitAndCloseSession();
				}
			} 

			uhd.setBooleanDataValue(LAST_USER_MODIFICATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<Long> getRepositoryEntryKeys() {
		String q = "select v.key from repositoryentry as v";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.getResultList();
	}
	
	
	private boolean processCourseAssessmentLastModified(RepositoryEntry entry) {
		try {
			ICourse course = CourseFactory.loadCourse(entry);	
			CourseNode rootNode = course.getRunStructure().getRootNode();
			Set<Identity> changeSet = new HashSet<>();
			processCourseNodeAssessmentLastModified(course, entry, rootNode, changeSet);
			dbInstance.commitAndCloseSession();
			
			//update structure nodes and efficiency statement if any
			Set<Identity> identitiesWithEfficiencyStatements = findIdentitiesWithEfficiencyStatements(entry);
			for(Identity assessedIdentity:changeSet) {
				UserCourseEnvironment userCourseEnv = AssessmentHelper
						.createInitAndUpdateUserCourseEnvironment(assessedIdentity, course);
				if(identitiesWithEfficiencyStatements.contains(assessedIdentity)) {
					efficiencyStatementManager.updateUserEfficiencyStatement(userCourseEnv);
				}
				dbInstance.commitAndCloseSession();
			}
		} catch(CorruptedCourseException e) {
			log.error("Corrupted course: " + entry.getKey(), e);
		} catch (Exception e) {
			log.error("Unexpected error", e);
		} finally {
			dbInstance.commitAndCloseSession();
		}
		return true;
	}
	
	private boolean processCourseNodeAssessmentLastModified(ICourse course, RepositoryEntry entry, CourseNode courseNode, Set<Identity> changeSet) {
		if(courseNode instanceof IQTESTCourseNode) {
			updateTest(entry, (IQTESTCourseNode)courseNode, changeSet);
		} else if(courseNode instanceof MSCourseNode) {
			updateMS(entry, courseNode, changeSet);
		} else if(courseNode instanceof ScormCourseNode) {
			updateScorm(course, entry, courseNode, changeSet);
		}
		dbInstance.commitAndCloseSession();

		for(int i=courseNode.getChildCount(); i-->0; ) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			processCourseNodeAssessmentLastModified(course, entry, child, changeSet);
		}
		return true;
	}
	
	private void updateTest(RepositoryEntry entry, IQTESTCourseNode courseNode, Set<Identity> changeSet) {
		boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI));
		if (onyx) return;
		
		RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
		OLATResource ores = testEntry.getOlatResource();
		boolean qti21 = ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName());

		List<AssessmentEntryImpl> assessmentEntries = loadAssessmentEntries(entry, courseNode.getIdent());
		for(AssessmentEntryImpl assessmentEntry:assessmentEntries) {
			if(assessmentEntry.getLastUserModified() != null || assessmentEntry.getLastCoachModified() != null)  continue;

			if(qti21) {
				Long assessmentId = assessmentEntry.getAssessmentId();
				if(assessmentId != null) {
					AssessmentTestSession session = qtiService.getAssessmentTestSession(assessmentEntry.getAssessmentId());
					if(session != null && session.getFinishTime() != null) {
						assessmentEntry.setLastUserModified(session.getFinishTime());
						updateAssessmentEntry(assessmentEntry);
						changeSet.add(assessmentEntry.getIdentity());
					}
				}
			}
		}
	}

	private void updateMS(RepositoryEntry entry, CourseNode msNode, Set<Identity> changeSet) {
		List<AssessmentEntryImpl> assessmentEntries = loadAssessmentEntries(entry, msNode.getIdent());
		for(AssessmentEntryImpl assessmentEntry:assessmentEntries) {
			if(assessmentEntry.getLastCoachModified() != null) continue;
			
			assessmentEntry.setLastCoachModified(assessmentEntry.getLastModified());
			updateAssessmentEntry(assessmentEntry);
			changeSet.add(assessmentEntry.getIdentity());
		}
	}
	
	private void updateScorm(ICourse course, RepositoryEntry entry, CourseNode scormNode, Set<Identity> changeSet) {
		Long courseId = course.getResourceableId();
		String courseIdNodeId = courseId + "-" + scormNode.getIdent();
		Calendar cal = Calendar.getInstance();
		
		List<AssessmentEntryImpl> assessmentEntries = loadAssessmentEntries(entry, scormNode.getIdent());
		for(AssessmentEntryImpl assessmentEntry:assessmentEntries) {
			if(assessmentEntry.getLastUserModified() != null || assessmentEntry.getLastCoachModified() != null) continue;
			
			String userId = assessmentEntry.getIdentity().getName();
			String path = FolderConfig.getCanonicalRoot() + "/scorm/" + userId + "/" + courseIdNodeId;
			File quiz = new File(path, "thequiz.xml");
			if(quiz.exists()) {
				long lastModified = quiz.lastModified();
				if(lastModified > 0) {
					cal.setTimeInMillis(lastModified);
					assessmentEntry.setLastUserModified(cal.getTime());
					updateAssessmentEntry(assessmentEntry);
					changeSet.add(assessmentEntry.getIdentity());
				}
			}
		}
	}
	
	private AssessmentEntry updateAssessmentEntry(AssessmentEntry nodeAssessment) {
		return dbInstance.getCurrentEntityManager().merge(nodeAssessment);
	}
	
	private List<AssessmentEntryImpl> loadAssessmentEntries(RepositoryEntryRef courseEntry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data")
		  .append(" inner join fetch data.identity ident")
		  .append(" where data.repositoryEntry.key=:courseEntryKey and data.subIdent=:subIdent")
		  .append(" and data.lastUserModified is null and data.lastCoachModified is null");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntryImpl.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	private Set<Identity> findIdentitiesWithEfficiencyStatements(RepositoryEntryRef courseRepoEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(statement.identity) from ").append(UserEfficiencyStatementImpl.class.getName()).append(" as statement ")
		  .append(" where statement.lastUserModified is null and statement.lastCoachModified is null and statement.courseRepoKey=:repoKey");

		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", courseRepoEntry.getKey())
				.getResultList();
		return new HashSet<>(identities);
	}
}
