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
package org.olat.course.nodes.st.assessment;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLearningPathConfigs implements LearningPathConfigs {
	
	private static final Set<AssessmentObligation> AVAILABLE_OBLIGATIONS = 
			Set.of(AssessmentObligation.evaluated, AssessmentObligation.excluded);
	
	static final String CONFIG_VERSION = "lp.st.configversion";
	static final int VERSION_CURRENT = 1;
	public static final String CONFIG_LP_SEQUENCE_KEY = "learning.path.sequence";
	public static final String CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL = "learning.path.sequence.sequential";
	public static final String CONFIG_LP_SEQUENCE_VALUE_WITHOUT = "learning.path.sequence.without";
	public static final String CONFIG_LP_SEQUENCE_DEFAULT = CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL;
	public static final String CONFIG_KEY_OBLIGATION = "lp.obligation";
	public static final String CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS = "lp.exeptional.obligations";
	
	private final ModuleConfiguration moduleConfigs;
	private final boolean isRoot;

	public STLearningPathConfigs(ModuleConfiguration moduleConfigs, INode parent) {
		this.moduleConfigs = moduleConfigs;
		this.isRoot = parent == null;
	}
	
	public void updateDefaults(INode parent) {
		int version = moduleConfigs.getIntegerSafe(CONFIG_VERSION, 0);
		
		if (version < 1) {
			// CONFIG_LP_SEQUENCE_KEY was set in STCourseNode.updateModuleConfigDefaults()
			// in the past (even for not lp nodes)
			if (!moduleConfigs.has(CONFIG_LP_SEQUENCE_KEY)) {
				STCourseNode stParent = STCourseNode.getFirstSTParent(parent);
				String sequence = stParent != null
						? stParent.getModuleConfiguration().getStringValue(CONFIG_LP_SEQUENCE_KEY, CONFIG_LP_SEQUENCE_DEFAULT)
						: CONFIG_LP_SEQUENCE_DEFAULT;
				moduleConfigs.setStringValue(CONFIG_LP_SEQUENCE_KEY, sequence);
			}
			moduleConfigs.set(CONFIG_KEY_OBLIGATION, AssessmentObligation.evaluated.name());
		}
		
		moduleConfigs.setIntValue(CONFIG_VERSION, VERSION_CURRENT);
	}

	@Override
	public Boolean hasSequentialChildren() {
		String sequenceConfig = moduleConfigs.getStringValue(CONFIG_LP_SEQUENCE_KEY);
		return CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL.equals(sequenceConfig);
	}
	
	@Override
	public Integer getDuration() {
		return null;
	}

	@Override
	public void setDuration(Integer duration) {
		// Duration is calculated. You can not set it in the course node.
	}

	@Override
	public Set<AssessmentObligation> getAvailableObligations() {
		return isRoot? Collections.emptySet(): AVAILABLE_OBLIGATIONS;
	}

	@Override
	public AssessmentObligation getObligation() {
		String config = moduleConfigs.getStringValue(CONFIG_KEY_OBLIGATION);
		return StringHelper.containsNonWhitespace(config)
				? AssessmentObligation.valueOf(config)
				: AssessmentObligation.evaluated;
	}

	@Override
	public void setObligation(AssessmentObligation obligation) {
		if (obligation != null) {
			moduleConfigs.setStringValue(CONFIG_KEY_OBLIGATION, obligation.name());
		} else {
			moduleConfigs.remove(CONFIG_KEY_OBLIGATION);
		}
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		return moduleConfigs.getList(CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS, ExceptionalObligation.class);
	}

	@Override
	public void setExceptionalObligations(List<ExceptionalObligation> exeptionalObligations) {
		moduleConfigs.setList(CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS, exeptionalObligations);
	}

	@Override
	public Date getStartDate() {
		return null;
	}

	@Override
	public void setStartDate(Date start) {
		//
	}

	@Override
	public Date getEndDate() {
		return null;
	}

	@Override
	public void setEndDate(Date end) {
		//
	}

	@Override
	public FullyAssessedTrigger getFullyAssessedTrigger() {
		return null;
	}

	@Override
	public void setFullyAssessedTrigger(FullyAssessedTrigger trigger) {
		//
	}
	
	@Override
	public Integer getScoreTriggerValue() {
		return null;
	}

	@Override
	public void setScoreTriggerValue(Integer score) {
		//
	}


	@Override
	public FullyAssessedResult isFullyAssessedOnNodeVisited() {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnConfirmation(boolean confirmed) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnScore(Float score, Boolean userVisibility) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnPassed(Boolean passed, Boolean userVisibility) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status) {
		return LearningPathConfigs.notFullyAssessed();
	}
}
