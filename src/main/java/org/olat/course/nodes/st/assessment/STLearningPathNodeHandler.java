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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathEditConfigs;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class STLearningPathNodeHandler implements LearningPathNodeHandler {

	@Override
	public String acceptCourseNodeType() {
		return STCourseNode.TYPE;
	}

	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public void updateDefaultConfigs(CourseNode courseNode, boolean newNode, INode parent) {
		getLearningPathConfigs(courseNode, parent);
	}

	@Override
	public LearningPathConfigs getConfigs(CourseNode courseNode) {
		return getLearningPathConfigs(courseNode, courseNode.getParent());
	}
	
	@Override
	public LearningPathConfigs getConfigs(CourseNode courseNode, INode parent) {
		return getLearningPathConfigs(courseNode, parent);
	}

	private LearningPathConfigs getLearningPathConfigs(CourseNode courseNode, INode parent) {
		STLearningPathConfigs configs = new STLearningPathConfigs(courseNode.getModuleConfiguration(), parent);
		configs.updateDefaults(parent);
		return configs;
	}

	@Override
	public Controller createConfigEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode) {
		return new STLearningPathConfigController(ureq, wControl, courseEntry, courseNode);
	}

	@Override
	public LearningPathEditConfigs getEditConfigs() {
		return null;
	}

	@Override
	public void onMigrated(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode)courseNode;
			ModuleConfiguration config = stCourseNode.getModuleConfiguration();
			config.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, false);
			stCourseNode.setScoreCalculator(null);
			if (stCourseNode.getParent() == null) {
				config.setStringValue(STCourseNode.CONFIG_SCORE_KEY, STCourseNode.CONFIG_SCORE_VALUE_SUM);
				config.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			}
			// CONFIG_LP_SEQUENCE_KEY was accidentally set in conventional courses.
			config.remove(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY);
		}
	}

}
