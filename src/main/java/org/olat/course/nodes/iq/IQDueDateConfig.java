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
package org.olat.course.nodes.iq;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.CoreSpringFactory;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * 
 * Initial date: 9 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQDueDateConfig {
	
	public static List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel(ModuleConfiguration config) {
		return List.of(
				Map.entry("test.start", getDueDateConfig(config, IQEditController.CONFIG_KEY_START_TEST_DATE)),
				Map.entry("test.end", getDueDateConfig(config, IQEditController.CONFIG_KEY_END_TEST_DATE)),
				Map.entry("test.results.start", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_START_DATE)),
				Map.entry("test.results.end", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_END_DATE)),
				Map.entry("test.results.failed.start", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE)),
				Map.entry("test.results.failed.end", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE)),
				Map.entry("test.results.passed.start", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE)),
				Map.entry("test.results.passed.end", getDueDateConfig(config, IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE))
			);
	}

	public static DueDateConfig getDueDateConfig(ModuleConfiguration config, String key) {
		if (IQEditController.CONFIG_KEY_START_TEST_DATE.equals(key)) {
			return config.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST)
					? DueDateConfig.ofModuleConfiguration(config,
							IQEditController.CONFIG_KEY_RELATIVE_DATES,
							IQEditController.CONFIG_KEY_START_TEST_DATE,
							IQEditController.CONFIG_KEY_START_TEST_DATE_REL,
							IQEditController.CONFIG_KEY_START_TEST_DATE_REL_TO)
					: DueDateConfig.noDueDateConfig();
		} else if (IQEditController.CONFIG_KEY_END_TEST_DATE.equals(key)) {
			return config.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST)
					? DueDateConfig.ofModuleConfiguration(config,
							IQEditController.CONFIG_KEY_RELATIVE_DATES,
							IQEditController.CONFIG_KEY_END_TEST_DATE,
							IQEditController.CONFIG_KEY_END_TEST_DATE_REL,
							IQEditController.CONFIG_KEY_END_TEST_DATE_REL_TO)
					: DueDateConfig.noDueDateConfig();
		} else if (IQEditController.CONFIG_KEY_RESULTS_START_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_START_DATE,
					IQEditController.CONFIG_KEY_RESULTS_START_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_START_DATE_REL_TO);
		} else if (IQEditController.CONFIG_KEY_RESULTS_END_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_END_DATE,
					IQEditController.CONFIG_KEY_RESULTS_END_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_END_DATE_REL_TO);
		} else if (IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE_REL_TO);
		} else if (IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE_REL_TO);
		} else if (IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE_REL_TO);
		} else if (IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE.equals(key)) {
			return DueDateConfig.ofModuleConfiguration(config,
					IQEditController.CONFIG_KEY_RELATIVE_DATES,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE_REL,
					IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE_REL_TO);
		}
		return null;
	}
	
	public static void postCopy(String courseNodeIdent, ModuleConfiguration config, CopyCourseContext context) {
		if (context != null) {
			long dateDifference = context.getDateDifference(courseNodeIdent);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_START_TEST_DATE);
			checkAndUpdateDate(config, dateDifference, IQEditController.CONFIG_KEY_END_TEST_DATE);
		}
	}
	
	private static void checkAndUpdateDate(ModuleConfiguration config, long dateDifference, String configKey) {
		Date dateToCheck = config.getDateValue(configKey);
		
		if (dateToCheck != null) {
			dateToCheck.setTime(dateToCheck.getTime() + dateDifference);
			config.setDateValue(configKey, dateToCheck);
		}
	}
	
	/**
	 * Evaluates if the results are visible or not in respect of the configured CONFIG_KEY_DATE_DEPENDENT_RESULTS parameter.<br>
	 * The results are always visible if not date dependent.
	 * EndDate could be null, that is there is no restriction for the end date.
	 * 
	 * 
	 * @param courseNode The QTI course node
	 * @param userCourseEnv The assessed user course environment
	 * @param passed If passed or not
	 * @param status The status of the assessment entry
	 * @return true if the results are visible to the assessed user
	 */
	public static boolean isResultVisibleBasedOnResults(QTICourseNode courseNode,
			UserCourseEnvironment userCourseEnv, boolean passed, AssessmentEntryStatus status) {
		String showResultsActive = courseNode.getModuleConfiguration()
				.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS);

		boolean isVisible;
		switch (showResultsActive) {
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS: {
				isVisible = AssessmentEntryStatus.done == status? true: false;
				break;
			}
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT: {
				Date passedStartDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
				Date passedEndDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
				Date failedStartDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
				Date failedEndDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
				isVisible = passed ? isNowVisible(passedStartDate, passedEndDate): isNowVisible(failedStartDate, failedEndDate);
				break;
			}
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY: {
				Date failedStartDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
				Date failedEndDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
				isVisible = !passed ? isNowVisible(failedStartDate, failedEndDate): false;
				break;
			}
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY: {
				Date passedStartDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
				Date passedEndDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
				isVisible = passed ? isNowVisible(passedStartDate, passedEndDate): false;
				break;
			}
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME: {
				Date startDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_START_DATE);
				Date endDate = getDueDate(courseNode, userCourseEnv, IQEditController.CONFIG_KEY_RESULTS_END_DATE);
				isVisible = isNowVisible(startDate, endDate);
				break;
			}
			default:
				isVisible = false;
				break;
		}

		return isVisible;
	}
	
	private static boolean isNowVisible(Date startDate, Date endDate) {
		boolean isVisible = true;
		Date currentDate = new Date();
		if (startDate != null && currentDate.before(startDate)) {
			isVisible &= false;
		} 
		if (endDate != null && currentDate.after(endDate)) {
			isVisible &= false;
		}
		return isVisible;
	}
	
	private static Date getDueDate(QTICourseNode courseNode, UserCourseEnvironment userCourseEnv, String configKey) {
		DueDateService dueDateService = CoreSpringFactory.getImpl(DueDateService.class);
		return dueDateService.getDueDate(
				courseNode.getDueDateConfig(configKey),
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				userCourseEnv.getIdentityEnvironment().getIdentity());
	}

}
