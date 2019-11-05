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
package org.olat.course.learningpath.evaluation;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.DateUtils;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.StartDateEvaluator;

/**
 * 
 * Initial date: 4 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigStartDateEvaluator implements StartDateEvaluator {

	@Override
	public void evaluate(CourseNode courseNode, Blocker blocker) {
		Date configStartDate = getConfigStartDate(courseNode);
		evaluateDate(configStartDate, blocker);
	}

	private Date getConfigStartDate(CourseNode courseNode) {
		LearningPathService learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		return learningPathService.getConfigs(courseNode).getStartDate();
	}

	Date evaluateDate(Date configStartDate, Blocker blocker) {
		Date now = new Date();
		if (configStartDate != null && configStartDate.after(now)) {
			Date later = DateUtils.getLater(configStartDate, blocker.getStartDate());
			blocker.block(later);
		}
		return configStartDate;
	}

}
