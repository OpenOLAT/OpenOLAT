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

import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.ASSESSMENT_ID;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.ATTEMPTS;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.COACH_COMMENT;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.COMMENT;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.FULLY_ASSESSED;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.PASSED;
import static org.olat.upgrade.legacy.NewCachePersistingAssessmentManager.SCORE;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.nodes.ta.StatusForm;
import org.olat.course.nodes.ta.StatusManager;
import org.olat.course.nodes.ta.TaskController;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.QTIEditorPackageImpl;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.FilePersister;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.scorm.assessment.CmiData;
import org.olat.modules.scorm.assessment.ScormAssessmentManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.upgrade.legacy.NewCacheKey;
import org.olat.upgrade.legacy.NewCachePersistingAssessmentManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_11_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_11_0_0.class);
	
	private static final int BATCH_SIZE = 50;
	private static final String ASSESSMENT_DATAS = "ASSESSMENT PROPERTY TABLE";
	private static final String EFFICIENCY_STATEMENT_DATAS = "EFFICIENCY STATEMENT TABLE";
	private static final String PORTFOLIO_SETTINGS = "PORTFOLIO SETTINGS";
	private static final String VERSION = "OLAT_11.0.0";

	private final Map<Long,Boolean> qtiEssayMap = new HashMap<>();

	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private QTIResultManager qtiResultManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	@Autowired
	private PortfolioV2Module portfolioV2Module;

	public OLATUpgrade_11_0_0() {
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
		allOk &= upgradeEfficiencyStatementTable(upgradeManager, uhd);
		allOk &= upgradeAssessmentPropertyTable(upgradeManager, uhd);
		allOk &= upgradePortfolioSettings(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_11_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_11_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	

	private boolean upgradePortfolioSettings(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(PORTFOLIO_SETTINGS)) {
			boolean hasBinder = hasBinder();
			if(!hasBinder) {
				portfolioV2Module.setEnabled(false);
			}
			uhd.setBooleanDataValue(PORTFOLIO_SETTINGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean hasBinder() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select binder.key from pfbinder as binder");
			List<Long> count =	dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
					.setFirstResult(0)
					.setMaxResults(1)
					.getResultList();
			return count != null && count.size() > 0 && count.get(0) != null && count.get(0) >= 0;
		} catch (Exception e) {
			log.error("", e);
			return true;
		}
	}

	private boolean upgradeEfficiencyStatementTable(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(EFFICIENCY_STATEMENT_DATAS)) {
			int counter = 0;
			final Roles roles = Roles.administratorAndManagersRoles();
			final SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(roles);
			params.setResourceTypes(Collections.singletonList("CourseModule"));
			
			List<RepositoryEntry> courses;
			do {
				courses = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, 50, true);
				for(RepositoryEntry course:courses) {
					try {
						convertUserEfficiencyStatemen(course);
					} catch (CorruptedCourseException e) {
						log.error("Corrupted course: " + course.getKey(), e);
					}
				}
				counter += courses.size();
				log.info(Tracing.M_AUDIT, "Efficiency statement data migration processed: " + courses.size() + ", total courses processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(courses.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(EFFICIENCY_STATEMENT_DATAS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean upgradeAssessmentPropertyTable(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_DATAS)) {
			int counter = 0;
			final Roles roles = Roles.administratorAndManagersRoles();
			final SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(roles);
			params.setResourceTypes(Collections.singletonList("CourseModule"));
			
			List<RepositoryEntry> courses;
			do {
				courses = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, 50, true);
				for(RepositoryEntry course:courses) {
					try {
						allOk &= processCourseAssessmentData(course);
					} catch (CorruptedCourseException e) {
						log.error("Corrupted course: " + course.getKey(), e);
					}
				}
				counter += courses.size();
				log.info(Tracing.M_AUDIT, "Assessment data migration processed: " + courses.size() + ", total courses processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(courses.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(ASSESSMENT_DATAS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void convertUserEfficiencyStatemen(RepositoryEntry courseEntry) {
		try {
			final ICourse course = CourseFactory.loadCourse(courseEntry);
			CourseNode rootNode = course.getRunStructure().getRootNode();
			Set<Long> identityKeys = new HashSet<>(loadIdentityKeyOfAssessmentEntries(courseEntry, rootNode.getIdent()));

			int count = 0;
			List<UserEfficiencyStatementLight> statements = getUserEfficiencyStatements(courseEntry);
			for(UserEfficiencyStatementLight statement:statements) {
				Identity identity = statement.getIdentity();
				if(!identityKeys.contains(identity.getKey())) {
					AssessmentEntry entry = createAssessmentEntry(identity, null, course, courseEntry, rootNode.getIdent());
					if(statement.getScore() != null) {
						entry.setScore(new BigDecimal(statement.getScore()));
					}
					if(statement.getPassed() != null) {
						entry.setPassed(statement.getPassed());
					}
					dbInstance.getCurrentEntityManager().persist(entry);
					if(count++ % 25 == 0) {
						dbInstance.commitAndCloseSession();
					}
				}
			}
		} catch (Exception e) {
			log.error("Error with " + courseEntry.getKey() + " " + courseEntry, e);
		}
		dbInstance.commitAndCloseSession();
	}
	
	public List<UserEfficiencyStatementLight> getUserEfficiencyStatements(RepositoryEntryRef courseRepoEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from ").append(UserEfficiencyStatementLight.class.getName()).append(" as statement ")
		  .append(" inner join fetch statement.identity as ident")
		  .append(" where statement.courseRepoKey=:repoKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementLight.class)
				.setParameter("repoKey", courseRepoEntry.getKey())
				.getResultList();
	}
	
	// select count(*) from o_property where name in ('SCORE','PASSED','ATTEMPTS','COMMENT','COACH_COMMENT','ASSESSMENT_ID','FULLY_ASSESSED');
	private boolean processCourseAssessmentData(RepositoryEntry courseEntry) {
		boolean allOk = true;
		try {
			final Long courseResourceId = courseEntry.getOlatResource().getResourceableId();
			final ICourse course = CourseFactory.loadCourse(courseEntry);

			//load all assessable identities
			List<Identity> assessableIdentities = getAllAssessableIdentities(course, courseEntry);

			Map<AssessmentDataKey,AssessmentEntryImpl> curentNodeAssessmentMap = new HashMap<>();
			{//load already migrated data
				List<AssessmentEntryImpl> currentNodeAssessmentList = loadAssessmentEntries(courseEntry);
				for(AssessmentEntryImpl currentNodeAssessment:currentNodeAssessmentList) {
					AssessmentDataKey key = new AssessmentDataKey(currentNodeAssessment.getIdentity().getKey(), courseResourceId, currentNodeAssessment.getSubIdent());
					curentNodeAssessmentMap.put(key, currentNodeAssessment);
				}
			}

			Map<AssessmentDataKey,AssessmentEntryImpl> nodeAssessmentMap = new HashMap<>();
			{//processed properties
				List<Property> courseProperties = loadAssessmentProperties(courseEntry);
				for(Property property:courseProperties) {
					String propertyCategory = property.getCategory();
					if(StringHelper.containsNonWhitespace(propertyCategory)) {
						int nodeIdentIndex = propertyCategory.indexOf("::");
						if(nodeIdentIndex > 0) {
							String nodeIdent = propertyCategory.substring(propertyCategory.indexOf("::") + 2);
							AssessmentDataKey key = new AssessmentDataKey(property.getIdentity().getKey(), property.getResourceTypeId(), nodeIdent);
							if(curentNodeAssessmentMap.containsKey(key)) {
								continue;
							}
							
							AssessmentEntryImpl nodeAssessment;
							if(nodeAssessmentMap.containsKey(key)) {
								nodeAssessment = nodeAssessmentMap.get(key);
								if(nodeAssessment.getCreationDate().after(property.getCreationDate())) {
									nodeAssessment.setCreationDate(property.getCreationDate());
								}
								
								if(nodeAssessment.getLastModified().before(property.getLastModified())) {
									nodeAssessment.setLastModified(property.getLastModified());
								}
							} else {
								nodeAssessment = createAssessmentEntry(property.getIdentity(), property, course, courseEntry, nodeIdent);
							}
							copyAssessmentProperty(property, nodeAssessment, course);
							nodeAssessmentMap.put(key, nodeAssessment);	
						}
					}	
				}
			}
			
			//check the transient qti ser
			CourseNode rootNode = course.getRunStructure().getRootNode();
			new TreeVisitor(new Visitor() {
				@Override
				public void visit(INode node) {
					if(node instanceof CourseNode) {
						processNonPropertiesStates(assessableIdentities, (CourseNode)node, course, courseEntry,
								nodeAssessmentMap, curentNodeAssessmentMap);
					}
				}
			}, rootNode, true).visitAll();
			
			dbInstance.commitAndCloseSession();
			
			int count = 0;
			for(AssessmentEntryImpl courseNodeAssessment:nodeAssessmentMap.values()) {
				dbInstance.getCurrentEntityManager().persist(courseNodeAssessment);
				if(++count % 50 == 0) {
					dbInstance.commit();
				}
			}
			dbInstance.commitAndCloseSession();
			
			allOk = verifyCourseAssessmentData(assessableIdentities, courseEntry);
			
			dbInstance.commitAndCloseSession();
			
			if(allOk) {
				List<STCourseNode> nodes = hasAssessableSTCourseNode(course);
				if(nodes.size() > 0) {
					log.info("Has assessables ST nodes");
					for(Identity identity:assessableIdentities) {
						IdentityEnvironment identityEnv = new IdentityEnvironment(identity, null);
						UserCourseEnvironmentImpl userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
						userCourseEnv.getScoreAccounting().evaluateAll(true);
						dbInstance.commit();
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}

		return allOk;
	}
	
	private void processNonPropertiesStates(List<Identity> assessableIdentities, CourseNode cNode,
			ICourse course, RepositoryEntry courseEntry, Map<AssessmentDataKey,AssessmentEntryImpl> nodeAssessmentMap,
			Map<AssessmentDataKey,AssessmentEntryImpl> curentNodeAssessmentMap) {
		
		if(cNode instanceof IQTESTCourseNode) {
			processNonPropertiesIQTESTStates(assessableIdentities, (IQTESTCourseNode)cNode, course, courseEntry, nodeAssessmentMap, curentNodeAssessmentMap);
		} else if(cNode instanceof TACourseNode) {
			processNonPropertiesTAStates(assessableIdentities, (TACourseNode)cNode, course, courseEntry, nodeAssessmentMap, curentNodeAssessmentMap);
		}
	}
	
	/**
	 * Find if someone dropped a file in a Task element without task assignment, or has a returned
	 * document.
	 * 
	 * @param assessableIdentities
	 * @param tNode
	 * @param course
	 * @param courseEntry
	 * @param nodeAssessmentMap
	 */
	private void processNonPropertiesTAStates(List<Identity> assessableIdentities, TACourseNode tNode,
			ICourse course, RepositoryEntry courseEntry, Map<AssessmentDataKey,AssessmentEntryImpl> nodeAssessmentMap,
			Map<AssessmentDataKey,AssessmentEntryImpl> curentNodeAssessmentMap) {
		
		for(Identity assessedIdentity:assessableIdentities) {	
			AssessmentDataKey key = new AssessmentDataKey(assessedIdentity, course.getResourceableId(), tNode.getIdent());
			if(curentNodeAssessmentMap.containsKey(key)) {
				continue;
			}
			
			AssessmentEntryImpl nodeAssessment;
			if(!nodeAssessmentMap.containsKey(key)) {
				nodeAssessment = createAssessmentEntry(assessedIdentity, null, course, courseEntry, tNode.getIdent());
				nodeAssessmentMap.put(key, nodeAssessment);
				
				String dropbox = DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), tNode) + File.separator + assessedIdentity.getName();
				LocalFolderImpl dropBox = VFSManager.olatRootContainer(dropbox, null);
				if (dropBox.getBasefile().exists() && dropBox.getBasefile().listFiles(SystemFileFilter.FILES_ONLY).length > 0) {
					nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
				} else {
					String returnbox = ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), tNode) + File.separator + assessedIdentity.getName();
					LocalFolderImpl returnBox = VFSManager.olatRootContainer(returnbox, null);
					if (returnBox.getBasefile().exists() && returnBox.getBasefile().listFiles(SystemFileFilter.FILES_ONLY).length > 0) {
						nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
					}
				}
			}
		}
	}
	
	/**
	 * Find if someone has started a test without getting a score, passed status...
	 * 
	 * @param assessableIdentities
	 * @param iqNode
	 * @param course
	 * @param courseEntry
	 * @param nodeAssessmentMap
	 */
	private void processNonPropertiesIQTESTStates(List<Identity> assessableIdentities, IQTESTCourseNode iqNode,
			ICourse course, RepositoryEntry courseEntry, Map<AssessmentDataKey,AssessmentEntryImpl> nodeAssessmentMap,
			Map<AssessmentDataKey,AssessmentEntryImpl> curentNodeAssessmentMap) {
		
		for(Identity assessedIdentity:assessableIdentities) {
			if(iqTestPersisterExists(assessedIdentity, iqNode, course)) {
				AssessmentDataKey key = new AssessmentDataKey(assessedIdentity, course.getResourceableId(), iqNode.getIdent());
				if(curentNodeAssessmentMap.containsKey(key)) {
					continue;
				}
				
				AssessmentEntryImpl nodeAssessment;
				if(nodeAssessmentMap.containsKey(key)) {
					nodeAssessment = nodeAssessmentMap.get(key);
				} else {
					nodeAssessment = createAssessmentEntry(assessedIdentity, null, course, courseEntry, iqNode.getIdent());
					nodeAssessmentMap.put(key, nodeAssessment);

					Long courseResourceableId = course.getResourceableId();
					String resourcePath = courseResourceableId + File.separator + iqNode.getIdent();
					FilePersister qtiPersister = new FilePersister(assessedIdentity, resourcePath);
					nodeAssessment.setCreationDate(qtiPersister.getLastModified());
					nodeAssessment.setLastModified(qtiPersister.getLastModified());
				}
				nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
			}
		}
	}
	
	private List<STCourseNode> hasAssessableSTCourseNode(ICourse course) {
		List<STCourseNode> assessableSTNodes = new ArrayList<>();
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		new TreeVisitor(new Visitor() {
			@Override
			public void visit(INode node) {
				if(node instanceof STCourseNode) {
					STCourseNode stNode = (STCourseNode)node;
					ScoreCalculator calculator = stNode.getScoreCalculator();
					if(StringHelper.containsNonWhitespace(calculator.getPassedExpression())) {
						assessableSTNodes.add(stNode);
					} else if(StringHelper.containsNonWhitespace(calculator.getScoreExpression())) {
						assessableSTNodes.add(stNode);
					}
				}
			}
		}, rootNode, true).visitAll();
		
		return assessableSTNodes;
	}
	
	/**
	 * The method compare the content of the cache used in assessment tool
	 * with the migrated values and vice versa.
	 * 
	 * @param courseEntry
	 * @return
	 */
	private boolean verifyCourseAssessmentData(List<Identity> assessableIdentities, RepositoryEntry courseEntry) {
		//load the cache and fill it with the same amount of datas as in assessment tool
		final ICourse course = CourseFactory.loadCourse(courseEntry);
		final Long courseResourceableId = course.getResourceableId();
		
		StaticCacheWrapper cache = new StaticCacheWrapper();
		NewCachePersistingAssessmentManager assessmentManager = new NewCachePersistingAssessmentManager(course, cache);
		assessmentManager.preloadCache(assessableIdentities);
		dbInstance.commitAndCloseSession();
		
		Set<Identity> assessableIdentitySet = new HashSet<>(assessableIdentities);
		List<AssessmentEntryImpl> nodeAssessments = loadAssessmentEntries(courseEntry);
		Map<AssessmentDataKey, AssessmentEntryImpl> nodeAssessmentMap = new HashMap<>();
		for(AssessmentEntryImpl nodeAssessment:nodeAssessments) {
			if(!assessableIdentitySet.contains(nodeAssessment.getIdentity())) {
				assessmentManager.preloadCache(nodeAssessment.getIdentity());
			}
			if(nodeAssessment.getIdentity() != null && nodeAssessment.getRepositoryEntry() != null
					&& nodeAssessment.getRepositoryEntry().getOlatResource() != null) {
				nodeAssessmentMap.put(new AssessmentDataKey(nodeAssessment), nodeAssessment);
			}
		}
		dbInstance.commitAndCloseSession();
		
		//compare the value in CourseNodeAssessment with the content of the cache
		boolean allOk = true;
		for(AssessmentEntryImpl nodeAssessment:nodeAssessments) {
			allOk &= compareCourseNodeAssessment(nodeAssessment, assessmentManager, course, courseEntry);
		}
		dbInstance.commitAndCloseSession();
		
		//compare the content of the cache with the CourseNodeAssessments
		for(NewCacheKey cacheKey:cache.getKeys()) {
			Map<String,Serializable> dataMap = cache.get(cacheKey);
			Long identityKey = cacheKey.getIdentityKey();
			for(Map.Entry<String, Serializable> data:dataMap.entrySet()) {
				String key = data.getKey();
				int index = key.indexOf("_");
				if(index > 0 && !key.equals("LAST_MODIFIED")) {
					String nodeIdent = key.substring(0, index);
					String dataType = key.substring(index + 1);
					
					AssessmentDataKey assessmentDataKey = new AssessmentDataKey(identityKey, courseResourceableId, nodeIdent);
					AssessmentEntryImpl nodeAssessment = nodeAssessmentMap.get(assessmentDataKey);
					allOk &= compareProperty(dataType, data.getValue(), nodeAssessment, courseEntry, nodeIdent, identityKey);
				}
			}
		}
		dbInstance.commitAndCloseSession();
		
		if(!allOk) {
			log.error("Critical error during course verification: " + courseEntry.getDisplayname() + "(" + courseEntry.getKey() + ")");
		}
		return allOk;
	}
	
	private boolean compareProperty(String dataType, Serializable value, AssessmentEntryImpl nodeAssessment, RepositoryEntry courseEntry, String nodeIdent, Long identityKey) {
		boolean allOk = true;
		if(nodeAssessment == null) {
			log.info(Tracing.M_AUDIT, "ERROR nodeAssessment not found: " + getErrorAt(courseEntry, nodeIdent, identityKey));
			allOk = false;
		} else if(ATTEMPTS.equals(dataType)) {
			if((value == null && nodeAssessment.getAttempts() == null) ||
					(value != null && nodeAssessment.getAttempts() != null && value.equals(nodeAssessment.getAttempts()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR number of attempts: " + value + " / " + nodeAssessment.getAttempts() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(PASSED.equals(dataType)) {
			if((value == null && nodeAssessment.getPassed() == null) ||
					(value != null && nodeAssessment.getPassed() != null && value.equals(nodeAssessment.getPassed()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR passed: " + value + " / " + nodeAssessment.getPassed() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(FULLY_ASSESSED.equals(dataType)) {
			if((value == null && nodeAssessment.getFullyAssessed() == null) ||
					(value != null && nodeAssessment.getFullyAssessed() != null && value.equals(nodeAssessment.getFullyAssessed()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR fullyAssessed: " + value + " / " + nodeAssessment.getFullyAssessed() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(SCORE.equals(dataType)) {
			if((value == null && nodeAssessment.getScore() == null) ||
					(value instanceof Float && nodeAssessment.getScore() != null && Math.abs(((Float)value).floatValue() - nodeAssessment.getScore().floatValue()) < 0.00001f)) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR score: " + value + " / " + nodeAssessment.getScore() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		}
		return allOk;
	}

	private String getErrorAt(RepositoryEntry courseEntry, String nodeIdent, Long identityKey) {
		return " at course : " + courseEntry.getDisplayname() + " (" + courseEntry.getKey() + ")"
			+ " at node: " + nodeIdent + " for identity: " + identityKey;
	}
	
	private boolean compareCourseNodeAssessment(AssessmentEntryImpl entry, NewCachePersistingAssessmentManager assessmentManager,
			ICourse course, RepositoryEntry courseEntry) {
		CourseNode node = course.getRunStructure().getNode(entry.getSubIdent());
		if(node == null) {
			CourseEditorTreeNode editorNode = course.getEditorTreeModel().getCourseEditorNodeById(entry.getSubIdent());
			if(editorNode != null) {
				node = editorNode.getCourseNode();
			}
		}
		
		boolean allOk = true;
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(node);
		if(assessmentConfig.isAssessable() && !(node instanceof STCourseNode)) {
			Identity assessedIdentity = entry.getIdentity();
			
			Integer attempts = assessmentManager.getNodeAttempts(node, assessedIdentity);
			if((attempts == null && entry.getAttempts() == null) ||
					(attempts != null && entry.getAttempts() != null && attempts.equals(entry.getAttempts()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR number of attempts: " + attempts + " / " + entry.getAttempts() + getErrorAt(courseEntry, node));
				allOk &= false;
			}
			
			Boolean passed = assessmentManager.getNodePassed(node, assessedIdentity);
			if((passed == null && entry.getPassed() == null) ||
					(passed != null && entry.getPassed() != null && passed.equals(entry.getPassed()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR passed: " + passed + " / " + entry.getPassed() + getErrorAt(courseEntry, node));
				allOk &= false;
			}

			Boolean fullyAssessed = assessmentManager.getNodeFullyAssessed(node, assessedIdentity);
			if((fullyAssessed == null && entry.getFullyAssessed() == null) ||
					(fullyAssessed != null && entry.getFullyAssessed() != null && fullyAssessed.equals(entry.getFullyAssessed()))) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR fullyAssessed: " + fullyAssessed + " / " + entry.getFullyAssessed() + getErrorAt(courseEntry, node));
				allOk &= false;
			}

			Float score = assessmentManager.getNodeScore(node, assessedIdentity);
			if((score == null && entry.getScore() == null) ||
					(score != null && entry.getScore() != null && Math.abs(score.floatValue() - entry.getScore().floatValue()) < 0.00001f)) {
				//ok
			} else {
				log.info(Tracing.M_AUDIT, "ERROR score: " + score + " / " + entry.getScore() + getErrorAt(courseEntry, node));
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private String getErrorAt(RepositoryEntry courseEntry, CourseNode courseNode) {
		return " at course : " + courseEntry.getDisplayname() + " (" + courseEntry.getKey() + ")"
			+ " at node: " + courseNode.getShortTitle() + " (" + courseNode.getIdent() + ")";
	}
	
	/**
	 * Return the same amount of user as in the assessment tool.
	 * @param entry
	 * @return
	 */
	private List<Identity> getAllAssessableIdentities(ICourse course, RepositoryEntry entry) {
		Set<Identity> duplicateKiller = new HashSet<>();
		List<Identity> assessableIdentities = new ArrayList<>();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> coachedGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
		
		List<Identity> participants = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
		for(Identity participant:participants) {
			if(!duplicateKiller.contains(participant)) {
				assessableIdentities.add(participant);
				duplicateKiller.add(participant);
			}
		}
		

		List<Identity> courseParticipants = repositoryService.getMembers(entry, RepositoryEntryRelationType.defaultGroup,  GroupRoles.participant.name());
		for(Identity participant:courseParticipants) {
			if(!duplicateKiller.contains(participant)) {
				assessableIdentities.add(participant);
				duplicateKiller.add(participant);
			}
		}

		List<Identity> assessedUsers = getAllIdentitiesWithCourseAssessmentData(course.getResourceableId());
		for(Identity assessedUser:assessedUsers) {
			if(!duplicateKiller.contains(assessedUser)) {
				assessableIdentities.add(assessedUser);
				duplicateKiller.add(assessedUser);
			}
		}

		return assessableIdentities;
	}
	
	private List<Identity> getAllIdentitiesWithCourseAssessmentData(Long resourceId) {
		StringBuilder query = new StringBuilder();
		query.append("select p.identity from ")
			.append(Property.class.getName()).append(" as p")
			.append(" where p.resourceTypeName = 'CourseModule'")
			.append(" and p.resourceTypeId = :resid")
			.append(" and p.identity is not null")
			.append(" and p.name in ('").append(SCORE).append("','").append(PASSED).append("')");

		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Identity.class)
				.setParameter("resid", resourceId)
				.getResultList();
	}

	private AssessmentEntryImpl createAssessmentEntry(Identity assessedIdentity, Property property, ICourse course, RepositoryEntry courseEntry, String nodeIdent) {
		AssessmentEntryImpl entry = new AssessmentEntryImpl();
		if(property == null) {
			entry.setCreationDate(new Date());
			entry.setLastModified(entry.getCreationDate());
		} else {
			entry.setCreationDate(property.getCreationDate());
			entry.setLastModified(property.getLastModified());
		}
		entry.setIdentity(assessedIdentity);
		entry.setRepositoryEntry(courseEntry);
		entry.setSubIdent(nodeIdent);
		entry.setAttempts(new Integer(0));
		entry.setUserVisibility(Boolean.TRUE);

		CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
		if(courseNode != null) {
			if(courseNode.needsReferenceToARepositoryEntry()) {
				RepositoryEntry referenceEntry = courseNode.getReferencedRepositoryEntry();
				entry.setReferenceEntry(referenceEntry);
			}
	
			if(courseNode instanceof GTACourseNode) {
				processAssessmentPropertyForGTA(assessedIdentity, entry, (GTACourseNode)courseNode, courseEntry);
			} else if(courseNode instanceof TACourseNode) {
				processAssessmentPropertyForTA(assessedIdentity, entry, (TACourseNode)courseNode, course);
			} else if(courseNode instanceof IQTESTCourseNode) {
				processAssessmentPropertyForIQTEST(assessedIdentity, entry, (IQTESTCourseNode)courseNode, course);
			} else if(courseNode instanceof MSCourseNode) {
				entry.setAssessmentStatus(AssessmentEntryStatus.inReview);
			} else if(courseNode instanceof BasicLTICourseNode) {
				processAssessmentPropertyForBasicLTI(assessedIdentity, entry, (BasicLTICourseNode)courseNode, course);
			} else if(courseNode instanceof ScormCourseNode) {
				String username = assessedIdentity.getName();
				Map<Date, List<CmiData>> rawDatas = ScormAssessmentManager.getInstance()
						.visitScoDatasMultiResults(username, course.getCourseEnvironment(), courseNode);
				if(rawDatas != null && rawDatas.size() > 0) {
					entry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
				} else {
					entry.setAssessmentStatus(AssessmentEntryStatus.notStarted);
				}
			}
		}
		return entry;
	}

	private void processAssessmentPropertyForBasicLTI(Identity assessedIdentity, AssessmentEntryImpl entry, BasicLTICourseNode cNode, ICourse course) {
		List<LoggingObject> objects = getLoggingObject(assessedIdentity, cNode, course);
		if(objects.size() > 0) {
			entry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
		} else {
			entry.setAssessmentStatus(AssessmentEntryStatus.notStarted);
		}
	}
	
	/**
	 * Use the log of the navigation handler of the course to know if the user
	 * launched the specified course element.
	 * 
	 * @param user
	 * @param courseNode
	 * @param course
	 * @return
	 */
	private List<LoggingObject> getLoggingObject(IdentityRef user, CourseNode courseNode, ICourse course) {
		StringBuilder query = new StringBuilder();
		query.append("select log from ").append(LoggingObject.class.getName()).append(" log")
		     .append(" where log.userId=:userId and sourceclass='org.olat.course.run.navigation.NavigationHandler'")
		     .append(" and parentResType='CourseModule' and parentResId=:courseId")
		     .append(" and targetResId=:targetResId")
		     .append(" and actionVerb='launch' and actionObject='node'");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), LoggingObject.class)
				.setParameter("userId", user.getKey())
				.setParameter("courseId", course.getResourceableId().toString())
				.setParameter("targetResId", courseNode.getIdent())
				.getResultList();
	}
	
	/**
	 * If a QTI ser is found, the test is in progress. If not, check if some result set is available
	 * and if the test has essay to set the status as inReview or done.
	 * 
	 * @param assessedIdentity
	 * @param entry
	 * @param cNode
	 * @param course
	 */
	private void processAssessmentPropertyForIQTEST(Identity assessedIdentity, AssessmentEntryImpl entry, IQTESTCourseNode cNode, ICourse course) {
		entry.setAssessmentStatus(AssessmentEntryStatus.notStarted);
		
		if(iqTestPersisterExists(assessedIdentity, cNode, course)) {
			entry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
		} else {
			RepositoryEntry ref = cNode.getReferencedRepositoryEntry();
			if(ref != null) {
				Long courseResourceableId = course.getResourceableId();
				List<QTIResultSet> resultSets = qtiResultManager.getResultSets(courseResourceableId, cNode.getIdent(), ref.getKey(), assessedIdentity);
				if(resultSets.size() > 0) {
					if(QTIResourceTypeModule.isOnyxTest(ref.getOlatResource())) {
						//make it later with the flag fully assessed
						entry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
					} else if(checkEssay(ref)) {
						entry.setAssessmentStatus(AssessmentEntryStatus.inReview);
					} else {
						entry.setAssessmentStatus(AssessmentEntryStatus.done);
					}
				}
			}	
		}
	}
	
	private boolean iqTestPersisterExists(Identity assessedIdentity, IQTESTCourseNode cNode, ICourse course) {
		Long courseResourceableId = course.getResourceableId();
		String resourcePath = courseResourceableId + File.separator + cNode.getIdent();
		FilePersister qtiPersister = new FilePersister(assessedIdentity, resourcePath);
		return qtiPersister.exists();
	}

	private boolean checkEssay(RepositoryEntry testEntry) {
		if(qtiEssayMap.containsKey(testEntry.getKey())) {
			return qtiEssayMap.get(testEntry.getKey()).booleanValue();
		}
		
		TestFileResource fr = new TestFileResource();
		fr.overrideResourceableId(testEntry.getOlatResource().getResourceableId());
		
		TransientIdentity pseudoIdentity = new TransientIdentity();
		pseudoIdentity.setName("transient");
		Translator translator = Util.createPackageTranslator(QTIModule.class, Locale.ENGLISH);
		try {
			QTIEditorPackage qtiPackage = new QTIEditorPackageImpl(pseudoIdentity, fr, null, translator);
			if(qtiPackage.getQTIDocument() != null && qtiPackage.getQTIDocument().getAssessment() != null) {
				Assessment ass = qtiPackage.getQTIDocument().getAssessment();
				//Sections with their Items
				List<Section> sections = ass.getSections();
				for (Section section:sections) {
					List<Item> items = section.getItems();
					for (Item item:items) {
						String ident = item.getIdent();
						if(ident != null && ident.startsWith("QTIEDIT:ESSAY")) {
							qtiEssayMap.put(testEntry.getKey(), Boolean.TRUE);
							return true;
						}
					}
				}
			}
		} catch (OLATRuntimeException e) {
			log.warn("QTI without content in repository entry: " + testEntry.getKey(), e);
		}
		qtiEssayMap.put(testEntry.getKey(), Boolean.FALSE);
		return false;
	}
	
	/**
	 * Search first for the status of the task, if not found, see if some documents
	 * where dropped or returned, in this case, the status is in review, if not
	 * the status is not started.
	 * 
	 * @param assessedIdentity
	 * @param entry
	 * @param tNode
	 * @param course
	 */
	private void processAssessmentPropertyForTA(Identity assessedIdentity, AssessmentEntryImpl entry, TACourseNode tNode, ICourse course) {
		List<Property> samples = course.getCourseEnvironment().getCoursePropertyManager()
				.findCourseNodeProperties(tNode, assessedIdentity, null, TaskController.PROP_ASSIGNED);
		if (samples.size() > 0) {
			String details = samples.get(0).getStringValue();
			entry.setDetails(details);
		}
		
		Property statusProperty = course.getCourseEnvironment().getCoursePropertyManager()
				.findCourseNodeProperty(tNode, assessedIdentity, null, StatusManager.PROPERTY_KEY_STATUS);
		AssessmentEntryStatus assessmentStatus = null;
		if (statusProperty != null) {
			String status = statusProperty.getStringValue();
			if(status != null) {
				switch(status) {
					case StatusForm.STATUS_VALUE_NOT_OK: assessmentStatus = AssessmentEntryStatus.inProgress; break;
					case StatusForm.STATUS_VALUE_OK: assessmentStatus = AssessmentEntryStatus.done; break;
					case StatusForm.STATUS_VALUE_WORKING_ON: assessmentStatus = AssessmentEntryStatus.inProgress; break;
					case StatusForm.STATUS_VALUE_UNDEFINED: assessmentStatus = AssessmentEntryStatus.inProgress; break;
				}
			}
		}
		if(assessmentStatus == null) {
			String dropbox = DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), tNode) + File.separator + assessedIdentity.getName();
			LocalFolderImpl dropBox = VFSManager.olatRootContainer(dropbox, null);
			boolean hasDropped = (dropBox.getBasefile().exists() && dropBox.getBasefile().listFiles(SystemFileFilter.FILES_ONLY).length > 0);
			
			String returnbox = ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), tNode) + File.separator + assessedIdentity.getName();
			LocalFolderImpl returnBox = VFSManager.olatRootContainer(returnbox, null);
			boolean hasReturned = (returnBox.getBasefile().exists() && returnBox.getBasefile().listFiles(SystemFileFilter.FILES_ONLY).length > 0);
			
			if(hasReturned || hasDropped) {
				assessmentStatus = AssessmentEntryStatus.inReview;
			} else {
				assessmentStatus = AssessmentEntryStatus.notStarted;
			}
		}
		entry.setAssessmentStatus(assessmentStatus);
	}
	
	
	private void processAssessmentPropertyForGTA(Identity assessedIdentity, AssessmentEntryImpl entry, GTACourseNode cNode, RepositoryEntry courseEntry) {
		List<Task> tasks = gtaManager.getTasks(assessedIdentity, courseEntry, cNode);
		if(tasks != null && !tasks.isEmpty()) {
			Task task = tasks.get(0);
			AssessmentEntryStatus status = gtaManager.convertToAssessmentEntryStatus(task, cNode);
			entry.setStatus(status.name());
			
			String details = gtaManager.getDetails(assessedIdentity, courseEntry, cNode);
			entry.setDetails(details);
		}
	}

	private void copyAssessmentProperty(Property property, AssessmentEntryImpl nodeAssessment, ICourse course) {
		String propertyName = property.getName();
		if (propertyName.equals(ATTEMPTS)) {
			if(property.getLongValue() != null) {
				nodeAssessment.setAttempts(property.getLongValue().intValue());
			}
		} else if (propertyName.equals(SCORE)) {
			if(property.getFloatValue() != null) {
				BigDecimal score = new BigDecimal(Float.toString(property.getFloatValue().floatValue()));
				nodeAssessment.setScore(score);
				postCopyPassedScore(nodeAssessment, course);
			}
		} else if (propertyName.equals(PASSED)) {
			if(StringHelper.containsNonWhitespace(property.getStringValue())) {
				nodeAssessment.setPassed(new Boolean(property.getStringValue()));
				postCopyPassedScore(nodeAssessment, course);
			}
		} else if(propertyName.equals(FULLY_ASSESSED)) {
			if(StringHelper.containsNonWhitespace(property.getStringValue())) {
				Boolean fullyAssessed = new Boolean(property.getStringValue());
				nodeAssessment.setFullyAssessed(fullyAssessed);
				if(nodeAssessment.getStatus() == null
						|| nodeAssessment.getAssessmentStatus() == AssessmentEntryStatus.notStarted
						|| nodeAssessment.getAssessmentStatus() == AssessmentEntryStatus.inProgress) {
					if(fullyAssessed.booleanValue()) {
						nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
					} else {
						nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
					}
				}
			}
		} else if (propertyName.equals(ASSESSMENT_ID)) {
			nodeAssessment.setAssessmentId(property.getLongValue());
		} else if (propertyName.equals(COMMENT)) {
			nodeAssessment.setComment(property.getTextValue());
		} else if(propertyName.equals(COACH_COMMENT)) {
			nodeAssessment.setCoachComment(property.getTextValue());
		} else if(propertyName.equals(TaskController.PROP_ASSIGNED)) {
			nodeAssessment.setDetails(property.getStringValue());
		}
	}
	
	/**
	 * Used if a passed or score value was set.
	 * @param nodeAssessment
	 * @param course
	 */
	private void postCopyPassedScore(AssessmentEntry entry, ICourse course) {
		String nodeIdent = entry.getSubIdent();
		CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
		if(courseNode instanceof GTACourseNode) {
			//
		} else if(courseNode instanceof TACourseNode) {
			entry.setAssessmentStatus(AssessmentEntryStatus.done);
		} else if(courseNode instanceof IQTESTCourseNode) {
			//
		} else if(courseNode instanceof PortfolioCourseNode) {
			entry.setAssessmentStatus(AssessmentEntryStatus.done);
		} else if(courseNode instanceof MSCourseNode) {
			entry.setAssessmentStatus(AssessmentEntryStatus.done);
		} else if(courseNode instanceof BasicLTICourseNode) {
			entry.setAssessmentStatus(AssessmentEntryStatus.done);
		} else if(courseNode instanceof ScormCourseNode) {
			entry.setAssessmentStatus(AssessmentEntryStatus.done);	
		}
	}
	
	private List<AssessmentEntryImpl> loadAssessmentEntries(RepositoryEntry courseEntry) {
		String sb = "select data from assessmententry data where data.repositoryEntry.key=:courseEntryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, AssessmentEntryImpl.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.getResultList();
	}
	
	private List<Long> loadIdentityKeyOfAssessmentEntries(RepositoryEntry courseEntry, String subIdent) {
		String sb = "select data.identity.key from assessmententry data where data.repositoryEntry.key=:courseEntryKey and data.subIdent=:subIdent";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Long.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	private List<Property> loadAssessmentProperties(RepositoryEntry course) {
		StringBuilder sb = new StringBuilder();
		sb.append("from org.olat.properties.Property as p")
		  .append(" inner join fetch p.identity as ident ")
		  .append(" inner join fetch ident.user as user ")
		  .append(" where p.resourceTypeId = :restypeid and p.resourceTypeName = :restypename")
		  .append(" and p.name in ('")
		  .append(ATTEMPTS).append("','")
		  .append(SCORE).append("','")
		  .append(FULLY_ASSESSED).append("','")
		  .append(PASSED).append("','")
		  .append(ASSESSMENT_ID).append("','")
		  .append(COMMENT).append("','")
		  .append(COACH_COMMENT).append("','")
		  .append(TaskController.PROP_ASSIGNED).append("','")
		  .append(StatusManager.PROPERTY_KEY_STATUS).append("','")
		  .append("')");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("restypename", course.getOlatResource().getResourceableTypeName())
				.setParameter("restypeid", course.getOlatResource().getResourceableId())
				.getResultList();	
	}
	
	private static class AssessmentDataKey {
		
		private final Long courseId;
		private final Long identityKey;
		private final String courseNodeIdent;
		
		public AssessmentDataKey(Identity identity, Long courseOresId, String courseNodeIdent) {
			this.courseId = courseOresId;
			this.identityKey = identity.getKey();
			this.courseNodeIdent = courseNodeIdent;
		}
		
		public AssessmentDataKey(Long identityKey, Long courseOresId, String courseNodeIdent) {
			this.courseId = courseOresId;
			this.identityKey = identityKey;
			this.courseNodeIdent = courseNodeIdent;
		}
		
		public AssessmentDataKey(AssessmentEntry nodeAssessment) {
			this.courseId = nodeAssessment.getRepositoryEntry().getOlatResource().getResourceableId();
			this.identityKey = nodeAssessment.getIdentity().getKey();
			this.courseNodeIdent = nodeAssessment.getSubIdent();
		}

		@Override
		public int hashCode() {
			return (courseId == null ? 32876 : courseId.hashCode())
					+ (identityKey == null ? 7525 : identityKey.hashCode())
					+ (courseNodeIdent == null ? 39841 : courseNodeIdent.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof AssessmentDataKey) {
				AssessmentDataKey key = (AssessmentDataKey)obj;
				return courseId != null && courseId.equals(key.courseId)
						&& identityKey != null && identityKey.equals(key.identityKey)
						&& courseNodeIdent != null && courseNodeIdent.equals(key.courseNodeIdent);	
			}	
			return false;
		} 	
	}
	
	private static class StaticCacheWrapper implements CacheWrapper<NewCacheKey,HashMap<String, Serializable>> {
		
		private ConcurrentHashMap<NewCacheKey,HashMap<String, Serializable>> map = new ConcurrentHashMap<>();

		@Override
		public boolean containsKey(NewCacheKey key) {
			return map.containsKey(key);
		}

		@Override
		public HashMap<String, Serializable> get(NewCacheKey key) {
			return map.get(key);
		}

		@Override
		public HashMap<String, Serializable> update(NewCacheKey key, HashMap<String, Serializable> value) {
			return map.put(key, value);
		}

		@Override
		public HashMap<String, Serializable> put(NewCacheKey key, HashMap<String, Serializable> value) {
			return map.put(key, value);
		}

		@Override
		public HashMap<String, Serializable> put(NewCacheKey key, HashMap<String, Serializable> value, int expirationTime) {
			return map.put(key, value);
		}

		@Override
		public HashMap<String, Serializable> putIfAbsent(NewCacheKey key, HashMap<String, Serializable> value) {
			return map.putIfAbsent(key, value);
		}

		@Override
		public HashMap<String, Serializable> replace(NewCacheKey key, HashMap<String, Serializable> value) {
			return map.replace(key, value);
		}

		@Override
		public HashMap<String, Serializable> computeIfAbsent(NewCacheKey key,
				Function<? super NewCacheKey, ? extends HashMap<String, Serializable>> mappingFunction) {
			return map.computeIfAbsent(key, mappingFunction);
		}

		@Override
		public List<NewCacheKey> getKeys() {
			return new ArrayList<>(map.keySet());
		}

		@Override
		public HashMap<String, Serializable> remove(NewCacheKey key) {
			return map.remove(key);
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public long maxCount() {
			return -1;
		}

		@Override
		public Iterator<NewCacheKey> iterateKeys() {
			return map.keySet().iterator();
		}

		@Override
		public void clear() {
			//
		}

		@Override
		public void addListener(Object obj) {
			//
		}
	}
}
