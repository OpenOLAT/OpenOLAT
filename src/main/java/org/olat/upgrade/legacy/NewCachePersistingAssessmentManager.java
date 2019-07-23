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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.upgrade.legacy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentLoggingAction;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;


/**
 * Description:<BR>
 * The assessment manager is used by the assessable course nodes to store and
 * retrieve user assessment data from the database. The assessment Manager
 * should not be used directly from the controllers but only via the assessable
 * course nodes interface.<BR>
 * Exception are nodes that want to save or get node attempts variables for
 * nodes that are not assessable nodes (e.g. questionnaire) <BR>
 * This implementation will store its values using the property manager and has
 * a cache built in for frequently used assessment data like score, passed and
 * attempts variables.
 * <P>
 * 
 * the underlying cache is segmented as follows:
 * 1.) by this class (=owner in singlevm, coowner in cluster mode)
 * 2.) by course (so that e.g. deletion of a course removes all caches)
 * 3.) by identity, for preloading and invalidating (e.g. a user entering a course will cause the identity's cache to be loaded)
 * 
 * each cache only has -one- key, which is a hashmap with all the information (score,passed, etc) for the given user/course.
 * the reason for this is that it must be possible to see a difference between a null value (key expired) and a value which corresponds to
 * e.g. "this user has never attempted this test in this course". since only the concrete set, but not the possible set is known. (at least
 * not in the database). so all keys of a given user/course will therefore expire together which also makes sense from a use point of view.
 * 
 * Cache usage with e.g. the wiki: wikipages should be saved as separate keys, since no batch updates are needed for perf. reasons.
 * 
 * reason for 3: preloading all data of all users of a course lasts up to 5 seconds and will waste memory.
 * a user in a course only needs its own data. only when a tutor enters the assessment functionality, all data of all users is needed ->
 * do a full load only then.
 * 
 * TODO: e.g. IQTEST.onDelete(..) cleans all data without going over the assessmentmanager here. meaning that the cache has stale data in it.
 * since coursenode.getIdent (partial key of this cache) is forever unique, then this doesn't really matter. - but it is rather unintended...
 * point is that a node can save lots of data that have nothing to do with assessments
 * 
 * 
 * @author Felix Jost
 */
public class NewCachePersistingAssessmentManager {
	
	private static final Logger log = Tracing.createLoggerFor(NewCachePersistingAssessmentManager.class);
	
	public static final String SCORE = "SCORE";
	public static final String PASSED = "PASSED";
	public static final String ATTEMPTS = "ATTEMPTS";
	public static final String COMMENT = "COMMENT";
	public static final String COACH_COMMENT = "COACH_COMMENT";
	public static final String ASSESSMENT_ID = "ASSESSMENT_ID";

	public final static String FULLY_ASSESSED = "FULLY_ASSESSED";

	/**
	 * the key under which a hashmap is stored in a cachewrapper. we only use one key so that either all values of a user are there or none are there.
	 * (otherwise we cannot know whether a null value means expiration of cache or no-such-property-yet-for-user)
	 */
	//private static final String FULLUSERSET = "FULLUSERSET";
	private static final String LAST_MODIFIED = "LAST_MODIFIED";
	
	
	// Float and Integer are immutable objects, we can reuse them.
	private static final Float FLOAT_ZERO = new Float(0);
	private static final Integer INTEGER_ZERO = new Integer(0);

	// the cache per assessment manager instance (=per course)
	private CacheWrapper<NewCacheKey,HashMap<String, Serializable>> courseCache;
	private OLATResourceable ores;

	/**
	 * Private since singleton
	 */
	public NewCachePersistingAssessmentManager(ICourse course) {
		this.ores = course;
		//String cacheName = "Course@" + course.getResourceableId();
		courseCache = CoordinatorManager.getInstance().getCoordinator().getCacher()
				.getCache(AssessmentManager.class.getSimpleName(), "newpersisting");
	}
	
