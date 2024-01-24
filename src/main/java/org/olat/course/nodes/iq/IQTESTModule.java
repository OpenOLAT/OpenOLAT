/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.iq;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: Nov 08, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class IQTESTModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String CONFIG_KEY_IQ_TEST_ENABLED = "iq.test.enabled";
	public static final String CONFIG_KEY_IQ_TEST_ENABLE_SCORE_INFO = "iq.test.enable.score.info";
	public static final String CONFIG_KEY_IQ_TEST_DATE_DEPENDENT_RESULTS = "iq.test.result.date.dependent.results";
	public static final String CONFIG_KEY_IQ_TEST_RESULT_ON_FINISH = "iq.test.result.on.finish";
	public static final String CONFIG_KEY_IQ_TEST_SUMMARY = "iq.test.summary";

	@Value("${iq.test.enabled:true}")
	private boolean enabled;
	@Value("${iq.test.enable.score.info:true}")
	private boolean isScoreInfoEnabled;
	@Value("${iq.test.result.date.dependent.results:no}")
	private String dateDependentResults;
	@Value("${iq.test.result.on.finish:false}")
	private boolean showResultOnFinish;
	@Value("${iq.test.summary:null}")
	private String qtiResultsSummary;

	public IQTESTModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
		setDefaultProperties();
	}

	public void updateProperties() {
		String enabledObj;

		enabledObj = getStringPropertyValue(CONFIG_KEY_IQ_TEST_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_IQ_TEST_ENABLE_SCORE_INFO, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isScoreInfoEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_IQ_TEST_DATE_DEPENDENT_RESULTS, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			dateDependentResults = enabledObj;
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_IQ_TEST_RESULT_ON_FINISH, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			showResultOnFinish = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_IQ_TEST_SUMMARY, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			qtiResultsSummary = enabledObj;
		}
	}

	public void setDefaultProperties() {
		setStringPropertyDefault(CONFIG_KEY_IQ_TEST_ENABLE_SCORE_INFO, isScoreInfoEnabled ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_IQ_TEST_DATE_DEPENDENT_RESULTS, dateDependentResults);
		setStringPropertyDefault(CONFIG_KEY_IQ_TEST_RESULT_ON_FINISH, showResultOnFinish ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_IQ_TEST_SUMMARY, qtiResultsSummary);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(CONFIG_KEY_IQ_TEST_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isScoreInfoEnabled() {
		return isScoreInfoEnabled;
	}

	public void setScoreInfoEnabled(boolean scoreInfoEnabled) {
		isScoreInfoEnabled = scoreInfoEnabled;
		setStringProperty(CONFIG_KEY_IQ_TEST_ENABLE_SCORE_INFO, Boolean.toString(scoreInfoEnabled), true);
	}

	public String getDateDependentResults() {
		return dateDependentResults;
	}

	public void setDateDependentResults(String dateDependentResults) {
		this.dateDependentResults = dateDependentResults;
		setStringProperty(CONFIG_KEY_IQ_TEST_DATE_DEPENDENT_RESULTS, dateDependentResults, true);
	}

	public boolean isShowResultOnFinish() {
		return showResultOnFinish;
	}

	public void setShowResultOnFinish(boolean showResultOnFinish) {
		this.showResultOnFinish = showResultOnFinish;
		setStringProperty(CONFIG_KEY_IQ_TEST_RESULT_ON_FINISH, Boolean.toString(showResultOnFinish), true);
	}

	public String getQtiResultsSummary() {
		return qtiResultsSummary;
	}

	public void setQtiResultsSummary(String qtiResultsSummary) {
		this.qtiResultsSummary = qtiResultsSummary;
		setStringProperty(CONFIG_KEY_IQ_TEST_SUMMARY, qtiResultsSummary, true);
	}

	public void resetProperties() {
		removeProperty(CONFIG_KEY_IQ_TEST_ENABLE_SCORE_INFO, true);
		removeProperty(CONFIG_KEY_IQ_TEST_DATE_DEPENDENT_RESULTS, true);
		removeProperty(CONFIG_KEY_IQ_TEST_RESULT_ON_FINISH, true);
		removeProperty(CONFIG_KEY_IQ_TEST_SUMMARY, true);
		updateProperties();
	}
}
