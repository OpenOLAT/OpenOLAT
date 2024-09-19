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
package org.olat.course.nodes.cns.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.CNSEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSPreviewEnvironment implements CNSEnvironment {
	
	private static final AssessmentEvaluation EVALUATION = new AssessmentEvaluation(null, null, null, null, null, null,
			null, null, null, null, null, null, null, AssessmentEntryStatus.notStarted, null, null, null, null, null,
			null, null, null, null, 0, null, null, null, null, null, null,
			ObligationOverridable.of(AssessmentObligation.mandatory), null, null, null);
	
	private final Set<String> selectedNodeIdents = new HashSet<>(3);

	@Override
	public void select(CourseNode selectedCourseNode) {
		selectedNodeIdents.add(selectedCourseNode.getIdent());
	}

	@Override
	public Map<String, AssessmentEvaluation> getNodeIdentToAssessmentEvaluation(List<CourseNode> courseNodes) {
		Map<String, AssessmentEvaluation> nodeIdentToEvaluations = new HashMap<>(selectedNodeIdents.size());
		selectedNodeIdents.forEach(ident -> nodeIdentToEvaluations.put(ident, EVALUATION));
		return nodeIdentToEvaluations;
	}

}
