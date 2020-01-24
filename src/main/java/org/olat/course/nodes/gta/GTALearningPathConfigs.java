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
package org.olat.course.nodes.gta;

import org.olat.course.learningpath.model.ModuleLearningPathConfigs;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTALearningPathConfigs extends ModuleLearningPathConfigs {

	public GTALearningPathConfigs(ModuleConfiguration moduleConfiguration) {
		super(moduleConfiguration, false);
	}

	@Override
	public AssessmentObligation getObligation() {
		return moduleConfiguration.getBooleanSafe(MSCourseNode.CONFIG_KEY_OPTIONAL)
				? AssessmentObligation.optional
				: AssessmentObligation.mandatory;
	}

	@Override
	public void setObligation(AssessmentObligation obligation) {
		boolean valuea = obligation != null && AssessmentObligation.optional.equals(obligation);
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_OPTIONAL, valuea );
	}

}
