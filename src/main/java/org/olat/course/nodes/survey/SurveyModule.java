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
package org.olat.course.nodes.survey;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.survey.ui.SurveyEditController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: Jan 15, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class SurveyModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String CONFIG_KEY_SURVEY_ENABLED = "survey.enabled";
	public static final String CONFIG_KEY_SURVEY_EXECUTION_OWNER = "survey.execution.owner";
	public static final String CONFIG_KEY_SURVEY_EXECUTION_COACH = "survey.execution.coach";
	public static final String CONFIG_KEY_SURVEY_EXECUTION_PARTICIPANT = "survey.execution.participant";
	public static final String CONFIG_KEY_SURVEY_EXECUTION_GUEST = "survey.execution.guest";
	public static final String CONFIG_KEY_SURVEY_REPORT_OWNER = "survey.report.owner";
	public static final String CONFIG_KEY_SURVEY_REPORT_COACH = "survey.report.coach";
	public static final String CONFIG_KEY_SURVEY_REPORT_PARTICIPANT = "survey.report.participant";
	public static final String CONFIG_KEY_SURVEY_REPORT_GUEST = "survey.report.guest";

	@Value("${survey.enabled:true}")
	private boolean enabled;
	@Value("${survey.execution.owner:false}")
	private boolean isExecutionOwner;
	@Value("${survey.execution.coach:false}")
	private boolean isExecutionCoach;
	@Value("${survey.execution.participant:true}")
	private boolean isExecutionParticipant;
	@Value("${survey.execution.guest:false}")
	private boolean isExecutionGuest;
	@Value("${survey.report.owner:true}")
	private boolean isReportOwner;
	@Value("${survey.report.coach:true}")
	private boolean isReportCoach;
	@Value("${survey.report.participant:false}")
	private boolean isReportParticipant;
	@Value("${survey.report.guest:false}")
	private boolean isReportGuest;


	public SurveyModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
		setDefaultProperties();
	}

	public void updateProperties() {
		String enabledObj;

		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_EXECUTION_OWNER, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isExecutionOwner = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_EXECUTION_COACH, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isExecutionCoach = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_EXECUTION_PARTICIPANT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isExecutionParticipant = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_EXECUTION_GUEST, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isExecutionGuest = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_REPORT_OWNER, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isReportOwner = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_REPORT_COACH, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isReportCoach = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_REPORT_PARTICIPANT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isReportParticipant = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_SURVEY_REPORT_GUEST, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			isReportGuest = "true".equals(enabledObj);
		}
	}

	public void setDefaultProperties() {
		setStringPropertyDefault(CONFIG_KEY_SURVEY_EXECUTION_OWNER, isExecutionOwner ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_EXECUTION_COACH, isExecutionCoach ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_EXECUTION_PARTICIPANT, isExecutionParticipant ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_EXECUTION_GUEST, isExecutionGuest ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_REPORT_OWNER, isReportOwner ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_REPORT_COACH, isReportCoach ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_REPORT_PARTICIPANT, isReportParticipant ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_SURVEY_REPORT_GUEST, isReportGuest ? "true" : "false");
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
		setStringProperty(CONFIG_KEY_SURVEY_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isExecutionOwner() {
		return isExecutionOwner;
	}

	public void setExecutionOwner(boolean executionOwner) {
		isExecutionOwner = executionOwner;
		setStringProperty(CONFIG_KEY_SURVEY_EXECUTION_OWNER, Boolean.toString(executionOwner), true);
	}

	public boolean isExecutionCoach() {
		return isExecutionCoach;
	}

	public void setExecutionCoach(boolean executionCoach) {
		isExecutionCoach = executionCoach;
		setStringProperty(CONFIG_KEY_SURVEY_EXECUTION_COACH, Boolean.toString(executionCoach), true);
	}

	public boolean isExecutionParticipant() {
		return isExecutionParticipant;
	}

	public void setExecutionParticipant(boolean executionParticipant) {
		isExecutionParticipant = executionParticipant;
		setStringProperty(CONFIG_KEY_SURVEY_EXECUTION_PARTICIPANT, Boolean.toString(executionParticipant), true);
	}

	public boolean isExecutionGuest() {
		return isExecutionGuest;
	}

	public void setExecutionGuest(boolean executionGuest) {
		isExecutionGuest = executionGuest;
		setStringProperty(CONFIG_KEY_SURVEY_EXECUTION_GUEST, Boolean.toString(executionGuest), true);
	}

	public boolean isReportOwner() {
		return isReportOwner;
	}

	public void setReportOwner(boolean reportOwner) {
		isReportOwner = reportOwner;
		setStringProperty(CONFIG_KEY_SURVEY_REPORT_OWNER, Boolean.toString(reportOwner), true);
	}

	public boolean isReportCoach() {
		return isReportCoach;
	}

	public void setReportCoach(boolean reportCoach) {
		isReportCoach = reportCoach;
		setStringProperty(CONFIG_KEY_SURVEY_REPORT_COACH, Boolean.toString(reportCoach), true);
	}

	public boolean isReportParticipant() {
		return isReportParticipant;
	}

	public void setReportParticipant(boolean reportParticipant) {
		isReportParticipant = reportParticipant;
		setStringProperty(CONFIG_KEY_SURVEY_REPORT_PARTICIPANT, Boolean.toString(reportParticipant), true);
	}

	public boolean isReportGuest() {
		return isReportGuest;
	}

	public void setReportGuest(boolean reportGuest) {
		isReportGuest = reportGuest;
		setStringProperty(CONFIG_KEY_SURVEY_REPORT_GUEST, Boolean.toString(reportGuest), true);
	}

	public void resetProperties() {
		removeProperty(CONFIG_KEY_SURVEY_EXECUTION_OWNER, true);
		removeProperty(CONFIG_KEY_SURVEY_EXECUTION_COACH, true);
		removeProperty(CONFIG_KEY_SURVEY_EXECUTION_PARTICIPANT, true);
		removeProperty(CONFIG_KEY_SURVEY_EXECUTION_GUEST, true);
		removeProperty(CONFIG_KEY_SURVEY_REPORT_OWNER, true);
		removeProperty(CONFIG_KEY_SURVEY_REPORT_COACH, true);
		removeProperty(CONFIG_KEY_SURVEY_REPORT_PARTICIPANT, true);
		removeProperty(CONFIG_KEY_SURVEY_REPORT_GUEST, true);
		updateProperties();
	}

	public NodeRightType getExecutionNodeRightType() {
		return NodeRightTypeBuilder.ofIdentifier("execution")
				.setLabel(SurveyEditController.class, "edit.execution")
				.addRole(NodeRightGrant.NodeRightRole.owner, isExecutionOwner)
				.addRole(NodeRightGrant.NodeRightRole.coach, isExecutionCoach)
				.addRole(NodeRightGrant.NodeRightRole.participant, isExecutionParticipant)
				.addRole(NodeRightGrant.NodeRightRole.guest, isExecutionGuest)
				.build();
	}

	public NodeRightType getReportNodeRightType() {
		return NodeRightTypeBuilder.ofIdentifier("report")
				.setLabel(SurveyEditController.class, "edit.report")
				.addRole(NodeRightGrant.NodeRightRole.owner, isReportOwner)
				.addRole(NodeRightGrant.NodeRightRole.coach, isReportCoach)
				.addRole(NodeRightGrant.NodeRightRole.participant, isReportParticipant)
				.addRole(NodeRightGrant.NodeRightRole.guest, isReportGuest)
				.build();
	}
}
