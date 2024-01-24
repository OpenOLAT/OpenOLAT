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
package org.olat.course.nodes.gta;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: Nov 06, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class GTAModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String CONFIG_KEY_GTA_ENABLED = "gta.enabled";
	public static final String CONFIG_KEY_GTASK_OBLIGATION = "gta.obligation";
	public static final String CONFIG_KEY_GTASK_ASSIGNMENT = "gta.assignment";
	public static final String CONFIG_KEY_GTASK_SUBMIT = "gta.submit";
	public static final String CONFIG_KEY_GTASK_REVIEW_AND_CORRECTION = "gta.review.and.correction";
	public static final String CONFIG_KEY_GTASK_REVISION_PERIOD = "gta.revision.period";
	public static final String CONFIG_KEY_GTASK_SAMPLE_SOLUTION = "gta.solution";
	public static final String CONFIG_KEY_GTASK_GRADING = "gta.grading";
	public static final String CONFIG_KEY_GTASK_COACH_ALLOWED_UPLOAD_TASKS = "gta.coach.allowed.upload.tasks";
	public static final String CONFIG_KEY_GTASK_COACH_ASSIGNMENT = "gta.coach.assignment";

	@Value("${gta.enabled:true}")
	private boolean enabled;
	@Value("${gta.obligation:true}")
	private boolean hasObligation;
	@Value("${gta.assignment:true}")
	private boolean hasAssignment;
	@Value("${gta.submit:true}")
	private boolean hasSubmission;
	@Value("${gta.review.and.correction:true}")
	private boolean hasReviewAndCorrection;
	@Value("${gta.revision.period:true}")
	private boolean hasRevisionPeriod;
	@Value("${gta.solution:true}")
	private boolean hasSampleSolution;
	@Value("${gta.grading:true}")
	private boolean hasGrading;
	@Value("${gta.coach.allowed.upload.tasks:false}")
	private boolean canCoachUploadTasks;
	@Value("${gta.coach.assignment:false}")
	private boolean canCoachAssign;

	public GTAModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
		setDefaultProperties();
	}

	public void updateProperties() {
		String enabledObj;

		enabledObj = getStringPropertyValue(CONFIG_KEY_GTA_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_OBLIGATION, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasObligation = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_ASSIGNMENT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasAssignment = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_SUBMIT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasSubmission = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_REVIEW_AND_CORRECTION, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasReviewAndCorrection = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_REVISION_PERIOD, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasRevisionPeriod = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_SAMPLE_SOLUTION, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasSampleSolution = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_GRADING, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasGrading = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_COACH_ALLOWED_UPLOAD_TASKS, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			canCoachUploadTasks = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_GTASK_COACH_ASSIGNMENT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			canCoachAssign = "true".equals(enabledObj);
		}
	}

	public void setDefaultProperties() {
		setStringPropertyDefault(CONFIG_KEY_GTASK_OBLIGATION, hasObligation ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_ASSIGNMENT, hasAssignment ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_SUBMIT, hasSubmission ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_REVIEW_AND_CORRECTION, hasReviewAndCorrection ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_REVISION_PERIOD, hasRevisionPeriod ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_SAMPLE_SOLUTION, hasSampleSolution ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_GRADING, hasGrading ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_COACH_ALLOWED_UPLOAD_TASKS, canCoachUploadTasks ? "true": "false");
		setStringPropertyDefault(CONFIG_KEY_GTASK_COACH_ASSIGNMENT, canCoachAssign ? "true": "false");
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
		setStringProperty(CONFIG_KEY_GTA_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean hasObligation() {
		return hasObligation;
	}

	public void setHasObligation(boolean hasObligation) {
		this.hasObligation = hasObligation;
		setStringProperty(CONFIG_KEY_GTASK_OBLIGATION, Boolean.toString(hasObligation), true);
	}

	public boolean hasAssignment() {
		return hasAssignment;
	}

	public void setHasAssignment(boolean hasAssignment) {
		this.hasAssignment = hasAssignment;
		setStringProperty(CONFIG_KEY_GTASK_ASSIGNMENT, Boolean.toString(hasAssignment), true);
	}

	public boolean hasSubmission() {
		return hasSubmission;
	}

	public void setHasSubmission(boolean hasSubmission) {
		this.hasSubmission = hasSubmission;
		setStringProperty(CONFIG_KEY_GTASK_SUBMIT, Boolean.toString(hasSubmission), true);
	}

	public boolean hasReviewAndCorrection() {
		return hasReviewAndCorrection;
	}

	public void setHasReviewAndCorrection(boolean hasReviewAndCorrection) {
		this.hasReviewAndCorrection = hasReviewAndCorrection;
		setStringProperty(CONFIG_KEY_GTASK_REVIEW_AND_CORRECTION, Boolean.toString(hasReviewAndCorrection), true);
	}

	public boolean hasRevisionPeriod() {
		return hasRevisionPeriod;
	}

	public void setHasRevisionPeriod(boolean hasRevisionPeriod) {
		this.hasRevisionPeriod = hasRevisionPeriod;
		setStringProperty(CONFIG_KEY_GTASK_REVISION_PERIOD, Boolean.toString(hasRevisionPeriod), true);
	}

	public boolean hasSampleSolution() {
		return hasSampleSolution;
	}

	public void setHasSampleSolution(boolean hasSampleSolution) {
		this.hasSampleSolution = hasSampleSolution;
		setStringProperty(CONFIG_KEY_GTASK_SAMPLE_SOLUTION, Boolean.toString(hasSampleSolution), true);
	}

	public boolean hasGrading() {
		return hasGrading;
	}

	public void setHasGrading(boolean hasGrading) {
		this.hasGrading = hasGrading;
		setStringProperty(CONFIG_KEY_GTASK_GRADING, Boolean.toString(hasGrading), true);
	}

	public boolean canCoachUploadTasks() {
		return canCoachUploadTasks;
	}

	public void setCanCoachUploadTasks(boolean canCoachUploadTasks) {
		this.canCoachUploadTasks = canCoachUploadTasks;
		setStringProperty(CONFIG_KEY_GTASK_COACH_ALLOWED_UPLOAD_TASKS, Boolean.toString(canCoachUploadTasks), true);
	}

	public boolean canCoachAssign() {
		return canCoachAssign;
	}

	public void setCanCoachAssign(boolean canCoachAssign) {
		this.canCoachAssign = canCoachAssign;
		setStringProperty(CONFIG_KEY_GTASK_COACH_ASSIGNMENT, Boolean.toString(canCoachAssign), true);
	}

	public void resetProperties() {
		removeProperty(CONFIG_KEY_GTASK_OBLIGATION, true);
		removeProperty(CONFIG_KEY_GTASK_ASSIGNMENT, true);
		removeProperty(CONFIG_KEY_GTASK_SUBMIT, true);
		removeProperty(CONFIG_KEY_GTASK_REVIEW_AND_CORRECTION, true);
		removeProperty(CONFIG_KEY_GTASK_REVISION_PERIOD, true);
		removeProperty(CONFIG_KEY_GTASK_SAMPLE_SOLUTION, true);
		removeProperty(CONFIG_KEY_GTASK_GRADING, true);
		removeProperty(CONFIG_KEY_GTASK_COACH_ALLOWED_UPLOAD_TASKS, true);
		removeProperty(CONFIG_KEY_GTASK_COACH_ASSIGNMENT, true);
		updateProperties();
	}
}