	public NewCachePersistingAssessmentManager(ICourse course, CacheWrapper<NewCacheKey,HashMap<String, Serializable>> cache) {
		this.ores = course;
		//String cacheName = "Course@" + course.getResourceableId();
		courseCache = cache;
	}
	/**
	 * @param identity the identity for which to properties are to be loaded. 
	 * if null, the properties of all identities (=all properties of this course)
	 * are loaded.
	 * @return
	 */
	private List<Property> loadPropertiesFor(List<Identity> identities) {
		if(identities == null || identities.isEmpty()) return Collections.emptyList();
		
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
		if (identities != null) {
			sb.append(" and p.identity.key in (:id)");
		}
		TypedQuery<Property> query = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("restypename", ores.getResourceableTypeName())
				.setParameter("restypeid", ores.getResourceableId());
		if (identities != null) {
			query.setParameter("id", PersistenceHelper.toKeys(identities));
		}
		return query.getResultList();	
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#preloadCache(org.olat.core.id.Identity)
	 */
	public void preloadCache(Identity identity) {
		// triggers loading of data of the given user.
		getOrLoadScorePassedAttemptsMap(identity, null, false);
	}

	public void preloadCache(List<Identity> identities) {
		int count = 0;
		int batch = 200;

		Map<Identity, List<Property>> map = new HashMap<>(201); 
		do {
			int toIndex = Math.min(count + batch, identities.size());
			List<Identity> toLoad = identities.subList(count, toIndex);
			List<Property> allProperties = loadPropertiesFor(toLoad);
			
			map.clear();
			for(Property prop:allProperties) {
				if(!map.containsKey(prop.getIdentity())) {
					map.put(prop.getIdentity(), new ArrayList<Property>());
				}
				map.get(prop.getIdentity()).add(prop);
			}
			
			for(Identity id:toLoad) {
				List<Property> props = map.get(id);
				if(props == null) {
					props = new ArrayList<>(1);
				}
				getOrLoadScorePassedAttemptsMap(id, props, false);
			}
			count += batch;
		} while(count < identities.size());
	}
	
	/**
	 * retrieves the Map which contains all data for this course and the given user. 
	 * if the cache evicted the map in the meantime, then it is recreated
	 * by querying the database and fetching all that data in one query, and then reput into the cache.
	 * <br>
	 * this method is threadsafe.
	 * 
	 * @param identity the identity 
	 * @param notify if true, then the
	 * @return a Map containing nodeident+"_"+ e.g. PASSED as key, Boolean (for PASSED), Float (for SCORE), or Integer (for ATTEMPTS) as values
	 */
	private Map<String, Serializable> getOrLoadScorePassedAttemptsMap(Identity identity, List<Property> properties, boolean prepareForNewData) {

		// a user is only active on one node at the same time.
		NewCacheKey cacheKey = new NewCacheKey(ores.getResourceableId(), identity.getKey());
		HashMap<String, Serializable> m = courseCache.get(cacheKey);
		if (m == null) {
			// cache entry (=all data of the given identity in this course) has expired or has never been stored yet into the cache.
			// or has been invalidated (in cluster mode when puts occurred from an other node for the same cache)
			m = new HashMap<>();
			// load data
			List<Property> loadedProperties = properties == null ? loadPropertiesFor(Collections.singletonList(identity)) : properties;
			for (Property property:loadedProperties) {
				addPropertyToCache(m, property);
			}
			
			//If property not found, prefill with default value.
			if(!m.containsKey(ATTEMPTS)) {
				m.put(ATTEMPTS, INTEGER_ZERO);
			}
			if(!m.containsKey(SCORE)) {
				m.put(SCORE, FLOAT_ZERO);
			}
			if(!m.containsKey(LAST_MODIFIED)) {
				m.put(LAST_MODIFIED, null);
			}
			
			// we use a putSilent here (no invalidation notifications to other cluster nodes), since
			// we did not generate new data, but simply asked to reload it. 
			if (prepareForNewData) {
				courseCache.update(cacheKey, m);
			} else {
				courseCache.put(cacheKey, m);
			}
		} else {
			// still in cache. 
			if (prepareForNewData) { // but we need to notify that data has changed: we reput the data into the cache - a little hacky yes
				courseCache.update(cacheKey, m);
			}
		}
		return m;
	}
	
	// package local for perf. reasons, threadsafe.
	/**
	 * puts a property into the cache. 
	 * since it only puts data into a map which in turn is put under the FULLUSERSET key into the cache, we need to 
	 * explicitly reput that key from the cache first, so that the cache notices that that data has changed 
	 * (and can propagate to other nodes if applicable) 
	 * 
	 */
	void putPropertyIntoCache(Identity identity, Property property) { 
		// load the data, and indicate it to reput into the cache so that the cache knows it is something new.
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, true);
		addPropertyToCache(m, property);		
	}
	
