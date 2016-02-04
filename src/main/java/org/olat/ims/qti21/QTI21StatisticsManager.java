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

import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.SimpleChoiceStatistics;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;

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
	public StatisticAssessment getAssessmentStatistics(QTI21StatisticSearchParams searchParams);
	
	
	public StatisticsItem getAssessmentItemStatistics(String itemRefIdent, double maxScore, QTI21StatisticSearchParams searchParams);
	
	public List<SimpleChoiceStatistics> getChoiceInteractionStatistics(String itemRefIdent,
			AssessmentItem assessmentItem, ChoiceInteraction choiceInteraction, QTI21StatisticSearchParams searchParams);

}
