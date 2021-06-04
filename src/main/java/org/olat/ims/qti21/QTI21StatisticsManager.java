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
package org.olat.ims.qti21;

import java.util.List;

import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.AbstractTextEntryInteractionStatistics;
import org.olat.ims.qti21.model.statistics.AssessmentItemStatistic;
import org.olat.ims.qti21.model.statistics.ChoiceStatistics;
import org.olat.ims.qti21.model.statistics.HotspotChoiceStatistics;
import org.olat.ims.qti21.model.statistics.KPrimStatistics;
import org.olat.ims.qti21.model.statistics.MatchStatistics;
import org.olat.ims.qti21.model.statistics.OrderStatistics;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.ims.qti21.model.statistics.StatisticsItem;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 24.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QTI21StatisticsManager {

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
	public StatisticAssessment getAssessmentStatistics(QTI21StatisticSearchParams searchParams, Double cutValue);
	
	
	public StatisticsItem getAssessmentItemStatistics(String itemRefIdent, double maxScore, QTI21StatisticSearchParams searchParams);
	
	public List<ChoiceStatistics> getChoiceInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, ChoiceInteraction choiceInteraction, QTI21StatisticSearchParams searchParams);
	
	public List<ChoiceStatistics> getHottextInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, HottextInteraction hottextInteraction, QTI21StatisticSearchParams searchParams);
	
	public List<OrderStatistics> getOrderInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, OrderInteraction choiceInteraction, QTI21StatisticSearchParams searchParams);
	
	public List<HotspotChoiceStatistics> getHotspotInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, HotspotInteraction hotspotInteraction, QTI21StatisticSearchParams searchParams);
	
	public List<KPrimStatistics> getKPrimStatistics(String itemRefIdent,
			AssessmentItem item, MatchInteraction interaction, QTI21StatisticSearchParams searchParams);
	
	public List<MatchStatistics> getMatchStatistics(String itemRefIdent,
			AssessmentItem item, MatchInteraction interaction, QTI21StatisticSearchParams searchParams);
	
	public List<AbstractTextEntryInteractionStatistics> getTextEntryInteractionsStatistic(String itemRefIdent,
			AssessmentItem item, List<TextEntryInteraction> interactions, QTI21StatisticSearchParams searchParams);
	
	/**
	 * 
	 * @param items
	 * @param searchParams
	 * @param numOfParticipants
	 * @return
	 */
	public List<AssessmentItemStatistic> getStatisticPerItem(ResolvedAssessmentTest resolvedAssessmentTest, QTI21StatisticSearchParams searchParams,
			double numOfParticipants);

}
