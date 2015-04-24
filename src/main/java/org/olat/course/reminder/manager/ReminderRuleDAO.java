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
package org.olat.course.reminder.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderRuleDAO {

	@Autowired
	private DB dbInstance;

	public Map<Long,Float> getScores(Long courseResourceId, CourseNode node, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<Long,Float>();
		}

		List<Long> identityKeys = PersistenceHelper.toKeys(identities);

		StringBuilder sb = new StringBuilder();
		sb.append("select p.identity.key, p.floatValue from org.olat.properties.Property as p")
		  .append(" where p.resourceTypeId = :resId and p.resourceTypeName='CourseModule'")
		  .append(" and p.name='").append(AssessmentManager.SCORE).append("'")
		  .append(" and p.category=:category");

		Set<Long> identityKeySet = null;
		if(identityKeys.size() < 100) {
			sb.append(" and p.identity.key in (:identityKeys)");
		} else {
			identityKeySet = new HashSet<Long>(identityKeys);
		}
		String myCategory = buildCourseNodePropertyCategory(node);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseResourceId)
				.setParameter("category", myCategory);
		if(identityKeys.size() < 100) {
			query.setParameter("identityKeys", identityKeys);
		}

		List<Object[]> infoList = query.getResultList();
		Map<Long,Float> dateMap = new HashMap<>();
		for(Object[] infos:infoList) {
			Long identityKey = (Long)infos[0];
			if(identityKeySet == null || identityKeySet.contains(identityKey)) {
				Float score = (Float)infos[1];
				dateMap.put(identityKey, score);
			}
		}
		return dateMap;
	}
	
	public Map<Long,Integer> getAttempts(Long courseResourceId, CourseNode node, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<Long,Integer>();
		}

		List<Long> identityKeys = PersistenceHelper.toKeys(identities);

		StringBuilder sb = new StringBuilder();
		sb.append("select p.identity.key, p.longValue from org.olat.properties.Property as p")
		  .append(" where p.resourceTypeId = :resId and p.resourceTypeName='CourseModule'")
		  .append(" and p.name='").append(AssessmentManager.ATTEMPTS).append("'")
		  .append(" and p.category=:category");

		Set<Long> identityKeySet = null;
		if(identityKeys.size() < 100) {
			sb.append(" and p.identity.key in (:identityKeys)");
		} else {
			identityKeySet = new HashSet<Long>(identityKeys);
		}
		String myCategory = buildCourseNodePropertyCategory(node);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseResourceId)
				.setParameter("category", myCategory);
		if(identityKeys.size() < 100) {
			query.setParameter("identityKeys", identityKeys);
		}

		List<Object[]> infoList = query.getResultList();
		Map<Long,Integer> dateMap = new HashMap<>();
		for(Object[] infos:infoList) {
			Long identityKey = (Long)infos[0];
			if(identityKeySet == null || identityKeySet.contains(identityKey)) {
				Long attempts = (Long)infos[1];
				dateMap.put(identityKey, new Integer(attempts.intValue()));
			}
		}
		return dateMap;
	}
	
	public Map<Long,Date> getInitialAttemptDates(Long courseResourceId, CourseNode node, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<Long,Date>();
		}

		List<Long> identityKeys = PersistenceHelper.toKeys(identities);

		StringBuilder sb = new StringBuilder();
		sb.append("select p.identity.key, p.creationDate from org.olat.properties.Property as p")
		  .append(" where p.resourceTypeId = :resId and p.resourceTypeName='CourseModule'")
		  .append(" and p.name='").append(AssessmentManager.ATTEMPTS).append("'")
		  .append(" and p.category=:category");

		Set<Long> identityKeySet = null;
		if(identityKeys.size() < 100) {
			sb.append(" and p.identity.key in (:identityKeys)");
		} else {
			identityKeySet = new HashSet<Long>(identityKeys);
		}
		String myCategory = buildCourseNodePropertyCategory(node);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseResourceId)
				.setParameter("category", myCategory);
		if(identityKeys.size() < 100) {
			query.setParameter("identityKeys", identityKeys);
		}

		List<Object[]> infoList = query.getResultList();
		Map<Long,Date> dateMap = new HashMap<>();
		for(Object[] infos:infoList) {
			Long identityKey = (Long)infos[0];
			if(identityKeySet == null || identityKeySet.contains(identityKey)) {
				Date attempts = (Date)infos[1];
				dateMap.put(identityKey, attempts);
			}
		}
		return dateMap;
	}
	
	public Map<Long,Boolean> getPassed(Long courseResourceId, CourseNode node, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<Long,Boolean>();
		}

		List<Long> identityKeys = PersistenceHelper.toKeys(identities);

		StringBuilder sb = new StringBuilder();
		sb.append("select p.identity.key, p.stringValue from org.olat.properties.Property as p")
		  .append(" where p.resourceTypeId = :resId and p.resourceTypeName='CourseModule'")
		  .append(" and p.name='").append(AssessmentManager.PASSED).append("'")
		  .append(" and p.category=:category");

		Set<Long> identityKeySet = null;
		if(identityKeys.size() < 100) {
			sb.append(" and p.identity.key in (:identityKeys)");
		} else {
			identityKeySet = new HashSet<Long>(identityKeys);
		}
		String myCategory = buildCourseNodePropertyCategory(node);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseResourceId)
				.setParameter("category", myCategory);
		if(identityKeys.size() < 100) {
			query.setParameter("identityKeys", identityKeys);
		}

		List<Object[]> infoList = query.getResultList();
		Map<Long,Boolean> dateMap = new HashMap<>();
		for(Object[] infos:infoList) {
			Long identityKey = (Long)infos[0];
			if(identityKeySet == null || identityKeySet.contains(identityKey)) {
				String passed = (String)infos[1];
				dateMap.put(identityKey, new Boolean(passed));
			}
		}
		return dateMap;
	}
	
	private String buildCourseNodePropertyCategory(CourseNode node) {
		String type = (node.getType().length() > 4 ? node.getType().substring(0, 4) : node.getType());
		return ("NID:" + type + "::" + node.getIdent());
	}

}
