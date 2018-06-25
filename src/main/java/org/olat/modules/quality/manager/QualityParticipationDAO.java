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
package org.olat.modules.quality.manager;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityParticipationDAO {
	
	@Autowired
	private DB dbInstance;

	int getParticipationCount(QualityDataCollectionLight dataCollection) {
		if (dataCollection == null) return 0;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(participation.key)");
		sb.append("  from evaluationformparticipation as participation");
		sb.append(" inner join participation.survey as survey");
		sb.append(" where survey.resName=:resName");
		sb.append("   and survey.resId=:resId");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resName", dataCollection.getResourceableTypeName())
				.setParameter("resId", dataCollection.getResourceableId())
				.getResultList();
		return Math.toIntExact(counts.get(0));
	}

	List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy) {
		if (dataCollection == null)
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityParticipationImpl(");
		sb.append("       participation.key");
		sb.append("     , user.firstName");
		sb.append("     , user.lastName");
		sb.append("     , user.email");
		sb.append("       )");
		sb.append("  from evaluationformparticipation as participation");
		sb.append(" inner join participation.survey as survey");
		sb.append("  left join participation.executor as executor");
		sb.append(" inner join executor.user as user");
		sb.append(" where survey.resName=:resName");
		sb.append("   and survey.resId=:resId");
		
		appendParticipationOrderBy(sb, orderBy);

		TypedQuery<QualityParticipation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityParticipation.class)
				.setParameter("resName", dataCollection.getResourceableTypeName())
				.setParameter("resId", dataCollection.getResourceableId());
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private void appendParticipationOrderBy(StringBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			if (sortKey.equals(ParticipationCols.firstname.name())) {
				sb.append("user.firstName");
			} else if (sortKey.equals(ParticipationCols.lastname.name())) {
				sb.append("user.lastName");
			} else if (sortKey.equals(ParticipationCols.email.name())) {
				sb.append("user.email");
			} else {
				sb.append(sortKey);
			}
			appendAsc(sb, asc);
		} else {
			sb.append(" order by participation.key asc ");
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}

	int getExecutorParticipationCount(IdentityRef executor) {
		if (executor == null) return 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(participation.key)");
		sb.append("  from evaluationformparticipation as participation");
		sb.append(" inner join participation.survey as survey");
		sb.append(" inner join participation.executor as executor");
		sb.append(" where survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("   and executor.key = :executorKey");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("executorKey", executor.getKey())
				.getResultList();
		return Math.toIntExact(counts.get(0));
	}

	public List<QualityExecutorParticipation> loadExecutorParticipations(IdentityRef executor, int firstResult,
			int maxResults, SortKey... orderBy) {
		if (executor == null)
			return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityExcecutorParticipationImpl(");
		sb.append("       participation.key as participationKey");
		sb.append("     , participation.status as participationStatus");
		sb.append("     , collection.start as start");
		sb.append("     , collection.deadline as deadline");
		sb.append("     , collection.title as title");
		sb.append("       )");
		sb.append("  from evaluationformparticipation as participation");
		sb.append(" inner join participation.survey as survey");
		sb.append(" inner join participation.executor as executor");
		sb.append(" inner join qualitydatacollection as collection on collection.key = survey.resId");
		sb.append(" where survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("   and executor.key = :executorKey");
		
		appendExecutorParticipationOrderBy(sb, orderBy);

		TypedQuery<QualityExecutorParticipation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityExecutorParticipation.class)
				.setParameter("executorKey", executor.getKey());
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private void appendExecutorParticipationOrderBy(StringBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			sb.append(sortKey);
			appendAsc(sb, asc);
		} else {
			sb.append(" order by participation.status asc ");
		}
	}

}
