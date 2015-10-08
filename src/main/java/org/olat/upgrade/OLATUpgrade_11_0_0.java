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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
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
	
	private static final int BATCH_SIZE = 50;
	private static final String ASSESSMENT_DATAS = "ASSESSMENT PROPERTY TABLE";
	private static final String VERSION = "OLAT_11.0.0";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;

	public OLATUpgrade_11_0_0() {
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
		allOk &= upgradeAssessmentPropertyTable(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_11_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_11_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeAssessmentPropertyTable(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_DATAS)) {
			int counter = 0;
			final Roles roles = new Roles(true, true, true, true, false, true, false);
			final SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(roles);
			params.setResourceTypes(Collections.singletonList("CourseModule"));
			
			List<RepositoryEntry> courses;
			do {
				courses = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, 50, true);
				for(RepositoryEntry course:courses) {
					allOk &= processCourseAssessmentData(course); 
				}
				counter += courses.size();
				log.audit("Assessment data migration processed: " + courses.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(courses.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(ASSESSMENT_DATAS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	// select count(*) from o_property where name in ('SCORE','PASSED','ATTEMPTS','COMMENT','COACH_COMMENT','ASSESSMENT_ID','FULLY_ASSESSED');
	private boolean processCourseAssessmentData(RepositoryEntry courseEntry) {
		Long courseResourceId = courseEntry.getOlatResource().getResourceableId();
		
		//load already migrated data
		List<AssessmentEntryImpl> currentNodeAssessmentList = loadAssessmentEntries(courseEntry);
		Map<AssessmentDataKey,AssessmentEntryImpl> curentNodeAssessmentMap = new HashMap<>();
		for(AssessmentEntryImpl currentNodeAssessment:currentNodeAssessmentList) {
			AssessmentDataKey key = new AssessmentDataKey(currentNodeAssessment.getIdentity(), courseResourceId, currentNodeAssessment.getSubIdent());
			curentNodeAssessmentMap.put(key, currentNodeAssessment);
		}

		Map<AssessmentDataKey,AssessmentEntryImpl> nodeAssessmentMap = new HashMap<>();
		
		List<Property> courseProperties = loadAssessmentProperties(courseEntry);
		for(Property property:courseProperties) {
			String propertyCategory = property.getCategory();
			if(StringHelper.containsNonWhitespace(propertyCategory)) {
				int nodeIdentIndex = propertyCategory.indexOf("::");
				if(nodeIdentIndex > 0) {
					String nodeIdent = propertyCategory.substring(propertyCategory.indexOf("::") + 2);
					AssessmentDataKey key = new AssessmentDataKey(property.getIdentity(), property.getResourceTypeId(), nodeIdent);
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
						nodeAssessment = createAssessmentEntry(property, courseEntry, nodeIdent, courseEntry);
					}
					copyAssessmentProperty(property, nodeAssessment);
					nodeAssessmentMap.put(key, nodeAssessment);	
				}
			}	
		}
		
		int count = 0;
		for(AssessmentEntryImpl courseNodeAssessment:nodeAssessmentMap.values()) {
			dbInstance.getCurrentEntityManager().persist(courseNodeAssessment);
			if(++count % 50 == 0) {
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
		
		return verifyCourseAssessmentData(courseEntry);
	}
	
	/**
	 * The method compare the content of the cache used in assessment tool
	 * with the migrated values and vice versa.
	 * 
	 * @param courseEntry
	 * @return
	 */
	private boolean verifyCourseAssessmentData(RepositoryEntry courseEntry) {
		//load the cache and fill it with the same amount of datas as in assessment tool
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource());
		StaticCacheWrapper cache = new StaticCacheWrapper();
		NewCachePersistingAssessmentManager assessmentManager = new NewCachePersistingAssessmentManager(course, cache);
		List<Identity> assessableIdentities = getAllAssessableIdentities(courseEntry);
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
					
					AssessmentDataKey assessmentDataKey = new AssessmentDataKey(identityKey, course.getResourceableId(), nodeIdent);
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
			log.audit("ERROR nodeAssessment not found: " + getErrorAt(courseEntry, nodeIdent, identityKey));
			allOk = false;
		} else if(ATTEMPTS.equals(dataType)) {
			if((value == null && nodeAssessment.getAttempts() == null) ||
					(value != null && nodeAssessment.getAttempts() != null && value.equals(nodeAssessment.getAttempts()))) {
				//ok
			} else {
				log.audit("ERROR number of attempts: " + value + " / " + nodeAssessment.getAttempts() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(PASSED.equals(dataType)) {
			if((value == null && nodeAssessment.getPassed() == null) ||
					(value != null && nodeAssessment.getPassed() != null && value.equals(nodeAssessment.getPassed()))) {
				//ok
			} else {
				log.audit("ERROR passed: " + value + " / " + nodeAssessment.getPassed() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(FULLY_ASSESSED.equals(dataType)) {
			if((value == null && nodeAssessment.getFullyAssessed() == null) ||
					(value != null && nodeAssessment.getFullyAssessed() != null && value.equals(nodeAssessment.getFullyAssessed()))) {
				//ok
			} else {
				log.audit("ERROR fullyAssessed: " + value + " / " + nodeAssessment.getFullyAssessed() + getErrorAt(courseEntry, nodeIdent, identityKey));
				allOk &= false;
			}
		} else if(SCORE.equals(dataType)) {
			if((value == null && nodeAssessment.getScore() == null) ||
					(value instanceof Float && nodeAssessment.getScore() != null && Math.abs(((Float)value).floatValue() - nodeAssessment.getScore().floatValue()) < 0.00001f)) {
				//ok
			} else {
				log.audit("ERROR score: " + value + " / " + nodeAssessment.getScore() + getErrorAt(courseEntry, nodeIdent, identityKey));
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
		if(node != null) {
			Identity assessedIdentity = entry.getIdentity();
			
			Integer attempts = assessmentManager.getNodeAttempts(node, assessedIdentity);
			if((attempts == null && entry.getAttempts() == null) ||
					(attempts != null && entry.getAttempts() != null && attempts.equals(entry.getAttempts()))) {
				//ok
			} else {
				log.audit("ERROR number of attempts: " + attempts + " / " + entry.getAttempts() + getErrorAt(courseEntry, node));
				allOk &= false;
			}
			
			Boolean passed = assessmentManager.getNodePassed(node, assessedIdentity);
			if((passed == null && entry.getPassed() == null) ||
					(passed != null && entry.getPassed() != null && passed.equals(entry.getPassed()))) {
				//ok
			} else {
				log.audit("ERROR passed: " + passed + " / " + entry.getPassed() + getErrorAt(courseEntry, node));
				allOk &= false;
			}

			Boolean fullyAssessed = assessmentManager.getNodeFullyAssessed(node, assessedIdentity);
			if((fullyAssessed == null && entry.getFullyAssessed() == null) ||
					(fullyAssessed != null && entry.getFullyAssessed() != null && fullyAssessed.equals(entry.getFullyAssessed()))) {
				//ok
			} else {
				log.audit("ERROR fullyAssessed: " + fullyAssessed + " / " + entry.getFullyAssessed() + getErrorAt(courseEntry, node));
				allOk &= false;
			}

			Float score = assessmentManager.getNodeScore(node, assessedIdentity);
			if((score == null && entry.getScore() == null) ||
					(score != null && entry.getScore() != null && Math.abs(score.floatValue() - entry.getScore().floatValue()) < 0.00001f)) {
				//ok
			} else {
				log.audit("ERROR score: " + score + " / " + entry.getScore() + getErrorAt(courseEntry, node));
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
	private List<Identity> getAllAssessableIdentities(RepositoryEntry entry) {
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
		

		List<Identity> courseParticipants = repositoryService.getMembers(entry, GroupRoles.participant.name());
		for(Identity participant:courseParticipants) {
			if(!duplicateKiller.contains(participant)) {
				assessableIdentities.add(participant);
				duplicateKiller.add(participant);
			}
		}

		ICourse course = CourseFactory.loadCourse(entry);
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

		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(query.toString(), Identity.class)
				.setParameter("resid", resourceId)
				.getResultList();
	}
	
	private AssessmentEntryImpl createAssessmentEntry(Property property, RepositoryEntry courseEntry,
			String nodeIdent, RepositoryEntry referenceEntry) {
		AssessmentEntryImpl entry = new AssessmentEntryImpl();
		entry.setCreationDate(property.getCreationDate());
		entry.setLastModified(property.getLastModified());
		entry.setIdentity(property.getIdentity());
		entry.setRepositoryEntry(courseEntry);
		entry.setSubIdent(nodeIdent);
		entry.setReferenceEntry(referenceEntry);
		//TODO qti ita/gta -> set status
		//TODO qti 1.2 -> find status ( test closed?)
		//TODO portfolio -> map.status (close status -> coach)
		//TODO ms -> score -> done
		//TODO lti -> score -> done
		//TODO SCORM -> score ( state finished?) 
		
		return entry;
	}

	private void copyAssessmentProperty(Property property, AssessmentEntry nodeAssessment) {
		String propertyName = property.getName();
		if (propertyName.equals(ATTEMPTS)) {
			if(property.getLongValue() != null) {
				nodeAssessment.setAttempts(property.getLongValue().intValue());
			}
		} else if (propertyName.equals(SCORE)) {
			if(property.getFloatValue() != null) {
				BigDecimal score = new BigDecimal(Float.toString(property.getFloatValue().floatValue()));
				nodeAssessment.setScore(score);
			}	
		} else if (propertyName.equals(PASSED)) {
			if(StringHelper.containsNonWhitespace(property.getStringValue())) {
				nodeAssessment.setPassed(new Boolean(property.getStringValue()));
			}
		} else if(propertyName.equals(FULLY_ASSESSED)) {
			if(StringHelper.containsNonWhitespace(property.getStringValue())) {
				nodeAssessment.setFullyAssessed(new Boolean(property.getStringValue()));
			}
		} else if (propertyName.equals(ASSESSMENT_ID)) {
			nodeAssessment.setAssessmentId(property.getLongValue());
		} else if (propertyName.equals(COMMENT)) {
			nodeAssessment.setComment(property.getTextValue());
		} else if(propertyName.equals(COACH_COMMENT)) {
			nodeAssessment.setCoachComment(property.getTextValue());
		}
	}
	
	private List<AssessmentEntryImpl> loadAssessmentEntries(RepositoryEntry courseEntry) {
		String sb = "select data from assessmententry data where data.repositoryEntry.key=:courseEntryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, AssessmentEntryImpl.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
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
		  .append(COACH_COMMENT)
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
		public HashMap<String, Serializable> putIfAbsent(NewCacheKey key, HashMap<String, Serializable> value) {
			return map.putIfAbsent(key, value);
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
		public Iterator<NewCacheKey> iterateKeys() {
			return map.keySet().iterator();
		}
	}
}
