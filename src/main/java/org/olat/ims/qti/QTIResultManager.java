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

package org.olat.ims.qti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: Useful functions for download
 * 
 * @author Alexander Schneider
 */
@Service("qtiResultManager")
public class QTIResultManager implements UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(QTIResultManager.class);

	private static QTIResultManager instance;
	
	@Autowired
	private DB dbInstance;

	/**
	 * Constructor for QTIResultManager.
	 */
	private QTIResultManager() {
		instance = this;
	}

	/**
	 * @return QTIResultManager
	 */
	public static QTIResultManager getInstance() {
		return instance;
	}
	
	/**
	 * [user by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	/**
	 * @param olatResource
	 * @param olatResourceDetail
	 * @param repositoryRef
	 * @return True if true, false otherwise.
	 */
	public boolean hasResultSets(Long olatResource, String olatResourceDetail, Long repositoryRef) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(rset.key) from ").append(QTIResultSet.class.getName()).append(" as rset ")
		  .append("where rset.olatResource=:resId and rset.olatResourceDetail=:resSubPath and rset.repositoryRef=:repoKey");

		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resId", olatResource)
				.setParameter("resSubPath", olatResourceDetail)
				.setParameter("repoKey", repositoryRef).getSingleResult();
		return count == null ? false : count.intValue() > 0;
	}

	/**
	 * Get the resulkt sets.
	 * @param olatResource
	 * @param olatResourceDetail
	 * @param repositoryRef
	 * @param identity May be null
	 * @return List of resultsets
	 */
	public List<QTIResultSet> getResultSets(Long olatResource, String olatResourceDetail, Long repositoryRef, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rset from ").append(QTIResultSet.class.getName()).append(" as rset ")
		  .append("where rset.olatResource=:resId and rset.olatResourceDetail=:resSubPath and rset.repositoryRef=:repoKey");
		if (identity != null) {
			sb.append(" and rset.identity.key=:identityKey ");
		}
		
		TypedQuery<QTIResultSet> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QTIResultSet.class)
				.setParameter("resId", olatResource)
				.setParameter("resSubPath", olatResourceDetail)
				.setParameter("repoKey", repositoryRef);
		if (identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		return query.getResultList();
	}

	/**
	 * selects all resultsets of a IQCourseNode of a particular course
	 * 
	 * @param olatResource
	 * @param olatResourceDetail
	 * @param repositoryRef
	 * @return List of QTIResult objects
	 */
	public List<QTIResult> selectResults(Long olatResource, String olatResourceDetail, Long repositoryRef,
			List<Group> limitToSecGroups, int type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res from ").append(QTIResult.class.getName()).append(" as res ")
		  .append(" inner join res.resultSet as rset")
		  .append(" inner join rset.identity as ident")
		  .append(" inner join ident.user as usr")
		  .append(" where rset.olatResource=:resId and rset.olatResourceDetail=:resSubPath and rset.repositoryRef=:repoKey");
		if(limitToSecGroups != null && limitToSecGroups.size() > 0) {
			sb.append(" and rset.identity.key in ( select membership.identity.key from bgroupmember membership ")
			  .append("   where membership.group in (:baseGroups)")
			  .append(" )");
		}
		
		if(type == 1 || type == 2) {
			 // 1 -> iqtest, 2 -> iqself
		    sb.append(" order by usr.lastName, rset.assessmentID, res.itemIdent");
		} else {
			//3 -> iqsurv: the alphabetical assortment above could destroy the anonymization
		    // if names and quantity of the persons is well-known
		    sb.append(" order by rset.creationDate, rset.assessmentID, res.itemIdent");
		}

		TypedQuery<QTIResult> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), QTIResult.class)
				.setParameter("resId", olatResource)
				.setParameter("resSubPath", olatResourceDetail)
				.setParameter("repoKey", repositoryRef);
		
		if(limitToSecGroups != null && limitToSecGroups.size() > 0) {
			query.setParameter("baseGroups", limitToSecGroups);
		}
		
		return query.getResultList();
	}
	
	/**
	 * Same as above but only count the number of results
	 * @param olatResource
	 * @param olatResourceDetail
	 * @param repositoryRef
	 * @return
	 */
	public int countResults(Long olatResource, String olatResourceDetail, Long repositoryRef) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(res.key) from ").append(QTIResult.class.getName()).append(" as res ")
		  .append(" inner join res.resultSet as rset")
		  .append(" inner join rset.identity as ident")
		  .append(" inner join ident.user as usr")
		  .append(" where rset.olatResource=:resId and rset.olatResourceDetail=:resSubPath and rset.repositoryRef=:repoKey");

		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resId", olatResource)
				.setParameter("resSubPath", olatResourceDetail)
				.setParameter("repoKey", repositoryRef)
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	/**
	 * Deletes all Results and ResultSets of a test, selftest or survey
	 * 
	 * @param olatRes
	 * @param olatResDet
	 * @param repRef
	 * @return deleted ResultSets
	 */
	public int deleteAllResults(Long olatRes, String olatResDet, Long repRef) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rset from ").append(QTIResultSet.class.getName()).append(" as rset ");
		sb.append(" where rset.olatResource=:resId and rset.olatResourceDetail=:resSubPath and rset.repositoryRef=:repoKey ");
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<QTIResultSet> sets = em.createQuery(sb.toString(), QTIResultSet.class).setParameter("resId", olatRes)
				.setParameter("resSubPath", olatResDet)
				.setParameter("repoKey", repRef)
				.getResultList();

		StringBuilder delSb = new StringBuilder();
		delSb.append("delete from ").append(QTIResult.class.getName()).append(" as res where res.resultSet.key=:setKey");
		Query delResults = em.createQuery(delSb.toString());
		for (QTIResultSet set:sets) {
			delResults.setParameter("setKey", set.getKey()).executeUpdate();
			em.remove(set);
		}
		return sets.size();
	}
	
	/**
	 * Deletes all Results AND all ResultSets for certain QTI-ResultSet.
	 * @param qtiResultSet
	 */
	public void deleteResults(QTIResultSet qtiResultSet) {
		deleteAllResults(qtiResultSet.getOlatResource(), qtiResultSet.getOlatResourceDetail(), qtiResultSet.getRepositoryRef());
	}

	/**
	 * translates the answerstring stored in table o_qtiresult
	 * 
	 * @param answerCode
	 * @return translation
	 */
	public static Map<String,String> parseResponseStrAnswers(String answerCode) {
		// calculate the correct answer, if eventually needed
		int modus = 0;
		int startIdentPosition = 0;
		int startCharacterPosition = 0;
		String tempIdent = null;
		Map<String,String> result = new HashMap<String,String>();
		char c;

		for (int i = 0; i < answerCode.length(); i++) {
			c = answerCode.charAt(i);
			if (modus == 0) {
				if (c == '[') {
					String sIdent = answerCode.substring(startIdentPosition, i);
					if (sIdent.length() > 0) {
						tempIdent = sIdent;
						modus = 1;
					}
				}
			} else if (modus == 1) {
				if (c == '[') {
					startCharacterPosition = i + 1;
					modus = 2;
				} else if (c == ']') {
					startIdentPosition = i + 1;
					tempIdent = null;
					modus = 0;
				}
			} else if (modus == 2) {
				if (c == ']') {
					if (answerCode.charAt(i - 1) != '\\') {
						String s = answerCode.substring(startCharacterPosition, i);
						if (tempIdent != null) result.put(tempIdent, s.replaceAll("\\\\\\]", "]"));
						modus = 1;
					}
				}
			}
		}
		return result;
	}

	/**
	 * translates the answerstring stored in table o_qtiresult
	 * 
	 * @param answerCode
	 * @return translation
	 */
	public static List<String> parseResponseLidAnswers(String answerCode) {
		// calculate the correct answer, if eventually needed
		int modus = 0;
		int startCharacterPosition = 0;
		List<String> result = new ArrayList<String>();
		char c;

		for (int i = 0; i < answerCode.length(); i++) {
			c = answerCode.charAt(i);
			if (modus == 0) {
				if (c == '[') {
					modus = 1;
				}
			} else if (modus == 1) {
				if (c == '[') {
					startCharacterPosition = i + 1;
					modus = 2;
				} else if (c == ']') {
					modus = 0;
				}
			} else if (modus == 2) {
				if (c == ']') {
					if (answerCode.charAt(i - 1) != '\\') {
						String s = answerCode.substring(startCharacterPosition, i);
						result.add(s.replaceAll("\\\\\\]", "]"));
						modus = 1;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Find all ResultSets for certain identity.
	 * @param identity
	 * @param assessmentID
	 * @return
	 */
	public List<QTIResultSet> findQtiResultSets(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rset from ").append(QTIResultSet.class.getName()).append(" as rset")
		  .append(" where rset.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QTIResultSet.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	/**
	 * Delete all ResultSet for certain identity.
	 * @param identity
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<QTIResultSet> qtiResults = findQtiResultSets(identity);
		for (QTIResultSet set:qtiResults) {
			deleteResultSet(set);
		}
		if(log.isDebugEnabled()) {
			log.debug("Delete all QTI result data in db for identity=" + identity);
		}
	}

	/**
	 * Delete all qti-results and qti-result-set entry for certain result-set.
	 * @param rSet 
	 */
	public void deleteResultSet(QTIResultSet rSet) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		
		StringBuilder delResultsSb = new StringBuilder();
		delResultsSb.append("delete from ").append(QTIResult.class.getName()).append(" as res where res.resultSet.key=:setKey");
		em.createQuery(delResultsSb.toString()).setParameter("setKey", rSet.getKey()).executeUpdate();
		
		StringBuilder delSetSb = new StringBuilder();
		delSetSb.append("delete from ").append(QTIResultSet.class.getName()).append(" as rset where rset.key=:setKey");
		em.createQuery(delSetSb.toString()).setParameter("setKey", rSet.getKey()).executeUpdate();
	}
	
}