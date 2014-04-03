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
package org.olat.ims.qti.statistics;

import java.util.List;

import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.statistics.model.QTIStatisticResult;
import org.olat.ims.qti.statistics.model.QTIStatisticResultSet;
import org.olat.ims.qti.statistics.model.StatisticAnswerOption;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticChoiceOption;
import org.olat.ims.qti.statistics.model.StatisticFIBOption;
import org.olat.ims.qti.statistics.model.StatisticItem;
import org.olat.ims.qti.statistics.model.StatisticKPrimOption;
import org.olat.ims.qti.statistics.model.StatisticSurveyItem;
import org.olat.ims.qti.statistics.model.StatisticsItem;

/**
 * 
 * @author srosse
 *
 */
public interface QTIStatisticsManager {

	/*
	 * minimal number of results we need for a test, or for a question..
	 * 
	 * (if number of participants in test is smaller, don't show test) (if
	 * results for a specific question is smaller, don't display question)
	 */
	public static final int MIN_RESULTS_TEST = 2;
	public static final int MIN_RESULTS_QUESTION = 1;
	

	public static final int AVG_NUM_OF_SCORE_BUCKETS = 10;

	/**
	 * Return the statistics of a test in a course
	 * @param courseResID
	 * @param resSubPath
	 * @return
	 */
	public StatisticAssessment getAssessmentStatistics(QTIStatisticSearchParams searchParams);
	
	/**
	 * 
	 * @param courseResID
	 * @param resSubPath
	 * @return
	 */
	public List<QTIStatisticResultSet> getAllResultSets(QTIStatisticSearchParams searchParams);
	
	/**
	 * 
	 * @param searchParams
	 * @return
	 */
	public List<QTIStatisticResult> getResults(QTIStatisticSearchParams searchParams);
	
	/**
	 * 
	 * @param items
	 * @param searchParams
	 * @param numOfParticipants
	 * @return
	 */
	public List<StatisticItem> getStatisticPerItem(List<Item> items, QTIStatisticSearchParams searchParams,
			double numOfParticipants);

	/**
	 * 
	 * @param itemIdent
	 * @param searchParams
	 * @return
	 */
	public List<StatisticAnswerOption> getStatisticAnswerOptionsOfItem(String itemIdent, QTIStatisticSearchParams searchParams);
	
	public List<StatisticFIBOption> getStatisticAnswerOptionsFIB(Item itemIdent, QTIStatisticSearchParams searchParams);
	
	/**
	 * 
	 * @param itemIdent
	 * @param courseResID
	 * @param resSubPath
	 * @param maxScore
	 * @return
	 */
	public StatisticsItem getItemStatistics(String itemIdent, double maxScore, QTIStatisticSearchParams searchParams);

	
	/**
	 * calculates how many participants selected answer option 1 and/or option 2
	 * and/or option 3...
	 * 
	 * @param aQuestion
	 * @param olatResource
	 * @param olatResourceDetail
	 * @return
	 */
	public List<StatisticChoiceOption> getNumOfAnswersPerSingleChoiceAnswerOption(Item item, QTIStatisticSearchParams searchParams);
	
	
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
	public List<StatisticChoiceOption> getNumOfRightAnsweredMultipleChoice(Item item, QTIStatisticSearchParams searchParams);
	
	
	public List<StatisticKPrimOption> getNumbersInKPrim(Item item, QTIStatisticSearchParams searchParams);
	
	/**
	 * The returned list is ordered like the items given as parameter.
	 * @param searchParams
	 * @param items
	 * @return
	 */
	public List<StatisticSurveyItem> getStatisticAnswerOptions(QTIStatisticSearchParams searchParams, List<Item> items);
	
	public List<String> getAnswers(String itemIdent, QTIStatisticSearchParams searchParams);
	

}
