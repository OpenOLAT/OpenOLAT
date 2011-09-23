/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course.assessment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataDeletable;

/**
 * Description:<br>
 * Methods to update a users efficiency statement and to retrieve such statements
 * from the database.
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class EfficiencyStatementManager extends BasicManager implements UserDataDeletable {
	//TODO remove as already definded in basic manager
	OLog log = Tracing.createLoggerFor(EfficiencyStatementManager.class);
	public static final String KEY_ASSESSMENT_NODES = "assessmentNodes";
	public static final String KEY_COURSE_TITLE = "courseTitle";
	private static final String PROPERTY_CATEGORY = "efficiencyStatement";
	private static EfficiencyStatementManager INSTANCE;
	
	/**
	 * Constructor
	 */
	private EfficiencyStatementManager(UserDeletionManager userDeletionManager) {
		userDeletionManager.getInstance().registerDeletableUserData(this);
		INSTANCE = this;
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static EfficiencyStatementManager getInstance() {
		return INSTANCE;
	}
	
	
	/**
	 * Updates the users efficiency statement for this course. <p>
	 * Called in AssessmentManager in a <code>doInSync</code> block, toghether with the saveScore.
	 * @param userCourseEnv
	 */
	protected void updateUserEfficiencyStatement(UserCourseEnvironment userCourseEnv) {
		Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId(); 
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(
				OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		updateUserEfficiencyStatement(userCourseEnv, re.getKey(), course, true);
	}
	
	/**
	 * Updates the users efficiency statement for this course
	 * @param userCourseEnv
	 * @param repoEntryKey
	 * @param checkForExistingProperty
	 */
	private void updateUserEfficiencyStatement(final UserCourseEnvironment userCourseEnv, final Long repoEntryKey, ICourse course, final boolean checkForExistingProperty) {
    //	o_clusterOK: by ld
		CourseConfig cc = userCourseEnv.getCourseEnvironment().getCourseConfig();
		// write only when enabled for this course
		if (cc.isEfficencyStatementEnabled()) {
			final boolean logDebug = log.isDebug();
			final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			final PropertyManager pm = PropertyManager.getInstance();					
			final String courseRepoEntryKey = getPropertyName(repoEntryKey);					
				
			CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode(); 
			List<Map<String,Object>> assessmentNodes = AssessmentHelper.addAssessableNodeAndDataToList(0, rootNode, userCourseEnv, true, true);
					
			EfficiencyStatement efficiencyStatement = new EfficiencyStatement();
			efficiencyStatement.setAssessmentNodes(assessmentNodes);
			efficiencyStatement.setCourseTitle(userCourseEnv.getCourseEnvironment().getCourseTitle());
			efficiencyStatement.setCourseRepoEntryKey(repoEntryKey);
			User user = identity.getUser();
			efficiencyStatement.setDisplayableUserInfo(user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null) + " (" + identity.getName() + ")");
			efficiencyStatement.setLastUpdated(System.currentTimeMillis());
					
			// save efficiency statement as xtream persisted list
			final String efficiencyStatementX = XStreamHelper.toXML(efficiencyStatement);  					
			Property efficiencyProperty = null;
			if (checkForExistingProperty) {
				efficiencyProperty = pm.findUserProperty(identity, PROPERTY_CATEGORY, courseRepoEntryKey);
			}
			if (assessmentNodes != null) {				
				if (efficiencyProperty == null) {
					// create new
					efficiencyProperty = pm.createUserPropertyInstance(identity, PROPERTY_CATEGORY, courseRepoEntryKey, null, null, null,	efficiencyStatementX);
					pm.saveProperty(efficiencyProperty);
					if (logDebug) log.debug("creating new efficiency statement property::" + efficiencyProperty.getKey() + " for id::"
									+ identity.getName() + " repoEntry::" + courseRepoEntryKey);
				} else {
					// update existing
					if (logDebug) log.debug("updatting efficiency statement property::" + efficiencyProperty.getKey() + " for id::"
									+ identity.getName() + " repoEntry::" + courseRepoEntryKey);
					efficiencyProperty.setTextValue(efficiencyStatementX);
					pm.updateProperty(efficiencyProperty);
				}
			} else {
				if (efficiencyProperty != null) {
					// remove existing since now empty empty efficiency statements
					if (logDebug) log.debug("removing efficiency statement property::" + efficiencyProperty.getKey() + " for id::"
									+ identity.getName() + " repoEntry::" + courseRepoEntryKey + " since empty");
					pm.deleteProperty(efficiencyProperty);
				}
				// else nothing to create and nothing to delete
			}					
			
			// send modified event to everybody
			AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED, identity);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
		}
	}
	
	/**
	 * LD: Debug method. 
	 * @param efficiencyStatement
	 */
	private void printEfficiencyStatement(EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		if (assessmentNodes != null) {
			Iterator<Map<String,Object>> iter = assessmentNodes.iterator();
			while (iter.hasNext()) {
				Map<String,Object> nodeData = iter.next();
				String title = (String)nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
				String score = (String)nodeData.get(AssessmentHelper.KEY_SCORE);
				Boolean passed = (Boolean)nodeData.get(AssessmentHelper.KEY_PASSED);
				Integer attempts = (Integer)nodeData.get(AssessmentHelper.KEY_ATTEMPTS);
				String attemptsStr = attempts==null ? null : String.valueOf(attempts.intValue());				
				System.out.println("title: " + title + " score: " + score + " passed: " + passed + " attempts: " + attemptsStr);				
			}
		}		
	}
	

	/**
	 * Get the user efficiency statement list for this course
	 * @param courseRepoEntryKey
	 * @param identity
	 * @return Map containing a list of maps that contain the nodeData for this user and course using the
	 * keys defined in the AssessmentHelper and the title of the course
	 */
	public EfficiencyStatement getUserEfficiencyStatement(Long courseRepoEntryKey, Identity identity){
		PropertyManager pm = PropertyManager.getInstance();
		Property efficiencyProperty;

		efficiencyProperty = pm.findUserProperty(identity,PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey));
		if (efficiencyProperty == null) {
			return null;
		} else {
			return (EfficiencyStatement) XStreamHelper.fromXML(efficiencyProperty.getTextValue());
		}
	}

	/**
	 * Get the passed value of a course node of a specific efficiency statment
	 * @param nodeIdent
	 * @param efficiencyStatement
	 * @return true if passed, false if not, null if node not found
	 */
	public Boolean getPassed(String nodeIdent, EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		if (assessmentNodes != null) {
			Iterator<Map<String,Object>> iter = assessmentNodes.iterator();
			while (iter.hasNext()) {
				Map<String,Object> nodeData = iter.next();
				if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
					return (Boolean) nodeData.get(AssessmentHelper.KEY_PASSED);
				}
			}
		}
		return null;
	}

	/**
	 * Get the score value of a course node of a specific efficiency statment
	 * @param nodeIdent
	 * @param efficiencyStatement
	 * @return the score, null if node not found
	 */
	public Double getScore(String nodeIdent, EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		if (assessmentNodes != null) {
			Iterator<Map<String,Object>> iter = assessmentNodes.iterator();
			while (iter.hasNext()) {
				Map<String,Object> nodeData = iter.next();
				if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
					String scoreString = (String) nodeData.get(AssessmentHelper.KEY_SCORE);
					return Double.valueOf(scoreString);
				}
			}
		}
		return null;
	}

	
	/**
	 * Find all efficiency statements for a specific user
	 * @param identity
	 * @return List of efficiency statements
	 */
	protected List<EfficiencyStatement> findEfficiencyStatements(Identity identity) {
		PropertyManager pm = PropertyManager.getInstance();
		List<Property> esProperties = pm.listProperties(identity, null, null, PROPERTY_CATEGORY, null);
		List<EfficiencyStatement> efficiencyStatements = new ArrayList<EfficiencyStatement>();
		Iterator<Property> iter = esProperties.iterator();
		while (iter.hasNext()) {
			Property efficiencyProperty = iter.next();
			EfficiencyStatement efficiencyStatement = (EfficiencyStatement) XStreamHelper.fromXML(efficiencyProperty.getTextValue());
			efficiencyStatements.add(efficiencyStatement);
		}
		return efficiencyStatements;
	}
	
	/**
	 * Find all identities who have an efficiency statement for this course repository entry
	 * @param courseRepoEntryKey
	 * @return List of identities
	 */
	protected List<Identity> findIdentitiesWithEfficiencyStatements(Long courseRepoEntryKey) {
		PropertyManager pm = PropertyManager.getInstance();
		return pm.findIdentitiesWithProperty(null, PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey), false);
	}
	
	/**
	 * Delete all efficiency statements from the given course for all users
	 * @param courseRepoEntryKey
	 * @return int number of deleted efficiency statements
	 */
	public void deleteEfficiencyStatementsFromCourse(Long courseRepoEntryKey) {
		PropertyManager pm = PropertyManager.getInstance();
		pm.deleteProperties(null, null, null, PROPERTY_CATEGORY, getPropertyName(courseRepoEntryKey));
	}

	/**
	 * Delete the given efficiency statement for this person
	 * @param identity
	 * @param efficiencyStatement
	 */
	protected void deleteEfficiencyStatement(Identity identity, EfficiencyStatement efficiencyStatement) {
		PropertyManager pm = PropertyManager.getInstance();
		String crourseRepoEntryKey = getPropertyName(efficiencyStatement.getCourseRepoEntryKey());
		pm.deleteProperties(identity, null, null, PROPERTY_CATEGORY, crourseRepoEntryKey);
	}
	
	
	/**
	 * Internal helper: convert the course repository entry key to a value that is used
	 * in the property name field
	 * @param courseRepoEntryKey
	 * @return String converted course id
	 */
	private String getPropertyName(Long courseRepoEntryKey) {
		return courseRepoEntryKey.toString();
	}

	/**
	 * Create or update all efficiency statment lists for the given list of identities and this course
	 * This is called from only one thread, since the course is locked at editing (either CourseEdit or CourseDetails edit).
	 * 
	 * @param course 
	 * @param identities List of identities
	 * @param checkForExistingRecord true: check if efficiency statement for this user exist;
	 * false: always create new one (be careful with this one!)
	 */	
	public void updateEfficiencyStatements(OLATResourceable ores, List<Identity> identities, final boolean checkForExistingProperty) {
		if (identities.size() > 0) {
			final ICourse course = CourseFactory.loadCourse(ores);
			log.audit("Updating efficiency statements for course::" + course.getResourceableId() + ", this might produce temporary heavy load on the CPU");
			Long courseResId = course.getCourseEnvironment().getCourseResourceableId(); 
			final RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(
					OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);

			// preload cache to speed up things
			AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
			long start = System.currentTimeMillis();
			am.preloadCache();
			long between = System.currentTimeMillis();

			Iterator<Identity> iter = identities.iterator();			
			while (iter.hasNext()) {
				final Identity identity = iter.next();					
				//o_clusterOK: by ld
				OLATResourceable efficiencyStatementResourceable = am.createOLATResourceableForLocking(identity);
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(efficiencyStatementResourceable, new SyncerExecutor() {
					public void execute() {					
						// create temporary user course env
						UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
						updateUserEfficiencyStatement(uce, re.getKey(), course, checkForExistingProperty);
						}
					});
				if (Thread.interrupted()) break;
			}
			//}
			long end = System.currentTimeMillis();
			if (log.isDebug()) 
				log.debug("Updated efficiency statements for course::" + course.getResourceableId() 
						+ ". Prepare cache: " + (between-start) + "ms; Updating statements: " + (end-between) + "ms; Users: " + identities.size());
		}
	}

	public void archiveUserData(Identity identity, File archiveDir) {
		List<EfficiencyStatement> efficiencyStatements = this.findEfficiencyStatements(identity);
		EfficiencyStatementArchiver.getInstance().archive(efficiencyStatements, identity, archiveDir);
	}
	
	/**
	 * Delete all efficiency-statements for certain identity.
	 * @param identity  Delete data for this identity.
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<EfficiencyStatement> efficiencyStatements = this.findEfficiencyStatements(identity);
		for (Iterator<EfficiencyStatement> iter = efficiencyStatements.iterator(); iter.hasNext();) {
			deleteEfficiencyStatement(identity, iter.next());
		}
		log.debug("All efficiency statements deleted for identity=" + identity);
	}
	
}
