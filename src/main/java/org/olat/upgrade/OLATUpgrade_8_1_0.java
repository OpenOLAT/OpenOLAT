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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.id.Identity;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_1_0 extends OLATUpgrade {

	private static final String TASK_EFFICIENCY_STATEMENT = "Upgrade efficiency statement";
	private static final String TASK_LAUNCH_DATES = "Upgrade launch dates";
	private static final int REPO_ENTRIES_BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_8.1.0";
	
	private static final String PROPERTY_INITIAL_LAUNCH_DATE = "initialCourseLaunchDate";
	private static final String PROPERTY_RECENT_LAUNCH_DATE = "recentCourseLaunchDate";
	private static final String ASSESSMENT_PUBLISHER = "assessmentPublisher";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private BaseSecurity securityManager;

	public OLATUpgrade_8_1_0() {
		super();
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
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		upgradeEfficiencyStatements(upgradeManager, uhd);
		upgradeLaunchDates(upgradeManager, uhd);
		upgradeAssessmentPublisher(upgradeManager, uhd);

		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		log.audit("Finished OLATUpgrade_8_1_0 successfully!");
		return true;
	}
	
	private void upgradeAssessmentPublisher(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(ASSESSMENT_PUBLISHER)) {

			int count = 0;
			List<Publisher> publishers = getAssessmentPublishers();
			for(Publisher publisher:publishers) {
				String businessPath = publisher.getBusinessPath();
				if(businessPath != null && businessPath.startsWith("[RepositoryEntry:") && !businessPath.endsWith("[assessmentTool:0]")) {
					publisher.setBusinessPath(businessPath + "[assessmentTool:0]");
					dbInstance.updateObject(publisher);
				}
				if(count++ % 20 == 0) {
					dbInstance.intermediateCommit();
				}
			}
			uhd.setBooleanDataValue(ASSESSMENT_PUBLISHER, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private List<Publisher> getAssessmentPublishers() {
		StringBuilder sb = new StringBuilder();
		sb.append("select pub from notipublisher pub where pub.resName='AssessmentManager' and type='AssessmentManager'");
		DBQuery query = dbInstance.createQuery(sb.toString());
		@SuppressWarnings("unchecked")
		List<Publisher> res = query.list();
		return res;
	}
	
	private void upgradeEfficiencyStatements(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_EFFICIENCY_STATEMENT)) {

			int counter = 0;
			List<Property> properties;
			do {
				properties = getEfficiencyStatement(counter);
				for(Property property:properties) {
					createStatement(property);
				}
				counter += properties.size();
				log.audit("Processed efficiency statement: " + properties.size());
			} while(properties.size() == REPO_ENTRIES_BATCH_SIZE);

			uhd.setBooleanDataValue(TASK_EFFICIENCY_STATEMENT, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void createStatement(Property property) {
		String repoKeyStr = property.getName();
		Long repoKey = new Long(repoKeyStr);
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoKey, false);
		UserEfficiencyStatementImpl impl = efficiencyStatementManager.getUserEfficiencyStatementFull(re, property.getIdentity());
		if(impl != null) {
			return;
		}

		UserEfficiencyStatementImpl statement = new UserEfficiencyStatementImpl();
		statement.setIdentity(property.getIdentity());
		statement.setStatementXml(property.getTextValue());
		if(re != null) {
			statement.setResource(re.getOlatResource());
			statement.setCourseRepoKey(re.getKey());
		}

		EfficiencyStatement s = (EfficiencyStatement)XStreamHelper.createXStreamInstance().fromXML(property.getTextValue());
		efficiencyStatementManager.fillEfficiencyStatement(s, null, statement);
		statement.setLastModified(property.getLastModified());
		
		dbInstance.saveObject(statement);
		dbInstance.commitAndCloseSession();
	}
	
	private List<Property> getEfficiencyStatement(int firstResult) {
		StringBuilder query = new StringBuilder();
		query.append("select p from ").append(Property.class.getName()).append(" as p ");
		query.append(" where p.category='efficiencyStatement' order by p.key");
		
		DBQuery dbQuery = dbInstance.createQuery(query.toString());
		dbQuery.setFirstResult(firstResult);
		dbQuery.setMaxResults(REPO_ENTRIES_BATCH_SIZE);
		@SuppressWarnings("unchecked")
		List<Property> props = dbQuery.list();
		return props;
	}
	
	private void upgradeLaunchDates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_LAUNCH_DATES)) {
			int count = 0;
			Set<SimpleProp> props = getLaunchProperties();
			if(props == null) {
				//problems
				uhd.setBooleanDataValue(TASK_LAUNCH_DATES, false);	
			} else {
				for(SimpleProp prop:props) {
					Date d = getInitialLaunchDate(prop.resourceId, prop.identityKey);
					if(d == null) {
						createUserCourseInformation(prop);
					}
					
					if(count++ % 25 == 0) {
						dbInstance.commitAndCloseSession();
						log.info("Convert launch dates properties: " + count);
					}
				}
				uhd.setBooleanDataValue(TASK_LAUNCH_DATES, true);
			}
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private Date getInitialLaunchDate(Long courseResourceId, Long identityKey) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where infos.identity.key=:identityKey and resource.resId=:resId and resource.resName='CourseModule'");

			List<Date> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Date.class)
					.setParameter("identityKey", identityKey)
					.setParameter("resId", courseResourceId)
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + courseResourceId, e);
			return null;
		}
	}
	
	private void createUserCourseInformation(SimpleProp prop) {
		Identity identity = securityManager.loadIdentityByKey(prop.identityKey);
		OLATResource resource = resourceManager.findResourceable(prop.resourceId, "CourseModule");

		UserCourseInfosImpl infos = new UserCourseInfosImpl();
		infos.setIdentity(identity);
		infos.setCreationDate(new Date());
		infos.setLastModified(new Date());
		infos.setInitialLaunch(prop.initialLaunch);
		infos.setRecentLaunch(prop.recentLaunch);
		infos.setVisit(1);
		infos.setResource(resource);
		dbInstance.saveObject(infos);
	}
	
	private Set<SimpleProp> getLaunchProperties() {

		try {
			StringBuilder query = new StringBuilder();
			query.append("select p.resourceTypeId, p.identity.key, p.name, p.stringValue from ").append(Property.class.getName()).append(" as p ");
			query.append(" where p.resourceTypeName='CourseModule' and p.name in ('initialCourseLaunchDate','recentCourseLaunchDate')");
			
			DBQuery dbQuery = dbInstance.createQuery(query.toString());
			@SuppressWarnings("unchecked")
			List<Object[]> props = dbQuery.list();
			
			Calendar cal = Calendar.getInstance();
			Map<SimpleProp, SimpleProp> simpleProps = new HashMap<SimpleProp, SimpleProp>((2 * props.size()) + 1);
			for(Object[] prop:props) {
				SimpleProp simpleProp = new SimpleProp();
				simpleProp.resourceId = (Long)prop[0];
				simpleProp.identityKey = (Long)prop[1];
				if(simpleProps.containsKey(simpleProp)) {
					simpleProp = simpleProps.get(simpleProp);
				}

				String name = (String)prop[2];
				try {
					long time = Long.valueOf((String)prop[3]);
					cal.setTimeInMillis(time);
					if(PROPERTY_INITIAL_LAUNCH_DATE.equals(name)) {
						simpleProp.initialLaunch = cal.getTime();
					}else if(PROPERTY_RECENT_LAUNCH_DATE.equals(name)) {
						simpleProp.recentLaunch = cal.getTime();
					}
					simpleProps.put(simpleProp, simpleProp);
				} catch(Exception e) {
					log.error("", e);
				}
			}
			return simpleProps.keySet();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}
	
	private class SimpleProp {
		private Long resourceId;
		private Long identityKey;
		private Date initialLaunch;
		private Date recentLaunch;
		
		@Override
		public int hashCode() {
			return (resourceId == null ? -1 : resourceId.hashCode())
					+ (identityKey == null ? -1 : identityKey.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof SimpleProp) {
				SimpleProp prop = (SimpleProp)obj;
				return resourceId != null && resourceId.equals(prop.resourceId)
						&& identityKey != null && identityKey.equals(prop.identityKey);
			}
			return false;
		}
	}
}
