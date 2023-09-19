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
package org.olat.course.nodes.pf;

import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.PFCourseNode;

/**
 * 
 * Initial date: 18 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PFAssessmentConfig extends ModuleAssessmentConfig {
	
	public PFAssessmentConfig(PFCourseNode courseNode) {
		super(courseNode.getModuleConfiguration());
	}
	
	@Override
	public boolean isAssessable() {
		return true;
	}
	
	@Override
	public Mode getScoreMode() {
		return Mode.none;
	}
	
	@Override
	public Mode getPassedMode() {
		return Mode.none;
	}
	
	@Override
	public boolean ignoreInCourseAssessment() {
		return false;
	}
	
	@Override
	public boolean hasMaxAttempts() {
		return false;
	}

	@Override
	public Integer getMaxAttempts() {
		return null;
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return Boolean.TRUE;
	}

	@Override
	public boolean isEditable() {
		// manual scoring fields can be edited manually
		return false;
	}

	@Override
	public boolean isBulkEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return true;
	}
	
	@Override
	public boolean hasAssessmentForm() {
		return false;
	}
	
	@Override
	public boolean hasAssessmentStatistics() {
		return false;
	}
}
