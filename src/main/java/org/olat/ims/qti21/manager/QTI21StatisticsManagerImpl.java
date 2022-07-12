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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.AbstractTextEntryInteractionStatistics;
import org.olat.ims.qti21.model.statistics.AssessmentItemStatistic;
import org.olat.ims.qti21.model.statistics.ChoiceStatistics;
import org.olat.ims.qti21.model.statistics.HotspotChoiceStatistics;
import org.olat.ims.qti21.model.statistics.InlineChoiceInteractionStatistics;
import org.olat.ims.qti21.model.statistics.KPrimStatistics;
import org.olat.ims.qti21.model.statistics.MatchStatistics;
import org.olat.ims.qti21.model.statistics.NumericalInputInteractionStatistics;
import org.olat.ims.qti21.model.statistics.OrderStatistics;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.ims.qti21.model.statistics.StatisticsItem;
import org.olat.ims.qti21.model.statistics.StatisticsPart;
import org.olat.ims.qti21.model.statistics.TextEntryInteractionStatistics;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.modules.vitero.model.GroupRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;

/**
 * 
 * Initial date: 24.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21StatisticsManagerImpl implements QTI21StatisticsManager {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21StatisticsManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	
	private StringBuilder decorateRSet(StringBuilder sb, QTI21StatisticSearchParams searchParams, boolean finished) {
		sb.append(" where asession.testEntry.key=:testEntryKey and asession.repositoryEntry.key=:repositoryEntryKey");
		if(searchParams.getNodeIdent() != null ) {
			sb.append(" and asession.subIdent=:subIdent");
		} else {
			sb.append(" and asession.subIdent is null");
		}
		sb.append(" and asession.authorMode=false");

		if(finished) {
			sb.append(" and asession.finishTime is not null");
		}
		
		sb.append(" and asession.lastModified = (select max(a2session.lastModified) from qtiassessmenttestsession a2session")
		  .append("   where asession.testEntry.key=a2session.testEntry.key and a2session.repositoryEntry.key=asession.repositoryEntry.key")
		  .append("   and a2session.exploded=false and a2session.cancelled=false");
		if(searchParams.getNodeIdent() != null ) {
			sb.append(" and a2session.subIdent=asession.subIdent");
		} else {
			sb.append(" and asession.subIdent is null and a2session.subIdent is null");
		}
		sb.append("   and (a2session.identity.key=asession.identity.key or a2session.anonymousIdentifier=asession.anonymousIdentifier)")
		  .append(" )");

		if(searchParams.isViewAllUsers() && searchParams.isViewAnonymUsers()) {
			//no restrictions
		} else if(searchParams.isViewAnonymUsers()) {
			sb.append(" and asession.anonymousIdentifier is not null");
		} else if(searchParams.isViewAllUsers()) {
			sb.append(" and asession.identity.key in (select data.identity.key from assessmententry data")
			  .append("   where data.repositoryEntry.key=asession.repositoryEntry.key")
			  .append(" )");
		} else if(searchParams.getLimitToGroups() != null && !searchParams.getLimitToGroups().isEmpty()) {
			sb.append(" and asession.identity.key in ( select membership.identity.key from bgroupmember membership")
			  .append("   where membership.group in (:baseGroups) and membership.role='").append(GroupRole.participant).append("'")
			  .append(" )");
		} else if(searchParams.getLimitToIdentities() != null && !searchParams.getLimitToIdentities().isEmpty()) {
			sb.append(" and asession.identity.key in (:limitIdentityKeys)");
		} else {
			//limit to participants
			sb.append(" and (asession.identity.key in ( select membership.identity.key from repoentrytogroup as rel, bgroupmember membership ")
			  .append("   where rel.entry.key=:repositoryEntryKey and rel.group.key=membership.group.key and membership.role='").append(GroupRole.participant).append("'")
			  //.append("   where rel.entry.key=:repositoryEntryKey and rel.group.key=reBaseGroup.key and membership.group.key=reBaseGroup.key and membership.role='").append(GroupRole.participant).append("'")
			   .append(" )");
			// add non members
			if(searchParams.isViewNonMembers()) {
				sb.append(" or asession.identity.key not in (select membership.identity.key from repoentrytogroup as rel, bgroupmember as membership")
			      .append("    where rel.entry.key=:repositoryEntryKey and rel.group.key=membership.group.key")
			      .append(" )");
			}
			sb.append(")");
		}

		return sb;
	}
	
	private void decorateRSetQuery(TypedQuery<?> query, QTI21StatisticSearchParams searchParams) {
		query.setParameter("testEntryKey", searchParams.getTestEntry().getKey());
		if(searchParams.getCourseEntry() == null) {
			query.setParameter("repositoryEntryKey", searchParams.getTestEntry().getKey());
		} else {
			query.setParameter("repositoryEntryKey", searchParams.getCourseEntry().getKey());
		}
		if(searchParams.getNodeIdent() != null ) {
			query.setParameter("subIdent", searchParams.getNodeIdent());
		}
		
		if(searchParams.isViewAllUsers() && searchParams.isViewAnonymUsers()) {
			//no restrictions
		} else if(searchParams.isViewAnonymUsers()) {
			//
		} else if(searchParams.isViewAllUsers()) {
			//
		} else if(searchParams.getLimitToGroups() != null && !searchParams.getLimitToGroups().isEmpty()) {
			query.setParameter("baseGroups", searchParams.getLimitToGroups());
		} else if(searchParams.getLimitToIdentities() != null && !searchParams.getLimitToIdentities().isEmpty()) {
			List<Long> keys = searchParams.getLimitToIdentities().stream()
					.map(Identity::getKey).collect(Collectors.toList());
			query.setParameter("limitIdentityKeys", keys);
		}
	}

	@Override
	public StatisticAssessment getAssessmentStatistics(QTI21StatisticSearchParams searchParams, Double cutValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asession.score, asession.manualScore, asession.passed, asession.duration from qtiassessmenttestsession asession ");
		decorateRSet(sb, searchParams, true);
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
		double maxDuration = 0.0d;
		
		BigDecimal cutBigValue = cutValue == null ? null : BigDecimal.valueOf(cutValue.doubleValue());
		
		int dataPos = 0;
		boolean hasScore = false;
		for(Object[] rawData:rawDatas) {
			int pos = 0;
			BigDecimal score = (BigDecimal)rawData[pos++];
			BigDecimal manualScore = (BigDecimal)rawData[pos++];
			if(score == null) {
				score = manualScore;
			} else if(manualScore != null) {
				score = score.add(manualScore);
			}
			if(score != null) {
				double scored = score.doubleValue();
				scores[dataPos] = scored;
				maxScore = Math.max(maxScore, scored);
				minScore = Math.min(minScore, scored);
				hasScore = true;
			}
			
			Boolean passed = (Boolean)rawData[pos++];
			if(cutBigValue != null && score != null) {
				passed = score.compareTo(cutBigValue) >= 0;
			}
			if(passed != null) {
				if(passed.booleanValue()) {
					numOfPassed++;
				} else {
					numOfFailed++;
				}
			}

			Long duration = (Long)rawData[pos];
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
		if (rawDatas.isEmpty()) {
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
		if(hasScore) {
			double range = maxScore - minScore;
			stats.setRange(range);
			stats.setMaxScore(maxScore);
			stats.setMinScore(minScore);
		}
		stats.setStandardDeviation(statisticsHelper.getStdDev());
		stats.setMedian(statisticsHelper.median());
		stats.setMode(statisticsHelper.mode());
		stats.setScores(scores);
		stats.setDurations(durationSeconds);
		return stats;
	}

	@Override
	public StatisticsPart getAssessmentPartStatistics(double maxScore,
			QTI21StatisticSearchParams searchParams, TestPart testPart, List<AssessmentSection> sections) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.key, isession.score, isession.manualScore, isession.duration, asession.identity.key from qtiassessmentitemsession isession ")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams, true);
		
		if(testPart != null) {
			sb.append(" and isession.testPartIdentifier=:testPartId");
		}
		if(sections != null && !sections.isEmpty()) {
			sb.append(" and isession.sectionIdentifier in (:sectionPartIds)");
		}
		
		sb.append(" and isession.duration > 0")
		  .append(" order by asession.identity.key");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class);
		decorateRSetQuery(query, searchParams);
		if(testPart != null) {
			query.setParameter("testPartId", testPart.getIdentifier().toString());
		}
		if(sections != null && !sections.isEmpty()) {
			List<String> sectionIds = sections.stream()
					.map(sect -> sect.getIdentifier().toString()).collect(Collectors.toList());
			query.setParameter("sectionPartIds", sectionIds);
		}
		
		List<Object[]> results = query.getResultList();

		
		boolean hasScore = false;
		double minScore = 0.0d;
		double totalDuration = 0.0;

		IdentityStats currentIdentity = null;
		List<IdentityStats> list = new ArrayList<>();
		for(Object[] result:results) {
			Long identityKey = (Long)result[4];
			if(currentIdentity == null || !currentIdentity.isSameIdentity(identityKey)) {
				currentIdentity = new IdentityStats(identityKey);
				list.add(currentIdentity);
			}

			BigDecimal score = (BigDecimal)result[1];
			BigDecimal manualScore = (BigDecimal)result[2];
			if(score == null) {
				score = manualScore;
			} else if(manualScore != null) {
				score = score.add(manualScore);
			}
			
			if(currentIdentity.score == null) {
				currentIdentity.score = score;
			} else {
				currentIdentity.score = currentIdentity.score.add(score);
			}

			Number duration = (Number)result[3];
			if(duration != null) {
				double durationd = duration.doubleValue();
				currentIdentity.duration += durationd;
				totalDuration += durationd;
			}
		}

		double[] scores = new double[list.size()];
		double[] durationSeconds = new double[list.size()];
		for(int i=0; i<list.size(); i++) {
			IdentityStats stats = list.get(i);
			if(list.get(i).score != null) {
				scores[i] = stats.score.doubleValue();
			}
			if(stats.duration > 0.0d) {
				durationSeconds[i] = stats.duration / 1000.0d;
			}
		}
		
		Statistics statisticsHelper = new Statistics(scores);		

		StatisticsPart stats = new StatisticsPart();
		int numOfParticipants = list.size();
		stats.setNumOfParticipants(numOfParticipants);

		long averageDuration = 0l;
		if(numOfParticipants > 0) {
			averageDuration = Math.round(totalDuration / numOfParticipants);
		}
		stats.setAverageDuration(averageDuration);
		stats.setAverage(statisticsHelper.getMean());
		if(hasScore) {
			double range = maxScore - minScore;
			stats.setRange(range);
			stats.setMaxScore(maxScore);
			stats.setMinScore(minScore);
		}
		stats.setStandardDeviation(statisticsHelper.getStdDev());
		stats.setMedian(statisticsHelper.median());
		stats.setMode(statisticsHelper.mode());
		stats.setScores(scores);

		stats.setDurations(durationSeconds);
		
		return stats;
	}
	
	private static class IdentityStats {
		
		private final Long identityKey;
		
		private double duration = 0.0d;
		private BigDecimal score = null;
		
		public IdentityStats(Long identityKey) {
			this.identityKey = identityKey;
		}
		
		public boolean isSameIdentity(Long newIdentityKey) {
			return identityKey.equals(newIdentityKey);
		}
	}

	@Override
	public StatisticsItem getAssessmentItemStatistics(String itemIdent, double maxScore,
			QTI21StatisticSearchParams searchParams) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.score, isession.manualScore, count(isession.key), avg(isession.duration) from qtiassessmentitemsession isession ")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams, true);
		sb.append(" and isession.assessmentItemIdentifier=:itemIdent and isession.duration > 0")
		  .append(" group by isession.score, isession.manualScore");

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
			BigDecimal score = (BigDecimal)result[0];
			BigDecimal manualScore = (BigDecimal)result[1];
			if(score == null) {
				score = manualScore;
			} else if(manualScore != null) {
				score = score.add(manualScore);
			}
			
			long numOfResults = ((Number)result[2]).longValue();
			double averageDuration = ((Number)result[3]).doubleValue();
			
			//average
			double dScore = score == null ? 0.0d : score.doubleValue();
			totalScore += (dScore * numOfResults);
			totalResults += numOfResults;
			
			if((maxScore - dScore) < 0.0001) {
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
	public List<OrderStatistics> getOrderInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, OrderInteraction orderInteraction, QTI21StatisticSearchParams searchParams) {

		List<RawData> results = getRawDatas(itemRefIdent, orderInteraction.getResponseIdentifier().toString(), searchParams);
		
		List<SimpleChoice> orderedChoices = CorrectResponsesUtil.getCorrectOrderedChoices(assessmentItem, orderInteraction);
		long[] numOfCorrects = new long[orderedChoices.size()];
		long[] numOfIncorrects = new long[orderedChoices.size()];
		long[] numOfNotAnswered = new long[orderedChoices.size()];
		for(int i=numOfCorrects.length; i-->0; ) {
			numOfCorrects[i] = 0l;
			numOfIncorrects[i] = 0l;
			numOfNotAnswered[i] = 0l;
		}
		
		List<Identifier> orderedChoiceIdentifiers = CorrectResponsesUtil.getCorrectOrderedIdentifierResponses(assessmentItem, orderInteraction);
		List<String> orderedStringIdentifiers = orderedChoiceIdentifiers.stream()
				.map(Identifier::toString)
				.collect(Collectors.toList());

		for(RawData result:results) {
			Long numOfAnswers = result.getCount();
			if(numOfAnswers != null && numOfAnswers.longValue() > 0) {
				String stringuifiedResponse = result.getStringuifiedResponse();
				List<String> orderedResponses = splitStringuifiedResponse(stringuifiedResponse);

				for(int i=0; i<orderedChoices.size(); i++) {
					if(i < orderedStringIdentifiers.size()) {
						String identifier = orderedStringIdentifiers.get(i);
						if(i < orderedResponses.size()) {
							if(identifier.equals(orderedResponses.get(i))) {
								numOfCorrects[i]++;
							} else {
								numOfIncorrects[i]++;
							}		
						} else {
							numOfNotAnswered[i]++;
						}
					} else {
						// not the same number of choices as the number of defined correct answers
						if(i < orderedResponses.size()) {
							numOfIncorrects[i]++;
						} else {
							numOfCorrects[i]++;
						}
					}
				}
			}
		}

		List<OrderStatistics> choicesStatistics = new ArrayList<>();
		for(int i=0; i<orderedChoices.size(); i++) {
			choicesStatistics.add(new OrderStatistics(orderedChoices.get(i), numOfCorrects[i], numOfIncorrects[i], numOfNotAnswered[i]));
		}
		return choicesStatistics;
	}
	
	private List<String> splitStringuifiedResponse(String stringuifiedResponse) {
		String[] orderResponses = stringuifiedResponse.split("[\\[,\\]]");
		List<String> orderResponsesList = new ArrayList<>(orderResponses.length);
		for(String orderResponse:orderResponses) {
			if(StringHelper.containsNonWhitespace(orderResponse)) {
				orderResponsesList.add(orderResponse);
			}
		}
		return orderResponsesList;
	}

	@Override
	public List<ChoiceStatistics> getChoiceInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, ChoiceInteraction choiceInteraction, QTI21StatisticSearchParams searchParams) {

		List<RawData> results = getRawDatas(itemRefIdent, choiceInteraction.getResponseIdentifier().toString(), searchParams);
		
		List<SimpleChoice> simpleChoices = choiceInteraction.getSimpleChoices();
		long[] counts = new long[simpleChoices.size()];
		for(int i=counts.length; i-->0; ) {
			counts[i] = 0l;
		}

		for(RawData result:results) {
			Long numOfAnswers = result.getCount();
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

		List<ChoiceStatistics> choicesStatistics = new ArrayList<>();
		for(int i=0; i<simpleChoices.size(); i++) {
			choicesStatistics.add(new ChoiceStatistics(simpleChoices.get(i), counts[i]));
		}
		return choicesStatistics;
	}
	
	@Override
	public List<ChoiceStatistics> getHottextInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, HottextInteraction hottextInteraction,
			QTI21StatisticSearchParams searchParams) {

		List<RawData> results = getRawDatas(itemRefIdent, hottextInteraction.getResponseIdentifier().toString(), searchParams);
		
		List<Hottext> hottexts = QueryUtils.search(Hottext.class, hottextInteraction);
		long[] counts = new long[hottexts.size()];
		for(int i=counts.length; i-->0; ) {
			counts[i] = 0l;
		}

		for(RawData result:results) {
			Long numOfAnswers = result.getCount();
			if(numOfAnswers != null && numOfAnswers.longValue() > 0) {
				String stringuifiedResponse = result.getStringuifiedResponse();
				for(int i=hottexts.size(); i-->0; ) {
					String identifier = hottexts.get(i).getIdentifier().toString();
					if(stringuifiedResponse.contains(identifier)) {
						counts[i] += numOfAnswers.longValue();
					}
				}
			}
		}

		List<ChoiceStatistics> choicesStatistics = new ArrayList<>();
		for(int i=0; i<hottexts.size(); i++) {
			choicesStatistics.add(new ChoiceStatistics(hottexts.get(i), counts[i]));
		}
		return choicesStatistics;
	}

	@Override
	public List<HotspotChoiceStatistics> getHotspotInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, HotspotInteraction hotspotInteraction,
			QTI21StatisticSearchParams searchParams) {

		List<RawData> results = getRawDatas(itemRefIdent, hotspotInteraction.getResponseIdentifier().toString(), searchParams);
		
		List<HotspotChoice> hotspotChoices = hotspotInteraction.getHotspotChoices();
		long[] counts = new long[hotspotChoices.size()];
		for(int i=counts.length; i-->0; ) {
			counts[i] = 0l;
		}

		for(RawData result:results) {
			Long numOfAnswers = result.getCount();
			if(numOfAnswers != null && numOfAnswers.longValue() > 0) {
				String stringuifiedResponse = result.getStringuifiedResponse();
				for(int i=hotspotChoices.size(); i-->0; ) {
					String identifier = hotspotChoices.get(i).getIdentifier().toString();
					if(stringuifiedResponse.contains(identifier)) {
						counts[i] += numOfAnswers.longValue();
					}
				}
			}
		}

		List<HotspotChoiceStatistics> choicesStatistics = new ArrayList<>();
		for(int i=0; i<hotspotChoices.size(); i++) {
			choicesStatistics.add(new HotspotChoiceStatistics(hotspotChoices.get(i), counts[i]));
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
	
	@Override
	public List<MatchStatistics> getMatchStatistics(String itemRefIdent, AssessmentItem item, MatchInteraction interaction,
			QTI21StatisticSearchParams searchParams) {
		List<RawData> rawDatas = getRawDatas(itemRefIdent, interaction.getResponseIdentifier().toString(), searchParams);
		SimpleMatchSet sourceMatchSets = interaction.getSimpleMatchSets().get(0);
		SimpleMatchSet targetMatchSets = interaction.getSimpleMatchSets().get(1);
		
		List<MatchStatistics> matchPoints = new ArrayList<>();
		ResponseDeclaration responseDeclaration = item.getResponseDeclaration(interaction.getResponseIdentifier());
		
		//readable responses
		Map<Identifier,List<Identifier>> associations = new HashMap<>();
		CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
		QtiNodesExtractor.extractIdentifiersFromCorrectResponse(correctResponse, associations);
		
		for(SimpleAssociableChoice sourceChoice:sourceMatchSets.getSimpleAssociableChoices()) {
			for(SimpleAssociableChoice targetChoice:targetMatchSets.getSimpleAssociableChoices()) {
				DirectedPairValue dKey = new DirectedPairValue(sourceChoice.getIdentifier(), targetChoice.getIdentifier());
				String choiceIdentifier = dKey.toQtiString();
				String marker = "[" + choiceIdentifier + "]";
				
				boolean correct = associations.containsKey(sourceChoice.getIdentifier())
						&& associations.get(sourceChoice.getIdentifier()).contains(targetChoice.getIdentifier());

				long numCorrect = 0;
				long numIncorrect = 0;
				for(RawData rawData:rawDatas) {
					String response = rawData.getStringuifiedResponse();
					if(response.indexOf(marker) >= 0) {
						if(correct) {
							numCorrect += rawData.getCount();
						} else {
							numIncorrect += rawData.getCount();
						}
					}
				}
				matchPoints.add(new MatchStatistics(sourceChoice.getIdentifier(), targetChoice.getIdentifier(), numCorrect, numIncorrect));
			}
		}
		return matchPoints;
	}
	
	@Override
	public List<AbstractTextEntryInteractionStatistics> getTextEntryInteractionsStatistic(String itemRefIdent, AssessmentItem item, List<TextEntryInteraction> interactions,
			QTI21StatisticSearchParams searchParams) {

		List<AbstractTextEntryInteractionStatistics> options = new ArrayList<>();
		Map<String, AbstractTextEntryInteractionStatistics> optionMap = new HashMap<>();

		for(TextEntryInteraction interaction:interactions) {
			Identifier responseIdentifier = interaction.getResponseIdentifier();
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(responseIdentifier);
			if(responseDeclaration == null) {
				log.warn("Missing response declaration {}", responseIdentifier);
			} else if(responseDeclaration.hasBaseType(BaseType.STRING)) {
				TextEntryInteractionStatistics stats = getTextEntryInteractionSettings(responseIdentifier, responseDeclaration);
				optionMap.put(responseIdentifier.toString(), stats);
				options.add(stats);
			} else if(responseDeclaration.hasBaseType(BaseType.FLOAT)) {
				NumericalInputInteractionStatistics stats = getNumericalInputInteractionSettings(responseIdentifier, responseDeclaration, item);
				optionMap.put(responseIdentifier.toString(), stats);
				options.add(stats);
			}
		}
		
		for(TextEntryInteraction interaction:interactions) {
			String responseIdentifier = interaction.getResponseIdentifier().toString();
			List<RawData> datas = getRawDatas(itemRefIdent, responseIdentifier, searchParams);
			for(RawData data:datas) {
				Long count = data.getCount();
				if(count != null && count.longValue() > 0) {
					AbstractTextEntryInteractionStatistics stats = optionMap.get(responseIdentifier);
					String response = data.getStringuifiedResponse();
					if(response != null && response.length() >= 2 && response.startsWith("[") && response.endsWith("]")) {
						response = response.substring(1, response.length() - 1);
					}
					
					if(stats == null) {
						// missing response declaration
					} else if(stats.matchResponse(response)) {
						stats.addCorrect(count.longValue());
					} else {
						stats.addIncorrect(count.longValue());
						stats.addWrongResponses(response);
					}
				}
			}
		}

		return options;
	}
	
	private NumericalInputInteractionStatistics getNumericalInputInteractionSettings(Identifier responseIdentifier, ResponseDeclaration responseDeclaration, AssessmentItem item) {
		NumericalEntry numericalEntry = new NumericalEntry(responseIdentifier);
		FIBAssessmentItemBuilder.extractNumericalEntrySettings(item, numericalEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());

		String correctResponse = "";
		Double solution = numericalEntry.getSolution();
		if(numericalEntry.getSolution() != null) {
			correctResponse = solution.toString();
		}
		
		double points;
		if(numericalEntry.getScore() == null) {
			points = 0.0d;//all score
		} else  {
			points = numericalEntry.getScore().doubleValue();
		}

		return new NumericalInputInteractionStatistics(responseIdentifier, correctResponse, solution,
				numericalEntry.getToleranceMode(), numericalEntry.getLowerTolerance(), numericalEntry.getUpperTolerance(),
				points);
	}
	
	private TextEntryInteractionStatistics getTextEntryInteractionSettings(Identifier responseIdentifier, ResponseDeclaration responseDeclaration) {
		String correctResponse = null;
		boolean caseSensitive = true;
		double points = Double.NaN;
		List<String> alternatives = new ArrayList<>();

		Mapping mapping = responseDeclaration.getMapping();
		if(mapping != null) {
			List<MapEntry> mapEntries = mapping.getMapEntries();
			for(MapEntry mapEntry:mapEntries) {
				SingleValue mapKey = mapEntry.getMapKey();
				if(mapKey instanceof StringValue) {
					String value = ((StringValue)mapKey).stringValue();
					if(correctResponse == null) {
						correctResponse = value;
						points = mapEntry.getMappedValue();
					} else {
						alternatives.add(value);
					}
				}
				
				caseSensitive &= mapEntry.getCaseSensitive();
			}
		}

		if(points == -1.0d) {
			points = 0.0d;//all score
		}

		return new TextEntryInteractionStatistics(responseIdentifier, caseSensitive, correctResponse, alternatives, points);
	}
	
	@Override
	public List<InlineChoiceInteractionStatistics> getInlineChoiceInteractionsStatistic(String itemRefIdent,
			AssessmentItem item, List<InlineChoiceInteraction> interactions, QTI21StatisticSearchParams searchParams) {
		
		List<InlineChoiceInteractionStatistics> statistics = new ArrayList<>();
		
		for(InlineChoiceInteraction interaction:interactions) {
			InlineChoiceInteractionStatistics interactionStatistics = new InlineChoiceInteractionStatistics(interaction);
			
			statistics.add(interactionStatistics);
			
			Identifier responseIdentifier = interaction.getResponseIdentifier();
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(responseIdentifier);
			if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
				Identifier correctResponseId = QtiNodesExtractor.getIdentifierFromCorrectResponse(responseDeclaration.getCorrectResponse());
				if(correctResponseId != null) {
					interactionStatistics.setCorrectResponseId(correctResponseId);
					String correctResponse = correctResponseId.toString();
	
					List<RawData> datas = getRawDatas(itemRefIdent, responseIdentifier.toString(), searchParams);
					for(RawData data:datas) {
						Long count = data.getCount();
						if(count != null && count.longValue() > 0) {
							String response = data.getStringuifiedResponse();
							if(response != null && response.length() >= 2 && response.startsWith("[") && response.endsWith("]")) {
								response = response.substring(1, response.length() - 1);
							}
							
							if(correctResponse.equals(response)) {
								interactionStatistics.addCorrect(1);
							} else {
								interactionStatistics.addIncorrect(1);
							}
						}
					}
				}
			}
		}
		
		return statistics;
	}
	
	private List<RawData> getRawDatas(String itemRefIdent, String responseIdentifier, QTI21StatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.key, aresponse.responseIdentifier, aresponse.stringuifiedResponse, count(aresponse.key) from qtiassessmentresponse aresponse ")
		  .append(" inner join aresponse.assessmentItemSession isession")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams, true);
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
	
	@Override
	public List<AssessmentItemStatistic> getStatisticPerItem(ResolvedAssessmentTest resolvedAssessmentTest, QTI21StatisticSearchParams searchParams,
			TestPart testPart, List<AssessmentSection> sections, double numOfParticipants) {
		StringBuilder sb = new StringBuilder();
		sb.append("select isession.assessmentItemIdentifier, isession.score, isession.manualScore, count(*) from qtiassessmentitemsession isession")
		  .append(" inner join isession.assessmentTestSession asession");
		decorateRSet(sb, searchParams, true);
		if(testPart != null) {
			sb.append(" and isession.testPartIdentifier=:testPartId");
		}
		if(sections != null && !sections.isEmpty()) {
			sb.append(" and isession.sectionIdentifier in (:sectionPartIds)");
		}
		
		sb.append(" and isession.duration > 0")
		  .append(" group by isession.assessmentItemIdentifier, isession.score, isession.manualScore");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		decorateRSetQuery(query, searchParams);
		if(testPart != null) {
			query.setParameter("testPartId", testPart.getIdentifier().toString());
		}
		if(sections != null && !sections.isEmpty()) {
			List<String> sectionIds = sections.stream()
					.map(sect -> sect.getIdentifier().toString()).collect(Collectors.toList());
			query.setParameter("sectionPartIds", sectionIds);
		}
		
		List<Object[]> results = query.getResultList();
		if(results.isEmpty()) {
			return new ArrayList<>();
		}
		
		Map<String,AssessmentItemRef> itemMap = new HashMap<>();
		List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs().stream()
				.filter(itemRef -> acceptAssessmentItem(itemRef, testPart, sections))
				.collect(Collectors.toList());
		for(AssessmentItemRef itemRef:itemRefs) {
			itemMap.put(itemRef.getIdentifier().toString(), itemRef);
		}

		Map<String, AssessmentItemHelper> identifierToHelpers = new HashMap<>();
		for(Object[] result:results) {
			int pos = 0;
			String identifier = PersistenceHelper.extractString(result, pos++);
			BigDecimal score = (BigDecimal)result[pos++];
			BigDecimal manualScore = (BigDecimal)result[pos++];
			Long count = PersistenceHelper.extractLong(result, pos++);
			if(score == null || identifier == null || count == null) {
				continue;
			}

			AssessmentItemHelper helper = identifierToHelpers.get(identifier);
			if(helper == null) {
				AssessmentItemRef itemRef = itemMap.get(identifier);
				if(itemRef == null) {
					continue;
				} 
				ResolvedAssessmentItem item = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				if(item == null) {
					continue;
				}
				helper = new AssessmentItemHelper(item.getRootNodeLookup().extractIfSuccessful());
				identifierToHelpers.put(identifier, helper);
			}
			
			helper.addCount(count);
			if(manualScore != null) {
				helper.addTotalScore(count, manualScore);
			} else {
				helper.addTotalScore(count, score);
			}

			if(helper.getMaxScore() != null) {
				double maxValue = helper.getMaxScore().doubleValue();
				if(Math.abs(score.doubleValue() - maxValue) < 0.0001) {
					helper.addCorrectAnswers(count);
				}
			}
		}
		
		List<AssessmentItemStatistic> statistics = new ArrayList<>(identifierToHelpers.size());
		for(AssessmentItemRef itemRef:itemRefs) {
			AssessmentItemHelper helper = identifierToHelpers.get(itemRef.getIdentifier().toString());
			if(helper != null) {
				long numOfAnswersItem = helper.count;
				long numOfCorrectAnswers = helper.countCorrectAnswers;
				double average = (helper.totalScore / helper.count);
				double averageParticipants = (helper.totalScore / numOfParticipants);
				statistics.add(new AssessmentItemStatistic(helper.getAssessmentItem(), average, averageParticipants, numOfAnswersItem, numOfCorrectAnswers));
			} else {
				ResolvedAssessmentItem item = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				if(item != null) {
					statistics.add(new AssessmentItemStatistic(item.getRootNodeLookup().extractIfSuccessful(), 0.0d, 0.0d, 0l, 0l));
				}
			}
		}
		return statistics;
	}
	
	private boolean acceptAssessmentItem(AssessmentItemRef itemRef, TestPart testPart, List<AssessmentSection> sections) {
		if(sections != null && !sections.isEmpty()) {
			for(AssessmentSection section:sections) {
				if(section.getIdentifier().equals(itemRef.getParentSection().getIdentifier())) {
					return true;
				}
			}
			return false;
		}
		if(testPart != null) {
			return testPart.getIdentifier().equals(itemRef.getEnclosingTestPart().getIdentifier());
		}
		return true;
	}
	
	public static class AssessmentItemHelper {
		private long count = 0l;
		private double totalScore = 0.0d;
		private Double maxScore;
		private long countCorrectAnswers = 0;
		private final AssessmentItem assessmentItem;
		
		public AssessmentItemHelper(AssessmentItem assessmentItem) {
			this.assessmentItem = assessmentItem;
			if(assessmentItem != null) {
				maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
			}
		}
		
		public AssessmentItem getAssessmentItem() {
			return assessmentItem;
		}
		
		public Double getMaxScore() {
			return maxScore;
		}
		
		public void addTotalScore(Long numOfAnswers, BigDecimal score) {
			if(numOfAnswers != null && score != null) {
				totalScore += (numOfAnswers.doubleValue() * score.doubleValue());
			}
		}
		
		public void addCount(Long toAdd) {
			if(toAdd != null) {
				count += toAdd.longValue();
			}
		}
		
		public void addCorrectAnswers(Long toAdd) {
			if(toAdd != null) {
				countCorrectAnswers += toAdd.longValue();
			}
		}
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