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

import org.olat.course.duedate.DueDateConfig;
import org.olat.modules.ModuleConfiguration;
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

}
