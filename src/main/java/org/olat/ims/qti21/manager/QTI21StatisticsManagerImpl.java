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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.ims.qti21.manager;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti.statistics.manager.Statistics;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21StatisticsManagerImpl implements QTI21StatisticsManager {
	
	@Autowired
	private DB dbInstance;
	
	private StringBuilder decorateRSet(StringBuilder sb, QTI21StatisticSearchParams searchParams) {
		sb.append(" where asession.testEntry.key=:testEntryKey");
		if(searchParams.getCourseEntry() != null) {
			sb.append(" and asession.repositoryEntry.key=:repositoryEntryKey and asession.subIdent=:subIdent");
		}
		sb.append(" and asession.lastModified = (select max(a2session.lastModified) from qtiassessmenttestsession a2session")
		  .append("   where a2session.identity.key=asession.identity.key and a2session.repositoryEntry.key=asession.repositoryEntry.key")
		  .append("   and a2session.subIdent=asession.subIdent")
		  .append(" )");
		
		if(searchParams.getLimitToGroups() != null && searchParams.getLimitToGroups().size() > 0) {
			sb.append(" and asession.identity.key in ( select membership.identity.key from bgroupmember membership ")
			  .append("   where membership.group in (:baseGroups)")
			  .append(" )");
		}
		
		if(searchParams.isMayViewAllUsersAssessments()) {
			sb.append(" and asession.identity.key in (select data.identity.key from assessmententry data ")
			  .append("   where data.repositoryEntry=asession.repositoryEntry")
			  .append(" )");
		}
		return sb;
	}
	
	private void decorateRSetQuery(TypedQuery<?> query, QTI21StatisticSearchParams searchParams) {
		query.setParameter("testEntryKey", searchParams.getTestEntry().getKey());
		if(searchParams.getCourseEntry() != null) {
			query.setParameter("repositoryEntryKey", searchParams.getCourseEntry().getKey());
			query.setParameter("subIdent", searchParams.getNodeIdent());
		}
		if(searchParams.getLimitToGroups() != null && searchParams.getLimitToGroups().size() > 0) {
			query.setParameter("baseGroups", searchParams.getLimitToGroups());
		}
	}

	@Override
	public StatisticAssessment getAssessmentStatistics(QTI21StatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asession.score, asession.passed, asession.duration from qtiassessmenttestsession asession ");
		decorateRSet(sb, searchParams);
		sb.append(" order by asession.key asc");

		TypedQuery<Object[]> rawDataQuery = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class);
		decorateRSetQuery(rawDataQuery, searchParams);
		List<Object[]> rawDatas =	rawDataQuery.getResultList();
		
		int numOfPassed = 0;
		int numOfFailed = 0;
		double totalDuration = 0.0;
		double maxScore = 0.0;
		double minScore = Double.MAX_VALUE;
		double[] scores = new double[rawDatas.size()];
		double[] durationSeconds = new double[rawDatas.size()];
		
		double minDuration = Double.MAX_VALUE;
		double maxDuration = 0d;
		
		int dataPos = 0;
		for(Object[] rawData:rawDatas) {
			BigDecimal score = (BigDecimal)rawData[0];
			if(score != null) {
				double scored = score.doubleValue();
				scores[dataPos] = scored;
				maxScore = Math.max(maxScore, scored);
				minScore = Math.min(minScore, scored);
			}
			
			Boolean passed = (Boolean)rawData[1];
			if(passed != null) {
				if(passed.booleanValue()) {
					numOfPassed++;
				} else {
					numOfFailed++;
				}
			}

			Long duration = (Long)rawData[2];
			if(duration != null) {
				double durationd = duration.doubleValue();
				double durationSecond = Math.round(durationd / 1000d);
				durationSeconds[dataPos] = durationSecond;
				totalDuration += durationd;
				minDuration = Math.min(minDuration, durationSecond);
				maxDuration = Math.max(maxDuration, durationSecond);
			}
			dataPos++;
		}
		if (rawDatas.size() == 0) {
			minScore = 0;
		}
		
		Statistics statisticsHelper = new Statistics(scores);		

		int numOfParticipants = rawDatas.size();
		StatisticAssessment stats = new StatisticAssessment();
		stats.setNumOfParticipants(numOfParticipants);
		stats.setNumOfPassed(numOfPassed);
		stats.setNumOfFailed(numOfFailed);
		long averageDuration = Math.round(totalDuration / numOfParticipants);
		stats.setAverageDuration(averageDuration);
		stats.setAverage(statisticsHelper.getMean());
		double range = maxScore - minScore;
		stats.setRange(range);
		stats.setMaxScore(maxScore);
		stats.setMinScore(minScore);
		stats.setStandardDeviation(statisticsHelper.getStdDev());
		stats.setMedian(statisticsHelper.median());
		stats.setMode(statisticsHelper.mode());
		stats.setScores(scores);
		stats.setDurations(durationSeconds);
		return stats;
	}

}