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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.ims.qti.statistics.manager.Statistics;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.KPrimStatistics;
import org.olat.ims.qti21.model.statistics.SimpleChoiceStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

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

	@Override
	public StatisticsItem getAssessmentItemStatistics(String itemIdent, double maxScore,
			QTI21StatisticSearchParams searchParams) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.score, count(isession.key), avg(isession.duration) from qtiassessmentitemsession isession ")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams);
		sb.append(" and isession.assessmentItemIdentifier=:itemIdent and isession.duration > 0")
		  .append(" group by isession.score");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("itemIdent", itemIdent);
		decorateRSetQuery(query, searchParams);
		List<Object[]> results = query.getResultList();
		if(results.isEmpty()) {
			return new StatisticsItem();
		}

		int totalResults = 0;
		double totalScore = 0.0;
		double totalDuration = 0.0;
		long numOfCorrectAnswers = 0;
		long numOfIncorrectAnswers = 0;
		
		for(Object[] result:results) {
			double score = ((Number)result[0]).doubleValue();
			long numOfResults = ((Number)result[1]).longValue();
			double averageDuration = ((Number)result[2]).doubleValue();
			
			//average
			totalScore += (score * numOfResults);
			totalResults += numOfResults;
			
			if((maxScore - score) < 0.0001) {
				numOfCorrectAnswers += numOfResults;
			} else {
				numOfIncorrectAnswers += numOfResults;
			}
			
			totalDuration += (averageDuration * numOfResults);
		}

		double averageScore = totalScore / totalResults;
		//difficulty (p-value)
		double difficulty = numOfCorrectAnswers / (double)totalResults;
		double averageDuration = totalDuration / totalResults;
		
		StatisticsItem stats = new StatisticsItem();
		stats.setAverageDuration(Math.round(averageDuration));
		stats.setAverageScore(averageScore);
		stats.setNumOfResults(totalResults);
		stats.setDifficulty(difficulty);
		stats.setNumOfCorrectAnswers(numOfCorrectAnswers);
		stats.setNumOfIncorrectAnswers(numOfIncorrectAnswers);
		return stats;
	}

	@Override
	public List<SimpleChoiceStatistics> getChoiceInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, ChoiceInteraction choiceInteraction, QTI21StatisticSearchParams searchParams) {

		List<RawData> results = getRawDatas(itemRefIdent, choiceInteraction.getResponseIdentifier().toString(), searchParams);
		
		List<SimpleChoice> simpleChoices = choiceInteraction.getSimpleChoices();
		long[] counts = new long[simpleChoices.size()];
		for(int i=counts.length; i-->0; ) {
			counts[i] = 0l;
		}

		for(RawData result:results) {
			Long numOfAnswers = result.getCount();;
			if(numOfAnswers != null && numOfAnswers.longValue() > 0) {
				String stringuifiedResponse = result.getStringuifiedResponse();
				for(int i=simpleChoices.size(); i-->0; ) {
					String identifier = simpleChoices.get(i).getIdentifier().toString();
					if(stringuifiedResponse.contains(identifier)) {
						counts[i] += numOfAnswers.longValue();
					}
				}
			}
		}

		List<SimpleChoiceStatistics> choicesStatistics = new ArrayList<>();
		for(int i=0; i<simpleChoices.size(); i++) {
			choicesStatistics.add(new SimpleChoiceStatistics(simpleChoices.get(i), counts[i]));
		}
		return choicesStatistics;
	}
	
	//stringuifiedResponse: [a93247453265982 correct][b93247453265983 correct][c93247453265984 correct][d93247453265985 correct]
	@Override
	public List<KPrimStatistics> getKPrimStatistics(String itemRefIdent, AssessmentItem item, MatchInteraction interaction,
			QTI21StatisticSearchParams searchParams) {
		List<RawData> rawDatas = getRawDatas(itemRefIdent, interaction.getResponseIdentifier().toString(), searchParams);
		List<SimpleMatchSet> matchSets = interaction.getSimpleMatchSets();
		List<KPrimStatistics> kprimPoints = new ArrayList<>();
		
		SimpleMatchSet fourMatchSet = matchSets.get(0);
		ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
		
		//readable responses
		Set<String> rightResponses = new HashSet<>();
		List<MapEntry> mapEntries = responseDeclaration.getMapping().getMapEntries();
		for(MapEntry mapEntry:mapEntries) {
			SingleValue mapKey = mapEntry.getMapKey();
			if(mapKey instanceof DirectedPairValue) {
				DirectedPairValue pairValue = (DirectedPairValue)mapKey;
				String source = pairValue.sourceValue().toString();
				String destination = pairValue.destValue().toString();
				rightResponses.add("[" + source + " " + destination + "]");
			}
		}
		
		for(SimpleAssociableChoice choice:fourMatchSet.getSimpleAssociableChoices()) {
			String choiceIdentifier = choice.getIdentifier().toString();
			String markerCorrect = "[" + choiceIdentifier + " correct]";
			String markerWrong = "[" + choiceIdentifier + " wrong]";
			
			boolean isCorrectRight = rightResponses.contains(markerCorrect);
			String rightFlag = isCorrectRight ? markerCorrect : markerWrong;
			String wrongFlag = isCorrectRight ? markerWrong : markerCorrect;

			long numCorrect = 0;
			long numIncorrect = 0;
			long numUnanswered = 0;
			for(RawData rawData:rawDatas) {
				String response = rawData.getStringuifiedResponse();
				if(response.indexOf(rightFlag) >= 0) {
					numCorrect += rawData.getCount();
				} else if(response.indexOf(wrongFlag) >= 0) {
					numIncorrect += rawData.getCount();
				} else {
					numUnanswered += rawData.getCount();
				}
			}

			kprimPoints.add(new KPrimStatistics(choice.getIdentifier(), isCorrectRight, numCorrect, numIncorrect, numUnanswered));
		}
		return kprimPoints;
	}
	
	private List<RawData> getRawDatas(String itemRefIdent, String responseIdentifier, QTI21StatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.key, aresponse.responseIdentifier, aresponse.stringuifiedResponse, count(aresponse.key) from qtiassessmentresponse aresponse ")
		  .append(" inner join aresponse.assessmentItemSession isession")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams);
		sb.append(" and isession.assessmentItemIdentifier=:itemIdent and aresponse.responseIdentifier=:responseIdentifier and isession.duration > 0")
		  .append(" group by isession.key, aresponse.responseIdentifier, aresponse.stringuifiedResponse");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("itemIdent", itemRefIdent)
				.setParameter("responseIdentifier", responseIdentifier);
		decorateRSetQuery(query, searchParams);
		List<Object[]> results = query.getResultList();
		if(results.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<RawData> datas = new ArrayList<>(results.size());
		for(Object[] result:results) {
			Long itemSessionKey = PersistenceHelper.extractLong(result, 0);
			String stringuifiedResponse = PersistenceHelper.extractString(result, 2);
			Long count = PersistenceHelper.extractLong(result, 3);
			datas.add(new RawData(itemSessionKey, responseIdentifier, stringuifiedResponse, count));
		}
		return datas;
	}
	
	public static class RawData {
		
		private final Long itemSessionKey;
		private final String responseIdentifier;
		private final String stringuifiedResponse;
		private final Long count;
		
		public RawData(Long itemSessionKey, String responseIdentifier, String stringuifiedResponse, Long count) {
			this.itemSessionKey = itemSessionKey;
			this.responseIdentifier = responseIdentifier;
			this.stringuifiedResponse = stringuifiedResponse;
			this.count = count;
		}

		public Long getItemSessionKey() {
			return itemSessionKey;
		}

		public String getResponseIdentifier() {
			return responseIdentifier;
		}

		public String getStringuifiedResponse() {
			return stringuifiedResponse;
		}

		public Long getCount() {
			return count;
		}
	}
}