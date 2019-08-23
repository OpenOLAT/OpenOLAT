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
package org.olat.ims.qti.statistics.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.model.QTIStatisticResult;
import org.olat.ims.qti.statistics.model.QTIStatisticResultSet;
import org.olat.ims.qti.statistics.model.StatisticAnswerOption;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticChoiceOption;
import org.olat.ims.qti.statistics.model.StatisticFIBOption;
import org.olat.ims.qti.statistics.model.StatisticItem;
import org.olat.ims.qti.statistics.model.StatisticKPrimOption;
import org.olat.ims.qti.statistics.model.StatisticSurveyItem;
import org.olat.ims.qti.statistics.model.StatisticSurveyItemResponse;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTIStatisticsManagerImpl implements QTIStatisticsManager {
	
	@Autowired
	private DB dbInstance;
	
	private StringBuilder decorateRSet(StringBuilder sb, QTIStatisticSearchParams searchParams) {
		sb.append(" where rset.olatResource=:resourceId and rset.olatResourceDetail=:resSubPath")
		  .append(" and rset.lastModified = (select max(r2set.lastModified) from ").append(QTIStatisticResultSet.class.getName()).append(" r2set")
		  .append("   where r2set.identityKey=rset.identityKey and r2set.olatResource=rset.olatResource and r2set.olatResourceDetail=rset.olatResourceDetail")
		  .append(" )");
		
		if(searchParams.getLimitToGroups() != null && searchParams.getLimitToGroups().size() > 0) {
			sb.append(" and rset.identityKey in ( select membership.identity.key from bgroupmember membership ")
			  .append("   where membership.group in (:baseGroups)")
			  .append(" )");
		}
		
		if(searchParams.isMayViewAllUsersAssessments()) {
			sb.append(" and rset.identityKey in (select data.identity.key from assessmententry data ")
			  .append("   where data.repositoryEntry.key=rset.repositoryEntryKey and data.subIdent=rset.olatResourceDetail")
			  .append(" )");
		}
		return sb;
	}
	
	private void decorateRSetQuery(TypedQuery<?> query, QTIStatisticSearchParams searchParams) {
		query.setParameter("resourceId", searchParams.getResourceableId())
		     .setParameter("resSubPath", searchParams.getResSubPath());
		if(searchParams.getLimitToGroups() != null && searchParams.getLimitToGroups().size() > 0) {
			query.setParameter("baseGroups", searchParams.getLimitToGroups());
		}
	}

	@Override
	public StatisticAssessment getAssessmentStatistics(QTIStatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rset.score, rset.duration, rset.isPassed from ").append(QTIStatisticResultSet.class.getName()).append(" rset ");
		decorateRSet(sb, searchParams);
		sb.append(" order by rset.duration asc");

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
		double[] durationSecondes = new double[rawDatas.size()];
		
		double minDuration = Double.MAX_VALUE;
		double maxDuration = 0d;

		int dataPos = 0;
		for(Object[] rawData:rawDatas) {
			Boolean passed = (Boolean)rawData[2];
			if(passed != null) {
				if(passed.booleanValue()) {
					numOfPassed++;
				} else {
					numOfFailed++;
				}
			}
			
			Float score = (Float)rawData[0];
			if(score != null) {
				double scored = score.doubleValue();
				scores[dataPos] = scored;
				maxScore = Math.max(maxScore, scored);
				minScore = Math.min(minScore, scored);
			}
			
			Long duration = (Long)rawData[1];
			if(duration != null) {
				double durationd = duration.doubleValue();
				double durationSeconde = Math.round(durationd / 1000d);
				durationSecondes[dataPos] = durationSeconde;
				totalDuration += durationd;
				minDuration = Math.min(minDuration, durationSeconde);
				maxDuration = Math.max(maxDuration, durationSeconde);
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
		stats.setDurations(durationSecondes);
		stats.setScores(scores);
		return stats;
	}

	@Override
	public List<QTIStatisticResultSet> getAllResultSets(QTIStatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rset from qtistatsresultset rset ");
		decorateRSet(sb, searchParams);
		sb.append(" order by rset.duration asc");

		TypedQuery<QTIStatisticResultSet> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), QTIStatisticResultSet.class);
		decorateRSetQuery(query, searchParams);
		return query.getResultList();
	}

	@Override
	public List<QTIStatisticResult> getResults(QTIStatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams);

		TypedQuery<QTIStatisticResult> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QTIStatisticResult.class);
		decorateRSetQuery(query, searchParams);
		return query.getResultList();
	}

	@Override
	public List<StatisticItem> getStatisticPerItem(List<Item> items, QTIStatisticSearchParams searchParams,
			double numOfParticipants) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.itemIdent, res.score, count(res.key) from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams);
		sb.append(" group by res.itemIdent, res.score");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		decorateRSetQuery(query, searchParams);
		List<Object[]> results = query.getResultList();
		
		Map<String, StatisticItemHelper> itemToHelpers = new HashMap<>();
		for (Object[] result : results) {
			String itemIdent = (String)result[0];
			Float score = (Float)result[1];
			Long count = (Long)result[2];
			if(count == null || score == null || itemIdent == null) continue;

			StatisticItemHelper helper = itemToHelpers.get(itemIdent);
			if(helper == null) {
				helper = new StatisticItemHelper();
				itemToHelpers.put(itemIdent, helper);
			}
			
			helper.count += count.longValue();
			helper.totalScore += (count.longValue() * score.doubleValue());
			for (Item item:items) {
				if(item.getIdent().equals(itemIdent)) {
					double maxValue = item.getQuestion().getMaxValue();
					if(Math.abs(score.doubleValue() - maxValue) < 0.0001) {
						helper.countCorrectAnswers += count.longValue();
					}
				}
			}
		}

		List<StatisticItem> averages = new ArrayList<>();
		for (Item item:items) {
			StatisticItemHelper helper = itemToHelpers.get(item.getIdent());
			if(helper == null) {
				averages.add(new StatisticItem(item, -1.0, -1.0, -1, -1));
			} else {
				long numOfAnswersItem = helper.count;
				long numOfCorrectAnswers = helper.countCorrectAnswers;
				double average = (helper.totalScore / helper.count);
				double averageParticipants = (helper.totalScore / numOfParticipants);
				averages.add(new StatisticItem(item, average, averageParticipants, numOfAnswersItem, numOfCorrectAnswers));
			}
		}
		return averages;
	}
	
	private static class StatisticItemHelper {
		private long count;
		private double totalScore;
		private long countCorrectAnswers;
	}

	@Override
	public StatisticsItem getItemStatistics(String itemIdent, double maxScore, QTIStatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.score, count(res.key), avg(res.duration) from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams);
		sb.append(" and res.itemIdent=:itemIdent and res.duration > 0 group by res.score");

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
			long numOfResults = ((Long)result[1]).longValue();
			//average
			double score = ((Float)result[0]).doubleValue();
			totalScore += (score * numOfResults);
			totalResults += numOfResults;
			
			if((maxScore - score) < 0.0001) {
				numOfCorrectAnswers += numOfResults;
			} else {
				numOfIncorrectAnswers += numOfResults;
			}
			
			double averageDuration = ((Double)result[2]).doubleValue();
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

	
	/**
	 * calculates how many participants selected answer option 1 and/or option 2
	 * and/or option 3...
	 * 
	 * @param aQuestion
	 * @param olatResource
	 * @param olatResourceDetail
	 * @return
	 */
	@Override
	public List<StatisticChoiceOption> getNumOfAnswersPerSingleChoiceAnswerOption(Item item, QTIStatisticSearchParams searchParams) {
		List<StatisticAnswerOption> answerToNumberList = getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams);
		List<Response> answerOptions = item.getQuestion().getResponses();
		
		List<StatisticChoiceOption> numOfAnswersPerOption = new ArrayList<>();
		for(int i=0; i<answerOptions.size(); i++) {
			Response response = answerOptions.get(i);
			String responseIdent = response.getIdent();

			long num = 0;
			for(StatisticAnswerOption answerToNumber:answerToNumberList) {
				String answer = answerToNumber.getAnswer();
				if(answer.indexOf(responseIdent) >= 0) {
					num += answerToNumber.getCount();
				}
			}
			numOfAnswersPerOption.add(new StatisticChoiceOption(response, num));
		}
		return numOfAnswersPerOption;
	}
	
	
	/**
	 * calculates the percentage of participants that answered a answer option
	 * correctly.<br>
	 * Number at index 0 = answer option 1, Number at index 1 = answer option 2,
	 * etc.
	 * 
	 * @param item
	 * @param numberOfParticipants
	 * @param olatResource
	 * @param olatResourceDetail
	 * @return
	 */
	@Override
	public List<StatisticChoiceOption> getNumOfRightAnsweredMultipleChoice(Item item, QTIStatisticSearchParams searchParams) {
		List<StatisticAnswerOption> answerToNumberList = getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams);

		List<Response> responses = item.getQuestion().getResponses();
		List<StatisticChoiceOption> percentageRightAnswered = new ArrayList<StatisticChoiceOption>();
		for (Response response:responses) {
			String answerIdent = response.getIdent();
			long num = 0;
			for(StatisticAnswerOption answerToNumber:answerToNumberList) {
				String answer = answerToNumber.getAnswer();
				if(answer.indexOf(answerIdent) >= 0) {
					num += answerToNumber.getCount();
				}
			}
			percentageRightAnswered.add(new StatisticChoiceOption(response, num));
		}
		return percentageRightAnswered;
	}

	@Override
	public List<StatisticKPrimOption> getNumbersInKPrim(Item item, QTIStatisticSearchParams searchParams) {
		List<StatisticAnswerOption> rawDatas = getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams);
		List<Response> responses = item.getQuestion().getResponses();
		List<StatisticKPrimOption> kprimPoints = new ArrayList<>();
		for(Response response:responses) {
			String answerIdent = response.getIdent();
			boolean isCorrect = response.isCorrect();
			
			String rightFlag = answerIdent + ":" + (isCorrect ? "correct" : "wrong");
			String wrongFlag = answerIdent + ":" + (isCorrect ? "wrong" : "correct");
			
			long numCorrect = 0;
			long numIncorrect = 0;
			long numUnanswered = 0;
			for(StatisticAnswerOption rawData:rawDatas) {
				String answer = rawData.getAnswer();
				if(answer.indexOf(rightFlag) >= 0) {
					numCorrect += rawData.getCount();
				} else if(answer.indexOf(wrongFlag) >= 0) {
					numIncorrect += rawData.getCount();
				} else {
					numUnanswered += rawData.getCount();
				}
			}

			kprimPoints.add(new StatisticKPrimOption(response, numCorrect, numIncorrect, numUnanswered));
		}
		return kprimPoints;
	}

	@Override
	public List<StatisticFIBOption> getStatisticAnswerOptionsFIB(Item item, QTIStatisticSearchParams searchParams) {

		List<StatisticFIBOption> options = new ArrayList<>();
		Map<String,StatisticFIBOption> optionMap = new HashMap<>();
		
		boolean groupBy = true;
		
		List<Response> responses = item.getQuestion().getResponses();
		for(Response response:responses) {
			if(response instanceof FIBResponse) {
				FIBResponse fibResponse = (FIBResponse)response;
				if(FIBResponse.TYPE_BLANK.equals(fibResponse.getType())) {
					String ident = fibResponse.getIdent();
					String[] correctFIBs = fibResponse.getCorrectSynonyms();
					if(correctFIBs == null || correctFIBs.length == 0) {
						continue;
					}
					
					StatisticFIBOption option = new StatisticFIBOption();
					option.setCorrectBlank(correctFIBs[0]);
					option.setAlternatives(Arrays.asList(correctFIBs));
					boolean caseSensitive = "Yes".equals(fibResponse.getCaseSensitive());
					groupBy &= !caseSensitive;
					option.setCaseSensitive(caseSensitive);
					option.setPoints(fibResponse.getPoints());
					options.add(option);
					optionMap.put(ident, option);
				}
			}
		}
		
		
		List<StatisticAnswerOption> answerOptions = getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams, groupBy);
		
		for(StatisticAnswerOption answerOption:answerOptions) {
			long count = answerOption.getCount();
			String concatenedAnswer = answerOption.getAnswer();
			Map<String,String> parsedAnswerMap = QTIResultManager.parseResponseStrAnswers(concatenedAnswer);
			for(Map.Entry<String, String> parsedAnswerEntry: parsedAnswerMap.entrySet()) {
				String ident = parsedAnswerEntry.getKey();
				StatisticFIBOption option = optionMap.get(ident);
				if(option == null) {
					continue;
				}
				
				String text = parsedAnswerEntry.getValue();
				boolean correct;
				if(option.isCaseSensitive()) {
					correct = option.getAlternatives().contains(text);
				} else {
					correct = false;
					for(String alt:option.getAlternatives()) {
						if(alt.equalsIgnoreCase(text)) {
							correct = true;
						}
					}
				}
				
				if(correct) {
					option.setNumOfCorrect(option.getNumOfCorrect() + count);
				} else {
					option.setNumOfIncorrect(option.getNumOfIncorrect() + count);
					option.getWrongAnswers().add(text);
				}
			}
		}

		return options;
	}
	
	@Override
	public List<StatisticAnswerOption> getStatisticAnswerOptionsOfItem(String itemIdent, QTIStatisticSearchParams searchParams) {
		return getStatisticAnswerOptionsOfItem(itemIdent, searchParams, true);
	}
	
	private List<StatisticAnswerOption> getStatisticAnswerOptionsOfItem(String itemIdent, QTIStatisticSearchParams searchParams, boolean groupBy) {
		//the group by of mysql is case insensitive
		if(!groupBy && !dbInstance.getDbVendor().equals("mysql")) {
			groupBy = true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select res.answer, count(res.key) from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams);
		sb.append(" and res.itemIdent=:itemIdent and res.duration > 0 ");
		if(groupBy) {
			sb.append("group by res.answer");
		} else {
			sb.append("group by res.key");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
			.setParameter("itemIdent", itemIdent);
		decorateRSetQuery(query, searchParams);
		List<Object[]> results = query.getResultList();
		
		if(results.isEmpty()) {
			return Collections.emptyList();
		}

		List<StatisticAnswerOption> answerToNumberList = new ArrayList<>();
		for(Object[] result:results) {
			String answer = (String)result[0];
			Long numOfAnswers = (Long)result[1];
			answerToNumberList.add(new StatisticAnswerOption(answer, numOfAnswers.longValue()));
		}
		return answerToNumberList;
	}
	
	@Override
	public List<StatisticSurveyItem> getStatisticAnswerOptions(QTIStatisticSearchParams searchParams, List<Item> items) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.itemIdent, res.answer, count(res.key) from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams)
		  .append(" and res.duration > 0")
		  .append(" group by res.itemIdent, res.answer")
		  .append(" order by res.itemIdent");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		decorateRSetQuery(query, searchParams);
		List<Object[]> results = query.getResultList();
		
		if(results.isEmpty()) {
			return  Collections.emptyList();
		}
		
		Map<String, Item> identToItemMap = new HashMap<>();
		for(Item item:items) {
			identToItemMap.put(item.getIdent(), item);
		}

		StatisticSurveyItem currentItem = null;
		Map<Item, StatisticSurveyItem> itemToStatisticsMap = new HashMap<>();
		for(Object[] result:results) {
			String itemIdent = (String)result[0];
			String answer = (String)result[1];
			Long numOfAnswers = (Long)result[2];
			
			Item item = identToItemMap.get(itemIdent);
			if(currentItem == null || !currentItem.getItem().getIdent().equals(itemIdent)) {
				currentItem = new StatisticSurveyItem(item);
				itemToStatisticsMap.put(item, currentItem);
			}
			
			Response response = findResponses(item, answer);
			currentItem.getResponses().add(new StatisticSurveyItemResponse(response, answer, numOfAnswers));
		}
		
		List<StatisticSurveyItem> reorderList = new ArrayList<>();
		for(Item item:items) {
			StatisticSurveyItem statsItem = itemToStatisticsMap.get(item);
			if(statsItem != null) {
				reorderList.add(statsItem);
			}
		}
		return reorderList;
	}
	
	private Response findResponses(Item item, String answer) {
		List<Response> responses = item.getQuestion().getResponses();
		if(responses != null) {
			for(Response response:responses) {
				if(answer.indexOf(response.getIdent()) > 0) {
					return response;
				}
			}
		}
		return null;
	}

	@Override
	public List<String> getAnswers(String itemIdent, QTIStatisticSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select res.answer from qtistatsresult res ")
		  .append(" inner join res.resultSet rset");
		decorateRSet(sb, searchParams);
		sb.append(" and res.itemIdent=:itemIdent and res.duration > 0");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("itemIdent", itemIdent);
		decorateRSetQuery(query, searchParams);
		return query.getResultList();
	}
}