	/**
	 * Removes a property from cache.
	 * @param identity
	 * @param property
	 */
	void removePropertyFromCache(Identity identity, Property property) { 
		// load the data, and indicate it to reput into the cache so that the cache knows it is something new.
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, true);
		this.removePropertyFromCache(m, property);
	}

	/**
	 * thread safe.
	 * @param property
	 * @throws AssertionError
	 */
	private void addPropertyToCache(Map<String, Serializable> acache, Property property) throws AssertionError {
		String propertyName = property.getName();
		Serializable value;
		if (propertyName.equals(ATTEMPTS)) {
			value = new Integer(property.getLongValue().intValue());
		} else if (propertyName.equals(SCORE)) {
			value = property.getFloatValue();
		} else if (propertyName.equals(PASSED) || FULLY_ASSESSED.equals(propertyName)) {
			value = new Boolean(property.getStringValue());
		} else if (propertyName.equals(ASSESSMENT_ID)) {
			value = property.getLongValue();			
		} else if (propertyName.equals(COMMENT) || propertyName.equals(COACH_COMMENT)) {
			value = property.getTextValue();			
		} else {
			throw new AssertionError("property in list that is not of type attempts, score, passed or ASSESSMENT_ID, COMMENT and COACH_COMMENT :: " + propertyName);
		}
		
		Date lastModified = property.getLastModified();
		// put in cache, maybe overriding old values		
		String cacheKey = getPropertyCacheKey(property);		
		synchronized(acache) {//cluster_ok acache is an element from the cacher
			acache.put(cacheKey, value);
			
			String lmCacheKey = getLastModifiedCacheKey(property);
			Long currentLastModifiedDate = (Long)acache.get(lmCacheKey);
			if(currentLastModifiedDate == null || currentLastModifiedDate.longValue() < lastModified.getTime()) {
				acache.put(lmCacheKey, new Long(lastModified.getTime()));
			}
		}
	}
	
	/**
	 * Removes property from cache
	 * @param acache
	 * @param property
	 * @throws AssertionError
	 */
	private void removePropertyFromCache(Map<String, Serializable> acache, Property property) throws AssertionError {
		String propertyName = property.getName();		
		if (!(propertyName.equals(ATTEMPTS) || propertyName.equals(SCORE) || propertyName.equals(PASSED))) {
			throw new AssertionError("property in list that is not of type attempts, score or passed ::" + propertyName);
		}
				
		String cacheKey = getPropertyCacheKey(property);		
		synchronized(acache) {//cluster_ok acache is an elment from the cacher
			acache.remove(cacheKey);			
		}
	}
	
	/**
	 * 
	 * @param courseNode
	 * @param identity
	 * @param assessedIdentity
	 * @param score
	 * @param coursePropManager
	 */
	void saveNodeScore(CourseNode courseNode, Identity assessedIdentity, Float score, CoursePropertyManager coursePropManager) {
    // olat:::: introduce a createOrUpdate method in the cpm and also if applicable in the general propertymanager
		if (score != null) {
			Property scoreProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, SCORE);
			if (scoreProperty == null) {
				scoreProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, SCORE, score, null, null, null);
				coursePropManager.saveProperty(scoreProperty);
			} else {
				scoreProperty.setFloatValue(score);
				coursePropManager.updateProperty(scoreProperty);
			}
			// add to cache
			putPropertyIntoCache(assessedIdentity, scoreProperty);
		}
	}

	/**
	 * 
	 * @param courseNode
	 * @param identity
	 * @param assessedIdentity
	 * @param mark
	 * @param coursePropManager
	 */
	void saveNodeFullyAssessed(CourseNode courseNode, Identity identity, Identity assessedIdentity, Boolean fullyAssessed,
			CoursePropertyManager coursePropManager) {
		// olat:::: introduce a createOrUpdate method in the cpm and also if applicable in the general propertymanager
		if (fullyAssessed != null) {
			Property markProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, FULLY_ASSESSED);
			if (markProperty == null) {
				markProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, FULLY_ASSESSED, null, null,
						String.valueOf(fullyAssessed), null);
				coursePropManager.saveProperty(markProperty);
			} else {
				markProperty.setStringValue(String.valueOf(fullyAssessed));
				coursePropManager.updateProperty(markProperty);
			}
			// add to cache
			putPropertyIntoCache(assessedIdentity, markProperty);
		}
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#saveNodeAttempts(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.core.id.Identity,
	 *      java.lang.Integer)
	 */
	public void saveNodeAttempts(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Integer attempts) {
		//   A note on updating the EfficiencyStatement:
		// In the equivalent method incrementNodeAttempts() in this class, the following code is executed:
		//   // Update users efficiency statement
	  //   EfficiencyStatementManager esm =	EfficiencyStatementManager.getInstance();
	  //   esm.updateUserEfficiencyStatement(userCourseEnv);
		// One would expect that saveNodeAttempts would also have to update the EfficiencyStatement - or
		// the caller of this method would have to make sure that this happens in the same transaction.
		// While this is not explicitly so, implicitly it is: currently the only user this method is 
		// the AssessmentEditController - which as the 2nd last method calls into saveScoreEvaluation
		// - which in turn does update the EfficiencyStatement - at which point we're happy and everything works fine.
		// But it seems like this mechanism is a bit unobvious and might well be worth some refactoring...
		ICourse course = CourseFactory.loadCourse(ores);
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor(){
			@Override
			public void execute() {
				Property attemptsProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, ATTEMPTS);
				if (attemptsProperty == null) {
					attemptsProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, ATTEMPTS, 
							null, new Long(attempts.intValue()), null, null);
					cpm.saveProperty(attemptsProperty);
				} else {
					attemptsProperty.setLongValue(new Long(attempts.intValue()));
					cpm.updateProperty(attemptsProperty);
				}
				// add to cache
				putPropertyIntoCache(assessedIdentity, attemptsProperty);
			}
		});

		// node log
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, ATTEMPTS + " set to: " + String.valueOf(attempts), null);

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED, assessedIdentity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));	
		}

	
	/**
	 * 
	 * @param courseNode
	 * @param identity
	 * @param assessedIdentity
	 * @param passed
	 * @param coursePropManager
	 */
	void saveNodePassed(CourseNode courseNode, Identity assessedIdentity, Boolean passed, CoursePropertyManager coursePropManager) {		
		  Property passedProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, PASSED);
		  if (passedProperty == null && passed!=null) {					
			  String pass = passed.toString();
			  passedProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, PASSED, null, null, pass, null);
			  coursePropManager.saveProperty(passedProperty);					 
		  } else if (passedProperty!=null){
			  if (passed!=null) {
			  passedProperty.setStringValue(passed.toString());
			  coursePropManager.updateProperty(passedProperty);
			  } else {
			  	removePropertyFromCache(assessedIdentity,passedProperty);
				  coursePropManager.deleteProperty(passedProperty);
			  }
		  }
		  
		  //add to cache
		  if(passed!=null && passedProperty!=null) {
			  putPropertyIntoCache(assessedIdentity, passedProperty);
		  }		
	}
	
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#saveNodeComment(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity, org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public void saveNodeComment(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final String comment) {
		ICourse course = CourseFactory.loadCourse(ores);
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor(){
			@Override
			public void execute() {
				Property commentProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, COMMENT);
				if (commentProperty == null) {
					commentProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, COMMENT, null, null, null, comment);
					cpm.saveProperty(commentProperty);
				} else {
					commentProperty.setTextValue(comment);
					cpm.updateProperty(commentProperty);
				}
			  // add to cache
				putPropertyIntoCache(assessedIdentity, commentProperty);
			}
		});
		// node log
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, COMMENT + " set to: " + comment, null);

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_USER_COMMENT_CHANGED, assessedIdentity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_USERCOMMENT_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiUserComment, "", StringHelper.stripLineBreaks(comment)));	
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#saveNodeCoachComment(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity, java.lang.String)
	 */
	public void saveNodeCoachComment(final CourseNode courseNode, final Identity assessedIdentity, final String comment) {
		ICourse course = CourseFactory.loadCourse(ores);
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerExecutor(){
			@Override
			public void execute() {
				Property commentProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, COACH_COMMENT);
				if (commentProperty == null) {
					commentProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, COACH_COMMENT, null, null, null, comment);
					cpm.saveProperty(commentProperty);
				} else {
					commentProperty.setTextValue(comment);
					cpm.updateProperty(commentProperty);
				}
			  // add to cache
				putPropertyIntoCache(assessedIdentity, commentProperty);
			}
		});
		// olat::: no node log here? (because what we did above is a node log with custom text AND by a coach)?

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_COACH_COMMENT_CHANGED, assessedIdentity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_COACHCOMMENT_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiCoachComment, "", StringHelper.stripLineBreaks(comment)));	
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#incrementNodeAttempts(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public void incrementNodeAttempts(CourseNode courseNode, Identity identity, UserCourseEnvironment userCourseEnv) {
		incrementNodeAttempts(courseNode, identity, userCourseEnv, true);
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#incrementNodeAttemptsInBackground(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity, org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public void incrementNodeAttemptsInBackground(CourseNode courseNode, Identity identity, UserCourseEnvironment userCourseEnv) {
		incrementNodeAttempts(courseNode, identity, userCourseEnv, false);
	}

	private void incrementNodeAttempts(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnv, boolean logActivity) {
		ICourse course = CourseFactory.loadCourse(ores);
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		long attempts = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(identity), new SyncerCallback<Long>(){
			@Override
			public Long execute() {
				long attempts = incrementNodeAttemptsProperty(courseNode, identity, cpm);
				if(courseNode instanceof AssessableCourseNode) {
          // Update users efficiency statement
				  EfficiencyStatementManager esm =	CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
				  esm.updateUserEfficiencyStatement(userCourseEnv);
				}
				return attempts;
			}
		});

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED, identity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		if(logActivity) {
			// user activity logging
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(identity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));
		}
	}
	
	/**
	 * Private method. Increments the attempts property.
	 * @param courseNode
	 * @param identity
	 * @param cpm
	 * @return the resulting new number of node attempts
	 */
	private long incrementNodeAttemptsProperty(CourseNode courseNode, Identity identity, CoursePropertyManager cpm) {		
		Long attempts;
		Property attemptsProperty = cpm.findCourseNodeProperty(courseNode, identity, null, ATTEMPTS);
		if (attemptsProperty == null) {
			attempts = new Long(1);
			attemptsProperty = cpm.createCourseNodePropertyInstance(courseNode, identity, null, ATTEMPTS, null, attempts, null, null);
			cpm.saveProperty(attemptsProperty);
		} else {
			attempts = new Long(attemptsProperty.getLongValue().longValue() + 1);
			attemptsProperty.setLongValue(attempts);
			cpm.updateProperty(attemptsProperty);
		}
		// add to cache
		putPropertyIntoCache(identity, attemptsProperty);
		
		return attempts;
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#getNodeScore(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public Float getNodeScore(CourseNode courseNode, Identity identity) {
		// Check if courseNode exist
		if (courseNode == null) {
			return FLOAT_ZERO; // return default value
		}
		
		String cacheKey = getCacheKey(courseNode, SCORE);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			Float result = (Float) m.get(cacheKey);
			return result;
		}	
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#getNodePassed(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public Boolean getNodePassed(CourseNode courseNode, Identity identity) {
		// Check if courseNode exist
		if (courseNode == null) {
			return Boolean.FALSE; // return default value
		}
		
		String cacheKey = getCacheKey(courseNode, PASSED);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			Boolean result = (Boolean) m.get(cacheKey);
			return result;
		}		
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#getNodeAttempts(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public Integer getNodeAttempts(CourseNode courseNode, Identity identity) {
		// Check if courseNode exist
		if (courseNode == null) {
			return INTEGER_ZERO; // return default value
		}
		
		String cacheKey = getCacheKey(courseNode, ATTEMPTS);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			Integer result = (Integer) m.get(cacheKey);
			// see javadoc of org.olat.course.assessment.AssessmentManager#getNodeAttempts
			return result == null? INTEGER_ZERO : result;
		}				
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#getNodeComment(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public String getNodeComment(CourseNode courseNode, Identity identity) {		
		if (courseNode == null) {
			return null; // return default value
		}
				
		String cacheKey = getCacheKey(courseNode, COMMENT);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			String result = (String) m.get(cacheKey);			
			return result;
		}		
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#getNodeCoachComment(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public String getNodeCoachComment(CourseNode courseNode, Identity identity) {				
		if (courseNode == null) {
			return null; // return default value
		}
				
		String cacheKey = getCacheKey(courseNode, COACH_COMMENT);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			String result = (String) m.get(cacheKey);			
			return result;
		}		
	}

	public Boolean getNodeFullyAssessed(CourseNode courseNode, Identity identity) {
		Boolean fullyAssessed = null;
		if (courseNode != null) {
			String cacheKey = getCacheKey(courseNode, FULLY_ASSESSED);
			Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);
			synchronized (m) {//o_clusterOK by:fj is per vm only
				fullyAssessed = (Boolean) m.get(cacheKey);
			}
		}

		return fullyAssessed;
	}

	/**
	 * Internal method to create a cache key for a given node, and property
	 * @param identity
	 * @param nodeIdent
	 * @param propertyName
	 * @return String the key
	 */
	private String getCacheKey(CourseNode courseNode, String propertyName) {
		String nodeIdent = courseNode.getIdent();
		return getCacheKey(nodeIdent, propertyName);
	}

	/**
	 * threadsafe.
	 * @param nodeIdent
	 * @param propertyName
	 * @return
	 */
	private String getCacheKey(String nodeIdent, String propertyName) {
		StringBuilder key = new StringBuilder(nodeIdent.length()+propertyName.length()+1);
		key.append(nodeIdent).append('_').append(propertyName);
		return key.toString();
	}
	
	/**
	 * Finds the cacheKey for the input property.
	 * @param property
	 * @return Returns the cacheKey
	 */
	private String getPropertyCacheKey(Property property) {
    //- node id is coded into property category like this: NID:ms::12345667
		// olat::: move the extract method below to the CoursePropertyManager - since the generation/concat method is also there.
		String propertyName = property.getName();
		String propertyCategory = property.getCategory();
		String nodeIdent = propertyCategory.substring(propertyCategory.indexOf("::") + 2);
		String cacheKey = getCacheKey(nodeIdent, propertyName);
    //cacheKey is now e.g. 12345667_PASSED
		return cacheKey;
	}
	
	private String getLastModifiedCacheKey(Property property) {;
		String propertyCategory = property.getCategory();
		String nodeIdent = propertyCategory.substring(propertyCategory.indexOf("::") + 2);
		String cacheKey = getCacheKey(nodeIdent, LAST_MODIFIED);
		return cacheKey;
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentManager#registerForAssessmentChangeEvents(org.olat.core.util.event.GenericEventListener,
	 *      org.olat.core.id.Identity)
	 */
	public void registerForAssessmentChangeEvents(GenericEventListener gel, Identity identity) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, identity, ores);
	}

	/**
	 * @see org.olat.course.assessment.AssessmentManager#deregisterFromAssessmentChangeEvents(org.olat.core.util.event.GenericEventListener)
	 */
	public void deregisterFromAssessmentChangeEvents(GenericEventListener gel) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, ores);
	}
	  
  /**
   * 
   * @param courseNode
   * @param assessedIdentity
   * @param assessmentID
   * @param coursePropManager
   */
  void saveAssessmentID(CourseNode courseNode, Identity assessedIdentity, Long assessmentID, CoursePropertyManager coursePropManager) { 
  	if(assessmentID!=null) {
  	  Property assessmentIDProperty = coursePropManager.findCourseNodeProperty(courseNode, assessedIdentity, null, ASSESSMENT_ID);
		  if (assessmentIDProperty == null) {					
			  assessmentIDProperty = coursePropManager.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, ASSESSMENT_ID, null, assessmentID, null, null);
			  coursePropManager.saveProperty(assessmentIDProperty);
		  } else {
			  assessmentIDProperty.setLongValue(assessmentID);
			  coursePropManager.updateProperty(assessmentIDProperty);
		  }	
		  // add to cache
			putPropertyIntoCache(assessedIdentity, assessmentIDProperty);
  	}
  }
	
	/**
	 * No caching for the assessmentID.
	 * @see org.olat.course.assessment.AssessmentManager#getAssessmentID(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity)
	 */
	public Long getAssessmentID(CourseNode courseNode, Identity identity) {
		if (courseNode == null) {
			return Long.valueOf(0); // return default value
		}
				
		String cacheKey = getCacheKey(courseNode, ASSESSMENT_ID);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			Long result = (Long) m.get(cacheKey);			
			return result;
		}				
	}
	
	public Date getScoreLastModifiedDate(CourseNode courseNode, Identity identity) {
		if (courseNode == null) {
			return null; // return default value
		}
		
		String cacheKey = getCacheKey(courseNode, LAST_MODIFIED);
		Map<String, Serializable> m = getOrLoadScorePassedAttemptsMap(identity, null, false);		
		synchronized(m) {//o_clusterOK by:fj is per vm only
			Long lastModified = (Long) m.get(cacheKey);
			if(lastModified != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(lastModified.longValue());
				return cal.getTime();
			}
		}
		return null;
	}

	/**
	 * 
	 * @see org.olat.course.assessment.AssessmentManager#saveScoreEvaluation(org.olat.course.nodes.CourseNode, org.olat.core.id.Identity, org.olat.core.id.Identity, org.olat.course.run.scoring.ScoreEvaluation)
	 */
	public void saveScoreEvaluation(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final ScoreEvaluation scoreEvaluation, 
			final UserCourseEnvironment userCourseEnv, final boolean incrementUserAttempts) {
		final ICourse course = CourseFactory.loadCourse(ores);
		final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		final RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		// o_clusterREVIEW we could sync on a element finer than course, e.g. the composite course+assessIdentity.
		// +: concurrency would be higher
		// -: many entries (num of courses * visitors of given course) in the locktable.
		// we could also sync on the assessedIdentity.
		
		Long attempts = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(assessedIdentity), new SyncerCallback<Long>(){
			@Override
			public Long execute() {
				Long attempts = null;
				Float score = scoreEvaluation.getScore();
				Boolean passed = scoreEvaluation.getPassed();
				
				saveNodeScore(courseNode, assessedIdentity, score, cpm);
				saveNodePassed(courseNode, assessedIdentity, passed, cpm);
				saveAssessmentID(courseNode, assessedIdentity, scoreEvaluation.getAssessmentID(), cpm);				
				if(incrementUserAttempts) {
					attempts = incrementNodeAttemptsProperty(courseNode, assessedIdentity, cpm);
				}
				if(courseNode instanceof AssessableCourseNode) {
				  userCourseEnv.getScoreAccounting().evaluateAll();
				  // Update users efficiency statement
				  EfficiencyStatementManager esm =	CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
				  esm.updateUserEfficiencyStatement(userCourseEnv);
				}
				
				if(passed != null && passed.booleanValue() && course.getCourseConfig().isAutomaticCertificationEnabled()) {
					CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
					if(certificatesManager.isCertificationAllowed(assessedIdentity, courseEntry)) {
						CertificateTemplate template = null;
						Long templateId = course.getCourseConfig().getCertificateTemplate();
						if(templateId != null) {
							template = certificatesManager.getTemplateById(templateId);
						}
						CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, score, passed);
						CertificateConfig config = CertificateConfig.builder()
								.withCustom1(course.getCourseConfig().getCertificateCustom1())
								.withCustom2(course.getCourseConfig().getCertificateCustom2())
								.withCustom3(course.getCourseConfig().getCertificateCustom3())
								.withSendEmailBcc(false)
								.withSendEmailLinemanager(false)
								.withSendEmailIdentityRelations(false)
								.build();
						certificatesManager.generateCertificate(certificateInfos, courseEntry, template, config);
					}
				}
				
				return attempts;
			}});
		
		
		// node log
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, SCORE + " set to: " + String.valueOf(scoreEvaluation.getScore()), null);
		if(scoreEvaluation.getPassed()!=null) {
		  am.appendToUserNodeLog(courseNode, identity, assessedIdentity, PASSED + " set to: " + scoreEvaluation.getPassed().toString(), null);
		} else {
			 am.appendToUserNodeLog(courseNode, identity, assessedIdentity, PASSED + " set to \"undefined\"", null);
		}
		if(scoreEvaluation.getAssessmentID()!=null) {
			am.appendToUserNodeLog(courseNode, assessedIdentity, assessedIdentity, ASSESSMENT_ID + " set to: " + scoreEvaluation.getAssessmentID().toString(), null);
		}		

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED, assessedIdentity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		if (scoreEvaluation.getScore()!=null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_SCORE_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiScore, "", String.valueOf(scoreEvaluation.getScore())));
		}

		if (scoreEvaluation.getPassed()!=null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", String.valueOf(scoreEvaluation.getPassed())));
		} else {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", "undefined"));
		}

		if (incrementUserAttempts && attempts!=null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(identity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));	
		}
	}

	public Long syncAndsaveScoreEvaluation(CourseNode courseNode, Identity identity, Identity assessedIdentity,
			ScoreEvaluation scoreEvaluation, boolean incrementUserAttempts, UserCourseEnvironment userCourseEnv,
			CoursePropertyManager cpm) {
		return saveScoreEvaluationInSync(courseNode, identity, assessedIdentity, scoreEvaluation, incrementUserAttempts, userCourseEnv, cpm);
	}

	private Long saveScoreEvaluationInSync(CourseNode courseNode, Identity identity, Identity assessedIdentity, ScoreEvaluation scoreEvaluation,
			boolean incrementUserAttempts, final UserCourseEnvironment userCourseEnv, final CoursePropertyManager cpm) {
		Long attempts = null;
		saveNodeScore(courseNode, assessedIdentity, scoreEvaluation.getScore(), cpm);
		log.debug("successfully saved node score : " + scoreEvaluation.getScore());
		saveNodePassed(courseNode, assessedIdentity, scoreEvaluation.getPassed(), cpm);
		log.debug("successfully saved node passed : " + scoreEvaluation.getPassed());
		saveAssessmentID(courseNode, assessedIdentity, scoreEvaluation.getAssessmentID(), cpm);
		log.debug("successfully saved node asssessmentId : " + scoreEvaluation.getPassed());
		saveNodeFullyAssessed(courseNode, identity, assessedIdentity, scoreEvaluation.getFullyAssessed(), cpm);
		log.debug("successfully saved node marked completely : " + scoreEvaluation.getPassed());
		if (incrementUserAttempts) {
			attempts = incrementNodeAttemptsProperty(courseNode, assessedIdentity, cpm);
			log.debug("successfully saved user attemps : " + attempts);
		}
		saveNodeFullyAssessed(courseNode, identity, assessedIdentity, scoreEvaluation.getFullyAssessed(), cpm);
		log.debug("successfully saved node fullyAssessed : " + scoreEvaluation.getFullyAssessed());
		DBFactory.getInstance().commitAndCloseSession();
		if (courseNode instanceof AssessableCourseNode) {
			userCourseEnv.getScoreAccounting().evaluateAll();
			EfficiencyStatementManager esm = CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
			esm.updateUserEfficiencyStatement(userCourseEnv);
		}
		return attempts;
	}

	/**
	 * Always use this to get a OLATResourceable for doInSync locking!
	 * Uses the assessIdentity.
	 * 
	 * @param course
	 * @param assessedIdentity
	 * @param courseNode
	 * @return
	 */
	public OLATResourceable createOLATResourceableForLocking(Identity assessedIdentity) {				
		String type = "AssessmentManager::Identity";
		OLATResourceable oLATResourceable = OresHelper.createOLATResourceableInstance(type,assessedIdentity.getKey());
		return oLATResourceable;
	}
	
}
